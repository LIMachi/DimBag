package com.limachi.dimensional_bags.client.widgets;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.Vector2d;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

public class Base {

    public static final Matrix4f DEFAULT_TRANSFORM = new Matrix4f(new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});

    public boolean isCut;
    public Box2d coords;

    public Base(Base parent, Box2d coords, boolean isCut) {
        this.parent = parent;
        assert (parent != null || this instanceof Root);
        if (parent != null) {
            parent.attachChild(this);
            this.root = parent.root;
        }
        else
            this.root = (Root)this;
        this.coords = coords;
        this.isCut = isCut;
        onNew();
    }

    public void onNew() {}

    public Base(Base parent, double x, double y, double width, double height, boolean isCut) {
        this(parent, new Box2d(x, y, width, height), isCut);
    }

    public Matrix4f getTransform() {
        Matrix4f mat = DEFAULT_TRANSFORM.copy();
        mat.mul(Matrix4f.makeTranslate((float)coords.getX1(), (float)coords.getY1(), 0));
        return mat;
    }

    public Matrix4f getLocalMatrix() { return matrix; }

    @OnlyIn(Dist.CLIENT)
    public void onRender(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {}
    public void onTick(int tick) {}
    public boolean onMouseMoved(double mouseX, double mouseY) { return false; }
    public boolean onMouseClicked(double mouseX, double mouseY, int button) { return false; }
    public boolean onMouseReleased(double mouseX, double mouseY, int button) { return false; }
    public boolean onMouseDragged(double mouseX, double mouseY, int button, double initialDragX, double initialDragY, double dragScrollX, double dragScrollY) { return false; }
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollAmount) { return false; }
    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) { return false; }
    public boolean onCharTyped(char codePoint, int modifiers) { return false; }

    private Matrix4f matrix = DEFAULT_TRANSFORM;
    public final Base parent;
    public final Root root;
    private final ArrayList<Base> children = new ArrayList<>();

    private boolean isHovered = false;

    public boolean isFocused() { return root.getFocusedWidget() == this; }
    public boolean isHovered() { return isHovered; }

    public boolean isActive = true;

    private int drag = -1;
    private double initialDragX = 0;
    private double initialDragY = 0;

    public double getInitialDragX() { return initialDragX; }
    public double getInitialDragY() { return initialDragY; }
    public int getDrag() { return drag; }
    public boolean isDragged() { return drag != -1; }

    public void attachChild(Base child) { children.add(child); }
    public void detachChild(Base child) { children.remove(child); }

    public final void tick(int tick) {
        if (!isActive) return;
        onTick(tick);
        for (Base child : children)
            child.tick(tick);
    }

    @OnlyIn(Dist.CLIENT)
    public final void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!isActive) return;
        if (isCut)
            root.scissor(matrixStack, coords.getX1(), coords.getY1(), coords.getX2(), coords.getY2());
        onRender(matrixStack, mouseX, mouseY, partialTicks);
        matrix = matrixStack.getLast().getMatrix().copy();
        matrixStack.push();
        matrixStack.getLast().getMatrix().mul(getTransform());
        for (Base child : children)
            child.render(matrixStack, mouseX, mouseY, partialTicks);
        if (isCut)
            root.removeScissor();
        matrixStack.pop();
    }

    @OnlyIn(Dist.CLIENT)
    public int getBlitOffset() { return root.getBlitOffset(); }

    @OnlyIn(Dist.CLIENT)
    public FontRenderer getFont() { return Minecraft.getInstance().fontRenderer; }

    public final Box2d getTransformedCoords() {
        Box2d b = this.coords.copy();
        return b.transform(matrix);
    }

    public final Vector2d getUVpos(double x, double y) {
        Box2d b = getTransformedCoords();
        return new Vector2d((x - b.getX1()) / b.getWidth(), (y - b.getY1()) / b.getHeight());
    }

    public boolean mouseMoved(double mouseX, double mouseY) {
        if (!isActive) return false;
        isMouseOver(mouseX, mouseY);
        if (isFocused() && onMouseMoved(mouseX, mouseY))
            return true;
        for (Base child : children)
            if (child.mouseMoved(mouseX, mouseY))
                return true;
        return !isFocused() && onMouseMoved(mouseX, mouseY);
    }

    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isActive) return false;
        if (isFocused() && onMouseClicked(mouseX, mouseY, button)) {
            drag = button;
            initialDragX = mouseX;
            initialDragY = mouseY;
            return true;
        }
        for (Base child : children)
            if (child.mouseClicked(mouseX, mouseY, button))
                return true;
        if (!isFocused() && onMouseClicked(mouseX, mouseY, button)) {
            drag = button;
            initialDragX = mouseX;
            initialDragY = mouseY;
            return true;
        }
        return false;
    }

    public final boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isActive) return false;
        drag = -1;
        if (isFocused() && onMouseReleased(mouseX, mouseY, button))
            return true;
        for (Base child : children)
            if (child.mouseReleased(mouseX, mouseY, button))
                return true;
        return !isFocused() && onMouseReleased(mouseX, mouseY, button);
    }

    public final boolean mouseDragged(double mouseX, double mouseY, int button, double dragScrollX, double dragScrollY) {
        if (!isActive) return false;
        for (Base child : children)
            if (child.mouseDragged(mouseX, mouseY, button, dragScrollX, dragScrollY))
                return true;
        if (drag == button) return onMouseDragged(mouseX, mouseY, button, initialDragX, initialDragY, dragScrollX, dragScrollY);
        return false;
    }

    public final boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (!isActive) return false;
        if (isFocused() && onMouseScrolled(mouseX, mouseY, scrollAmount))
            return true;
        for (Base child : children)
            if (child.mouseScrolled(mouseX, mouseY, scrollAmount))
                return true;
        return !isFocused() && onMouseScrolled(mouseX, mouseY, scrollAmount);
    }

    public final boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isActive) return false;
        if (isFocused() && onKeyPressed(keyCode, scanCode, modifiers))
            return true;
        for (Base child : children)
            if (child.keyPressed(keyCode, scanCode, modifiers))
                return true;
        return !isFocused() && onKeyPressed(keyCode, scanCode, modifiers);
    }

    public final boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!isActive) return false;
        if (isFocused() && onKeyReleased(keyCode, scanCode, modifiers))
            return true;
        for (Base child : children)
            if (child.keyReleased(keyCode, scanCode, modifiers))
                return true;
        return !isFocused() && onKeyReleased(keyCode, scanCode, modifiers);
    }

    public final boolean charTyped(char codePoint, int modifiers) {
        if (!isActive) return false;
        if (isFocused() && onCharTyped(codePoint, modifiers))
            return true;
        for (Base child : children)
            if (child.charTyped(codePoint, modifiers))
                return true;
        return !isFocused() && onCharTyped(codePoint, modifiers);
    }

    public final boolean changeFocus(boolean isFocused) {
        if (isFocused)
            root.setFocusedWidget(this);
        else if (isFocused())
            root.setFocusedWidget(null);
        return isFocused;
    }

    public final boolean isMouseOver(double mouseX, double mouseY) {
        if (!isActive) return false;
        Box2d b = getTransformedCoords();
        return isHovered = mouseX >= b.getX1() && mouseX <= b.getX2() && mouseY >= b.getY1() && mouseY <= b.getY2();
    }
}
