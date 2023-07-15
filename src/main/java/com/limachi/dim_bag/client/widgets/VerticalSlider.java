package com.limachi.dim_bag.client.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class VerticalSlider extends AbstractWidget {

    public static final ScreenRectangle BACKGROUND = new ScreenRectangle(0, 0, 16, 256);
    public static final ScreenRectangle CURSOR = new ScreenRectangle(16, 0, 12, 15);
    public static final ScreenRectangle SELECTED_CURSOR = new ScreenRectangle(16, 15, 12, 15);


    protected ScreenRectangle area;
    protected ScreenRectangle cursor;
    protected double cursorPos;
    protected Consumer<VerticalSlider> onValueChange;

    public VerticalSlider(int x, int y, int w, int h, double value, Consumer<VerticalSlider> onValueChange) {
        super(x, y, w, h, Component.empty());
        area = new ScreenRectangle(x, y, w, h);
        cursor = new ScreenRectangle(x + 2, y + 2, w - 4, Math.min(15, h - 4));
        cursorPos = Mth.clamp(0., 1., value);
        this.onValueChange = onValueChange;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        setValueFromMouseY(mouseY);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double fromX, double fromY) {
        setValueFromMouseY(mouseY);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        setValueFromMouseY(mouseY);
    }

    protected void setValueFromMouseY(double mouseY) {
        double value = (mouseY - (double)area.top() - 2.) / (double)(area.height() - 4);
        setValue(value);
    }

    public void setValue(double value) {
        value = Mth.clamp(value, 0., 1.);
        if (value != cursorPos) {
            cursorPos = value;
            cursor = new ScreenRectangle(cursor.left(), area.top() + 2 + (int)(cursorPos * (area.height() - cursor.height() - 4)), cursor.width(), cursor.height());
            if (onValueChange != null)
                onValueChange.accept(this);
        }
    }

    public double getValue() { return cursorPos; }

    @Override
    protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.blitNineSliced(Builders.WIDGETS, area.left(), area.top(), area.width(), area.height(), 2, BACKGROUND.width(), BACKGROUND.height(), BACKGROUND.left(), BACKGROUND.top());
        if (isFocused() && isFocused())
            gui.blitNineSliced(Builders.WIDGETS, cursor.left(), cursor.top(), cursor.width(), cursor.height(), 1, 1, 1, 2, SELECTED_CURSOR.width(), SELECTED_CURSOR.height(), SELECTED_CURSOR.left(), SELECTED_CURSOR.top());
        else
            gui.blitNineSliced(Builders.WIDGETS, cursor.left(), cursor.top(), cursor.width(), cursor.height(), 1, 1, 1, 2, CURSOR.width(), CURSOR.height(), CURSOR.left(), CURSOR.top());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrator) {}
}
