package com.limachi.dimensional_bags.common.references;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

        public static final ResourceLocation PLAYER_INVENTORY = new ResourceLocation(MOD_ID, "textures/screens/player_inventory_basic.png");

        public static final int SLOT_SIZE_X = 18;
        public static final int SLOT_SIZE_Y = 18;

        public static final ResourceLocation SLOT = new ResourceLocation(MOD_ID, "textures/screens/parts/slot.png");
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
        public static int calculateShiftLeft(int columns) { return (int)Math.floor(((double)columns - (double) ScreenParts.PLAYER_INVENTORY_COLUMNS) / 2.0d); }
    }

    public static class UpgradeScreen {
        public static final int BACKGROUND_X = 174;
        public static final int BACKGROUND_Y = 180;
        public static final int FIRST_SLOT_X = 6;
        public static final int FIRST_SLOT_Y = 11;
        public static final int TITLES_X = 6;
        public static final int GUI_TITLE_Y = 4;
        public static final int INVENTORY_TITLE_Y = 90;
        public static final int HELP_X = 8;
        public static final int HELP_Y = 50;
        public static final int PLAYER_INVENTORY_PART_Y = 90;

        public static final ResourceLocation BACKGROUND = new ResourceLocation(MOD_ID, "textures/screens/upgrade_screen.png");
    }

    @OnlyIn(Dist.CLIENT)
    public static void addPlayerSlotsScreen(ContainerScreen<? extends Container> gui, int x, int y) {

    }
}
