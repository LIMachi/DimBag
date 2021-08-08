package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.common.tileentities.PadTileEntity;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.managers.Mode;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PokeBall extends Mode {
    public PokeBall() { super("PokeBall", false, true); }

    @Override
    public void initSettings(SettingsData.SettingsReader settingsReader) {
    }

    @Override
    public boolean onScroll(PlayerEntity player, int eye, boolean up) {
        if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) {
            SubRoomsManager.execute(eye, sm->sm.selectOtherPad(!up));
            return true;
        }
        return false;
    }

    @Override
    public ActionResultType onAttack(int eyeId, PlayerEntity player, Entity entity) {
        if (entity instanceof PlayerEntity || entity instanceof EnderDragonEntity || entity instanceof WitherEntity) return ActionResultType.SUCCESS;
        Entity r = SubRoomsManager.execute(eyeId, subRoomsManager -> subRoomsManager.captureEntity(entity), null);
        if (r == null && player instanceof ServerPlayerEntity)
            player.sendStatusMessage(new TranslationTextComponent("mode.error.no_valid_pad"), true);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onEntityTick(int eyeId, World world, Entity entity, boolean isSelected) {
        UpgradeManager manager = UpgradeManager.getInstance(eyeId);
        if (manager != null && world instanceof ServerWorld) {
            Entity e = getClosestNonPlayerEntityNextToEye(eyeId);
            manager.getUpgradesNBT().putString("PokeBall_closest_entity_name", e != null ? e.getDisplayName().getString() : "No entity close to this pad");
            manager.markDirty();
        }
        return ActionResultType.CONSUME;
    }

    private Entity getClosestNonPlayerEntityNextToEye(int id) { //TODO: should be rewritten to only accept pad providers
//        BlockPos eyePos = SubRoomsManager.getEyePos(id);
//        WorldUtils.getRiftWorld().getChunk(eyePos);
//        List<Entity> le = WorldUtils.getRiftWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(eyePos.add(-7, -7, -7), eyePos.add(7, 7, 7)), e->!(e instanceof PlayerEntity));
//        if (le.size() > 0)
//            le.sort((o1, o2) -> {
//                Vector3d v = new Vector3d(eyePos.getX() + 0.5, eyePos.getY() + 0.5, eyePos.getZ() + 0.5);
//                Vector3d d1 = o1.getPositionVec().subtract(v);
//                Vector3d d2 = o2.getPositionVec().subtract(v);
//                double sd1 = d1.x * d1.x + d1.y * d1.y + d1.z * d1.z;
//                double sd2 = d2.x * d2.x + d2.y * d2.y + d2.z * d2.z;
//                return Double.compare(sd1, sd2);
//            });
//        return le.size() > 0 ? le.get(0) : null;
        return SubRoomsManager.execute(id, sm->{
            PadTileEntity pad = sm.getSelectedPad();
            if (pad == null) return null;
            return pad.getEntity(Entity.class);
        }, null);
    }

    @Override
    public ActionResultType onItemUse(int eyeId, World world, PlayerEntity player, BlockRayTraceResult ray) {
        Entity target = getClosestNonPlayerEntityNextToEye(eyeId);
        if (target != null)
            SubRoomsManager.execute(eyeId, sm->sm.leaveBag(target, false, ray.getPos().offset(Direction.UP), world.getDimensionKey()));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onActivateItem(int eyeId, PlayerEntity player) {
        Entity target = getClosestNonPlayerEntityNextToEye(eyeId);
        if (target != null)
            SubRoomsManager.execute(eyeId, sm->sm.leaveBag(target, false, new BlockPos(player.getPositionVec().add(0, 1, 0).add(player.getLookVec().scale(5))), player.world.getDimensionKey()));
        return ActionResultType.SUCCESS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRenderHud(int eyeId, boolean isSelected, PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {
        if (isSelected)
            RenderUtils.drawString(matrixStack, Minecraft.getInstance().fontRenderer, "Target: " + UpgradeManager.execute(eyeId, upgradeManager -> upgradeManager.getUpgradesNBT().getString("PokeBall_closest_entity_name"), "No entity close to the eye"), new Box2d(10, 20, 100, 10), 0xFFFFFFFF, true, false);
    }
}
