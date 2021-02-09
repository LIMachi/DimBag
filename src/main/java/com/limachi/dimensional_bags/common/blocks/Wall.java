package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class Wall extends Block {

    public static final String NAME = "wall";

    static {
        Registries.registerBlock(NAME, Wall::new);
        Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);
    }

    public Wall() { super(Properties.create(Material.ROCK).hardnessAndResistance(-1f, 3600000f).sound(SoundType.CLOTH)); }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        entityIn.onLivingFall(fallDistance, 0.0F);
    }

    @Override
    public void onLanded(IBlockReader worldIn, Entity entityIn) {
        Vector3d vector3d = entityIn.getMotion();
        entityIn.setMotion(vector3d.x, 0, vector3d.z);
    }
}
