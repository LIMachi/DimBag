package com.limachi.dim_bag.bag_modules;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.bag_modules.block_entity.SlotModuleBlockEntity;
import com.limachi.dim_bag.menus.SlotMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class SlotModule extends BaseModule implements EntityBlock {
    public static final String SLOT_KEY = "slots";

    @RegisterBlock
    public static RegistryObject<SlotModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.slotsHandle().ifPresent(s->s.installSlot(pos, stack.getOrCreateTag()));
    }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.slotsHandle().ifPresent(s->stack.getOrCreateTag().merge(s.uninstallSlot(pos)));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BagsData.runOnBag(level, pos, b->SlotMenu.open(player, b.bagId(), pos));
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return SlotModuleBlockEntity.R_TYPE.get().create(pos, state);
    }
}
