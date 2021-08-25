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

    /**
     * color order: 0xAARRGGBB
     */

    public static Vector4f expandColor(int color, boolean isShadow) {
        float shadow = isShadow ? 0.25f : 1.0f;
        return new Vector4f(
                (float)(color >> 16 & 255) / 255.0F * shadow,
                (float)(color >> 8 & 255) / 255.0F * shadow,
                (float)(color & 255) / 255.0F * shadow,
                (float)(color >> 24 & 255) / 255.0F);
    }

    /**
     * color order: 0xAARRGGBB
     */
    public static int compactColor(Vector4f color) {
        return ((int)(color.w() * 255) << 24) | ((int)(color.x() * 255) << 16) | ((int)(color.y() * 255) << 8) | (int)(color.z() * 255);
    }

    public static void drawBox(MatrixStack matrixStack, Box2d box, int color, int depth) {
        Matrix4f matrix = matrixStack.last().pose();
        Vector4f ec = expandColor(color, false);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(matrix, (float)box.getX1(), (float)box.getY2(), depth).color(ec.x(), ec.y(), ec.z(), ec.w()).endVertex();
        bufferbuilder.vertex(matrix, (float)box.getX2(), (float)box.getY2(), depth).color(ec.x(), ec.y(), ec.z(), ec.w()).endVertex();
        bufferbuilder.vertex(matrix, (float)box.getX2(), (float)box.getY1(), depth).color(ec.x(), ec.y(), ec.z(), ec.w()).endVertex();
        bufferbuilder.vertex(matrix, (float)box.getX1(), (float)box.getY1(), depth).color(ec.x(), ec.y(), ec.z(), ec.w()).endVertex();
        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static int getPrintedStringWidth(Matrix4f matrix, FontRenderer font, String text) {
        float m00 = new Float(matrix.toString().substring(10).split(" ")[0]); //dirty trick to get the current scale on the X axis from the matrix
        return (int)(font.width(text) * m00);
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
            while (r < string.length() && y + l * font.lineHeight < coords.getY2()) {
                tmpStr = font.plainSubstrByWidth(string.substring(r), (int) coords.getWidth());
                if (tmpStr.isEmpty())
                    tmpStr = string.substring(0, 1);
                r += tmpStr.length();
                font.draw(matrixStack, tmpStr, x, (float)(y + l * font.lineHeight), textColor);
                ++l;
            }
        else
            font.draw(matrixStack, string, x, y, textColor);
    }
}
