package com.limachi.dim_bag.client.layers;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.client.models.BagEntityModel;
import com.limachi.dim_bag.client.renderers.entities.BagEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.system.NonnullDefault;

@OnlyIn(Dist.CLIENT)
@NonnullDefault
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = DimBag.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BagLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    @SubscribeEvent
    static void registerLayersRenderers(EntityRenderersEvent.AddLayers event) {
        for (String rp : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(rp);
            if (renderer != null)
                renderer.addLayer(new BagLayer<>(renderer, event.getEntityModels()));
        }
    }

    private final BagEntityModel<T> model;

    public BagLayer(RenderLayerParent<T, M> parent, EntityModelSet set) {
        super(parent);
        model = new BagEntityModel<T>(set.bakeLayer(BagEntityModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isInvisible() && DimBag.getBagAccess(entity, 0, true, false, false) > 0) {
            pose.pushPose();
            getParentModel().copyPropertiesTo(model);
            if (getParentModel() instanceof HumanoidModel<?> humanoid)
                model.copyPropertiesFromHumanoid(humanoid);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            pose.translate(0f, 0.5f, 0.1f);
            VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(BagEntityRenderer.TEXTURE), false, false);
            model.renderToBuffer(pose, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            pose.popPose();
        }
    }
}
