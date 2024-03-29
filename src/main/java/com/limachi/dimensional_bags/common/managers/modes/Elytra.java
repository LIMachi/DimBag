package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.items.IDimBagCommonItem;
import com.limachi.dimensional_bags.common.managers.Mode;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Elytra extends Mode {
    public static final ItemStack ENERGY_FIREWORK_ROCKET = new ItemStack(Items.FIREWORK_ROCKET);

    public Elytra() { super("Elytra", false, false); }

    @Override
    public ActionResultType onEntityTick(int eyeId, World world, Entity entity, boolean isSelected) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (player.isElytraFlying() && KeyMapController.KeyBindings.SNEAK_KEY.getState(player))
                player.setMotionMultiplier(player.world.getBlockState(player.getPosition()), new Vector3d(0.25D, 0.05D, 0.25D)); //not working
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    @Override
    public ActionResultType onItemUse(int eyeId, World world, PlayerEntity player, BlockRayTraceResult ray) { return ActionResultType.SUCCESS; }

    @Override
    public ActionResultType onActivateItem(int eyeId, PlayerEntity player) {
        if (!player.getItemStackFromSlot(EquipmentSlotType.CHEST).canElytraFly(player)) return ActionResultType.FAIL;
        if (!player.isElytraFlying()) {
            player.setOnGround(false);
            player.startFallFlying();
        }
        if (UpgradeManager.execute(eyeId, upgradeManager -> upgradeManager.getInstalledUpgrades().contains("upgrade_energy"), false)) {
            EnergyData energyData = EnergyData.getInstance(eyeId);
            if (energyData != null && energyData.extractEnergy(512, true) == 512)
                if (energyData.extractEnergy(512, false) == 512)
                    player.world.addEntity(new FireworkRocketEntity(player.world, ENERGY_FIREWORK_ROCKET, player));
        } else {
            IDimBagCommonItem.ItemSearchResult res = IDimBagCommonItem.searchItem(player, 10, FireworkRocketItem.class, (x)->{
                CompoundNBT nbt = x.getChildTag("Fireworks");
                return nbt == null || nbt.getList("Explosions", 10).size() == 0;
            }, false);
            if (res == null)
                res = IDimBagCommonItem.searchItem(player, 10, FireworkRocketItem.class, (x)->true, false);
            if (res != null) {
                player.world.addEntity(new FireworkRocketEntity(player.world, res.stack, player));
                if (!player.isCreative()) {
                    res.stack.shrink(1);
                    res.setStackDirty();
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRenderHud(int eyeId, boolean isSelected, PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {
        if (isSelected) {
            if (UpgradeManager.execute(eyeId, upgradeManager -> !upgradeManager.getInstalledUpgrades().contains("upgrade_energy"), true)) {
                IDimBagCommonItem.ItemSearchResult res = IDimBagCommonItem.searchItem(player, 10, FireworkRocketItem.class, (x) -> {
                    CompoundNBT nbt = x.getChildTag("Fireworks");
                    return nbt == null || nbt.getList("Explosions", 10).size() == 0;
                }, true);
                int total = 0;
                if (res != null)
                    for (ItemStack stack : res.stackList)
                        total += stack.getCount();
                RenderUtils.drawString(matrixStack, Minecraft.getInstance().fontRenderer, "Firework count: " + total, new Box2d(10, 20, 100, 10), 0xFFFFFFFF, true, false);
            }
        }
    }
}
