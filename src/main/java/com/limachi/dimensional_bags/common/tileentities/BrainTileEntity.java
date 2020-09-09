package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Brain;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import com.limachi.dimensional_bags.common.readers.EntityReader;
import com.limachi.dimensional_bags.common.readers.PlayerReader;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public class BrainTileEntity extends TileEntity implements ITickableTileEntity, IMarkDirty {

    private EyeData data = null;
//    private String command = EntityReader.Commands.COMPARE_KEY_CONSTANT.name() + ";is_bag_key_down;" + EntityReader.Comparator.EQUAL.name() + ";true"; //default test command: look if the player is holding the bag action key TODO: create an interface to edit this command
    private String command = EntityReader.Commands.RANGE_DOUBLE.name() + ";fall_distance;-0.1;5;"; //test if the user is falling (the longer the distance, the stronger the signal)
    private int cachedPower = 0;

    public BrainTileEntity() {
        super(Registries.BRAIN_TE.get());

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

    @Override
    public void tick() {
        if (!DimBag.isServer(world)) return;
        if (data == null) {
            data = EyeData.getEyeData(world, pos, false);
        } else if (command.length() != 0 && data.getUser() != null) {
            Entity user = data.getUser();
            if (user instanceof PlayerEntity) {
                PlayerReader<PlayerEntity> reader = new PlayerReader<>((PlayerEntity) user);
                int r = reader.redstoneFromCommand(command);
                if (r != cachedPower) {
                    world.setBlockState(pos, getBlockState().with(Brain.POWER, r));
                    cachedPower = r;
                }
            }
        }
    }
}
