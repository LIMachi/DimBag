package com.limachi.dim_bag.items;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_modes.BaseMode;
import com.limachi.dim_bag.bag_modes.ModesRegistry;
import com.limachi.dim_bag.capabilities.entities.BagMode;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.entities.BagItemEntity;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.capabilities.Cap;
import com.limachi.lim_lib.integration.Curios.CuriosIntegration;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import com.limachi.lim_lib.scrollSystem.IScrollItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class BagItem extends Item implements IScrollItem {
    public static final String BAG_ID_KEY = "bag_id";
    public static final String BAG_MODE_OVERRIDE = "bag_mode";
    public static final String[] CURIO_SLOTS = {"back", "body"};

    @RegisterItem
    public static RegistryObject<Item> R_ITEM;

    public BagItem() { super(new Item.Properties().stacksTo(1).fireResistant()); }

    /**
     * unequip all bags with the given id from the entity at pos in level
     * @param entity to remove bags from
     * @param id of the bags to remove
     * @param level to spawn the bags into (might be different from entity's)
     * @param pos to spawn the bags at in level
     * @return list of spawned bags
     */
    public static Collection<Entity> unequipBags(Entity entity, int id, Level level, BlockPos pos) {
        List<SlotAccess> bags = CuriosIntegration.searchItem(entity, BagItem.class, s->!(s.getItem() instanceof VirtualBagItem) && getBagId(s) == id, true);
        ArrayList<Entity> out = new ArrayList<>(bags.size());
        for (SlotAccess bag : bags)
            if (bag.set(ItemStack.EMPTY))
                out.add(BagEntity.create(level, pos, id));
        return out;
    }

    /**
     * try to equip the given bag in a valid slot for the entity (curios/chestplate) and return true on success
     * @param entity that will be equipping the bag
     * @param bag stack to equip
     * @return true if successful (entity will be modified, but not the stack)
     */
    public static boolean equipBag(LivingEntity entity, ItemStack bag) {
        for (String test : CURIO_SLOTS) {
            if (CuriosIntegration.equipOnFirstValidSlot(entity, test, bag))
                return true;
        }
        if (entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            entity.setItemSlot(EquipmentSlot.CHEST, bag);
            return entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BagItem;
        }
        return false;
    }

    /**
     * try to equip the given bag in a valid slot for the entity (curios/chestplate) and return true on success
     * @param entity that will be equipping the bag
     * @param bag BagEntity that will be converted to item to equip
     * @return true if successful (entity will be modified, bag will be despawned)
     */
    public static boolean equipBag(LivingEntity entity, BagEntity bag) {
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
//        if (data != null && data.contains(BAG_MODE_OVERRIDE))
//            return (float) ModesData.getModeIndex(data.getString(BAG_MODE_OVERRIDE));
//        if (player instanceof Player)
//            return (float) ModesData.getModeIndex(Cap.run(player, BagMode.TOKEN, c->c.getMode(getBagId(bag)), "Default"));
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

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) {
            if (stack.getOrCreateTag().getInt(BAG_ID_KEY) < 1)
                stack.getTag().putInt(BAG_ID_KEY, BagsData.newBagId());
            int id = getBagId(stack);
            getModeBehavior(entity, stack).inventoryTick(stack, level, entity, slot, selected);
            if (level.dimension().equals(DimBag.BAG_DIM)) {
//                RoomData room = RoomData.getRoom(entity.blockPosition()); //FIXME: should be handed by setHolder instead
//                if (room != null && room.getId() == id && !ParadoxModule.isParadoxCompatible(id)) {
//                    for (Entity bag : unequipBags(entity, id, entity.level(), entity.blockPosition()))
//                        room.leave(bag);
//                }
//                if (room != null && Events.tick % 100 == 0) //FIXME: wrong if branch
//                    room.temporaryChunkLoad();
            }
//            new HolderData(id).setHolder(entity);
            BagsData.runOnBag(id, b->b.setHolder(entity));
//            IBagsData.bag(id).ifPresent(b->b.setHolder(entity));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return getModeBehavior(player, player.getItemInHand(hand)).use(level, player, hand);
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
        return getModeBehavior(ctx.getPlayer(), ctx.getItemInHand()).useOn(ctx);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        return getModeBehavior(player, itemstack).onBlockStartBreak(itemstack, pos, player);
    }

    @Override
    public void scroll(Player player, int slot, int amount) {
        ItemStack bag = player.getInventory().getItem(slot);
        BaseMode behavior = getModeBehavior(player, bag);
        if (behavior.canScroll(player, slot))
            behavior.scroll(player, slot, amount);
        else {
//            int id = getBagId(bag); //FIXME
//            if (id > 0)
//                Cap.run(player, BagMode.TOKEN, c -> c.setMode(player, id, new ModesData(id).cycleMode(c.getMode(id), amount)));
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
//                ModesManager m = ModesManager.getInstance(id); //FIXME
//                if (m != null)
//                    player.displayClientMessage(Component.translatable("notification.bag.changed_mode", Component.translatable("bag.mode." + Cap.run(player, BagMode.TOKEN, c -> new ModesData(id).cycleMode(c.getMode(id), amount), "Default"))), true);
            }
        }
    }

    @Override
    public boolean canScroll(Player player, int slot) {
        return DimBag.BAG_KEY.getState(player) || getModeBehavior(player, player.getInventory().getItem(slot)).canScroll(player, slot);
    }
}
