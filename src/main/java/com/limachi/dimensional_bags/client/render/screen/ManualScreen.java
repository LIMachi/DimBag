package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.widgets.TextWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ManualScreen extends ClientSideOnlyScreenHandler.ClientSideOnlyScreen {

    private static final ITextComponent DEFAULT_TEXT = new TranslationTextComponent("screen.manual.missing_patchouli");

    private TextWidget text;

    public static void open() { ClientSideOnlyScreenHandler.open(new ManualScreen()); }

    public ManualScreen() {
    }

    @Override
    public void first() {
        text = new TextWidget(10, 10, 156, 147, Minecraft.getInstance().font, DEFAULT_TEXT, 4210752).enableClickBehavior(false).enableToggleBehavior(false).enableRenderStandardBackground(false);
    }

    @Override
    public void rebuild() {
        handler.addButton(text, 10, 10);
    }

    @Override
    public void end() {}
}
