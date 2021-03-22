package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.widgets.Text;
import com.limachi.dimensional_bags.client.widgets.TextField;
import com.limachi.dimensional_bags.common.container.BaseContainer;
import com.limachi.dimensional_bags.common.container.GhostHandContainer;
import com.limachi.dimensional_bags.common.executors.EntityExecutor;
import com.limachi.dimensional_bags.common.tileentities.GhostHandTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/*
@OnlyIn(Dist.CLIENT)
public class GhostHandGUI extends BaseScreen<GhostHandContainer> {

    protected EntityExecutor placeHolderReader;
    protected GhostHandTileEntity ghostHand;

    protected TextField commandField;
    protected Text resultPreviewWidget;
    protected String resultPreview;
    protected String command;

    public GhostHandGUI(GhostHandContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    protected void init() {
//        placeHolderReader = new EntityExecutor(Minecraft.getInstance().player, eyeId);
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
*/