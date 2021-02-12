package com.limachi.dimensional_bags.client.entity.layer;

import com.limachi.dimensional_bags.client.entity.model.BagLayerModel;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.items.Bag;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class BagLayer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends BipedArmorLayer<T, M, A> {

    private static final ResourceLocation TEXTURE_BAG = new ResourceLocation(MOD_ID, "textures/entity/bag_entity.png");
    private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");

    private final BagLayerModel<T> model;

    public BagLayer(IEntityRenderer<T, M> renderer, A leggings, A armor) {
        super(renderer, leggings, armor);
        model = new BagLayerModel<>(false);
    }

    protected void renderElytra(BipedModel<T> entityModel, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemstack = entity.getItemStackFromSlot(EquipmentSlotType.CHEST);
        ResourceLocation resourcelocation;
        if (entity instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity abstractclientplayerentity = (AbstractClientPlayerEntity)entity;
            if (abstractclientplayerentity.isPlayerInfoSet() && abstractclientplayerentity.getLocationElytra() != null) {
                resourcelocation = abstractclientplayerentity.getLocationElytra();
            } else if (abstractclientplayerentity.hasPlayerInfo() && abstractclientplayerentity.getLocationCape() != null && abstractclientplayerentity.isWearing(PlayerModelPart.CAPE)) {
                resourcelocation = abstractclientplayerentity.getLocationCape();
            } else {
                resourcelocation = TEXTURE_ELYTRA;
            }
        } else {
            resourcelocation = TEXTURE_ELYTRA;
        }
        matrixStackIn.push();
        matrixStackIn.translate(0, -0.11D, 0.565D);
        if (entity.isSneaking())
            matrixStackIn.translate(0.0D, 0.-0.24D, -0.1D);
        matrixStackIn.scale(1.1f, 1.1f, 1.1f);
        ElytraModel<T> modelElytra = new ElytraModel<>();
        entityModel.copyModelAttributesTo(modelElytra);
        modelElytra.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        IVertexBuilder ivertexbuilder = ItemRenderer.getArmorVertexBuilder(bufferIn, RenderType.getArmorCutoutNoCull(resourcelocation), false, itemstack.hasEffect());
        modelElytra.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStackIn.pop();
    }

    @Override
    public void render(MatrixStack matrix, IRenderTypeBuffer buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemstack = entity.getItemStackFromSlot(EquipmentSlotType.CHEST);
        int eyeId;
        if (itemstack.getItem() instanceof Bag && (eyeId = Bag.getEyeId(itemstack)) > 0)
        {
            matrix.push();
            getEntityModel().setModelAttributes(model);
            this.model.Body.copyModelAngles(this.getEntityModel().bipedBody);
            IVertexBuilder builder = ItemRenderer.getBuffer(buffer, this.model.getRenderType(TEXTURE_BAG), false, false); //IRenderTypeBuffer bufferIn, RenderType renderTypeIn, boolean isItemIn, boolean glintIn
            this.model.render(matrix, builder, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f); //FIXME: light and color calculation
            matrix.pop();
            ItemStack chestPlate = Bag.getChestPlate(itemstack);
            if (!chestPlate.isEmpty())
                super.render(matrix, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            ItemStack elytra = Bag.getElytra(itemstack);
            if (!elytra.isEmpty())
                renderElytra(getEntityModel(), matrix, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            ClientDataManager dataManager = ClientDataManager.getInstance(itemstack);
            dataManager.onRenderEquippedBag(getEntityModel(), matrix, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
