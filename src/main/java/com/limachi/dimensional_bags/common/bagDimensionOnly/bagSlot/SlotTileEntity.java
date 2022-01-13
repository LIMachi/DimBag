package com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.lib.common.tileentities.IInstallUpgradeTE;
import com.limachi.dimensional_bags.lib.common.tileentities.IisBagTE;
import com.limachi.dimensional_bags.lib.common.tileentities.TEWithUUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

@StaticInit
public class SlotTileEntity extends TEWithUUID implements IisBagTE, IInstallUpgradeTE {

    public static final String NAME = "slot";

    private int bagId;

    static {
        Registries.registerTileEntity(NAME, SlotTileEntity::new, ()->Registries.getBlock(SlotBlock.NAME), null);
    }

    public int getbagId() {
        if (bagId == 0)
            bagId = SubRoomsManager.getbagId(level, worldPosition, false);
        return bagId;
    }

    public SlotTileEntity() { super(Registries.getBlockEntityType(NAME)); }

    public SlotInventory getInventory() { //FIXME: find why sometimes the id is invalid (very annoying for new bags generated with invalid ids)
        return (SlotInventory)InventoryData.execute(getbagId(), invData->invData.getPillarInventory(getUUID()), null);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.of(this::getInventory).cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public ItemStack installUpgrades(PlayerEntity player, ItemStack stack) { return getInventory().getUpgradesInventory().installUpgrades(stack); }
}
