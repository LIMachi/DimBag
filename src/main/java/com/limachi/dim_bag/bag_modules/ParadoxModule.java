package com.limachi.dim_bag.bag_modules;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public class ParadoxModule extends BaseModule {

    public static final String PARADOX_KEY = "paradox";

    @RegisterBlock
    public static RegistryObject<ParadoxModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) { bag.simpleInstall(PARADOX_KEY, pos); }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) { bag.simpleUninstall(PARADOX_KEY, pos); }

    public static boolean isParadoxCompatible(BagInstance bag) { return bag.isPresent(PARADOX_KEY); }
}
