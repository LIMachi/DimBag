package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Pad;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.HashSet;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class PadTileEntity extends TileEntity implements ITickableTileEntity, IMarkDirty {

    public static final String NAME = "pad";

    protected HashSet<String> nameList = new HashSet<>();
    protected boolean isWhitelist = false;
    protected boolean needUpdate = true;
    protected int tick = -1;

    public static final int TICK_RATE = 8;

    static {
        Registries.registerTileEntity(NAME, PadTileEntity::new, ()->Registries.getBlock(Pad.NAME), null);
    }

    public PadTileEntity() { super(Registries.getTileEntityType(NAME)); }

    public void needUpdate() { //should be used when the block change or when read
        this.needUpdate = true;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putBoolean("IsWhitelist", isWhitelist);
        ListNBT list = new ListNBT();
        for (String name : nameList)
            list.add(StringNBT.valueOf(name));
        compound.put("List", list);
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        tick = tick % TICK_RATE;
        needUpdate();
        isWhitelist = compound.getBoolean("IsWhitelist");
        nameList = new HashSet<>();
        for (INBT sn : compound.getList("List", 8))
            nameList.add(((StringNBT)sn).getString()); //NOTE: this cast (even if grayed by most IDE) is necessary to prevent ambiguity with the default getString from INBT
        super.read(state, compound);
    }

    @Override
    public void tick() {
        ++tick;
        if (world == null || !DimBag.isServer(world) || tick % TICK_RATE != 0 || !needUpdate || !(world.getBlockState(pos).getBlock() instanceof Pad)) return;
        SubRoomsManager sm = SubRoomsManager.getInstance(SubRoomsManager.getEyeId(world, pos, false));
        if (sm != null) {
            if (Pad.isPowered(world.getBlockState(pos)))
                sm.activatePad(pos);
            else
                sm.deactivatePad(pos);
        }
        needUpdate = false;
    }

    public boolean isValidEntity(Entity entity) {
        String name = entity.getName().getUnformattedComponentText();
        if (name.isEmpty())
            name = entity.getName().getString();
        DimBag.LOGGER.info("validating entity entering the bag with name: " + name);
        return nameList.contains(name) == isWhitelist;
    }
}
