package com.limachi.dimensional_bags.client.entity.layer;

import com.limachi.dimensional_bags.client.entity.model.BagEntityModel;
import com.limachi.dimensional_bags.client.entity.model.BagLayerModel;
import com.limachi.dimensional_bags.client.entity.render.BagEntityRender;
import com.limachi.dimensional_bags.common.IMC.curios.Curios;
import com.limachi.dimensional_bags.common.items.Bag;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;
import static net.minecraft.client.renderer.LightTexture.packLight;

public class BagLayer<T extends PlayerEntity, M extends BipedModel<T>> extends LayerRenderer<T, M> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID, "textures/entity/bag_entity.png");

    private BagLayerModel<T> model;

    public BagLayer(IEntityRenderer<T, M> renderer, BagLayerModel<T> model) {
        super(renderer);
        this.model = model;
    }

    @Override
    public void render(MatrixStack matrix, IRenderTypeBuffer buffer, int i, T player, float v, float v1, float v2, float v3, float v4, float v5) {
        //for now, try to render the bag if the item bag is selected, after integration, will only render when the bag is equiped
//        LOGGER.info("player render called");
//        Item item = player.inventory.getCurrentItem().getItem();
        Item item = Curios.getStack(player, Curios.BACKPACK_SLOT_ID, 0).getItem();
        if (item instanceof Bag)
        {
            matrix.push();
            getEntityModel().setModelAttributes(model);
            this.model.Body.copyModelAngles(this.getEntityModel().bipedBody);
            IVertexBuilder builder = ItemRenderer.getBuffer(buffer, this.model.getRenderType(TEXTURE), false, false); //IRenderTypeBuffer bufferIn, RenderType renderTypeIn, boolean isItemIn, boolean glintIn
            this.model.render(matrix, builder, i, packLight(0, 0), 1f, 1f, 1f, 1f);
            matrix.pop();
        }
    }
}
