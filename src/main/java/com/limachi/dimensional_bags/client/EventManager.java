package com.limachi.dimensional_bags.client;

import com.google.common.collect.ArrayListMultimap;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.items.TunnelPlacer;
import com.limachi.dimensional_bags.utils.TextUtils;
//import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javafx.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class EventManager {

    @Config
    static boolean NBT_TOOLTIP = true;

    @SubscribeEvent /** small helper event to add extra tooltip information on items with nbt (and remove the vanilla text for nbt) */
    public static void addExtendedTooltip(ItemTooltipEvent event) {
        if (NBT_TOOLTIP && event.getItemStack().getTag() != null && event.getFlags().isAdvanced() && event.getPlayer() instanceof ClientPlayerEntity && Screen.hasControlDown()) {
            List<ITextComponent> tooltip = event.getToolTip();
            ITextComponent remove = null;
            for (ITextComponent t : tooltip)
                if (t.getString().matches("NBT: [0-9]+ tag\\(s\\)")) {
                    remove = t;
                    break;
                }
            if (remove != null)
                tooltip.remove(remove);
            tooltip.add(TextUtils.prettyNBT(event.getItemStack().getTag()));
        }
    }

    /*
    @SubscribeEvent
    public static void addBagInformationToOverlay(RenderGameOverlayEvent.Pre event) { //might be used at some point to add hud ellements
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            PlayerEntity player = DimBag.getPlayer();
            ItemStack mainHand = player.getMainHandItem();
            if (!mainHand.isEmpty() && (mainHand.getItem() instanceof Bag || mainHand.getItem() instanceof GhostBag)) {
                ClientDataManager data = GhostBag.getClientData(mainHand);
                if (data != null) {
                    data.onRenderHud((ClientPlayerEntity)player, event.getWindow(), event.getMatrixStack(), event.getPartialTicks());
                    return;
                }
            }
            ItemStack offHand = player.getOffHandItem();
            if (!offHand.isEmpty() && (offHand.getItem() instanceof Bag || offHand.getItem() instanceof GhostBag)) {
                ClientDataManager data = GhostBag.getClientData(offHand);
                if (data != null) {
                    data.onRenderHud((ClientPlayerEntity)player, event.getWindow(), event.getMatrixStack(), event.getPartialTicks());
                    return;
                }
            }
            ItemStack chestPlate = player.getItemBySlot(EquipmentSlotType.CHEST);
            if (!chestPlate.isEmpty() && (chestPlate.getItem() instanceof Bag || chestPlate.getItem() instanceof GhostBag)) {
                ClientDataManager data = GhostBag.getClientData(chestPlate);
                if (data != null) {
                    data.onRenderHud((ClientPlayerEntity)player, event.getWindow(), event.getMatrixStack(), event.getPartialTicks());
                    return;
                }
            }
        }
    }
    */

    @SubscribeEvent
    static void removeHighlightOnTunnelPlacer(DrawHighlightEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        if (player.getMainHandItem().getItem() instanceof TunnelPlacer || player.getOffhandItem().getItem() instanceof TunnelPlacer)
            event.setCanceled(true);
    }

    public static class RenderTypes extends RenderType {
        public static final RenderType TunnelPlacerOverlayRenderType = create("TunnelPlacerBlockOverlay",
                DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
                RenderType.State.builder()
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(NO_TEXTURE)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setCullState(CULL)
                        .setLightmapState(NO_LIGHTMAP)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(false));

        public RenderTypes(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
            super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
        }
    }

    public static void renderTunnelPlacerOverlay(Matrix4f matrix, IVertexBuilder builder, BlockPos pos, Color color) {
        float red = color.getRed() / 255f, green = color.getGreen() / 255f, blue = color.getBlue() / 255f, alpha = .125f;

        float startX = 0, startY = 0, startZ = -1, endX = 1, endY = 1, endZ = 0;

        //down
        builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //up
        builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();

        //east
        builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();

        //west
        builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();

        //south
        builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //north
        builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
    }

    @SubscribeEvent
    static void addRoomDependentHighlightOnTunnelPlacer(RenderWorldLastEvent event) {
        final Minecraft mc = Minecraft.getInstance();
        final ClientPlayerEntity player = mc.player;
        if (player == null) return;
        if (player.getMainHandItem().getItem() instanceof TunnelPlacer || player.getOffhandItem().getItem() instanceof TunnelPlacer) {
            List<Pair<BlockPos, Boolean>> coords = SubRoomsManager.collectPlacerOverlays(player, player.getMainHandItem().getItem() instanceof TunnelPlacer ? player.getMainHandItem() : player.getOffhandItem());
            if (coords.isEmpty()) return;
            IRenderTypeBuffer.Impl rtb = mc.renderBuffers().bufferSource();
            Vector3d b = mc.gameRenderer.getMainCamera().getPosition();
            MatrixStack matrix = event.getMatrixStack();
            matrix.pushPose();
            matrix.translate(-b.x(), -b.y(), -b.z());
            IVertexBuilder builder;
            builder = rtb.getBuffer(RenderTypes.TunnelPlacerOverlayRenderType);
            coords.forEach(p -> {
                BlockPos pos = p.getKey();
                matrix.pushPose();
                matrix.translate(pos.getX(), pos.getY(), pos.getZ());
                matrix.translate(-0.0005f, -0.0005f, -0.0005f);
                matrix.scale(1.001f, 1.001f, 1.001f);
                matrix.mulPose(Vector3f.YP.rotationDegrees(-90.0F));

                Matrix4f positionMatrix = matrix.last().pose();
                renderTunnelPlacerOverlay(positionMatrix, builder, pos, p.getValue() ? Color.GREEN : Color.RED);
                matrix.popPose();
            });
            matrix.popPose();
            RenderSystem.disableDepthTest();
            rtb.endBatch(RenderTypes.TunnelPlacerOverlayRenderType);
        }
    }

    private static int tick = 0;
    private static ArrayListMultimap<Integer, Runnable> pendingTasks = ArrayListMultimap.create();

    public static <T> void delayedTask(int ticksToWait, Runnable run) { pendingTasks.put(ticksToWait + tick, run); }

    public static int getTick() { return tick; }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            List<Runnable> tasks = pendingTasks.get(tick);
            if (tasks != null)
                for (Runnable task : tasks)
                    task.run();
        } else if (event.phase == TickEvent.Phase.END) pendingTasks.removeAll(tick);
        ++tick;
    }
}
