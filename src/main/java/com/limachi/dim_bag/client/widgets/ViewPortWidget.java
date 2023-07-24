package com.limachi.dim_bag.client.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

/**
 * currently breaks focus transfer
 */
public class ViewPortWidget extends AbstractWidget {
    protected int dx = 0;
    protected int dy = 0;
    protected ArrayList<AbstractWidget> children = new ArrayList<>();

    public ViewPortWidget(int x, int y, int w, int h) {
        super(x, y, w, h, Component.empty());
    }

    public void addWidget(AbstractWidget widget) {
        children.add(widget);
    }

    public void applyDelta(int x, int y) {
        if (x != dx) {
            int ax = dx - x;
            for (AbstractWidget widget : children)
                widget.setX(widget.getX() + ax);
            dx = x;
        }
        if (y != dy) {
            int ay = dy - y;
            for (AbstractWidget widget : children)
                widget.setY(widget.getY() + ay);
            dy = y;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (clicked(mouseX, mouseY))
            for (AbstractWidget widget : children)
                if (widget.mouseClicked(mouseX, mouseY, button))
                    return true;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (clicked(mouseX, mouseY))
            for (AbstractWidget widget : children)
                if (widget.mouseReleased(mouseX, mouseY, button))
                    return true;
        return false;
    }

    @Override
    protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.enableScissor(getX(), getY(), getX() + width, getY() + height);
        if (!clicked(mouseX, mouseY)) { //prevent hover detection if mouse is outside main widget
            mouseX = Integer.MIN_VALUE;
            mouseY = Integer.MIN_VALUE;
        }
        for (AbstractWidget widget : children)
            widget.render(gui, mouseX, mouseY, partialTick);
        gui.disableScissor();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narratorOutput) {
        for (AbstractWidget widget : children)
            widget.updateNarration(narratorOutput);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (clicked(mouseX, mouseY))
            for (AbstractWidget widget : children)
                widget.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (clicked(mouseX, mouseY))
            for (AbstractWidget widget : children)
                if (widget.mouseScrolled(mouseX, mouseY, scroll))
                    return true;
        return false;
    }

    @Override
    public boolean keyPressed(int p_94745_, int p_94746_, int p_94747_) {
        for (AbstractWidget widget : children)
            if (widget.keyPressed(p_94745_, p_94746_, p_94747_))
                return true;
        return false;
    }

    @Override
    public boolean keyReleased(int p_94750_, int p_94751_, int p_94752_) {
        for (AbstractWidget widget : children)
            if (widget.keyReleased(p_94750_, p_94751_, p_94752_))
                return true;
        return false;
    }

    @Override
    public boolean charTyped(char p_94732_, int p_94733_) {
        for (AbstractWidget widget : children)
            if (widget.charTyped(p_94732_, p_94733_))
                return true;
        return false;
    }
}
