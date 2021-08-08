package com.limachi.dimensional_bags.common.container.widgets;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.Vector2d;
import com.limachi.dimensional_bags.client.render.screen.SimpleContainerScreen;
import com.limachi.dimensional_bags.common.container.BaseContainer;
import com.limachi.dimensional_bags.utils.NBTUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseContainerWidget<C extends BaseContainer<C>> {

    public static final Matrix4f DEFAULT_TRANSFORM = new Matrix4f(new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});

    protected CompoundNBT localData = new CompoundNBT();
    boolean isCut;
    Box2d coords;
    private Matrix4f matrix = DEFAULT_TRANSFORM;
    C handler;
    private boolean isHovered = false;
    public boolean isActive = true;
    private int drag = -1;
    private double initialDragX = 0;
    private double initialDragY = 0;

    abstract Renderer renderer();
    abstract ClientEvents clientEvents();

    public BaseContainerWidget(C handler, Box2d coords, boolean isCut) {
        this.handler = handler;
        this.coords = coords;
        this.isCut = isCut;
        if (handler.isClient) {
            this.renderer = renderer();
            this.clientEvents = clientEvents();
        }
    }

    public CompoundNBT getLocalData() { return localData; }

    public CompoundNBT getDiffOrData(CompoundNBT against) {
        CompoundNBT d = NBTUtils.extractDiff(localData, against);
        if (d.toString().length() < localData.toString().length())
            return d;
        return localData;
    }

    public void loadDiffOrData(CompoundNBT load) {
        if (load.getBoolean("IsDIff"))
            NBTUtils.applyDiff(localData, load);
        else
            localData = load;
        localDataChanged();
    }

    /**
     * called on any side when the localData was changed externally (usually server side, when the client interacted with the widget, or client side if another client changed a widget while you are looking to the same container)
     */
    public abstract void localDataChanged();

    public Matrix4f getTransform() {
        Matrix4f mat = DEFAULT_TRANSFORM.copy();
        mat.mul(Matrix4f.makeTranslate((float)coords.getX1(), (float)coords.getY1(), 0));
        return mat;
    }

    public Matrix4f getLocalMatrix() { return matrix; }

    public Renderer renderer = null;
    public static abstract class Renderer {
        public abstract void onRender(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
    }

    public void onTick(int tick) {}

    public ClientEvents clientEvents = null;
    public static abstract class ClientEvents {
        public abstract boolean onMouseMoved(double mouseX, double mouseY);
        public abstract boolean onMouseClicked(double mouseX, double mouseY, int button);
        public abstract boolean onMouseReleased(double mouseX, double mouseY, int button);
        public abstract boolean onMouseDragged(double mouseX, double mouseY, int button, double initialDragX, double initialDragY, double dragScrollX, double dragScrollY);
        public abstract boolean onMouseScrolled(double mouseX, double mouseY, double scrollAmount);
        public abstract boolean onKeyPressed(int keyCode, int scanCode, int modifiers);
        public abstract boolean onKeyReleased(int keyCode, int scanCode, int modifiers);
        public abstract boolean onCharTyped(char codePoint, int modifiers);
    }

    public static class ClientEventsDefault extends ClientEvents {
        public boolean onMouseMoved(double mouseX, double mouseY) { return false; }
        public boolean onMouseClicked(double mouseX, double mouseY, int button) { return false; }
        public boolean onMouseReleased(double mouseX, double mouseY, int button) { return false; }
        public boolean onMouseDragged(double mouseX, double mouseY, int button, double initialDragX, double initialDragY, double dragScrollX, double dragScrollY) { return false; }
        public boolean onMouseScrolled(double mouseX, double mouseY, double scrollAmount) { return false; }
        public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) { return false; }
        public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) { return false; }
        public boolean onCharTyped(char codePoint, int modifiers) { return false; }
    }

    public boolean isFocused() { return handler.getFocusedWidget() == this; }
    public boolean isHovered() { return isHovered; }

    public double getInitialDragX() { return initialDragX; }
    public double getInitialDragY() { return initialDragY; }
    public int getDrag() { return drag; }
    public boolean isDragged() { return drag != -1; }

    public final void tick(int tick) {
        if (!isActive) return;
        onTick(tick);
    }

    @OnlyIn(Dist.CLIENT)
    private SimpleContainerScreen<C> screen;

    @OnlyIn(Dist.CLIENT)
    public void screen(Consumer<SimpleContainerScreen<C>> run) {
        SimpleContainerScreen<C> ps = getScreen();
        if (ps != null)
            run.accept(ps);
    }

    @OnlyIn(Dist.CLIENT)
    public <T> T screen(Function<SimpleContainerScreen<C>, T> run, T def) {
        SimpleContainerScreen<C> ps = getScreen();
        if (ps != null)
            return run.apply(ps);
        return def;
    }

    @OnlyIn(Dist.CLIENT)
    public SimpleContainerScreen<C> getScreen() { return handler.screen; }

    @OnlyIn(Dist.CLIENT)
    public final void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        SimpleContainerScreen<C> ts = getScreen();
        if (!isActive || ts == null || renderer == null) return;
        if (isCut)
            ts.scissor(matrixStack, coords.getX1(), coords.getY1(), coords.getX2(), coords.getY2());
        renderer.onRender(matrixStack, mouseX, mouseY, partialTicks);
        if (isCut)
            ts.removeScissor();
    }

    public final Box2d getTransformedCoords() {
        Box2d b = this.coords.copy();
        return b.transform(matrix);
    }

    public final Vector2d getUVpos(double x, double y) {
        Box2d b = getTransformedCoords();
        return new Vector2d((x - b.getX1()) / b.getWidth(), (y - b.getY1()) / b.getHeight());
    }

    public boolean mouseMoved(double mouseX, double mouseY) {
        if (!isActive || clientEvents == null) return false;
        isMouseOver(mouseX, mouseY);
        return clientEvents.onMouseMoved(mouseX, mouseY);
    }

    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isActive || clientEvents == null) return false;
        if (clientEvents.onMouseClicked(mouseX, mouseY, button)) {
            drag = button;
            initialDragX = mouseX;
            initialDragY = mouseY;
            return true;
        }
        return false;
    }

    public final boolean mouseReleased(double mouseX, double mouseY, int button) {
        drag = -1;
        if (!isActive || clientEvents == null) return false;
        return clientEvents.onMouseReleased(mouseX, mouseY, button);
    }

    public final boolean mouseDragged(double mouseX, double mouseY, int button, double dragScrollX, double dragScrollY) {
        if (!isActive || clientEvents == null) return false;
        if (drag == button) return clientEvents.onMouseDragged(mouseX, mouseY, button, initialDragX, initialDragY, dragScrollX, dragScrollY);
        return false;
    }

    public final boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (!isActive || clientEvents == null) return false;
        return clientEvents.onMouseScrolled(mouseX, mouseY, scrollAmount);
    }

    public final boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isActive || clientEvents == null) return false;
        return clientEvents.onKeyPressed(keyCode, scanCode, modifiers);
    }

    public final boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!isActive || clientEvents == null) return false;
        return clientEvents.onKeyReleased(keyCode, scanCode, modifiers);
    }

    public final boolean charTyped(char codePoint, int modifiers) {
        if (!isActive || clientEvents == null) return false;
        return clientEvents.onCharTyped(codePoint, modifiers);
    }

    public final boolean changeFocus(boolean isFocused) {
        if (isFocused)
            handler.setFocusedWidget(this);
        else if (isFocused())
            handler.setFocusedWidget(null);
        return isFocused;
    }

    public final boolean isMouseOver(double mouseX, double mouseY) {
        if (!isActive) return false;
        Box2d b = getTransformedCoords();
        return isHovered = mouseX >= b.getX1() && mouseX <= b.getX2() && mouseY >= b.getY1() && mouseY <= b.getY2();
    }
}
