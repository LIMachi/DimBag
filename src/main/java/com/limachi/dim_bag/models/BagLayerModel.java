package com.limachi.dim_bag.models;

import com.limachi.dim_bag.DimBag;
import com.limachi.utils.ClientRegistries;
import com.limachi.utils.StaticInitializer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

@StaticInitializer.StaticClient
public class BagLayerModel<T extends LivingEntity> extends EntityModel<T> {
    public static final ResourceLocation MODEL = new ResourceLocation(DimBag.MOD_ID, "bag_entity");
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(MODEL, "equipped");
    private final ModelPart bag;
    private final ModelPart lid;

    public BagLayerModel(ModelPart root) {
        bag = root.getChild("bag");
        lid = root.getChild("lid");
    }

    static {
        ClientRegistries.setLayerDefinition(LAYER, ()->BagEntityModel.createBodyLayer(true));
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bag.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        lid.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void setupAnim(T entity, float p_102619_, float p_102620_, float p_102621_, float p_102622_, float p_102623_) {
        boolean lidOpen = false; //FIXME: code to test if bag is open
        if (lidOpen)
            lid.xRot = 1.3f;
        else
            lid.xRot = 0f;
    }
}
