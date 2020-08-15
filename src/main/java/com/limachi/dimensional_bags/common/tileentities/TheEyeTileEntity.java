package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class TheEyeTileEntity extends TileEntity implements ITickableTileEntity {

    private LazyOptional<Wrapper> invPtr = LazyOptional.empty();
    private EyeData data;
    private boolean initialized;

    public TheEyeTileEntity() {
        super(Registries.BAG_EYE_TE.get());
    }

    private void init() {
        data = EyeData.getEyeData(this.world, this.pos, true);
        if (data != null)
            invPtr = LazyOptional.of(() -> data.getInventory());
        initialized = true;
    }

    @Override
    public void tick() {
        if (!initialized)
            init();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (data != null) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return invPtr.cast();
            }
        }
        return super.getCapability(capability, facing);
    }
}
