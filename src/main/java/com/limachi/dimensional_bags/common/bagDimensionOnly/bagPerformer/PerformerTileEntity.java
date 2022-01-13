package com.limachi.dimensional_bags.common.bagDimensionOnly.bagPerformer;

import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.tileentities.BaseTileEntity;
import com.limachi.dimensional_bags.lib.common.tileentities.IisBagTE;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagPerformer.performances.EntityPerformer;
import net.minecraft.entity.Entity;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class PerformerTileEntity extends BaseTileEntity implements IisBagTE {

    public static final String NAME = "performer";

    public static final String NBT_KEY_COMMAND = "Command";

    static {
        Registries.registerTileEntity(NAME, PerformerTileEntity::new, ()->Registries.getBlock(PerformerBlock.NAME), null);
    }

    public PerformerTileEntity() { super(Registries.getBlockEntityType(NAME)); }

    public String getCommand() { return getTileData().getString(NBT_KEY_COMMAND); }

    public void setCommand(String command) { getTileData().putString(NBT_KEY_COMMAND, command); setChanged(); }

    public int getbagId() { return SubRoomsManager.getbagId(level, worldPosition, false); }

    public Entity getHolder(int bagId) { return HolderData.execute(bagId, HolderData::getEntity, null); }

    public void runCommand() {
        int bagId = getbagId();
        new EntityPerformer(getHolder(bagId), bagId).run(getCommand());
    }
}
