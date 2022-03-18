package com.limachi.dimensional_bags.client.entity.layer;

import com.limachi.dimensional_bags.client.entity.model.BagEntityModel;
import com.limachi.dimensional_bags.lib.common.worldData.IBagIdHolder;
import com.limachi.dimensional_bags.common.bag.GhostBagItem;
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
import net.minecraft.util.ResourceLocation;
import top.theillusivec4.curios.api.CuriosApi;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class BagLayer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends BipedArmorLayer<T, M, A> {

    private static final ResourceLocation TEXTURE_BAG = new ResourceLocation(MOD_ID, "textures/entity/bag_entity.png");

    private final BagEntityModel model;

    public BagLayer(IEntityRenderer<T, M> renderer, A leggings, A armor) {
        super(renderer, leggings, armor);
        model = new BagEntityModel(true);
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
        if (entity.isInvisible()) return;
        CuriosApi.getCuriosHelper().findEquippedCurio(t->!(t.getItem() instanceof GhostBagItem) && t.getTag() != null && t.getTag().getInt(IBagIdHolder.BAG_ID) != 0, entity)
                .ifPresent(f -> CuriosApi.getCuriosHelper().getCuriosHandler(entity).resolve()
                        .ifPresent(h -> h.getStacksHandler(f.getLeft())
                                .ifPresent(c -> {
            if (c.getRenders().get(f.getMiddle())) {
//                int eye = f.getRight().getTag().getInt(IBagIdHolder.EYE_ID_KEY);
                matrix.pushPose();
                model.attackTime = getParentModel().attackTime;
                model.riding = getParentModel().riding;
                model.young = getParentModel().young;
                copyModelAngles(getParentModel().body, model.body);
                IVertexBuilder builder = ItemRenderer.getFoilBuffer(buffer, this.model.renderType(TEXTURE_BAG), false, false);
                this.model.renderToBuffer(matrix, builder, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
                matrix.popPose();
            }
        })));
    }
}
