package com.limachi.dim_bag.renderers;

import com.limachi.dim_bag.Constants;
import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.models.BagEntityModel;
import com.limachi.lim_lib.ClientRegistries;
import com.limachi.lim_lib.StaticInitializer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@StaticInitializer.StaticClient
public class BagEntityRenderer extends MobRenderer<BagEntity, BagEntityModel> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/entity/bag_entity.png");

    static {
        ClientRegistries.setEntityRenderer(BagEntity.R_TYPE, BagEntityRenderer::new);
    }

    public BagEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx, new BagEntityModel(ctx.bakeLayer(BagEntityModel.LAYER)), 0.5f); }

    @Override
    public ResourceLocation getTextureLocation(BagEntity entity) { return TEXTURE; }
}
