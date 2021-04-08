package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Fountain;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.TankData;
import com.limachi.dimensional_bags.common.inventory.FountainTank;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.UUID;

@StaticInit
public class FountainTileEntity extends TileEntity {

    public static final String NAME = "fountain";

    public static final String NBT_KEY_ID = "ID";

    public UUID id;
    private int eyeId;

    static {
        Registries.registerTileEntity(NAME, FountainTileEntity::new, ()->Registries.getBlock(Fountain.NAME), null);
    }

    public int getEyeId() {
        if (eyeId == 0)
            eyeId = SubRoomsManager.getEyeId(world, pos, false);
        return eyeId;
    }

    public FountainTileEntity() {
        super(Registries.getTileEntityType(NAME));
        id = UUID.randomUUID();
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putUniqueId(NBT_KEY_ID, id);
        return super.write(compound);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if (DimBag.isServer(world)) { //TODO: clean this hack, this is used to silence invalid id client side (ids are only used server side)
            id = compound.getUniqueId(NBT_KEY_ID);
        }
    }

    public FountainTank getTank() { return (FountainTank)TankData.execute(getEyeId(), tankData->tankData.getFountainTank(id), null); }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
//        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
//            return LazyOptional.of(this::getInventory).cast();
//        }
        return super.getCapability(capability, facing);
    }
}
