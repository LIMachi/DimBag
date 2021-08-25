package com.limachi.dimensional_bags.client.render;

import com.limachi.dimensional_bags.DimBag;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderTypes {
    public static final RenderType FOUNTAIN_FLUID = RenderType.create(DimBag.MOD_ID + ".fountain_fluid", DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 128, RenderType.State.builder().setTextureState(new RenderState.TextureState()).setWriteMaskState(new RenderState.WriteMaskState(false, true)).createCompositeState(false));
}
