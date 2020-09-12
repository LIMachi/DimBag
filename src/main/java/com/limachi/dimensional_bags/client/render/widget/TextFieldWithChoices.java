package com.limachi.dimensional_bags.client.render.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Function;

public class TextFieldWithChoices extends TextFieldWidget {

    protected ArrayList<String> cachedChoices;
    protected Function<String, ArrayList<String>> searchFunction;
    protected ScrollBar scrollBar;
    protected MultyButtonArray buttons;

    public TextFieldWithChoices(FontRenderer font, int x, int y, int l, Function<String, ArrayList<String>> searchSorter, @Nullable String input) {
        super(font, x, y, 9 * l, 9, new StringTextComponent(""));
        this.setText(input != null ? input : "");
        this.searchFunction = searchSorter;
        this.setVisible(true);
        this.cachedChoices = searchFunction.apply(getText());
        if (cachedChoices.size() > 7) {
            this.buttons = new MultyButtonArray(x + 9, y + 9, 9 * (l - 1), 8, 1, cachedChoices.subList(0, 7), this::onButtonClicked);
//            this.scrollBar = new ScrollBar(x, y + 9, 9, buttons.getHeight(), 0, 0, true, false, 7, 7, this::onScrollBarChanged);
        }
        else
            this.buttons = new MultyButtonArray(x, y + 9, 9 * l, 8, 1, cachedChoices, this::onButtonClicked);
        changeComponentsVisibility();
        this.setResponder(this::onTextBoxChanged);
    }

    protected void onTextBoxChanged(String new_input) {
        cachedChoices = searchFunction.apply(new_input);
        if (cachedChoices.size() > 7) {
//            int i = (int) ((cachedChoices.size() - 7) * scrollBar.yScroll);
//            buttons.updateLabels(cachedChoices.subList(i, i + 7));
        }
        else
            buttons.updateLabels(cachedChoices);
    }

    protected void onScrollBarChanged(double x, double y) {
        onTextBoxChanged(getText());
    }

    protected void onButtonClicked(String label) {
        setText(label);
        onTextBoxChanged(label);
    }

    void changeComponentsVisibility() {
//        this.scrollBar.visible = visible && this.active && this.isFocused();
        this.buttons.visible = visible && this.active && this.isFocused();
    }

    @Override
    protected void onFocusedChanged(boolean isFocused) {
        changeComponentsVisibility();
        super.onFocusedChanged(isFocused);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        buttons.mouseClicked(mouseX, mouseY, 0);
        scrollBar.mouseClicked(mouseX, mouseY, 0);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if (visible) {
            buttons.render(matrixStack, mouseX, mouseY, partialTicks);
            scrollBar.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        return scrollBar.mouseScrolled(mouseX, mouseY, scroll);
    }
}
