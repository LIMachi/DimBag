package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.widgets.TextWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ManualScreen extends ClientSideOnlyScreenHandler.ClientSideOnlyScreen {

    private static final ITextComponent DEFAULT_TEXT = new TranslationTextComponent("screen.manual.default");

    private TextWidget text;

    public static void open() { ClientSideOnlyScreenHandler.open(new ManualScreen()); }

    public ManualScreen() {
    }

    @Override
    public void first() {
        handler.fullBackground = new Box2d(handler.getGuiLeft(), handler.getGuiTop(), 300, 250);
        text = new TextWidget(handler.getGuiLeft() + 10, handler.getGuiTop() + 10, 128, 230, Minecraft.getInstance().font, DEFAULT_TEXT, 0xFFFFFFFF).enableClickBehavior(false).enableToggleBehavior(false);
    }

    @Override
    public void rebuild() {
        handler.addButton(text);
    }

    @Override
    public void end() {
    }
}
