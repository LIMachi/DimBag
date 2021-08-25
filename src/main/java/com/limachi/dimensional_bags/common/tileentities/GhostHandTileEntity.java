package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.GhostHand;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.executors.EntityExecutor;
import net.minecraft.entity.Entity;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class GhostHandTileEntity extends BaseTileEntity implements IisBagTE {

    public static final String NAME = "ghost_hand";

    public static final String NBT_KEY_COMMAND = "Command";

    static {
        Registries.registerTileEntity(NAME, GhostHandTileEntity::new, ()->Registries.getBlock(GhostHand.NAME), null);
    }

    public GhostHandTileEntity() { super(Registries.getBlockEntityType(NAME)); }

    public String getCommand() { return getTileData().getString(NBT_KEY_COMMAND); }

    public void setCommand(String command) { getTileData().putString(NBT_KEY_COMMAND, command); setChanged(); }

    public int getEyeId() { return SubRoomsManager.getEyeId(level, worldPosition, false); }

    public Entity getHolder(int eyeId) { return HolderData.execute(eyeId, HolderData::getEntity, null); }

    public void runCommand() {
        int eyeId = getEyeId();
        new EntityExecutor(getHolder(eyeId), eyeId).run(getCommand());
    }
}
