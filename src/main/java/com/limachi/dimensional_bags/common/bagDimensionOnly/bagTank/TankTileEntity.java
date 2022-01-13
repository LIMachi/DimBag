package com.limachi.dimensional_bags.common.bagDimensionOnly.bagTank;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.tileentities.BaseTileEntity;
import com.limachi.dimensional_bags.lib.common.tileentities.IInstallUpgradeTE;
import com.limachi.dimensional_bags.lib.common.tileentities.IisBagTE;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.TankData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.UUID;

@StaticInit
public class TankTileEntity extends BaseTileEntity implements IisBagTE, IInstallUpgradeTE {

    public static final String NAME = "tank";

    public static final String NBT_KEY_ID = "ID";

    private int bagId;

    static {
        Registries.registerTileEntity(NAME, TankTileEntity::new, ()->Registries.getBlock(TankBlock.NAME), null);
    }

    public int getbagId() {
        if (bagId == 0)
            bagId = SubRoomsManager.getbagId(level, worldPosition, false);
        return bagId;
    }

    public TankTileEntity() {
        super(Registries.getBlockEntityType(NAME));
        getTileData().putUUID(NBT_KEY_ID, UUID.randomUUID());
    }

    public UUID getId() { return getTileData().getUUID(NBT_KEY_ID); }

    public void setId(UUID id) { getTileData().putUUID(NBT_KEY_ID, id); setChanged(); }

    public TankInventory getTank() { return (TankInventory)TankData.execute(getbagId(), tankData->tankData.getFountainTank(getId()), null); }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return LazyOptional.of(this::getTank).cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public ItemStack installUpgrades(PlayerEntity player, ItemStack stack) {
        return stack;
    }
}
