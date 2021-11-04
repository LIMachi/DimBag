package com.limachi.dimensional_bags.client.render.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * very simple widget that is only text, but compatible with other widgets. the text, color and font can be changed between frames
 */
public class TextWidget extends BaseWidget {

    public ITextComponent text;
    public int color;
    public FontRenderer font;

    public TextWidget(int x, int y, int width, int height, @Nonnull FontRenderer font, @Nullable ITextComponent initial_string, int color) {
        super(x, y, width, height, StringTextComponent.EMPTY);
        text = initial_string != null ? initial_string : new StringTextComponent("");
        this.font = font;
        this.color = color;
        renderTitle = false;
    }

    public TextWidget(int x, int y, int width, int height, String text) {
        this(x, y, width, height, MINECRAFT.font, new StringTextComponent(text), 0xFFFFFFFF);
    }

    @Override
    public void renderButton(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (renderStandardBackground)
            font.drawWordWrap(text, x() + 3, y() + (height - font.lineHeight) / 2, width - 7, color);
        else
            font.drawWordWrap(text, x(), y(), width, color);
    }
}
