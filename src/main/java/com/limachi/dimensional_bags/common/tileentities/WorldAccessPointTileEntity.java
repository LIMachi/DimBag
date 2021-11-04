package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.ConfigManager;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.WorldAccessPoint;
import com.limachi.dimensional_bags.common.container.BaseContainer;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.utils.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventoryProvider;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@StaticInit
public class WorldAccessPointTileEntity extends BaseTileEntity implements IEnergyStorage, IItemHandler, IFluidHandler, IisBagTE {

    public static final String NAME = "world_access_point";

    static {
        Registries.registerTileEntity(NAME, WorldAccessPointTileEntity::new, ()->Registries.getBlock(WorldAccessPoint.NAME), null);
    }

    private int cooldown = 0; //'holdItem' will be used only once every X ticks, cooldown is set to X after rightclick
    private List<WeakReference<ItemEntity>> worldItems = new ArrayList<>(); //weak reference list updated every tick
    private WeakReference<IInventory> inv = new WeakReference<>(null); //weakreference to the closest inventory (directly below/above/on bag)
    private WeakReference<Entity> lastKnownHolder = new WeakReference<>(null);
    private boolean isEquipped;

    public static final int RADIUS_EQUIPPED = 5;
    public static final int DEFAULT_RADIUS = 2;

    @ConfigManager.Config
    public static final int MAX_ENERGY = 4096;
    @ConfigManager.Config
    public static final int ENERGY_USAGE_PER_TICK = 0;
    @ConfigManager.Config
    public static final int ENERGY_USAGE_PER_ACTION = 0;
    @ConfigManager.Config
    public static final int COOLDOWN = 10;

    public WorldAccessPointTileEntity() { super(Registries.getBlockEntityType(NAME)); }

    public ItemStack getItemHold() { return ItemStack.of(getTileData().getCompound("ItemHold")); }

    public void setItemHold(ItemStack item) { item.save(getTileData().getCompound("ItemHold")); setChanged(); }

    public int getEnergy() { return getTileData().getInt("Energy"); }

    public void setEnergy(int energy) { getTileData().putInt("Energy", energy); setChanged(); }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || cap == CapabilityEnergy.ENERGY || cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return LazyOptional.of(()->this).cast();
        return super.getCapability(cap, side);
    }

    @Override //the total size of the cached inventory is 1 reserver slot + number of items visible by the bag + remote inventory. the radius of detection and if the remote inventory should be accessible is dependent on the bag state (equiped vs inworld)
    public void tick(int tick) {
        int eye = SubRoomsManager.getEyeId(level, worldPosition, false);

        if (eye > 0) {
            Entity e = HolderData.getInstance(SubRoomsManager.getEyeId(level, worldPosition, false)).getEntity();
            if (e == null) return;
            lastKnownHolder = new WeakReference<>(e);
            isEquipped = !(e instanceof BagEntity);

            if (isEquipped) {
                List<ItemEntity> tl = e.level.getEntitiesOfClass(ItemEntity.class, new AxisAlignedBB(e.blockPosition().offset(-RADIUS_EQUIPPED, -RADIUS_EQUIPPED, -RADIUS_EQUIPPED), e.blockPosition().offset(RADIUS_EQUIPPED, RADIUS_EQUIPPED, RADIUS_EQUIPPED)), f -> Bag.getEyeId(f.getItem()) != eye);
                worldItems = new ArrayList<>();
                for (ItemEntity ie : tl)
                    worldItems.add(new WeakReference<>(ie));
                inv = new WeakReference<>(null);
            } else {
                List<ItemEntity> tl = e.level.getEntitiesOfClass(ItemEntity.class, new AxisAlignedBB(e.blockPosition().offset(-DEFAULT_RADIUS, -DEFAULT_RADIUS, -DEFAULT_RADIUS), e.blockPosition().offset(DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS)), f -> Bag.getEyeId(f.getItem()) != eye);
                worldItems = new ArrayList<>();
                for (ItemEntity ie : tl)
                    worldItems.add(new WeakReference<>(ie));
                inv = getNearInventory(e);
            }

            if (cooldown > 0) //should remove the concept of cooldown
                --cooldown;
            ItemStack holdItem = getItemHold();
            if (WorldAccessPoint.isPowered(getBlockState()) && cooldown == 0 && !holdItem.isEmpty() && e instanceof LivingEntity)
                simulateRightClick((LivingEntity) e, holdItem);
        }
    }

    /**
     * try to simulate as much of right click behaviors as possible (stop at the first valid) (only run if there is enough energy)
     */
    public void simulateRightClick(LivingEntity e, ItemStack holdItem) {
        if (extractEnergy(ENERGY_USAGE_PER_ACTION, true) == ENERGY_USAGE_PER_ACTION) {

            ItemStack original = holdItem.copy();
            ItemStack cacheMain = e.getItemInHand(Hand.MAIN_HAND).copy();
            ItemStack cacheOff = e.getItemInHand(Hand.OFF_HAND).copy();
            e.setItemInHand(Hand.MAIN_HAND, holdItem);
            e.setItemInHand(Hand.OFF_HAND, ItemStack.EMPTY);

            if (e instanceof PlayerEntity) {
                ActionResult<ItemStack> ars = holdItem.getItem().use(e.level, (PlayerEntity) e, Hand.MAIN_HAND);
                if (ars.getResult().consumesAction()) {
                    holdItem = ars.getObject();
                    simulateRightClickCleanup(e, holdItem, original, cacheMain, cacheOff);
                    return;
                }


                BlockRayTraceResult rr = Bag.rayTrace(e.level, (PlayerEntity) e, RayTraceContext.FluidMode.NONE);
                if (rr.getType() != RayTraceResult.Type.MISS && holdItem.getItem().useOn(new ItemUseContext((PlayerEntity) e, Hand.MAIN_HAND, rr)).consumesAction()) {
                    holdItem = e.getItemInHand(Hand.MAIN_HAND);
                    simulateRightClickCleanup(e, holdItem, original, cacheMain, cacheOff);
                    return;
                }
            } else {
                BlockRayTraceResult rb = e.level.clip(new RayTraceContext(e.position(), e.position().add(0.d, -1.5d, 0.d), RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, e));
                FakePlayer fp = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft((net.minecraft.world.server.ServerWorld)e.level);
                fp.setItemInHand(Hand.MAIN_HAND, holdItem);
                fp.setItemInHand(Hand.OFF_HAND, ItemStack.EMPTY);
                if (rb.getType() != RayTraceResult.Type.MISS && holdItem.getItem().useOn(new ItemUseContext(fp, Hand.MAIN_HAND, rb)).consumesAction()) {
                    holdItem = fp.getItemInHand(Hand.MAIN_HAND);
                    simulateRightClickCleanup(e, holdItem, original, cacheMain, cacheOff);
                    return;
                }
            }

            //could use a test to see if the entity can use the item to prevent some weird behavior
//            e.setActiveHand(Hand.MAIN_HAND); //should be a better version of the test (note: verify if bows work, they might not return 0 as the final use count)
//            while (e.getItemInUseCount() > 0)
//                ReflectionUtils.runMethod(e, "updateActiveHand", "func_184608_ct"); //hopefully the new version of this reflection works
//            e.stopActiveHand();
            {
                ItemStack test = holdItem.copy();
                int dur = net.minecraftforge.event.ForgeEventFactory.onItemUseStart(e, test, test.getUseDuration());
                if (dur > 0) {
                    if (net.minecraftforge.common.ForgeHooks.canContinueUsing(test, holdItem)) test = holdItem.copy();
                    if (test.equals(holdItem, false)) {
                        if (!test.isEmpty())
                            if (net.minecraftforge.event.ForgeEventFactory.onItemUseTick(e, test, dur) > 0)
                                test.onUsingTick(e, dur);
                        test.onUseTick(e.level, e, dur);
                        if (!(test.getItem() instanceof CrossbowItem))
                            holdItem = net.minecraftforge.event.ForgeEventFactory.onItemUseFinish(e, test, 0, holdItem.finishUsingItem(e.level, e));
                        if (!net.minecraftforge.event.ForgeEventFactory.onUseItemStop(e, holdItem, 0)) {
                            ItemStack copy = e instanceof PlayerEntity ? holdItem.copy() : null;
                            holdItem.releaseUsing(e.level, e, 0);
                            if (copy != null && holdItem.isEmpty())
                                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem((PlayerEntity) e, copy, Hand.MAIN_HAND);
                        }
                    }
                }
            }
//            holdItem = holdItem.getItem().onItemUseFinish(holdItem, e.world, e);
//            if (original.equals(holdItem, false))
//                holdItem.getItem().onPlayerStoppedUsing(holdItem, e.world, e, 0);
//            holdItem = e.getItemInHand(Hand.MAIN_HAND);
            simulateRightClickCleanup(e, holdItem, original, cacheMain, cacheOff);
        }
    }

    private void simulateRightClickCleanup(LivingEntity e, ItemStack holdItem, ItemStack original, ItemStack cacheMain, ItemStack cacheOff) {
        boolean update = !original.equals(holdItem, false);
        e.setItemInHand(Hand.MAIN_HAND, cacheMain);
        e.setItemInHand(Hand.OFF_HAND, cacheOff);
        extractEnergy(ENERGY_USAGE_PER_ACTION, false);
        cooldown = COOLDOWN;
        if (update)
            setItemHold(holdItem);
    }

    public WeakReference<IInventory> getNearInventory(Entity entity) {
        IInventory i = HopperTileEntity.getContainerAt(entity.level, entity.position().x, entity.position().y - 1.0D, entity.position().z);
        if (i != null) return new WeakReference<>(i);
        i = HopperTileEntity.getContainerAt(entity.level, entity.position().x, entity.position().y + 1.0D, entity.position().z);
        if (i != null) return new WeakReference<>(i);
        i = HopperTileEntity.getContainerAt(entity.level, entity.position().x, entity.position().y, entity.position().z);
        if (i != null) return new WeakReference<>(i);
        return new WeakReference<>(null);
    }

    @Override
    public int getSlots() {
        IInventory li = inv.get();
        return 1 + worldItems.size() + (li != null ? li.getContainerSize() : 0);
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index < 0) return ItemStack.EMPTY;
        if (index == 0) return getItemHold();
        if (index < 1 + worldItems.size()) {
            ItemEntity ie = worldItems.get(index - 1).get();
            return ie != null ? ie.getItem() : ItemStack.EMPTY;
        }
        IInventory li = inv.get();
        if (li != null && index < 1 + worldItems.size() + li.getContainerSize())
            return li.getItem(index - 1 - worldItems.size());
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack holdItem = getItemHold();
        if (slot == 0 && (holdItem.isEmpty() || StackUtils.areStackable(holdItem, stack))) {
            int toInput = Math.min(stack.getCount(), 64 - holdItem.getCount());
            if (toInput == 0) return stack;
            ItemStack out = stack.copy();
            out.shrink(toInput);
            if (!simulate) {
                if (holdItem.isEmpty()) {
                    holdItem = stack.copy();
                    holdItem.setCount(toInput);
                } else
                    holdItem.grow(toInput);
                setItemHold(holdItem);
                Entity e = lastKnownHolder.get();
                if (e instanceof LivingEntity)
                    simulateRightClick((LivingEntity) e, holdItem);
            }
            return out;
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = getStackInSlot(slot);
        if (amount > stack.getMaxStackSize()) { //this might occur if a mod is requesting the entirety of the inventory or does not check the max size of a stack before output, we reduce the amount of the request to prevent incompatibility with vanilla which could cause loss of items
            DimBag.LOGGER.error("watch out, we've got a badass here");
            amount = stack.getMaxStackSize();
        }
        if (amount <= 0 || stack.isEmpty()) return ItemStack.EMPTY;
        int toOutput = Math.min(stack.getCount(), amount);
        if (toOutput == 0) return ItemStack.EMPTY;
        ItemStack out = stack.copy();
        out.setCount(toOutput);
        if (!simulate) {
            stack.shrink(toOutput);
            if (slot == 0)
                setChanged();
        }
        return out;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot == 0 ? 64 : 0;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return slot == 0; }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) return 0;

        int energy = getEnergy();
        int energyReceived = Math.min(MAX_ENERGY - energy, maxReceive);
        if (!simulate)
            setEnergy(energy + energyReceived);
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) return 0;

        int energy = getEnergy();
        int energyExtracted = Math.min(energy, maxExtract);
        if (!simulate)
            setEnergy(energy - energyExtracted);
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() { return getEnergy(); }

    @Override
    public int getMaxEnergyStored() { return MAX_ENERGY; }

    @Override
    public boolean canExtract() { return false; }

    @Override
    public boolean canReceive() { return true; }

    @Override
    public int getTanks() { //one for the player input, many for inworld, a proxy for near fluidhandlers
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
