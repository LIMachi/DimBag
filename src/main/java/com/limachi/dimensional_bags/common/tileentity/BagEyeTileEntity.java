package com.limachi.dimensional_bags.common.tileentity;

import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.IdHandler;
import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import javax.annotation.Nullable;

public class BagEyeTileEntity extends TileEntity implements ITickableTileEntity, ISidedInventory {

    private int id = 0;
    private boolean initialized = false;

    public BagEyeTileEntity() {
        super(Registries.BAG_EYE_TE.get());
    }

    @Override
    public void tick() {
        if (!this.initialized)
            this.init();
    }

    public IdHandler getId() { return new IdHandler(this.id); }

    private void init() {
        if ((this.pos.getX() - 8) % 1024 == 0 && this.pos.getY() == 128 && this.pos.getZ() == 8)
            this.id = (this.pos.getX() - 8) / 1024 + 1;
        else
            this.id = 0;
        this.initialized = true;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        new IdHandler(this.id).write(compound);
        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        new IdHandler(compound).write(this);
    }

    protected EyeData getEyeData() {
        if (this.world == null || this.world.isRemote() || this.id == 0) return null;
        return DimBagData.get(this.world.getServer()).getEyeData(this.id);
    }

    @Override
    public int getSizeInventory() {
        EyeData data = this.getEyeData();
        if (data == null) return 0;
        return data.items.getSizeInventory();
    }

    @Override
    public boolean isEmpty() {
        EyeData data = this.getEyeData();
        if (data == null) return true;
        return data.items.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        EyeData data = this.getEyeData();
        if (data == null) return ItemStack.EMPTY;
        return data.items.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        EyeData data = this.getEyeData();
        if (data == null) return ItemStack.EMPTY;
        return data.items.decrStackSize(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        EyeData data = this.getEyeData();
        if (data == null) return ItemStack.EMPTY;
        return data.items.removeStackFromSlot(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        EyeData data = this.getEyeData();
        if (data == null) return;
        data.items.setInventorySlotContents(index, stack);
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) { return true; }

    @Override
    public void clear() {}

    @Override
    public int[] getSlotsForFace(Direction side) { //FIXME: for now, just return all the slots
        int[] ret = new int[this.getSizeInventory()];
        for (int i = 0; i < ret.length; ++i)
            ret[i] = i;
        return ret;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        return true; //FIXME: missing logic
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        return true; //FIXME: missing logic
    }
}