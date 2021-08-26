package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.widgets.StringListDropDownWidget;
import com.limachi.dimensional_bags.common.tileentities.PadTileEntity;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.stream.Collectors;
/*
@OnlyIn(Dist.CLIENT)
public class PadScreen extends SimpleContainerScreen<BaseContainer.NullContainer> {

    private static final ITextComponent isWhitelistTitle = new TranslationTextComponent("screen.pad.checkbox_whitelist");

    private final PadTileEntity pad;
    private CheckboxButton isWhitelist;
    private StringListDropDownWidget list;

    public PadScreen(PadTileEntity pad) {
        super(BaseContainer.NullContainer.NULL_CONTAINER, BaseContainer.NullPlayerInventory.NULL_PLAYER_CONTAINER, new TranslationTextComponent("screen.pad.title"));
        this.pad = pad;
        isWhitelist = new CheckboxButton(154, 10, 16, 16, isWhitelistTitle, pad.isWhitelist());
        list = new StringListDropDownWidget(10, 10, 128, 16, new TranslationTextComponent("screen.pad.drop_list.title"), 256, this, true, pad.getNameList().collect(Collectors.toList()));
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        addButton(isWhitelist);
        addButton(list);
        list.init();
    }

    public static void open(PadTileEntity pad) {
        if (!DimBag.isServer(null)) Minecraft.getInstance().setScreen(new PadScreen(pad));
    }

    public void onClose() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        if (pad != null)
            pad.updateList(isWhitelist.selected(), list.finalEntries().collect(Collectors.toList()));
        super.onClose();
    }
}*/

public class PadScreen extends ClientSideOnlyScreenHandler.ClientSideOnlyScreen {

    private static final ITextComponent isWhitelistTitle = new TranslationTextComponent("screen.pad.checkbox_whitelist");

    private final PadTileEntity pad;
    private CheckboxButton isWhitelist;
    private StringListDropDownWidget list;

    public static void open(PadTileEntity pad) {
        ClientSideOnlyScreenHandler.open(new PadScreen(pad));
    }

    public PadScreen(PadTileEntity pad) {
        this.pad = pad;
    }

    @Override
    public void first() {
        isWhitelist = new CheckboxButton(154, 10, 16, 16, isWhitelistTitle, pad.isWhitelist());
        list = new StringListDropDownWidget(10, 10, 128, 16, new TranslationTextComponent("screen.pad.drop_list.title"), 256, this.handler, true, pad.getNameList().collect(Collectors.toList()));
    }

    @Override
    public void rebuild() {
        handler.addButton(isWhitelist);
        handler.addButton(list);
        list.init();
    }

    @Override
    public void end() {
        pad.updateList(isWhitelist.selected(), list.finalEntries().collect(Collectors.toList()));
    }
}