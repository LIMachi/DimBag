package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.widgets.Text;
import com.limachi.dimensional_bags.client.render.widgets.TextField;
import com.limachi.dimensional_bags.common.container.BaseContainer;
import com.limachi.dimensional_bags.common.container.BrainContainer;
import com.limachi.dimensional_bags.common.readers.EntityReader;
import com.limachi.dimensional_bags.common.tileentities.BrainTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BrainGUI extends BaseScreen<BrainContainer> {

    protected EntityReader placeHolderReader;
    protected BrainTileEntity brain;

    protected TextField commandField;
    protected Text resultPreviewWidget;
    protected String resultPreview;
    protected String command;

    public BrainGUI(BrainContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    protected void init() {
        placeHolderReader = new EntityReader(Minecraft.getInstance().player);
        commandField = new TextField(this, null, 10, 10, 100, 10, container.command, (s1, s2)->true,s->{command = s;});
        resultPreviewWidget = new Text(this, null, 10, 25, 100, 10, font, "", 0xFFFFFFFF, true);
        command = container.command;
        container.trackString(new BaseContainer.StringReferenceHolder() {
            @Override
            public String get() { return command; }

            @Override
            public void set(String value) { command = value; }
        });
        container.trackString(new BaseContainer.StringReferenceHolder() {
            @Override
            public String get() { return resultPreview; }

            @Override
            public void set(String value) { resultPreview = value; }
        });
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {

    }
}
