package com.limachi.dim_bag.blocks;

import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class WallBlock extends Block {

    @Configs.Config(cmt = "List of blocks (ressource style, regex compatible) that should not be valid as walls. Example: 'minecraft:tnt' because when lit they let a hole in the walls. 'nice_mod:.*' would match any block added by the mod named 'nice_mod', falling block (like sand) are always disabled")
    public static String[] BLACKLISTED_WALL_BLOCKS = new String[] {"minecraft:tnt"};

    @RegisterBlock
    public static RegistryObject<Block> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<Item> R_ITEM;

    public WallBlock() { super(Properties.copy(Blocks.BEDROCK).sound(SoundType.WOOL).isSuffocating((s, l, p)->false).mapColor(MapColor.WOOL).noOcclusion()); }
}
