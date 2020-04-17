package com.limachi.dimensional_bags.common.IMC.curios;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.api.capability.ICurioItemHandler;
import top.theillusivec4.curios.api.imc.CurioIMCMessage;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;

import java.util.SortedMap;

public class Curios {
    public static final Curios INSTANCE = new Curios();
    public static final String BACKPACK_SLOT_ID = "backpack";
    public static final ResourceLocation BACKPACK_SLOT_ICON = new ResourceLocation("assets/" + CuriosAPI.MODID + "textures/item/empty_" + BACKPACK_SLOT_ID + "_slot.png");
    @CapabilityInject(ICurioItemHandler.class)
    public static final Capability<ICurioItemHandler> INVENTORY = null;
    @CapabilityInject(ICurio.class)
    public static final Capability<ICurio> CURIO = null;

    public static void registerBagSlot() {
        InterModComms.sendTo(CuriosAPI.MODID, CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage(BACKPACK_SLOT_ID).setSize(1));
        InterModComms.sendTo(CuriosAPI.MODID, CuriosAPI.IMC.REGISTER_ICON, () -> new Tuple<>(BACKPACK_SLOT_ID, BACKPACK_SLOT_ICON));
    }

    public static ItemStack getStack(PlayerEntity player, String slot_id, int slot_index) {
        if (INVENTORY == null)
            return ItemStack.EMPTY;
        return player.getCapability(INVENTORY, null).map(iCurioItemHandler -> {
            SortedMap<String, CurioStackHandler> map = iCurioItemHandler.getCurioMap();
            if (map == null || map.get(slot_id) == null)
                return ItemStack.EMPTY;
            return map.get(slot_id).getStackInSlot(slot_index);
        }).orElse(ItemStack.EMPTY);
    }

    public static <T>LazyOptional<T> getCapability(Capability<T> capability) {
        return capability == CURIO ? LazyOptional.of(DimBagCurioCap::new).cast() : LazyOptional.empty();
    }

    public static class DimBagCurioCap implements ICurio {}
}
