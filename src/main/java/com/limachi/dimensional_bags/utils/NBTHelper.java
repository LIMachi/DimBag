package com.limachi.dimensional_bags.utils;

import com.limachi.dimensional_bags.common.tileentity.BagEyeTileEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NBTHelper {

    public static CompoundNBT toNBT(Object o) {
        if (o instanceof ItemStack) {
            return writeItemStack((ItemStack)o);
        }

        if (o instanceof BagEyeTileEntity) {
            return writeBagEye((BagEyeTileEntity)o);
        }

        return null;
    }

    private static CompoundNBT writeItemStack(ItemStack i) {
        CompoundNBT compound = new CompoundNBT();
        String rn = i.getItem().getRegistryName().toString();
        if (rn == null)
            rn = Blocks.AIR.asItem().getRegistryName().toString();
        compound.putInt("count", i.getCount());
        compound.putString("item", rn);
        compound.putByte("type", (byte)0);
        return compound;
    }

    private static CompoundNBT writeBagEye(BagEyeTileEntity o) {
        CompoundNBT compound = new CompoundNBT();
        compound.putInt("tick", o.tick);
        return compound;
    }

    @Nullable
    public static Object fromNBT(@Nonnull CompoundNBT compound) {
        switch (compound.getByte("type")) {
            case 0:
                return readItemStack(compound);
            default:
                return null;
        }
    }

    private static ItemStack readItemStack(CompoundNBT compound) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString("item")));
        int count = compound.getInt("count");
        return new ItemStack(item, count);
    }
}
