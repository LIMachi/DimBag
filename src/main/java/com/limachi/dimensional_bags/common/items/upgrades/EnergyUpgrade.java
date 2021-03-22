package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

@StaticInit
public class EnergyUpgrade extends BaseUpgrade {

    public static final String NAME = "energy_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;
    @Config(cmt = "how many of this upgrade can be installed")
    public static int MAX_UPGRADES = 5;
    @Config(cmt = "each new upgrade past the first will multiply the current storage by this amount")
    public static int MULTIPLIER = 10;
    @Config(cmt = "how much storage the first upgrade will add")
    public static int BASE_STORAGE = 10000;

    static {
        Registries.registerItem(NAME, EnergyUpgrade::new);
        UpgradeManager.registerUpgrade(NAME, new EnergyUpgrade());
    }

    public EnergyUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public int getMaxCount() { return MAX_UPGRADES; }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    @Override
    public int installUpgrade(UpgradeManager manager, int qty) {
        int installed = 0;
        long max_storage = BASE_STORAGE * (long)Math.pow(MULTIPLIER, MAX_UPGRADES - 1);
        EnergyData energyData = EnergyData.getInstance(manager.getEyeId());
        if (energyData.getMaxEnergyStored() == 0 && qty > 0 && MAX_UPGRADES > 0) {
            energyData.changeBatterySize(BASE_STORAGE);
            ++installed;
        }
        while (installed < qty && energyData.getMaxEnergyStored() < max_storage) {
            energyData.changeBatterySize(Math.min((long) energyData.getMaxEnergyStored() * MULTIPLIER, max_storage));
            ++installed;
        }
        return installed;
    }

    @Override
    public void onRenderHud(int eyeId, PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {
        EnergyData.execute(eyeId, energyData->{
            RenderUtils.drawString(matrixStack, Minecraft.getInstance().fontRenderer, "Energy: " + energyData.getEnergyStored() + " / " + energyData.getMaxEnergyStored(), new Box2d(10, 10, 100, 10), 0xFFFFFFFF, true, false); return true;}, false);
    }
}
