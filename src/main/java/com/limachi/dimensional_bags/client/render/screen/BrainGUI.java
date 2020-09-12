package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.widget.TextFieldWithChoices;
import com.limachi.dimensional_bags.common.readers.PlayerReader;
import com.limachi.dimensional_bags.common.tileentities.BrainTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BrainGUI extends Screen {

    protected TextFieldWithChoices key1;
    protected PlayerReader<PlayerEntity> placeHolderReader;
    protected BrainTileEntity brain;

    protected BrainGUI(ITextComponent titleIn) {
        super(titleIn);
    }

//    public BrainGUI(BrainTileEntity brain) { this.brain = brain; }

    protected void init() {
        placeHolderReader = new PlayerReader(Minecraft.getInstance().player);
        key1 = new TextFieldWithChoices(font, 10, 10, 32, placeHolderReader::sortedKeys, "");
    }
}
