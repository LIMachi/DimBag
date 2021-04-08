package com.limachi.dimensional_bags.common.network;

import com.limachi.dimensional_bags.common.container.BaseContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.UUID;
/*
public class Network {

    public static <T extends BaseContainer> void openContainer(ServerPlayerEntity player, T container) {
        NetworkHooks.openGui(player, container, container::writeToBuff);
    }

    public static void openEyeInventory(ServerPlayerEntity player, int eyeId, UUID slot) { //FIXME
//        ISimpleItemHandlerSerializable inv = InventoryData.execute(eyeId, d-> d.getPillarInventory(slot), null);
//        ISimpleFluidHandlerSerializable tank = TankData.execute(eyeId, d-> d.getFountainTank(slot), null);
//        if (inv != null || tank != null)
//            SimpleContainer.open(player, new TranslationTextComponent("inventory.bag.name"), inv, tank);

//        InventoryData data = InventoryData.getInstance(eyeId);
//        NetworkHooks.openGui(player, new INamedContainerProvider() {
//            @Override
//            public ITextComponent getDisplayName() {
//                return new TranslationTextComponent("inventory.bag.name");
//            }
//
//            @Nullable
//            @Override
//            public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
//                return SimpleContainer.CreateServer(windowId, inventory, data, slot);
//            }
//        }, (buffer) -> {
//            data.getUserInventory().sizeAndRightsToBuffer(buffer);
//        });
    }

//    public static void openWrappedPlayerInventory(ServerPlayerEntity player, PlayerInvWrapper inv, TileEntity te) {
//        NetworkHooks.openGui(player, new INamedContainerProvider() {
//                @Override
//                public ITextComponent getDisplayName() {
//                    return new TranslationTextComponent("inventory.player_interface.name");
//                }
//
//                @Nullable
//                @Override
//                public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
//                    return new WrappedPlayerInventoryContainer(windowId, playerInv, te, inv);
//                }
//            }, (buffer) -> {
//                WrappedPlayerInventoryContainer.writeParameters(buffer, te, inv.matchInventory(player.inventory), inv);
//        });
//    }
//
//    public static void openBrainInterface(ServerPlayerEntity player, BrainTileEntity te) {
//        NetworkHooks.openGui(player, new INamedContainerProvider() {
//            @Override
//            public ITextComponent getDisplayName() {
//                return new TranslationTextComponent("inventory.brain.name");
//            }
//
//            @Nullable
//            @Override
//            public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
//                return new BrainContainer(windowId, playerInv, te);
//            }
//        }, (buffer)-> BaseContainer.writeBaseParameters(buffer, BaseContainer.ContainerConnectionType.TILE_ENTITY, te, 0));
//    }
//
//    public static void openGhostHandInterface(ServerPlayerEntity player, GhostHandTileEntity te) {
//        NetworkHooks.openGui(player, new INamedContainerProvider() {
//            @Override
//            public ITextComponent getDisplayName() {
//                return new TranslationTextComponent("inventory.ghost_hand.name");
//            }
//
//            @Nullable
//            @Override
//            public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
//                return new GhostHandContainer(windowId, playerInv, te);
//            }
//        }, (buffer)-> BaseContainer.writeBaseParameters(buffer, BaseContainer.ContainerConnectionType.TILE_ENTITY, te, 0));
//    }

    public static void openSettingsGui(ServerPlayerEntity player, int eyeId, int slot) {
//        SimpleContainer.open(player, new TranslationTextComponent("inventory.bag.name"), handler);
//        NetworkHooks.openGui(player, new INamedContainerProvider() {
//            @Override
//            public ITextComponent getDisplayName() { return new TranslationTextComponent("inventory.settings.name"); }
//
//            @Nullable
//            @Override
//            public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
//                return new SettingsContainer(windowId, playerInv, slot);
//            }
//        }, buffer->BaseContainer.writeBaseParameters(buffer, BaseContainer.ContainerConnectionType.ITEM, null, slot));
    }
}*/
