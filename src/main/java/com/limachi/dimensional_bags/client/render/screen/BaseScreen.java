package com.limachi.dimensional_bags.client.render.screen;

/*
public abstract class BaseScreen<T extends BaseContainer> extends ContainerScreen<T> {

    private boolean isFirstInit = true;

    public BaseScreen(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    public FontRenderer getFont() { return font; }

    public void addWidget(Base widget) {
        if (widget instanceof Root)
            root = (Root)widget;
        else if (root != null)
            root.widgets.add(widget);
    }

    @Override
    public int getGuiLeft() { return super.getGuiLeft(); }

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

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        for (Base widget : root.widgets)
            widget.render(matrixStack, mouseX, mouseY, partialTicks);
        renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double x, double y, int b, double px, double py) {
        if (root.focusedWidget != null)
            return root.focusedWidget.mouseDragged(x, y, b, px, py) || super.mouseDragged(x, y, b, px, py);
        for (Base widget : root.widgets)
            if (widget.mouseDragged(x, y, b, px, py))
                return true;
        return super.mouseDragged(x, y, b, px, py);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (Base widget : root.widgets)
            widget.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (root.focusedWidget != null)
            return root.focusedWidget.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
        for (Base widget : root.widgets)
            if (widget.mouseClicked(mouseX, mouseY, button))
                return true;
        return super.mouseClicked(mouseX,mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (root.focusedWidget != null)
            return root.focusedWidget.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
        for (Base widget : root.widgets)
            if (widget.mouseReleased(mouseX, mouseY, button))
                return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (root.focusedWidget != null)
            return root.focusedWidget.mouseScrolled(mouseX, mouseY, scrollAmount) || super.mouseScrolled(mouseX, mouseY, scrollAmount);
        for (Base widget : root.widgets)
            if (widget.mouseScrolled(mouseX, mouseY, scrollAmount))
                return true;
        return super.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (root.focusedWidget != null)
            return root.focusedWidget.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        for (Base widget : root.widgets)
            if (widget.keyPressed(keyCode, scanCode, modifiers))
                return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (root.focusedWidget != null)
            return root.focusedWidget.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
        for (Base widget : root.widgets)
            if (widget.keyReleased(keyCode, scanCode, modifiers))
                return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public final boolean charTyped(char codePoint, int modifiers) {
        if (root.focusedWidget != null)
            return root.focusedWidget.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
        for (Base widget : root.widgets)
            if (widget.charTyped(codePoint, modifiers))
                return true;
        return super.charTyped(codePoint, modifiers);
    }

}*/
