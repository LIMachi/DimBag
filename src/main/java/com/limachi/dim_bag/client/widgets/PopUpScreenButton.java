package com.limachi.dim_bag.client.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class PopUpScreenButton extends AbstractButton {

    protected Function<PopUpScreenButton, Screen> popUp;

    public PopUpScreenButton(int x, int y, int w, int h, Component message, Function<PopUpScreenButton, Screen> popUp) {
        super(x, y, w, h, message);
        this.popUp = popUp;
    }

    @Override
    public void onPress() {
        if (popUp != null)
            ForgeHooksClient.pushGuiLayer(Minecraft.getInstance(), popUp.apply(this));
    }

    @Override
    protected void updateWidgetNarration(@Nonnull NarrationElementOutput narration) {}
}
