package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.widget.RootWidget;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;

public abstract class BaseScreen<T extends Container> extends ContainerScreen<T> {

    public RootWidget rootWidget;
    private boolean isFirstInit = true;

    public BaseScreen(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public int getGuiLeft() { return super.getGuiLeft(); }

    protected void firstInit() {
        rootWidget = new RootWidget(this);
        this.addListener(rootWidget);
    }

    protected void reloadInit() {
        this.addListener(rootWidget);
        rootWidget.resize(width, height);
    }

    @Override
    public void tick() {
        super.tick();
        rootWidget.tick();
    }

    @Override
    protected void init() {
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(true);
        super.init();
        if (isFirstInit) {
            firstInit();
            isFirstInit = false;
        }
        else
            reloadInit();
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        rootWidget.render(matrixStack, mouseX, mouseY, partialTicks);
        func_230459_a_(matrixStack, mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double x, double y, int b, double px, double py) {
        return rootWidget.mouseDragged(x,y,b,px, py);
    }
}
