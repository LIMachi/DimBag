package com.limachi.dimensional_bags.client.entity.render;

import com.limachi.dimensional_bags.client.entity.model.BagEntityModel;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class BagEntityRender extends MobRenderer<BagEntity, BagEntityModel> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID, "textures/entity/bag_entity.png");

    public BagEntityRender(EntityRendererManager renderManager) {
        super(renderManager, new BagEntityModel(false,false), 0.5f);
    }

    @Override
    public void render(BagEntity entity, float yaw, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int packeLight) {
        super.render(entity, yaw, partialTicks, matrix, buffer, packeLight);
        matrix.push();
        //do the extra render here
        matrix.pop();
    }

    @Override
    public ResourceLocation getEntityTexture(BagEntity entity) { return TEXTURE; }
}
