package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.tileentities.TEWithUUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class BlockWithUUID<T extends TEWithUUID> extends AbstractTileEntityBlock<T> {

    public BlockWithUUID(String registryName, Properties properties, Class<T> tileEntityClass, String tileEntityRegistryName) {
        super(registryName, properties, tileEntityClass, tileEntityRegistryName);
    }

    @Override
    public void onValidPlace(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, T tileEntity) {
        if (DimBag.isServer(worldIn) && tileEntity.getUUID() == TEWithUUID.NULL_UUID) //if we are server side and the TE contains a null uuid (not set)
            tileEntity.setUUID(UUID.randomUUID());
    }
}
