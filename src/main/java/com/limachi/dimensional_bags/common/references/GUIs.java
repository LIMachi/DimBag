package com.limachi.dimensional_bags.common.references;

import net.minecraft.util.ResourceLocation;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class GUIs { //common resources and size/position information for screens and containers
    public static class ScreenParts {
        public static final int PLAYER_INVENTORY_X = 174;
        public static final int PLAYER_INVENTORY_Y = 90;
        public static final int PLAYER_INVENTORY_COLUMNS = 9;
        public static final int PLAYER_INVENTORY_ROWS = 3;
        public static final int PLAYER_INVENTORY_FIRST_SLOT_X = 6;
        public static final int PLAYER_INVENTORY_FIRST_SLOT_Y = 7;
        public static final int PLAYER_BELT_FIRST_SLOT_Y = 65;

        public static final ResourceLocation PLAYER_INVENTORY = new ResourceLocation(MOD_ID, "textures/screens/parts/player_inventory_basic.png");

        public static final int SLOT_SIZE_X = 18;
        public static final int SLOT_SIZE_Y = 18;

        public static final ResourceLocation SLOT = new ResourceLocation(MOD_ID, "textures/screens/parts/slot.png");
        public static final ResourceLocation INPUT_SLOT = new ResourceLocation(MOD_ID, "textures/screens/parts/input_slot.png");
        public static final ResourceLocation OUTPUT_SLOT = new ResourceLocation(MOD_ID, "textures/screens/parts/output_slot.png");
        public static final ResourceLocation LOCKED_SLOT = new ResourceLocation(MOD_ID, "textures/screens/parts/locked_slot.png");
        public static final ResourceLocation UNUSED_SLOT = new ResourceLocation(MOD_ID, "textures/screens/parts/no_slot.png");

        public static final int PART_SIZE_X = 6;
        public static final int PART_SIZE_Y = 6;

        public static final ResourceLocation LEFT = new ResourceLocation(MOD_ID, "textures/screens/parts/left.png");
        public static final ResourceLocation RIGHT = new ResourceLocation(MOD_ID, "textures/screens/parts/right.png");
        public static final ResourceLocation UPPER = new ResourceLocation(MOD_ID, "textures/screens/parts/upper.png");
        public static final ResourceLocation LOWER = new ResourceLocation(MOD_ID, "textures/screens/parts/lower.png");
        public static final ResourceLocation UPPER_RIGHT = new ResourceLocation(MOD_ID, "textures/screens/parts/upper_right.png");
        public static final ResourceLocation UPPER_LEFT = new ResourceLocation(MOD_ID, "textures/screens/parts/upper_left.png");
        public static final ResourceLocation LOWER_LEFT = new ResourceLocation(MOD_ID, "textures/screens/parts/lower_left.png");
        public static final ResourceLocation LOWER_RIGHT = new ResourceLocation(MOD_ID, "textures/screens/parts/lower_right.png");
        public static final ResourceLocation FILLER = new ResourceLocation(MOD_ID, "textures/screens/parts/filler.png");
        public static final ResourceLocation EXPANDER_LEFT = new ResourceLocation(MOD_ID, "textures/screens/parts/expander_left.png");
        public static final ResourceLocation EXPANDER_RIGHT = new ResourceLocation(MOD_ID, "textures/screens/parts/expander_right.png");
        public static final ResourceLocation THINNER_LEFT = new ResourceLocation(MOD_ID, "textures/screens/parts/thinner_left.png");
        public static final ResourceLocation THINNER_RIGHT = new ResourceLocation(MOD_ID, "textures/screens/parts/thinner_right.png");
    }

    public static class BagScreen {
        public static int calculateYSize(int rows) { return ScreenParts.PLAYER_INVENTORY_Y + rows * ScreenParts.SLOT_SIZE_Y + ScreenParts.PART_SIZE_Y * 3; }
        public static int calculateShiftLeft(int columns) { return (int)Math.ceil(((double)columns - (double) ScreenParts.PLAYER_INVENTORY_COLUMNS) / 2.0d); }
    }

    public static class UpgradeScreen {
        public static final int BACKGROUND_X = 174;
        public static final int BACKGROUND_Y = 186;
        public static final int FIRST_SLOT_X = 6;
        public static final int FIRST_SLOT_Y = 13;
        public static final int TITLES_X = 6;
        public static final int GUI_TITLE_Y = 4;
        public static final int INVENTORY_TITLE_Y = 94;
        public static final int HELP_X = 9;
        public static final int HELP_Y = 54;
        public static final int PLAYER_INVENTORY_PART_Y = 96;

        public static final ResourceLocation BACKGROUND = new ResourceLocation(MOD_ID, "textures/screens/upgrade_screen.png");
    }

    public static class PlayerInterface {
        public static final int BACKGROUND_X = 174;
        public static final int BACKGROUND_Y = 204;
        public static final int PLAYER_INVENTORY_PART_Y = 114;
        public static final int SPECIAL_SLOTS_Y = 13;
        public static final int ARMOR_SLOTS_X = 6;
        public static final int OFF_HAND_SLOT_X = 82;
        public static final int BELT_Y = 93;
        public static final int BELT_X = 6;
        public static final int MAIN_INVENTORY_Y = 35;
        public static final int MAIN_INVENTORY_X = 6;
        public static final int TITLES_X = 6;
        public static final int GUI_TITLE_Y = 4;
        public static final int INVENTORY_TITLE_Y = 112;

        public static final ResourceLocation BACKGROUND = new ResourceLocation(MOD_ID, "textures/screens/player_interface.png");
    }

    public static class ArmorStandUpgrade {
        public static final int BACKGROUND_X = 176;
        public static final int BACKGROUND_Y = 166;
        public static final int PLAYER_INVENTORY_X = 7;
        public static final int PLAYER_INVENTORY_Y = 83;
        public static final int ARMOR_INVENTORY_X = 79;
        public static final int ARMOR_INVENTORY_CHEST_PLATE_Y = 25;
        public static final int ARMOR_INVENTORY_ELYTRA_Y = 43;
        public static final int EMPTY_CHEST_PLATE_SLOT_X = 176;
        public static final int EMPTY_ELYTRA_SLOT_X = 192;
        public static final int TITLES_X = 6;
        public static final int GUI_TITLE_Y = 5;
        public static final int INVENTORY_TITLE_Y = 72;

        public static final ResourceLocation BACKGROUND = new ResourceLocation(MOD_ID, "textures/screens/armor_stand_upgrade.png");
    }
}
