package com.limachi.dimensional_bags.client.entity.layer;

import com.limachi.dimensional_bags.client.entity.model.BagLayerModel;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.items.Bag;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class BagLayer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends BipedArmorLayer<T, M, A> {

    private static final ResourceLocation TEXTURE_BAG = new ResourceLocation(MOD_ID, "textures/entity/bag_entity.png");
    private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");

    private final BagLayerModel<T> model;

    public BagLayer(IEntityRenderer<T, M> renderer, A leggings, A armor) {
        super(renderer, leggings, armor);
        model = new BagLayerModel<>(false);
    }

    public static void copyModelAngles(ModelRenderer modelRendererIn, ModelRenderer modelRendererOut) {
      modelRendererOut.xRot = modelRendererIn.xRot;
      modelRendererOut.yRot = modelRendererIn.yRot;
      modelRendererOut.zRot = modelRendererIn.zRot;
      modelRendererOut.x = modelRendererIn.x;
      modelRendererOut.y = modelRendererIn.y;
      modelRendererOut.z = modelRendererIn.z;
   }

    @Override
    public void render(MatrixStack matrix, IRenderTypeBuffer buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
//        ItemStack itemstack = entity.getItemBySlot(EquipmentSlotType.CHEST);
        if (entity.isInvisible()) return;
        Optional<ImmutableTriple<String, Integer, ItemStack>> s = CuriosApi.getCuriosHelper().findEquippedCurio((Item)Registries.getItem(Bag.NAME), entity);
        ItemStack itemstack = s.isPresent() ? s.get().getRight() : ItemStack.EMPTY;
        int eyeId;
        if (itemstack.getItem() instanceof Bag && (eyeId = Bag.getEyeId(itemstack)) > 0)
        {
            matrix.pushPose();
            getParentModel().copyPropertiesTo(model);
            copyModelAngles(getParentModel().body, model.Body);
            IVertexBuilder builder = ItemRenderer.getFoilBuffer(buffer, this.model.renderType(TEXTURE_BAG), false, false);
            this.model.renderToBuffer(matrix, builder, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f); //FIXME: light and color calculation
            matrix.popPose();
//            ItemStack chestPlate = Bag.getChestPlate(itemstack);
//            if (!chestPlate.isEmpty())
//                super.render(matrix, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
//            ItemStack elytra = Bag.getElytra(itemstack);
//            if (!elytra.isEmpty())
//                renderElytra(getEntityModel(), matrix, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
//            ClientDataManager dataManager = ClientDataManager.getInstance(itemstack);
//            dataManager.onRenderEquippedBag(getEntityModel(), matrix, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
