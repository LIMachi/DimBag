package com.limachi.dim_bag.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.Consumer;

import static com.limachi.dim_bag.Constants.MOD_ID;
import static org.lwjgl.opengl.GL11.*;
/*
public class BaseWidget extends Widget {
    public static final ResourceLocation DEFAULT_BACKGROUND = new ResourceLocation(MOD_ID, "textures/widgets/backgrounds.png");
    public static final TextureCutout IDLE_TEXTURE = new TextureCutout(DEFAULT_BACKGROUND, 0, 0, 128, 128);
    public static final TextureCutout HOVERED_TEXTURE = new TextureCutout(DEFAULT_BACKGROUND, 0, 128, 128, 128);
    public static final TextureCutout SELECTED_TEXTURE = new TextureCutout(DEFAULT_BACKGROUND, 128, 0, 128, 128);
    public static final TextureCutout HOVERED_SELECTED_TEXTURE = new TextureCutout(DEFAULT_BACKGROUND, 128, 128, 128, 128);
    public static final TextureCutout DISABLED_TEXTURE = SELECTED_TEXTURE;
    public static final TextureCutout[] STATE_ARRAY = {IDLE_TEXTURE, SELECTED_TEXTURE, HOVERED_TEXTURE, HOVERED_SELECTED_TEXTURE};
    public static final Minecraft MINECRAFT = Minecraft.getInstance();
    protected boolean consumeEscKey = false;
    protected boolean isSelected = false;
    protected boolean renderTitle = true;
    protected boolean renderStandardBackground = true;
    protected boolean isToggle = false;
    protected int drag = -1;
    protected boolean enableClickBehavior = true;
    protected BaseWidget parent = null;
    protected boolean wasHovered = false;
    private SimpleContainerScreen<?> screen = null;
    protected Function<BaseWidget, IFormattableTextComponent> tooltip = null;
    protected Function<BaseWidget, IRenderable> deferredRender = null;
    protected int group_id = 0;

    protected boolean isRenderingChildren = false;
    protected boolean canTakeFocus = false;
    protected final ArrayList<BaseWidget> children = new ArrayList<>();

    public static void runOnGroup(SimpleContainerScreen<?> screen, int group, Consumer<BaseWidget> run) {
        screen.getButtons().forEach(b -> {
            if (b instanceof BaseWidget && ((BaseWidget)b).group_id == group)
                run.accept((BaseWidget) b);
        });
    }

    public void runOnGroup(int group, Consumer<BaseWidget> run) {
        if (screen != null)
            runOnGroup(screen, group, run);
    }

    public void runOnGroup(Consumer<BaseWidget> run) {
        if (screen != null)
            runOnGroup(screen, group_id, run);
    }

    public int getGroup() { return group_id; }

    public <T extends BaseWidget> T setGroup(int group) { this.group_id = group; return (T)this; }

    public int x() { return x + (parent != null ? parent.x() : 0); }

    public int y() { return y + (parent != null ? parent.y() : 0); }

    public BaseWidget(int x, int y, int width, int height, ITextComponent title) {
        super(x, y, width, height, title);
    }

    public BaseWidget(int x, int y, int width, int height) { this(x, y, width, height, StringTextComponent.EMPTY); }

    public <T extends BaseWidget> T disable() {
        active = false;
        visible = false;
        changeFocus(false);
        setSelected(false);
        return (T)this;
    }

    public <T extends BaseWidget> T enable() {
        active = true;
        visible = true;
        return (T)this;
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY) {
        if (tooltip != null)
            screen.renderToolTip(matrixStack, screen.getFont().split(tooltip.apply(this), Math.max(screen.width / 2 - 43, 170)), mouseX, mouseY, screen.getFont());
    }

    public <T extends BaseWidget> T setTooltipProcessor(Function<BaseWidget, IFormattableTextComponent> tooltip) { this.tooltip = tooltip; return (T)this; }
    public <T extends BaseWidget> T appendTooltipProcessor(Function<BaseWidget, IFormattableTextComponent> tooltip) {
        if (this.tooltip == null)
            this.tooltip = tooltip;
        else {
            final Function<BaseWidget, IFormattableTextComponent> t = this.tooltip;
            this.tooltip = b -> t.apply(b).copy().append(tooltip.apply(b));
        }
        return (T)this;
    }

    public void attachToScreen(SimpleContainerScreen<?> screen) { this.screen = screen; }
    public void detachFromScreen() { screen = null; }
    public SimpleContainerScreen<?> getScreen() { return screen; }

    public boolean consumeEscKey() { return consumeEscKey; }

    public boolean changeFocus(boolean focus) {
        if (active && visible && canTakeFocus && focus != isFocused()) {
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
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
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
                    renderStandardBackground(matrixStack, partialTicks, x(), y(), width, height, active, renderState());
                renderBg(matrixStack, MINECRAFT, mouseX, mouseY);
                if (renderTitle)
                    drawCenteredString(matrixStack, MINECRAFT.font, getMessage(), x() + width / 2, y() + (height - 8) / 2, getFGColor() | MathHelper.ceil(alpha * 255.0F) << 24);
                renderButton(matrixStack, mouseX, mouseY, partialTicks);
            }

            narrate();
            wasHovered = isHovered();
        }
    }

    public static void renderStandardBackground(MatrixStack matrixStack, float partialTicks, int x, int y, int width, int height, boolean active, int state) {
        (active ? STATE_ARRAY[state] : DISABLED_TEXTURE).blit(matrixStack, new Box2d(x, y, width, height), 0, TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION);
    }

    public void init() {
        for (BaseWidget widget : children) {
            if (screen != null) {
                widget.attachToScreen(screen);
                screen.addButton(widget);
            }
            widget.init();
        }
    }

    public void addChild(BaseWidget widget) {
        if (widget == null) return;
        if (screen != null) {
            widget.attachToScreen(screen);
            screen.addButton(widget);
        }
        children.add(widget);
        widget.setParent(this);
    }

    public void removeChild(BaseWidget widget) {
        if (widget == null) return;
        children.remove(widget);
        widget.setParent(null);
        widget.detachFromScreen();
        if (screen != null)
            screen.removeButton(widget);
    }

    @Override
    public void renderButton(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!children.isEmpty()) {
            glEnable(GL_SCISSOR_TEST);
            scissor(x(), y(), width, height); //make sure the children widgets will not 'bleed' outside the parent
            isRenderingChildren = true; //trick to signify to children that they are allowed to render (by checking the state of the parent)
            for (BaseWidget child : children)
                child.render(matrixStack, mouseX, mouseY, partialTicks);
            isRenderingChildren = false;
            glDisable(GL_SCISSOR_TEST);
        }
    }

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

    public <T extends BaseWidget> T setParent(BaseWidget parent) { this.parent = parent; return (T)this; }

    public BaseWidget getParent() { return parent; }

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
        boolean hover = false;
        for (BaseWidget child : children)
            hover |= child.isMouseOver(mouseX, mouseY);
        return !hover && active && visible && mouseX >= (double)x() && mouseY >= (double)y() && mouseX < (double)(x() + width) && mouseY < (double)(y() + height);
    }

    @Override
    public boolean clicked(double mouseX, double mouseY) {
        boolean click = false;
        for (BaseWidget child : children)
            click |= child.clicked(mouseX, mouseY);
        return !click && enableClickBehavior && active && isMouseOver(mouseX, mouseY);
    }
}
*/