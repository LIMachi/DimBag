package com.limachi.dimensional_bags.client.render.widget;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.sun.javafx.util.Utils;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class ScrollBar extends BaseWidget {
    public static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID,"textures/widgets/view_port.png");

    public static final TextureCutout VERTICAL_BACKGROUND_START = new TextureCutout(TEXTURE, 192, 0, 11, 2);
    public static final TextureCutout VERTICAL_BACKGROUND_CENTER = new TextureCutout(TEXTURE, 192, 2, 11, 188);
    public static final TextureCutout VERTICAL_BACKGROUND_END = new TextureCutout(TEXTURE, 192, 190, 11, 2);
    public static final TextureCutout VERTICAL_SCROLLER = new TextureCutout(TEXTURE, 0, 225, 11, 11);

    public static final TextureCutout HORIZONTAL_BACKGROUND_START = new TextureCutout(TEXTURE, 0, 192, 2, 11);
    public static final TextureCutout HORIZONTAL_BACKGROUND_CENTER = new TextureCutout(TEXTURE, 2, 192, 188, 11);
    public static final TextureCutout HORIZONTAL_BACKGROUND_END = new TextureCutout(TEXTURE, 190, 192, 2, 11);
    public static final TextureCutout HORIZONTAL_SCROLLER = new TextureCutout(TEXTURE, 11, 225, 11, 11);

    protected boolean horizontal;
    public double scroll;
    protected Consumer<Double> onChange;
    protected Box2d scrollDetectionArea;
    protected Box2d cursor;
    public double mouseScrollFactor;
    protected int length;
    private boolean draging = false;

    public ScrollBar(int x, int y, int length, boolean isHorizontal, double initialScroll, Consumer<Double> onChange, double mouseScrollFactor, @Nullable Box2d scrollDetectionArea) {
        super(x, y, isHorizontal ? length : 11, isHorizontal ? 11 : length);
        this.mouseScrollFactor = mouseScrollFactor;
        this.horizontal = isHorizontal;
        this.scroll = initialScroll;
        this.length = length;
        this.onChange = onChange;
        this.scrollDetectionArea = scrollDetectionArea;
        this.cursor = new Box2d(x, y, 11, 11);
        updateCursorPos(false);
    }

    @Override
    public void resize(int width, int height) {
        length = horizontal ? width : height;
        coords = new Box2d(coords.getX1(), coords.getY1(), width, height);
        getWritableArea(true);
    }

    public void setScroll(double scroll) {
        this.scroll = Utils.clamp(0.0D, 1.0D, scroll);
        updateCursorPos(true);
    }

    protected void updateCursorPos(boolean withUpdate) {
        if (horizontal)
            cursor.setX1(coords.getX1() + 1 + (int)((coords.getWidth() - 13.0D) * scroll));
        else
            cursor.setY1(coords.getY1() + 1 + (int)((coords.getHeight() - 13.0D) * scroll));
        if (withUpdate && onChange != null)
            onChange.accept(scroll);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, Box2d limit) {
        Box2d mcl = coords.mergeCut(limit);
        Box2d t = coords.copy();
        int blitOffset = getScreen().getBlitOffset();
        if (horizontal) {
            HORIZONTAL_BACKGROUND_START.bindTexture();
            t.setWidth(2).blit(matrixStack, HORIZONTAL_BACKGROUND_START, mcl, blitOffset);
            HORIZONTAL_BACKGROUND_CENTER.bindTexture();
            for (int i = 2; i < length - 4; i += HORIZONTAL_BACKGROUND_CENTER.corners.getWidth())
                t.setWidth(HORIZONTAL_BACKGROUND_CENTER.corners.getWidth()).setX1(coords.getX1() + i).blit(matrixStack, HORIZONTAL_BACKGROUND_CENTER, mcl, blitOffset);
            HORIZONTAL_BACKGROUND_END.bindTexture();
            t.setWidth(2).setX1(coords.getX2() - 2).blit(matrixStack, HORIZONTAL_BACKGROUND_END, mcl, blitOffset);
            HORIZONTAL_SCROLLER.bindTexture();
            cursor.blit(matrixStack, HORIZONTAL_SCROLLER, mcl, blitOffset);
        } else {
            VERTICAL_BACKGROUND_START.bindTexture();
            t.setHeight(2).blit(matrixStack, VERTICAL_BACKGROUND_START, mcl, blitOffset);
            VERTICAL_BACKGROUND_CENTER.bindTexture();
            for (int i = 2; i < length - 4; i += VERTICAL_BACKGROUND_CENTER.corners.getHeight())
                t.setHeight(VERTICAL_BACKGROUND_CENTER.corners.getHeight()).setY1(coords.getY1() + i).blit(matrixStack, VERTICAL_BACKGROUND_CENTER, mcl, blitOffset);
            VERTICAL_BACKGROUND_END.bindTexture();
            t.setHeight(2).setY1(coords.getY2() - 2).blit(matrixStack, VERTICAL_BACKGROUND_END, mcl, blitOffset);
            VERTICAL_SCROLLER.bindTexture();
            cursor.blit(matrixStack, VERTICAL_SCROLLER, mcl, blitOffset);
        }
    }

    private boolean _onMouseClicked(double mouseX, double mouseY) {
        if (horizontal)
            scroll = (mouseX - 5.5D) / (coords.getWidth() - 11D);
        else
            scroll = (mouseY - 5.5D) / (coords.getHeight() - 11D);
        scroll = Utils.clamp(0.0D, scroll, 1.0D);
        updateCursorPos(true);
        return true;
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isMouseOver(mouseX, mouseY)) return false; //only accept left click in the widget area
        draging = true;
        return _onMouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        draging = false;
        return false;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scroll) {
        if (scrollDetectionArea == null || scrollDetectionArea.isInArea((int)mouseX, (int)mouseY)) {
            this.scroll = Utils.clamp(0.0D, this.scroll - scroll * mouseScrollFactor, 1.0D);
            updateCursorPos(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseDragged(double mouseX, double mouseY, int button) {
        if (!draging || button != 0 || (scrollDetectionArea != null && !scrollDetectionArea.isInArea((int)mouseX, (int)mouseY))) return false;
        return _onMouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_END:
                scroll = 1.0D;
                updateCursorPos(true);
                return true;
            case GLFW.GLFW_KEY_HOME:
                scroll = 0.0D;
                updateCursorPos(true);
                return true;
            case GLFW.GLFW_KEY_PAGE_UP:
                scroll = Utils.clamp(0.0D,scroll - 11.0D / length, 1.0D);
                updateCursorPos(true);
                return true;
            case GLFW.GLFW_KEY_PAGE_DOWN:
                scroll = Utils.clamp(0.0D,scroll + 11.0D / length, 1.0D);
                updateCursorPos(true);
                return true;
        }
        return false;
    }
}
