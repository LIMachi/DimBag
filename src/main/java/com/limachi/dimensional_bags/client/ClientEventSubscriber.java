package com.limachi.dimensional_bags.client;

import com.limachi.dimensional_bags.client.screen.InventoryGUI;
import com.limachi.dimensional_bags.client.screen.UpgradeGUI;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventSubscriber {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(Registries.BAG_CONTAINER.get(), InventoryGUI::new);
        ScreenManager.registerFactory(Registries.UPGRADE_CONTAINER.get(), UpgradeGUI::new);
    }
}
