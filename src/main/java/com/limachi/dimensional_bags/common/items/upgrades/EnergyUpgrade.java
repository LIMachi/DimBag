package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.common.Config.Config;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@StaticInit
public class EnergyUpgrade extends BaseUpgrade {

    public static final String NAME = "energy_upgrade";

    @Config.Boolean(def = true, cmt = "can this upgrade be installed")
    public static boolean ACTIVE;
    @Config.Int(def = 5, cmt = "how many of this upgrade can be installed")
    public static int MAX_UPGRADES;
    @Config.Int(def = 10, cmt = "each new upgrade past the first will multiply the current storage by this amount")
    public static int MULTIPLIER;
    @Config.Long(def = 10000, cmt = "how much storage the first upgrade will add")
    public static int BASE_STORAGE;

    static { Registries.registerItem(NAME, EnergyUpgrade::new); }

    public EnergyUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public int getItemStackLimit(ItemStack stack) { return MAX_UPGRADES; }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    @Override
    public int installUpgrade(int eyeId, int qty) {
        long max_storage = BASE_STORAGE * (long)Math.pow(MULTIPLIER, MAX_UPGRADES - 1);
        EnergyData energyData = EnergyData.getInstance(eyeId);
        if (energyData.getMaxEnergyStored() == 0 && qty > 0 && MAX_UPGRADES > 0) {
            energyData.changeBatterySize(BASE_STORAGE);
            --qty;
        }
        while (qty > 0 && energyData.getMaxEnergyStored() < max_storage) {
            energyData.changeBatterySize(Math.min((long) energyData.getMaxEnergyStored() * MULTIPLIER, max_storage));
            --qty;
        }
        return qty;
    }

    @Override
    public void onRenderHud(int eyeId, PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {
        EnergyData.execute(eyeId, energyData->{
            RenderUtils.drawString(matrixStack, Minecraft.getInstance().fontRenderer, "Energy: " + energyData.getEnergyStored() + " / " + energyData.getMaxEnergyStored(), new Box2d(10, 10, 100, 10), 0xFFFFFFFF, true, false); return true;}, false);
    }
}
