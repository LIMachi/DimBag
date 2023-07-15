package com.limachi.dim_bag;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.items.VirtualBagItem;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.ModBase;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.integration.Curios.CuriosIntegration;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Mod(DimBag.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DimBag extends ModBase {
    public static final String MOD_ID = "dim_bag";

    public static final KeyMapController.GlobalKeyBinding BAG_KEY = KeyMapController.registerKeyBind("key.bag_action", InputConstants.KEY_LALT, "key.categories.dim_bag");

    public static final ResourceKey<Level> BAG_DIM = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, new ResourceLocation(DimBag.MOD_ID, "bag"));

    public static <T> T runOnWorld(Function<Level, T> runner, T fail) {
        Level level = World.getLevel(BAG_DIM);
        if (level != null)
            return runner.apply(level);
        return fail;
    }

    public static boolean runOnWorld(Consumer<Level> runner) {
        Level level = World.getLevel(BAG_DIM);
        if (level != null) {
            runner.accept(level);
            return true;
        }
        return false;
    }

    /**
     * Get a bag id > 0 if the entity might interact with a bag (if it has a bag on itself, the entity is inside a room, or if there is a bag entity nearby)
     * @param entity the entity to search from
     * @param id if > 0, only return != 0 if a bag with that specific id is found instead of any bag
     * @param realOnly if true, skip virtual bags inside the entity and room
     * @param includeNearbyBags if true, also look for nearby bag entities
     * @return 0 if no bag found, bag id (> 0) otherwise
     */
    public static int getBagAccess(Entity entity, int id, boolean realOnly, boolean includeNearbyBags, boolean includeRooms) {
        Predicate<ItemStack> pred = i->true;
        if (id > 0)
            if (realOnly)
                pred = i->!(i.getItem() instanceof VirtualBagItem) && BagItem.getBagId(i) == id;
            else
                pred = i->BagItem.getBagId(i) == id;
        else if (realOnly)
            pred = i->!(i.getItem() instanceof VirtualBagItem);
        int out = BagItem.getBagId(CuriosIntegration.searchItem(entity, BagItem.class, pred).get());
        if (out == 0 && includeRooms)
            out = BagsData.runOnBag(entity.level(), entity.blockPosition(), BagInstance::bagId, 0);
        if (out == 0 && includeNearbyBags) {
            List<Entity> f = entity.level().getEntities(entity, new AABB(entity.getX() - 4, entity.getY() - 2, entity.getZ() - 4, entity.getX() + 4, entity.getY() + 2, entity.getZ() + 4), id > 0 ? e->e instanceof BagEntity bag && bag.getBagId() == id : e->e instanceof BagEntity);
            if (f.size() > 0 && f.get(0) instanceof BagEntity bag)
                return bag.getBagId();
        }
        return out;
    }

    public DimBag() { super(MOD_ID, "Dimensional Bags", true, createTab(MOD_ID, MOD_ID, ()->BagItem.R_ITEM)); }
}
