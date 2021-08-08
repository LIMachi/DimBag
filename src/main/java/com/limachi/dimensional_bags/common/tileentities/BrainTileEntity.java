package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Brain;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.readers.EntityReader;
import net.minecraft.entity.Entity;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class BrainTileEntity extends BaseTileEntity implements IisBagTE {

    public static final String NAME = "brain";

    public static final String NBT_KEY_COMMAND = "Command";

    static {
        Registries.registerTileEntity(NAME, BrainTileEntity::new, ()->Registries.getBlock(Brain.NAME), null);
    }

//    private String command = EntityReader.Commands.COMPARE_KEY_CONSTANT.name() + ";is_bag_key_down;" + EntityReader.Comparator.EQUAL.name() + ";true"; //default test command: look if the player is holding the bag action key TODO: create an interface to edit this command
//    private String command = EntityReader.Commands.RANGE.name() + ";fall_distance;-0.1;5;"; //test if the user is falling (the longer the distance, the stronger the signal)
    private int cachedPower = 0;

    public BrainTileEntity() { super(Registries.getTileEntityType(NAME)); }

    public String getCommand() { return getTileData().getString(NBT_KEY_COMMAND); }

    public void setCommand(String command) { getTileData().putString(NBT_KEY_COMMAND, command); markDirty(); }

    public Entity getHolder() {
        return HolderData.execute(SubRoomsManager.getEyeId(world, pos, false), HolderData::getEntity, null);
    }

    @Override
    public void tick(int tick) {
        if (world == null || !DimBag.isServer(world) || (tick % 4) != 0) return;
        Entity holder = getHolder();
        String command = getCommand();
        if (holder != null && command.length() != 0 && EnergyData.execute(SubRoomsManager.getEyeId(world, pos, false), energyData -> energyData.extractEnergy(8, true) == 8, false)) {
            EnergyData.execute(SubRoomsManager.getEyeId(world, pos, false), energyData -> energyData.extractEnergy(8, false));
            int r = new EntityReader(holder).redstoneFromCommand(command);
            if (r != cachedPower) {
                world.setBlockState(pos, getBlockState().with(Brain.POWER, r));
                cachedPower = r;
            }
        }
    }
}
