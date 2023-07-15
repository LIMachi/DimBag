package com.limachi.dim_bag.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
public interface ICapabilityProviderBlock {
    @Nonnull
    <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, @Nonnull final Level level, @Nonnull final BlockPos pos, final @Nullable Direction side);

    @Nonnull
    default <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, @Nonnull final Level level, @Nonnull final BlockPos pos) {
        return getCapability(cap, level, pos, null);
    }
}
