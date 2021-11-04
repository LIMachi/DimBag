package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.limachi.dimensional_bags.client.render.widgets.BaseWidget;
import com.limachi.dimensional_bags.client.render.widgets.CenteredBackgroundWidget;
import com.limachi.dimensional_bags.common.container.BaseContainer;
import com.limachi.dimensional_bags.common.items.FluidItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.IScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;
import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

public class SimpleContainerScreen<C extends BaseContainer<C>> extends /*DisplayEffectsScreen<C>*//*ContainerScreenUnprivatized<C>*/ContainerScreen<C> {

    boolean dynamicBackground = true;

    public CenteredBackgroundWidget background = new CenteredBackgroundWidget();

    public Stack<int[]> scissors = new Stack<>();
    public int ticks = 0;

    protected boolean hideTitle = false;
    protected boolean hideContainerTitle = false;

    protected int mouseX;
    protected int mouseY;
    protected float partialTick;

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTick = partialTick;
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTick);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    protected void renderTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (this.minecraft.player.inventory.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            this.renderTooltip(matrixStack, this.hoveredSlot.getItem(), mouseX, mouseY);
        }
        else {
            for (Widget w : buttons)
                if (w.isHovered())
                    w.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }

    public List<? extends Widget> getButtons() { return buttons; }

    public FontRenderer getFont() { return font; }

    /**
     * dirty fix for widgets that change their focus without notifying it to the screen
     * @return
     */
    @Nullable
    @Override
    public IGuiEventListener getFocused() {
        IGuiEventListener f = super.getFocused();
        if (f instanceof Widget && !((Widget)f).isFocused()) {
            f = null;
            setFocused(null);
        }
        return f;
    }

    @Override
    public void renderSlot(MatrixStack matrixStack, Slot slot) {
        if (slot instanceof IRenderable)
            ((IRenderable)slot).render(matrixStack, mouseX, mouseY, partialTick);
        else
            super.renderSlot(matrixStack, slot);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !(getFocused() instanceof BaseWidget && ((BaseWidget)getFocused()).consumeEscKey());
    }

    public void scissor(MatrixStack matrixStack, double x1, double y1, double x2, double y2) {
        if (scissors.empty())
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        if (x1 > x2) {
            double x = x1;
            x1 = x2;
            x2 = x;
        }
        if (y1 > y2) {
            double y = y1;
            y1 = y2;
            y2 = y;
        }
        Vector4f v1 = new Vector4f((float)x1, (float)y1, 0, 1);
        Vector4f v2 = new Vector4f((float)x2, (float)y2, 0, 1);
        v1.transform(matrixStack.last().pose());
        v2.transform(matrixStack.last().pose());
        double factor = Minecraft.getInstance().getWindow().getGuiScale();
        int x = (int)(v1.x() * factor);
        int y = (int)(Minecraft.getInstance().getWindow().getScreenHeight() - v2.y() * factor);
        int [] entry = {x, y, x + (int)((v2.x() - v1.x()) * factor), y + (int)((v2.y() - v1.y()) * factor)};
        if (!scissors.empty()) {
            int [] t = scissors.peek();
            entry[0] = Math.max(entry[0], t[0]);
            entry[1] = Math.max(entry[1], t[1]);
            entry[2] = Math.min(entry[2], t[2]);
            entry[3] = Math.min(entry[3], t[3]);
        }
        scissors.push(entry);
        if (entry[2] - entry[0] <= 0 || entry[3] - entry[1] <= 0)
            GL11.glScissor(0, 0, 0, 0);
        else
            GL11.glScissor(entry[0], entry[1], entry[2] - entry[0], entry[3] - entry[1]);
    }

    public void removeScissor() {
        scissors.pop();
        if (scissors.empty())
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private static final HashMap<Class<?>, Consumer<Object>> TICK_INTERFACES = new HashMap<>();
    static {
        TICK_INTERFACES.put(TextFieldWidget.class, c->((TextFieldWidget)c).tick());
        TICK_INTERFACES.put(IScreen.class, c->((IScreen)c).tick());
        TICK_INTERFACES.put(ITickable.class, c->((ITickable)c).tick());
    }

    @Override
    public void tick() {
        super.tick();
        for (IGuiEventListener child : children) {
            boolean hasTicked = false;
            for (Class<?> clazz : TICK_INTERFACES.keySet())
                if (!hasTicked && clazz.isInstance(child)) {
                    TICK_INTERFACES.get(clazz).accept(child);
                    hasTicked = true;
                }
        }
//        for (BaseContainerWidget<C> widget : menu.widgets)
//            widget.tick(ticks);
//        menu.detectAndSendWidgetChanges();
        ++ticks;
    }

    public SimpleContainerScreen(C screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
//        screenContainer.screen = this;
        itemRenderer = SimpleItemRenderer.INSTANCE;
//        if (!screenContainer.slots.isEmpty())
//            background.addChild(new SlotArrayWidget(0, 0, screenContainer.slots));
    }

    @Override
    protected void init() {
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
        super.init();
        if (dynamicBackground)
            calculateBackGround();
        addButton(background).init();
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    protected void calculateBackGround() {
        Box2d sc = new Box2d(-1, -1, -1, -1);
        Box2d sp = new Box2d(-1, -1, -1, -1);
        int spacer = 5; //how many pixels should be added for the border
        int stringSpacer = font.lineHeight + 4; //how tall a string should be
        int stringDelta = 2; //by how much the string position should be moved down from the spacer
        titleLabelY = 0; //open container title, calculated relative to the highest slot not targeting a PlayerInventory
        titleLabelX = 2 + spacer;
        inventoryLabelY = 0; //"Inventory" most of the time, calculated relative to the highest slot targeting a PlayerInventory
        inventoryLabelX = 2 + spacer;
        for (Slot slot : menu.slots)
            if (slot.container instanceof PlayerInventory) {
                if (sp.getX1() == -1)
                    sp.setX1(slot.x).setY1(slot.y).setWidth(SLOT_SIZE_X).setHeight(SLOT_SIZE_Y);
                else {
                    sp.expandToContain(slot.x, slot.y);
                    sp.expandToContain(slot.x + SLOT_SIZE_X, slot.y + SLOT_SIZE_Y);
                }
            } else {
                if (sc.getX1() == -1)
                    sc.setX1(slot.x).setY1(slot.y).setWidth(SLOT_SIZE_X).setHeight(SLOT_SIZE_Y);
                else {
                    sc.expandToContain(slot.x, slot.y);
                    sc.expandToContain(slot.x + SLOT_SIZE_X, slot.y + SLOT_SIZE_Y);
                }
            }
        Box2d sw = new Box2d(-1, -1, -1, -1);
        for (Widget widget : buttons)
            if (sw.getX1() == -1)
                sp.setX1(widget.x).setY1(widget.y).setWidth(widget.getWidth()).setHeight(widget.getHeight());
            else {
                sp.expandToContain(widget.x, widget.y);
                sp.expandToContain(widget.x + widget.getWidth(), widget.y + widget.getHeight());
            }
        if (sp.getX1() == -1) hideContainerTitle = true;
        Box2d sf = null;
        if (sc.getX1() != -1) sf = sc;
        if (sp.getX1() != -1) {
            if (sf == null)
                sf = sp;
            else
                sf.expandToContain(sp, 0, 0);
        }
        if (sw.getX1() != -1) {
            if (sf == null)
                sf = sw;
            else
                sf.expandToContain(sw, 0, 0);
        }
        if (sf == null)
            sf = new Box2d(0, 0, imageWidth - 2 * spacer, imageHeight - 2 * spacer);
        if (!hideTitle)
            sf.expand(0, stringSpacer);
        sf.expand(spacer * 2, spacer * 2);
        if (!hideTitle && sc.getX1() != -1)
            titleLabelY = (int)sc.getY1() - stringSpacer + stringDelta;
        if (!hideContainerTitle)
            inventoryLabelY = (int)sp.getY1() - stringSpacer + stringDelta;
        imageWidth = (int)sf.getWidth();
        imageHeight = (int)sf.getHeight();
    }

    //based on the container's slot, calculate the size and shape of the background (player part, container part, scroll part, tabs part, widgets part)
    /*protected void calculateBackGround() {
        int spacer = 8;
        playerBackGround = new Box2d(-1, 0, 0, 0);
        containerBackGround = new Box2d(-1, 0, 0, 0);

        for (Slot slot : menu.slots) {
            if (slot.container instanceof PlayerInventory) {
                if (playerBackGround.getX1() == -1)
                    playerBackGround.setX1(slot.x).setY1(slot.y);
                playerBackGround.expandToContain(slot.x - 1, slot.y - 1, spacer, spacer);
                playerBackGround.expandToContain(slot.x + SLOT_SIZE_X - 1, slot.y + SLOT_SIZE_Y - 1, spacer, spacer);
            } else {
                if (containerBackGround.getX1() == -1)
                    containerBackGround.setX1(slot.x).setY1(slot.y);
                containerBackGround.expandToContain(slot.x - 1, slot.y - 1, spacer, spacer + 3);
                containerBackGround.expandToContain(slot.x + SLOT_SIZE_X - 1, slot.y + SLOT_SIZE_Y - 1, spacer, spacer + 3);
            }
        }
        fullBackground = new Box2d(-1,0, 0, 0);
        if (playerBackGround.getX1() != -1) {
            inventoryLabelY = (int)playerBackGround.getY1() - 2;
            inventoryLabelX = (int)playerBackGround.getX1() + 10;
            playerBackGround.move(leftPos, topPos - 6).expand(0, 6);
            fullBackground = playerBackGround.copy();
        }
        if (containerBackGround.getX1() != -1) {
            titleLabelY = (int)containerBackGround.getY1() - 2;
            if (playerBackGround.getX1() == -1)
                titleLabelX = (int)containerBackGround.getX1() + 10;
            else
                titleLabelX = inventoryLabelX;
            containerBackGround.move(leftPos, topPos - 6).expand(0, 6);
            if (fullBackground.getX1() == -1)
                fullBackground = containerBackGround.copy();
            else
                fullBackground.expandToContain(containerBackGround, 0, 0);
        }
    }*/

    public static final TextureCutout SIMPLE_BACKGROUND = new TextureCutout(new ResourceLocation(MOD_ID, "textures/screens/vanilla/full_background.png"), 0, 0, 256, 256);

    private void defaultBackground(MatrixStack matrixStack, Box2d size) {
        SIMPLE_BACKGROUND.blit(matrixStack, size, getBlitOffset(), TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION);
    }

    private void blitGuiFull(MatrixStack matrixStack, int x, int y, int w, int h, int depth) {
//        this.blit(matrixStack, x + leftPos, y + topPos, 0, 0, w, h, w, h);
        this.blit(matrixStack, x + leftPos, y + topPos, depth, 0, 0, w, h, w, h);
    }

//    protected void renderPlayerBackground(MatrixStack matrixStack, TextureManager tm) {
//        RenderUtils.drawBox(matrixStack, playerBackGround, 0xFFFF0000, 0);
//    }
/*
    @Override
    public void renderSlot(MatrixStack matrixStack, Slot slot) {
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
    }*/

    @Override
    public ITextComponent getTitle() {
        if (hideTitle)
            return StringTextComponent.EMPTY;
        return super.getTitle();
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.font.draw(matrixStack, getTitle(), (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        if (!hideContainerTitle)
            this.font.draw(matrixStack, inventory.getDisplayName(), (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
    }
/*
    @Override
    protected void renderBg(MatrixStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {

    }*/

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableDepthTest();
        if (this.minecraft == null) return;
        TextureManager tm = this.minecraft.getTextureManager();
//        if (fullBackground == null)
//            fullBackground = new Box2d(getGuiLeft(), getGuiTop(), imageWidth, imageHeight);
//        if (fullBackground.getX1() != -1) {
//            defaultBackground(matrixStack, fullBackground);
            tm.bind(SLOT);
            boolean shouldResetTexture = false;
            for (Slot slot : menu.slots) {
                if (slot instanceof IRenderable) continue;
                if (!slot.isActive()) {
                    tm.bind(LOCKED_SLOT);
                    shouldResetTexture = true;
                }
                this.blitGuiFull(matrixStack, slot.x - 1, slot.y - 1, SLOT_SIZE_X, SLOT_SIZE_Y, 200);
                if (shouldResetTexture) {
                    tm.bind(SLOT);
                    shouldResetTexture = false;
                }
            }
//        }
    }

    @Override
    public <T extends Widget> T addButton(T button) {
        if (button == null) return null;
        if (button instanceof BaseWidget)
            ((BaseWidget)button).attachToScreen(this);
        return super.addButton(button);
    }

    public <T extends Widget> T addButton(T button, int relX, int relY) {
//        button.x = getGuiLeft() + relX;
//        button.y = getGuiTop() + relY;
        return addButton(button);
    }

    public <T extends Widget> void removeButton(T button) {
        if (button == null) return;
        if (button instanceof BaseWidget)
            ((BaseWidget)button).detachFromScreen();
        if (getFocused() == button)
            setFocused(null);
        buttons.remove(button);
        children.remove(button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        if (isDragging()) //add back the default INestedGuiEventHandler#mouseDragged behavior after the ContainerScreen#mouseDragged (since this one always returns true in container, it disable the draging from other widgets)
            return getFocused() != null && button == 0 && getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return true;
    }

    public static class SimpleItemRenderer extends ItemRenderer {
        public static final Minecraft mc = Minecraft.getInstance();
        public static final SimpleItemRenderer INSTANCE = new SimpleItemRenderer(mc.textureManager, mc.getModelManager(), mc.getItemColors());

        public SimpleItemRenderer(TextureManager textureManagerIn, ModelManager modelManagerIn, ItemColors itemColorsIn) {
            super(textureManagerIn, modelManagerIn, itemColorsIn);
        }

        @Override
        public void render(ItemStack itemStackIn, ItemCameraTransforms.TransformType transformTypeIn, boolean leftHand, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, IBakedModel modelIn) {
            if (itemStackIn.getItem() instanceof FluidItem) return; //skip fluid items, they will be rendered during the foreground rendering
            super.render(itemStackIn, transformTypeIn, leftHand, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, modelIn);
        }
    }
}
