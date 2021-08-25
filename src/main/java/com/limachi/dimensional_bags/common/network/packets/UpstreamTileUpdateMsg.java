package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.common.network.PacketHandler;
import com.limachi.dimensional_bags.common.tileentities.BaseTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class UpstreamTileUpdateMsg extends PacketHandler.Message {
    private BlockPos pos;
    private CompoundNBT data;

    public UpstreamTileUpdateMsg(PacketBuffer buffer) {
        this.pos = buffer.readBlockPos();
        this.data = buffer.readAnySizeNbt();
    }

    public UpstreamTileUpdateMsg(BlockPos pos, CompoundNBT dataIn) {
        this.pos = pos;
        this.data = dataIn;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeNbt(data);
    }

    @Override
    public void serverWork(ServerPlayerEntity player) {
        if (player != null) {
            TileEntity t = player.level.getBlockEntity(pos);
            if (!(t instanceof BaseTileEntity)) return;
            BaseTileEntity te = (BaseTileEntity)t;
            if (te.validateUpstreamUpdate(data))
                te.readDataPacket(data);
            te.setChanged();
        }
    }
}
