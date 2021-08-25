package com.limachi.dimensional_bags.client.entity.model;

import com.google.common.collect.ImmutableList;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class BagEntityModel extends SegmentedModel<BagEntity> {

    public final ModelRenderer body;
    private final ModelRenderer bag; //shild of body, used as back
    private final ModelRenderer lid; //shild of body, used as head
    private final ModelRenderer leftPocket; //shild of bag
    private final ModelRenderer rightPocket; //shild of bag
    private final ModelRenderer portal; //portal

    public BagEntityModel(boolean lidOpen, boolean portalOpen) { //same model for the portal, entity and player layer
        //<part> = new ModelRenderer(this);
        //<part>.setRotationPoint(posX, posY, posZ)
        //<part>.setTextureOffset(tx, ty).addBox(poxX, posY, posZ, sizeX, sizeY, sizeZ, scale, mirrored)
        //<part>.addChild(<sub-part>);

        texWidth = 64;
        texHeight = 64;

        body = new ModelRenderer(this);
        body.setPos(0.0F, 15.0F, -4.0F);

        lid = new ModelRenderer(this);
        lid.setPos(0.0F, -5.0F, 0.0F);
        if (lidOpen) //missing opening/closing lid animation
            setRotationAngle(lid, 1.309F, 0.0F, 0.0F);
        body.addChild(lid);
        lid.texOffs(0, 22).addBox(-6.0F, -3.0F, 0.0F, 12, 3, 7, 0.0F, false);

        bag = new ModelRenderer(this);
        bag.setPos(0.0F, 9.0F, 4.0F);
        body.addChild(bag);
        bag.texOffs(0, 0).addBox(-6.0F, -14.0F, -4.0F, 12, 14, 8, 0.0F, false);

        leftPocket = new ModelRenderer(this);
        leftPocket.setPos(0.0F, 0.0F, 0.0F);
        bag.addChild(leftPocket);
        leftPocket.texOffs(28, 32).addBox(6.0F, -11.0F, -2.0F, 2, 10, 4, 0.0F, false);

        rightPocket = new ModelRenderer(this);
        rightPocket.setPos(0.0F, 0.0F, 0.0F);
        bag.addChild(rightPocket);
        rightPocket.texOffs(16, 32).addBox(-8.0F, -11.0F, -2.0F, 2, 10, 4, 0.0F, false);

        if (portalOpen) { //missing fade-in/fade-out animation
            portal = new ModelRenderer(this);
            portal.setPos(-0.5F, -5.5F, 4.0F);
            body.addChild(portal);
            portal.texOffs(0, 32).addBox(-3.5F, -16.0F, 0.0F, 8, 16, 0, 0.0F, false);
        } else
            portal = null;
    }

    public void setRotationAngle(ModelRenderer renderer, float x, float y, float z) {
        renderer.xRot = x;
        renderer.yRot = y;
        renderer.zRot = z;
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int overlay, float red, float green, float blue, float alpha) {
        body.render(matrixStack, buffer, packedLight, overlay, red, green, blue, alpha);
    }

    @Override
    public Iterable<ModelRenderer> parts() {
        return ImmutableList.of(body);
    }

    @Override
    public void setupAnim(BagEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        //will do the animation of the opening/closing of the lid/portal there
    }
}