package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * fluid handler with a single tank that can only be full or empty, no in-between (only accepts fill and drain which would completely fill or drain the tank)
 */
public class BinaryStateSingleFluidHandler implements IFluidHandler, IFluidTank {

    FluidStack stack;
    final int capacity;
    Predicate<FluidStack> validator;

    public static final Set<BinaryStateSingleFluidHandlerItem> REGISTERED_HANDLERS = new HashSet<>();
    public static final BinaryStateSingleFluidHandlerItem WATER_BOTTLE = BinaryStateSingleFluidHandlerItem.registerPotion(Fluids.WATER, Potions.WATER);
    public static final BinaryStateSingleFluidHandlerItem DEBUG_TEST_1 = BinaryStateSingleFluidHandlerItem.registerPotion(Fluids.LAVA, Potions.LONG_FIRE_RESISTANCE);
    public static final BinaryStateSingleFluidHandlerItem DEBUG_TEST_2 = BinaryStateSingleFluidHandlerItem.registerSplashPotion(Fluids.FLOWING_LAVA, Potions.LONG_FIRE_RESISTANCE);

    @ObjectHolder("pneumaticcraft:memory_essence")
    public static final Fluid pneumaticcraft_xp_fluid = null;
    @ObjectHolder("cofh_core:experience")
    public static final Fluid cofh_xp_fluid = null;
    @ObjectHolder("industrial_foregoing:memory_essence")
    public static final Fluid industrial_foregoing_xp_fluid = null;
    public static ArrayList<BinaryStateSingleFluidHandlerItem> XP_BOTTLES = new ArrayList<>();

    /**
     * xp conversion by default: 1xp -> 20mb, 1 bottle -> 250mb ~> 12.5xp, 1 bottle broken -> 3-11 ~7 (in accordance with other mods, if it was me, a bottle would cost less mb to fill)
     */

    static {
        if (pneumaticcraft_xp_fluid != null)
            XP_BOTTLES.add(BinaryStateSingleFluidHandlerItem.registerBottle(pneumaticcraft_xp_fluid, 250, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        if (cofh_xp_fluid != null)
            XP_BOTTLES.add(BinaryStateSingleFluidHandlerItem.registerBottle(cofh_xp_fluid, 250, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        if (industrial_foregoing_xp_fluid != null)
            XP_BOTTLES.add(BinaryStateSingleFluidHandlerItem.registerBottle(industrial_foregoing_xp_fluid, 250, new ItemStack(Items.EXPERIENCE_BOTTLE)));
    }

    @ObjectHolder("create:honey")
    public static final Fluid create_honey_fluid = null;
    @ObjectHolder("cofh_core:honey")
    public static final Fluid cofh_honey_fluid = null;
    @ObjectHolder("resourcefulbees:honey")
    public static final Fluid resourcefulbees_honey_fluid = null;
    public static ArrayList<BinaryStateSingleFluidHandlerItem> HONEY_BOTTLES = new ArrayList<>();

    static {
        if (create_honey_fluid != null)
            HONEY_BOTTLES.add(BinaryStateSingleFluidHandlerItem.registerBottle(create_honey_fluid, 250, new ItemStack(Items.HONEY_BOTTLE)));
        if (cofh_honey_fluid != null)
            HONEY_BOTTLES.add(BinaryStateSingleFluidHandlerItem.registerBottle(cofh_honey_fluid, 250, new ItemStack(Items.HONEY_BOTTLE)));
        if (resourcefulbees_honey_fluid != null)
            HONEY_BOTTLES.add(BinaryStateSingleFluidHandlerItem.registerBottle(resourcefulbees_honey_fluid, 250, new ItemStack(Items.HONEY_BOTTLE)));
    }

    @ObjectHolder("mekanism:oxygen")
    public static final Fluid mekanism_air_fluid = null;

    /**
     * try to find a valid BinaryStateSingleFluidHandlerItem for the given fluid and item (example: if you have a glass bottle and water or a water bottle and water, will return WATER_BOTTLE)
     * target item will be tested against full and empty state of the BinaryStateSingleFluidHandlerItem, while the fluid will only be tested using the validator in BinaryStateSingleFluidHandler
     * the returned value is either null (not found) or an initialized instance (either full or empty, based on if the itemstack matched the full or empty state)
     */
    public static BinaryStateSingleFluidHandlerItem getItemHandler(FluidStack targetFluid, ItemStack targetItem) {
        for (BinaryStateSingleFluidHandlerItem t : REGISTERED_HANDLERS)
            if (t.isFluidValid(targetFluid) && (targetItem.equals(t.full, false) || targetItem.equals(t.empty, false))) {
                if (targetItem.equals(t.full, false))
                    return t.fullInstance();
                else
                    return t.emptyInstance();
            }
        return null;
    }

    public static class BinaryStateSingleFluidHandlerItem extends BinaryStateSingleFluidHandler implements IFluidHandlerItem {

        private final ItemStack full;
        private final ItemStack empty;

        private BinaryStateSingleFluidHandlerItem(FluidStack fullFluid, ItemStack fullItem, ItemStack emptyItem, boolean full, boolean isRegistering) {
            super(full ? fullFluid : FluidStack.EMPTY, fullFluid.getAmount(), fs->fs.getFluid().equals(fullFluid.getFluid()));
            this.full = fullItem;
            this.empty = emptyItem;
            if (isRegistering)
                REGISTERED_HANDLERS.add(this);
        }

        private static BinaryStateSingleFluidHandlerItem registerBottle(Fluid fluid, int mb, ItemStack filled) { return new BinaryStateSingleFluidHandlerItem(new FluidStack(fluid, mb), filled, new ItemStack(Items.GLASS_BOTTLE), true, true); }
        /**
         * potions are standardised to use 333mb of fluid, their empty state is always a glass_bottle, and their full state is a Potion item with potion nbt
         */
        private static BinaryStateSingleFluidHandlerItem registerPotion(Fluid fluid, Potion potion) { return new BinaryStateSingleFluidHandlerItem(new FluidStack(fluid, 333), PotionUtils.setPotion(new ItemStack(Items.POTION), potion), new ItemStack(Items.GLASS_BOTTLE), true, true); }
        private static BinaryStateSingleFluidHandlerItem registerSplashPotion(Fluid fluid, Potion potion) { return new BinaryStateSingleFluidHandlerItem(new FluidStack(fluid, 333), PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion), new ItemStack(Items.GLASS_BOTTLE), true, true); }
        private static BinaryStateSingleFluidHandlerItem registerLingeringPotion(Fluid fluid, Potion potion) { return new BinaryStateSingleFluidHandlerItem(new FluidStack(fluid, 333), PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion), new ItemStack(Items.GLASS_BOTTLE), true, true); }

        public BinaryStateSingleFluidHandlerItem fullInstance() { return new BinaryStateSingleFluidHandlerItem(stack, full, empty, true, false); }

        public BinaryStateSingleFluidHandlerItem emptyInstance() { return new BinaryStateSingleFluidHandlerItem(stack, full, empty, false, false); }

        @Nonnull
        @Override
        public ItemStack getContainer() {
            if (stack.isEmpty())
                return empty.copy();
            return full.copy();
        }
    }

    /**
     * default constructor for any fluid, starts empty
     * @param capacity
     */
    public BinaryStateSingleFluidHandler(int capacity) { this(FluidStack.EMPTY, capacity, fs->true); }

    public BinaryStateSingleFluidHandler(FluidStack stack, int capacity, Predicate<FluidStack> validator) {
        this.stack = stack.copy();
        if (!stack.isEmpty() && stack.getAmount() != capacity)
            this.stack.setAmount(capacity);
        this.capacity = capacity;
        this.validator = validator;
    }

    @Override
    public int getTanks() { return 1; }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return stack; }

    @Override
    public int getTankCapacity(int tank) { return capacity; }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return isFluidValid(stack); }

    @Nonnull
    @Override
    public FluidStack getFluid() { return stack; }

    @Override
    public int getFluidAmount() { return stack.getAmount(); }

    @Override
    public int getCapacity() { return capacity; }

    @Override
    public boolean isFluidValid(FluidStack stack) { return stack.isEmpty() || validator.test(stack); }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!stack.isEmpty() || resource.getAmount() < capacity || !isFluidValid(0, resource)) return 0;
        if (action.execute()) {
            stack = resource.copy();
            stack.setAmount(capacity);
        }
        return capacity;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (stack.isEmpty() || resource.getAmount() < capacity || !resource.isFluidEqual(stack)) return FluidStack.EMPTY;
        FluidStack out = stack.copy();
        if (action.execute())
            stack = FluidStack.EMPTY;
        return out;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (stack.isEmpty() || maxDrain < capacity) return FluidStack.EMPTY;
        FluidStack out = stack.copy();
        if (action.execute())
            stack = FluidStack.EMPTY;
        return out;
    }
}
