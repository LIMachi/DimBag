package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.PlayerPillar;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import com.limachi.dimensional_bags.common.inventory.PlayerInvWrapper;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import net.minecraft.block.BlockState;
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

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class PlayerPillarTileEntity extends TileEntity implements ITickableTileEntity, IMarkDirty {

    public static final String NAME = "player_pillar";

    static {
        Registries.registerTileEntity(NAME, PlayerPillarTileEntity::new, ()->Registries.getBlock(PlayerPillar.NAME), null);
    }

    private LazyOptional<IItemHandlerModifiable> invPtr = LazyOptional.empty();
    private WeakReference<HolderData> holderDataRef = new WeakReference<>(null);
    private Wrapper.IORights[] rights;
    private int tick;

    public PlayerPillarTileEntity() {
        super(Registries.getTileEntityType(NAME));
        rights = new Wrapper.IORights[41];
        for (int i = 0; i < 41; ++i) { //the default rights are 0-64 items of any kind and IO disabled
            rights[i] = new Wrapper.IORights();
            rights[i].flags = 0;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (int i = 0; i < 41; ++i)
            list.add(rights[i].write(new CompoundNBT()));
        compound.put("IORights", list);
        return super.write(compound);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        ListNBT list = compound.getList("IORights", 10);
        if (list.size() != 41)
            for (int i = 0; i < 41; ++i) {
                rights[i] = new Wrapper.IORights();
                rights[i].flags = 0;
            }
        else
            for (int i = 0; i < 41; ++i)
                rights[i].read(list.getCompound(i));
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
            PlayerInventory inv = holderDataRef.get().getPlayerInventory();
            if (inv != null) {
                IItemHandlerModifiable handler = null;
                if (invPtr.isPresent())
                    handler = invPtr.orElse(null);
                if (!invPtr.isPresent() || handler == null || ((PlayerInvWrapper) handler).getPlayerInventory() != inv)
                    invPtr = LazyOptional.of(() -> new PlayerInvWrapper(inv, rights, this));
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
