package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.bag.BagEntity;
import com.limachi.dimensional_bags.common.bag.BagEntityItem;
import com.limachi.dimensional_bags.common.bag.BagItem;
import com.limachi.dimensional_bags.common.bag.GhostBagItem;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.CuriosIntegration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import java.util.Random;
import java.util.function.Supplier;

@StaticInit
public class CloudBlock extends Block {

    public static final String NAME = "cloud";

    public static final Supplier<CloudBlock> INSTANCE = Registries.registerBlock(NAME, CloudBlock::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 6);

    public CloudBlock() {
        super(Properties.of(new Material(MaterialColor.SNOW, false, false, true, false, false, true, PushReaction.DESTROY)).strength(0).sound(SoundType.SNOW).isValidSpawn((s,r,p,e)->false).isRedstoneConductor((s,r,p)->false).isSuffocating((s,r,p)->false).isViewBlocking((s,r,p)->false).noOcclusion());
        this.registerDefaultState(defaultBlockState().setValue(AGE, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE);
    }


    @Override
    public void fallOn(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        entityIn.causeFallDamage(fallDistance, 0.0F);
    }

    @Override
    public void updateEntityAfterFallOn(IBlockReader worldIn, Entity entityIn) {
        Vector3d vector3d = entityIn.getDeltaMovement();
        entityIn.setDeltaMovement(vector3d.x, 0, vector3d.z);
    }

    @OnlyIn(Dist.CLIENT)
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) { return 1.0F; }

    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) { return true; }

    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        this.tick(state, worldIn, pos, random);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) { return state.getValue(AGE) != 0 || super.isRandomlyTicking(state); }

    @Override
    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getValue(AGE) != 0)
            worldIn.getBlockTicks().scheduleTick(pos, this, 10);
        
        super.onPlace(state, worldIn, pos, oldState, isMoving);
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        int age = state.getValue(AGE);
        if (age != 0) {
            if (worldIn.getEntitiesOfClass(Entity.class, new AxisAlignedBB(pos.offset(-5, -5, -5), pos.offset(5, 5, 5)), e->e instanceof BagEntityItem || e instanceof BagEntity || (e instanceof LivingEntity && CuriosIntegration.searchItem(e, BagItem.class, s->true).isValid())).isEmpty())
                worldIn.setBlock(pos, age < 6 ? state.setValue(AGE, age + 1) : Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
            worldIn.getBlockTicks().scheduleTick(pos, this, MathHelper.nextInt(rand, 10, 40));
        }
        super.tick(state, worldIn, pos, rand);
    }
}
