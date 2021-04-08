package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.container.FountainContainer;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.TankData;
import com.limachi.dimensional_bags.common.inventory.BinaryStateSingleFluidHandler;
import com.limachi.dimensional_bags.common.inventory.ISimpleFluidHandlerSerializable;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.Mode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

public class Tank extends Mode {

    public static final String ID = "Tank";

    public Tank() { super(ID, false, true); }

    protected void playEmptySound(@Nullable PlayerEntity player, IWorld worldIn, BlockPos pos, Fluid fluid) {
        SoundEvent soundevent = fluid.getAttributes().getEmptySound();
        if(soundevent == null) soundevent = fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
        worldIn.playSound(player, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private boolean placeFluid(World world, PlayerEntity player, BlockPos pos, Fluid fluid, @Nullable BlockRayTraceResult rayTrace) {
        BlockState blockstate = world.getBlockState(pos);
        Block block = blockstate.getBlock();
        Material material = blockstate.getMaterial();
        boolean flag = blockstate.isReplaceable(fluid);
        boolean flag1 = blockstate.isAir() || flag || block instanceof ILiquidContainer && ((ILiquidContainer)block).canContainFluid(world, pos, blockstate, fluid);
        if (!flag1) {
            return rayTrace != null && this.placeFluid(world, player, rayTrace.getPos().offset(rayTrace.getFace()), fluid, null);
        } else if (world.getDimensionType().isUltrawarm() && fluid.isIn(FluidTags.WATER)) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

            for(int l = 0; l < 8; ++l) {
                world.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
            }

            return true;
        } else if (block instanceof ILiquidContainer && ((ILiquidContainer)block).canContainFluid(world, pos, blockstate, fluid)) {
            ((ILiquidContainer)block).receiveFluid(world, pos, blockstate, ((FlowingFluid)fluid).getStillFluidState(false));
            this.playEmptySound(player, world, pos, fluid);
            return true;
        } else {
            if (!world.isRemote && flag && !material.isLiquid()) {
                world.destroyBlock(pos, true);
            }

            if (!world.setBlockState(pos, fluid.getDefaultState().getBlockState(), 11) && !blockstate.getFluidState().isSource()) {
                return false;
            } else {
                this.playEmptySound(player, world, pos, fluid);
                return true;
            }
        }
    }

    private boolean canBlockContainFluid(World world, BlockPos pos, BlockState blockstate, Fluid fluid)
    {
        return blockstate.getBlock() instanceof ILiquidContainer && ((ILiquidContainer)blockstate.getBlock()).canContainFluid(world, pos, blockstate, fluid);
    }

    @Override
    public ActionResultType onItemUse(int eyeId, World world, PlayerEntity player, BlockRayTraceResult ray) {
        return onItemRightClick(eyeId, world, player);
    }

    public static ItemStack stackInteraction(ItemStack stack, ISimpleFluidHandlerSerializable tanks, PlayerInventory playerinventory) {
        ItemStack held = stack.copy();
        ItemStack output = ItemStack.EMPTY;
        FluidStack tmpFluid = FluidStack.EMPTY;
        while(!held.isEmpty()) { //we iterate on the stack, to handle if the player has
            ItemStack holding = held.copy();
            if (!holding.isEmpty())
                holding.setCount(1);
            else break;
            IFluidHandlerItem cap = holding.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(BinaryStateSingleFluidHandler.getItemHandler(tanks.getFluidInTank(tanks.getSelectedTank()), holding));
            if (cap == null) break; //held items is no longer valid, get the heck out (might be null if BinaryStateSingleFluidHandler.getItemHandler returns null)
            boolean isFillingItem;
            if (!cap.getFluidInTank(0).isEmpty()) { //stack contains fluid, dump it
                isFillingItem = false;
                tmpFluid = cap.drain(cap.getTankCapacity(0), IFluidHandler.FluidAction.SIMULATE);
                tmpFluid = cap.drain(tanks.fill(tmpFluid, IFluidHandler.FluidAction.SIMULATE), IFluidHandler.FluidAction.EXECUTE);
                holding = cap.getContainer();
            } else { //stack does not contain fluid but can take fluid, fill it
                isFillingItem = true;
                tmpFluid = tanks.getFluidInTank(tanks.getSelectedTank()).copy();
                if (!tmpFluid.isEmpty()) {
                    tmpFluid.setAmount(cap.fill(tmpFluid, IFluidHandler.FluidAction.SIMULATE));
                    tmpFluid = tanks.drain(tmpFluid, IFluidHandler.FluidAction.SIMULATE);
                    if (!tmpFluid.isEmpty())
                        tmpFluid.setAmount(cap.fill(tmpFluid, IFluidHandler.FluidAction.EXECUTE));
                }
                holding = cap.getContainer();
            }
            boolean cf = !holding.equals(held) && !holding.isEmpty(); //stack changed
            if (cf) {
                if (output.isEmpty())
                    output = holding;
                else if (output.isStackable() && output.getCount() < output.getMaxStackSize() && ItemStack.areItemStackTagsEqual(output, holding))
                    output.grow(1);
                else {
                    int t = playerinventory.getFirstEmptyStack();
                    if (t == -1)
                        break; //hover fill, cancel the current try (only the copied itemstack was modified by the above actions, so we are good to leave)
                    else
                        playerinventory.setInventorySlotContents(t, output);
                    output = holding;
                }
            }
            if (isFillingItem)
                tanks.drain(tmpFluid, IFluidHandler.FluidAction.EXECUTE);
            else
                tanks.fill(tmpFluid, IFluidHandler.FluidAction.EXECUTE);
            if (holding.isEmpty() || cf) //the current stack was consumed (either changed or plain removed)
                held.shrink(1);
        }
        if (held.isEmpty())
            return output;
        else if (!output.isEmpty())
            DimBag.LOGGER.error("OUPS");
        return held;
    }

    @Override
    public ActionResultType onItemRightClick(int eyeId, World world, PlayerEntity player) {
        TankData mt = TankData.getInstance(eyeId);
        if (mt == null || mt.getTanks() == 0) return ActionResultType.PASS;
        for (RayTraceContext.FluidMode mode : new RayTraceContext.FluidMode[]{RayTraceContext.FluidMode.SOURCE_ONLY, RayTraceContext.FluidMode.NONE}) {
            RayTraceResult ray = Bag.rayTrace(world, player, mode);
            if (ray.getType() == RayTraceResult.Type.BLOCK) { //we found a source block, first we try to take it, if we can't and the tank is of different fluid type, try to replace the fluid block
                BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult)ray;
                BlockPos blockHit = blockraytraceresult.getPos();
                BlockPos blockOffset = blockHit.offset(blockraytraceresult.getFace());
                if (world.isBlockModifiable(player, blockHit) && player.canPlayerEdit(blockOffset, blockraytraceresult.getFace(), new ItemStack(new Bag()))) {
                    BlockState blockState = world.getBlockState(blockHit);
                    if (blockState.getBlock() instanceof IBucketPickupHandler && !blockState.getFluidState().isEmpty()) {
                        FluidStack targetedFluid = new FluidStack(blockState.getFluidState().getFluid(), 1000);
                        if (mt.fill(targetedFluid, IFluidHandler.FluidAction.SIMULATE) == 1000) { //we should be able to add this stack to the tank
                            ((IBucketPickupHandler)blockState.getBlock()).pickupFluid(world, blockHit, blockState); //we remove the fluid from the block (should be compatible with waterlogged blocks)
                            SoundEvent soundevent = targetedFluid.getFluid().getAttributes().getEmptySound();
                            if (soundevent == null) soundevent = targetedFluid.getFluid().isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
                            player.playSound(soundevent, 1.0F, 1.0F);
                            mt.fill(targetedFluid, IFluidHandler.FluidAction.EXECUTE);
                            return ActionResultType.SUCCESS;
                        } else {
                            FluidStack drainable = mt.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                            if (!drainable.isFluidEqual(targetedFluid) && drainable.getAmount() == 1000 && placeFluid(world, player, canBlockContainFluid(world, blockHit, blockState, drainable.getFluid()) ? blockHit : blockOffset, drainable.getFluid(), blockraytraceresult)) {
                                mt.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                                return ActionResultType.SUCCESS;
                            }
                        }
                    } else {
                        FluidStack drainable = mt.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                        if (drainable.getAmount() == 1000 && placeFluid(world, player, canBlockContainFluid(world, blockHit, blockState, drainable.getFluid()) ? blockHit : blockOffset, drainable.getFluid(), blockraytraceresult)) {
                            mt.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                            return ActionResultType.SUCCESS;
                        }
                    }
                }
                if (mode == RayTraceContext.FluidMode.NONE)
                    return ActionResultType.FAIL;
            }
        }
        new FountainContainer(0, player.inventory, eyeId, null).open(player);
        return ActionResultType.SUCCESS;
    }
}