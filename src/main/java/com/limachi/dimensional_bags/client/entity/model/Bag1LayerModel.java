package com.limachi.dimensional_bags.client.entity.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.player.PlayerEntity;

public class Bag1LayerModel<T extends PlayerEntity> extends BipedModel<T> {
    public final ModelRenderer VoxelShapes;

    public Bag1LayerModel() {
        super(0f);
        textureWidth = 128;
        textureHeight = 128;

        VoxelShapes = new ModelRenderer(this);
        VoxelShapes.setRotationPoint(4.0F, 16.0F, 0.0F);
        VoxelShapes.setTextureOffset(0, 73).addBox(4.0F, -2.0F, -5.0F, 2, 8, 10, 0.0F, false);
        VoxelShapes.setTextureOffset(74, 13).addBox(4.0F, -8.0F, -5.0F, 2, 5, 10, 0.0F, false);
        VoxelShapes.setTextureOffset(46, 48).addBox(2.0F, -9.0F, -6.0F, 2, 16, 12, 0.0F, false);
        VoxelShapes.setTextureOffset(28, 28).addBox(1.0F, -10.0F, -7.0F, 1, 18, 14, 0.0F, false);
        VoxelShapes.setTextureOffset(44, 0).addBox(-3.0F, -10.0F, -7.0F, 1, 18, 14, 0.0F, false);
        VoxelShapes.setTextureOffset(0, 46).addBox(-2.0F, -10.0F, -7.0F, 3, 13, 14, 0.0F, false);
        VoxelShapes.setTextureOffset(60, 62).addBox(0.0F, 6.0F, -7.0F, 1, 1, 14, 0.0F, false);
        VoxelShapes.setTextureOffset(20, 62).addBox(-2.0F, 6.0F, -7.0F, 1, 1, 14, 0.0F, false);
        VoxelShapes.setTextureOffset(58, 32).addBox(-2.0F, 7.0F, -7.0F, 3, 1, 14, 0.0F, false);
        VoxelShapes.setTextureOffset(60, 0).addBox(-1.0F, 6.0F, -6.0F, 1, 1, 12, 0.0F, false);
        VoxelShapes.setTextureOffset(26, 0).addBox(-2.0F, 3.0F, -5.0F, 3, 3, 10, 0.0F, false);
        VoxelShapes.setTextureOffset(0, 0).addBox(-8.0F, -11.0F, -8.0F, 5, 19, 16, 0.0F, false);
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        VoxelShapes.render(matrixStack, buffer, packedLight, overlay, red, green, blue, alpha);
    }
}
