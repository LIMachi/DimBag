package com.limachi.dim_bag.bag_modules.block_entity;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.registries.annotations.RegisterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SlotModuleBlockEntity extends BlockEntity {

    @RegisterBlockEntity(blocks = "slot_module")
    public static RegistryObject<BlockEntityType<SlotModuleBlockEntity>> R_TYPE;

    public SlotModuleBlockEntity(BlockPos pos, BlockState state) { super(R_TYPE.get(), pos, state); }

    private LazyOptional<IItemHandler> slotHandle = null;

    @Override
    public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (slotHandle == null) {
                BagInstance bag = BagsData.getBagHandle(level, getBlockPos(), ()->slotHandle = null);
                if (bag != null) {
                    slotHandle = bag.slotHandle(getBlockPos());
                    slotHandle.addListener(t -> slotHandle = null);
                }
            }
            if (slotHandle != null)
                return slotHandle.cast();
        }
        return super.getCapability(cap, side);
    }
}
