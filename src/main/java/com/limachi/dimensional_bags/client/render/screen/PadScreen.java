package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.widgets.BaseWidget;
import com.limachi.dimensional_bags.client.render.widgets.ImageWidget;
import com.limachi.dimensional_bags.client.render.widgets.StringListDropDownWidget;
import com.limachi.dimensional_bags.client.render.widgets.TextFieldWidget;
import com.limachi.dimensional_bags.common.tileentities.PadTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.stream.Collectors;

public class PadScreen extends ClientSideOnlyScreenHandler.ClientSideOnlyScreen {

    private static final ITextComponent isWhitelistTitle = new TranslationTextComponent("screen.pad.checkbox_whitelist");
    private static final ITextComponent listTitle = new TranslationTextComponent("screen.pad.drop_list.title");

    private final PadTileEntity pad;
    private ImageWidget.Toggle isWhitelist;
    private StringListDropDownWidget list;
    private TextFieldWidget title;

    public static void open(PadTileEntity pad) { ClientSideOnlyScreenHandler.open(new PadScreen(pad)); }

    public PadScreen(PadTileEntity pad) { this.pad = pad; }

    @Override
    public void first() {
        title = new TextFieldWidget(10, 10, 156, 16, BaseWidget.MINECRAFT.font, pad.getName(), (s, w)->!s.isEmpty(), w->{});
        title.tooltip = new TranslationTextComponent("screen.pad.tooltip.title");
        isWhitelist = new ImageWidget.Toggle(140, 30, 16, 16, pad.isWhitelist(), t->{});
        isWhitelist.tooltip = new TranslationTextComponent("screen.pad.tooltip.whitelist");
        list = new StringListDropDownWidget(10, 30, 128, 16, listTitle, 230, this.handler, true, pad.getNameList().collect(Collectors.toList()));
        list.tooltip = new TranslationTextComponent("screen.pad.tooltip.list");
    }

    @Override
    public void rebuild() {
        handler.addButton(title, 10, 10);
        handler.addButton(isWhitelist, 140, 30);
        handler.addButton(list, 10, 30);
        list.init();
    }

    @Override
    public void end() { pad.updatePad(title.getText(), isWhitelist.isSelected(), list.finalEntries().collect(Collectors.toList())); }
}