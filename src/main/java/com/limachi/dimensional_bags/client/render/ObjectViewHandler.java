package com.limachi.dimensional_bags.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;

import javax.annotation.Nullable;
import java.util.Stack;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.*;

public class ObjectViewHandler {

    private static Stack<Box2d> scissors = new Stack<>();
    protected Box2d coords;
    private Box2d originAndFactor;
    protected @Nullable Supplier<ObjectViewHandler> parent;
    public static final Box2d DEFAULT_ORIGIN_AND_FACTOR = new Box2d(0, 0, 1, 1);

    /**
     * @param coords object coordinates in the parent view
     */
    public ObjectViewHandler(Box2d coords) { this(coords, null, null); }

    /**
     * @param coords object coordinates in the parent view
     * @param parent parent supplier, the supplier can be null, but the supplied value should never be null
     */
    public ObjectViewHandler(Box2d coords, Supplier<ObjectViewHandler> parent) { this(coords, parent, null); }

    /**
     * @param coords object coordinates in the parent view
     * @param parent parent supplier, the supplier can be null, but the supplied value should never be null
     * @param originAndFactor determine the virtual origin and factor (delta x/y and zoom) of the view
     */
    public ObjectViewHandler(Box2d coords, @Nullable Supplier<ObjectViewHandler> parent, @Nullable Box2d originAndFactor) {
        this.coords = coords;
        this.parent = parent;
        this.originAndFactor = originAndFactor != null ? originAndFactor : DEFAULT_ORIGIN_AND_FACTOR;
    }

    public void setOriginAndFactor(Box2d originAndFactor) { this.originAndFactor = originAndFactor != null ? originAndFactor : DEFAULT_ORIGIN_AND_FACTOR; }

    public Box2d getOriginAndFactor() { return originAndFactor; }

    public Box2d getView() {
        if (parent != null)
            return parent.get().coords;
        return coords;
    }

    public void move(double x, double y) { coords.setX1(x).setY1(y); }

    public Box2d getCoords() { return coords; }

    public void setCoords(Box2d coords) { this.coords = coords; }

    public void attachParent(@Nullable Supplier<ObjectViewHandler> parent) { this.parent = parent; }

    private Vector2d intoInternal(Vector2d point) {
        return new Vector2d((point.x - originAndFactor.getX1()) / originAndFactor.getWidth(), (point.y - originAndFactor.getY1()) / originAndFactor.getHeight());
    }

    /**
     * calculate the relative position to the virtual windows of a point coming from the parent/root window
     * usually used to convert a mouse position in the root window to a position in virtual windows
     * @param point the current coordinates of the point (from the parent/root window)
     * @param withCut does this scale/translate can return null if the point is outside of view or viewCoords
     * @return the coordinates scaled and translated in this virtual window
     */
    public Vector2d into(Vector2d point, boolean withCut) {
        if (parent != null)
            if ((point = parent.get().into(point, withCut)) == null)
                return null;
        point = intoInternal(point);
        if (withCut && !getView().isIn(point.x, point.y))
            return null;
        return point;
    }

    private Vector2d fromInternal(Vector2d point) {
        return new Vector2d(point.x * originAndFactor.getWidth() + originAndFactor.getX1(), point.y * originAndFactor.getHeight() + originAndFactor.getY1());
    }

    /**
     * calculate the absolute position in the parent/root window of a point comming from this virtual window
     * usually used to test if part of an object in a virtual window is visible in the root window
     * @param point the current coordinates of the point (in this window)
     * @param withCut does this scale/translate can return null if the point is outside of view or viewCoords
     * @return the coordinates scaled and translated in the parent/root window
     */

    /*public Vector2d from(Vector2d point, boolean withCut) {
        if (withCut && !getView().isIn(point.x, point.y))
            return null;
        point = fromInternal(point);
        if (parent != null)
            if ((point = parent.get().from(point, withCut)) == null)
                return null;
        return point.copy();
    }*/

    /**
     * helper functionm similar to @ref into(Vector2d point), except if the box is cut, it will not return null but a culled box
     */
    public Box2d into(Box2d box, boolean withCut) {
        if (parent != null)
            box = parent.get().into(box, withCut);
        box = Box2d.fromCorners(intoInternal(box.getTopLeft()), intoInternal(box.getBottomRight()));
        if (withCut)
            box.mergeCut(getView());
        return box.copy();
    }

    /**
     * helper functionm similar to @ref from(Vector2d point), except if the box is cut, it will not return null but a culled box
     * usually used to print an object from a virtual window in the root window
     */
    public Box2d from(Box2d box, boolean withCut) {
        box = Box2d.fromCorners(fromInternal(box.getTopLeft()), fromInternal(box.getBottomRight()));
        if (withCut)
            box = box.mergeCut(getView());
        if (parent != null)
            box = parent.get().from(box, withCut);
        return box.copy();
    }

    public static Box2d applyMatrix(MatrixStack matrixStack, Box2d box) {
        Matrix4f mat = matrixStack.getLast().getMatrix().copy();
//        float factor = (float)Minecraft.getInstance().getMainWindow().getGuiScaleFactor();
//        mat.mul(Matrix4f.makeScale(factor, factor, factor));
        Vector4f vec1 = new Vector4f((float)box.getX1(), (float)box.getY1(), 0, 1);
        Vector4f vec2 = new Vector4f((float)box.getX2(), (float)box.getY2(), 0, 1);
        vec1.transform(mat);
        vec2.transform(mat);
        return Box2d.fromCorners(vec1.getX(), vec1.getY(), vec2.getX(), vec2.getY());
    }

    private static void innerBlit(Matrix4f matrix, double x, double x2, double y, double y2, int blitOffset, double minU, double maxU, double minV, double maxV) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(matrix, (float)x, (float)y2, (float)blitOffset).tex((float)minU, (float)maxV).endVertex();
        bufferbuilder.pos(matrix, (float)x2, (float)y2, (float)blitOffset).tex((float)maxU, (float)maxV).endVertex();
        bufferbuilder.pos(matrix, (float)x2, (float)y, (float)blitOffset).tex((float)maxU, (float)minV).endVertex();
        bufferbuilder.pos(matrix, (float)x, (float)y, (float)blitOffset).tex((float)minU, (float)minV).endVertex();
        bufferbuilder.finishDrawing();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }

    public void blit(MatrixStack matrix, TextureCutout texture, boolean withCut, int blitOffset) { //virtual window blit function, entire area
        blit(matrix, /*from(coords, false)*/coords, texture, withCut ? /*from(coords, true)*/coords : null, blitOffset);
    }

    public void blit(MatrixStack matrix, Box2d coords, TextureCutout texture, boolean withCut, int blitOffset) { //virtual window blit function, only the specified area
        blit(matrix, /*from(coords, false)*/coords, texture, withCut ? /*from(this.coords, true)*/this.coords : null, blitOffset);
    }

    public static void blit(MatrixStack matrix, Box2d coords, TextureCutout texture, @Nullable Box2d writableArea, int blitOffset) { //root blit function
        texture.bindTexture();
        double minU = texture.corners.getX1() / (double)texture.fileWidth;
        double minV = texture.corners.getY1() / (double)texture.fileHeight;
        double maxU = texture.corners.getX2() / (double)texture.fileWidth;
        double maxV = texture.corners.getY2() / (double)texture.fileHeight;
        if (writableArea == null || (coords.getX1() >= writableArea.getX1() && coords.getY1() >= writableArea.getY1() && coords.getX2() <= writableArea.getX2() && coords.getY2() <= writableArea.getY2())) {
            innerBlit(matrix.getLast().getMatrix(), coords.getX1(), coords.getX2(), coords.getY1(), coords.getY2(), blitOffset, minU, maxU, minV, maxV);
            return;
        }
        double cMinU = minU;
        double cMinV = minV;
        double cMaxU = maxU;
        double cMaxV = maxV;
        if (coords.getX1() < writableArea.getX1())
            cMinU += (maxU - minU) * ((writableArea.getX1() - coords.getX1()) / coords.getWidth());
        if (coords.getY1() < writableArea.getY1())
            cMinV += (maxV - minV) * ((writableArea.getY1() - coords.getY1()) / coords.getHeight());
        if (coords.getX2() > writableArea.getX2())
            cMaxU -= (maxU - minU) * ((coords.getX2() - writableArea.getX2()) / coords.getWidth());
        if (coords.getY2() > writableArea.getY2())
            cMaxV -= (maxV - minV) * ((coords.getY2() - writableArea.getY2()) / coords.getHeight());
        innerBlit(matrix.getLast().getMatrix(), Math.max(coords.getX1(), writableArea.getX1()), Math.min(coords.getX2(), writableArea.getX2()), Math.max(coords.getY1(), writableArea.getY1()), Math.min(coords.getY2(), writableArea.getY2()), blitOffset, cMinU, cMaxU, cMinV, cMaxV);
    }

    public void drawBox(MatrixStack matrixStack, int color) {
        drawBoxS(matrixStack, /*from(coords, true)*/coords, color);
    }

    public void drawBox(MatrixStack matrixStack, Box2d box, int color) {
        drawBoxS(matrixStack, /*from(*/box/*, true)*/, color);
    }

    public static Vector4f expandColor(int color, boolean isShadow) {
        float shadow = isShadow ? 0.25f : 1.0f;
        return new Vector4f(
        (float)(color >> 16 & 255) / 255.0F * shadow,
        (float)(color >> 8 & 255) / 255.0F * shadow,
        (float)(color & 255) / 255.0F * shadow,
        (float)(color >> 24 & 255) / 255.0F);
    }

    public static void drawBoxS(MatrixStack matrixStack, Box2d box, int color) {
        Matrix4f matrix = matrixStack.getLast().getMatrix();
        Vector4f ec = expandColor(color, false);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(matrix, (float)box.getX1(), (float)box.getY2(), 0.0F).color(ec.getX(), ec.getY(), ec.getZ(), ec.getW()).endVertex();
        bufferbuilder.pos(matrix, (float)box.getX2(), (float)box.getY2(), 0.0F).color(ec.getX(), ec.getY(), ec.getZ(), ec.getW()).endVertex();
        bufferbuilder.pos(matrix, (float)box.getX2(), (float)box.getY1(), 0.0F).color(ec.getX(), ec.getY(), ec.getZ(), ec.getW()).endVertex();
        bufferbuilder.pos(matrix, (float)box.getX1(), (float)box.getY1(), 0.0F).color(ec.getX(), ec.getY(), ec.getZ(), ec.getW()).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

//    public static void scissorStart() {
//        glEnable(GL_SCISSOR_TEST);
//    }

    public void scissor(MatrixStack matrixStack) {
        ObjectViewHandler.scissorAbsolute(ObjectViewHandler.applyMatrix(matrixStack, coords));
    }

    public static void scissorAbsolute(Box2d coords) {
        if (scissors.empty())
            glEnable(GL_SCISSOR_TEST);
        double factor = Minecraft.getInstance().getMainWindow().getGuiScaleFactor();
        coords = new Box2d(coords.getX1() * factor, Minecraft.getInstance().getMainWindow().getFramebufferHeight() - coords.getY2() * factor, coords.getWidth() * factor, coords.getHeight() * factor);
        if (!scissors.empty())
            coords = coords.mergeCut(scissors.peek());
        scissors.push(coords);
        if (coords.getWidth() > 0 && coords.getHeight() > 0)
            glScissor((int)coords.getX1(), (int)coords.getY1(), (int)coords.getWidth(), (int)coords.getHeight());
        else
            glScissor(0, 0, 0, 0);
    }

    public void removeScissor() {
        scissors.pop();
        if (scissors.empty())
            glDisable(GL_SCISSOR_TEST);
    }

//    public void scissor(MatrixStack matrixStack, Box2d coords) { scissorAbsolute(matrixStack, from(coords, false)); }

//    public void scissor(MatrixStack matrixStack) { scissorAbsolute(matrixStack, from(coords, false)); }

//    public static void scissorEnd() { glDisable(GL_SCISSOR_TEST); }

    public void drawString(MatrixStack matrixStack, FontRenderer font, String string, Box2d coords, int textColor, boolean withWrap) {
        int r = 0;
//        coords = from(coords, true);
        float x = (float)coords.getX1();
        float y = (float)coords.getY1();
        int l = 0;
        String tmpStr;

//        matrixStack.push();
//        matrixStack.getLast().getMatrix().set(from(matrixStack.getLast().getMatrix())); //swap the new pushed matrix for one that is scaled/translated to the root widget view
        if (withWrap)
            while (r < string.length() && y + l * font.FONT_HEIGHT < coords.getY2()) {
                tmpStr = font.trimStringToWidth(string.substring(r), (int) coords.getWidth());
                if (tmpStr.isEmpty())
                    tmpStr = string.substring(0, 1);
                r += tmpStr.length();
                font.drawString(matrixStack, tmpStr, x, (float)(y + l * font.FONT_HEIGHT), textColor);
                ++l;
            }
        else
            font.drawString(matrixStack, string, x, y, textColor);
//        matrixStack.pop();
    }

    public void drawString(MatrixStack matrixStack, FontRenderer font, String string, int textColor, boolean withWrap) { drawString(matrixStack, font, string, coords, textColor, withWrap); }
}
