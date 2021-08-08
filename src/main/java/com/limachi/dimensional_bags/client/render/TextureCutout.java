package com.limachi.dimensional_bags.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TextureCutout {

    public static final int SELECTED = 1;
    public static final int HOVERED = 2;

    @OnlyIn(Dist.CLIENT)
    public static Minecraft mc = Minecraft.getInstance();

    public ResourceLocation file;
    public int tint;
    public Box2d corners;
    public int fileWidth;
    public int fileHeight;

    public enum TextureApplicationPattern {
        STRETCH, //default mode, fill the given coordinates by deforming the image
        MIDDLE_EXPANSION, //vanilla method for buttons, scale the image from the corners to the middle of the image
        TILE //vanilla method for background, repeat the image with it's original ratio to fill the coordinates
    }

    public void setTint(int tint) { this.tint = tint; }

    public TextureCutout(ResourceLocation file, int fileWidth, int fileHeight, double x, double y, double w, double h) {
        this(file, fileWidth, fileHeight, new Box2d(x, y, w, h));
    }

    public TextureCutout(ResourceLocation file, double x, double y, double w, double h) { this(file, 256, 256, x, y, w, h); }

    public TextureCutout(ResourceLocation file, Box2d cutout) { this(file, 256, 256, cutout); }

    public TextureCutout(ResourceLocation file, int fileWidth, int fileHeight, Box2d cutout) {
        this.file = file;
        this.fileWidth = fileWidth;
        this.fileHeight = fileHeight;
        this.corners = cutout;
    }

    @OnlyIn(Dist.CLIENT)
    public TextureCutout bindTexture() { mc.getTextureManager().bindTexture(file); return this; }

    public TextureCutout copy() { return new TextureCutout(file, fileWidth, fileHeight, corners.getX1(), corners.getY1(), corners.getWidth(), corners.getHeight()); }

    public TextureCutout setCorners(Box2d corners) { this.corners = corners; return this; }

    @OnlyIn(Dist.CLIENT)
    private static void innerBlit(double x, double x2, double y, double y2, int blitOffset, float minU, float maxU, float minV, float maxV) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y2, blitOffset).tex(minU, maxV).endVertex();
        bufferbuilder.pos(x2, y2, blitOffset).tex(maxU, maxV).endVertex();
        bufferbuilder.pos(x2, y, blitOffset).tex(maxU, minV).endVertex();
        bufferbuilder.pos(x, y, blitOffset).tex(minU, minV).endVertex();
        bufferbuilder.finishDrawing();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }

    @OnlyIn(Dist.CLIENT)
    public void blit(MatrixStack matrixStack, Box2d coords, int blitOffset, TextureApplicationPattern pattern) {
        if (file != null) bindTexture();
        blitRec(matrixStack, coords, blitOffset, pattern);
    }

    @OnlyIn(Dist.CLIENT)
    public void blitButton(MatrixStack matrixStack, Box2d coords, int blitOffset, TextureApplicationPattern pattern, int state) {
        if (file != null) bindTexture();
        if (state != 0) {
            double dx = (state & SELECTED) == SELECTED ? corners.getWidth() : 0.;
            double dy = (state & HOVERED) == HOVERED ? corners.getHeight() : 0.;
            corners = corners.move(dx, dy);
            blitRec(matrixStack, coords, blitOffset, pattern);
            corners = corners.move(-dx, -dy);
        } else
            blitRec(matrixStack, coords, blitOffset, pattern);
    }

    @OnlyIn(Dist.CLIENT)
    private void blitRec(MatrixStack matrixStack, Box2d coords, int blitOffset, TextureApplicationPattern pattern) {
        if (pattern == TextureApplicationPattern.MIDDLE_EXPANSION) {
            double mx = coords.getWidth() / corners.getWidth();
            double my = coords.getHeight() / corners.getHeight();
            double mm = Math.max(mx, my);
            if (mm > 1) {
                mx /= mm;
                my /= mm;
            }
            TextureCutout rec = this.copy();
            Box2d tc = coords.copy().scaleWidthAndHeight(0.5, 0.5);
            rec.corners.scaleWidthAndHeight(mx / 2, my / 2);
            rec.blitRec(matrixStack, tc, blitOffset, TextureApplicationPattern.STRETCH);
            rec.corners.move(corners.getWidth() - rec.corners.getWidth(), 0);
            rec.blitRec(matrixStack, tc.move(coords.getWidth() / 2, 0), blitOffset, TextureApplicationPattern.STRETCH);
            rec.corners.move(0, corners.getHeight() - rec.corners.getHeight());
            rec.blitRec(matrixStack, tc.move(0, coords.getHeight() / 2), blitOffset, TextureApplicationPattern.STRETCH);
            rec.corners.move(-(corners.getWidth() - rec.corners.getWidth()), 0);
            rec.blitRec(matrixStack, tc.move(-coords.getWidth() / 2, 0), blitOffset, TextureApplicationPattern.STRETCH);
            return;
        }
        float minU = (float)corners.getX1() / (float)fileWidth;
        float minV = (float)corners.getY1() / (float)fileHeight;
        float maxU = (float)corners.getX2() / (float)fileWidth;
        float maxV = (float)corners.getY2() / (float)fileHeight;
        if (pattern == TextureApplicationPattern.STRETCH) {
            Box2d c = coords.copy().transform(matrixStack.getLast().getMatrix());
            innerBlit(c.getX1(), c.getX2(), c.getY1(), c.getY2(), blitOffset, minU, maxU, minV, maxV);
            if (tint != 0)
                RenderUtils.drawBox(matrixStack, coords, tint, 0);
            return;
        }
        if (pattern == TextureApplicationPattern.TILE) {
            float ratioX = (float)coords.getWidth() / (float)corners.getWidth();
            float ratioY = (float)coords.getHeight() / (float)corners.getHeight();
            for (float y = 0; y < ratioY; y += 1)
                for (float x = 0; x < ratioX; x += 1) {
                    innerBlit(coords.getX1() + x * coords.getWidth(), coords.getX2() + x * coords.getHeight(), coords.getY1() + y * coords.getHeight(), coords.getY2() + y * coords.getHeight(), blitOffset, minU, maxU, minV, maxV);
                    if (tint != 0)
                        RenderUtils.drawBox(matrixStack, coords.copy().move(x * coords.getWidth(), y * coords.getHeight()), tint, 0);
                }
        }
    }
}
