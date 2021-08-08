package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.entity.player.PlayerInventory;
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

    public SettingsContainer(int windowId, PlayerInventory playerInv, int eye) {
        super(Registries.getContainerType(NAME), windowId, playerInv, eye);
    }

    public SettingsContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.getContainerType(NAME), windowId, playerInv, extraData);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() { return new TranslationTextComponent("container.display_name." + NAME); }
}