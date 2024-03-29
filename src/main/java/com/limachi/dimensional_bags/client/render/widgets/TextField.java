package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.client.render.screen.BaseScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import javafx.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;

public class TextField extends Base {

    protected FontRenderer font;
    protected String text = "";
    protected int selection = 0;
    protected int cursor = 0;
    protected int leftCorrection = 0;
    protected BiFunction<String, String, Boolean> validateText;
    protected Consumer<String> onFinishInput;
    protected boolean insertMode = true;
    public int textColor = 0xFFFFFFFF;
    public int selectionColor = 0x7744CCCC;
    public int cursorColor = 0xFFAAAAAA;
    public int characterPadding = 3; //how close to the border the cursor can be before correction
    protected ArrayList<Pair<Integer, String>> history = new ArrayList<>(); //history only persist for this instance of the widget, for advanced history, a variant of this widget could be made
    protected int historyPage = -1;

    public TextField(BaseScreen<?> screen, Base parent, double x, double y, double width, double height, @Nullable String initialText, @Nullable BiFunction<String, String, Boolean> validateText, @Nullable Consumer<String> onFinishInput) {
        super(screen, parent, x, y, width, height, true);
        this.font = screen.getFont();
        if (initialText != null)
            setText(initialText);
        pushToHistory();
        this.onFinishInput = onFinishInput;
        this.validateText = validateText;
    }

    public void finishInput(boolean withCallback) {
        if (withCallback && onFinishInput != null)
            onFinishInput.accept(text);
        changeFocus(false);
        selection = cursor;
        changeFocus(false);
    }

    protected void setCursorPos(int pos) {
        cursor = MathHelper.clamp(pos, 0, text.length());
        if (text.length() != 0) {
            if (cursor - characterPadding < leftCorrection)
                leftCorrection = MathHelper.clamp(cursor - characterPadding, 0, text.length() - 1);
            else {
                String vt = getVisibleText();
                if (cursor + characterPadding > leftCorrection + vt.length() && getFont().getStringWidth(vt) + getFont().getStringWidth("|_|_") >= coords.getWidth())
                    leftCorrection = MathHelper.clamp(cursor - vt.length() + characterPadding, 0, text.length() - 1);
            }
        }
        if (!Screen.hasShiftDown() && !isDragged())
            selection = cursor;
    }

    public void setText(String text) {
        if (validateText == null || validateText.apply(text, this.text)) {
            this.text = text;
            if (cursor > text.length())
                setCursorPos(text.length());
        }
    }

    public String getText() { return text; }

    public String getSelectedText() {
        return text.length() == 0 || selection == cursor ? "" : text.substring(Math.min(selection, cursor), Math.max(selection, cursor));
    }

    public String getVisibleText() {
        return getFont().func_238412_a_(text.substring(leftCorrection), (int)coords.getWidth());
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
            setCursorPos(p.getKey());
        }
    }

    public void writeText(String add) {
        add = SharedConstants.filterAllowedCharacters(add);
        String start = text.substring(0, Math.min(selection, cursor));
        String end = text.substring(Math.max(selection, cursor));
        if (!insertMode)
            end = end.length() > add.length() ? end.substring(add.length()) : "";
        setText(start + add + end);
        setCursorPos((start + add).length());
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
            setCursorPos(newPos);
            selection = cursor;
        }
        else
            text = text.substring(0, cursor) + text.substring(newPos);
    }

    @Override
    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
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
                    setCursorPos(cursor + 1);
                    return true;
                case GLFW_KEY_LEFT:
                    setCursorPos(cursor - 1);
                    return true;
                case GLFW_KEY_HOME:
                    setCursorPos(0);
                    return true;
                case GLFW_KEY_END:
                    setCursorPos(text.length());
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
    public boolean onCharTyped(char codePoint, int modifiers) {
        if (!isFocused()) return false;
        if (SharedConstants.isAllowedCharacter(codePoint)) {
            writeText(Character.toString(codePoint));
            return true;
        }
        return false;
    }

    public FontRenderer getFont() {
        if (font == null)
            font = screen.getFont();
        return font;
    }

    private void moveCursorToMouse(double mouseX, double mouseY) {
        String s = getVisibleText();
        int i = 1;
        double lx = getTransformedCoords().getX1();
        while (i <= s.length() && lx + RenderUtils.getPrintedStringWidth(getLocalMatrix(), font, s.substring(0, i)) < mouseX)
            ++i;
        setCursorPos(i - 1 + leftCorrection);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered() && button == 0) {
            moveCursorToMouse(mouseX, mouseY);
            changeFocus(true);
        }
        else
            finishInput(false);
        return isFocused();
    }

    @Override
    public boolean onMouseDragged(double mouseX, double mouseY, int button, double initialDragX, double initialDragY, double dragScrollX, double dragScrollY) {
        if (!isFocused() || !isHovered() || button != 0) return false;
        moveCursorToMouse(mouseX, mouseY);
        return true;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        if (!isHovered() || scrollAmount == 0 || text.length() < characterPadding) return false;
        leftCorrection = MathHelper.clamp(leftCorrection + (scrollAmount < 0 ? characterPadding : -characterPadding), 0, text.length() - 1);
        return true;
    }

    @Override
    public void onRender(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int cursorPos = leftCorrection < cursor ? getFont().getStringWidth((text + " ").substring(leftCorrection, cursor)) : 0;
        if (selection != cursor) {
            int selectionPos = 0;
            if (selection >= leftCorrection)
                selectionPos = getFont().getStringWidth(text.substring(leftCorrection, selection));
            int start = Math.min(selectionPos, cursorPos);
            int end = Math.max(selectionPos, cursorPos);
            RenderUtils.drawBox(matrixStack, new Box2d(start, 0, end - start + 1, getFont().FONT_HEIGHT), selectionColor);
        }
        RenderUtils.drawString(matrixStack, getFont(), getVisibleText(), coords.copy().move(1, 1), textColor, true, false);
        if (isFocused() && (screen.getTick() & 0x10) == 0x10 && leftCorrection < cursor) {
            if (insertMode && cursor != text.length())
                RenderUtils.drawString(matrixStack, getFont(), "|", coords.copy().move(cursorPos, 1), cursorColor, true, false);
            else
                RenderUtils.drawString(matrixStack, getFont(), "_", coords.copy().move(cursorPos + 1, 1), cursorColor, true, false);
        }
    }
}
