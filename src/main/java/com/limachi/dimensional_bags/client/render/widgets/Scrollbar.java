package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.limachi.dimensional_bags.client.render.screen.BaseScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.sun.javafx.util.Utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.function.Function;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class Scrollbar extends Base {
    public static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID,"textures/widgets/view_port.png");

    public static final TextureCutout VERTICAL_BACKGROUND = new TextureCutout(TEXTURE, 192, 0, 11, 192);
    public static final TextureCutout VERTICAL_SCROLLER = new TextureCutout(TEXTURE, 0, 225, 11, 11);

    public static final TextureCutout HORIZONTAL_BACKGROUND = new TextureCutout(TEXTURE, 0, 192, 192, 11);
    public static final TextureCutout HORIZONTAL_SCROLLER = new TextureCutout(TEXTURE, 11, 225, 11, 11);

    protected boolean horizontal;
    public double scroll;
    protected Function<Double, Boolean> onChange;
    protected Box2d scrollDetectionArea;
    public double mouseScrollFactor;

    public Scrollbar(BaseScreen<?> screen, Base parent, double x, double y, double length, boolean isHorizontal, double initialScroll, Function<Double, Boolean> onChange, double mouseScrollFactor, @Nullable Box2d scrollDetectionArea) {
        super(screen, parent, x, y, isHorizontal ? length : 11, isHorizontal ? 11 : length, true);
        this.mouseScrollFactor = mouseScrollFactor;
        this.horizontal = isHorizontal;
        this.scroll = initialScroll;
        this.onChange = onChange;
        this.scrollDetectionArea = scrollDetectionArea;
    }

    public static class ScrollbarWithButtons extends Base {
        public final Scrollbar scrollbar;
        public final Button button1;
        public final Button button2;

        public ScrollbarWithButtons(BaseScreen<?> screen, Base parent, double x, double y, double length, boolean isHorizontal, double initialScroll, Function<Double, Boolean> onChange, double mouseScrollFactor, @Nullable Box2d scrollDetectionArea) {
            super(screen, parent, x, y, isHorizontal ? length : 11, isHorizontal ? 11 : length, true);
            scrollbar = new Scrollbar(screen, this, isHorizontal ? 11 : 0, isHorizontal ? 0 : 11, length - 22, isHorizontal, initialScroll, onChange, mouseScrollFactor, scrollDetectionArea);
            button1 = Button.smallButton(screen, this, 0, 0, isHorizontal ? Button.ARROW_LEFT : Button.ARROW_UP, b->scrollbar.setScroll(scrollbar.scroll - mouseScrollFactor * 3));
            button2 = Button.smallButton(screen, this, isHorizontal ? length - 11 : 0, isHorizontal ? 0 : length - 11, isHorizontal ? Button.ARROW_RIGHT : Button.ARROW_DOWN, b->scrollbar.setScroll(scrollbar.scroll + mouseScrollFactor * 3));
        }

        public boolean setScroll(double scroll) {
            return scrollbar.setScroll(scroll);
        }

        public void setScrollDetectionArea(Box2d scrollDetectionArea) {
            scrollbar.setScrollDetectionArea(scrollDetectionArea);
        }
    }

    public void setScrollDetectionArea(Box2d scrollDetectionArea) { this.scrollDetectionArea = scrollDetectionArea; }

    public boolean setScroll(double scroll) {
        double prevScroll = this.scroll;
        this.scroll = Utils.clamp(0.0D, scroll, 1.0D);
        if (prevScroll != this.scroll && onChange != null) {
            return onChange.apply(this.scroll);
        }
        return false;
    }

    @Override
    public void onRender(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Box2d cursor = new Box2d(coords.getX1(), coords.getY1(), 11, 11);
        if (horizontal) {
            HORIZONTAL_BACKGROUND.blit(matrixStack, coords, screen.getBlitOffset(), true, TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION);
            HORIZONTAL_SCROLLER.blit(matrixStack, cursor.setX1(coords.getX1() + 1.0d + (coords.getWidth() - 13.0d) * scroll), screen.getBlitOffset(), true, TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION);
        } else {
            VERTICAL_BACKGROUND.blit(matrixStack, coords, screen.getBlitOffset(), true, TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION);
            VERTICAL_SCROLLER.blit(matrixStack, cursor.setY1(coords.getY1() + 1.0d + (coords.getHeight() - 13.0d) * scroll), screen.getBlitOffset(), true, TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION);
        }
    }

    private boolean _onMouseClicked(double mouseX, double mouseY) {
        Box2d b = coords.copy();
        if (horizontal) {
            b.move(5.5D, 0).setWidth(coords.getWidth() - 13D).transform(getLocalMatrix());
            setScroll((mouseX - b.getX1()) / b.getWidth());
        } else {
            setScroll(getUVpos(mouseX, mouseY).y);
            b.move(0, 5.5D).setHeight(coords.getHeight() - 13D).transform(getLocalMatrix());
            setScroll((mouseY - b.getY1()) / b.getHeight());
        }
        return true;
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !isHovered()) return false;
        return _onMouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean onMouseDragged(double mouseX, double mouseY, int button, double initialDragX, double initialDragY, double dragScrollX, double dragScrollY) {
        if (button != 0) return false;
        return _onMouseClicked(mouseX, mouseY);
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (isHovered() || (scrollDetectionArea != null && scrollDetectionArea.isIn(mouseX, mouseY))) {
            if (horizontal) {
                if (Screen.hasShiftDown()) {
                    setScroll(scroll - scrollAmount * mouseScrollFactor);
                    return true;
                }
            } else {
                if (!Screen.hasShiftDown()) {
                    setScroll(scroll - scrollAmount * mouseScrollFactor);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_END:
                return setScroll(1);
            case GLFW.GLFW_KEY_HOME:
                return setScroll(0);
            case GLFW.GLFW_KEY_PAGE_UP:
                return setScroll(scroll - 11.0D / (horizontal ? coords.getWidth() : coords.getHeight()));
            case GLFW.GLFW_KEY_PAGE_DOWN:
                return setScroll(scroll + 11.0D / (horizontal ? coords.getWidth() : coords.getHeight()));
            case GLFW.GLFW_KEY_UP:
                return !horizontal && setScroll(scroll - mouseScrollFactor);
            case GLFW.GLFW_KEY_DOWN:
                return !horizontal && setScroll(scroll + mouseScrollFactor);
            case GLFW.GLFW_KEY_LEFT:
                return horizontal && setScroll(scroll - mouseScrollFactor);
            case GLFW.GLFW_KEY_RIGHT:
                return horizontal && setScroll(scroll + mouseScrollFactor);
        }
        return false;
    }
}
