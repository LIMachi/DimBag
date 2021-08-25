package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Pillar;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.inventory.PillarInventory;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

@StaticInit
public class PillarTileEntity extends TEWithUUID implements IisBagTE {

    public static final String NAME = "pillar";

    private int eyeId;

    static {
        Registries.registerTileEntity(NAME, PillarTileEntity::new, ()->Registries.getBlock(Pillar.NAME), null);
    }

    public int getEyeId() {
        if (eyeId == 0)
            eyeId = SubRoomsManager.getEyeId(level, worldPosition, false);
        return eyeId;
    }

    public PillarTileEntity() { super(Registries.getBlockEntityType(NAME)); }

    public PillarInventory getInventory() { //FIXME: find why sometimes the id is invalid (very annoying for new bags generated with invalid ids)
        return (PillarInventory)InventoryData.execute(getEyeId(), invData->invData.getPillarInventory(getUUID()), null);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.of(this::getInventory).cast();
        return super.getCapability(capability, facing);
    }
}
