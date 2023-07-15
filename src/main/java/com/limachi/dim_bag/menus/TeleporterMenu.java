package com.limachi.dim_bag.menus;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_modules.data.TeleporterData;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.menus.IAcceptUpStreamNBT;
import com.limachi.lim_lib.network.messages.ScreenNBTMsg;
import com.limachi.lim_lib.registries.annotations.RegisterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

public class TeleporterMenu extends AbstractContainerMenu implements IAcceptUpStreamNBT {
    @RegisterMenu
    public static RegistryObject<MenuType<BagMenu>> R_TYPE;

    public static void open(Player player, BlockPos pos) {
        if (!player.level().isClientSide) {
//            Teleporters.getTeleporter(player.level(), pos).ifPresent(td->
//                NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((id, inv, p) -> new TeleporterMenu(id, inv, td), td.getLabel()), buff -> buff.writeNbt(td.serializeNBT())));
        }
    }

    public TeleporterMenu(int id, Inventory playerInventory, FriendlyByteBuf buff) {
        this(id, playerInventory, new TeleporterData(buff.readNbt()));
    }

    public final TeleporterData data;

    public TeleporterMenu(int id, Inventory playerInventory, TeleporterData data) {
        super(R_TYPE.get(), id);
        this.data = data;
    }

    public void close() { ScreenNBTMsg.send(0, data.serializeNBT()); }

    @Override
    public ItemStack quickMoveStack(Player p_38941_, int p_38942_) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public void upstreamNBTMessage(int i, CompoundTag compoundTag) {
//        Teleporters.getTeleporter(World.getLevel(DimBag.BAG_DIM), BlockPos.of(compoundTag.getLong("pos"))).ifPresent(td->td.replace(compoundTag));
    }
}
