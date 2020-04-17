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
    public static int startingColumns = 9;
    public static int maxCOlumns = 9;
    public static int startingRows = 3;
    public static int maxRows = 3;

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
        if (event.getConfig().getSpec() == DimBagConfig.SPEC)
            bakeConfig();
    }

    private static void bakeConfig() {
        startingRadius = Config.startingRadius.get();
        maxRadius = Config.maxRadius.get();
//        listOfRadius = new ArrayList<>(6);
//        listOfRows = new ArrayList<>(6);
//        listOfColumns = new ArrayList<>(6);
//        for (int i = 0; i < 6; ++i) {
//            listOfRadius.add(CONFIG.bags.get(i).roomRadius.get());
//            listOfRows.add(CONFIG.bags.get(i).rows.get());
//            listOfColumns.add(CONFIG.bags.get(i).columns.get());
//        }
    }

    static class Config {

        static ForgeConfigSpec.IntValue startingRadius;
        static ForgeConfigSpec.IntValue maxRadius;
        static ForgeConfigSpec.IntValue startingColumns;
        static ForgeConfigSpec.IntValue maxCOlumns;
        static ForgeConfigSpec.IntValue startingRows;
        static ForgeConfigSpec.IntValue maxRows;

//        static final ArrayList<Bag> bags = new ArrayList<>(6);
//
//        static class Bag {
//            final ForgeConfigSpec.IntValue roomRadius;
//            final ForgeConfigSpec.IntValue rows;
//            final ForgeConfigSpec.IntValue columns;
//            int tier;
//
//            Bag(ForgeConfigSpec.Builder builder, int tierIn) {
//                tier = tierIn;
//                builder.push("Bag Tier " + tier);
//                roomRadius = builder
//                        .comment("radius of the room inside the bag, not counting the eye and walls")
//                        .translation(MOD_ID + ".config.bag_tier_" + tier + ".roomRadius")
//                        .defineInRange("bag_tier_" + tier + ".roomRadius", 3 + 2 * tier, 2, 126);
//                rows = builder
//                        .comment("number of rows of items the bag can contain")
//                        .translation(MOD_ID + ".config.bag_tier_" + tier + ".rows")
//                        .defineInRange("bag_tier_" + tier + ".rows", 3 + 2 * tier, 2, 15);
//                columns = builder
//                        .comment("number of columns of items the bag can contain")
//                        .translation(MOD_ID + ".config.bag_tier_" + tier + ".columns")
//                        .defineInRange("bag_tier_" + tier + ".columns", 9 + tier, 9, 18);
//                builder.pop();
//            }
//        }
//
//        Config(ForgeConfigSpec.Builder builder) {
//            for (int i = 0; i < 6; ++i)
//                bags.add(new Bag(builder, i));
//        }
        Config(ForgeConfigSpec.Builder builder) {
            startingRadius = builder.comment("starting radius of the bag (not counting the eye and walls)").translation(MOD_ID + ".config.bag.roomRadius").defineInRange("bag.roomRadius", 3, 2, 126);
            maxRadius = builder.comment("maximum radius of the bag (not counting the eye and walls)").translation(MOD_ID + ".config.bag.roomRadiusMax").defineInRange("bag.roomRadiusMax", 31, 2, 126);
        }
    }
}