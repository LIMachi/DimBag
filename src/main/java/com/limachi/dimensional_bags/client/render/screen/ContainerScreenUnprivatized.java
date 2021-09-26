package com.limachi.dimensional_bags.client.render.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * reimplement all functions and variables that where declared private in ContainerScreen, so we can manipulate them in screen extending this
 */
public abstract class ContainerScreenUnprivatized<T extends Container> extends ContainerScreen<T> {
    @Nullable
    protected Slot clickedSlot;
    @Nullable
    protected Slot snapbackEnd;
    @Nullable
    protected Slot quickdropSlot;
    @Nullable
    protected Slot lastClickSlot;

    protected boolean isSplittingStack;
    protected ItemStack draggingItem = ItemStack.EMPTY;
    protected int snapbackStartX;
    protected int snapbackStartY;
    protected long snapbackTime;
    protected ItemStack snapbackItem = ItemStack.EMPTY;
    protected long quickdropTime;

    protected int quickCraftingType;
    protected int quickCraftingButton;
    protected boolean skipNextRelease;
    protected int quickCraftingRemainder;
    protected long lastClickTime;
    protected int lastClickButton;
    protected boolean doubleclick;
    protected ItemStack lastQuickMoved = ItemStack.EMPTY;

    public ContainerScreenUnprivatized(T container, PlayerInventory playerInv, ITextComponent title) {
        super(container, playerInv, title);
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(matrixStack, partialTick, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiContainerEvent.DrawBackground(this, matrixStack, mouseX, mouseY));
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
//        super.render(matrixStack, mouseX, mouseY, partialTick);
        for(int t = 0; t < this.buttons.size(); ++t) {
            this.buttons.get(t).render(matrixStack, mouseX, mouseY, partialTick);
        }
//
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)i, (float)j, 0.0F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableRescaleNormal();
        this.hoveredSlot = null;
        int k = 240;
        int l = 240;
        RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        for(int i1 = 0; i1 < this.menu.slots.size(); ++i1) {
            Slot slot = this.menu.slots.get(i1);
            if (slot instanceof IRenderable) {
                ((IRenderable)slot).render(matrixStack, mouseX, mouseY, partialTick);
            } else if (slot.isActive()) {
                this.renderSlot(matrixStack, slot);
            }

            if (this.isHovering(slot, (double)mouseX, (double)mouseY) && slot.isActive()) {
                this.hoveredSlot = slot;
                RenderSystem.disableDepthTest();
                int j1 = slot.x;
                int k1 = slot.y;
                RenderSystem.colorMask(true, true, true, false);
                int slotColor = this.getSlotColor(i1);
                this.fillGradient(matrixStack, j1, k1, j1 + 16, k1 + 16, slotColor, slotColor);
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.enableDepthTest();
            }
        }

        this.renderLabels(matrixStack, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiContainerEvent.DrawForeground(this, matrixStack, mouseX, mouseY));
        PlayerInventory playerinventory = this.minecraft.player.inventory;
        ItemStack itemstack = this.draggingItem.isEmpty() ? playerinventory.getCarried() : this.draggingItem;
        if (!itemstack.isEmpty()) {
            int j2 = 8;
            int k2 = this.draggingItem.isEmpty() ? 8 : 16;
            String s = null;
            if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
                itemstack = itemstack.copy();
                itemstack.setCount(MathHelper.ceil((float)itemstack.getCount() / 2.0F));
            } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
                itemstack = itemstack.copy();
                itemstack.setCount(this.quickCraftingRemainder);
                if (itemstack.isEmpty()) {
                    s = "" + TextFormatting.YELLOW + "0";
                }
            }

            this.renderFloatingItem(itemstack, mouseX - i - 8, mouseY - j - k2, s);
        }

        if (!this.snapbackItem.isEmpty()) {
            float f = (float)(Util.getMillis() - this.snapbackTime) / 100.0F;
            if (f >= 1.0F) {
                f = 1.0F;
                this.snapbackItem = ItemStack.EMPTY;
            }

            int l2 = this.snapbackEnd.x - this.snapbackStartX;
            int i3 = this.snapbackEnd.y - this.snapbackStartY;
            int l1 = this.snapbackStartX + (int)((float)l2 * f);
            int i2 = this.snapbackStartY + (int)((float)i3 * f);
            this.renderFloatingItem(this.snapbackItem, l1, i2, (String)null);
        }

        RenderSystem.popMatrix();
        RenderSystem.enableDepthTest();
    }

    protected boolean defaultMouseClicked(double mouseX, double mouseY, int button) {
        for(IGuiEventListener iguieventlistener : this.children()) {
            if (iguieventlistener.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(iguieventlistener);
                if (button == 0) {
                    this.setDragging(true);
                }

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (defaultMouseClicked(mouseX, mouseY, button)) {
            return true;
        } else {
            InputMappings.Input mouseKey = InputMappings.Type.MOUSE.getOrCreate(button);
            boolean flag = this.minecraft.options.keyPickItem.isActiveAndMatches(mouseKey);
            Slot slot = this.findSlot(mouseX, mouseY);
            long i = Util.getMillis();
            this.doubleclick = this.lastClickSlot == slot && i - this.lastClickTime < 250L && this.lastClickButton == button;
            this.skipNextRelease = false;
            if (button != 0 && button != 1 && !flag) {
                this.checkHotbarMouseClicked(button);
            } else {
                int j = this.leftPos;
                int k = this.topPos;
                boolean flag1 = this.hasClickedOutside(mouseX, mouseY, j, k, button);
                if (slot != null) flag1 = false; // Forge, prevent dropping of items through slots outside of GUI boundaries
                int l = -1;
                if (slot != null) {
                    l = slot.index;
                }

                if (flag1) {
                    l = -999;
                }

                if (this.minecraft.options.touchscreen && flag1 && this.minecraft.player.inventory.getCarried().isEmpty()) {
                    this.minecraft.setScreen((Screen)null);
                    return true;
                }

                if (l != -1) {
                    if (this.minecraft.options.touchscreen) {
                        if (slot != null && slot.hasItem()) {
                            this.clickedSlot = slot;
                            this.draggingItem = ItemStack.EMPTY;
                            this.isSplittingStack = button == 1;
                        } else {
                            this.clickedSlot = null;
                        }
                    } else if (!this.isQuickCrafting) {
                        if (this.minecraft.player.inventory.getCarried().isEmpty()) {
                            if (this.minecraft.options.keyPickItem.isActiveAndMatches(mouseKey)) {
                                this.slotClicked(slot, l, button, ClickType.CLONE);
                            } else {
                                boolean flag2 = l != -999 && (InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
                                ClickType clicktype = ClickType.PICKUP;
                                if (flag2) {
                                    this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                                    clicktype = ClickType.QUICK_MOVE;
                                } else if (l == -999) {
                                    clicktype = ClickType.THROW;
                                }

                                this.slotClicked(slot, l, button, clicktype);
                            }

                            this.skipNextRelease = true;
                        } else {
                            this.isQuickCrafting = true;
                            this.quickCraftingButton = button;
                            this.quickCraftSlots.clear();
                            if (button == 0) {
                                this.quickCraftingType = 0;
                            } else if (button == 1) {
                                this.quickCraftingType = 1;
                            } else if (this.minecraft.options.keyPickItem.isActiveAndMatches(mouseKey)) {
                                this.quickCraftingType = 2;
                            }
                        }
                    }
                }
            }

            this.lastClickSlot = slot;
            this.lastClickTime = i;
            this.lastClickButton = button;
            return true;
        }
    }

    protected boolean defaultMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.getFocused() != null && this.isDragging() && button == 0 ? this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY) : false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Slot slot = this.findSlot(mouseX, mouseY);
        ItemStack itemstack = this.minecraft.player.inventory.getCarried();
        if (this.clickedSlot != null && this.minecraft.options.touchscreen) {
            if (button == 0 || button == 1) {
                if (this.draggingItem.isEmpty()) {
                    if (slot != this.clickedSlot && !this.clickedSlot.getItem().isEmpty()) {
                        this.draggingItem = this.clickedSlot.getItem().copy();
                    }
                } else if (this.draggingItem.getCount() > 1 && slot != null && Container.canItemQuickReplace(slot, this.draggingItem, false)) {
                    long i = Util.getMillis();
                    if (this.quickdropSlot == slot) {
                        if (i - this.quickdropTime > 500L) {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                            this.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                            this.quickdropTime = i + 750L;
                            this.draggingItem.shrink(1);
                        }
                    } else {
                        this.quickdropSlot = slot;
                        this.quickdropTime = i;
                    }
                }
            }
        } else if (this.isQuickCrafting && slot != null && !itemstack.isEmpty() && (itemstack.getCount() > this.quickCraftSlots.size() || this.quickCraftingType == 2) && Container.canItemQuickReplace(slot, itemstack, true) && slot.mayPlace(itemstack) && this.menu.canDragTo(slot)) {
            this.quickCraftSlots.add(slot);
            this.recalculateQuickCraftRemaining();
        }

        return true;
    }

    protected boolean defaultMouseReleased(double mouseX, double mouseY, int button) {
        this.setDragging(false);
        return this.getChildAt(mouseX, mouseY).filter((child) -> {
            return child.mouseReleased(mouseX, mouseY, button);
        }).isPresent();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
//        super.mouseReleased(mouseX, mouseY, button); //Forge, Call parent to release buttons
        defaultMouseReleased(mouseX, mouseY, button);
        Slot slot = this.findSlot(mouseX, mouseY);
        int i = this.leftPos;
        int j = this.topPos;
        boolean flag = this.hasClickedOutside(mouseX, mouseY, i, j, button);
        if (slot != null) flag = false; // Forge, prevent dropping of items through slots outside of GUI boundaries
        InputMappings.Input mouseKey = InputMappings.Type.MOUSE.getOrCreate(button);
        int k = -1;
        if (slot != null) {
            k = slot.index;
        }

        if (flag) {
            k = -999;
        }

        if (this.doubleclick && slot != null && button == 0 && this.menu.canTakeItemForPickAll(ItemStack.EMPTY, slot)) {
            if (hasShiftDown()) {
                if (!this.lastQuickMoved.isEmpty()) {
                    for(Slot slot2 : this.menu.slots) {
                        if (slot2 != null && slot2.mayPickup(this.minecraft.player) && slot2.hasItem() && slot2.isSameInventory(slot) && Container.canItemQuickReplace(slot2, this.lastQuickMoved, true)) {
                            this.slotClicked(slot2, slot2.index, button, ClickType.QUICK_MOVE);
                        }
                    }
                }
            } else {
                this.slotClicked(slot, k, button, ClickType.PICKUP_ALL);
            }

            this.doubleclick = false;
            this.lastClickTime = 0L;
        } else {
            if (this.isQuickCrafting && this.quickCraftingButton != button) {
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                this.skipNextRelease = true;
                return true;
            }

            if (this.skipNextRelease) {
                this.skipNextRelease = false;
                return true;
            }

            if (this.clickedSlot != null && this.minecraft.options.touchscreen) {
                if (button == 0 || button == 1) {
                    if (this.draggingItem.isEmpty() && slot != this.clickedSlot) {
                        this.draggingItem = this.clickedSlot.getItem();
                    }

                    boolean flag2 = Container.canItemQuickReplace(slot, this.draggingItem, false);
                    if (k != -1 && !this.draggingItem.isEmpty() && flag2) {
                        this.slotClicked(this.clickedSlot, this.clickedSlot.index, button, ClickType.PICKUP);
                        this.slotClicked(slot, k, 0, ClickType.PICKUP);
                        if (this.minecraft.player.inventory.getCarried().isEmpty()) {
                            this.snapbackItem = ItemStack.EMPTY;
                        } else {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, button, ClickType.PICKUP);
                            this.snapbackStartX = MathHelper.floor(mouseX - (double)i);
                            this.snapbackStartY = MathHelper.floor(mouseY - (double)j);
                            this.snapbackEnd = this.clickedSlot;
                            this.snapbackItem = this.draggingItem;
                            this.snapbackTime = Util.getMillis();
                        }
                    } else if (!this.draggingItem.isEmpty()) {
                        this.snapbackStartX = MathHelper.floor(mouseX - (double)i);
                        this.snapbackStartY = MathHelper.floor(mouseY - (double)j);
                        this.snapbackEnd = this.clickedSlot;
                        this.snapbackItem = this.draggingItem;
                        this.snapbackTime = Util.getMillis();
                    }

                    this.draggingItem = ItemStack.EMPTY;
                    this.clickedSlot = null;
                }
            } else if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty()) {
                this.slotClicked((Slot)null, -999, Container.getQuickcraftMask(0, this.quickCraftingType), ClickType.QUICK_CRAFT);

                for(Slot slot1 : this.quickCraftSlots) {
                    this.slotClicked(slot1, slot1.index, Container.getQuickcraftMask(1, this.quickCraftingType), ClickType.QUICK_CRAFT);
                }

                this.slotClicked((Slot)null, -999, Container.getQuickcraftMask(2, this.quickCraftingType), ClickType.QUICK_CRAFT);
            } else if (!this.minecraft.player.inventory.getCarried().isEmpty()) {
                if (this.minecraft.options.keyPickItem.isActiveAndMatches(mouseKey)) {
                    this.slotClicked(slot, k, button, ClickType.CLONE);
                } else {
                    boolean flag1 = k != -999 && (InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
                    if (flag1) {
                        this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                    }

                    this.slotClicked(slot, k, button, flag1 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
                }
            }
        }

        if (this.minecraft.player.inventory.getCarried().isEmpty()) {
            this.lastClickTime = 0L;
        }

        this.isQuickCrafting = false;
        return true;
    }

    protected void renderFloatingItem(ItemStack stack, int posX, int posY, String quantity) {
        RenderSystem.translatef(0.0F, 0.0F, 32.0F);
        this.setBlitOffset(200);
        this.itemRenderer.blitOffset = 200.0F;
        net.minecraft.client.gui.FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = this.font;
        this.itemRenderer.renderAndDecorateItem(stack, posX, posY);
        this.itemRenderer.renderGuiItemDecorations(font, stack, posX, posY - (this.draggingItem.isEmpty() ? 0 : 8), quantity);
        this.setBlitOffset(0);
        this.itemRenderer.blitOffset = 0.0F;
    }

    protected void renderSlot(MatrixStack matrixStack, Slot slot) {
        int i = slot.x;
        int j = slot.y;
        ItemStack itemstack = slot.getItem();
        boolean flag = false;
        boolean flag1 = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack itemstack1 = this.minecraft.player.inventory.getCarried();
        String s = null;
        if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemstack.isEmpty()) {
            itemstack = itemstack.copy();
            itemstack.setCount(itemstack.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !itemstack1.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }

            if (Container.canItemQuickReplace(slot, itemstack1, true) && this.menu.canDragTo(slot)) {
                itemstack = itemstack1.copy();
                flag = true;
                Container.getQuickCraftSlotCount(this.quickCraftSlots, this.quickCraftingType, itemstack, slot.getItem().isEmpty() ? 0 : slot.getItem().getCount());
                int k = Math.min(itemstack.getMaxStackSize(), slot.getMaxStackSize(itemstack));
                if (itemstack.getCount() > k) {
                    s = TextFormatting.YELLOW.toString() + k;
                    itemstack.setCount(k);
                }
            } else {
                this.quickCraftSlots.remove(slot);
                this.recalculateQuickCraftRemaining();
            }
        }

        this.setBlitOffset(100);
        this.itemRenderer.blitOffset = 100.0F;
        if (itemstack.isEmpty() && slot.isActive()) {
            Pair<ResourceLocation, ResourceLocation> pair = slot.getNoItemIcon();
            if (pair != null) {
                TextureAtlasSprite textureatlassprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                this.minecraft.getTextureManager().bind(textureatlassprite.atlas().location());
                blit(matrixStack, i, j, this.getBlitOffset(), 16, 16, textureatlassprite);
                flag1 = true;
            }
        }

        if (!flag1) {
            if (flag) {
                fill(matrixStack, i, j, i + 16, j + 16, -2130706433);
            }

            RenderSystem.enableDepthTest();
            this.itemRenderer.renderAndDecorateItem(this.minecraft.player, itemstack, i, j);
            this.itemRenderer.renderGuiItemDecorations(this.font, itemstack, i, j, s);
        }

        this.itemRenderer.blitOffset = 0.0F;
        this.setBlitOffset(0);
    }

    protected void recalculateQuickCraftRemaining() {
        ItemStack itemstack = this.minecraft.player.inventory.getCarried();
        if (!itemstack.isEmpty() && this.isQuickCrafting) {
            if (this.quickCraftingType == 2) {
                this.quickCraftingRemainder = itemstack.getMaxStackSize();
            } else {
                this.quickCraftingRemainder = itemstack.getCount();

                for(Slot slot : this.quickCraftSlots) {
                    ItemStack itemstack1 = itemstack.copy();
                    ItemStack itemstack2 = slot.getItem();
                    int i = itemstack2.isEmpty() ? 0 : itemstack2.getCount();
                    Container.getQuickCraftSlotCount(this.quickCraftSlots, this.quickCraftingType, itemstack1, i);
                    int j = Math.min(itemstack1.getMaxStackSize(), slot.getMaxStackSize(itemstack1));
                    if (itemstack1.getCount() > j) {
                        itemstack1.setCount(j);
                    }

                    this.quickCraftingRemainder -= itemstack1.getCount() - i;
                }

            }
        }
    }

    @Nullable
    protected Slot findSlot(double mouseX, double mouseY) {
        for(int i = 0; i < this.menu.slots.size(); ++i) {
            Slot slot = this.menu.slots.get(i);
            if (this.isHovering(slot, mouseX, mouseY) && slot.isActive()) {
                return slot;
            }
        }

        return null;
    }

    protected void checkHotbarMouseClicked(int button) {
        if (this.hoveredSlot != null && this.minecraft.player.inventory.getCarried().isEmpty()) {
            if (this.minecraft.options.keySwapOffhand.matchesMouse(button)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
                return;
            }

            for(int i = 0; i < 9; ++i) {
                if (this.minecraft.options.keyHotbarSlots[i].matchesMouse(button)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, i, ClickType.SWAP);
                }
            }
        }

    }

    protected boolean isHovering(Slot slot, double mouseX, double mouseY) {
        return this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY);
    }
}
