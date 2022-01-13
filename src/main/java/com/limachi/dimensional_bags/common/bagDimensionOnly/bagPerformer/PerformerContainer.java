package com.limachi.dimensional_bags.common.bagDimensionOnly.bagPerformer;

import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagWatcher.WatcherContainer;
import com.limachi.dimensional_bags.lib.common.container.BaseEyeContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

import com.limachi.dimensional_bags.StaticInit;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;

@StaticInit
public class PerformerContainer extends BaseEyeContainer {

    public static final String NAME = "performer";

    static {
        Registries.registerContainer(NAME, WatcherContainer::new);
    }

    public String command;

    public PerformerContainer(int windowId, PlayerInventory playerInv, int eye) {
        super(Registries.getContainerType(NAME), windowId, playerInv, eye);
    }

    public PerformerContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.getContainerType(NAME), windowId, playerInv, extraData);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() { return new TranslationTextComponent("container.display_name." + NAME); }
}
