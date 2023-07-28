package com.limachi.dim_bag.items;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.bag_modes.BaseMode;
import com.limachi.dim_bag.bag_modes.ModesRegistry;
import com.limachi.dim_bag.bag_modes.SettingsMode;
import com.limachi.dim_bag.capabilities.entities.BagMode;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.entities.BagItemEntity;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.Sides;
import com.limachi.lim_lib.capabilities.Cap;
import com.limachi.lim_lib.integration.Curios.CuriosIntegration;
import com.limachi.lim_lib.network.IRecordMsg;
import com.limachi.lim_lib.network.NetworkManager;
import com.limachi.lim_lib.network.RegisterMsg;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import com.limachi.lim_lib.scrollSystem.IScrollItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class BagItem extends Item implements IScrollItem {
    public static final String BAG_ID_KEY = "bag_id";
    public static final String BAG_MODE_OVERRIDE = "bag_mode";
    public static final String BAG_NAME_OVERRIDE = "bag_name";
    public static final String[] CURIO_SLOTS = {"back", "body"};

    @RegisterItem
    public static RegistryObject<Item> R_ITEM;

    public BagItem() { super(new Item.Properties().stacksTo(1).fireResistant()); }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return itemStack.copy();
    }

    @Override
    public boolean overrideStackedOnOther(@Nonnull ItemStack bag, @Nonnull Slot slot, @Nonnull ClickAction action, @Nonnull Player player) {
        if (bag.getCount() != 1 || action != ClickAction.SECONDARY) return false;
        ItemStack itemstack = slot.getItem();
        BagsData.runOnBag(bag, b->
            b.slotsHandle().ifPresent(d->{
                if (itemstack.isEmpty())
                    for (int i = 0; i < d.getSlots(); ++i) {
                        ItemStack found = d.getStackInSlot(i);
                        if (!found.isEmpty()) {
                            d.setStackInSlot(i, slot.safeInsert(found));
                            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                        }
                    }
                else {
                    ItemStack original = itemstack.copy();
                    slot.set(ItemHandlerHelper.insertItem(d, itemstack, false));
                    if (!original.equals(slot.getItem()))
                        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                }
            }));
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack bag, ItemStack itemstack, Slot slot, ClickAction action, Player player, SlotAccess sa) {
        if (bag.getCount() != 1 || action != ClickAction.SECONDARY) return false;
        BagsData.runOnBag(bag, b->
            b.slotsHandle().ifPresent(d->{
                if (itemstack.isEmpty()) {
                    for (int i = 0; i < d.getSlots(); ++i) {
                        ItemStack found = d.getStackInSlot(i);
                        if (!found.isEmpty()) {
                            sa.set(found.copy());
                            d.setStackInSlot(i, ItemStack.EMPTY);
                            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                        }
                    }
                } else {
                    ItemStack original = itemstack.copy();
                    sa.set(ItemHandlerHelper.insertItem(d, itemstack, false));
                    if (!original.equals(slot.getItem()))
                        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                }
            }));
        return true;
    }

    public static class CapabilityProvider implements ICapabilityProvider {

        @Nonnull
        final protected ItemStack bag;

        public CapabilityProvider(@Nonnull ItemStack bag) { this.bag = bag; }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (ForgeCapabilities.FLUID_HANDLER_ITEM.equals(cap) || ForgeCapabilities.FLUID_HANDLER.equals(cap))
                return BagsData.runOnBag(bag, b->b.tanksHandle().lazyMap(d->d.setContainer(bag)), LazyOptional.empty()).cast();
            else if (ForgeCapabilities.ITEM_HANDLER.equals(cap))
                return BagsData.runOnBag(bag, BagInstance::slotsHandle, LazyOptional.empty()).cast();
            else if (ForgeCapabilities.ENERGY.equals(cap))
                return BagsData.runOnBag(bag, BagInstance::energyHandle, LazyOptional.empty()).cast();
            else
                return LazyOptional.empty();
        }
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (stack.getItem() instanceof BagItem bag && !(bag instanceof VirtualBagItem))
            return new CapabilityProvider(stack);
        return null;
    }

    @RegisterMsg
    public record BagRidingPlayer(UUID entityId, boolean state) implements IRecordMsg {
        public void clientWork(Player player) {
            if (state)
                player.level().getEntities(player, new AABB(player.blockPosition().offset(-8, -8, -8), player.blockPosition().offset(8, 8, 8)), e->e instanceof BagEntity && e.getUUID().equals(entityId)).forEach(e->e.startRiding(player));
            else
                player.getPassengers().forEach(e->{
                    if (e instanceof BagEntity && e.getUUID().equals(entityId))
                        e.stopRiding();
                });
        }
    }

    /**
     * unequip all bags with the given id from the entity at pos in level
     * @param entity to remove bags from
     * @param id of the bags to remove
     * @param level to spawn the bags into (might be different from entity's)
     * @param pos to spawn the bags at in level
     * @return list of spawned bags
     */
    public static Collection<Entity> unequipBags(Entity entity, int id, Level level, BlockPos pos, boolean oneOnly) {
        ArrayList<Entity> out = new ArrayList<>();
        for (Entity passenger : entity.getPassengers()) {
            if (passenger instanceof BagEntity bag && bag.getBagId() == id) {
                if (entity instanceof ServerPlayer player)
                    NetworkManager.toClient(DimBag.MOD_ID, player, new BagRidingPlayer(bag.getUUID(), false));
                bag.stopRiding();
                out.add(bag);
            }
        }
        if (entity instanceof Player) {
            List<SlotAccess> bags = CuriosIntegration.searchItem(entity, BagItem.class, s -> !(s.getItem() instanceof VirtualBagItem) && getBagId(s) == id, true);
            for (SlotAccess bag : bags)
                if (bag.set(ItemStack.EMPTY))
                    out.add(BagEntity.create(level, pos, id));
        }
        return out;
    }

    /**
     * try to equip the given bag in a valid slot for the entity (curios/chestplate) and return true on success
     * @param entity that will be equipping the bag
     * @param bag stack to equip
     * @return true if successful (entity will be modified, but not the stack)
     */
    public static boolean equipBag(Entity entity, ItemStack bag) {
        if (entity instanceof Player player) {
            for (String test : CURIO_SLOTS) {
                if (CuriosIntegration.equipOnFirstValidSlot(player, test, bag))
                    return true;
            }
            if (player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                player.setItemSlot(EquipmentSlot.CHEST, bag);
                return player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BagItem;
            }
        }
        int id = getBagId(bag);
        if (!entity.isVehicle() && id > 0 && /**unstable for now!*/!(entity instanceof Player)) {
            BagEntity newBag = BagEntity.create(entity.level(), entity.blockPosition(), id);
            if (!newBag.startRiding(entity))
                newBag.remove(Entity.RemovalReason.KILLED);
            else {
//                if (entity instanceof ServerPlayer player)
//                    NetworkManager.toClient(DimBag.MOD_ID, player, new BagRidingPlayer(newBag.getUUID(), true));
                return true;
            }
        }
        return false;
    }

    /**
     * try to equip the given bag in a valid slot for the entity (curios/chestplate) and return true on success
     * @param entity that will be equipping the bag
     * @param bag BagEntity that will be converted to item to equip
     * @return true if successful (entity will be modified, bag will be despawned)
     */
    public static boolean equipBag(Entity entity, BagEntity bag) {
        boolean ok = equipBag(entity, create(bag.getPersistentData().getInt(BAG_ID_KEY)));
        if (ok)
            bag.remove(Entity.RemovalReason.KILLED);
        return ok;
    }

    /**
     * test if any bag is equipped in curio or chest slot, used for render and model interaction
     */
    public static int hasEquippedBag(LivingEntity entity) {
        final int[] found = {0};
        for (String test : CURIO_SLOTS)
            CuriosIntegration.getCurioCategory(entity, test).ifPresent(h->{
                if (found[0] > 0) return;
                for (int i = 0; found[0] <= 0 && i < h.getSlots(); ++i)
                    found[0] = getBagId(h.getStackInSlot(i));
            });
        return found[0] > 0 ? found[0] : getBagId(entity.getItemBySlot(EquipmentSlot.CHEST));
    }

    public static int getBagId(ItemStack bag) {
        if (bag.getItem() instanceof BagItem) {
            CompoundTag tag = bag.getTag();
            if (tag != null)
                return tag.getInt(BAG_ID_KEY);
        }
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    @StaticInitClient
    public static void prop() {
        ClientRegistries.registerItemModelProperty(R_ITEM, new ResourceLocation(DimBag.MOD_ID, "bag_mode_property"), BagItem::getMode);
    }

    @OnlyIn(Dist.CLIENT)
    public static float getMode(ItemStack bag, @Nullable ClientLevel level, @Nullable LivingEntity player, int index) {
        CompoundTag data = bag.getTag(); //FIXME
        if (data != null && data.contains(BAG_MODE_OVERRIDE))
            return (float) ModesRegistry.getModeIndex(data.getString(BAG_MODE_OVERRIDE));
        if (player instanceof Player)
            return (float) ModesRegistry.getModeIndex(Cap.run(player, BagMode.TOKEN, c->c.getMode(getBagId(bag)), "Default"));
        return 0;
    }

    public static ItemStack create(int id) {
        ItemStack out = new ItemStack(BagItem.R_ITEM.get());
        out.getOrCreateTag().putInt(BAG_ID_KEY, id);
        return out;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return stack.getItem() instanceof BagItem && !(stack.getItem() instanceof VirtualBagItem) && getBagId(stack) > 0;
    }

    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        BagItemEntity entity = new BagItemEntity(level, location.position().x, location.position().y, location.position().z, stack);
        entity.setDeltaMovement(location.getDeltaMovement());
        return entity;
    }

    @Override
    public boolean canFitInsideContainerItems() { return false; }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return armorType == EquipmentSlot.CHEST;
    }

    public static BaseMode getModeBehavior(Entity entity, ItemStack bag) {
        int id = getBagId(bag);
        if (id > 0 && entity instanceof Player)
            return Cap.run(entity, BagMode.TOKEN, c->ModesRegistry.getMode(c.getMode(id)), ModesRegistry.DEFAULT.mode());
        return ModesRegistry.DEFAULT.mode();
    }

    protected void commonBagTick(BagInstance bag, CompoundTag data, ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (entity instanceof Player player)
            player.getCapability(BagMode.CAPABILITY).ifPresent(bagMode->{
                long enabled = bag.enabledModesMask();
                if (data.getLong(BagInstance.MODES_STORAGE) != enabled) {
                    data.putLong(BagInstance.MODES_STORAGE, enabled);
                    if ((enabled & 1L << ModesRegistry.getModeIndex(bagMode.getMode(bag.bagId()))) == 0L)
                        bagMode.setMode(player, bag.bagId(), SettingsMode.NAME);
                }
                ModesRegistry.getMode(bagMode.getMode(bag.bagId())).inventoryTick(stack, level, entity, slot, selected);
            });
        String name = Component.Serializer.toJson(getName(stack));
        if (!stack.getTag().getString(BAG_NAME_OVERRIDE).equals(name))
            stack.getTag().putString(BAG_NAME_OVERRIDE, name);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) {
            CompoundTag data = stack.getOrCreateTag();
            int id = getBagId(stack);
            if (id < 1)
                data.putInt(BAG_ID_KEY, id = BagsData.newBagId());
            BagsData.runOnBag(id, b->{
                b.setHolder(entity);
                b.temporaryChunkLoad();
                commonBagTick(b, data, stack, level, entity, slot, selected);
            });
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) return InteractionResultHolder.pass(player.getItemInHand(hand));
        return getModeBehavior(player, player.getItemInHand(hand)).use(level, player, hand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return getModeBehavior(player, player.getItemInHand(hand)).interactLivingEntity(stack, player, target, hand);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack bag = event.getItemStack();
        if (bag.getItem() instanceof BagItem) {
            Player player = event.getEntity();
            if (getModeBehavior(player, bag).onLeftClickBlock(bag, player, event.getPos()))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        ItemStack bag = event.getItemStack();
        if (bag.getItem() instanceof BagItem) {
            Player player = event.getEntity();
            if (getModeBehavior(player, bag).onLeftClickEmpty(bag, player))
                event.setCanceled(true);
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return getModeBehavior(player, stack).onLeftClickEntity(stack, player, entity);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide) return InteractionResult.PASS;
        return getModeBehavior(ctx.getPlayer(), ctx.getItemInHand()).useOn(ctx);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        return getModeBehavior(player, itemstack).onBlockStartBreak(itemstack, pos, player);
    }

    @Override
    public Component getName(ItemStack bag) {
        if (Sides.isLogicalClient() && bag.getOrCreateTag().contains(BAG_NAME_OVERRIDE))
            return Component.Serializer.fromJson(bag.getTag().getString(BAG_NAME_OVERRIDE));
        Component def = super.getName(bag);
        return BagsData.runOnBag(bag, b->{
            CompoundTag settings = b.getModeData(SettingsMode.NAME);
            if (settings.contains("label", Tag.TAG_STRING))
                return Component.Serializer.fromJson(settings.getString("label"));
            return def;
        }, def);
    }

    @Override
    public void scroll(Player player, int slot, int amount) {
        ItemStack bag = player.getInventory().getItem(slot);
        BaseMode behavior = getModeBehavior(player, bag);
        if (behavior.canScroll(player, slot))
            behavior.scroll(player, slot, amount);
        else {
            int id = getBagId(bag); //FIXME
            if (id > 0)
                Cap.run(player, BagMode.TOKEN, c -> {
                    int mode = ModesRegistry.getModeIndex(c.getMode(id));
                    mode = ModesRegistry.cycleMode(mode, bag.getOrCreateTag().getInt(BagInstance.MODES_STORAGE), amount);
                    c.setMode(player, id, ModesRegistry.getMode(mode).name);
                });
        }
    }

    @Override
    public void scrollFeedBack(Player player, int slot, int amount) {
        ItemStack bag = player.getInventory().getItem(slot);
        BaseMode behavior = getModeBehavior(player, bag);
        if (behavior.canScroll(player, slot))
            behavior.scrollFeedBack(player, slot, amount);
        else {
            int id = getBagId(bag);
            if (id > 0) {
                Cap.run(player, BagMode.TOKEN, c -> {
                    int mode = ModesRegistry.getModeIndex(c.getMode(id));
                    mode = ModesRegistry.cycleMode(mode, bag.getOrCreateTag().getInt(BagInstance.MODES_STORAGE), amount);
                    player.displayClientMessage(Component.translatable("notification.bag.changed_mode", Component.translatable("bag.mode." + ModesRegistry.getMode(mode).name)), true);
                });
            }
        }
    }

    @Override
    public boolean canScroll(Player player, int slot) {
        return DimBag.BAG_KEY.getState(player) || getModeBehavior(player, player.getInventory().getItem(slot)).canScroll(player, slot);
    }

    public static BlockHitResult raycast(Level level, Player player, ClipContext.Fluid fluidCtx) {
        return getPlayerPOVHitResult(level, player, fluidCtx);
    }
}
