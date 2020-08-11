package com.limachi.dimensional_bags.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class Wall extends Block {
    public Wall() { super(Properties.create(Material.ROCK).hardnessAndResistance(-1f, 3600000f).sound(SoundType.CLOTH)); }
}
