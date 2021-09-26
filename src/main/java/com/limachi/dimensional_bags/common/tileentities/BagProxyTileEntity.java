package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.BagProxyBlock;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.inventory.BagProxy;
import net.minecraft.util.Direction;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@StaticInit
public class BagProxyTileEntity extends BaseTileEntity  {

    public static final String NAME = "bag_proxy";

    static { Registries.registerTileEntity(NAME, BagProxyTileEntity::new, ()->Registries.getBlock(BagProxyBlock.NAME), null); }

    private final BagProxy proxy = new BagProxy();

    public BagProxyTileEntity() { super(Registries.getBlockEntityType(NAME)); }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) { return proxy.getCapability(cap, side); }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) { return proxy.getCapability(cap); }

    /**
     * used by the block to know if this proxy should run without a bag attached
     */
    public boolean isOP() { return getTileData().getBoolean("IsOP"); }
    public void setOP(boolean state) { getTileData().putBoolean("IsOP", state); setChanged(); }

    public int eyeId() { return getTileData().getInt("EyeId"); }
    public void setID(int eye) {
        getTileData().putInt("EyeId", eye);
        proxy.setEyeID(eye);
        setChanged();
    }

    @Override
    protected void afterTileDataUpdate() { proxy.setEyeID(eyeId()); }

    int alreadyProcessedTick = -1;

    @Override
    public void tick(int tick) {
        int eye = eyeId();
        if (level instanceof ServerWorld && alreadyProcessedTick != tick) {
            HolderData.tickBagWithFakePlayer(eye, level);
            alreadyProcessedTick = tick; //FIXME: security to avoid infinte loops when a proxy ticks inside the bag it is ticking, TODO: we might blacklist proxy from ticking inside bags, at least the one using the same id as the bag
        }
    }


}
