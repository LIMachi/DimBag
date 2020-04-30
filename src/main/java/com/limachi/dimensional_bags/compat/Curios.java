package com.limachi.dimensional_bags.compat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

public class Curios {
    public static final Curios INSTANCE = new Curios();
    public static final String BACKPACK_SLOT_ID = "backpack";
    public static final ResourceLocation BACKPACK_SLOT_ICON = new ResourceLocation("assets/" + CuriosAPI.MODID + "textures/item/empty_" + BACKPACK_SLOT_ID + "_slot.png");
    @CapabilityInject(ICurioItemHandler.class)
    public static final Capability<ICurioItemHandler> INVENTORY = null;
    @CapabilityInject(ICurio.class)
    public static final Capability<ICurio> CURIO = null;

    public void registerBagSlot() {
        InterModComms.sendTo(CuriosAPI.MODID, CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage(BACKPACK_SLOT_ID).setSize(1));
        InterModComms.sendTo(CuriosAPI.MODID, CuriosAPI.IMC.REGISTER_ICON, () -> new Tuple<>(BACKPACK_SLOT_ID, BACKPACK_SLOT_ICON));
    }

    public static ItemStack getStack(LivingEntity entity, String slot_name, int slot_index) {
        return CuriosAPI.getCuriosHandler(entity).map(h->h.getStackInSlot(slot_name, slot_index)).orElse(new ItemStack(Items.AIR, 0));
    }

    public static void setStack(LivingEntity entity, String slot_name, int slot_index, ItemStack stack) {
        CuriosAPI.getCuriosHandler(entity).ifPresent(h->h.setStackInSlot(slot_name, slot_index, stack));
    }

    public <T>LazyOptional<T> getCapability(Capability<T> capability) {
        return capability == CURIO ? LazyOptional.of(DimBagCurioCap::new).cast() : LazyOptional.empty();
    }

    public static class DimBagCurioCap implements ICurio {

    }
}
