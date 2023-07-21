package com.limachi.dim_bag.client.renderers.block_entitites;

import com.limachi.dim_bag.bag_modules.block_entity.SlotModuleBlockEntity;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.Sides;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@StaticInitClient
public class SlotBlockEntityRenderer implements BlockEntityRenderer<SlotModuleBlockEntity> {

    static {
        ClientRegistries.setBer(SlotModuleBlockEntity.R_TYPE, SlotBlockEntityRenderer::new);
    }

    public static final float SCALE = 0.875f;

    public static final double CENTER = 0.5d;

    protected final BlockEntityRendererProvider.Context ctx;

    protected SlotBlockEntityRenderer(@Nonnull BlockEntityRendererProvider.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void render(@Nonnull SlotModuleBlockEntity be, float f, @Nonnull PoseStack pose, @Nonnull MultiBufferSource buffer, int i1, int i2) {
        BlockPos pos = be.getBlockPos();
        ItemStack stack = be.renderStack;
        if (!stack.isEmpty() && Sides.getPlayer() instanceof LocalPlayer player) {
            pose.pushPose();
            pose.translate(CENTER, CENTER, CENTER);
            if (!KeyMapController.SNEAK.getState(player)) {
                double dx = player.position().x - pos.getX() - 0.5;
                double dy = player.position().y - pos.getY() - 0.5 + player.getEyeHeight();
                double dz = player.position().z - pos.getZ() - 0.5;
                pose.mulPose(Axis.YP.rotation((float) Math.atan2(dx, dz)));
                pose.mulPose(Axis.XP.rotation(-(float) Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
            }
            pose.scale(SCALE, SCALE, SCALE);
            ctx.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, i1, i2, pose, buffer, be.getLevel(), 0);
            pose.popPose();
        }
    }
}
