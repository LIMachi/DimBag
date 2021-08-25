package com.limachi.dimensional_bags.common.tileentities;

import net.minecraft.tileentity.TileEntityType;

import java.util.UUID;

/**
 * TE that guarantees a uuid is generated on the first place, and sync client side from the server
 * this only works if the TE is used with a block of type BlockWithUUID
 */
public class TEWithUUID extends BaseTileEntity {

    public static final String NBT_KEY_UUID = "UUID";

    public static final UUID NULL_UUID = new UUID(0, 0);

    public TEWithUUID(TileEntityType<?> tileEntityTypeIn) { super(tileEntityTypeIn); }

    public UUID getUUID() { return getTileData().contains(NBT_KEY_UUID) ? getTileData().getUUID(NBT_KEY_UUID) : NULL_UUID; }

    public void setUUID(UUID uuid) { getTileData().putUUID(NBT_KEY_UUID, uuid); setChanged(); }
}
