package com.limachi.utils;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class NbtTooltip {

    @SubscribeEvent
    public static void addExtendedTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getTag() != null && event.getFlags().isAdvanced()) {
            if (Screen.hasAltDown()) {
                List<Component> tooltip = event.getToolTip();
                Component remove = null;
                for (Component t : tooltip)
                    if (t.getString().matches("NBT: [0-9]+ tag\\(s\\)")) {
                        remove = t;
                        break;
                    }
                if (remove != null)
                    tooltip.remove(remove);
                TextUtils.prettyTagTooltip(tooltip, event.getItemStack().getTag());

            } else
                event.getToolTip().add(new TranslatableComponent("extended_tooltip.nbt_tooltip.use_alt"));
        }
    }
}
