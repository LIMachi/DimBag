package com.limachi.dimensional_bags.client.entity.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class BagLayerModel<T extends LivingEntity> extends BipedModel<T> {

    public final ModelRenderer Body;
    private final ModelRenderer Bag; //shild of body, used as back
    private final ModelRenderer Lid; //shild of body, used as head
    private final ModelRenderer LeftPocket; //shild of bag
    private final ModelRenderer RightPocket; //shild of bag

    public BagLayerModel(boolean lidOpen) { //same model for the portal, entity and player layer
        super(0f);

        textureWidth = 64;
        textureHeight = 64;

        Body = new ModelRenderer(this);
        Body.setRotationPoint(0.0F, 15.0F, -4.0F);

        Lid = new ModelRenderer(this);
        Lid.setRotationPoint(0.0F, -5.0F, 0.0F);
        if (lidOpen) //missing opening/closing lid animation
            setRotationAngle(Lid, 1.309F, 0.0F, 0.0F);
        Body.addChild(Lid);
        Lid.setTextureOffset(0, 22).addBox(-6.0F, 0F, 2.0F, 12, 3, 7, 0.0F, false);

        Bag = new ModelRenderer(this);
        Bag.setRotationPoint(0.0F, 9.0F, 4.0F);
        Body.addChild(Bag);
        Bag.setTextureOffset(0, 0).addBox(-6.0F, -11.0F, -2.0F, 12, 14, 8, 0.0F, false);

        LeftPocket = new ModelRenderer(this);
        LeftPocket.setRotationPoint(0.0F, 0.0F, 0.0F);
        Bag.addChild(LeftPocket);
        LeftPocket.setTextureOffset(28, 32).addBox(6.0F, -8.0F, 0F, 2, 10, 4, 0.0F, false);

        RightPocket = new ModelRenderer(this);
        RightPocket.setRotationPoint(0.0F, 0.0F, 0.0F);
        Bag.addChild(RightPocket);
        RightPocket.setTextureOffset(16, 32).addBox(-8.0F, -8.0F, 0F, 2, 10, 4, 0.0F, false);
    }

    @Override
    public void setRotationAngles(T entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch) {
        //will do the lid animation latter
    }

    public void setRotationAngle(ModelRenderer renderer, float x, float y, float z) {
        renderer.rotateAngleX = x;
        renderer.rotateAngleY = y;
        renderer.rotateAngleZ = z;
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        Body.render(matrixStack, buffer, packedLight, overlay, red, green, blue, alpha);
    }
}
