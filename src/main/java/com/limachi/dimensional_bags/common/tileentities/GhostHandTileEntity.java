package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import com.limachi.dimensional_bags.common.executors.EntityExecutor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public class GhostHandTileEntity extends TileEntity implements ITickableTileEntity, IMarkDirty {

    private String command = "set_velocity 0 10 0";

    public GhostHandTileEntity() {
        super(Registries.GHOST_HAND_TE.get());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putString("Command", command);
        return super.write(compound);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        command = compound.getString("Command");
        super.read(state, compound);
    }

    public int getEyeId() {
        return SubRoomsManager.getEyeId(world, pos, false);
    }

    public Entity getHolder(int eyeId) {
        return HolderData.execute(eyeId, HolderData::getEntity, null);
    }

    public void runCommand() {
        int eyeId = getEyeId();
        new EntityExecutor(getHolder(eyeId), eyeId).run(command);
    }

    @Override
    public void tick() {}
}
