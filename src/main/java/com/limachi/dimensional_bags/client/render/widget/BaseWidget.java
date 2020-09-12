package com.limachi.dimensional_bags.client.render.widget;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.limachi.dimensional_bags.client.render.screen.BaseScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public abstract class BaseWidget implements IGuiEventListener, IRenderable {

    public static final TextureCutout MISSING_TEXTURE = new TextureCutout(new ResourceLocation(MOD_ID, "textures/placeholderdimensionalbags.png"), 16, 16, 0, 0, 16, 16);

    private boolean visible = true;
    private boolean listening = true;

    protected Box2d coords;

    private BaseWidget parent = null;
    private List<BaseWidget> childs = new ArrayList<>();
    private BaseScreen<?> screen = null;

    protected TextureCutout background = null;

    private Box2d cachedWritableArea = null;

    public BaseWidget(int x, int y, int width, int height) {
        this.coords = new Box2d(x, y, width, height);
    }

    public void resize(int width, int height) {
        coords = new Box2d(coords.getX1(), coords.getY1(), width, height);
        getWritableArea(true);
    }

    public void move(int newX, int newY) {
        coords = new Box2d(newX, newY, coords.getWidth(), coords.getHeight());
        getWritableArea(true);
    }

    public void setListening(boolean listening) { this.listening = listening; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isVisible() { return visible; }
    public boolean isListening() { return listening; }

    public final void attachChild(BaseWidget child) {
        child.parent = this;
        child.attachToScreen(screen);
        childs.add(child);
    }

    public final BaseWidget detachChild(BaseWidget child) {
        child.parent = null;
        child.attachToScreen(null);
        childs.remove(child);
        return child;
    }

    public void attachToScreen(BaseScreen<?> screen) { this.screen = screen; }
    public final BaseScreen<?> getScreen() { return screen = screen != null ? this.screen : this.parent != null ? this.parent.getScreen() : null; }

    public final void setChildFirst(BaseWidget child) { //push a child in first place to be used first in events
        if (childs.get(0) != child) {
            childs.remove(child);
            childs.add(0, child);
        }
        if (parent != null)
            parent.setChildFirst(this);
    }

    public final Box2d getWritableArea(boolean reload) { //calculate the writable area based on this widget coordinates and all it's parents
        if (reload || cachedWritableArea == null)
            cachedWritableArea = (parent != null ? coords.mergeCut(parent.getWritableArea(false)) : coords);
        return cachedWritableArea;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, Box2d limit) {
        if (getScreen() != null) {
            if (background == null)
                MISSING_TEXTURE.bindTexture();
            else
                background.bindTexture();
            coords.blit(matrixStack, background != null ? background : MISSING_TEXTURE, getWritableArea(false), screen.getBlitOffset());
        }
    }

    @Override
    public final void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (visible)
            render(matrixStack, mouseX, mouseY, partialTicks, getWritableArea(false));
        for (int i = childs.size() - 1; i >= 0; --i) { //rendering is done in the reverse order, so the first widget will be rendered last (above the others)
            childs.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    protected final double relativeMousePositionX(double mouseX) { return mouseX - coords.getX1() - screen.getGuiLeft(); }

    protected final double relativeMousePositionY(double mouseY) { return mouseY - coords.getY1() - screen.getGuiTop(); }

    public void onTick() {} //can be used for animations

    public final void tick() {
        for (BaseWidget widget : childs)
            widget.tick();
        onTick();
    }

    public boolean onMouseMoved(double mouseX, double mouseY) { return false; }

    private boolean _mouseMoved(double mouseX, double mouseY) {
        for (BaseWidget widget : childs)
            if (widget._mouseMoved(mouseX, mouseY))
                return true;
        return listening && onMouseMoved(mouseX, mouseY);
    }

    @Override
    public final void mouseMoved(double mouseX, double mouseY) { _mouseMoved(mouseX, mouseY); }

    public boolean onMouseClicked(double mouseX, double mouseY, int button) { return false; }

    @Override
    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (BaseWidget widget : childs)
            if (widget.mouseClicked(mouseX, mouseY, button))
                return true;
        return listening && onMouseClicked(mouseX, mouseY, button);
    }

    public boolean onMouseReleased(double mouseX, double mouseY, int button) { return false; }

    @Override
    public final boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (BaseWidget widget : childs)
            if (widget.mouseReleased(mouseX, mouseY, button))
                return true;
        return listening && onMouseReleased(mouseX, mouseY, button);
    }

    public boolean onMouseDragged(double mouseX, double mouseY, int button) { return false; }

    @Override
    public final boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        for (BaseWidget widget : childs)
            if (widget.mouseDragged(mouseX, mouseY, button, dx, dy))
                return true;
        return listening && onMouseDragged(mouseX, mouseY, button);
    }

    public boolean onMouseScrolled(double mouseX, double mouseY, double scroll) { return false; }

    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        for (BaseWidget widget : childs)
            if (widget.mouseScrolled(mouseX, mouseY, scroll))
                return true;
        return listening && onMouseScrolled(mouseX, mouseY, scroll);
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) { return false; }

    @Override
    public final boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (BaseWidget widget : childs)
            if (widget.keyPressed(keyCode, scanCode, modifiers))
                return true;
        return listening && onKeyPressed(keyCode, scanCode, modifiers);
    }

    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) { return false; }

    @Override
    public final boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (BaseWidget widget : childs)
            if (widget.keyReleased(keyCode, scanCode, modifiers))
                return true;
        return listening && onKeyReleased(keyCode, scanCode, modifiers);
    }

    public boolean onCharTyped(char codePoint, int modifiers) { return false; }

    @Override
    public final boolean charTyped(char codePoint, int modifiers) {
        for (BaseWidget widget : childs)
            if (widget.charTyped(codePoint, modifiers))
                return true;
        return listening && onCharTyped(codePoint, modifiers);
    }

    @Override
    public final boolean changeFocus(boolean focused) {
        if (focused && parent != null)
            parent.setChildFirst(this);
        return true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return getWritableArea(false).isInArea((int)mouseX, (int)mouseY); }
}
