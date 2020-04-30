package com.limachi.dimensional_bags.common.config;

import com.limachi.dimensional_bags.common.upgradesManager.UpgradeManager;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DimBagConfig {
    public static final int ROWS_ID = 0;
    public static final int COLUMNS_ID = 1;
    public static final int RADIUS_ID = 2;

    /*
    public static final UpgradeConfig upgrades[] = { //exact order of the config maters and should be maintained for compatibility of futur versions of the mod
            new UpgradeConfig("rows", true, 3, 9, 1, 14),
            new UpgradeConfig("columns", true, 9, 18, 1, 35),
            new UpgradeConfig("radius", true, 3, 31, 2, 126), //note: the actual maximum of an itemstack should be 127, so we are barely sage to manipulate up to 126 items
    };
    */

    private static final Config CONFIG;
    private static final ForgeConfigSpec SPEC;
    static {
        final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
        SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    public static final ForgeConfigSpec getSpec() { return SPEC; }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
        if (event.getConfig().getSpec() == DimBagConfig.SPEC)
            bakeConfig();
    }

    /*
    public static class UpgradeConfig {
        public int start;
        public int limit;
        public String id;

        private boolean canConfig;
        private final String cId;
        private int min;
        private int max;
        private ForgeConfigSpec.IntValue cStart;
        private ForgeConfigSpec.IntValue cLimit;

        UpgradeConfig(String id, boolean canConfig, int start, int limit, int min, int max) {
            this.id = id;
            this.cId = MOD_ID + ".config.upgrade." + id;
            this.canConfig = canConfig;
            this.start = start;
            this.min = min;
            this.limit = limit;
            this.max = max;
        }

        protected UpgradeConfig(String id, boolean canConfig) { this(id, canConfig, 0, 1, 0, 1); }

        void builConfig(ForgeConfigSpec.Builder builder) {
            if (this.canConfig) {
                this.cStart = builder.comment("initial amount of '" + this.id + "' upgradesManager").translation(this.cId + ".start").defineInRange(this.cId + ".start", this.start, this.min, this.max);
                this.cLimit = builder.comment("maximum amount of '" + this.id + "' upgradesManager").translation(this.cId + ".limit").defineInRange(this.cId + ".limit", this.limit, this.min, this.max);
            }
        }

        void bake() {
            if (this.canConfig) {
                this.start = this.cStart.get();
                this.limit = this.cLimit.get();
            }
        }
    }*/

    private static void bakeConfig() {
        UpgradeManager.bakeConfig();
        /*
        for (int i = 0; i < upgrades.length; ++i)
            upgrades[i].bake();
            */
    }

    private static class Config {
        Config(ForgeConfigSpec.Builder builder) {
            UpgradeManager.buildConfig(builder);
            /*
            for (int i = 0; i < upgrades.length; ++i)
                upgrades[i].builConfig(builder);
            */
        }
    }
}