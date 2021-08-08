package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Fountain;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.TankData;
import com.limachi.dimensional_bags.common.inventory.FountainTank;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.UUID;

@StaticInit
public class FountainTileEntity extends BaseTileEntity implements IisBagTE {

    public static final String NAME = "fountain";

    public static final String NBT_KEY_ID = "ID";

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
        getTileData().putUniqueId(NBT_KEY_ID, UUID.randomUUID());
    }

    public UUID getId() { return getTileData().getUniqueId(NBT_KEY_ID); }

    public void setId(UUID id) { getTileData().putUniqueId(NBT_KEY_ID, id); markDirty(); }

    public FountainTank getTank() { return (FountainTank)TankData.execute(getEyeId(), tankData->tankData.getFountainTank(getId()), null); }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return LazyOptional.of(this::getTank).cast();
        }
        return super.getCapability(capability, facing);
    }
}
