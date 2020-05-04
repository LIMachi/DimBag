package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ConfigBuilder CONFIG;
    private static final ForgeConfigSpec SPEC;
    static {
        final Pair<ConfigBuilder, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ConfigBuilder::new);
        SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    public static final ForgeConfigSpec getSpec() { return SPEC; }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
        if (event.getConfig().getSpec() == Config.SPEC)
            bakeConfig();
    }

    private static void bakeConfig() {
        UpgradeManager.bakeConfig();
    }

    private static class ConfigBuilder {
        ConfigBuilder(ForgeConfigSpec.Builder builder) {
            UpgradeManager.buildConfig(builder);
        }
    }
}
