package com.limachi.dimensional_bags.client.render.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.function.BiFunction;

public class RadialMenu {/*extends Screen {

    public static final Minecraft MC = Minecraft.getInstance();

    public static void openRadialMenu(List<SliceDescription> slices, double radius, BiFunction<Integer, Integer, Boolean> onClick) {
        MC.displayGuiScreen(new RadialMenu(slices, radius, onClick));
    }

    public static class SliceDescription {
        public final ItemStack icon;
        public final ITextComponent title;

        public SliceDescription(ITextComponent title, ItemStack icon) {
            this.icon = icon;
            this.title = title;
        }
    }

    protected int selectedSlice = -1;
    protected final List<SliceDescription> slices;
    protected final double radius;
    protected final BiFunction<Integer, Integer, Boolean> onClick;
    protected int cx;
    protected int cy;
    protected final double sliceAngle;

    public RadialMenu(List<SliceDescription> slices, double radius, BiFunction<Integer, Integer, Boolean> onClick) {
        super(new StringTextComponent(""));
        passEvents = true;
        this.slices = slices;
        this.radius = radius * MC.getMainWindow().getGuiScaleFactor();
        this.onClick = onClick;
        sliceAngle = 360D / slices.size();
    }

    @Override
    protected void init() {
        super.init();
        cx = width / 2;
        cy = height / 2;
    }

    public void closeRadialMenu(int button) {
        if (MC.currentScreen instanceof RadialMenu) {
            if (selectedSlice != -1)
                onClick.apply(selectedSlice, button);
            MC.displayGuiScreen(null);
        }
    }

    protected void mouseHover(double mouseX, double mouseY) {
        double x = mouseX - cx; //we offset the mouse from the center (so the 0,0 of the X and Y axis is the center of the screen)
        double y = mouseY - cy;

        if (x * x + y * y <= radius * radius) {//the mouse is inside the circle of the menu

            double theta = (Math.toDegrees(Math.atan2(y, x)) + 360); //we calculate the position on the pi circle of the mouse and convert it to degrees to simplify calculation, we offset by 360 to remove the need to test the sign of the theta

            for (int i = 0; i < slices.size(); ++i) //we iterate on all the slices to find the one containing the mouse theta (by range inclusion)
                if (theta >= i * sliceAngle - sliceAngle / 2 + 360 && theta <= i * sliceAngle + sliceAngle / 2 + 360) {
                    selectedSlice = i;
                    return;
                }
        }
        selectedSlice = -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseHover(mouseX, mouseY);
        if (selectedSlice != -1 && onClick.apply(selectedSlice, button))
            MC.displayGuiScreen(null);
        return true;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        mouseHover(mouseX, mouseY);

        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < slices.size(); ++i)
            drawSliceBackground(buffer, i == selectedSlice, i * sliceAngle - sliceAngle / 2, i * sliceAngle + sliceAngle / 2);

        tessellator.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        for (int i = 0; i < slices.size(); ++i)
            drawSliceIconAndText(buffer, slices.get(i), i * sliceAngle);
    }

    protected void drawSliceBackground(BufferBuilder buffer, boolean isSelected, double a1, double a2) {
        double iterations = Math.max(sliceAngle / 3D, 1D); //the slice will actually be a bunch of small triangles, to approximate the curvature of the circle
        double r1 = Math.toRadians(a1);
        double r2 = Math.toRadians(a2);
        double d = r2 - r1;

        int [] color = isSelected ? new int[]{180, 180, 180, 128} : new int[]{60, 60, 60, 64};

        for (double i = 0; i < iterations; ++i) {
            double t1 = r1 + (i / iterations) * d;
            double t2 = r1 + ((i + 1) / iterations) * d;

            buffer.pos(cx + Math.cos(t1) * radius, cy + Math.sin(t1) * radius, 0).color(color[0], color[1], color[2], color[3]).endVertex();
            buffer.pos(cx + Math.cos(t2) * radius, cy + Math.sin(t2) * radius, 0).color(color[0], color[1], color[2], color[3]).endVertex();
            buffer.pos(cx, cy, 0).color(color[0], color[1], color[2], color[3]).endVertex();
        }
    }

    protected void drawSliceIconAndText(BufferBuilder buffer, SliceDescription slice, double a) {

    }
*/}
