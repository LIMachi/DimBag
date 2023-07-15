package com.limachi.dim_bag.bag_modules;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.menus.TeleporterMenu;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

@SuppressWarnings({"unused", "deprecation"})
public class TeleportModule extends BaseModule {

    @RegisterBlock
    public static RegistryObject<TeleportModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Block block, @Nonnull BlockPos pos2, boolean bool) {
//        Teleporters.getTeleporter(level, pos).ifPresent(t->t.setActiveState(level.getBestNeighborSignal(pos) == 0)); //FIXME
        super.neighborChanged(state, level, pos, block, pos2, bool);
    }

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        CompoundTag data = stack.getOrCreateTag();
        data.putLong("pos", pos.asLong());
//        Teleporters.install(bag, new TeleporterData(data)); //FIXME
    }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
//        stack.getOrCreateTag().merge(Teleporters.uninstall(bag, pos)); //FIXME
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        TeleporterMenu.open(player, pos);
        return InteractionResult.SUCCESS;
    }
}
