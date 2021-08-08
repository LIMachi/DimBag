package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.common.container.PillarContainer;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.Mode;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class Default extends Mode {

    public static final String ID = "Default";

    public Default() { super(ID, true, true); } //will always be called last (if no mode consumed the event first)

    @Override
    public void initSettings(SettingsData.SettingsReader settingsReader) {
        settingsReader.bool("should_show_energy", true);
    }

    @Override
    public ActionResultType onItemUse(int eyeId, World world, PlayerEntity player, BlockRayTraceResult ray) { //called when the bag is right clicked on something, before the bag does anything
        if (player == null || !KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) return ActionResultType.PASS; //only test a player croushing
        int x = Math.abs(ray.getPos().getX() - player.getPosition().getX());
        int y = Math.abs(ray.getPos().getY() - player.getPosition().getY());
        int z = Math.abs(ray.getPos().getZ() - player.getPosition().getZ());
        if (x > 1 || y > 2 || z > 1) return ActionResultType.PASS; //only validate if the click is close enough to the player
        Bag.unequippedBags(player, eyeId, ray.getPos().up(1));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onActivateItem(int eyeId, PlayerEntity player) {
        if (!KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) {
            if (player instanceof ServerPlayerEntity)
//                Network.openEyeInventory((ServerPlayerEntity) player, eyeId, null);
                new PillarContainer(0, player.inventory, eyeId, null).open(player);
                ; //FIXME
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
    @Override
    public void onRenderHud(int eyeId, boolean isSelected, PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {
        if (getSetting(eyeId, "should_show_energy"))
            EnergyData.execute(eyeId, energyData->{
                if (energyData.getMaxEnergyStored() > 0 || energyData.getEnergyStored() > 0)
                    RenderUtils.drawString(matrixStack, Minecraft.getInstance().fontRenderer, "Energy: " + energyData.getEnergyStored() + " / " + energyData.getMaxEnergyStored(), new Box2d(10, 10, 100, 10), 0xFFFFFFFF, true, false);});
    }
}
