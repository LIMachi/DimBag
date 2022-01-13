package com.limachi.dimensional_bags.client.entity.model;

import com.limachi.dimensional_bags.common.bag.BagEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class BagEntityModel extends EntityModel<BagEntity> {

    public final ModelRenderer body;
    private final ModelRenderer bag; //shild of body, used as back
    private final ModelRenderer lid; //shild of body, used as head
    private final ModelRenderer leftPocket; //shild of bag
    private final ModelRenderer rightPocket; //shild of bag
    private final ModelRenderer portal; //portal
    private final boolean equipped;

    public BagEntityModel(boolean equipped) {
        this.equipped = equipped;

        texWidth = 64;
        texHeight = 64;

        body = new ModelRenderer(this);
        body.setPos(0f, 15f, -4f);

        lid = new ModelRenderer(this);
        lid.setPos(0f, -5f, 0f);
        body.addChild(lid);
        lid.texOffs(0, 22).addBox(-6f, equipped ? 0f : -3f, equipped ? 2f : 0f, 12, 3, 7, 0f, false);

        bag = new ModelRenderer(this);
        bag.setPos(0f, 9f, 4f);
        body.addChild(bag);
        bag.texOffs(0, 0).addBox(-6f, equipped ? -11f : -14f, equipped ? -2f : -4f, 12, 14, 8, 0f, false);

        leftPocket = new ModelRenderer(this);
        leftPocket.setPos(0f, 0f, 0f);
        bag.addChild(leftPocket);
        leftPocket.texOffs(28, 32).addBox(6f, equipped ? -8f : -11f, equipped ? 0f : -2f, 2, 10, 4, 0f, false);

        rightPocket = new ModelRenderer(this);
        rightPocket.setPos(0f, 0f, 0f);
        bag.addChild(rightPocket);
        rightPocket.texOffs(16, 32).addBox(-8f, equipped ? -8f : -11f, equipped ? 0f : -2f, 2, 10, 4, 0f, false);

        portal = new ModelRenderer(this);
        portal.setPos(-0.5F, -5.5F, 4f);
        body.addChild(portal);
        portal.texOffs(0, 32).addBox(-3.5F, -16f, 0f, 8, 16, 0, 0f, false);
        portal.visible = false;
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
    public void setupAnim(BagEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        //will do the animation of the opening/closing of the lid/portal there
        boolean lidOpen = false;
        boolean portalOpen = false;

        if (lidOpen)
            setRotationAngle(lid, 1.309F, 0f, 0f);
        else
            setRotationAngle(lid, 0f, 0f, 0f);
        portal.visible = portalOpen;
    }
}