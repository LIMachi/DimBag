package com.limachi.dimensional_bags.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;

import javax.annotation.Nonnull;

public interface ITooltipRenderer {

    void renderToolTip(@Nonnull Screen screen, @Nonnull MatrixStack matrixStack, int mouseX, int mouseY);
}
