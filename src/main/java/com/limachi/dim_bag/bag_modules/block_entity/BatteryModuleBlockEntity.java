package com.limachi.dim_bag.bag_modules.block_entity;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.bag_modules.BatteryModule;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BatteryModuleBlockEntity extends BlockEntity {

    @RegisterBlockEntity(blocks = "battery_module")
    public static RegistryObject<BlockEntityType<BatteryModuleBlockEntity>> R_TYPE;

    public BatteryModuleBlockEntity(BlockPos pos, BlockState state) { super(R_TYPE.get(), pos, state); }

    private LazyOptional<IEnergyStorage> energyHandle = null;
    public double renderEnergy = 0;

    @Override
    public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            if (energyHandle == null) {
                BagInstance bag = BagsData.getBagHandle(level, getBlockPos(), ()->energyHandle = null);
                if (bag != null) {
                    energyHandle = bag.energyHandle().cast();
                    energyHandle.addListener(t -> energyHandle = null);
                }
            }
            if (energyHandle != null)
                return energyHandle.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag out = super.getUpdateTag();
        out.putDouble("render_energy", renderEnergy);
        return out;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("render_energy", renderEnergy);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        renderEnergy = tag.getDouble("render_energy");
    }

    public int getColor() {
        int grey = Mth.clamp((int)Math.round((1. + renderEnergy * 14. - getFullness(renderEnergy)) * 255), 0, 255);
        return 0xFF000000 | (grey << 16) | (grey << 8) | grey;
    }

    int getFullness(double ratio) {
        return Math.min(13, (int)Math.ceil(ratio * 14.));
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
    }

    public void tick() {
        if (level instanceof ServerLevel serverLevel)
            getCapability(ForgeCapabilities.ENERGY).ifPresent(h->{
                //do push/pull there
                double ratio = (double)h.getEnergyStored() / (double)h.getMaxEnergyStored();
                if (renderEnergy > ratio || renderEnergy < ratio) {
                    renderEnergy = ratio;
                    setChanged();
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                    int fullness = getFullness(ratio);
                    if (fullness != getBlockState().getValue(BatteryModule.FULLNESS))
                        level.setBlock(worldPosition, getBlockState().setValue(BatteryModule.FULLNESS, fullness), 3);
//                    serverLevel.players().forEach(p->p.connection.send(getUpdatePacket()));
                }
            });
    }
}
