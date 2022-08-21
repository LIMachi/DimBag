package com.limachi.dim_bag.layers;

import com.limachi.dim_bag.models.BagLayerModel;
import com.limachi.dim_bag.renderers.BagEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.NonnullDefault;

@OnlyIn(Dist.CLIENT)
@NonnullDefault
public class BagLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final BagLayerModel<T> model;

    public BagLayer(RenderLayerParent<T, M> parent, EntityModelSet set) {
        super(parent);
        model = new BagLayerModel<>(set.bakeLayer(BagLayerModel.LAYER));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        //FIXME: code to test if equiped
        if (true) {
            getParentModel().copyPropertiesTo(model);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(BagEntityRenderer.TEXTURE), false, false);
            model.renderToBuffer(pose, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        }
    }
}
