package com.limachi.dimensional_bags.client.widgets;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.glfw.GLFW;

/**
 * building block of window, scrollable list, etc...
 */
public class ViewPort extends Base {
    /*
    double areaW;
    double areaH;
    double viewX;
    double viewY;
    double viewScale;

    public ViewPort(double x, double y, double width, double height, double areaW, double areaH, double viewX, double viewY, double viewScale) {
        super(x, y, width, height, true);
        this.areaW = areaW;
        this.areaH = areaH;
        this.viewX = viewX;
        this.viewY = viewY;
        this.viewScale = viewScale;
    }

    @Override
    public Matrix4f getTransform() {
        Matrix4f t = super.getTransform();
        t.mul(Matrix4f.makeScale((float)viewScale, (float)viewScale, (float)viewScale));
        t.mul(Matrix4f.makeTranslate(-(float)viewX, -(float)viewY, 0));
        return t;
    }

    public void moveToUV(double u, double v) {
        double w = coords.getWidth() * viewScale;
        double h = coords.getHeight() * viewScale;
        viewX = (areaW - w) * u;
        viewY = (areaH - h) * v;
    }

    public double getU() {
        double w = coords.getWidth() * viewScale;
        return viewX / (areaW - w);
    }

    public double getV() {
        double h = coords.getHeight() * viewScale;
        return viewY / (areaH - h);
    }

    public void zoom(double scale) {
        viewScale = scale;
    }

    @Override
    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_KP_SUBTRACT:
                if (Screen.hasControlDown()) {
                    viewScale *= 0.5;
                    return true;
                }
            case GLFW.GLFW_KEY_KP_ADD:
                if (Screen.hasControlDown()) {
                    viewScale *= 2;
                    return true;
                }
        }
        return false;
    }

    public static class ViewPortWithButtons extends Base {

        public ViewPort view;
        public final Scrollbar.ScrollbarWithButtons rightScrollbarWidget;
        public final Scrollbar.ScrollbarWithButtons bottomScrollbarWidget;
        public final Button.DragButton dragButtonWidget;
        private boolean init = false;

        public ViewPortWithButtons(double x, double y, double width, double height, double areaW, double areaH, double viewX, double viewY, double viewScale, boolean rightScrollbar, boolean bottomScrollbar, boolean dragButton) {
            super(x, y, width, height, true);
            rightScrollbarWidget = rightScrollbar ? new Scrollbar.ScrollbarWithButtons(width - 11, 0, height - (bottomScrollbar || dragButton ? 11 : 0), false, 0, d->{view.moveToUV(view.getU(), d); return true;}, 0.03, getTransformedCoords()) : null;
            bottomScrollbarWidget = bottomScrollbar ? new Scrollbar.ScrollbarWithButtons(0, height - 11, width - (rightScrollbar || dragButton ? 11 : 0), true, 0, d->{view.moveToUV(d, view.getV()); return true;}, 0.03, getTransformedCoords()) : null;
            dragButtonWidget = dragButton ? new Button.DragButton(width - 11, height - 11, (u, v)->{view.moveToUV(u, v); if (rightScrollbar) rightScrollbarWidget.setScroll(v); if (bottomScrollbar) bottomScrollbarWidget.setScroll(u); return true;}) : null;
            view = new ViewPort(0, 0, width - (rightScrollbar || dragButton ? 11 : 0), height - (bottomScrollbar || dragButton ? 11 : 0), areaW, areaH, viewX, viewY, viewScale);
            this.attachChild(rightScrollbarWidget);
            this.attachChild(bottomScrollbarWidget);
            this.attachChild(dragButtonWidget);
            this.attachChild(view);
            init = true;
        }

        @Override
        public void attachChild(Base child) {
            if (init) {
                if (view != null)
                    view.attachChild(child);
            } else
                super.attachChild(child);
        }

        @Override
        public void detachChild(Base child) {
            if (child == view || child == rightScrollbarWidget || child == bottomScrollbarWidget || child == dragButtonWidget) {
                super.detachChild(child);
                if (child == view)
                    view = null;
                return;
            }
            if (view != null)
                view.detachChild(child);
        }
    }*/
}
