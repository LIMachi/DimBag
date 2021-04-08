package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.TheEye;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.limachi.dimensional_bags.StaticInit;

/*
@StaticInit
public class TheEyeTileEntity extends TileEntity implements ITickableTileEntity {

    public static final String NAME = "bag_eye";

    static {
        Registries.registerTileEntity(NAME, BrainTileEntity::new, ()->Registries.getBlock(TheEye.NAME), null);
    }

    private LazyOptional<Wrapper> invPtr = LazyOptional.empty();

    public TheEyeTileEntity() {
        super(Registries.getTileEntityType(NAME));
    }

    public <T> List<T> getSurroundingCapabilities(Capability<T> capability){
        if(this.world == null)
            return Collections.emptyList();
        ArrayList<T> list = new ArrayList<>();
        for(Direction facing : Direction.values()){
            TileEntity tile = this.world.getTileEntity(this.pos.offset(facing));
            if(tile != null)
                tile.getCapability(capability, facing.getOpposite()).ifPresent(list::add);
        }
        return list;
    }

    @Override
    public void tick() {
        int amount = 100;
        for(IEnergyStorage storage : getSurroundingCapabilities(CapabilityEnergy.ENERGY)) {
            if (!storage.canReceive())
                continue;
            int amount2 = storage.receiveEnergy(amount, false);
            if(amount2 > 0)
                amount -= amount2;
            if(amount <= 0)
                break ;
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
//        if (data != null) {
//            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
//                return invPtr.cast();
//            }
//            return data.getCapability(capability, facing);
//        }
        return super.getCapability(capability, facing);
    }
}
*/