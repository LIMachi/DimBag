package com.limachi.dimensional_bags.lib.common.patches.vanilla;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class FluidCapabilities {

    @SubscribeEvent
    public static void addFluidCapability(AttachCapabilitiesEvent<ItemStack> event) {
        Item item = event.getObject().getItem();
//        if (false)
//            event.addCapability(, );
    }
}
