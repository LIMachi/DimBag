package com.limachi.dim_bag.utils;

import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.ItemFluidContainer;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class FluidItem extends ItemFluidContainer {

    @RegisterItem(tab = "minecraft:op_blocks")
    public static RegistryObject<Item> R_ITEM;

    public FluidItem() { super(new Properties().stacksTo(0), Integer.MAX_VALUE); }

    public static IFluidHandlerItem getHandler(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(()->new IllegalStateException("Missing FLUID_HANDLER_ITEM capability in FluidItem"));
    }

    @Nonnull
    @Override
    public Component getName(@Nonnull ItemStack stack) {
        FluidStack fluid = getHandler(stack).getFluidInTank(0);
        if (fluid.isEmpty())
            return Component.translatable("item.fluid_item.empty_name");
        return ((MutableComponent)fluid.getDisplayName()).append(Component.translatable("item.fluid_item.append_qty_to_name", fluid.getAmount()));
    }

    public static ItemStack fromFluid(FluidStack fluidStack) {
        ItemStack out = new ItemStack(R_ITEM.get(), 1);
        getHandler(out).fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        return out;
    }
}
