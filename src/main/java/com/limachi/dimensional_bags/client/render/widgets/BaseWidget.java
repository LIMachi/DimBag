package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.limachi.dimensional_bags.client.render.screen.SimpleContainerScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;
import static com.limachi.dimensional_bags.client.render.TextureCutout.HOVERED;
import static com.limachi.dimensional_bags.client.render.TextureCutout.SELECTED;
import static org.lwjgl.opengl.GL11.glScissor;

public class BaseWidget extends Widget {
    public static final ResourceLocation DEFAULT_BACKGROUND = new ResourceLocation(MOD_ID, "textures/widgets/backgrounds.png");
    public static final TextureCutout IDLE_TEXTURE = new TextureCutout(DEFAULT_BACKGROUND, 0, 0, 128, 128);
    public static final TextureCutout HOVERED_TEXTURE = new TextureCutout(DEFAULT_BACKGROUND, 0, 128, 128, 128);
    public static final TextureCutout SELECTED_TEXTURE = new TextureCutout(DEFAULT_BACKGROUND, 128, 0, 128, 128);
    public static final TextureCutout HOVERED_SELECTED_TEXTURE = new TextureCutout(DEFAULT_BACKGROUND, 128, 128, 128, 128);
    public static final TextureCutout DISABLED_TEXTURE = SELECTED_TEXTURE;
    public static final TextureCutout[] STATE_ARRAY = {IDLE_TEXTURE, SELECTED_TEXTURE, HOVERED_TEXTURE, HOVERED_SELECTED_TEXTURE};
    public static final Minecraft MINECRAFT = Minecraft.getInstance();
    protected boolean isSelected = false;
    protected boolean renderTitle = true;
    protected boolean renderStandardBackground = true;
    protected boolean isToggle = false;
    protected int drag = -1;
    protected boolean enableClickBehavior = true;
    protected BaseParentWidget parent = null;
    protected boolean wasHovered = false;
    private SimpleContainerScreen<?> screen = null;

    public BaseWidget(int x, int y, int width, int height, ITextComponent title) { super(x, y, width, height, title); }

    public BaseWidget(int x, int y, int width, int height) { super(x, y, width, height, StringTextComponent.EMPTY); }

    public void attachToScreen(SimpleContainerScreen<?> screen) { this.screen = screen; }
    public void detachFromScreen() { screen = null; }
    public SimpleContainerScreen<?> getScreen() { return screen; }

    public boolean changeFocus(boolean focus) {
        if (this.active && this.visible && focus != isFocused()) {
            setFocused(focus);
            if (screen != null) {
                if (focus) {
                    screen.children().forEach(b -> {
                        if (b == this || !(b instanceof Widget) || !((Widget) b).isFocused()) return;
                        b.changeFocus(false);
                    });
                    screen.setFocused(this);
                } else if (screen.getFocused() == this)
                    screen.setFocused(null);
            }
            onFocusedChanged(isFocused());
            return focus;
        } else {
            return false;
        }
    }

    @Override
    public boolean isHovered() { return isHovered; }

    public boolean isHoveredOrFocus() { return isHovered || isFocused(); }

    public int renderState() {
        return (isHoveredOrFocus() ? HOVERED : 0) + (isSelected || drag != -1 ? SELECTED : 0);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (visible && (parent == null || parent.isRenderingChildren)) {
            isHovered = isMouseOver(mouseX, mouseY);
            if (wasHovered != isHovered()) {
                if (isHovered()) {
                    if (isFocused()) {
                        queueNarration(200);
                    } else {
                        queueNarration(750);
                    }
                } else {
                    this.nextNarration = Long.MAX_VALUE;
                }
            }

            if (visible) {
                if (parent == null || !parent.isRenderingChildren) {
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.enableDepthTest();
                }
                if (renderStandardBackground)
                    renderStandardBackground(matrixStack, partialTicks, x, y, width, height, active, renderState());
                renderBg(matrixStack, MINECRAFT, mouseX, mouseY);
                if (renderTitle)
                    drawCenteredString(matrixStack, MINECRAFT.font, getMessage(), x + width / 2, y + (height - 8) / 2, getFGColor() | MathHelper.ceil(alpha * 255.0F) << 24);
                renderButton(matrixStack, mouseX, mouseY, partialTicks);
            }

            narrate();
            wasHovered = isHovered();
        }
    }

    public static void renderStandardBackground(MatrixStack matrixStack, float partialTicks, int x, int y, int width, int height, boolean active, int state) {
        (active ? STATE_ARRAY[state] : DISABLED_TEXTURE).blit(matrixStack, new Box2d(x, y, width, height), 0, TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION);
    }

    public void init() {}
    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {}

    public boolean isSelected() { return isSelected; }

    public <T extends BaseWidget> T setSelected(boolean state) { isSelected = state; return (T)this; }

    public <T extends BaseWidget> T enableRenderTitle(boolean state) { renderTitle = state; return (T)this; }

    public boolean renderTitle() { return renderTitle; }

    public <T extends BaseWidget> T enableRenderStandardBackground(boolean state) { renderStandardBackground = state; return (T)this; }

    public boolean renderStandardBackground() { return renderStandardBackground; }

    public <T extends BaseWidget> T enableToggleBehavior(boolean state) { isToggle = state; return (T)this; }

    public boolean isToggle() { return isToggle; }

    public <T extends BaseWidget> T enableClickBehavior(boolean state) { enableClickBehavior = state; return (T)this; }

    public boolean isClickable() { return enableClickBehavior; }

    public <T extends BaseWidget> T setParent(BaseParentWidget parent) { this.parent = parent; return (T)this; }

    public BaseParentWidget getParent() { return parent; }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (active && visible && enableClickBehavior && isValidClickButton(button) && clicked(mouseX, mouseY)) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            isSelected = !isToggle || !isSelected;
            changeFocus(true);
            onClick(mouseX, mouseY);
            if (!isToggle)
                isSelected = false;
            drag = button;
            return true;
        }
        changeFocus(false);
        return false;
    }

    public void scissor(int x, int y, int w, int h) {
        double factor = MINECRAFT.getWindow().getGuiScale();
        glScissor((int)(x * factor), (int)(MINECRAFT.getWindow().getScreenHeight() - (y + h) * factor), (int)(w * factor), (int)(h * factor));
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        drag = -1;
        if (enableClickBehavior && isValidClickButton(button)) {
            onRelease(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return active && visible && mouseX >= (double)x && mouseY >= (double)y && mouseX < (double)(x + width) && mouseY < (double)(y + height);
    }

    @Override
    public boolean clicked(double mouseX, double mouseY) { return isMouseOver(mouseX, mouseY); }
}
