package com.limachi.dimensional_bags.client.render.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MultyButtonArray extends Widget implements Button.IPressable {

    protected List<Button> buttons;
    protected Consumer<String> onButtonClicked;

    public MultyButtonArray(int x, int y, int buttonWidth, int buttonHeight, int spacing, List<String> labels, Consumer<String> onButtonClicked) {
        super(x, y, buttonWidth, buttonHeight * labels.size() + (labels.size() > 1 ? spacing * (labels.size() - 1) : 0), new StringTextComponent(""));
        this.buttons = new ArrayList<>();
        this.onButtonClicked = onButtonClicked;
        for (int i = 0; i < labels.size(); ++i)
            this.buttons.add(new Button(x, y + (labels.size() + spacing) * i, buttonWidth, buttonHeight, new StringTextComponent(labels.get(i)), this));
    }

    public int getHeight() { return this.height; }

    public void updateLabels(List<String> labels) {
        for (int i = 0; i < labels.size(); ++i)
            this.buttons.get(i).setMessage(new StringTextComponent(labels.get(i)));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if (this.visible) {
            for (Button button : buttons)
                button.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void onPress(Button button) { this.onButtonClicked.accept(button.getMessage().getString()); }

    public void onClick(double mouseX, double mouseY) { buttons.forEach(b->b.mouseClicked(mouseX, mouseY, 0)); }
}
