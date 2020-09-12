package com.limachi.dimensional_bags.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;

import javax.annotation.Nullable;

public class Box2d {
    private int x;
    private int y;
    private int w;
    private int h;

    public Box2d(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.w = width;
        this.h = height;
    }

    public Box2d copy() { return new Box2d(x, y, w, h); }

    public int getX1() { return x; }
    public int getY1() { return y; }
    public int getX2() { return x + w; }
    public int getY2() { return y + h; }
    public int getWidth() { return w; }
    public int getHeight() { return h; }

    public Box2d setX1(int x1) { x = x1; return this; }
    public Box2d setY1(int y1) { y = y1; return this; }
    public Box2d setX2(int x2) { w = x2 - x; return this; }
    public Box2d setY2(int y2) { w = y2 - y; return this; }
    public Box2d setWidth(int width) { w = width; return this; }
    public Box2d setHeight(int height) { h = height; return this; }

    public boolean isInArea(int tx, int ty) { return tx >= x && tx <= x + w && ty >= y && ty <= y + h; }

    public Box2d mergeCut(Box2d area) {
        int x1 = Math.max(x, area.x);
        int y1 = Math.max(y, area.y);
        int x2 = Math.min(getX2(), area.getX2());
        int y2 = Math.min(getY2(), area.getY2());
        return new Box2d(x1, y1, x2 - x1, y2 - y1);
    }

    private void innerBlit(Matrix4f matrix, int x, int x2, int y, int y2, int blitOffset, float minU, float maxU, float minV, float maxV) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(matrix, (float)x, (float)y2, (float)blitOffset).tex(minU, maxV).endVertex();
        bufferbuilder.pos(matrix, (float)x2, (float)y2, (float)blitOffset).tex(maxU, maxV).endVertex();
        bufferbuilder.pos(matrix, (float)x2, (float)y, (float)blitOffset).tex(maxU, minV).endVertex();
        bufferbuilder.pos(matrix, (float)x, (float)y, (float)blitOffset).tex(minU, minV).endVertex();
        bufferbuilder.finishDrawing();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }

    public Box2d blit(MatrixStack matrix, TextureCutout texture, @Nullable Box2d writableArea, int blitOffset) {
        float minU = ((float)texture.corners.x) / (float)texture.fileWidth;
        float minV = ((float)texture.corners.y) / (float)texture.fileHeight;
        float maxU = ((float)texture.corners.getX2()) / (float)texture.fileWidth;
        float maxV = ((float)texture.corners.getY2()) / (float)texture.fileHeight;
        if (writableArea == null || (x >= writableArea.x && y >= writableArea.y && getX2() <= writableArea.getX2() && getY2() <= writableArea.getY2())) {
            innerBlit(matrix.getLast().getMatrix(), x, getX2(), y, getY2(), blitOffset, minU, maxU, minV, maxV);
            return this;
        }
        float cMinU = minU;
        float cMinV = minV;
        float cMaxU = maxU;
        float cMaxV = maxV;
        if (x < writableArea.x) {
            int outside = writableArea.x - x; //1
            float proportion = ((float) outside) / (float)w;
            cMinU += (maxU - minU) * proportion;
        }
        if (y < writableArea.y) {
            int outside = writableArea.y - y;
            float proportion = ((float) outside) / (float)h;
            cMinV += (maxV - minV) * proportion;
        }
        if (getX2() > writableArea.getX2()) {
            int outside = getX2() - writableArea.getX2();
            float proportion = ((float) outside) / (float)w;
            cMaxU -= (maxU - minU) * proportion;
        }
        if (getY2() > writableArea.getY2()) {
            int outside = getY2() - writableArea.getY2();
            float proportion = ((float) outside) / (float)h;
            cMaxV -= (maxV - minV) * proportion;
        }
        innerBlit(matrix.getLast().getMatrix(), Math.max(x, writableArea.x), Math.min(getX2(), writableArea.getX2()), Math.max(y, writableArea.y), Math.min(getY2(), writableArea.getY2()), blitOffset, cMinU, cMaxU, cMinV, cMaxV);
        return this;
    }
}
