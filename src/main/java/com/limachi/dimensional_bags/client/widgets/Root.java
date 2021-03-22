package com.limachi.dimensional_bags.client.widgets;

import com.limachi.dimensional_bags.client.render.screen.SimpleContainerGUI;
import com.limachi.dimensional_bags.client.rootWidgets.RootProvider;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.client.Minecraft;

import net.minecraft.util.math.vector.Vector4f;

import org.lwjgl.opengl.GL11;

import java.util.*;

public class Root extends Base {

    private static final HashMap<UUID, RootProvider> ROOTS = new HashMap<>();

    public static void registerRoot(RootProvider rp) { ROOTS.put(rp.getId(), rp); }
    public static Root getRoot(UUID id) { return ROOTS.get(id).getRoot(); }

    public Root(double x, double y, double width, double height, boolean isCut) {
        super(null, x, y, width, height, isCut);
    }

    public Base focusedWidget = null;
    public Stack<int[]> scissors = new Stack<>();
    public int ticks = 0;

    @OnlyIn(Dist.CLIENT)
    SimpleContainerGUI screen;

    @OnlyIn(Dist.CLIENT)
    public void scissor(MatrixStack matrixStack, double x1, double y1, double x2, double y2) {
        if (scissors.empty())
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        if (x1 > x2) {
            double x = x1;
            x1 = x2;
            x2 = x;
        }
        if (y1 > y2) {
            double y = y1;
            y1 = y2;
            y2 = y;
        }
        Vector4f v1 = new Vector4f((float)x1, (float)y1, 0, 1);
        Vector4f v2 = new Vector4f((float)x2, (float)y2, 0, 1);
        v1.transform(matrixStack.getLast().getMatrix());
        v2.transform(matrixStack.getLast().getMatrix());
        double factor = Minecraft.getInstance().getMainWindow().getGuiScaleFactor();
        int x = (int)(v1.getX() * factor);
        int y = (int)(Minecraft.getInstance().getMainWindow().getFramebufferHeight() - v2.getY() * factor);
        int [] entry = {x, y, x + (int)((v2.getX() - v1.getX()) * factor), y + (int)((v2.getY() - v1.getY()) * factor)};
        if (!scissors.empty()) {
            int [] t = scissors.peek();
            entry[0] = Math.max(entry[0], t[0]);
            entry[1] = Math.max(entry[1], t[1]);
            entry[2] = Math.min(entry[2], t[2]);
            entry[3] = Math.min(entry[3], t[3]);
        }
        scissors.push(entry);
        if (entry[2] - entry[0] <= 0 || entry[3] - entry[1] <= 0)
            GL11.glScissor(0, 0, 0, 0);
        else
            GL11.glScissor(entry[0], entry[1], entry[2] - entry[0], entry[3] - entry[1]);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getBlitOffset() { return screen != null ? screen.getBlitOffset() : 0; }

    @OnlyIn(Dist.CLIENT)
    public void removeScissor() {
        scissors.pop();
        if (scissors.empty())
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public Base getFocusedWidget() { return focusedWidget; }
    public void setFocusedWidget(Base widget) { focusedWidget = widget; }

    @OnlyIn(Dist.CLIENT)
    public void attachToScreen(SimpleContainerGUI screen) { this.screen = screen; }

    @OnlyIn(Dist.CLIENT)
    public SimpleContainerGUI getScreen() { return screen; }

    public void tick() {
        tick(ticks);
        ++ticks;
    }

    public int getTicks() { return ticks; }
}
