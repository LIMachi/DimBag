package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.screen.SimpleContainerScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL11.*;

/**
 * widget to manage multiple attached widgets, needs access to the parent screen to add and remove widgets dynamically
 */

public class BaseParentWidget extends TextWidget {

    protected final Set<BaseWidget> children = new HashSet<>();
    protected boolean isRenderingChildren = false;

    protected final SimpleContainerScreen<?> parentScreen;

    public BaseParentWidget(int x, int y, int width, int height, ITextComponent title, SimpleContainerScreen<?> parentScreen) {
        super(x, y, width, height, MINECRAFT.font, title, 0xFFFFFF);
        this.parentScreen = parentScreen;
    }

    public void init() {
        for (BaseWidget widget : children) {
            parentScreen.addButton(widget);
            widget.init();
        }
    }


    public void addChild(BaseWidget widget) {
        if (widget == null) return;
        children.add(widget);
        widget.setParent(this);
        parentScreen.addButton(widget);
    }

    public void removeChild(BaseWidget widget) {
        if (widget == null) return;
        children.remove(widget);
        widget.setParent(null);
        parentScreen.removeButton(widget);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        glEnable(GL_SCISSOR_TEST);
        scissor(x, y, width, height); //make sure the children widgets will not 'bleed' outside the parent
        isRenderingChildren = true; //trick to signify to children that they are allowed to render (by checking the state of the parent)
        for (BaseWidget child : children)
            child.render(matrixStack, mouseX, mouseY, partialTicks);
        isRenderingChildren = false;
        glDisable(GL_SCISSOR_TEST);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean hover = false;
        for (BaseWidget child : children)
            hover |= child.isMouseOver(mouseX, mouseY);
        return !hover && super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean clicked(double mouseX, double mouseY) {
        boolean click = false;
        for (BaseWidget child : children)
            click |= child.clicked(mouseX, mouseY);
        return !click && super.clicked(mouseX, mouseY);
    }
}
