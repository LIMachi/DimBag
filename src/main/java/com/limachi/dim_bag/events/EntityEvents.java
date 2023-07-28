package com.limachi.dim_bag.events;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_modes.SettingsMode;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.registries.StaticInit;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@StaticInit
@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class EntityEvents {
    public static final KeyMapController.GlobalKeyBinding INVENTORY = new KeyMapController.GlobalKeyBinding("key.categories.inventory:key.inventory", false, ()->()->Minecraft.getInstance().options.keyInventory);
    static {
        KeyMapController.KEY_BINDINGS.add(INVENTORY);
    }

    @SubscribeEvent
    public static void interactWithEquippedBag(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        Player player = event.getEntity();
        if (event.getTarget() instanceof Player targetPlayer) { //bag on players can only be interacted when the other player is not looking towards the player interacting
            Vec3 deltaXZ = targetPlayer.position().subtract(player.position()).multiply(1, 0, 1).normalize();
            if (deltaXZ.dot(targetPlayer.getLookAngle().multiply(1, 0, 1).normalize()) < 0.25)
                return;
            int bag = DimBag.getBagAccess(event.getTarget(), 0, true, false, false, false);
            if (bag > 0) {
                if (KeyMapController.SNEAK.getState(event.getEntity()))
                    BagsData.runOnBag(bag, b->b.enter(event.getEntity(), false));
                else
                    BagMenu.open(event.getEntity(), bag, 0);
            }
        }
    }

    @SubscribeEvent
    public static void openBagInsteadOfInventory(KeyMapController.KeyMapChangedEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && INVENTORY.getState(player)) {
            BagsData.runOnBag(DimBag.getBagAccess(player, 0, false, false, true, true), b->{
                if (b.getModeData(SettingsMode.NAME).getBoolean("bag_instead_of_inventory")) {
                    player.closeContainer();
                    BagMenu.open(player, b.bagId(), 0);
                }
            });
        }
    }
}
