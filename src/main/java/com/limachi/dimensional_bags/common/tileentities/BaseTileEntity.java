package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.common.network.PacketHandler;
import com.limachi.dimensional_bags.common.network.packets.UpstreamTileUpdateMsg;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * inspired by vanilla and many mods, including mekanism, refined storage, mantle, tinker construct, etc...
 * added abstract and final keywords to make sure the creation of TE follow a certain pattern
 */

public abstract class BaseTileEntity extends TileEntity implements ITickableTileEntity {

    protected boolean hasTileData = true; //does this tile hold data that hould be sync and saved (rendering/ticking entities don't always need to store data to disc)
    protected boolean updateUpstream = false; //can this tile be modified client side and should be sync back to the server?
    private BlockState cachedBlockState; //used to shunt the 'private' nature of cachedBlockState in TileEntity (only used in 3 methods: getBlockState, updateContainingBlockInfo and markDirty)
    private CompoundNBT customTileData; //same as the blockstate, we need this to rewrite the way the custom tile data is handled
    private CompoundNBT cachedData; //used to test if the customTileData was changed and produce a diff (a diff is a special CompoundNBT that should be interpreted)
    private int tick = -1;
    private boolean isDirty;

    public BaseTileEntity(TileEntityType<?> tileEntityTypeIn) { super(tileEntityTypeIn); }

    public boolean hasComparatorOutput() { return false; }

    /**
     * called when data is sent from a client to the server (returning false will cancel the update and send the current state of the TE to the client)
     */
    public boolean validateUpstreamUpdate(CompoundNBT nbt) { return true; }

    @Override
    final public void read(BlockState state, CompoundNBT nbt) {
        if (nbt.contains("X") && nbt.contains("Y") && nbt.contains("Z")) pos = new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
        if (nbt.contains("ForgeData")) {
            customTileData = nbt.getCompound("ForgeData");
            cachedData = customTileData.copy();
        }
        if (getCapabilities() != null && nbt.contains("ForgeCaps")) deserializeCaps(nbt.getCompound("ForgeCaps"));
    }

    /**
     * tick since this object was created (not sync, not stored, reset on world/chunk/TE reload)
     */
    final public int getLocalTick() { return tick; }

    public void tick(int tick) {}

    /**
     * this method (super.tick()) should be called last in the override of tick()
     */
    @Override
    final public void tick() {
        tick(++tick);
        processMarkDirty(true);
    }

    @Override
    final public CompoundNBT write(CompoundNBT compound) {
        ResourceLocation resourcelocation = TileEntityType.getId(this.getType());
        if (resourcelocation == null) {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        } else {
            compound.putString("id", resourcelocation.toString());
            compound.putInt("x", this.pos.getX());
            compound.putInt("y", this.pos.getY());
            compound.putInt("z", this.pos.getZ());
            if (this.customTileData != null) compound.put("ForgeData", this.customTileData);
            if (getCapabilities() != null) compound.put("ForgeCaps", serializeCaps());
            return compound;
        }
    }

    final public boolean isClient() { return world != null && world.isRemote(); }

    final public boolean shouldUpdateData() { return customTileData != null && !customTileData.equals(cachedData); }

    @Override
    final public void markDirty() { isDirty = true; }

    final protected void processMarkDirty(boolean reloadCache) {
        if (world != null && isDirty) {
            if (reloadCache)
                cachedBlockState = world.getBlockState(pos);
            world.markChunkDirty(pos, this);
            if (hasComparatorOutput() && !isClient())
                world.updateComparatorOutputLevel(pos, cachedBlockState.getBlock());
            if (updateUpstream && hasTileData && isClient() && shouldUpdateData())
                PacketHandler.toServer(new UpstreamTileUpdateMsg(pos, getTileData()));
            isDirty = false;
        }
    }

    @Override
    final public BlockState getBlockState() {
        if (cachedBlockState == null && world != null)
            cachedBlockState = world.getBlockState(pos);
        return cachedBlockState;
    }

    @Override
    final public void updateContainingBlockInfo() { cachedBlockState = null; }

    @Override
    final public SUpdateTileEntityPacket getUpdatePacket() { return hasTileData && shouldUpdateData() ? new SUpdateTileEntityPacket(getPos(), 0, getTileData()) : null; }

    @Override
    final public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) { if (hasTileData) readDataPacket(pkt.getNbtCompound()); }

    @Nonnull
    @Override
    final public CompoundNBT getUpdateTag() { return write(new CompoundNBT()); }

    @Override
    final public void handleUpdateTag(BlockState state, CompoundNBT tag) { read(state, tag); }

    final public void readDataPacket(CompoundNBT nbt) { customTileData = nbt; cachedData = nbt.copy(); }

    @Override
    final public CompoundNBT getTileData() {
        if (this.customTileData == null)
            this.customTileData = new CompoundNBT();
        return this.customTileData;
    }
}
