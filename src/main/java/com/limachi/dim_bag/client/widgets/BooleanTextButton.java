package com.limachi.dim_bag.client.widgets;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class BooleanTextButton extends Button {

    protected boolean state;
    Pair<Component, Component> values;

    public BooleanTextButton(int x, int y, int w, int h, Component trueText, Component falseText, boolean initialState, Consumer<BooleanTextButton> onStateChange) {
        super(builder(initialState ? trueText : falseText, s->{
            BooleanTextButton b = (BooleanTextButton)s;
            b.setState(!b.state);
            onStateChange.accept(b);
        }).bounds(x, y, w, h));
        state = initialState;
        values = new Pair<>(trueText, falseText);
    }

    public boolean getState() { return state; }
    public void setState(boolean state) {
        this.state = state;
        setMessage(state ? values.getFirst() : values.getSecond());
    }
}
