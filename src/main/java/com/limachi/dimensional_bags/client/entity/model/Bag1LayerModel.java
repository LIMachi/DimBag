package com.limachi.dimensional_bags.client.entity.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.player.PlayerEntity;

public class Bag1LayerModel<T extends PlayerEntity> extends BipedModel<T> {
    public final ModelRenderer Body;
    public final ModelRenderer Bag;
//    public final ModelRenderer PlayerBody; //for reference

    public Bag1LayerModel() {
        super(0f);
        //Body.setTextureOffset(28, 35).addBox(-7.0F + deltaX, -19.0F + deltaY, 3.0F + deltaZ, 3, 1, 1, 0.0F, false);

        textureWidth = 64;
        textureHeight = 64;

        Body = new ModelRenderer(this);
        Body.setRotationPoint(0f, 0f, 0f);

        Bag = new ModelRenderer(this);
        Bag.setRotationPoint(0f, 0f, 2f);
        Body.addChild(Bag);
        Bag.setTextureOffset(0, 13).addBox(-4.0F, 1.0F, 4.0F, 8, 3, 1, 0.0F, false);
        Bag.setTextureOffset(20, 17).addBox(-7.0F, 4.0F, 1.0F, 14, 1, 3, 0.0F, false);
        Bag.setTextureOffset(0, 0).addBox(-4.0F, 4.0F, 0.0F, 8, 8, 5, 0.0F, false);
        Bag.setTextureOffset(0, 33).addBox(-3.0F, 6.0F, 5.0F, 6, 5, 1, 0.0F, false);
        Bag.setTextureOffset(42, 13).addBox(-2.0F, 2.0F, 6.0F, 4, 2, 1, 0.0F, false);
        Bag.setTextureOffset(0, 11).addBox(-2.0F, 1.0F, 1.0F, 4, 3, 3, 0.0F, false);
        Bag.setTextureOffset(42, 0).addBox(-3.0F, 1.0F, 5.0F, 6, 4, 1, 0.0F, false);
        Bag.setTextureOffset(0, 39).addBox(4.0F, 5.0F, 1.0F, 2, 6, 3, 0.0F, false);
        Bag.setTextureOffset(36, 36).addBox(-6.0F, 5.0F, 1.0F, 2, 6, 3, 0.0F, false);
        Bag.setTextureOffset(32, 31).addBox(-3.0F, 12.0F, 1.0F, 6, 1, 3, 0.0F, false);
        Bag.setTextureOffset(24, 24).addBox(-4.0F, -1.0F, 0.0F, 8, 1, 6, 0.0F, false);
        Bag.setTextureOffset(10, 40).addBox(-2.0F, 5.0F, 5.0F, 4, 1, 1, 0.0F, false);
        Bag.setTextureOffset(10, 40).addBox(-2.0F, 0.0F, 5.0F, 4, 1, 1, 0.0F, false);
        Bag.setTextureOffset(36, 29).addBox(-2.0F, -2.0F, 5.0F, 1, 1, 1, 0.0F, false);
        Bag.setTextureOffset(36, 29).addBox(-2.0F, -1.0F, 6.0F, 1, 1, 1, 0.0F, false);
        Bag.setTextureOffset(36, 29).addBox(1.0F, -2.0F, 5.0F, 1, 1, 1, 0.0F, false);
        Bag.setTextureOffset(0, 48).addBox(-4.0F, 2.0F, 1.0F, 8, 2, 3, 0.0F, false);
        Bag.setTextureOffset(36, 29).addBox(1.0F, -1.0F, 6.0F, 1, 1, 1, 0.0F, false);
        Bag.setTextureOffset(10, 46).addBox(-3.0F, 7.0F, 6.0F, 3, 1, 1, 0.0F, false);
        Bag.setTextureOffset(10, 46).addBox(0.0F, 7.0F, 6.0F, 3, 1, 1, 0.0F, false);
        Bag.setTextureOffset(10, 39).addBox(1.0F, 6.0F, 6.0F, 1, 3, 1, 0.0F, false);
        Bag.setTextureOffset(10, 39).addBox(-2.0F, 6.0F, 6.0F, 1, 3, 1, 0.0F, false);
        Bag.setTextureOffset(0, 0).addBox(-4.0F, 0.0F, 0.0F, 8, 1, 5, 0.0F, false);
        Bag.setTextureOffset(0, 13).addBox(-4.0F, 1.0F, 0.0F, 8, 3, 1, 0.0F, false);

//        PlayerBody = new ModelRenderer(this);
//        PlayerBody.setRotationPoint(0.0F, 0.0F, 2.0F);
//        Body.addChild(PlayerBody);
//        PlayerBody.setTextureOffset(0, 0).addBox(-4.0F, 0.0F, -4.0F, 8, 12, 4, 0.0F, false);
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        Body.render(matrixStack, buffer, packedLight, overlay, red, green, blue, alpha);
    }
}
