package com.limachi.dim_bag.client.screens;

import com.limachi.dim_bag.client.widgets.BooleanTextButton;
import com.limachi.dim_bag.client.widgets.ICatchEsc;
import com.limachi.dim_bag.menus.TeleporterMenu;
import com.limachi.dim_bag.client.widgets.Builders;
import com.limachi.dim_bag.client.widgets.TextEdit;
import com.limachi.lim_lib.registries.clientAnnotations.RegisterMenuScreen;
import com.limachi.lim_lib.render.GuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@RegisterMenuScreen
public class TeleporterScreen extends AbstractContainerScreen<TeleporterMenu> {
    private static final Component WHITELIST = Component.translatable("screen.teleporter.button.whitelist");
    private static final Component BLACKLIST = Component.translatable("screen.teleporter.button.blacklist");
    private static final Component AFFECT_PLAYER = Component.translatable("screen.teleporter.button.players");
    private static final Component DONT_AFFECT_PLAYER = Component.translatable("screen.teleporter.button.no_players");

    public TeleporterScreen(TeleporterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new TextEdit(font, 10 + getGuiLeft(), 10 + getGuiTop(), 156, 16, menu.data.getLabel().getString(), s->menu.data.setLabel(Component.literal(s.getValue()))));
        addRenderableWidget(new BooleanTextButton(10 + getGuiLeft(), 30 + getGuiTop(), 156, 16, WHITELIST, BLACKLIST, menu.data.isWhitelist(), b->menu.data.setWhitelistState(b.getState())));
        addRenderableWidget(new BooleanTextButton(10 + getGuiLeft(), 50 + getGuiTop(), 156, 16, AFFECT_PLAYER, DONT_AFFECT_PLAYER, menu.data.doesAffectPlayers(), b->menu.data.setAffectPlayersState(b.getState())));
        Builders.editableTextList(this, 10 + getGuiLeft(), 70 + getGuiTop(), 156, 86, menu.data.getFilters(), l->menu.data.setFilters(l.getEntries()));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double fromX, double fromY) {
        return ((getFocused() != null && isDragging() && button == 0) && getFocused().mouseDragged(mouseX, mouseY, button, fromX, fromY)) || super.mouseDragged(mouseX, mouseY, button, fromX, fromY);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (getFocused() instanceof ICatchEsc c)
            return !c.catchEsc();
        return true;
    }

    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics gui, int mouseX, int mouseY) {}

    @Override
    protected void renderBg(@Nonnull GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        GuiUtils.background(gui);
    }

    @Override
    public void onClose() {
        menu.close();
        super.onClose();
    }
}
