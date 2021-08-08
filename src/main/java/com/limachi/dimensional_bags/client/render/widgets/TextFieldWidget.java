package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.EventManager;
import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.mojang.blaze3d.matrix.MatrixStack;
import javafx.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;

public class TextFieldWidget extends BaseWidget {

    protected FontRenderer font;
    protected String text = "";
    protected int selection = 0;
    protected int cursor = 0;
    protected int leftCorrection = 0;
    protected BiFunction<String, TextFieldWidget, Boolean> validateText;
    protected Consumer<TextFieldWidget> onFinishInput;
    protected boolean insertMode = true;
    public int textColor = 0xFFFFFFFF;
    public int selectionColor = 0x3344CCCC;
    public int cursorColor = 0xFFAAAAAA;
    public int characterPadding = 3; //how close to the border the cursor can be before correction
    protected ArrayList<Pair<Integer, String>> history = new ArrayList<>(); //history only persist for this instance of the widget, for advanced history, a variant of this widget could be made
    protected int historyPage = -1;
    public boolean allowMultiLine = false;

    public TextFieldWidget(int x, int y, int width, int height, @Nonnull FontRenderer font, @Nullable String initialText, @Nullable BiFunction<String, TextFieldWidget, Boolean> validateText, @Nullable Consumer<TextFieldWidget> onFinishInput) {
        super(x, y, width, height);
        this.font = font;
        if (initialText != null)
            setText(initialText);
        pushToHistory();
        this.onFinishInput = onFinishInput;
        this.validateText = validateText;
        renderStandardBackground = false;
    }

    public void finishInput(boolean withCallback) {
        if (withCallback && onFinishInput != null)
            onFinishInput.accept(this);
        selection = cursor;
        setFocused(false);
    }

    protected void setCursorPos(int pos, boolean select) {
        cursor = MathHelper.clamp(pos, 0, text.length());
        if (text.length() != 0) {
            if (cursor - characterPadding < leftCorrection)
                leftCorrection = MathHelper.clamp(cursor - characterPadding, 0, text.length() - 1);
            else {
                String vt = getVisibleText();
                if (cursor + characterPadding > leftCorrection + vt.length() && font.getStringWidth(vt) + font.getStringWidth("|_|_") >= width)
                    leftCorrection = MathHelper.clamp(cursor - vt.length() + characterPadding, 0, text.length() - 1);
            }
        }
        if (!select)
            selection = cursor;
    }

    protected boolean shouldSelect() { return Screen.hasShiftDown() || drag != -1; }

    protected void select(int start, int end) {
        setCursorPos(start, false);
        setCursorPos(end, true);
    }

    public void selectWord(boolean left) {
        int start = 0;
        int end;
        do {
            end = text.indexOf(' ', start);
            if (end == -1 || end > cursor) {
                if (end == -1) end = text.length();
                select(left ? end : start, left ? start : end);
                return;
            }
            start = end + 1;
        } while (true);
    }

    public void setText(String text) {
        if (validateText == null || validateText.apply(text, this)) {
            this.text = text;
            if (cursor > text.length())
                setCursorPos(text.length(), shouldSelect());
        }
    }

    public String getText() { return text; }

    public String getSelectedText() {
        return text.length() == 0 || selection == cursor ? "" : text.substring(Math.min(selection, cursor), Math.max(selection, cursor));
    }

    public String getVisibleText() {
        return font.trimStringToWidth(text.substring(leftCorrection), width - 6);
    }

    public void pushToHistory() {
        history.add(++historyPage, new Pair<>(cursor, new String(text)));
        while (history.size() > historyPage + 1)
            history.remove(historyPage + 1);
    }

    public void loadHistoryPage(int page) {
        if (page >= 0 && page < history.size()) {
            historyPage = page;
            Pair<Integer, String> p = history.get(page);
            setText(p.getValue());
            setCursorPos(p.getKey(), shouldSelect());
        }
    }

    public void writeText(String add) {
        add = SharedConstants.filterAllowedCharacters(add);
        String start = text.substring(0, Math.min(selection, cursor));
        String end = text.substring(Math.max(selection, cursor));
        if (!insertMode)
            end = end.length() > add.length() ? end.substring(add.length()) : "";
        setText(start + add + end);
        setCursorPos((start + add).length(), shouldSelect());
        pushToHistory();
        selection = cursor;
    }

    protected void delete(int dir) {
        if (selection != cursor) {
            writeText("");
            return;
        }
        int newPos = MathHelper.clamp(cursor + dir, 0, text.length());
        if (dir < 0) {
            text = text.substring(0, newPos) + text.substring(cursor);
            setCursorPos(newPos, shouldSelect());
            selection = cursor;
        }
        else
            text = text.substring(0, cursor) + text.substring(newPos);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused()) return false;
        if (Screen.isSelectAll(keyCode)) {
            cursor = text.length();
            selection = 0;
            return true;
        } else if (Screen.isCopy(keyCode)) {
            Minecraft.getInstance().keyboardListener.setClipboardString(getSelectedText());
            return true;
        } else if (Screen.isPaste(keyCode)) {
            writeText(Minecraft.getInstance().keyboardListener.getClipboardString());
            return true;
        } else if (Screen.isCut(keyCode)) {
            Minecraft.getInstance().keyboardListener.setClipboardString(getSelectedText());
            writeText("");
            return true;
        } else {
            switch (keyCode) {
                case GLFW_KEY_ESCAPE:
                    finishInput(false);
                    return true;
                case GLFW_KEY_ENTER:
                    if (allowMultiLine && Screen.hasShiftDown())
                        writeText("\r\n");
                    else
                        finishInput(true);
                    return true;
                case GLFW_KEY_BACKSPACE:
                    delete(-1);
                    return true;
                case GLFW_KEY_INSERT:
                    insertMode = !insertMode;
                    return true;
                case GLFW_KEY_DELETE:
                    delete(1);
                    return true;
                case GLFW_KEY_RIGHT:
                    setCursorPos(cursor + 1, shouldSelect());
                    return true;
                case GLFW_KEY_LEFT:
                    setCursorPos(cursor - 1, shouldSelect());
                    return true;
                case GLFW_KEY_HOME:
                    setCursorPos(0, shouldSelect());
                    return true;
                case GLFW_KEY_END:
                    setCursorPos(text.length(), shouldSelect());
                    return true;
                case GLFW_KEY_Z:
                    if (Screen.hasControlDown()) {
                        loadHistoryPage(historyPage - 1);
                        return true;
                    }
                case GLFW_KEY_Y:
                    if (Screen.hasControlDown()) {
                        loadHistoryPage(historyPage + 1);
                        return true;
                    }
                case GLFW_KEY_TAB:
                case GLFW_KEY_DOWN:
                case GLFW_KEY_UP:
                case GLFW_KEY_PAGE_UP:
                case GLFW_KEY_PAGE_DOWN:
                default:
            }
            return true;
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!isFocused()) return false;
        if (SharedConstants.isAllowedCharacter(codePoint)) {
            writeText(Character.toString(codePoint));
            return true;
        }
        return false;
    }

    private void moveCursorToMouse(double mouseX, double mouseY) {
        String s = getVisibleText();
        int i = 1;
        double lx = /*getTransformedCoords().getX1()*/x;
        while (i <= s.length() && lx + /*RenderUtils.getPrintedStringWidth(getLocalMatrix(), font, s.substring(0, i))*/font.getStringWidth(s.substring(0, i)) < mouseX)
            ++i;
        setCursorPos(i - 1 + leftCorrection, shouldSelect());
    }

    int lastClick = 0;

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (isHovered()) {
            moveCursorToMouse(mouseX, mouseY);
            if (EventManager.getTick() - lastClick < 5)
                selectWord(true);
            lastClick = EventManager.getTick();
        }
        else
            finishInput(false);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragScrollX, double dragScrollY) {
        if (!isFocused() || !isHovered() || button != 0) return false;
        moveCursorToMouse(mouseX, mouseY);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (!isHovered() || scrollAmount == 0 || text.length() < characterPadding) return false;
        leftCorrection = MathHelper.clamp(leftCorrection + (scrollAmount < 0 ? characterPadding : -characterPadding), 0, text.length() - 1);
        return true;
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, Minecraft minecraft, int mouseX, int mouseY) {
        (isHovered ? BaseWidget.HOVERED_SELECTED_TEXTURE : BaseWidget.SELECTED_TEXTURE).blit(matrixStack, new Box2d(x, y, width, height), 0, TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION);
    }

    @Override
    public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int dy = (height - font.FONT_HEIGHT) / 2;
        int cursorPos = leftCorrection < cursor ? font.getStringWidth((text + " ").substring(leftCorrection, cursor)) : 0;
        if (selection != cursor) {
            int selectionPos = 0;
            if (selection >= leftCorrection)
                selectionPos = font.getStringWidth(text.substring(leftCorrection, selection));
            int start = Math.min(selectionPos, cursorPos);
            int end = Math.max(selectionPos, cursorPos);
            RenderUtils.drawBox(matrixStack, new Box2d(x + 4 + start, y + dy, end - start + 3, height - dy * 2), selectionColor, 0);
        }
        RenderUtils.drawString(matrixStack, font, getVisibleText(), new Box2d(x + 4, y + 1 + dy, width - 6, height - dy * 2), textColor, true, false);
        if (isFocused() && (EventManager.getTick() & 0x10) == 0x10 && leftCorrection <= cursor) {
            if (insertMode && cursor != text.length())
                RenderUtils.drawString(matrixStack, font, "|", new Box2d(x + cursorPos + 3, y + 1 + dy, width - 6, height - dy * 2), cursorColor, true, false);
            else
                RenderUtils.drawString(matrixStack, font, "_", new Box2d(x + cursorPos + 4, y + 1 + dy, width - 6, height - dy * 2), cursorColor, true, false);
        }
    }
}
