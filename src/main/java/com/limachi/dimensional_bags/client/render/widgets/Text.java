package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.client.render.screen.BaseScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;

public class Text extends Base {

    public String text = "";
    public FontRenderer font = null;
    public int color;
    public boolean withShadow;

    public Text(BaseScreen<?> screen, Base parent, double x, double y, double width, double height, FontRenderer font, String text, int color, boolean withShadow) {
        super(screen, parent, x, y, width, height, true);
        if (text == null)
            this.text = "";
        else
            this.text = text;
        if (font == null)
            this.font = screen.getFont();
        else
            this.font = font;
        this.color = color;
        this.withShadow = withShadow;
    }

    /**
     * basic white text, to be used with vanilla buttons (size 200x20)
     */
    public Text(BaseScreen<?> screen, Base parent, double x, double y, String text) {
        this(screen, parent, x + 5, y + 5, 190, 10, null, text, 0xFFFFFFFF, true);
    }

    @Override
    public void onRender(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawString(matrixStack, font == null ? screen.getFont() : font, text, coords, color, withShadow, true);
    }
}
