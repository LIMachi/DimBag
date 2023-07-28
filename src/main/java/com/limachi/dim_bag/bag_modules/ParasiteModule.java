package com.limachi.dim_bag.bag_modules;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public class ParasiteModule extends BaseModule {

    public static final String NAME = "parasite";

    @RegisterBlock
    public static RegistryObject<ParasiteModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    @Override
    public boolean canInstall(BagInstance bag) { return !bag.isModulePresent(NAME); }

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) { bag.installModule(NAME, pos, new CompoundTag()); }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) { bag.uninstallModule(NAME, pos); }
}
