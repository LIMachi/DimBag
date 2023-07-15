package com.limachi.dim_bag.client.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class TextEdit extends EditBox implements ICatchEsc {

    protected static final Minecraft mc = Minecraft.getInstance();
    protected Consumer<TextEdit> onFinish;

    public TextEdit(Font font, int x, int y, int w, int h, String value, Consumer<TextEdit> onFinish) {
        super(font, x, y, w, h, Component.empty());
        setTextColor(-1);
        setTextColorUneditable(-1);
        setBordered(true);
        setMaxLength(256);
        setValue(value);
        this.onFinish = onFinish;
    }

    public void finishInput() {
        setFocused(false);
        if (onFinish != null)
            onFinish.accept(this);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (canConsumeInput()) {
            if (keyCode == InputConstants.KEY_RETURN && !Screen.hasShiftDown()) {
                finishInput();
                return true;
            }
            if (keyCode == InputConstants.KEY_ESCAPE) {
                setFocused(false);
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers) || mc.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
        }
        return false;
    }

    @Override
    public boolean catchEsc() { return canConsumeInput(); }
}
