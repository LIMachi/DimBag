package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.widgets.*;
import com.limachi.dimensional_bags.common.tileentities.PadTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.stream.Collectors;

public class PadScreen extends ClientSideOnlyScreenHandler.ClientSideOnlyScreen {

    private static final ITextComponent isWhitelistTitle = new TranslationTextComponent("screen.pad.checkbox_whitelist");
    private static final ITextComponent listTitle = new TranslationTextComponent("screen.pad.drop_list.title");

    private final PadTileEntity pad;
    private ToggleWidget isWhitelist;
    private StringListDropDownWidget list;
    private TextFieldWidget title;

    public static void open(PadTileEntity pad) { ClientSideOnlyScreenHandler.open(new PadScreen(pad)); }

    public PadScreen(PadTileEntity pad) { this.pad = pad; }

    public static final TranslationTextComponent TITLE = new TranslationTextComponent("screen.pad.tooltip.title");
    public static final TranslationTextComponent WHITELIST = new TranslationTextComponent("screen.pad.tooltip.whitelist");
    public static final TranslationTextComponent LIST = new TranslationTextComponent("screen.pad.tooltip.list");

    @Override
    public void first() {
        title = new TextFieldWidget(10, 10, 156, 16, BaseWidget.MINECRAFT.font, pad.getName(), (s, w)->!s.isEmpty(), w->{}).appendTooltipProcessor(b->TITLE);
        isWhitelist = new ToggleWidget(140, 30, 16, 16, pad.isWhitelist(), t->{}).appendTooltipProcessor(b->WHITELIST);
        list = new StringListDropDownWidget(10, 30, 128, 16, listTitle, 230, true, pad.getNameList().collect(Collectors.toList())).appendTooltipProcessor(b->LIST);
        handler.background.addChild(title);
        handler.background.addChild(isWhitelist);
        handler.background.addChild(list);
    }

    @Override
    public void end() { pad.updatePad(title.getText(), isWhitelist.isSelected(), list.finalEntries().collect(Collectors.toList())); }
}