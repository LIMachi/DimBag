package com.limachi.dim_bag.models;

import com.limachi.dim_bag.Constants;
import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.lim_lib.ClientRegistries;
import com.limachi.lim_lib.StaticInitializer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

@StaticInitializer.StaticClient
public class BagEntityModel extends EntityModel<BagEntity> {
    public static final ResourceLocation MODEL = new ResourceLocation(Constants.MOD_ID, "bag_entity");
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(MODEL, "ground");
    private final ModelPart bag;
    private final ModelPart lid;

    public BagEntityModel(ModelPart root) {
        bag = root.getChild("bag");
        lid = root.getChild("lid");
    }

    static {
        ClientRegistries.setLayerDefinition(LAYER, ()->createBodyLayer(false));
    }

    public static LayerDefinition createBodyLayer(boolean onPlayer) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        CubeDeformation def = new CubeDeformation(0f);
        PartPose pose = PartPose.offset(0f, onPlayer ? 0f : 8f, onPlayer ? 0f : -6f);
        partdefinition.addOrReplaceChild("bag", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6f, 2f, 2f, 12f, 14f, 8f, def)
                        .texOffs(13, 34).addBox(-8f, 6f, 2f, 2f, 10f, 4f, def)
                        .texOffs(0, 34).addBox(6f, 6f, 2f, 2f, 10f, 4f, def), pose);
        partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 23).addBox(-6f, -1f, 2f, 12f, 3f, 7f, def), pose);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bag.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        lid.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void setupAnim(BagEntity entity, float p_102619_, float p_102620_, float p_102621_, float p_102622_, float p_102623_) {
        boolean lidOpen = false; //FIXME: code to test if bag is open
        if (lidOpen)
            lid.xRot = 1.3f;
        else
            lid.xRot = 0f;
    }
}
