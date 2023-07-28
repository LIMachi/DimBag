package com.limachi.dim_bag.menus;

import com.limachi.dim_bag.bag_modules.ObserverModule;
import com.limachi.dim_bag.bag_modules.block_entity.ObserverModuleBlockEntity;
import com.limachi.lim_lib.menus.IAcceptUpStreamNBT;
import com.limachi.lim_lib.registries.annotations.RegisterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class ObserverMenu extends AbstractContainerMenu implements IAcceptUpStreamNBT {

    @RegisterMenu
    public static RegistryObject<MenuType<ObserverMenu>> R_TYPE;

    public final ObserverModuleBlockEntity be;
    public final CompoundTag command;
    public final CompoundTag targetData;

    public static void open(Player player, BlockPos pos) {
        if (!player.level().isClientSide && player.level().getBlockEntity(pos) instanceof ObserverModuleBlockEntity be)
            NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((id, inv, p)->new ObserverMenu(id, inv, be, be.saveWithoutMetadata(), be.getTargetData()), Component.translatable("screen.observer.title")), b->b.writeBlockPos(pos).writeNbt(be.saveWithoutMetadata()).writeNbt(be.getTargetData()));
    }

    public ObserverMenu(int id, Inventory playerInventory, ObserverModuleBlockEntity be, CompoundTag bed, CompoundTag targetNbt) {
        super(R_TYPE.get(), id);
        this.be = be;
        this.command = bed.getCompound(ObserverModule.COMMAND_KEY);
        targetData = targetNbt;
    }

    public ObserverMenu(int id, Inventory playerInventory, FriendlyByteBuf buff) {
        this(id, playerInventory, (ObserverModuleBlockEntity)playerInventory.player.level().getBlockEntity(buff.readBlockPos()), buff.readNbt(), buff.readNbt());
    }

    @Override
    public void upstreamNBTMessage(int i, CompoundTag compoundTag) {
        be.replaceCommand(compoundTag);
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) { return be != null && !be.isRemoved() && player.blockPosition().distSqr(be.getBlockPos()) <= 36; }
}
