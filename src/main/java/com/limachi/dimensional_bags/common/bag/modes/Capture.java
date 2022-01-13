package com.limachi.dimensional_bags.common.bag.modes;

import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagPad.PadTileEntity;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.upgrades.BagUpgradeManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Capture extends AbstractMode { //should use a better version to handle name sync
    public Capture() { super("Capture", false, false); }

    @Override
    public void initSettings(SettingsData.SettingsReader settingsReader) {
    }

    @Override
    public boolean onScroll(PlayerEntity player, int eye, boolean up, boolean testOnly) {
        if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) {
            if (!testOnly)
                SubRoomsManager.execute(eye, sm->sm.selectOtherPad(!up));
            return true;
        }
        return false;
    }

    @Override
    public ActionResultType onAttack(int bagId, PlayerEntity player, Entity entity) {
        if (entity instanceof PlayerEntity || entity instanceof EnderDragonEntity || entity instanceof WitherEntity) return ActionResultType.SUCCESS;
        Entity r = SubRoomsManager.execute(bagId, subRoomsManager -> subRoomsManager.captureEntity(entity), null);
        if (r == null && player instanceof ServerPlayerEntity)
            player.displayClientMessage(new TranslationTextComponent("mode.error.no_valid_pad", entity.getName().getString()), true);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onEntityTick(int bagId, World world, Entity entity) {
        BagUpgradeManager manager = BagUpgradeManager.getInstance(bagId);
        if (manager != null && world instanceof ServerWorld) {
            String[] padName = {"Pad"};
            Entity e = SubRoomsManager.execute(bagId, sm->{
                PadTileEntity pad = sm.getSelectedPad();
                if (pad == null) return null;
                padName[0] = pad.getName();
                return pad.getEntity(Entity.class);
            }, null);
            manager.getUpgradesNBT().putString("Capture_entity_name", padName[0] + " : " + (e != null ? e.getDisplayName().getString() : "No entity close to this pad"));
            manager.setDirty();
        }
        return ActionResultType.CONSUME;
    }

    private Entity getPadEntity(int id) {
        return SubRoomsManager.execute(id, sm->{
            PadTileEntity pad = sm.getSelectedPad();
            if (pad == null) return null;
            return pad.getEntity(Entity.class);
        }, null);
    }

    @Override
    public ActionResultType onItemUse(int bagId, World world, PlayerEntity player, BlockRayTraceResult ray) {
        Entity target = getPadEntity(bagId);
        if (target != null)
            SubRoomsManager.execute(bagId, sm->sm.leaveBag(target, false, ray.getBlockPos().above(), world.dimension(), null));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onActivateItem(int bagId, PlayerEntity player) {
        Entity target = getPadEntity(bagId);
        if (target != null)
            SubRoomsManager.execute(bagId, sm->sm.leaveBag(target, false, new BlockPos(player.position().add(0, 1, 0).add(player.getLookAngle().scale(5))), player.level.dimension(), null));
        return ActionResultType.SUCCESS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRenderHud(int bagId, boolean isSelected, PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {
        if (isSelected)
            RenderUtils.drawString(matrixStack, Minecraft.getInstance().font, BagUpgradeManager.execute(bagId, upgradeManager -> upgradeManager.getUpgradesNBT().getString("Capture_entity_name"), "Missing active pad"), new Box2d(10, 20, 100, 10), 0xFFFFFFFF, true, false);
    }
}
