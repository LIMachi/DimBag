package com.limachi.dim_bag.items;

import com.limachi.dim_bag.Constants;
import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.saveData.Test;
import com.limachi.utils.CuriosIntegration;
import com.limachi.utils.Log;
import com.limachi.utils.Registries;
import com.limachi.utils.SaveData;
import com.limachi.utils.scrollSystem.IScrollItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

public class BagItem extends Item implements IScrollItem {

    @Registries.RegisterItem
    public static RegistryObject<BagItem> R_ITEM;

    public BagItem() { super(DimBag.INSTANCE.defaultProps().stacksTo(1)); }

    public static ItemStack bag(int id) {
        ItemStack out = new ItemStack(R_ITEM.get());
        if (out.getTag() == null)
            out.setTag(new CompoundTag());
        out.getTag().putInt(Constants.BAG_ID_TAG_KEY, id);
        return out;
    }

    public static void giveBag(int id, Player player) {
        ItemStack bag = bag(id);
        if (!player.addItem(bag))
            player.drop(bag, false);
    }

    public static boolean hasBag(int id, Entity entity) {
        return CuriosIntegration.searchItem(entity, BagItem.class, stack-> BagItem.getbagId(stack) == id) != null;
    }

    public static boolean equipBagOnCuriosSlot(ItemStack bag, LivingEntity player) {
        return CuriosIntegration.equipOnFirstValidSlot(player, Constants.BAG_CURIO_SLOT, bag);
    }

    public static int isEquippedOnCuriosSlot(LivingEntity entity, int eye_id) {
        Optional<ImmutableTriple<String, Integer, ItemStack>> ois = CuriosApi.getCuriosHelper().findEquippedCurio(stack->
                        stack.getItem() instanceof BagItem && BagItem.getbagId(stack) != 0 && (eye_id == 0 || BagItem.getbagId(stack) == eye_id)
                , entity);
        return ois.isPresent() ? BagItem.getbagId(ois.get().getRight()) : 0;
    }

    public static ArrayList<BagEntity> unequipBags(LivingEntity entity, int bagId, @Nullable BlockPos posIn, @Nullable Level worldIn) {
        Level world = worldIn != null ? worldIn : entity.level;
        BlockPos pos = posIn != null ? posIn : entity.blockPosition();
        ArrayList<BagEntity> spawned = new ArrayList<>();
        CuriosIntegration.searchItem(entity, BagItem.class, o->(!(o.getItem() instanceof GhostBagItem) && BagItem.getbagId(o) == bagId), true).forEach(p -> {
            spawned.add(BagEntity.spawn(world, pos, p.get()));
            p.set(ItemStack.EMPTY);
        });
        return spawned;
    }

    public static int getbagId(ItemStack stack) {
        if (stack.getTag() != null)
            return stack.getTag().getInt(Constants.BAG_ID_TAG_KEY);
        return 0;
    }

    @Override
    public void scroll(Player player, int slot, int delta) {
        Test test = SaveData.getInstance("test");
        delta += test.getCounter();
        Log.warn("validated scroll: " + delta + " for slot " + slot);
        test.setCounter(delta);
    }

    @Override
    public void scrollFeedBack(Player player, int slot, int delta) {

    }

    @Override
    public boolean canScroll(Player player, int slot) { return DimBag.ACTION_KEY.getState(player); }
}
