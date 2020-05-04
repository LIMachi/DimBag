package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.container.BagContainer;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.inventory.BaseInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class TheEyeTileEntity extends TileEntity implements ITickableTileEntity, IContainerProvider {

    private LazyOptional<BaseInventory> invPtr = LazyOptional.empty();
    private EyeData data;
    private boolean initialized;

    public TheEyeTileEntity() {
        super(Registries.BAG_EYE_TE.get());
    }

    private void init() {
        data = getEyeData();
        if (data != null)
            invPtr = LazyOptional.of(() -> getEyeData().getInventory());
        initialized = true;
    }

    protected EyeData getEyeData() {
        if (this.world == null || !DimBag.isServer(this.world) || (this.pos.getX() - 8) % 1024 != 0) return null;
        return EyeData.get(this.world.getServer(), (this.pos.getX() - 8) / 1024 + 1);
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
        if (data != null)
            return new BagContainer(windowId, (ServerPlayerEntity) player, data.getInventory());
        return null;
    }

    @Override
    public void tick() {
        if (!initialized)
            init();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return invPtr.cast();
        }
        return super.getCapability(capability, facing);
    }
}
