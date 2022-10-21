package com.limachi.dim_bag.blocks;

import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class WallBlock extends Block {

    @RegisterBlock
    public static RegistryObject<WallBlock> R_BLOCK;

    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    public WallBlock() { super(Properties.of(Material.HEAVY_METAL).strength(-1f, 3600000f).sound(SoundType.WOOL).isRedstoneConductor((s, r, p)->false).noOcclusion().isValidSpawn((s, r, p, a)->false).lightLevel(i->8).isRedstoneConductor((s, r, p)->false)); }
}
