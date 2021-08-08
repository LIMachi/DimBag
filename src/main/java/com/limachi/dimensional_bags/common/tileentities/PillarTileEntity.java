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

import java.util.UUID;

@StaticInit
public class PillarTileEntity extends BaseTileEntity implements IisBagTE {

    public static final String NAME = "pillar";

    public static final String NBT_KEY_ID = "ID";

    private int eyeId;

    static {
        Registries.registerTileEntity(NAME, PillarTileEntity::new, ()->Registries.getBlock(Pillar.NAME), null);
    }

    public int getEyeId() {
        if (eyeId == 0)
            eyeId = SubRoomsManager.getEyeId(world, pos, false);
        return eyeId;
    }

    public PillarTileEntity() {
        super(Registries.getTileEntityType(NAME));
        getTileData().putUniqueId(NBT_KEY_ID, UUID.randomUUID());
    }

    public UUID getId() { return getTileData().getUniqueId(NBT_KEY_ID); }

    public void setId(UUID id) { getTileData().putUniqueId(NBT_KEY_ID, id); markDirty(); }

    public PillarInventory getInventory() {
        return (PillarInventory)InventoryData.execute(getEyeId(), invData->invData.getPillarInventory(getId()), null);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(this::getInventory).cast();
        }
        return super.getCapability(capability, facing);
    }
}
