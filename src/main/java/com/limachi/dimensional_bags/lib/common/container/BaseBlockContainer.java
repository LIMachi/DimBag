package com.limachi.dimensional_bags.lib.common.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * we can't have a guarantee that the player is in the same dimension as the block (client does not know what world he's in)
 * for this reason, this container should not be used for remote access (unless you stream the block/tileentity itself to the client)
 */
public abstract class BaseBlockContainer<C extends BaseContainer<C>> extends BaseContainer<C> {

    public BlockPos pos;

    protected BaseBlockContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv, BlockPos pos, World world) {
        super(containerType, windowId, playerInv);
        this.pos = pos;
    }

    protected BaseBlockContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv, PacketBuffer buff) {
        super(containerType, windowId, playerInv, buff);
    }

    @Override
    public void readFromBuff(PacketBuffer buff) {
        pos = new BlockPos(buff.readInt(), buff.readInt(), buff.readInt());
    }

    @Override
    public void writeToBuff(PacketBuffer buff) { buff.writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ()); }

    @Override
    public boolean stillValid(@Nonnull PlayerEntity playerIn) {
        return playerIn.position().distanceTo(new Vector3d(pos.getX(), pos.getY(), pos.getZ())) < 6;
    }
}
