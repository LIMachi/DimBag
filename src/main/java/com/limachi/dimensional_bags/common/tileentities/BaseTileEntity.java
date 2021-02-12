package com.limachi.dimensional_bags.common.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import javax.annotation.Nullable;

/**
 * inspired by many mods, including mekanism, refined storage, mantle, tinker construct, etc...
 */

public abstract class BaseTileEntity extends TileEntity {

    protected BlockState cachedBlockState; //used to shunt the 'private' nature of cachedBlockState in TileEntity (only used in 3 methods: getBlockState, updateContainingBlockInfo and markDirty)

    public BaseTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public boolean isClient() { return world != null && world.isRemote(); }

    @Override
    public void markDirty() {
        baseMarkDirty(true);
    }

    public boolean hasComparatorOutput() { return false; }

    protected void baseMarkDirty(boolean reloadCache) {
        if (world != null) {
            if (reloadCache)
                cachedBlockState = world.getBlockState(pos);
            world.markChunkDirty(pos, this);
            if (hasComparatorOutput() && !isClient())
                world.updateComparatorOutputLevel(pos, cachedBlockState.getBlock());
        }
    }

    @Override
    public BlockState getBlockState() {
        if (cachedBlockState == null && world != null)
            cachedBlockState = world.getBlockState(pos);
        return cachedBlockState;
    }

    @Override
    public void updateContainingBlockInfo() {
        cachedBlockState = null;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return writeUpdateTag(super.getUpdateTag());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 0, getUpdateTag());
    }

    public CompoundNBT writeUpdateTag(CompoundNBT nbt) { return nbt; }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        readUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        read(state, tag);
        readUpdateTag(tag);
    }

    public void readUpdateTag(CompoundNBT nbt) {}
}
