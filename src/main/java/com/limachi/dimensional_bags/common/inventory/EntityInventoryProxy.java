package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.ConfigManager;
import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.utils.ReflectionUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SaddleItem;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * maps any entity (weak reference) inventory to an inventory similar to a player (currently not compatible with curios)
 */
@Mod.EventBusSubscriber
public class EntityInventoryProxy implements IItemHandlerModifiable, IEntityInventoryProxyIsActiveSlot, IFluidHandler, IEnergyStorage, ICapabilityProvider {

    public static final int HOTBAR_OFFSET = 0;
    public static final int INVENTORY_OFFSET = 9;
    public static final int MAIN_HAND = 0;
    public static final int ARMOR_OFFSET = 36;
    public static final int FEET = 36;
    public static final int LEGS = 37;
    public static final int CHEST = 38;
    public static final int HEAD = 39;
    public static final int OFF_HAND = 40;

    public static final int HOTBAR_SIZE = 9;
    public static final int INVENTORY_SIZE = 27;
    public static final int ARMOR_SIZE = 4;
    public static final int OFF_HAND_SIZE = 1;
    public static final int TOTAL_INVENTORY_SIZE = HOTBAR_SIZE + INVENTORY_SIZE + ARMOR_SIZE + OFF_HAND_SIZE;

//    @ObjectHolder()
//    public static final Fluid

    @ConfigManager.Config(cmt = "does pumping water inside the entity through an interface protect it from fall damage (by consuming and placing water on a landing that would damage the entity)")
    public static final boolean ADD_MLG_BEHAVIOR = true;

    private WeakReference<Entity> entityRef = new WeakReference<>(null);
    private int lastTickChange;

    public EntityInventoryProxy() { lastTickChange = EventManager.tick; }
    public EntityInventoryProxy(Entity entity) { setEntity(entity); }

    public Entity getEntity() { return entityRef.get(); }

    public void setEntity(Entity entity) {
        entityRef = new WeakReference<>(entity);
        lastTickChange = EventManager.tick;
    }

    public int getLastTickChange() { return lastTickChange; }

    public String getEntityName() {
        Entity entity = getEntity();
        if (entity == null) return "Unavailable";
        return entity.getName().getString();
    }

    /**
     * return null on invalid slot
     */
    protected ItemStack getStackInSlotInternal(int slot) {
        if (slot < 0 || slot >= TOTAL_INVENTORY_SIZE) return null;
        Entity entity = getEntity();
        if (entity == null) return null;
        if (entity instanceof PlayerEntity)
            return ((PlayerEntity)entity).inventory.getItem(slot);
        if (entity instanceof LivingEntity) {
            if (slot == CHEST) return CuriosIntegration.onNthEllem(entity.getArmorSlots(), 2, i->i, null);
            if (entity instanceof AbstractHorseEntity) {
                Inventory horseChest = (Inventory) ReflectionUtils.getField(entity, "horseChest", "field_110296_bG");
                if (horseChest != null && ((!(entity instanceof LlamaEntity) && slot == LEGS) || (slot >= INVENTORY_OFFSET && slot < ARMOR_OFFSET))) {
                    if (slot == LEGS) return horseChest.getContainerSize() > 0 ? horseChest.getItem(0) : null;
                    return horseChest.getContainerSize() > 2 + slot - INVENTORY_OFFSET ? horseChest.getItem(2 + slot - INVENTORY_OFFSET) : null;
                }
            } else {
                if ((slot == MAIN_HAND || slot == OFF_HAND))
                    return ((LivingEntity) entity).getItemInHand(slot == MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND);
                if (slot >= ARMOR_OFFSET)
                    return CuriosIntegration.onNthEllem(entity.getArmorSlots(), slot - ARMOR_OFFSET, i->i, ItemStack.EMPTY);
                //FIXME: missing: PillagerEntity, PiglinEntity, AbstractVillagerEntity
            }
            return null;
        }
        if (slot >= INVENTORY_OFFSET && slot < ARMOR_OFFSET) {
            IItemHandler t = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
            return t == null || slot - INVENTORY_OFFSET >= t.getSlots() ? null : t.getStackInSlot(slot - INVENTORY_OFFSET);
        }
        return null;
    }

    protected int iterateEnergy(int init, BiFunction<IEnergyStorage, Integer, Integer> run, Predicate<Integer> shouldContinue) {
        int t = init;
        for (int i = 0; i < TOTAL_INVENTORY_SIZE; ++i) {
            ItemStack s = getStackInSlotInternal(i);
            if (s != null) {
                IEnergyStorage e = s.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                if (e != null)
                    t = run.apply(e, t);
            }
            if (!shouldContinue.test(t))
                break;
        }
        return t;
    }

    public boolean isActiveSlot(int slot) { return getStackInSlotInternal(slot) != null; }

    //ICapabilityProvider

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (getEntity() != null && (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || cap == CapabilityEnergy.ENERGY || cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY))
            return LazyOptional.of(()->this).cast();
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        return getCapability(cap, null);
    }

    public static class EntityInventoryMirror extends Inventory implements IEntityInventoryProxyIsActiveSlot {
        protected boolean[] active_slots;

        public EntityInventoryMirror(PacketBuffer buffer) { this(unpackBuffer(buffer)); }

        protected static boolean[] unpackBuffer(PacketBuffer buffer) {
            boolean[] out = new boolean[EntityInventoryProxy.TOTAL_INVENTORY_SIZE];
            for (int i = 0; i < EntityInventoryProxy.TOTAL_INVENTORY_SIZE; ++i)
                out[i] = buffer.readBoolean();
            return out;
        }

        public EntityInventoryMirror(boolean[] active_slots) { super(EntityInventoryProxy.TOTAL_INVENTORY_SIZE); this.active_slots = active_slots; }

        @Override
        public int getContainerSize() { return active_slots.length; }

        @Override
        public ItemStack getItem(int index) {
            if (index < 0 || index >= active_slots.length || !active_slots[index]) return ItemStack.EMPTY;
            return super.getItem(index);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            if (index < 0 || index >= active_slots.length || !active_slots[index]) return ItemStack.EMPTY;
            return super.removeItem(index, count);
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            if (index < 0 || index >= active_slots.length || !active_slots[index]) return ItemStack.EMPTY;
            return super.removeItemNoUpdate(index);
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            if (index < 0 || index >= active_slots.length || !active_slots[index]) return;
            super.setItem(index, stack);
        }

        @Override
        public boolean stillValid(PlayerEntity player) { return true; }

        @Override
        public boolean isActiveSlot(int slot) {
            return slot >= 0 && slot < active_slots.length && active_slots[slot];
        }

        @Override
        public String getEntityName() { return "Unavailable"; }

        @Override
        public Entity getEntity() { return null; }
    }

    // IEnergyStorage

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive <= 0) return 0;
        return iterateEnergy(maxReceive, (storage, t)->t - storage.receiveEnergy(t, simulate), t->t > 0);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (maxExtract <= 0) return 0;
        return iterateEnergy(maxExtract, (storage, t)->t - storage.extractEnergy(t, simulate), t->t > 0);
    }

    @Override
    public int getEnergyStored() {
        return iterateEnergy(0, (storage, t)->t + storage.getEnergyStored(), t->true);
    }

    @Override
    public int getMaxEnergyStored() {
        return iterateEnergy(0, (storage, t)->t + storage.getMaxEnergyStored(), t->true);
    }

    @Override
    public boolean canExtract() {
        return iterateEnergy(0, (storage, t)->storage.canExtract() ? 1 : 0, t->t == 0) != 0;
    }

    @Override
    public boolean canReceive() {
        return iterateEnergy(0, (storage, t)->storage.canReceive() ? 1 : 0, t->t == 0) != 0;
    }

    //IFluidHandler

    @Override
    public int getTanks() { return 1; }

    @Nonnull
    @Override //should return a fluidstack of xp
    public FluidStack getFluidInTank(int tank) {
        return null;
    }

    @Override //xp should not have a limit
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override //accept xp, milk, water, air, other compats (potions)/xp is only valid for players
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void consumeWaterPouchForMLG(LivingFallEvent event) {
        if (ADD_MLG_BEHAVIOR) {
            int mlg = event.getEntity().getPersistentData().getInt("MLG_water");
            if (mlg > 0 && event.getDamageMultiplier() > 0 && event.getDistance() > 0) {
                event.getEntity().getPersistentData().putInt("MLG_water", mlg - 1);
                event.getEntity().level.setBlock(event.getEntity().blockPosition(), Blocks.WATER.defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
                event.setCanceled(true);
            }
        }
    }

    public static final ItemStack MILK = new ItemStack(Items.MILK_BUCKET);

    @Override //xp -> fill xp bar, milk -> 1 bucket, water -> 1 bucket
    public int fill(FluidStack resource, FluidAction action) {
        Fluid fluid = resource.getFluid();
        Entity entity = getEntity();
        if (entity == null) return 0;
        if (Tags.Fluids.MILK.contains(fluid) && resource.getAmount() >= 1000 && entity instanceof LivingEntity) {
            if (action.execute())
                ((LivingEntity)entity).curePotionEffects(MILK);
            return 1000;
        } else if (ADD_MLG_BEHAVIOR && Fluids.WATER.isSame(fluid) && resource.getAmount() >= 1000) {
            if (action.execute())
                entity.getPersistentData().putInt("MLG_water", 1 + entity.getPersistentData().getInt("MLG_water"));
            return 1000;
        } else if (false /*XP*/) {

        } else if (false /*Air*/) {

        }
        return 0;
    }

    @Nonnull
    @Override //drain xp or water (water only ! bucket at a time)
    public FluidStack drain(FluidStack resource, FluidAction action) {
        Entity entity = getEntity();
        if (entity == null) return FluidStack.EMPTY;
        if (ADD_MLG_BEHAVIOR && Fluids.WATER.isSame(resource.getFluid()) && resource.getAmount() >= 1000) {
            int mlg = entity.getPersistentData().getInt("MLG_water");
            int use = Integer.min(resource.getAmount() / 1000, mlg);
            if (use > 0) {
                if (action.execute())
                    entity.getPersistentData().putInt("MLG_water", mlg - use);
                return new FluidStack(Fluids.WATER, use * 1000);
            }
        } else if (false /*XP*/) {

        }
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override //drain only xp
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    // IItemHandlerModifiable, IEntityInventoryProxyIsActiveSlot

    @Override
    public int getSlots() { return TOTAL_INVENTORY_SIZE; }

    @Override
    public int getContainerSize() { return TOTAL_INVENTORY_SIZE; }

    @Override
    public boolean isEmpty() { for (int i = 0; i < TOTAL_INVENTORY_SIZE; ++i) if (!getStackInSlot(i).isEmpty()) return true; return false; }

    @Override
    public ItemStack getItem(int slot) {
        return getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemStack t = getStackInSlotInternal(slot);
        return t == null ? ItemStack.EMPTY : t;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack t = getStackInSlot(index).copy();
        ItemStack out = t.split(count);
        setStackInSlot(index, t);
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack t = getStackInSlot(index);
        setStackInSlot(index, ItemStack.EMPTY);
        return t;
    }

    @Override
    public void setItem(int index, ItemStack stack) { setStackInSlot(index, stack); }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(PlayerEntity player) { return true; }

    // /give Dev dim_bag:bag{eye_id:1}
    // /summon minecraft:zombie ~ ~ ~ {CanPickUpLoot: 1b}
    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot < 0 || slot >= TOTAL_INVENTORY_SIZE) return;
        Entity entity = getEntity();
        if (entity == null) return;
        if (entity instanceof PlayerEntity)
            ((PlayerEntity)entity).inventory.setItem(slot, stack);
        else if (entity instanceof LivingEntity) {
            if (entity instanceof MobEntity)
                ((MobEntity)entity).requiresCustomPersistence(); //fix to guarantee the content will not be lost because of entity despawn (note: if the bag was equipped by an entity, vanilla should have enabled this by default)
            if (slot == CHEST) entity.setItemSlot(EquipmentSlotType.CHEST, stack);
            else if (entity instanceof AbstractHorseEntity) {
                Inventory horseChest = (Inventory) ReflectionUtils.getField(entity, "horseChest", "field_110296_bG");
                if (horseChest != null && ((!(entity instanceof LlamaEntity) && slot == LEGS) || (slot >= INVENTORY_OFFSET && slot < ARMOR_OFFSET)))
                    horseChest.setItem(slot == LEGS ? 0 : 2 + slot - INVENTORY_OFFSET, stack);
            } else {
                if (slot == MAIN_HAND || slot == OFF_HAND) {
                    entity.setItemSlot(slot == MAIN_HAND ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND, stack);
                    if (entity instanceof MobEntity)
                        ((MobEntity)entity).setDropChance(slot == MAIN_HAND ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND, 2f); //fix to guarantee the content will still be usable on the mod death (vanilla use 2f for picked up loot by mobs)
                }
                if (slot >= ARMOR_OFFSET && slot < OFF_HAND) {
                    entity.setItemSlot(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, slot - ARMOR_OFFSET), stack);
                    if (entity instanceof MobEntity)
                        ((MobEntity)entity).setDropChance(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, slot - ARMOR_OFFSET), 2f); //fix to guarantee the content will still be usable on the mod death (vanilla use 2f for picked up loot by mobs)
                }
            }
        } else if (slot >= INVENTORY_OFFSET && slot < ARMOR_OFFSET) {
            IItemHandler t = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
            if (t instanceof IItemHandlerModifiable && slot - INVENTORY_OFFSET < t.getSlots()) ((IItemHandlerModifiable) t).setStackInSlot(slot - INVENTORY_OFFSET, stack);
        }
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack input, boolean simulate) {
        if (input.isEmpty() || isItemValid(slot, input)) return input;
        ItemStack p = getStackInSlotInternal(slot);
        if (p == null) return input;
        int pc = p.getCount();
        int toInput = Math.min(input.getCount(), getSlotLimit(slot) - pc);
        if (toInput <= 0) return input;
        ItemStack out = input.copy();
        out.shrink(toInput);
        if (!simulate) {
            ItemStack t = input.copy();
            t.setCount(toInput + pc);
            setStackInSlot(slot, t);
        }
        return out;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;
        ItemStack p = getStackInSlotInternal(slot);
        if (p == null) return ItemStack.EMPTY;
        int pc = p.getCount();
        if (pc == 0) return ItemStack.EMPTY;
        int toOutput = Math.min(pc, amount);
        ItemStack out = p.copy();
        out.setCount(toOutput);
        if (!simulate) {
            ItemStack n = p.copy();
            n.shrink(toOutput);
            setStackInSlot(slot, n);
        }
        return out;
    }

    @Override
    public int getSlotLimit(int slot) { return 64; }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (getStackInSlotInternal(slot) == null) return false; //this slot is not valid
        if (slot < ARMOR_OFFSET || slot >= OFF_HAND) return true; //outside of armor slots
        Entity entity = getEntity();
        if (entity instanceof AbstractHorseEntity) {
            if (slot == FEET || slot == HEAD) return false;
            AbstractHorseEntity horse = (AbstractHorseEntity) entity;
            if (slot == CHEST) return horse.isArmor(stack);
            return !(entity instanceof LlamaEntity) && stack.getItem() instanceof SaddleItem;
        }
        return MobEntity.getEquipmentSlotForItem(stack).getIndex() == slot - ARMOR_OFFSET;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < TOTAL_INVENTORY_SIZE; ++i)
            setStackInSlot(i, ItemStack.EMPTY);
    }
}
