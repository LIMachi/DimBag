package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

import com.limachi.dimensional_bags.StaticInit;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;

@StaticInit
public class BrainContainer extends BaseEyeContainer {

    public static final String NAME = "brain";

    static {
        Registries.registerContainer(NAME, BrainContainer::new);
    }

    public String command;

    public BrainContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.getContainerType(NAME), windowId, playerInv, extraData);
    }

    public BrainContainer(int windowId, PlayerInventory playerInv, int eye) {
        super(Registries.getContainerType(NAME), windowId, playerInv, eye);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() { return new TranslationTextComponent("container.display_name." + NAME); }
}
