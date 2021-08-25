package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.StringTextComponent;

/**
 * very simple widget that is only text, but compatible with other widgets. the text, color and font can be changed between frames
 */
public class ImageWidget extends BaseWidget {

    public TextureCutout image;
    public TextureCutout.TextureApplicationPattern application = TextureCutout.TextureApplicationPattern.STRETCH;
    public boolean buttonRender = false;

    public ImageWidget(int x, int y, int width, int height, TextureCutout image) {
        super(x, y, width, height, StringTextComponent.EMPTY);
        this.image = image;
        renderTitle = false;
    }

    public ImageWidget enableButtonRenderBehavior(boolean state) {
        buttonRender = state;
        return this;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        image.blitButton(matrixStack, new Box2d(x, y, width, height), 0, application, buttonRender ? renderState() : 0);
    }
}
