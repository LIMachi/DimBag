package com.limachi.dim_bag.bag_modules;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class SleepyModule extends BaseModule {

    @Configs.Config
    public static String[] BLACK_LIST_IN_BAG_UNLESS_SLEEPY_MODULE = {"minecraft:.+_bed", "minecraft:respawn_anchor"};

    public static final String NAME = "sleep";

    @RegisterBlock
    public static RegistryObject<SleepyModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) { bag.installModule(NAME, pos, new CompoundTag()); }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) { bag.uninstallModule(NAME, pos); }

    @SubscribeEvent
    public static void placingInvalidBlock(PlayerInteractEvent.RightClickBlock event) {
        if (BagsData.runOnBag(event.getLevel(), event.getPos(), bag->{
            if (bag.isModulePresent(NAME)) return false;
            ResourceLocation item = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
            if (item != null) {
                String test = item.toString();
                return Arrays.stream(BLACK_LIST_IN_BAG_UNLESS_SLEEPY_MODULE).anyMatch(test::matches);
            }
            return false;
        }, true))
            event.setUseItem(Event.Result.DENY);
    }
}
