package com.limachi.dimensional_bags.lib.common.container;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;

@StaticInit
public class SettingsContainer extends BaseEyeContainer<SettingsContainer> {

    public static final String NAME = "settings";

    static {
        Registries.registerContainer(NAME, SettingsContainer::new);
    }

    public static void open(PlayerEntity player, int eye) {
        if (player instanceof ServerPlayerEntity)
            BaseContainer.open(player, new SettingsContainer(((ServerPlayerEntity)player).containerCounter + 1, player.inventory, eye));
    }

    private SettingsContainer(int windowId, PlayerInventory playerInv, int eye) {
        super(Registries.getContainerType(NAME), windowId, playerInv, eye);
    }

    public SettingsContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.getContainerType(NAME), windowId, playerInv, extraData);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() { return new TranslationTextComponent("container.display_name." + NAME); }
}