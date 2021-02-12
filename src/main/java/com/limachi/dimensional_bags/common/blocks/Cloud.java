package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class Cloud extends Block {

    public static final String NAME = "cloud";

    static {
        Registries.registerBlock(NAME, Cloud::new);
        Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);
    }
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 6);

    public Cloud() {
        super(Properties.create(new Material(MaterialColor.SNOW, false, false, true, false, false, true, PushReaction.DESTROY)).notSolid().zeroHardnessAndResistance().sound(SoundType.SNOW).setAllowsSpawn((s,r,p,e)->false).setOpaque((s,r,p)->false).setSuffocates((s,r,p)->false).setBlocksVision((s,r,p)->false));
        this.setDefaultState(getDefaultState().with(AGE, 1));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(AGE);
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        entityIn.onLivingFall(fallDistance, 0.0F);
    }

    @Override
    public void onLanded(IBlockReader worldIn, Entity entityIn) {
        Vector3d vector3d = entityIn.getMotion();
        entityIn.setMotion(vector3d.x, 0, vector3d.z);
    }

    @Override
    public boolean isTransparent(BlockState state) { return true; }

    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) { return 0; }

    @Override
    public VoxelShape getRayTraceShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) { return VoxelShapes.empty(); }

    @OnlyIn(Dist.CLIENT)
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) { return 1.0F; }

    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) { return true; }

    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        this.tick(state, worldIn, pos, random);
    }

    @Override
    public boolean ticksRandomly(BlockState state) {
        return state.get(AGE) != 0 || super.ticksRandomly(state);
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.get(AGE) != 0)
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, 10);
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        int age = state.get(AGE);
        if (age != 0) {
            worldIn.setBlockState(pos, age < 6 ? state.with(AGE, age + 1) : Blocks.AIR.getDefaultState());
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, MathHelper.nextInt(rand, 10, 40));
        }
        super.tick(state, worldIn, pos, rand);
    }
}
