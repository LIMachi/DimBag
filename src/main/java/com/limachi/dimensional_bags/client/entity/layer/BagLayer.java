package com.limachi.dimensional_bags.client.entity.layer;

import com.limachi.dimensional_bags.client.entity.model.BagLayerModel;
import com.limachi.dimensional_bags.common.items.Bag;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class BagLayer<T extends PlayerEntity, M extends BipedModel<T>> extends LayerRenderer<T, M> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID, "textures/entity/bag_entity.png");

    private BagLayerModel<T> model;

    public BagLayer(IEntityRenderer<T, M> renderer, BagLayerModel<T> model) {
        super(renderer);
        this.model = model;
    }

    @Override
    public void render(MatrixStack matrix, IRenderTypeBuffer buffer, int packedLight, T player, float v, float v1, float v2, float v3, float v4, float v5) {
        Item item = player.inventory.armorItemInSlot(EquipmentSlotType.CHEST.getIndex()).getItem();
        if (item instanceof Bag)
        {
            matrix.push();
            getEntityModel().setModelAttributes(model);
            this.model.Body.copyModelAngles(this.getEntityModel().bipedBody);
            IVertexBuilder builder = ItemRenderer.getBuffer(buffer, this.model.getRenderType(TEXTURE), false, false); //IRenderTypeBuffer bufferIn, RenderType renderTypeIn, boolean isItemIn, boolean glintIn
            this.model.render(matrix, builder, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f); //FIXME: light and color calculation
            matrix.pop();
        }
    }
}
