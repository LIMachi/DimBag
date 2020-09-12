package com.limachi.dimensional_bags.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class TextureCutout {
    public static final Minecraft mc = Minecraft.getInstance();
    public final ResourceLocation file;
    public final Box2d corners;
    public final int fileWidth;
    public final int fileHeight;

    public TextureCutout(ResourceLocation file, int fileWidth, int fileHeight, int x, int y, int w, int h) {
        this.file = file;
        this.fileWidth = fileWidth;
        this.fileHeight = fileHeight;
        this.corners = new Box2d(x, y, w, h);
    }

    public TextureCutout(ResourceLocation file, int x, int y, int w, int h) {
        this.file = file;
        this.fileWidth = 256;
        this.fileHeight = 256;
        this.corners = new Box2d(x, y, w, h);
    }

    public void bindTexture() { mc.getTextureManager().bindTexture(file); }
}
