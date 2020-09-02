package com.limachi.dimensional_bags.client;

import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import com.limachi.dimensional_bags.common.network.packets.ChangeModeRequest;
import com.limachi.dimensional_bags.common.network.packets.OpenGuiRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import static com.limachi.dimensional_bags.client.ClientEventSubscriber.openGuiKey;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class EventManager {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        int key = event.getKey();
        boolean used = event.getAction() == GLFW.GLFW_PRESS || event.getAction() == GLFW.GLFW_REPEAT;
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        if (key == openGuiKey.getKey().getKeyCode()) {
            int bagPos = Bag.getFirstValidBag(player);
            int bagId = Bag.getId(player, bagPos);
            if (bagId != 0) {
                PacketHandler.toServer(new OpenGuiRequest(player, bagId));
            }
        }
    }

    @SubscribeEvent
    public static void onScrollInput(InputEvent.MouseScrollEvent event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) return;
        double scroll = event.getScrollDelta();
        long window = mc.getMainWindow().getHandle();
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
            if (player.inventory.mainInventory.get(player.inventory.currentItem).getItem() instanceof Bag) {
                PacketHandler.toServer(new ChangeModeRequest(player, player.inventory.currentItem, scroll > 0));
                event.setCanceled(true);
            }
            else if (player.inventory.offHandInventory.get(0).getItem() instanceof Bag) {
                PacketHandler.toServer(new ChangeModeRequest(player, player.inventory.mainInventory.size(), scroll > 0));
                event.setCanceled(true);
            }
        }
    }
}
