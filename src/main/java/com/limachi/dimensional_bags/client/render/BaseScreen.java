package com.limachi.dimensional_bags.client.render;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

public abstract class BaseScreen extends Screen {
    public BaseScreen(ITextComponent titleIn) {
        super(titleIn);
    }
}
