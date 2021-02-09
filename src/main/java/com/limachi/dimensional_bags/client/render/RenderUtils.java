package com.limachi.dimensional_bags.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;

public class RenderUtils {

    public static Vector4f expandColor(int color, boolean isShadow) {
        float shadow = isShadow ? 0.25f : 1.0f;
        return new Vector4f(
                (float)(color >> 16 & 255) / 255.0F * shadow,
                (float)(color >> 8 & 255) / 255.0F * shadow,
                (float)(color & 255) / 255.0F * shadow,
                (float)(color >> 24 & 255) / 255.0F);
    }

    public static int compactColor(Vector4f color) {
        return ((int)(color.getW() * 255) << 24) | ((int)(color.getX() * 255) << 16) | ((int)(color.getY() * 255) << 8) | (int)(color.getZ() * 255);
    }

    public static void drawBox(MatrixStack matrixStack, Box2d box, int color) {
        Matrix4f matrix = matrixStack.getLast().getMatrix();
        Vector4f ec = expandColor(color, false);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(matrix, (float)box.getX1(), (float)box.getY2(), 0.0F).color(ec.getX(), ec.getY(), ec.getZ(), ec.getW()).endVertex();
        bufferbuilder.pos(matrix, (float)box.getX2(), (float)box.getY2(), 0.0F).color(ec.getX(), ec.getY(), ec.getZ(), ec.getW()).endVertex();
        bufferbuilder.pos(matrix, (float)box.getX2(), (float)box.getY1(), 0.0F).color(ec.getX(), ec.getY(), ec.getZ(), ec.getW()).endVertex();
        bufferbuilder.pos(matrix, (float)box.getX1(), (float)box.getY1(), 0.0F).color(ec.getX(), ec.getY(), ec.getZ(), ec.getW()).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static int getPrintedStringWidth(Matrix4f matrix, FontRenderer font, String text) {
        float m00 = new Float(matrix.toString().substring(10).split(" ")[0]); //dirty trick to get the current scale on the X axis from the matrix
        return (int)(font.getStringWidth(text) * m00);
    }

    public static void drawString(MatrixStack matrixStack, FontRenderer font, String string, Box2d coords, int textColor, boolean withShadow, boolean withWrap) {
        if (withShadow)
            drawString(matrixStack, font, string, coords.copy().move(1, 1), compactColor(expandColor(textColor, true)), false, withWrap);
        int r = 0;
        float x = (float)coords.getX1();
        float y = (float)coords.getY1();
        int l = 0;
        String tmpStr;

        if (withWrap)
            while (r < string.length() && y + l * font.FONT_HEIGHT < coords.getY2()) {
                tmpStr = font.func_238412_a_(string.substring(r), (int) coords.getWidth());
                if (tmpStr.isEmpty())
                    tmpStr = string.substring(0, 1);
                r += tmpStr.length();
                font.drawString(matrixStack, tmpStr, x, (float)(y + l * font.FONT_HEIGHT), textColor);
                ++l;
            }
        else
            font.drawString(matrixStack, string, x, y, textColor);
    }
}
