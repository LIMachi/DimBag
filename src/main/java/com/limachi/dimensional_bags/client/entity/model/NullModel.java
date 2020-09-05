package com.limachi.dimensional_bags.client.entity.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.player.PlayerEntity;

public class NullModel<T extends PlayerEntity> extends BipedModel<T> {

    public NullModel() { super(0f); }

    @Override
    public void setRotationAngles(T entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch) {}

    public void setRotationAngle(ModelRenderer renderer, float x, float y, float z) {}

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int overlay, float red, float green, float blue, float alpha) {}
}
