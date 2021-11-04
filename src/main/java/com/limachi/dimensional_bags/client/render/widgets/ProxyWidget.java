package com.limachi.dimensional_bags.client.render.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;

import javax.annotation.Nonnull;

public class ProxyWidget extends BaseWidget {

    IGuiEventListener listener;
    IRenderable renderable;

    public ProxyWidget() { super(0, 0, 0, 0); }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (renderable != null)
            renderable.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (listener != null)
            listener.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return listener != null && listener.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return listener != null && listener.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return listener != null && listener.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return listener != null && listener.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return false; }
}
