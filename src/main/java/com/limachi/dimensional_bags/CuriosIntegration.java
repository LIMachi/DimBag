package com.limachi.dimensional_bags;

import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.utils.ReflectionUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.minecart.ContainerMinecartEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class CuriosIntegration {

    public static final String BAG_CURIOS_SLOT = "back";
    protected static final ResourceLocation ICON = new ResourceLocation(DimBag.MOD_ID, "item/ghost_bag");
    private InterModEnqueueEvent event;

    @SubscribeEvent
    public static void atlasEvent(TextureStitchEvent.Pre event) { event.addSprite(ICON); }

    public static void enqueueIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, ()->new SlotTypeMessage.Builder(BAG_CURIOS_SLOT).priority(-10).icon(ICON).size(1).build());
    }

    /**
     * only find valid bags (eyeid > 0) in the CuriosIntegration.BAG_CURIOS_SLOT slots of the living entity
     */
    public static List<ProxySlotModifier> getEquippedBags(LivingEntity entity) {
        List<ProxySlotModifier> out = new ArrayList<>();
        Optional<ICuriosItemHandler> oih = CuriosApi.getCuriosHelper().getCuriosHandler(entity).resolve();
        if (!oih.isPresent()) return out;
        Optional<ICurioStacksHandler> osh = oih.get().getStacksHandler(CuriosIntegration.BAG_CURIOS_SLOT);
        if (!osh.isPresent()) return out;
        IDynamicStackHandler sh = osh.get().getStacks();
        for (int i = 0; i < sh.getSlots(); ++i)
            if (sh.getStackInSlot(i).getItem() instanceof Bag && Bag.getEyeId(sh.getStackInSlot(i)) > 0) {
                int finalI = i;
                out.add(new ProxySlotModifier(()->CuriosApi.getCuriosHelper().getCuriosHandler(entity).resolve().get().getStacksHandler(CuriosIntegration.BAG_CURIOS_SLOT).get().getStacks().getStackInSlot(finalI), s->CuriosApi.getCuriosHelper().getCuriosHandler(entity).resolve().get().getStacksHandler(CuriosIntegration.BAG_CURIOS_SLOT).get().getStacks().setStackInSlot(finalI, s)));
            }
        return out;
    }

    public static boolean equipOnFirstValidSlot(LivingEntity entity, String slot_category, ItemStack stack) {
        Optional<ICuriosItemHandler> oih = CuriosApi.getCuriosHelper().getCuriosHandler(entity).resolve();
        if (!oih.isPresent()) return false;
        Optional<ICurioStacksHandler> osh = oih.get().getStacksHandler(slot_category);
        if (!osh.isPresent()) return false;
        IDynamicStackHandler sh = osh.get().getStacks();
        for (int i = 0; i < sh.getSlots(); ++i)
            if (sh.getStackInSlot(i).isEmpty()) {
                sh.setStackInSlot(i, stack);
                return true;
            }
        return false;
    }

    /**
     * helper class to standardise access to slots (in IItemHandlerModifiable, Entity, IInventory and others)
     */
    public static class ProxySlotModifier {
        public static final ProxySlotModifier NULL_SLOT = new ProxySlotModifier(null, null, null, null);

        private final Supplier<ItemStack> getter;
        private final Consumer<ItemStack> setter;
        private final Predicate<ItemStack> itemstackValidator;
        private final Supplier<Integer> maxSize;

        public ProxySlotModifier(Entity entity, EquipmentSlotType slot) {
            this(slot.getType() == EquipmentSlotType.Group.HAND ?
                            ()->onNthEllem(entity.getHandSlots(), slot.getIndex(), item->item, ItemStack.EMPTY) :
                            ()->onNthEllem(entity.getArmorSlots(), slot.getIndex(), item->item, ItemStack.EMPTY),
                    itemStack -> entity.setItemSlot(slot, itemStack),
                    itemStack -> slot == EquipmentSlotType.MAINHAND || slot == EquipmentSlotType.OFFHAND || MobEntity.getEquipmentSlotForItem(itemStack) == slot,
                    () -> slot.getType() == EquipmentSlotType.Group.ARMOR ? 1 : 64);
        }
        public ProxySlotModifier(IItemHandlerModifiable handler, int slot, @Nullable Consumer<IItemHandlerModifiable> setChanged) { this(()->handler.getStackInSlot(slot), itemStack -> { handler.setStackInSlot(slot, itemStack); if (setChanged != null) setChanged.accept(handler); }, itemStack -> handler.isItemValid(slot, itemStack), () -> handler.getSlotLimit(slot)); }
        public ProxySlotModifier(IInventory handler, int slot, @Nullable Consumer<IInventory> setChanged) { this(()->handler.getItem(slot), itemStack -> { handler.setItem(slot, itemStack); if (setChanged != null) setChanged.accept(handler); }, itemStack -> handler.canPlaceItem(slot, itemStack), handler::getMaxStackSize); }
        public ProxySlotModifier(Supplier<ItemStack> getter, Consumer<ItemStack> setter) { this(getter, setter, i->!i.isEmpty(), ()->64); }
        public ProxySlotModifier(Consumer<ItemStack> setter) { this(null, setter, i->!i.isEmpty(), ()->64); }
        public ProxySlotModifier(Supplier<ItemStack> getter) { this(getter, null, null, ()->64); }
        public ProxySlotModifier(Supplier<ItemStack> getter, Consumer<ItemStack> setter, Predicate<ItemStack> itemstackValidator, Supplier<Integer> maxSize) {
            this.getter = getter;
            this.setter = setter;
            this.itemstackValidator = itemstackValidator;
            this.maxSize = maxSize;
        }

        public boolean isValid() { return getter != null || setter != null; }

        public boolean isItemStackValid(ItemStack stack) { return itemstackValidator != null && itemstackValidator.test(stack); }

        public int getMaxSize() { return maxSize == null ? 0 : maxSize.get(); }

        public ItemStack get() {
            if (getter == null) return ItemStack.EMPTY; //TODO: should warn there
            return getter.get();
        }

        public void set(ItemStack stack) {
            if (setter == null) return; //TODO: should warn there
            if (stack == null) stack = ItemStack.EMPTY;
            setter.accept(stack);
        }
    }

    public static ProxySlotModifier searchItem(Entity entity, Class<? extends Item> clazz, Predicate<? super ItemStack> predicate) {
        List<ProxySlotModifier> res = searchItem(entity, clazz, predicate, false);
        if (res.size() > 0)
            return res.get(0);
        return null;
    }

    public static <T, R> R onNthEllem(Iterable<T> it, int n, Function<T, R> run, R def) {
        for (T t: it) {
            if (n == 0) return run.apply(t);
            --n;
        }
        return def;
    }

    public static <T> void onNthEllem(Iterable<T> it, int n, Consumer<T> run) {
        for (T t: it) {
            if (n == 0) {
                run.accept(t);
                return;
            }
            --n;
        }
    }

    /**
     * slot order is standardised for all entities by following a few rules: note: pig saddle and horses/llama/donkey inventories use non-standard slots (actually the saddle on a pig is just a tag, not even a slot)
     * negative slot ids are used to access curio slots (negative is removed before searching slots, the range -1 -> -3 would become the range 0 -> 2 in curio slots)
     * 0-3 -> armor (0: feet -> 3: helm) (only chest for horses/llama -> armor/carpet, saddle is stored in a special slot, others should be empty)
     * 4   -> off hand (held equipment second slot) (usually empty for almost all entities)
     * 5   -> belt first slot (held equipment first slot for non player)
     * 6 ~ -> all other slots (rest of belt then inventory for player, extra inventory for horses/llama, extra off hand if more than 2 slots in held)
     *
     * this function should not be used to iterate over slots (as it is quite costly, particularly for curio slots)
     */
    /*
    public static ProxySlotModifier getSlotAccess(Entity entity, int slot) {
        if (slot < 0 && entity instanceof LivingEntity) {
            int slott = -slot - 1;
            ICuriosItemHandler h = CuriosApi.getCuriosHelper().getCuriosHandler((LivingEntity) entity).orElse(null);
            if (h == null) return ProxySlotModifier.NULL_SLOT;
            for (Map.Entry<String, ICurioStacksHandler> e : h.getCurios().entrySet()) {
                IDynamicStackHandler d = e.getValue().getStacks();
                if (slott < d.getSlots())
                    return new ProxySlotModifier(d, slott, null);
                slott -= d.getSlots();
                if (e.getValue().hasCosmetic()) {
                    d = e.getValue().getCosmeticStacks();
                    if (slott < d.getSlots())
                        return new ProxySlotModifier(d, slott, null);
                    slott -= d.getSlots();
                }
            }
        } else if (slot < 4) {
            int t = 0;
            for (ItemStack s : entity.getArmorSlots())
                if (t++ == slot)
                    return new ProxySlotModifier(entity, EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, slot));
        } else if (slot == 4) {
            int t = 0;
            for (ItemStack s : entity.getHandSlots())
                if (t++ == 1)
                    return new ProxySlotModifier(entity, EquipmentSlotType.OFFHAND);
        } else if (slot == 5) {
            if (entity instanceof PlayerEntity)
                return new ProxySlotModifier(((PlayerEntity)entity).inventory, 0, null);
            else if (entity.getHandSlots().iterator().hasNext())
                return new ProxySlotModifier(entity, EquipmentSlotType.MAINHAND);
        } else {
            if (entity instanceof PlayerEntity)
                return slot - 5 < 36 ? new ProxySlotModifier(((PlayerEntity)entity).inventory, slot - 5, null) : ProxySlotModifier.NULL_SLOT;
            else if (entity instanceof AbstractHorseEntity) { //llama/horse/donkey/mule/etc
                //TODO: horse code
            } else if (entity instanceof ContainerMinecartEntity) {

            } else {
                Iterable<ItemStack> it = entity.getHandSlots();
                if (it instanceof List && slot - 4 < ((List<ItemStack>)it).size())
                    return new ProxySlotModifier(()->((List<ItemStack>)entity.getHandSlots()).get(slot - 4), itemStack -> ((List<ItemStack>)entity.getHandSlots()).set(slot - 4, itemStack));
            }
        }
        return ProxySlotModifier.NULL_SLOT;
    }*/

    /**
     * get all slots of an entity ordered in the same way of a player inventory (the size will always be 41, but some slots might be disabled)
     * this operation does not include Curio slots, but does include armor and off hand
     * if the entity has more slots than a player, the extra slots will not be mapped
     */
    /*
    public static ProxySlotModifier[] mappedSlotAccess(Entity entity) {
        ProxySlotModifier[] out = new ProxySlotModifier[TOTAL_INVENTORY_SIZE];
        if (!(entity instanceof PlayerEntity))
            for (int i = 0; i < TOTAL_INVENTORY_SIZE; ++i)
                out[i] = ProxySlotModifier.NULL_SLOT;
        if (entity instanceof PlayerEntity)
            for (int i = 0; i < TOTAL_INVENTORY_SIZE; ++i)
                out[i] = new ProxySlotModifier(((PlayerEntity)entity).inventory, i, null);
        else if (entity instanceof LivingEntity) {
            out[0] = new ProxySlotModifier(entity, EquipmentSlotType.MAINHAND);
            if (entity instanceof AbstractHorseEntity) { //llama/horse/donkey/mule/etc
                Inventory horseChest = (Inventory)ReflectionUtils.getField(entity, "horseChest", "field_110296_bG"); //slot 0 -> saddle, slot 1 -> armor (armor might be mirrored on chestplate), slots 2~ chest added to llama, donkey, mule
                if (horseChest != null) {
                    for (int i = 0; i < horseChest.getContainerSize() - 2; ++i)
                        out[INVENTORY_OFFSET + i] = new ProxySlotModifier(horseChest, 2 + i, null);
                    out[LEGS] = new ProxySlotModifier(horseChest, 0, null);
                    out[CHEST] = new ProxySlotModifier(entity, EquipmentSlotType.CHEST);
                }
            } else {
                out[MAIN_HAND] = new ProxySlotModifier(entity, EquipmentSlotType.MAINHAND);
                out[OFF_HAND] = new ProxySlotModifier(entity, EquipmentSlotType.OFFHAND);
                out[FEET] = new ProxySlotModifier(entity, EquipmentSlotType.FEET);
                out[LEGS] = new ProxySlotModifier(entity, EquipmentSlotType.LEGS);
                out[CHEST] = new ProxySlotModifier(entity, EquipmentSlotType.CHEST);
                out[HEAD] = new ProxySlotModifier(entity, EquipmentSlotType.HEAD);
            }
        } else { //unknown non living entity, probably minecart
            IItemHandler t = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
            if (t != null && t instanceof IItemHandlerModifiable) {
                int l = Integer.min(t.getSlots(), INVENTORY_SIZE);
                for (int i = 0; i < l; ++i)
                    out[INVENTORY_OFFSET + i] = new ProxySlotModifier((IItemHandlerModifiable)t, i, null);
            }
        }
        return out;
    }*/

    /** FIXME: should add search through cosmetic slots and compatibility with armor and extra slots of living entity
     * search item in following order (for a player): main hand, off hand, curios (all), armor (all), belt (all), inventory (all)
     * search item in following order (non player entity): main hand, off hand, curios (all, if living entity), armor (all)
     * @param entity the entity to search
     * @param clazz the type of itemclass to match (put 'Item.class' to test all item types)
     * @param predicate a predicate to do finer testing (put '()->true' to accept any item matching the class)
     * @param continueAfterOne if true, continues until all the recursions finishes and return a list of matching items
     * @return a list of setter and getters for the found stacks
     */
    public static List<ProxySlotModifier> searchItem(Entity entity, Class<? extends Item> clazz, Predicate<? super ItemStack> predicate, boolean continueAfterOne) {
        ArrayList<ProxySlotModifier> out = new ArrayList<>();
        boolean isPlayer = entity instanceof PlayerEntity;
        Iterator<ItemStack> it = Collections.emptyIterator();
        PlayerInventory inv = entity instanceof PlayerEntity ? ((PlayerEntity)entity).inventory : null;

        if (isPlayer) {
            if (clazz.isInstance(inv.getSelected().getItem()) && predicate.test(inv.getSelected())) {
                int current = inv.selected;
                out.add(new ProxySlotModifier(() -> inv.getItem(current), stack -> inv.setItem(current, stack)));
            }
        } else {
            Iterator<ItemStack> tit = entity.getHandSlots().iterator();
            ItemStack test = tit.hasNext() ? tit.next() : ItemStack.EMPTY;
            if (clazz.isInstance(test.getItem()) && predicate.test(test))
                out.add(new ProxySlotModifier(()->entity.getHandSlots().iterator().next(), stack->entity.setItemSlot(EquipmentSlotType.MAINHAND, stack)));
        }
        if (!continueAfterOne && out.size() > 0)
            return out;

        int s = 1;
        int d = 5;
        if (isPlayer) {
            s = inv.offhand.size();
            d = inv.getContainerSize() - s;
            if (clazz.isInstance(inv.offhand.get(0).getItem()) && predicate.test(inv.offhand.get(0))) {
                int slot = d;
                out.add(new ProxySlotModifier(() -> inv.offhand.get(slot), stack -> inv.setItem(slot, stack)));
            }
        } else {
            it = entity.getHandSlots().iterator();
            it.next();
            ItemStack test = it.next();
            if (clazz.isInstance(test.getItem()) && predicate.test(test))
                out.add(new ProxySlotModifier(()->{Iterator<ItemStack> i = entity.getHandSlots().iterator(); i.next(); return i.next();}, stack->entity.setItemSlot(EquipmentSlotType.OFFHAND, stack)));
        }
        if (!continueAfterOne && out.size() > 0)
            return out;

        if (entity instanceof LivingEntity) {
            Optional<ImmutableTriple<String, Integer, ItemStack>> cs;
            ArrayList<ImmutableTriple<String, Integer, ItemStack>> blackList = new ArrayList<>();
            while ((cs = CuriosApi.getCuriosHelper().findEquippedCurio(stack->{
                for (ImmutableTriple<String, Integer, ItemStack> ignore: blackList)
                    if (stack == ignore.getRight())
                        return false;
                return clazz.isInstance(stack.getItem()) && predicate.test(stack);
            }, (LivingEntity) entity)).isPresent() && continueAfterOne)
                blackList.add(cs.get());
            if (continueAfterOne)
                for (ImmutableTriple<String, Integer, ItemStack> found : blackList)
                    out.add(new ProxySlotModifier(()->CuriosApi.getCuriosHelper().getEquippedCurios((LivingEntity) entity).resolve().get().getStackInSlot(found.getMiddle()), stack->CuriosApi.getCuriosHelper().getEquippedCurios((LivingEntity) entity).resolve().get().setStackInSlot(found.getMiddle(), stack)));
            if (cs.isPresent()) {
                ImmutableTriple<String, Integer, ItemStack> found = cs.get();
                out.add(new ProxySlotModifier(()->CuriosApi.getCuriosHelper().getEquippedCurios((LivingEntity) entity).resolve().get().getStackInSlot(found.getMiddle()), stack->CuriosApi.getCuriosHelper().getEquippedCurios((LivingEntity) entity).resolve().get().setStackInSlot(found.getMiddle(), stack)));
            }
            if (!continueAfterOne && out.size() > 0)
                return out;
        }

        if (isPlayer) {
            s = inv.armor.size();
            d = inv.getContainerSize() - inv.armor.size() - s;
        } else  {
            s = 4;
            d = 0;
            it = entity.getArmorSlots().iterator();
        }
        for (int i = 0; i < s; ++i) {
            int ind = d + i;
            ItemStack test = isPlayer ? inv.getItem(ind) : it.next();
            if (clazz.isInstance(test.getItem()) && predicate.test(test)) {
                if (isPlayer)
                    out.add(new ProxySlotModifier(()->inv.getItem(ind), stack->inv.setItem(ind, stack)));
                else
                    out.add(new ProxySlotModifier(()->{
                        Iterator<ItemStack> ar = entity.getArmorSlots().iterator();
                        for (int j = 0; j < ind; ++j)
                            ar.next();
                        return ar.next();
                    }, stack-> entity.setItemSlot(EquipmentSlotType.byTypeAndIndex(EquipmentSlotType.Group.ARMOR, ind), stack)));
                if (!continueAfterOne)
                    return out;
            }
        }

        if (isPlayer) {
            s = inv.items.size();
            for (int i = 0; i < s; ++i) {
                if (i == inv.selected) continue;
                int ind = i;
                ItemStack test = inv.getItem(ind);
                if (clazz.isInstance(test.getItem()) && predicate.test(test)) {
                    out.add(new ProxySlotModifier(()->inv.getItem(ind), stack->inv.setItem(ind, stack)));
                    if (!continueAfterOne)
                        return out;
                }
            }
        }

        return out;
    }
}
