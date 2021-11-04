package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.ITooltipRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class RenderOnlyWidget extends Widget implements ITooltipRenderer {

    protected IRenderable renderable;
    protected Function<Widget, IFormattableTextComponent> tooltip = null;

    public RenderOnlyWidget(int x, int y, int width, int height, IRenderable renderable) {
        super(x, y, width, height, StringTextComponent.EMPTY);
        this.renderable = renderable;
    }

    public <T extends Widget> T setTooltipProcessor(Function<Widget, IFormattableTextComponent> tooltip) { this.tooltip = tooltip; return (T)this; }
    public <T extends Widget> T appendTooltipProcessor(Function<Widget, IFormattableTextComponent> tooltip) {
        if (this.tooltip == null)
            this.tooltip = tooltip;
        else {
            final Function<Widget, IFormattableTextComponent> t = this.tooltip;
            this.tooltip = b -> t.apply(b).copy().append(tooltip.apply(b));
        }
        return (T)this;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (renderable != null)
            renderable.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override public void mouseMoved(double p_212927_1_, double p_212927_3_) {}
    @Override public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) { return false; }
    @Override public boolean mouseReleased(double p_231048_1_, double p_231048_3_, int p_231048_5_) { return false; }
    @Override public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int p_231045_5_, double p_231045_6_, double p_231045_8_) { return false; }
    @Override public boolean mouseScrolled(double p_231043_1_, double p_231043_3_, double p_231043_5_) { return false; }
    @Override public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) { return false; }
    @Override public boolean keyReleased(int p_223281_1_, int p_223281_2_, int p_223281_3_) { return false; }
    @Override public boolean charTyped(char p_231042_1_, int p_231042_2_) { return false; }
    @Override public boolean changeFocus(boolean p_231049_1_) { return false; }
    @Override public boolean isMouseOver(double p_231047_1_, double p_231047_3_) { return false; }

    @Override
    public void renderToolTip(@Nonnull Screen screen, @Nonnull MatrixStack matrixStack, int mouseX, int mouseY) {
        if (tooltip != null)
            screen.renderToolTip(matrixStack, screen.getMinecraft().font.split(tooltip.apply(this), Math.max(screen.width / 2 - 43, 170)), mouseX, mouseY, screen.getMinecraft().font);
    }
}
