package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;

import java.util.function.Supplier;

@StaticInit
public class Wall extends Block {

    public static final String NAME = "wall";

    public static final Supplier<Wall> INSTANCE = Registries.registerBlock(NAME, Wall::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public Wall() { super(Properties.of(Material.HEAVY_METAL).strength(-1f, 3600000f).sound(SoundType.WOOL).isRedstoneConductor((s,r,p)->false).noOcclusion().isValidSpawn((s,r,p,a)->false).lightLevel(i->8).isRedstoneConductor((s,r,p)->false)); }
}