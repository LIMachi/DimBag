package com.limachi.dimensional_bags.common.bagDimensionOnly.bagWatcher;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.tileentities.BaseTileEntity;
import com.limachi.dimensional_bags.lib.common.tileentities.IisBagTE;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagWatcher.watchers.EntityWatcher;
import net.minecraft.entity.Entity;

import com.limachi.dimensional_bags.StaticInit;
import net.minecraftforge.common.util.Constants;

@StaticInit
public class WatcherTileEntity extends BaseTileEntity implements IisBagTE {

    public static final String NAME = "watcher";

    public static final String NBT_KEY_COMMAND = "Command";

    static {
        Registries.registerTileEntity(NAME, WatcherTileEntity::new, ()->Registries.getBlock(WatcherBlock.NAME), null);
    }

//    private String command = EntityReader.Commands.COMPARE_KEY_CONSTANT.name() + ";is_bag_key_down;" + EntityReader.Comparator.EQUAL.name() + ";true"; //default test command: look if the player is holding the bag action key TODO: create an interface to edit this command
//    private String command = EntityReader.Commands.RANGE.name() + ";fall_distance;-0.1;5;"; //test if the user is falling (the longer the distance, the stronger the signal)
    private int cachedPower = 0;

    public WatcherTileEntity() { super(Registries.getBlockEntityType(NAME)); }

    public String getCommand() { return getTileData().getString(NBT_KEY_COMMAND); }

    public void setCommand(String command) { getTileData().putString(NBT_KEY_COMMAND, command); setChanged(); }

    public Entity getHolder() {
        return HolderData.execute(SubRoomsManager.getbagId(level, worldPosition, false), HolderData::getEntity, null);
    }

    @Override
    public void tick(int tick) {
        if (level == null || !DimBag.isServer(level) || (tick % 4) != 0) return;
        Entity holder = getHolder();
        String command = getCommand();
        if (holder != null && command.length() != 0 && EnergyData.execute(SubRoomsManager.getbagId(level, worldPosition, false), energyData -> energyData.extractEnergy(8, true) == 8, false)) {
            EnergyData.execute(SubRoomsManager.getbagId(level, worldPosition, false), energyData -> energyData.extractEnergy(8, false));
            int r = new EntityWatcher(holder).redstoneFromCommand(command);
            if (r != cachedPower) {
                level.setBlock(worldPosition, getBlockState().setValue(WatcherBlock.POWER, r), Constants.BlockFlags.DEFAULT_AND_RERENDER);
                cachedPower = r;
            }
        }
    }
}
