package com.limachi.dim_bag.client.renderers.entities;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.client.models.BagEntityModel;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@StaticInitClient
public class BagEntityRenderer extends MobRenderer<BagEntity, BagEntityModel<BagEntity>> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(DimBag.MOD_ID, "textures/entity/bag_entity.png");

    static {
        ClientRegistries.setEntityRenderer(BagEntity.R_TYPE, BagEntityRenderer::new);
    }

    public BagEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx, new BagEntityModel<BagEntity>(ctx.bakeLayer(BagEntityModel.LAYER_LOCATION)), 0.5f); }

    @Override
    public ResourceLocation getTextureLocation(BagEntity entity) { return TEXTURE; }
}
