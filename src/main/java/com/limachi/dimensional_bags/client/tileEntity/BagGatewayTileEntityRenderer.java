package com.limachi.dimensional_bags.client.tileEntity;

//import com.limachi.dimensional_bags.common.tileentities.BagGatewayTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.EndPortalTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/*
@OnlyIn(Dist.CLIENT)
public class BagGatewayTileEntityRenderer extends EndPortalTileEntityRenderer<BagGatewayTileEntity> {

    public BagGatewayTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    public void render(BagGatewayTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        super.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    protected int getPasses(double distanceSquared) { return super.getPasses(distanceSquared) / 2 + 1; }

    protected float getOffset() { return 1.0F; }
}
*/