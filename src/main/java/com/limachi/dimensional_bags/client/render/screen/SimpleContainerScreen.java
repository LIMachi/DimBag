package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.FluidStackRenderer;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.limachi.dimensional_bags.client.render.widgets.BaseWidget;
import com.limachi.dimensional_bags.common.container.BaseContainer;
import com.limachi.dimensional_bags.common.container.slot.FluidSlot;
import com.limachi.dimensional_bags.common.container.widgets.BaseContainerWidget;
import com.limachi.dimensional_bags.common.items.FluidItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.IScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Stack;
import java.util.function.Consumer;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;
import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

public class SimpleContainerScreen<C extends BaseContainer<C>> extends DisplayEffectsScreen<C> {

    Box2d playerBackGround;
    Box2d containerBackGround;
    Box2d fullBackground;

    public Stack<int[]> scissors = new Stack<>();
    public int ticks = 0;

    public FontRenderer getFont() { return font; }

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
        menu.detectAndSendWidgetChanges();
        ++ticks;
    }

    public SimpleContainerScreen(C screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        screenContainer.screen = this;
    }

    @Override
    protected void init() {
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
        super.init();
        this.itemRenderer = SimpleItemRenderer.INSTANCE;
        calculateBackGround();
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    //based on the container's slot, calculate the size and shape of the background (player part, container part, scroll part, tabs part, widgets part)
    protected void calculateBackGround() {
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
    }

    public static final TextureCutout SIMPLE_BACKGROUND = new TextureCutout(new ResourceLocation(MOD_ID, "textures/screens/vanilla/full_background.png"), 0, 0, 256, 256);

    private void defaultBackground(MatrixStack matrixStack, Box2d size) {
        SIMPLE_BACKGROUND.blit(matrixStack, size, getBlitOffset(), TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION);
    }

    private void blitGuiFull(MatrixStack matrixStack, int x, int y, int w, int h) {
        this.blit(matrixStack, x + leftPos, y + topPos, 0, 0, w, h, w, h);
    }

    protected void renderPlayerBackground(MatrixStack matrixStack, TextureManager tm) {
        RenderUtils.drawBox(matrixStack, playerBackGround, 0xFFFF0000, 0);

    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        for (Slot slot : menu.slots)
            if (slot instanceof FluidSlot) {
                FluidStack fluid = ((FluidSlot) slot).getFluid();
                FluidStackRenderer.INSTANCE.render(matrixStack, slot.x, slot.y, fluid);
                int amountInMB = fluid.getAmount();
                if (amountInMB > 0) {
                    String amount = amountInMB >= 1000 ? (amountInMB / 1000) + I18n.get("screen.fluid.bucket_acronym") : amountInMB + I18n.get("screen.fluid.milli_bucket_acronym");
                    RenderSystem.pushMatrix();
                    RenderSystem.translatef(slot.x, slot.y, 250);
                    if (amount.length() > 3) {
                        RenderSystem.scalef(0.5f, 0.5f, 1);
                        font.drawShadow(matrixStack, amount, 31 - font.width(amount), 23, 16777215);
                    } else
                        font.drawShadow(matrixStack, amount, 17 - font.width(amount), 9, 16777215);
                    RenderSystem.popMatrix();
                }
            }
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.minecraft == null) return;
        TextureManager tm = this.minecraft.getTextureManager();
        if (playerBackGround == null || containerBackGround == null)
            calculateBackGround();
        if (fullBackground.getX1() != -1) {
            defaultBackground(matrixStack, fullBackground);
            tm.bind(SLOT);
            boolean shouldResetTexture = false;
            for (Slot slot : menu.slots) {
                if (slot.isActive() && slot instanceof FluidSlot && ((FluidSlot) slot).isSelected() && menu.slots.size() > 1) {
                    tm.bind(SELECTED_FLUID_SLOT);
                    shouldResetTexture = true;
                }
                if (!slot.isActive()) {
                    tm.bind(LOCKED_SLOT);
                    shouldResetTexture = true;
                }
                this.blitGuiFull(matrixStack, slot.x - 1, slot.y - 1, SLOT_SIZE_X, SLOT_SIZE_Y);
                if (shouldResetTexture) {
                    tm.bind(SLOT);
                    shouldResetTexture = false;
                }
            }
        }
        if (containerBackGround.getX1() != -1)
            font.draw(matrixStack, title.getString(), (float)(fullBackground.getX1() + 9), (float)(fullBackground.getY1() + 6), 4210752);
        if (playerBackGround.getX1() != -1)
            font.draw(matrixStack, inventory.getDisplayName().getString(), (float)(playerBackGround.getX1() + 9), (float)(playerBackGround.getY1() + 3), 4210752);
    }

    @Override
    public <T extends Widget> T addButton(T button) {
        if (button == null) return null;
        if (button instanceof BaseWidget)
            ((BaseWidget)button).attachToScreen(this);
        return super.addButton(button);
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
        if (!isDragging()) //add back the default INestedGuiEventHandler#mouseDragged behavior after the ContainerScreen#mouseDragged (since this one always returns true in container, it disable the draging from other widgets)
            return getFocused() != null && isDragging() && button == 0 && getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
