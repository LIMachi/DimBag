package com.limachi.dim_bag.client.screens;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.client.widgets.VerticalSlider;
import com.limachi.dim_bag.menus.slots.BagSlot;
import com.limachi.dim_bag.menus.slots.TankSlot;
import com.limachi.lim_lib.network.messages.ScreenNBTMsg;
import com.limachi.lim_lib.registries.clientAnnotations.RegisterMenuScreen;
import com.limachi.lim_lib.render.GuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
@RegisterMenuScreen
public class BagScreen extends AbstractContainerScreen<BagMenu> {

    public static final Supplier<ItemStack> INVENTORY_ICON_BUILDER = ()->new ItemStack(BagItem.R_ITEM.get());
    public static final Supplier<ItemStack> SETTINGS_ICON_BUILDER = ()->{
        ItemStack out = new ItemStack(BagItem.R_ITEM.get());
        out.getOrCreateTag().putString(BagItem.BAG_MODE_OVERRIDE, "Settings");
        return out;
    };

    protected final ItemStack inventoryIcon;
    protected final ItemStack settingsIcon;

    public static final ResourceLocation BACKGROUND = new ResourceLocation(DimBag.MOD_ID, "textures/screen/bag_inventory.png");
    public static final int BACKGROUND_WIDTH = 218;
    public static final int BACKGROUND_HEIGHT = 184;

    protected VerticalSlider slider;

    public BagScreen(BagMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = BACKGROUND_WIDTH;
        imageHeight = BACKGROUND_HEIGHT;
        titleLabelX = 30;
        inventoryLabelX = 30;
        inventoryLabelY = 90;
        inventoryIcon = INVENTORY_ICON_BUILDER.get();
        settingsIcon = SETTINGS_ICON_BUILDER.get();
    }

    @Override
    protected void init() {
        super.init();
        slider = new VerticalSlider(195 + getGuiLeft(), 16 + getGuiTop(), 16, 103, 0., this::sliderUpdate);
        addRenderableWidget(slider);
    }

    @Override
    public void renderSlot(@Nonnull GuiGraphics gui, @Nonnull Slot slot) {
        if (slot instanceof TankSlot tank)
            tank.renderSlot(gui);
        else
            super.renderSlot(gui, slot);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double fromX, double fromY) {
        return ((getFocused() != null && isDragging() && button == 0) && getFocused().mouseDragged(mouseX, mouseY, button, fromX, fromY)) || super.mouseDragged(mouseX, mouseY, button, fromX, fromY);
    }

    protected void sliderUpdate(VerticalSlider slider) {
        CompoundTag out = new CompoundTag();
        out.putDouble("scroll", slider.getValue());
        ScreenNBTMsg.send(0, out);
    }

    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        gui.blit(BACKGROUND, getGuiLeft(), getGuiTop(), 0f, 0F, imageWidth, imageHeight, 256, 256);
        if (menu.page.get() == 0) {
            gui.blit(BACKGROUND, getGuiLeft(), getGuiTop() + 157, 0, imageHeight, 24, 23, 256, 256);
            for (Slot slot : menu.slots)
                if (slot instanceof BagSlot && !slot.isActive())
                    GuiUtils.slots(gui, slot.x - 1, slot.y - 1, 1, 1, true);
        } else if (menu.page.get() == 1) {
            gui.blit(BACKGROUND, getGuiLeft(), getGuiTop() + 131, 0, imageHeight, 24, 23, 256, 256);
            for (Slot slot : menu.slots)
                if (slot instanceof TankSlot && !slot.isActive())
                    GuiUtils.slots(gui, slot.x - 1, slot.y - 1, 1, 1, true);
        }
        else if (menu.page.get() == 2) {
            gui.blit(BACKGROUND, getGuiLeft() + 194, getGuiTop() + 157, 194, imageHeight, 24, 23, 256, 256);
            gui.blit(BACKGROUND, getGuiLeft() + 28, getGuiTop() + 16, 28, 184, 162, 72, 256, 256);
        }
        else if (menu.page.get() == 3) {
            gui.blit(BACKGROUND, getGuiLeft() + 194, getGuiTop() + 131, 194, imageHeight, 24, 23, 256, 256);
            gui.blit(BACKGROUND, getGuiLeft() + 28, getGuiTop() + 16, 28, 184, 162, 72, 256, 256);
        }
        gui.renderItem(inventoryIcon, getGuiLeft() + 4, getGuiTop() + 161);
        gui.renderItem(settingsIcon, getGuiLeft() + 4, getGuiTop() + 135);
        gui.renderItem(inventoryIcon, getGuiLeft() + 198, getGuiTop() + 161);
        gui.renderItem(settingsIcon, getGuiLeft() + 198, getGuiTop() + 135);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = mouseX - getGuiLeft();
        double y = mouseY - getGuiTop();
        if (y >= 131 && y <= 179) {
            if (x >= 0 && x <= 21) {
                if (y <= 154)
                    menu.page.set(1);
                if (y >= 157)
                    menu.page.set(0);
                CompoundTag out = new CompoundTag();
                out.putInt("page", menu.page.get());
                slider.setValue(0.);
                ScreenNBTMsg.send(0, out);
                return true;
            } else if (x >= 198 && x <= 218) {
                if (y <= 154)
                    menu.page.set(3);
                if (y >= 157)
                    menu.page.set(2);
                CompoundTag out = new CompoundTag();
                out.putInt("page", menu.page.get());
                slider.setValue(0.);
                ScreenNBTMsg.send(0, out);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
