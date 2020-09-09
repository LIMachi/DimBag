package com.limachi.dimensional_bags.client;

import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.IDimBagCommonItem;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import com.limachi.dimensional_bags.common.network.packets.ChangeModeRequest;
import com.limachi.dimensional_bags.common.network.packets.EmptyRightClick;
import com.limachi.dimensional_bags.common.network.packets.UseModeRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class EventManager {

    private static boolean openGuiKeyPressed = false;
    private static boolean openGuiKeyUsed = false;

    private static void openGuiKey(PlayerEntity player, boolean used) {
        if (openGuiKeyPressed && !used) {
            if (openGuiKeyUsed)
                openGuiKeyUsed = false;
            else {
                IDimBagCommonItem.ItemSearchResult res = IDimBagCommonItem.searchItem(player, 0, Bag.class, (x)->true);
                if (res == null) return;
                int bagId = Bag.getId(res.stack);
                if (bagId != 0) {
                    PacketHandler.toServer(new UseModeRequest(player, res.index));
                }
            }
        }
        openGuiKeyPressed = used;
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) { //might be used at some point to add hud ellements

    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseInputEvent event) {
        KeyMapController.syncKeyMap(event.getButton(), 0, true, event.getAction() == GLFW.GLFW_PRESS || event.getAction() == GLFW.GLFW_REPEAT);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        KeyMapController.syncKeyMap(event.getKey(), event.getScanCode(), false, event.getAction() == GLFW.GLFW_PRESS || event.getAction() == GLFW.GLFW_REPEAT);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickEmpty event) { //detect empty hand right click, client side only (will send it to the server so it can simulate the right click with an empty stack in hand)
        if (event.getHand() == Hand.MAIN_HAND)
            PacketHandler.toServer(new EmptyRightClick());
    }

    @SubscribeEvent
    public static void onScrollInput(InputEvent.MouseScrollEvent event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) return;
        double scroll = event.getScrollDelta();
        long window = mc.getMainWindow().getHandle();

        if (KeyMapController.getKey(null, KeyMapController.CROUCH_KEY)) {
            if (player.inventory.mainInventory.get(player.inventory.currentItem).getItem() instanceof Bag) {
                PacketHandler.toServer(new ChangeModeRequest(player, player.inventory.currentItem, scroll > 0));
                event.setCanceled(true);
            }
            else if (player.inventory.offHandInventory.get(0).getItem() instanceof Bag) {
                PacketHandler.toServer(new ChangeModeRequest(player, player.inventory.mainInventory.size(), scroll > 0));
                event.setCanceled(true);
            }
            return;
        }
        if (KeyMapController.getKey(null, KeyMapController.BAG_ACTION_KEY)) {
            openGuiKeyUsed = true;
            int p = IDimBagCommonItem.getFirstValidItemFromPlayer(player, Bag.class, (x)->true);;
            if (p != -1) {
                PacketHandler.toServer(new ChangeModeRequest(player, p, scroll > 0));
                event.setCanceled(true);
            }
            return;
        }
    }
}
