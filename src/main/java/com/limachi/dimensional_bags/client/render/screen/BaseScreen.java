package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.widgets.Base;
import com.limachi.dimensional_bags.common.container.BaseContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class BaseScreen<T extends BaseContainer> extends ContainerScreen<T> {

    protected List<Base> widgets = new ArrayList<>();
    protected Base focusedWidget = null;
    protected Stack<int[]> scissors = new Stack<>();
    protected int ticks = 0;
    private boolean isFirstInit = true;

    public BaseScreen(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    public FontRenderer getFont() { return font; }

    public Base getFocusedWidget() { return focusedWidget; }
    public void setFocusedWidget(Base widget) { focusedWidget = widget; }

    public void addWidget(Base widget) { widgets.add(widget); }

    public void scissor(MatrixStack matrixStack, double x1, double y1, double x2, double y2) {
        if (scissors.empty())
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        if (x1 > x2) {
            double x = x1;
            x1 = x2;
            x2 = x;
        }
        if (y1 > y2) {
            double y = y1;
            y1 = y2;
            y2 = y;
        }
        Vector4f v1 = new Vector4f((float)x1, (float)y1, 0, 1);
        Vector4f v2 = new Vector4f((float)x2, (float)y2, 0, 1);
        v1.transform(matrixStack.getLast().getMatrix());
        v2.transform(matrixStack.getLast().getMatrix());
        double factor = Minecraft.getInstance().getMainWindow().getGuiScaleFactor();
        int x = (int)(v1.getX() * factor);
        int y = (int)(Minecraft.getInstance().getMainWindow().getFramebufferHeight() - v2.getY() * factor);
        int [] entry = {x, y, x + (int)((v2.getX() - v1.getX()) * factor), y + (int)((v2.getY() - v1.getY()) * factor)};
        if (!scissors.empty()) {
            int [] t = scissors.peek();
            entry[0] = Math.max(entry[0], t[0]);
            entry[1] = Math.max(entry[1], t[1]);
            entry[2] = Math.min(entry[2], t[2]);
            entry[3] = Math.min(entry[3], t[3]);
        }
        scissors.push(entry);
        if (entry[2] - entry[0] <= 0 || entry[3] - entry[1] <= 0)
            GL11.glScissor(0, 0, 0, 0);
        else
            GL11.glScissor(entry[0], entry[1], entry[2] - entry[0], entry[3] - entry[1]);
    }

    public void removeScissor() {
        scissors.pop();
        if (scissors.empty())
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public int getGuiLeft() { return super.getGuiLeft(); }

    @Override
    public void tick() {
        for (Base widget : widgets)
            widget.tick(ticks);
        ++ticks;
        super.tick();
    }

    public int getTick() { return ticks; }

    @Override
    protected void init() {
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(true);
        super.init();
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
        for (Base widget : widgets)
            widget.render(matrixStack, mouseX, mouseY, partialTicks);
        renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double x, double y, int b, double px, double py) {
        if (focusedWidget != null)
            return focusedWidget.mouseDragged(x, y, b, px, py) || super.mouseDragged(x, y, b, px, py);
        for (Base widget : widgets)
            if (widget.mouseDragged(x, y, b, px, py))
                return true;
        return super.mouseDragged(x, y, b, px, py);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (Base widget : widgets)
            widget.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (focusedWidget != null)
            return focusedWidget.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
        for (Base widget : widgets)
            if (widget.mouseClicked(mouseX, mouseY, button))
                return true;
        return super.mouseClicked(mouseX,mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (focusedWidget != null)
            return focusedWidget.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
        for (Base widget : widgets)
            if (widget.mouseReleased(mouseX, mouseY, button))
                return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (focusedWidget != null)
            return focusedWidget.mouseScrolled(mouseX, mouseY, scrollAmount) || super.mouseScrolled(mouseX, mouseY, scrollAmount);
        for (Base widget : widgets)
            if (widget.mouseScrolled(mouseX, mouseY, scrollAmount))
                return true;
        return super.mouseScrolled(mouseX, mouseY, scrollAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focusedWidget != null)
            return focusedWidget.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        for (Base widget : widgets)
            if (widget.keyPressed(keyCode, scanCode, modifiers))
                return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (focusedWidget != null)
            return focusedWidget.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
        for (Base widget : widgets)
            if (widget.keyReleased(keyCode, scanCode, modifiers))
                return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public final boolean charTyped(char codePoint, int modifiers) {
        if (focusedWidget != null)
            return focusedWidget.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
        for (Base widget : widgets)
            if (widget.charTyped(codePoint, modifiers))
                return true;
        return super.charTyped(codePoint, modifiers);
    }

}
