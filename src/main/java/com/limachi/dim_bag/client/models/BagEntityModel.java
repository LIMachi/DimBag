package com.limachi.dim_bag.client.models;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

@StaticInitClient
public class BagEntityModel<T extends LivingEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DimBag.MOD_ID, "bag_model"), "main");

    static {
        ClientRegistries.setLayerDefinition(LAYER_LOCATION, BagEntityModel::createBodyLayer);
    }

    protected final ModelPart all;
    protected final ModelPart lid;
    protected final ModelPart left_handle;
    protected final ModelPart right_handle;

    public BagEntityModel(ModelPart root) {
        all = root.getChild("body");
        lid = all.getChild("lid");
        left_handle = all.getChild("handles").getChild("left_handle");
        right_handle = all.getChild("handles").getChild("right_handle");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        CubeDeformation nullDeformation = new CubeDeformation(0.0F);
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0f, 24f, -3f));

        body.addOrReplaceChild("main", CubeListBuilder.create().texOffs(40, 0).addBox(6f, -12f, -1f, 2f, 11f, 4f, nullDeformation)
                .texOffs(52, 0).addBox(-8f, -12f, -1f, 2f, 11f, 4f, nullDeformation)
                .texOffs(0, 0).addBox(-6f, -12f, -3f, 12f, 12f, 8f, nullDeformation), PartPose.offset(0f, 0f, 0f));

        body.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 20).addBox(-6f, -3f, 0f, 12f, 3f, 8f, nullDeformation)
                .texOffs(32, 22).addBox(-2f, -2f, 8f, 4f, 4f, 2f, nullDeformation), PartPose.offset(0f, -12f, -3f));

        PartDefinition handles = body.addOrReplaceChild("handles", CubeListBuilder.create(), PartPose.offset(0f, 0f, 0f));

        handles.addOrReplaceChild("right_handle", CubeListBuilder.create().texOffs(0, 31).addBox(-1f, 4f, -4f, 2f, 1f, 5f, nullDeformation)
                .texOffs(14, 31).addBox(-1f, -5f, -4f, 2f, 1f, 5f, nullDeformation)
                .texOffs(28, 31).addBox(-1f, -4f, -5f, 2f, 8f, 1f, nullDeformation), PartPose.offset(4f, -6f, -2f));

        handles.addOrReplaceChild("left_handle", CubeListBuilder.create().texOffs(0, 37).addBox(-1f, 4f, -4f, 2f, 1f, 5f, nullDeformation)
                .texOffs(14, 37).addBox(-1f, -5f, -4f, 2f, 1f, 5f, nullDeformation)
                .texOffs(34, 31).addBox(-1f, -4f, -5f, 2f, 8f, 1f, nullDeformation), PartPose.offset(-4f, -6f, -2f));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(@Nonnull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof BagEntity bag) {
            //do nothing?
            return;
        }
    }

    public <M extends HumanoidModel<?>> void copyPropertiesFromHumanoid(M parent) {
        all.copyFrom(parent.body);
    }

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        all.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}