package com.limachi.dim_bag.client.widgets;

import com.limachi.dim_bag.utils.LevenshteinDictionarySorter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Consumer;

public class TextEditWithSuggestions extends TextEdit {

    protected final Screen screen = mc.screen;
    protected final LevenshteinDictionarySorter dictionary;
    protected final int dictionaryHeight;
    protected final ScreenRectangle suggestionArea;
    protected int suggestionScroll = 0;
    protected int maxScroll = 0;
    protected boolean enforceSuggestion = false;

    protected ScreenRectangle prepareSuggestionArea() {
        boolean left = getX() <= screen.width / 2;
        if (getY() <= screen.height / 2) {
            int spaceLeft = screen.height - getY() - 10;
            spaceLeft -= spaceLeft % (mc.font.lineHeight + 2);
            int size = Math.min(spaceLeft, dictionaryHeight + 2);
            maxScroll = dictionary.size() - size / (mc.font.lineHeight + 2);
            return new ScreenRectangle(getX() + (left ? getWidth() : -getWidth()), getY(), getWidth(), size);
        } else {
            int spaceLeft = getY() - 10;
            spaceLeft -= spaceLeft % (mc.font.lineHeight + 2);
            int size = Math.min(spaceLeft, dictionaryHeight + 2);
            maxScroll = dictionary.size() - size / (mc.font.lineHeight + 2);
            return new ScreenRectangle(getX() + (left ? getWidth() : -getWidth()), getY() - size, getWidth(), size);
        }
    }

    public TextEditWithSuggestions forceSuggestion(boolean state) {
        this.enforceSuggestion = state;
        return this;
    }

    @Override
    public void setFocused(boolean state) {
        if (enforceSuggestion && !state)
            if (!dictionary.getSortedDictionary().contains(getValue()))
                setValue(dictionary.getSortedDictionary().get(0));
        super.setFocused(state);
    }

    public TextEditWithSuggestions(Font font, int x, int y, int w, int h, String value, Consumer<TextEdit> onFinish, String ... dictionary) {
        super(font, x, y, w, h, value, onFinish);
        this.dictionary = new LevenshteinDictionarySorter.Builder().addEntries(dictionary).setCosts(3, 1, 2).build();
        this.dictionary.sortAgainst(value);
        dictionaryHeight = this.dictionary.size() * (mc.font.lineHeight + 2);
        setResponder(this.dictionary::sortAgainst);
        suggestionArea = prepareSuggestionArea();
    }

    public TextEditWithSuggestions(Font font, int x, int y, int w, int h, String value, Consumer<TextEdit> onFinish, Collection<String> dictionaryEntries) {
        super(font, x, y, w, h, value, onFinish);
        dictionary = new LevenshteinDictionarySorter.Builder().addEntries(dictionaryEntries).setCosts(3, 1, 2).build();
        dictionary.sortAgainst(value);
        dictionaryHeight = dictionary.size() * (mc.font.lineHeight + 2);
        setResponder(this.dictionary::sortAgainst);
        suggestionArea = prepareSuggestionArea();
    }

    protected int inSuggestionArea(double mouseX, double mouseY) {
        if (isFocused() && mouseX >= suggestionArea.left() && mouseX <= suggestionArea.right() && mouseY >= suggestionArea.top() && mouseY <= suggestionArea.bottom())
            return (int)mouseY - suggestionArea.top();
        return -1;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return super.clicked(mouseX, mouseY) || inSuggestionArea(mouseX, mouseY) != -1;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return clicked(mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int y = inSuggestionArea(mouseX, mouseY);
        if (y != -1) {
            int l = y / (mc.font.lineHeight + 2);
            setValue(dictionary.getSortedDictionary().get(l + suggestionScroll));
            suggestionScroll = 0;
        } else
            super.onClick(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (inSuggestionArea(mouseX, mouseY) != -1)
            suggestionScroll = (int)Mth.clamp(suggestionScroll - scroll, 0, maxScroll);
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean isFocused() { return super.isFocused() || this.equals(screen.getFocused()); }

    @Override
    public void renderWidget(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(gui, mouseX, mouseY, partialTick);
        if (isFocused()) {
            gui.enableScissor(suggestionArea.left(), suggestionArea.top(), suggestionArea.right(), suggestionArea.bottom());
            gui.fill(suggestionArea.left(), suggestionArea.top(), suggestionArea.right(), suggestionArea.bottom(), 0xBB202599);
            int space = mc.font.lineHeight + 2;
            if (suggestionArea.left() <= getX())
                for (int i = 0; i * space < suggestionArea.height() - 2; ++i) {
                    String text = dictionary.getSortedDictionary().get(i + suggestionScroll);
                    gui.drawString(mc.font, Component.literal(text), suggestionArea.right() - mc.font.width(text) - 2, suggestionArea.top() + space * i + 2, -1);
                }
            else
                for (int i = 0; i * space < suggestionArea.height() - 2; ++i)
                    gui.drawString(mc.font, Component.literal(dictionary.getSortedDictionary().get(i + suggestionScroll)), suggestionArea.left() + 2, suggestionArea.top() + space * i + 2, -1);
            gui.disableScissor();
        }
    }
}
