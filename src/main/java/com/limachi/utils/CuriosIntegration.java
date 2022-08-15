package com.limachi.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class CuriosIntegration {
    private InterModEnqueueEvent event;
    protected static final HashSet<ResourceLocation> icons = new HashSet<>();
    protected static final HashMap<String, Pair<ResourceLocation, Integer>> slots = new HashMap<>();

    public static void registerIcon(ResourceLocation icon) { icons.add(icon); }
    public static void registerSlot(String slot, ResourceLocation icon, int size) { slots.put(slot, new Pair<>(icon, size)); }

    @SubscribeEvent
    public static void atlasEvent(TextureStitchEvent.Pre event) {
        for (ResourceLocation icon : icons)
            event.addSprite(icon);
    }

    public static void enqueueIMC(final InterModEnqueueEvent event) {
        for (Map.Entry<String, Pair<ResourceLocation, Integer>> s : slots.entrySet()) {
            InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, ()->new SlotTypeMessage.Builder(s.getKey()).priority(-10).icon(s.getValue().getFirst()).size(s.getValue().getSecond()).build());
        }
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

    public static <T, R> R onNthEllem(Iterable<T> it, int n, Function<T, R> run, R def) {
        for (T t: it) {
            if (n == 0) return run.apply(t);
            --n;
        }
        return def;
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

        public ProxySlotModifier(Entity entity, EquipmentSlot slot) {
            this(slot.getType() == EquipmentSlot.Type.HAND ?
                            ()->onNthEllem(entity.getHandSlots(), slot.getIndex(), item->item, ItemStack.EMPTY) :
                            ()->onNthEllem(entity.getArmorSlots(), slot.getIndex(), item->item, ItemStack.EMPTY),
                    itemStack -> entity.setItemSlot(slot, itemStack),
                    itemStack -> slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND || Mob.getEquipmentSlotForItem(itemStack) == slot,
                    () -> slot.getType() == EquipmentSlot.Type.ARMOR ? 1 : 64);
        }
        public ProxySlotModifier(IItemHandlerModifiable handler, int slot, @Nullable Consumer<IItemHandlerModifiable> setChanged) { this(()->handler.getStackInSlot(slot), itemStack -> { handler.setStackInSlot(slot, itemStack); if (setChanged != null) setChanged.accept(handler); }, itemStack -> handler.isItemValid(slot, itemStack), () -> handler.getSlotLimit(slot)); }
        public ProxySlotModifier(Container handler, int slot, @Nullable Consumer<Container> setChanged) { this(()->handler.getItem(slot), itemStack -> { handler.setItem(slot, itemStack); if (setChanged != null) setChanged.accept(handler); }, itemStack -> handler.canPlaceItem(slot, itemStack), handler::getMaxStackSize); }
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
        return ProxySlotModifier.NULL_SLOT;
    }

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
        boolean isPlayer = entity instanceof Player;
        Iterator<ItemStack> it = Collections.emptyIterator();
        Inventory inv = entity instanceof Player ? ((Player)entity).getInventory() : null;

        if (isPlayer) {
            if (clazz.isInstance(inv.getSelected().getItem()) && predicate.test(inv.getSelected())) {
                int current = inv.selected;
                out.add(new ProxySlotModifier(() -> inv.getItem(current), stack -> inv.setItem(current, stack)));
            }
        } else {
            Iterator<ItemStack> tit = entity.getHandSlots().iterator();
            ItemStack test = tit.hasNext() ? tit.next() : ItemStack.EMPTY;
            if (clazz.isInstance(test.getItem()) && predicate.test(test))
                out.add(new ProxySlotModifier(()->entity.getHandSlots().iterator().next(), stack->entity.setItemSlot(EquipmentSlot.MAINHAND, stack)));
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
                out.add(new ProxySlotModifier(() -> inv.offhand.get(0), stack -> inv.setItem(slot, stack)));
            }
        } else {
            it = entity.getHandSlots().iterator();
            if (it.hasNext()) {
                it.next();
                if (it.hasNext()) {
                    ItemStack test = it.next();
                    if (clazz.isInstance(test.getItem()) && predicate.test(test))
                        out.add(new ProxySlotModifier(() -> {
                            Iterator<ItemStack> i = entity.getHandSlots().iterator();
                            i.next();
                            return i.next();
                        }, stack -> entity.setItemSlot(EquipmentSlot.OFFHAND, stack)));
                }
            }
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
            ItemStack test = isPlayer ? inv.getItem(ind) : it.hasNext() ? it.next() : null;
            if (test != null && clazz.isInstance(test.getItem()) && predicate.test(test)) {
                if (isPlayer)
                    out.add(new ProxySlotModifier(()->inv.getItem(ind), stack->inv.setItem(ind, stack)));
                else
                    out.add(new ProxySlotModifier(()->{
                        Iterator<ItemStack> ar = entity.getArmorSlots().iterator();
                        for (int j = 0; j < ind; ++j)
                            ar.next();
                        return ar.next();
                    }, stack-> entity.setItemSlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, ind), stack)));
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