package com.limachi.dim_bag.blocks;

import com.limachi.lim_lib.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class WallBlock extends Block {

    @Registries.RegisterBlock
    public static RegistryObject<WallBlock> R_BLOCK;

    @Registries.RegisterBlockItem(block = "R_BLOCK")
    public static RegistryObject<BlockItem> R_ITEM;

    public WallBlock() { super(Properties.of(Material.HEAVY_METAL).strength(-1f, 3600000f).sound(SoundType.WOOL).isRedstoneConductor((s, r, p)->false).noOcclusion().isValidSpawn((s, r, p, a)->false).lightLevel(i->8).isRedstoneConductor((s, r, p)->false)); }
}
