package com.limachi.dim_bag.client.screens;

import com.limachi.dim_bag.client.widgets.TextEdit;
import com.limachi.dim_bag.menus.TankMenu;
import com.limachi.dim_bag.menus.slots.TankSlot;
import com.limachi.lim_lib.network.messages.ScreenNBTMsg;
import com.limachi.lim_lib.registries.clientAnnotations.RegisterMenuScreen;
import com.limachi.lim_lib.render.GuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@RegisterMenuScreen
public class TankScreen extends AbstractContainerScreen<TankMenu> {

    private Component label;

    public TankScreen(TankMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.empty());
        label = title;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new TextEdit(font, titleLabelX + getGuiLeft() - 2, titleLabelY + getGuiTop(), imageWidth - titleLabelX - 2, 14, label.getString(), w->{
            label = Component.literal(w.getValue());
            CompoundTag out = new CompoundTag();
            out.putString("label", Component.Serializer.toJson(label));
            ScreenNBTMsg.send(0, out);
        }));
    }

    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
        super.renderLabels(p_281635_, p_282681_, p_283686_);
    }

    @Override
    public void renderSlot(@Nonnull GuiGraphics gui, @Nonnull Slot slot) {
        if (slot instanceof TankSlot tank)
            tank.renderSlot(gui);
        else
            super.renderSlot(gui, slot);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        GuiUtils.background(gui);
        for (Slot slot : menu.slots)
            if (!(slot instanceof TankSlot))
                GuiUtils.slots(gui, slot.x - 1, slot.y - 1, 1, 1, false);
    }
}
