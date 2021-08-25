package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
/*
public class EnergyUpgrade extends Upgrade {
    public EnergyUpgrade() { super("energy", true, 0, 4, 0, 10); }

    @Override
    public void installUpgrade(int eyeId, ItemStack stack, int amount, boolean simulate) {
        if (!simulate) {
            EnergyData energyData = EnergyData.getInstance(eyeId); //FIXME: got null once in a modpack, reloading the world fixed the missing file
            int previousStorage = energyData.getMaxEnergyStored();
            if (previousStorage == 0 && amount > 0) {
                energyData.changeBatterySize(65536);
                --amount;
            }
            if (amount > 0)
                energyData.changeBatterySize(energyData.getMaxEnergyStored() * (int)Math.pow(4, amount));
        }
    }

    @Override
    public void onRenderHud(int eyeId, PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {
        EnergyData.execute(eyeId, energyData->{RenderUtils.drawString(matrixStack, Minecraft.getInstance().font, "Energy: " + energyData.getEnergyStored() + " / " + energyData.getMaxEnergyStored(), new Box2d(10, 10, 100, 10), 0xFFFFFFFF, true, false); return true;}, false);
    }
}*/
