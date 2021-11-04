package com.limachi.dimensional_bags.client.render.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

import static com.limachi.dimensional_bags.client.render.TextureCutout.HOVERED;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;

/**
 * widget that changes size when toggled, intended for masking children widgets
 */
public class ParentToggleWidget extends TextWidget {

    protected int heightOpened;
    protected int heightClosed;

    public ParentToggleWidget(int x, int y, int width, int height, ITextComponent title, int heightOpened) {
        super(x, y, width, height, MINECRAFT.font, title, 0xFFFFFFFF);
        isToggle = true;
        this.heightOpened = heightOpened;
        heightClosed = height;
    }

    @Override
    public void setHeight(int value) {
        super.setHeight(value);
        heightClosed = value;
    }

    public void setHeightOpened(int value) {
        heightOpened = value;
        if (isSelected)
            height = value;
    }

    @Override
    public void renderButton(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (isSelected) {
            glEnable(GL_SCISSOR_TEST);
            scissor(x(), y() + heightClosed, width, heightOpened - heightClosed); //make sure the children widgets will not 'bleed' outside the parent
            isRenderingChildren = true; //trick to signify to children that they are allowed to render (by checking the state of the parent)
            for (BaseWidget child : children)
                child.render(matrixStack, mouseX, mouseY, partialTicks);
            isRenderingChildren = false;
            glDisable(GL_SCISSOR_TEST);
            renderStandardBackground(matrixStack, partialTicks, x(), y(), width, heightClosed, active, isHovered ? HOVERED : 0);
        }
        font.draw(matrixStack, text, x() + 3, y() + (heightClosed - font.lineHeight) / 2.f, color);
    }

    @Override
    public boolean clicked(double mouseX, double mouseY) {
        return (mouseY < (double)(y() + heightClosed)) && super.clicked(mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        height = isSelected ? heightOpened : heightClosed;
    }
}
