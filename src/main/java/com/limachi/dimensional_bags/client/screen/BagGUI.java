package com.limachi.dimensional_bags.client.screen;

import com.google.common.primitives.Ints;
import com.limachi.dimensional_bags.common.data.inventory.container.DimBagContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

//@OnlyIn(Dist.CLIENT) //the constants for size are used by the container on both sides
public class BagGUI extends ContainerScreen<DimBagContainer> { //addapt the size of the gui depending on size of the eye

    public static final int PLAYER_INVENTORY_COLUMNS = 9;
    public static final int PLAYER_INVENTORY_X = 176;
    public static final int PLAYER_INVENTORY_Y = 83;
    public static final int SLOT_SIZE_X = 18;
    public static final int SLOT_SIZE_Y = 18;
    public static final int PART_SIZE_X = 6; //expected to be a third of a slot
    public static final int PART_SIZE_Y = 6; //expected to be a third of a slot

    private static final ResourceLocation PLAYER_INVENTORY = new ResourceLocation(MOD_ID, "textures/screens/player_inventory_basic.png");
    private static final ResourceLocation SLOT = new ResourceLocation(MOD_ID, "textures/screens/parts/slot.png");
    private static final ResourceLocation LEFT = new ResourceLocation(MOD_ID, "textures/screens/parts/left.png");
    private static final ResourceLocation RIGHT = new ResourceLocation(MOD_ID, "textures/screens/parts/right.png");
    private static final ResourceLocation UPPER = new ResourceLocation(MOD_ID, "textures/screens/parts/upper.png");
    private static final ResourceLocation LOWER = new ResourceLocation(MOD_ID, "textures/screens/parts/lower.png");
    private static final ResourceLocation UPPER_RIGHT = new ResourceLocation(MOD_ID, "textures/screens/parts/upper_right.png");
    private static final ResourceLocation UPPER_LEFT = new ResourceLocation(MOD_ID, "textures/screens/parts/upper_left.png");
    private static final ResourceLocation LOWER_LEFT = new ResourceLocation(MOD_ID, "textures/screens/parts/lower_left.png");
    private static final ResourceLocation LOWER_RIGHT = new ResourceLocation(MOD_ID, "textures/screens/parts/lower_right.png");
    private static final ResourceLocation FILLER = new ResourceLocation(MOD_ID, "textures/screens/parts/filler.png");
    private static final ResourceLocation EXPANDER_LEFT = new ResourceLocation(MOD_ID, "textures/screens/parts/expander_left.png");
    private static final ResourceLocation EXPANDER_RIGHT = new ResourceLocation(MOD_ID, "textures/screens/parts/expander_right.png");

    private int rows;
    private int columns;
    private int columns_shift_left;
    private int columns_shift_right;
    private int guitRight;
    private int guiBottom;

    public BagGUI(DimBagContainer container, PlayerInventory inv, ITextComponent titleIn) {
        super(container, inv, titleIn);
        this.rows = container.getRows();
        this.columns = container.getColumns();
        this.xSize = PLAYER_INVENTORY_X + (Ints.max(this.columns - PLAYER_INVENTORY_COLUMNS, 0)) * SLOT_SIZE_X; //total size of the gui x axis
        this.ySize = PLAYER_INVENTORY_Y + this.rows * SLOT_SIZE_Y + PART_SIZE_Y * 3; //total size of the gui y axis (the 3 parts are for the expander, the text space on top and the top)
        this.guiLeft = (this.width - this.xSize) / 2; //start of the gui in the x axis (this.width is the size of the screens on the x axis)
        this.guiTop = (this.height - this.ySize) / 2; //start of the gui in the y axis (this.height is the size of the screens on the y axis)
        this.guitRight = this.guiLeft + this.xSize;
        this.guiBottom = this.guiTop + this.ySize;
        this.columns_shift_left = (int)Math.floor(((double)this.columns - (double)PLAYER_INVENTORY_COLUMNS) / 2.0d); //how many extra columns on the left of the gui
        this.columns_shift_right = this.columns - this.columns_shift_left - PLAYER_INVENTORY_COLUMNS; //how many exta columns on the right of the gui
    }

    public void render_top(TextureManager tm) { //render the top part + a space for the name of the inventory
        tm.bindTexture(UPPER_LEFT);
        this.blit(0, 0, 0, 0, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(UPPER);
        for (int i = 1; i < this.rows * 3 - 1; ++i)
            this.blit(i * PART_SIZE_X, 0, 0, 0, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(UPPER_RIGHT);
        this.blit(this.xSize - PART_SIZE_X, 0, 0, 0, PART_SIZE_X, PART_SIZE_Y);
    }

    public void render_rows(TextureManager tm) { //render all rows, including right and left border
        int sy = this.ySize - PART_SIZE_Y;
        tm.bindTexture(LEFT);
        for (int i = 0; i < this.rows * 3; ++i)
            this.blit(0, sy - i * PART_SIZE_Y, 0, 0, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(RIGHT);
        for (int i = 0; i < this.rows * 3; ++i)
            this.blit(this.xSize, sy - i * PART_SIZE_Y, 0, 0, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(SLOT);
        for (int x = 0; x < this.columns; ++x)
            for (int y = 0; sy < this.rows; ++sy)
                this.blit(PART_SIZE_X + x * SLOT_SIZE_X, sy - y * SLOT_SIZE_Y, 0, 0, SLOT_SIZE_X, SLOT_SIZE_Y);
    }

    public void render_expander(TextureManager tm) { //render the part between the container inventory and the player inventory
        int y = this.ySize - PLAYER_INVENTORY_Y - PART_SIZE_Y;
        tm.bindTexture(this.columns_shift_left != 0 ? LEFT : LOWER_LEFT);
        this.blit(0, y, 0, 0, PART_SIZE_X, PART_SIZE_Y);
        if (this.columns_shift_left != 0) {
            tm.bindTexture(LOWER);
            for (int i = 1; i < this.columns_shift_left * 3; ++i)
                this.blit(i * PART_SIZE_X, y, 0, 0, PART_SIZE_X, PART_SIZE_Y);
            tm.bindTexture(EXPANDER_LEFT);
            this.blit(this.columns_shift_left * 3 * PART_SIZE_X, y, 0, 0, PART_SIZE_X, PART_SIZE_Y);
        }
        tm.bindTexture(FILLER);
        int x = this.columns_shift_left * 3 * PART_SIZE_X;
        for (int i = 1; i < PLAYER_INVENTORY_COLUMNS * 3 - 1; ++i)
            this.blit(x + i * PART_SIZE_X, y, 0, 0, PART_SIZE_X, PART_SIZE_Y);
        if (this.columns_shift_right != 0) {
            tm.bindTexture(EXPANDER_RIGHT);
            x = this.xSize - this.columns_shift_right * 3 * PART_SIZE_X;
            this.blit(x, y, 0, 0, PART_SIZE_X, PART_SIZE_Y);
            tm.bindTexture(LOWER);
            for (int i = 1; i < this.columns_shift_right * 3 - 1; ++i)
                this.blit(x + i * PART_SIZE_X, y, 0, 0, PART_SIZE_X, PART_SIZE_Y);
        }
        tm.bindTexture(this.columns_shift_right != 0 ? LOWER_RIGHT : RIGHT);
        this.blit(this.xSize - PART_SIZE_X, y, 0, 0, PART_SIZE_X, PART_SIZE_Y);
    }

    public void render_player_inventory(TextureManager tm) {
        tm.bindTexture(PLAYER_INVENTORY);
        this.blit(this.columns_shift_left * SLOT_SIZE_X, this.ySize - PLAYER_INVENTORY_Y, 0, 0, PLAYER_INVENTORY_X, PLAYER_INVENTORY_Y);
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.font.drawString(this.title.getFormattedText(), PART_SIZE_X, PART_SIZE_Y, 4210752);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), PART_SIZE_X, this.ySize - PLAYER_INVENTORY_Y - PART_SIZE_Y, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.minecraft == null) return;
        TextureManager tm = this.minecraft.getTextureManager();
        this.render_player_inventory(tm);
        this.render_expander(tm);
        this.render_rows(tm);
        this.render_top(tm);
    }
}
