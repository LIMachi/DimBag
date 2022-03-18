package com.limachi.dimensional_bags.client.render.tileEntity;

import com.limachi.dimensional_bags.common.bagDimensionOnly.bagTank.TankInventory;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagTank.TankTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class TankTileEntityRenderer extends TileEntityRenderer<TankTileEntity> {

    public TankTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    public static final double BLOCK_PIXEL = 1d/16d;

    @Override
    public void render(TankTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (tileEntityIn == null || tileEntityIn.isRemoved()) return;
        TankInventory tank = tileEntityIn.getTank();
        if (tank != null) {
            FluidStack fs = tank.getFluid();
            if (!fs.isEmpty()) {
                FluidAttributes attributes = fs.getFluid().getAttributes();
                TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(attributes.getStillTexture(fs));
                IVertexBuilder buffer = bufferIn.getBuffer(RenderType.translucent());
                int color = attributes.getColor();
                int r = color >> 16 & 0xFF;
                int g = color >> 8 & 0xFF;
                int b = color & 0xFF;
                float qty = (((float) tank.getFluidAmount()) / (float) tank.getCapacity());

                float v1 = sprite.getV(16 * qty);

                matrixStackIn.pushPose();
                matrixStackIn.scale((float)(14 * BLOCK_PIXEL), (float)(14 * BLOCK_PIXEL), (float)(14 * BLOCK_PIXEL));
                matrixStackIn.translate(BLOCK_PIXEL, BLOCK_PIXEL, BLOCK_PIXEL);

                Matrix4f matrix = matrixStackIn.last().pose();
                Matrix3f normal = matrixStackIn.last().normal();

                //top
                buffer.vertex(matrix, 0, qty, 1).color(r, g, b, 255).uv(sprite.getU0(), sprite.getV1()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
                buffer.vertex(matrix, 1, qty, 1).color(r, g, b, 255).uv(sprite.getU1(), sprite.getV1()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
                buffer.vertex(matrix, 1, qty, 0).color(r, g, b, 255).uv(sprite.getU1(), sprite.getV0()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
                buffer.vertex(matrix, 0, qty, 0).color(r, g, b, 255).uv(sprite.getU0(), sprite.getV0()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 1, 0).endVertex();
                //bottom
                buffer.vertex(matrix, 1, 0, 1).color(r, g, b, 255).uv(sprite.getU0(), sprite.getV1()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, -1, 0).endVertex();
                buffer.vertex(matrix, 0, 0, 1).color(r, g, b, 255).uv(sprite.getU1(), sprite.getV1()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, -1, 0).endVertex();
                buffer.vertex(matrix, 0, 0, 0).color(r, g, b, 255).uv(sprite.getU1(), sprite.getV0()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, -1, 0).endVertex();
                buffer.vertex(matrix, 1, 0, 0).color(r, g, b, 255).uv(sprite.getU0(), sprite.getV0()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, -1, 0).endVertex();
                //north
                buffer.vertex(matrix, 1, qty, 1).color(r, g, b, 255).uv(sprite.getU0(), v1).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 0, 1).endVertex();
                buffer.vertex(matrix, 0, qty, 1).color(r, g, b, 255).uv(sprite.getU1(), v1).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 0, 1).endVertex();
                buffer.vertex(matrix, 0, 0, 1).color(r, g, b, 255).uv(sprite.getU1(), sprite.getV0()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 0, 1).endVertex();
                buffer.vertex(matrix, 1, 0, 1).color(r, g, b, 255).uv(sprite.getU0(), sprite.getV0()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 0, 1).endVertex();
                //south
                buffer.vertex(matrix, 1, 0, 0).color(r, g, b, 255).uv(sprite.getU0(), v1).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 0, -1).endVertex();
                buffer.vertex(matrix, 0, 0, 0).color(r, g, b, 255).uv(sprite.getU1(), v1).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 0, -1).endVertex();
                buffer.vertex(matrix, 0, qty, 0).color(r, g, b, 255).uv(sprite.getU1(), sprite.getV0()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 0, -1).endVertex();
                buffer.vertex(matrix, 1, qty, 0).color(r, g, b, 255).uv(sprite.getU0(), sprite.getV0()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 0, 0, -1).endVertex();
                //east
                buffer.vertex(matrix, 1, qty, 0).color(r, g, b, 255).uv(sprite.getU0(), v1).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 1, 0, 0).endVertex();
                buffer.vertex(matrix, 1, qty, 1).color(r, g, b, 255).uv(sprite.getU1(), v1).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 1, 0, 0).endVertex();
                buffer.vertex(matrix, 1, 0, 1).color(r, g, b, 255).uv(sprite.getU1(), sprite.getV0()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 1, 0, 0).endVertex();
                buffer.vertex(matrix, 1, 0, 0).color(r, g, b, 255).uv(sprite.getU0(), sprite.getV0()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, 1, 0, 0).endVertex();
                //west
                buffer.vertex(matrix, 0, qty, 1).color(r, g, b, 255).uv(sprite.getU0(), v1).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, -1, 0, 0).endVertex();
                buffer.vertex(matrix, 0, qty, 0).color(r, g, b, 255).uv(sprite.getU1(), v1).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, -1, 0, 0).endVertex();
                buffer.vertex(matrix, 0, 0, 0).color(r, g, b, 255).uv(sprite.getU1(), sprite.getV0()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, -1, 0, 0).endVertex();
                buffer.vertex(matrix, 0, 0, 1).color(r, g, b, 255).uv(sprite.getU0(), sprite.getV0()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(normal, -1, 0, 0).endVertex();

                matrixStackIn.popPose();
            }
        }
    }
}
