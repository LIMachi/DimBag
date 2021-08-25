package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.UserPillar;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.inventory.EntityInventoryProxy;

import com.limachi.dimensional_bags.StaticInit;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

@StaticInit
public class UserPillarTileEntity extends BaseTileEntity implements IisBagTE {

    public static final String NAME = "user_pillar";

    static {
        Registries.registerTileEntity(NAME, UserPillarTileEntity::new, ()->Registries.getBlock(UserPillar.NAME), null);
    }

    private EntityInventoryProxy invProxy = null;
    private int ltc = 0;

    public UserPillarTileEntity() { super(Registries.getBlockEntityType(NAME)); }

    @Override
    public void tick(int tick) {
        if (invProxy == null || invProxy.getEntity() == null) {
            int eye = SubRoomsManager.getEyeId(level, worldPosition, false);
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
