package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.container.BaseContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

@StaticInit
public class ClientSideOnlyScreenHandler extends SimpleContainerScreen<ClientSideOnlyScreenHandler.ClientSideOnlyFakeContainer> {

    private final ClientSideOnlyScreen screen;

    public abstract static class ClientSideOnlyScreen {

        protected ClientSideOnlyScreenHandler handler;

        public ITextComponent getTitle() { return StringTextComponent.EMPTY; }

        public void first() {};

        public void rebuild() {};

        public void end() {};
    }

    public static final String NAME = "dim_bag_cso";

    static {
        Registries.registerContainer(NAME, ClientSideOnlyFakeContainer::new);
    }

    public ClientSideOnlyScreenHandler(ClientSideOnlyFakeContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.screen = null;
        dynamicBackground = false;
    }

    public static void open(ClientSideOnlyScreen screen) {
        if (!DimBag.isServer(null))
            Minecraft.getInstance().setScreen(new ClientSideOnlyScreenHandler(screen, Minecraft.getInstance().player.inventory));
    }

    public static class ClientSideOnlyFakeContainer extends BaseContainer<ClientSideOnlyFakeContainer> {

        protected ClientSideOnlyFakeContainer(int windowId, PlayerInventory playerInv, @Nullable PacketBuffer buffer) {
            super(Registries.getContainerType(NAME), windowId, playerInv, buffer);
        }

        @Override
        public ITextComponent getDisplayName() { return StringTextComponent.EMPTY; }

        @Override
        public boolean stillValid(PlayerEntity player) { return true; }
    }

    private ClientSideOnlyScreenHandler(ClientSideOnlyScreen screen, PlayerInventory inv) {
        super(new ClientSideOnlyFakeContainer(-1, inv, null), inv, screen.getTitle());
        hideContainerTitle = true;
        imageWidth = 176;
        imageHeight = 167;
        screen.handler = this;
        this.screen = screen;
        screen.first();
    }

    @Override
    protected void init() {
        if (screen == null) onClose();
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        screen.rebuild();
    }

    @Override
    public void onClose() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        if (screen != null)
            screen.end();
        super.onClose();
    }
}
