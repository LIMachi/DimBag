package com.limachi.dimensional_bags.client.render.tileEntity;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot.SlotInventory;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot.SlotTileEntity;
import com.limachi.dimensional_bags.lib.common.tileentities.TEWithUUID;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class SlotTileEntityRenderer extends TileEntityRenderer<SlotTileEntity> {

    public SlotTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) { super(rendererDispatcherIn); }

    public static final float SCALE = 0.875f;

    public static final double CENTER = 0.5d;
    public static final double POSITIVE = 0.975d;
    public static final double NEGATIVE = 0.025d;

    @Override
    public void render(SlotTileEntity te, float p_225616_2_, MatrixStack matrixStack, IRenderTypeBuffer buffer, int p_225616_5_, int p_225616_6_) {
        if (te == null || te.getUUID() == null || te.getUUID() == TEWithUUID.NULL_UUID) return; //missing tile entity or uninitialized inventory (te.getInventory() would cause invalid cast)
        SlotInventory inv = te.getInventory();
        if (inv == null) return;
        ItemStack itemstack = inv.getStackInSlot(0);
        if (itemstack != ItemStack.EMPTY) {
            PlayerEntity player = DimBag.getPlayer();
            if (player != null) {
                ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

                double dx = player.position().x - te.getBlockPos().getX() - 0.5;
                double dy = player.position().y - te.getBlockPos().getY() - 0.5 + player.getEyeHeight();
                double dz = player.position().z - te.getBlockPos().getZ() - 0.5;

                matrixStack.pushPose();

                matrixStack.translate(CENTER, CENTER, CENTER);
                matrixStack.mulPose(Vector3f.YP.rotation((float)MathHelper.atan2(dx, dz)));
                matrixStack.mulPose(Vector3f.XP.rotation(-(float)MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
                matrixStack.scale(SCALE, SCALE, SCALE);

                renderer.renderStatic(itemstack, ItemCameraTransforms.TransformType.FIXED, p_225616_5_, p_225616_6_, matrixStack, buffer);

                matrixStack.popPose();
            }
        }
    }
}
