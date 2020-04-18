package com.limachi.dimensional_bags.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DimBagConfig {
    public static final Config CONFIG;
    public static final ForgeConfigSpec SPEC;
    static {
        final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
        SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    public static int startingRadius;
    public static int maxRadius;
    public static int startingColumns;
    public static int maxCOlumns;
    public static int startingRows;
    public static int maxRows;

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
        if (event.getConfig().getSpec() == DimBagConfig.SPEC)
            bakeConfig();
    }

    private static void bakeConfig() {
        startingRadius = Config.startingRadius.get();
        maxRadius = Config.maxRadius.get();
        startingColumns = Config.startingColumns.get();
        maxCOlumns = Config.maxCOlumns.get();
        startingRows = Config.startingRows.get();
        maxRows = Config.maxRows.get();
    }

    static class Config {

        static ForgeConfigSpec.IntValue startingRadius;
        static ForgeConfigSpec.IntValue maxRadius;
        static ForgeConfigSpec.IntValue startingColumns;
        static ForgeConfigSpec.IntValue maxCOlumns;
        static ForgeConfigSpec.IntValue startingRows;
        static ForgeConfigSpec.IntValue maxRows;

        Config(ForgeConfigSpec.Builder builder) {
            startingRadius = builder.comment("starting radius of the bag (not counting the eye and walls)").translation(MOD_ID + ".config.bag.roomRadius").defineInRange("bag.roomRadius", 3, 2, 126);
            maxRadius = builder.comment("maximum radius of the bag (not counting the eye and walls)").translation(MOD_ID + ".config.bag.roomRadiusMax").defineInRange("bag.roomRadiusMax", 31, 2, 126);

            startingColumns = builder.comment("number of columns a new bag has").translation(MOD_ID + ".config.bag.columns").defineInRange("bag.columns", 9, 9, 18);
            maxCOlumns = builder.comment("maximum radius of columns a bag can have").translation(MOD_ID + ".config.bag.columnsMax").defineInRange("bag.columnsMax", 18, 9, 18);

            startingRows = builder.comment("number of rows a new bag has").translation(MOD_ID + ".config.bag.rows").defineInRange("bag.rows", 3, 1, 12);
            maxRows = builder.comment("maximum radius of rows a bag can have").translation(MOD_ID + ".config.bag.rowsMax").defineInRange("bag.rowsMax", 12, 1, 12);
        }
    }
}