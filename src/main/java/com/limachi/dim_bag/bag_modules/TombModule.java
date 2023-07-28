package com.limachi.dim_bag.bag_modules;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.items.VirtualBagItem;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.Log;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class TombModule extends BaseModule {

    public static final String NAME = "tomb";

    @RegisterBlock
    public static RegistryObject<TombModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) { bag.installModule(NAME, pos, new CompoundTag()); }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) { bag.uninstallModule(NAME, pos); }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void dropEvent(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide) return;
        int[] found = {0};
        event.getDrops().removeIf(e->{
            if (e.getItem().getItem() instanceof BagItem db && !(db instanceof VirtualBagItem)) {
                int id = BagItem.getBagId(e.getItem());
                if (found[0] == 0 && BagsData.runOnBag(id, b->b.isModulePresent(NAME), false))
                    found[0] = id;
                BagEntity.create(event.getEntity().level(), event.getEntity().blockPosition(), id);
                e.remove(Entity.RemovalReason.KILLED);
                return true;
            }
            return false;
        });
        if (found[0] == 0)
            found[0] = DimBag.getBagAccess(event.getEntity(), 0, true, false, true, true);
        BagsData.runOnBag(found[0], bag->
            bag.getAllModules(NAME).getAllKeys().stream().findFirst().ifPresent(at->{
                BlockPos pos;
                try {
                    pos = BlockPos.of(Long.parseLong(at)).relative(Direction.UP);
                } catch (NumberFormatException ignore) {
                    return;
                }
                BlockEntity testContainer = bag.bagLevel().getBlockEntity(pos);
                if (testContainer != null)
                    testContainer.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h->event.getDrops().removeIf(e->{
                        ItemStack stack = e.getItem();
                        if (ItemHandlerHelper.insertItem(h, stack, true).isEmpty()) {
                            ItemHandlerHelper.insertItem(h, stack, false);
                            e.remove(Entity.RemovalReason.KILLED);
                            return true;
                        }
                        return false;
                    }));
                event.getDrops().removeIf(e->{
                    if (bag.enter(e, false) instanceof ItemEntity tpe) {
                        tpe.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        tpe.setNoPickUpDelay();
                        tpe.setUnlimitedLifetime();
                        tpe.setDeltaMovement(0., 0., 0.);
                        return tpe != e;
                    } else
                        Log.error(e, "Could not send dropped item to tomb");
                    return false;
                });
            }));
    }
}
