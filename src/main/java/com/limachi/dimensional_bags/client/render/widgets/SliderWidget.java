package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.limachi.dimensional_bags.client.render.Vector2d;
import com.sun.javafx.util.Utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.IFormattableTextComponent;

import java.util.function.Consumer;

import static com.limachi.dimensional_bags.client.render.TextureCutout.HOVERED;
import static org.lwjgl.glfw.GLFW.*;

/**
 * a slider is a pair of widgets, one that can be dragged/clicked in an area represented by the other widget
 */
public class SliderWidget extends ImageWidget {

    public static class StepRange {
        protected double min = 0;
        protected double max = 0;
        protected double step = 0;
        protected double v = 0;

        public static final StepRange NULL = new StepRange();
        public static final double MIN_STEP = 0.001;

        public boolean isNull() { return min == 0 && max == 0 && step == 0 && v == 0; }

        public StepRange() {}
        public StepRange(double f) { this(0, f, 1, MIN_STEP); }
        public StepRange(double min, double max) { this(min, min, max, MIN_STEP); }
        public StepRange(double min, double v, double max) { this(min, v, max, MIN_STEP); }

        public StepRange(double min, double v, double max, double step) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
            this.step = step;
            this.v = v;
        }

        protected double closestStep(double t) {
            if (step < MIN_STEP) return t;
            double m = (t - min) % step;
            return m < step / 2. ? t - m : t - m + step;
        }

        public StepRange doSteps(double d) { v = Utils.clamp(min, v + d * step, max); return this; }
        public StepRange addValue(double v) { this.v = Utils.clamp(min, closestStep(this.v + v), max); return this; }

        public StepRange setMin(double min) { this.min = min; return this; }
        public StepRange setMax(double max) { this.max = max; return this; }
        public StepRange setStep(double step) { this.step = step; return this; }

        public StepRange setValue(double v) { this.v = Utils.clamp(min, closestStep(v), max); return this; }
        public StepRange setFactor(double f) { v = closestStep(min + f * (max - min)); return this; }
        public StepRange setSteps(double s) { v = Utils.clamp(min, min + s * step, max); return this; }

        public StepRange setNull() {
            min = NULL.min;
            max = NULL.max;
            step = NULL.step;
            v = NULL.v;
            return this;
        }

        public double min() { return min; }
        public double max() { return max; }
        public double step() { return step; }

        public double value() { return v; }
        public long longValue() { return Math.round(v); }
        public int intValue() { return (int)Math.round(v); }

        public double factor() { return (v - min) / (max - min); }
        public double steps() { return (v - min) / step; }
    }

    protected BaseWidget cursor;
    protected Consumer<SliderWidget> onRelease;

    protected StepRange cx;
    protected StepRange cy;
    protected StepRange rx;
    protected StepRange ry;

    @Override
    public int renderState() { return isHoveredOrFocus() ? HOVERED : 0; }

    public SliderWidget(int x, int y, int width, int height, TextureCutout backgroundImage, StepRange rx, StepRange ry, BaseWidget cursor, Consumer<SliderWidget> onRelease) {
        super(x, y, width, height, backgroundImage);
        enableButtonRenderBehavior(true);
        if (cursor instanceof ImageWidget)
            ((ImageWidget)cursor).enableButtonRenderBehavior(true);
        cursor.enableToggleBehavior(false).enableClickBehavior(false).canTakeFocus = false;
        cx = rx.isNull() ? StepRange.NULL : new StepRange(0, 0, width - cursor.getWidth(), 1).setFactor(rx.factor());
        cy = ry.isNull() ? StepRange.NULL : new StepRange(0, 0, height - cursor.getHeight(), 1).setFactor(ry.factor());
        this.rx = rx;
        this.ry = ry;
        cursor.x = cx.intValue();
        cursor.y = cy.intValue();
        application = TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION;
        this.cursor = cursor;
        addChild(cursor);
        this.onRelease = onRelease;
        canTakeFocus = true;
    }

    public StepRange getXRange() { return rx; }
    public StepRange getYRange() { return ry; }

    public boolean xAxis() { return !rx.isNull(); }
    public boolean yAxis() { return !ry.isNull(); }

    public SliderWidget(int x, int y, double start, double end, double val, double step, IFormattableTextComponent tooltipLabel, Consumer<Double> onRelease) {
        this(x, y, 150, 20, ImageWidget.SELECTED_TEXTURE, new StepRange(start, val, end, step), StepRange.NULL, new ImageWidget(x, y, 8, 20, ImageWidget.IDLE_TEXTURE).setTextureApplicationPattern(TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION), r->{if (onRelease != null) onRelease.accept(r.getXRange().value());});
        appendTooltipProcessor(b->tooltipLabel.copy().append("" + ((SliderWidget)b).rx.intValue()));
    }

    public void setCursorPosToMouse(double mouseX, double mouseY) {
        if (xAxis()) {
            rx.setFactor(cx.setValue(mouseX - x() - cursor.getWidth() / 2.).factor());
            cursor.x = cx.intValue();
        }
        if (yAxis()) {
            ry.setFactor(cy.setValue(mouseY - y() - cursor.getHeight() / 2.).factor());
            cursor.y = cy.intValue();
        }
    }

    @Override //move the cursor, if needed
    public void onClick(double mouseX, double mouseY) {
        setCursorPosToMouse(mouseX, mouseY);
    }

    @Override //move the cursor, if needed
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        setCursorPosToMouse(mouseX, mouseY);
    }

    public Vector2d getRelativeCursorPos() {
        return new Vector2d(width != cursor.getWidth() ? cursor.x / (double)(width - cursor.getWidth()) : 0, height != cursor.getHeight() ? cursor.y / (double)(height - cursor.getHeight()) : 0);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) || cursor.isMouseOver(mouseX, mouseY);
    }

    @Override //run onRelease consumer
    public void onRelease(double mouseX, double mouseY) {
        if (onRelease != null)
            onRelease.accept(this);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (isFocused()) {
            if (xAxis() && (Screen.hasShiftDown() || !yAxis())) {
                cursor.x = cx.setFactor(rx.doSteps(amount).factor()).intValue();
                if (onRelease != null)
                    onRelease.accept(this);
                return true;
            } else if (yAxis()) {
                cursor.y = cy.setFactor(ry.doSteps(amount).factor()).intValue();
                if (onRelease != null)
                    onRelease.accept(this);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused()) return false;
        double speed = Screen.hasShiftDown() ? 10 : 1;
        boolean did_something = false;
        switch (keyCode) {
            case GLFW_KEY_LEFT:
                if (xAxis()) {
                    cursor.x = cx.setFactor(rx.doSteps(-speed).factor()).intValue();
                    did_something = true;
                }
                else if (yAxis()) {
                    cursor.y = cy.setFactor(ry.doSteps(-speed).factor()).intValue();
                    did_something = true;
                }
                break;
            case GLFW_KEY_RIGHT:
                if (xAxis()) {
                    cursor.x = cx.setFactor(rx.doSteps(speed).factor()).intValue();
                    did_something = true;
                }
                else if (yAxis()) {
                    cursor.y = cy.setFactor(ry.doSteps(speed).factor()).intValue();
                    did_something = true;
                }
                break;
            case GLFW_KEY_UP:
                if (yAxis()) {
                    cursor.y = cy.setFactor(ry.doSteps(speed).factor()).intValue();
                    did_something = true;
                }
                else if (xAxis()) {
                    cursor.x = cx.setFactor(rx.doSteps(speed).factor()).intValue();
                    did_something = true;
                }
                break;
            case GLFW_KEY_DOWN:
                if (yAxis()) {
                    cursor.y = cy.setFactor(ry.doSteps(-speed).factor()).intValue();
                    did_something = true;
                }
                else if (xAxis()) {
                    cursor.x = cx.setFactor(rx.doSteps(-speed).factor()).intValue();
                    did_something = true;
                }
                break;
        }
        if (did_something && onRelease != null)
            onRelease.accept(this);
        return true;
    }
}
