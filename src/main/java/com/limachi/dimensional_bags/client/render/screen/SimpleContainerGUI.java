package com.limachi.dimensional_bags.client.render.screen;

/*
public class SimpleContainerGUI extends ContainerScreen<SimpleContainer> {

    private int rows;
    private int columns;
    private int columns_shift_left;
    private int columns_shift_right;

    public final Root root;

    @Override
    public void tick() {
        root.tick();
        super.tick();
    }

    public int getTick() { return root.ticks; }

    @Override
    protected void init() {
        Minecraft.getInstance().keyboardListener.setSendRepeatsToGui(true);
        super.init();
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().keyboardListener.setSendRepeatsToGui(false);
    }

    public SimpleContainerGUI(SimpleContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.rows = container.getRows();
        this.columns = container.getColumns();
        this.xSize = PLAYER_INVENTORY_X + (Ints.max(this.columns - PLAYER_INVENTORY_COLUMNS, 0)) * SLOT_SIZE_X; //total size of the gui x axis
        this.ySize = GUIs.BagScreen.calculateYSize(this.rows);
        this.root = Root.getRoot(screenContainer.rootUUID);
        this.columns_shift_left = GUIs.BagScreen.calculateShiftLeft(this.columns);
        this.columns_shift_right = this.columns - this.columns_shift_left - PLAYER_INVENTORY_COLUMNS; //how many exta columns on the right of the gui
    }

    public void render_top(MatrixStack matrixStack, TextureManager tm) { //render the top part + a space for the name of the inventory
        int x = this.columns_shift_left < 0 ? -this.columns_shift_left * SLOT_SIZE_X : 0;
        int x1 = this.xSize + (this.columns_shift_right < 0 ? this.columns_shift_right * SLOT_SIZE_X : 0) - PART_SIZE_X;
        tm.bindTexture(UPPER_LEFT);
        this.blitGuiFull(matrixStack, x, 0, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(UPPER);
        for (int i = 0; i < this.columns * 3; ++i)
            this.blitGuiFull(matrixStack, x + PART_SIZE_X + i * PART_SIZE_X, 0, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(UPPER_RIGHT);
        this.blitGuiFull(matrixStack, x1, 0, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(LEFT);
        this.blitGuiFull(matrixStack, x, PART_SIZE_Y, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(FILLER);
        for (int i = 0; i < this.columns * 3; ++i)
            this.blitGuiFull(matrixStack, PART_SIZE_X + x + i * PART_SIZE_X, PART_SIZE_Y, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(RIGHT);
        this.blitGuiFull(matrixStack, x1, PART_SIZE_Y, PART_SIZE_X, PART_SIZE_Y);
    }

    protected void drawAccessRectangle(MatrixStack matrixStack, TextureManager tm, int x, int y, Wrapper.IORights rights) {
        if (rights != null) {
            int flags = rights.flags & (CANINPUT | CANOUTPUT);
            if (flags == (CANINPUT | CANOUTPUT)) tm.bindTexture(SLOT);
            if (flags == CANINPUT) tm.bindTexture(INPUT_SLOT);
            if (flags == CANOUTPUT) tm.bindTexture(OUTPUT_SLOT);
            if (flags == 0) tm.bindTexture(LOCKED_SLOT);
        }
        else
            tm.bindTexture(UNUSED_SLOT);
        this.blitGuiFull(matrixStack, x, y, SLOT_SIZE_X, SLOT_SIZE_Y);
    }

    public void render_rows(MatrixStack matrixStack, TextureManager tm) { //render all rows, including right and left border
        int x = this.columns_shift_left < 0 ? -this.columns_shift_left * SLOT_SIZE_X : 0;
        int x1 = this.xSize + (this.columns_shift_right < 0 ? this.columns_shift_right * SLOT_SIZE_X : 0) - PART_SIZE_X;
        int sy = PART_SIZE_Y * 2;
        tm.bindTexture(LEFT);
        for (int i = 0; i < this.rows * 3; ++i)
            this.blitGuiFull(matrixStack, x, sy + i * PART_SIZE_Y, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(RIGHT);
        for (int i = 0; i < this.rows * 3; ++i)
            this.blitGuiFull(matrixStack, x1, sy + i * PART_SIZE_Y, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(SLOT);
        for (int i = 0; i < this.columns; ++i)
            for (int y = 0; y < this.rows; ++y)
                drawAccessRectangle(matrixStack, tm, x + i * SLOT_SIZE_X + PART_SIZE_X, sy + y * SLOT_SIZE_Y, i + y * this.columns < container.getSlotCount() ? new Wrapper.IORights() : null);
    }

    public void render_expander(MatrixStack matrixStack, TextureManager tm) { //render the part between the container inventory and the player inventory
        int y = this.ySize - PLAYER_INVENTORY_Y - PART_SIZE_Y;
        tm.bindTexture(this.columns_shift_left == 0 ? LEFT : LOWER_LEFT);
        this.blitGuiFull(matrixStack, 0, y, PART_SIZE_X, PART_SIZE_Y);
        if (this.columns_shift_left != 0) {
            tm.bindTexture(LOWER);
            for (int i = 1; i < this.columns_shift_left * 3; ++i)
                this.blitGuiFull(matrixStack, i * PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
            tm.bindTexture(EXPANDER_LEFT);
            this.blitGuiFull(matrixStack, this.columns_shift_left * 3 * PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
        }
        tm.bindTexture(FILLER);
        int x = this.columns_shift_left * 3 * PART_SIZE_X + PART_SIZE_X;
        for (int i = 0; i < PLAYER_INVENTORY_COLUMNS * 3; ++i)
            this.blitGuiFull(matrixStack, x + i * PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(EXPANDER_RIGHT);
        x = this.xSize - this.columns_shift_right * 3 * PART_SIZE_X - PART_SIZE_X;
        this.blitGuiFull(matrixStack, x, y, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(LOWER);
        for (int i = 1; i < this.columns_shift_right * 3; ++i)
            this.blitGuiFull(matrixStack, x + i * PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(this.columns_shift_right == 0 ? RIGHT : LOWER_RIGHT);
        this.blitGuiFull(matrixStack, this.xSize - PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
    }

    public void render_separator(MatrixStack matrixStack, TextureManager tm) {
        if (this.columns_shift_right != 0 || this.columns_shift_left != 0) {
            if (this.columns_shift_right >= 0)
                this.render_expander(matrixStack, tm);
            else
                this.render_thinner(matrixStack, tm);
            return;
        }
        int y = this.ySize - PLAYER_INVENTORY_Y - PART_SIZE_Y;
        tm.bindTexture(LEFT);
        this.blitGuiFull(matrixStack, 0, y, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(FILLER);
        for (int i = 0; i < PLAYER_INVENTORY_COLUMNS * 3; ++i)
            this.blitGuiFull(matrixStack, PART_SIZE_X + i * PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(RIGHT);
        this.blitGuiFull(matrixStack, this.xSize - PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
    }

    public void render_thinner(MatrixStack matrixStack, TextureManager tm) {
        int shif_left = -this.columns_shift_left;
        int shift_right = -this.columns_shift_right;
        int y = this.ySize - PLAYER_INVENTORY_Y - PART_SIZE_Y;

        tm.bindTexture(shif_left == 0 ? LEFT : UPPER_LEFT);
        this.blitGuiFull(matrixStack, 0, y, PART_SIZE_X, PART_SIZE_Y);
        int x = 0;
        if (this.columns_shift_left != 0) {
            tm.bindTexture(UPPER);
            for (int i = 0; i < shif_left * 3 - 1; ++i)
                this.blitGuiFull(matrixStack, x += PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
            tm.bindTexture(THINNER_LEFT);
            this.blitGuiFull(matrixStack, x += PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
        }
        tm.bindTexture(FILLER);
        for (int i = 0; i < this.columns * 3; ++i)
            this.blitGuiFull(matrixStack, x += PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(THINNER_RIGHT);
        this.blitGuiFull(matrixStack, x += PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(UPPER);
        for (int i = 0; i < shift_right * 3 - 1; ++i)
            this.blitGuiFull(matrixStack, x += PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
        tm.bindTexture(UPPER_RIGHT);
        this.blitGuiFull(matrixStack, x + PART_SIZE_X, y, PART_SIZE_X, PART_SIZE_Y);
    }

    private void blitGuiFull(MatrixStack matrixStack, int x, int y, int w, int h) {
        this.blit(matrixStack, x + this.guiLeft, y + this.guiTop, 0, 0, w, h, w, h);
    }

    public void render_player_inventory(MatrixStack matrixStack, TextureManager tm) {
        tm.bindTexture(PLAYER_INVENTORY);
        this.blitGuiFull(matrixStack, this.columns_shift_left > 0 ? this.columns_shift_left * SLOT_SIZE_X : 0, this.ySize - PLAYER_INVENTORY_Y, PLAYER_INVENTORY_X, PLAYER_INVENTORY_Y);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (this.columns < 9)
            this.font.drawString(matrixStack, title.getString(), PLAYER_INVENTORY_X / 2, this.ySize - PLAYER_INVENTORY_Y - PART_SIZE_Y / 2, 4210752);
        else
            this.font.drawString(matrixStack, title.getString(), PART_SIZE_X, PART_SIZE_Y / 2, 4210752);
        this.font.drawString(matrixStack, playerInventory.getDisplayName().getString(), PART_SIZE_X + (this.columns_shift_left > 0 ? this.columns_shift_left * SLOT_SIZE_X : 0), this.ySize - PLAYER_INVENTORY_Y - PART_SIZE_Y / 2, 4210752);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.minecraft == null) return;
        TextureManager tm = this.minecraft.getTextureManager();
        this.render_player_inventory(matrixStack, tm);
        this.render_separator(matrixStack, tm);
        this.render_rows(matrixStack, tm);
        this.render_top(matrixStack, tm);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.guiLeft = (this.width - this.xSize) / 2; //start of the gui in the x axis (this.width is the size of the screens on the x axis)
        this.guiTop = (this.height - this.ySize) / 2; //start of the gui in the y axis (this.height is the size of the screens on the y axis)
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        root.render(matrixStack, mouseX, mouseY, partialTicks);
        renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double x, double y, int b, double px, double py) {
        if (root.focusedWidget != null)
            return root.focusedWidget.mouseDragged(x, y, b, px, py) || super.mouseDragged(x, y, b, px, py);
        if (root.mouseDragged(x, y, b, px, py))
            return true;
        return super.mouseDragged(x, y, b, px, py);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        root.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (root.focusedWidget != null)
            return root.focusedWidget.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
        if (root.mouseClicked(mouseX, mouseY, button))
            return true;
        return super.mouseClicked(mouseX,mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (root.focusedWidget != null)
            return root.focusedWidget.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
        if (root.mouseReleased(mouseX, mouseY, button))
            return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (root.focusedWidget != null)
            return root.focusedWidget.mouseScrolled(mouseX, mouseY, scrollAmount) || super.mouseScrolled(mouseX, mouseY, scrollAmount);
        if (root.mouseScrolled(mouseX, mouseY, scrollAmount))
            return true;
        return super.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (root.focusedWidget != null)
            return root.focusedWidget.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        if (root.keyPressed(keyCode, scanCode, modifiers))
            return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (root.focusedWidget != null)
            return root.focusedWidget.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
        if (root.keyReleased(keyCode, scanCode, modifiers))
            return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public final boolean charTyped(char codePoint, int modifiers) {
        if (root.focusedWidget != null)
            return root.focusedWidget.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
        if (root.charTyped(codePoint, modifiers))
            return true;
        return super.charTyped(codePoint, modifiers);
    }
}
*/