package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Consumer;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

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

    public static class Toggle extends ImageWidget {

        private static final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/widgets/buttons.png");
        private static final TextureCutout validate_texture = new TextureCutout(texture, 32, 40, 16, 16);

        protected final Consumer<Toggle> onStateChange;

        public Toggle(int x, int y, int width, int height, boolean initialState, Consumer<Toggle> onStateChange) {
            super(x, y, width, height, validate_texture);
            buttonRender = true;
            isToggle = true;
            this.onStateChange = onStateChange;
            this.isSelected = initialState;
        }

        @Override
        public void onClick(double p_230982_1_, double p_230982_3_) { onStateChange.accept(this); }
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
