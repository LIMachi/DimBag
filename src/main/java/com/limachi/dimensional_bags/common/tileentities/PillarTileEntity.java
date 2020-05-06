package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

public class PillarTileEntity extends TileEntity implements ITickableTileEntity, IContainerProvider {

    private LazyOptional<IItemHandlerModifiable> invPtr = LazyOptional.empty();
    private EyeData data;
    private boolean initialized;

    public PillarTileEntity() { super(Registries.PILLAR_TE.get()); }

    /*
    private void init() {
        data = EyeData.getEyeData(this.world, this.pos, false);
        if (data != null && data.getUserPlayer() != null)
            invPtr = LazyOptional.of(() -> new InvWrapper(data.getUserPlayer().inventory));
        initialized = true;
    }
    */

    /*
    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
        if (data != null && data.getUserPlayer() != null)
            return new BagContainer(windowId, (ServerPlayerEntity) player, new PlayerInventoryWrapper(data.getUserPlayer().inventory));
        return null;
    }
    */

    @Override
    public void tick() {
        /*
        if (!initialized)
            init();
    */}

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (data != null) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return invPtr.cast();
            }
        }
        return super.getCapability(capability, facing);
    }

    @Nullable
    @Override
    public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
        return null;
    }
}
