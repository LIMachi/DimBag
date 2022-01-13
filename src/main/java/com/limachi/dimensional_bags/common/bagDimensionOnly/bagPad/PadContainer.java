package com.limachi.dimensional_bags.common.bagDimensionOnly.bagPad;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.container.BaseEyeContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;

@StaticInit
public class PadContainer extends BaseEyeContainer<PadContainer> {

    public static final String NAME = "pad";

    private PadTileEntity te;

    static {
        Registries.registerContainer(NAME, PadContainer::new);
    }

    public PadContainer(int windowId, PlayerInventory playerInv, int eye, PadTileEntity te) {
        super(Registries.getContainerType(NAME), windowId, playerInv, eye);
        this.te = te;
    }

    @Override
    public void readFromBuff(PacketBuffer buff) {
        super.readFromBuff(buff);
        TileEntity t = playerInv.player.level.getBlockEntity(buff.readBlockPos());
        if (t instanceof PadTileEntity)
            this.te = (PadTileEntity)t;
        else
            this.te = null;
    }

    @Override
    public void writeToBuff(PacketBuffer buff) {
        super.writeToBuff(buff);
        if (te != null)
            buff.writeBlockPos(te.getBlockPos());
        else
            buff.writeBlockPos(new BlockPos(0, -1, 0));
    }

    public PadContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.getContainerType(NAME), windowId, playerInv, extraData);
        TileEntity t = playerInv.player.level.getBlockEntity(extraData.readBlockPos());
        if (t instanceof PadTileEntity)
            this.te = (PadTileEntity)t;
        else
            this.te = null;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() { return new TranslationTextComponent("container.display_name." + NAME); }
}