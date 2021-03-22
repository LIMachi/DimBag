package com.limachi.dimensional_bags.client.widgets;

import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.mojang.blaze3d.matrix.MatrixStack;

public class Image extends Base {
    TextureCutout[] frames;
    int frameRate;
    int currentFrame = 0;

    public Image(Base parent, double x, double y, double w, double h, int frameRate, TextureCutout ...frames) {
        super(parent, x, y, w, h, true);
        this.frames = frames;
        this.frameRate = frameRate;
    }

    private static TextureCutout[] framesFromSpriteArea(TextureCutout area, int spriteWidth, int spriteHeight) {
        int xs = (int)area.corners.getWidth() / spriteWidth;
        int ys = (int)area.corners.getHeight() / spriteHeight;
        TextureCutout[] out = new TextureCutout[xs * ys];
        for (int y = 0; y < ys; ++y)
            for (int x = 0; x < xs; ++x) {
                out[x + y * xs] = new TextureCutout(area.file, area.fileWidth, area.fileHeight, (int)(area.corners.getX1() + (spriteWidth * x)), (int)(area.corners.getY1() + (spriteHeight * y)), spriteWidth, spriteHeight);
            }
        return out;
    }

    public Image(Base parent, double x, double y, double w, double h, int frameRate, TextureCutout area, int spriteWidth, int spriteHeight) {
        this(parent, x, y, w, h, frameRate, framesFromSpriteArea(area, spriteWidth, spriteHeight));
    }

    public Image(Base parent, double x, double y, double w, double h, TextureCutout image) { this(parent, x, y, w, h, 0, image); }

    @Override
    public void onTick(int tick) {
        if (frameRate > 0 && tick % frameRate == 0)
            currentFrame = (currentFrame + 1) % frames.length;
    }

    @Override
    public void onRender(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        frames[currentFrame].blit(matrixStack, coords, getBlitOffset(), true, TextureCutout.TextureApplicationPattern.STRETCH);
    }
}
