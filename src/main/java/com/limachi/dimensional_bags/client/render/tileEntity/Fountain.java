package com.limachi.dimensional_bags.client.render.tileEntity;

import com.limachi.dimensional_bags.client.render.FluidStackRenderer;
import com.limachi.dimensional_bags.client.render.RenderTypes;
import com.limachi.dimensional_bags.common.inventory.FountainTank;
import com.limachi.dimensional_bags.common.tileentities.FountainTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.fluids.FluidStack;

public class Fountain extends TileEntityRenderer<FountainTileEntity> {

    public Fountain(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    /*
    * shape: centered on 7.5, 1, 7.5 of the block
    * v = 7 * fullness
    * main fluid quad -> -7, v, -7, 7, v, 7 of still fluid (looking up)
    */

    @Override
    public void render(FountainTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        FountainTank tank = tileEntityIn.getTank();
        if (tank != null) {
            FluidStack fs = tank.getFluid();
            if (!fs.isEmpty()) {
                IVertexBuilder buffer = bufferIn.getBuffer(RenderTypes.FOUNTAIN_FLUID);
                matrixStackIn.push();
                matrixStackIn.translate(7.5, 1, 7.5);
                Matrix4f mat = matrixStackIn.getLast().getMatrix();
                float fullness = ((float) tank.getFluidAmount()) / (float) tank.getCapacity() * 7.f;
                TextureAtlasSprite stillFluid = FluidStackRenderer.getFluidSprite(tank.getFluid(), false);
                if (fullness < 6.999) {
                    TextureAtlasSprite flowingFluid = FluidStackRenderer.getFluidSprite(tank.getFluid(), true);
                    //render the center still overlay (top) FIXME: UV probably is wrong
                    buffer.pos(mat, -2, 7, 2).color(1, 1, 1, 1).tex(stillFluid.getMinU(), stillFluid.getMaxV()).endVertex();
                    buffer.pos(mat, 2, 7, 2).color(1, 1, 1, 1).tex(stillFluid.getMaxU(), stillFluid.getMaxV()).endVertex();
                    buffer.pos(mat, 2, 7, -2).color(1, 1, 1, 1).tex(stillFluid.getMaxU(), stillFluid.getMinV()).endVertex();
                    buffer.pos(mat, -2, 7, -2).color(1, 1, 1, 1).tex(stillFluid.getMinU(), stillFluid.getMinV()).endVertex();
                    //render the center flowing overlay (sides)
                }
                //render the still overlay
                buffer.pos(mat, -7, fullness, 7).color(1, 1, 1, 1).tex(stillFluid.getMinU(), stillFluid.getMaxV()).endVertex();
                buffer.pos(mat, 7, fullness, 7).color(1, 1, 1, 1).tex(stillFluid.getMaxU(), stillFluid.getMaxV()).endVertex();
                buffer.pos(mat, 7, fullness, -7).color(1, 1, 1, 1).tex(stillFluid.getMaxU(), stillFluid.getMinV()).endVertex();
                buffer.pos(mat, -7, fullness, -7).color(1, 1, 1, 1).tex(stillFluid.getMinU(), stillFluid.getMinV()).endVertex();
                matrixStackIn.pop();
            }
        }
    }
}
