package com.limachi.dim_bag.bag_modules;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.bag_data.TankData;
import com.limachi.dim_bag.bag_modules.block_entity.TankModuleBlockEntity;
import com.limachi.dim_bag.menus.TankMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class TankModule extends BaseModule implements EntityBlock {
    public static final String TANK_KEY = "tanks";

    @RegisterBlock
    public static RegistryObject<TankModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        CompoundTag data = stack.getOrCreateTag().copy();
        if (data.contains("display", Tag.TAG_COMPOUND)) {
            data.putString("label", data.getCompound("display").getString("Name"));
            data.remove("display");
        }
        if (bag.getModeData("Tank").isEmpty())
            bag.installMode("Tank");
        bag.getModeData("Tank").ifPresent(c->{
            if (c.getBoolean("disabled")) {
                c.putBoolean("disabled", false);
                c.putLong("selected", pos.asLong());
            } else if (!c.contains("selected"))
                c.putLong("selected", pos.asLong());
        });
        bag.tanksHandle().ifPresent(s->s.installTank(pos, data));
    }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.getModeData("Tank").ifPresent(c->{
            if (bag.tanksHandle().map(TankData::getTanks).orElse(0) <= 1)
                c.putBoolean("disabled", true);
            else if (c.getLong("selected") == pos.asLong()) {
                long p = bag.tanksHandle().map(d->d.getTank(0).asLong()).orElse(0L);
                if (p != 0)
                    c.putLong("selected", p);
                else
                    c.remove("selected");
            }
        });
        bag.tanksHandle().ifPresent(s->stack.getOrCreateTag().merge(s.uninstallTank(pos)));
    }

    @Override
    public boolean use(BagInstance bag, Player player, Level level, BlockPos pos, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent())
            FluidUtil.interactWithFluidHandler(player, hand, level, pos, null);
        else
            BagsData.runOnBag(level, pos, b-> TankMenu.open(player, b.bagId(), pos));
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return TankModuleBlockEntity.R_TYPE.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level unusedLevel, @Nonnull BlockState unusedState, @Nonnull BlockEntityType<T> unusedType) {
        return (level, pos, state, be) -> {
            if (be instanceof TankModuleBlockEntity o)
                o.tick();
        };
    }
}
