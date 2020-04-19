package com.limachi.dimensional_bags.common.nbt;

import com.limachi.dimensional_bags.common.config.DimBagConfig;
import com.limachi.dimensional_bags.common.dimensions.BagDimension;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.tileentity.BagEyeTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

public class bagNBT implements net.minecraftforge.common.util.INBTSerializable<CompoundNBT> {

    //contains all the important information about a bag
    //the same data is present on item/entity/eye of a bag, eye always having priority
    //nbt should never be used without using this class (for debuging purpose)

    private int ID;
    private int rows;
    private int columns;
    private int radius;

    public bagNBT() {
        ID = -1;
        rows = DimBagConfig.startingRows;
        columns = DimBagConfig.startingColumns;
        radius = DimBagConfig.startingRadius;
    }

    public bagNBT(int id, MinecraftServer server) { //try to get nbt from eye, otherwise return default nbt with id already set
        this();
        BagEyeTileEntity te = BagDimension.getRoomEye(server, id);
        if (te == null)
        {
            this.ID = id;
            return;
        }
        this.commonNBT(te.getTileData());
    }

    public bagNBT(ItemStack bag) { //get data from a bag itemstack
        this();
        if (bag.getItem() instanceof Bag)
            this.commonNBT(bag.getTag());
    }

    public bagNBT(BagEntity bag) { //get data from a bag entity
        this();
        this.commonNBT(bag.getPersistentData());
    }

    public bagNBT(BagEyeTileEntity eye) { //get data from the eye
        this();
        this.commonNBT(eye.getTileData());
    }

    public void writeToBag(ItemStack bag) { //store data to the bag itemstack
        if (bag.getItem() instanceof Bag)
            bag.setTag(this.serializeNBT());
    }

    public void writeToBag(BagEntity bag) { //store data to the bag entity
        this.write(bag.getPersistentData());
    }

    public void writeToEye(BagEyeTileEntity eye) { //store data to the eye
        this.write(eye.getTileData());
    }

    public void writeToEye(int id, MinecraftServer server) {
        BagEyeTileEntity te = BagDimension.getRoomEye(server, id);
        if (te == null) return;
        this.writeToEye(te);
    }

    public static void syncBag(ItemStack bag, MinecraftServer server) { //try to access the eye of the bag and rewrite the nbt of the itemstack accordingly
        bagNBT nbt = new bagNBT(bag);
        if (nbt.ID != -1) {
            BagEyeTileEntity te = BagDimension.getRoomEye(server, nbt.ID);
            if (te == null)
                return;
            bagNBT nbt1 = new bagNBT(te);
            nbt1.writeToBag(bag);
        }
    }

    public static void syncBag(BagEntity bag, MinecraftServer server) { //try to access the eye of the bag and rewrite the nbt of the bag entity accordingly
        bagNBT nbt = new bagNBT(bag);
        if (nbt.ID != -1) {
            BagEyeTileEntity te = BagDimension.getRoomEye(server, nbt.ID);
            if (te == null)
                return;
            bagNBT nbt1 = new bagNBT(te);
            nbt1.writeToBag(bag);
        }
    }

    public void commonNBT(CompoundNBT nbt) {
        if (nbt == null) return ;
        nbt = nbt.getCompound(MOD_ID);
        if (nbt != null)
            this.deserializeNBT(nbt);
    }

    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("ID", this.ID);
        nbt.putInt("rows", this.rows);
        nbt.putInt("columns", this.columns);
        nbt.putInt("radius", this.radius);
        return nbt;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.ID = nbt.getInt("ID");
        this.rows = nbt.getInt("rows");
        this.columns = nbt.getInt("columns");
        this.radius = nbt.getInt("radius");
    }
}
