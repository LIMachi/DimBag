package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Pillar;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import com.limachi.dimensional_bags.common.inventory.PillarInventory;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.UUID;

@StaticInit
public class PillarTileEntity extends TileEntity implements IMarkDirty {

    public static final String NAME = "pillar";

    public static final String NBT_KEY_ID = "ID";
    public static final String NBT_KEY_EYE = "EyeId";

    public UUID invId = UUID.randomUUID();
    public int eyeId;

    static {
        Registries.registerTileEntity(NAME, PillarTileEntity::new, ()->Registries.getBlock(Pillar.NAME), null);
    }

    public PillarTileEntity() {
        super(Registries.getTileEntityType(NAME));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putUniqueId(NBT_KEY_ID, invId);
        compound.putInt(NBT_KEY_EYE, eyeId);
        return super.write(compound);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if (DimBag.isServer(world)) { //TODO: clean this hack, this is used to silence invalid id client side (ids are only used server side)
            invId = compound.getUniqueId(NBT_KEY_ID);
            eyeId = compound.getInt(NBT_KEY_EYE);
        }
    }

    public PillarInventory getInventory() {
        return (PillarInventory)InventoryData.execute(eyeId, invData->invData.getPillarInventory(invId), null);
    }

    private net.minecraftforge.common.util.LazyOptional<?> itemHandler = net.minecraftforge.common.util.LazyOptional.of(this::getInventory);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, facing);
    }
}
