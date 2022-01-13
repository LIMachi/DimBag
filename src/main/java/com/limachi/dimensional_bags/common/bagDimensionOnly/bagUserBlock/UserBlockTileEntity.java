package com.limachi.dimensional_bags.common.bagDimensionOnly.bagUserBlock;

import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.tileentities.BaseTileEntity;
import com.limachi.dimensional_bags.lib.common.tileentities.IisBagTE;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.lib.common.inventory.EntityInventoryProxy;

import com.limachi.dimensional_bags.StaticInit;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

@StaticInit
public class UserBlockTileEntity extends BaseTileEntity implements IisBagTE {

    public static final String NAME = "user_block";

    static {
        Registries.registerTileEntity(NAME, UserBlockTileEntity::new, ()->Registries.getBlock(UserBlock.NAME), null);
    }

    private EntityInventoryProxy invProxy = null;
    private int ltc = 0;

    public UserBlockTileEntity() { super(Registries.getBlockEntityType(NAME)); }

    @Override
    public void tick(int tick) {
        if (invProxy == null || invProxy.getEntity() == null) {
            int eye = SubRoomsManager.getbagId(level, worldPosition, false);
            if (eye > 0)
                invProxy = HolderData.execute(eye, HolderData::getEntityInventory, null);
        }
        if (invProxy != null && ltc != invProxy.getLastTickChange()) {
            ltc = invProxy.getLastTickChange();
            if (level != null)
                level.updateNeighbourForOutputSignal(worldPosition, level.getBlockState(worldPosition).getBlock());
        }
    }

    public EntityInventoryProxy getInvProxy() { return invProxy; }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (invProxy != null)
            return invProxy.getCapability(capability, facing);
        return super.getCapability(capability, facing);
    }
}
