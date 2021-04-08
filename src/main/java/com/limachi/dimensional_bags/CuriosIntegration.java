package com.limachi.dimensional_bags;

import com.limachi.dimensional_bags.common.items.Bag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class CuriosIntegration {

    public static final String BAG_CURIOS_SLOT = "back";
    protected static final ResourceLocation ICON = new ResourceLocation(DimBag.MOD_ID, "item/ghost_bag");
    private InterModEnqueueEvent event;

    @SubscribeEvent
    public static void atlasEvent(TextureStitchEvent.Pre event) {
        event.addSprite(ICON);
    }

    public static void enqueueIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, ()->new SlotTypeMessage.Builder(BAG_CURIOS_SLOT).priority(-10).icon(ICON).size(1).build());
    }

//    public static Optional<ImmutableTriple<String, Integer, ItemStack>> getBag(LivingEntity livingEntity) {
//        return CuriosApi.getCuriosHelper().findEquippedCurio(stack->true, livingEntity);
//    }

    /**
     * only find valid bags (eyeid > 0) in the CuriosIntegration.BAG_CURIOS_SLOT slots of the living entity
     */
    public static List<ProxyItemStackModifier> getEquippedBags(LivingEntity entity) {
        List<ProxyItemStackModifier> out = new ArrayList<>();
        Optional<ICuriosItemHandler> oih = CuriosApi.getCuriosHelper().getCuriosHandler(entity).resolve();
        if (!oih.isPresent()) return out;
        Optional<ICurioStacksHandler> osh = oih.get().getStacksHandler(CuriosIntegration.BAG_CURIOS_SLOT);
        if (!osh.isPresent()) return out;
        IDynamicStackHandler sh = osh.get().getStacks();
        for (int i = 0; i < sh.getSlots(); ++i)
            if (sh.getStackInSlot(i).getItem() instanceof Bag && Bag.getEyeId(sh.getStackInSlot(i)) > 0) {
                int finalI = i;
                out.add(new ProxyItemStackModifier(()->CuriosApi.getCuriosHelper().getCuriosHandler(entity).resolve().get().getStacksHandler(CuriosIntegration.BAG_CURIOS_SLOT).get().getStacks().getStackInSlot(finalI), s->CuriosApi.getCuriosHelper().getCuriosHandler(entity).resolve().get().getStacksHandler(CuriosIntegration.BAG_CURIOS_SLOT).get().getStacks().setStackInSlot(finalI, s)));
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

    public static class ProxyItemStackModifier {
        public final Supplier<ItemStack> getter;
        public final Consumer<ItemStack> setter;

        public ProxyItemStackModifier(Supplier<ItemStack> getter, Consumer<ItemStack> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public ItemStack get() {
            if (getter != null)
                return getter.get();
            return null;
        }

        public void set(ItemStack stack) {
            if (setter == null) return;
            if (stack == null)
                stack = ItemStack.EMPTY;
            setter.accept(stack);
        }
    }

    public static ProxyItemStackModifier searchItem(Entity entity, Class<? extends Item> clazz, Predicate<? super ItemStack> predicate) {
        List<ProxyItemStackModifier> res = searchItem(entity, clazz, predicate, false);
        if (res.size() > 0)
            return res.get(0);
        return null;
    }

    /**
     * search item in following order (for a player): main hand, off hand, curios (all), armor (all), belt (all), inventory (all)
     * search item in following order (non player entity): main hand, off hand, curios (all, if living entity), armor (all)
     * @param entity the entity to search
     * @param clazz the type of itemclass to match (put 'Item.class' to test all item types)
     * @param predicate a predicate to do finer testing (put '()->true' to accept any item matching the class)
     * @param continueAfterOne if true, continues until all the recursions finishes and return a list of matching items
     * @return a list of setter and getters for the found stacks
     */
    public static List<ProxyItemStackModifier> searchItem(Entity entity, Class<? extends Item> clazz, Predicate<? super ItemStack> predicate, boolean continueAfterOne) {
        ArrayList<ProxyItemStackModifier> out = new ArrayList<>();
        boolean isPlayer = entity instanceof PlayerEntity;
        Iterator<ItemStack> it = Collections.emptyIterator();
        PlayerInventory inv = entity instanceof PlayerEntity ? ((PlayerEntity)entity).inventory : null;

        if (isPlayer) {
            if (clazz.isInstance(inv.getCurrentItem().getItem()) && predicate.test(inv.getCurrentItem())) {
                int current = inv.currentItem;
                out.add(new ProxyItemStackModifier(() -> inv.getStackInSlot(current), stack -> inv.setInventorySlotContents(current, stack)));
            }
        } else {
            ItemStack test = entity.getHeldEquipment().iterator().next();
            if (clazz.isInstance(test.getItem()) && predicate.test(test))
                out.add(new ProxyItemStackModifier(()->entity.getHeldEquipment().iterator().next(), stack->entity.setItemStackToSlot(EquipmentSlotType.MAINHAND, stack)));
        }
        if (!continueAfterOne && out.size() > 0)
            return out;

        int s = 1;
        int d = 5;
        if (isPlayer) {
            s = inv.offHandInventory.size();
            d = inv.getSizeInventory() - s;
            if (clazz.isInstance(inv.offHandInventory.get(0).getItem()) && predicate.test(inv.offHandInventory.get(0))) {
                int slot = d;
                out.add(new ProxyItemStackModifier(() -> inv.offHandInventory.get(slot), stack -> inv.setInventorySlotContents(slot, stack)));
            }
        } else {
            it = entity.getHeldEquipment().iterator();
            it.next();
            ItemStack test = it.next();
            if (clazz.isInstance(test.getItem()) && predicate.test(test))
                out.add(new ProxyItemStackModifier(()->{Iterator<ItemStack> i = entity.getHeldEquipment().iterator(); i.next(); return i.next();}, stack->entity.setItemStackToSlot(EquipmentSlotType.OFFHAND, stack)));
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
                    out.add(new ProxyItemStackModifier(()->CuriosApi.getCuriosHelper().getEquippedCurios((LivingEntity) entity).resolve().get().getStackInSlot(found.getMiddle()), stack->CuriosApi.getCuriosHelper().getEquippedCurios((LivingEntity) entity).resolve().get().setStackInSlot(found.getMiddle(), stack)));
            if (cs.isPresent()) {
                ImmutableTriple<String, Integer, ItemStack> found = cs.get();
                out.add(new ProxyItemStackModifier(()->CuriosApi.getCuriosHelper().getEquippedCurios((LivingEntity) entity).resolve().get().getStackInSlot(found.getMiddle()), stack->CuriosApi.getCuriosHelper().getEquippedCurios((LivingEntity) entity).resolve().get().setStackInSlot(found.getMiddle(), stack)));
            }
            if (!continueAfterOne && out.size() > 0)
                return out;
        }

        if (isPlayer) {
            s = inv.armorInventory.size();
            d = inv.getSizeInventory() - inv.offHandInventory.size() - s;
        } else  {
            s = 4;
            d = 0;
            it = entity.getArmorInventoryList().iterator();
        }
        for (int i = 0; i < s; ++i) {
            int ind = d + i;
            ItemStack test = isPlayer ? inv.getStackInSlot(ind) : it.next();
            if (clazz.isInstance(test.getItem()) && predicate.test(test)) {
                if (isPlayer)
                    out.add(new ProxyItemStackModifier(()->inv.getStackInSlot(ind), stack->inv.setInventorySlotContents(ind, stack)));
                else
                    out.add(new ProxyItemStackModifier(()->{
                        Iterator<ItemStack> ar = entity.getArmorInventoryList().iterator();
                        for (int j = 0; j < ind; ++j)
                            ar.next();
                        return ar.next();
                    }, stack-> entity.setItemStackToSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, ind), stack)));
                if (!continueAfterOne)
                    return out;
            }
        }

        if (isPlayer) {
            s = inv.mainInventory.size();
            for (int i = 0; i < s; ++i) {
                if (i == inv.currentItem) continue;
                int ind = i;
                ItemStack test = inv.getStackInSlot(ind);
                if (clazz.isInstance(test.getItem()) && predicate.test(test)) {
                    out.add(new ProxyItemStackModifier(()->inv.getStackInSlot(ind), stack->inv.setInventorySlotContents(ind, stack)));
                    if (!continueAfterOne)
                        return out;
                }
            }
        }

        return out;
    }
}
