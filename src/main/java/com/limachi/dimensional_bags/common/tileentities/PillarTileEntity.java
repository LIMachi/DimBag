package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import com.limachi.dimensional_bags.common.inventory.IORightsWrappedItemHandler;
import com.limachi.dimensional_bags.common.inventory.InventoryUtils;
import com.limachi.dimensional_bags.common.inventory.PlayerInvWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import net.minecraftforge.items.IItemHandlerModifiable;

import java.lang.ref.WeakReference;

public class PillarTileEntity extends TileEntity implements ITickableTileEntity, IMarkDirty {

    private LazyOptional<InventoryUtils.IIORIghtItemHandler> invPtr = LazyOptional.empty();
    private WeakReference<HolderData> holderDataRef = new WeakReference<>(null);
    private InventoryUtils.ItemStackIORights[] rights;
    private int tick;

    public PillarTileEntity() {
        super(Registries.PILLAR_TE.get());
        rights = new InventoryUtils.ItemStackIORights[41];
        for (int i = 0; i < 41; ++i) { //the default rights are 0-64 items of any kind and IO disabled
            rights[i] = new InventoryUtils.ItemStackIORights(false, false, 0, 64);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (int i = 0; i < 41; ++i)
            list.add(rights[i].writeNBT(new CompoundNBT()));
        compound.put("IORights", list);
        return super.write(compound);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        ListNBT list = compound.getList("IORights", 10);
        if (list.size() != 41)
            for (int i = 0; i < 41; ++i) {
                rights[i] = new InventoryUtils.ItemStackIORights(false, false, 0, 64);
            }
        else
            for (int i = 0; i < 41; ++i)
                rights[i].readNBT(list.getCompound(i));
        super.read(state, compound);
    }

    @Override
    public void tick() {
        ++tick;
        if ((tick & 7) == 0) {
            if (holderDataRef.get() == null)
                holderDataRef = new WeakReference<>(HolderData.getInstance(SubRoomsManager.getEyeId(this.world, this.pos, false)));
            if (holderDataRef.get() == null) {
                if (invPtr.isPresent())
                    invPtr = LazyOptional.empty();
                return;
            }
            Entity entity = holderDataRef.get().getEntity();
            if (entity != null) {
                InventoryUtils.IIORIghtItemHandler handler = null;
                if (invPtr.isPresent())
                    handler = invPtr.orElse(null);
                if (!invPtr.isPresent() || handler == null)
                    invPtr = LazyOptional.of(() -> new IORightsWrappedItemHandler.WrappedEntityInventory(entity, rights));
            } else if (invPtr.isPresent())
                invPtr = LazyOptional.empty();
        }
    }

    public PlayerInvWrapper getWrapper() { return (PlayerInvWrapper)invPtr.orElse(null); }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (holderDataRef.get() != null) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                if (invPtr.isPresent())
                    return invPtr.cast();
            }
        }
        return super.getCapability(capability, facing);
    }
}
