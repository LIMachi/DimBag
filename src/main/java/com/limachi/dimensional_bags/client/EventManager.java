package com.limachi.dimensional_bags.client;

import com.google.common.collect.ArrayListMultimap;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.utils.TextUtils;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class EventManager {

    @Config
    static boolean NBT_TOOLTIP = true;

    @SubscribeEvent /** small helper event to add extra tooltip information on items with nbt (and remove the vanilla text for nbt) */
    public static void onToolTip(ItemTooltipEvent event) {
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

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) { //might be used at some point to add hud ellements
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            PlayerEntity player = DimBag.getPlayer();
            ItemStack mainHand = player.getHeldItemMainhand();
            if (!mainHand.isEmpty() && (mainHand.getItem() instanceof Bag || mainHand.getItem() instanceof GhostBag)) {
                ClientDataManager data = GhostBag.getClientData(mainHand, player);
                if (data != null) {
                    data.onRenderHud((ClientPlayerEntity)player, event.getWindow(), event.getMatrixStack(), event.getPartialTicks());
                    return;
                }
            }
            ItemStack offHand = player.getHeldItemOffhand();
            if (!offHand.isEmpty() && (offHand.getItem() instanceof Bag || offHand.getItem() instanceof GhostBag)) {
                ClientDataManager data = GhostBag.getClientData(offHand, player);
                if (data != null) {
                    data.onRenderHud((ClientPlayerEntity)player, event.getWindow(), event.getMatrixStack(), event.getPartialTicks());
                    return;
                }
            }
            ItemStack chestPlate = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (!chestPlate.isEmpty() && (chestPlate.getItem() instanceof Bag || chestPlate.getItem() instanceof GhostBag)) {
                ClientDataManager data = GhostBag.getClientData(chestPlate, player);
                if (data != null) {
                    data.onRenderHud((ClientPlayerEntity)player, event.getWindow(), event.getMatrixStack(), event.getPartialTicks());
                    return;
                }
            }
        }
    }

    private static int tick = 0;
    private static ArrayListMultimap<Integer, Runnable> pendingTasks = ArrayListMultimap.create();

    public static <T> void delayedTask(int ticksToWait, Runnable run) { pendingTasks.put(ticksToWait + tick, run); }

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
