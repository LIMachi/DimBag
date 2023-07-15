package com.limachi.dim_bag.client.widgets;

import com.limachi.lim_lib.LimLib;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TextAndImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class Builders {
    public static final ResourceLocation WIDGETS = new ResourceLocation(LimLib.COMMON_ID, "textures/screen/widgets.png");
    public static final int CHECK_MARK_X = 28;
    public static final int CHECK_MARK_WIDTH = 8;
    public static final int CHECK_MARK_HEIGHT = 8;

    private static final Minecraft mc = Minecraft.getInstance();

    public static <T extends GuiEventListener & Renderable> void addWidget(Screen screen, T widget) {
        screen.renderables.add(widget);
        ((List<GuiEventListener>)screen.children()).add(widget);
    }

    //text box at top + validator button
    //entries are buttons (for selection) and have a remove button on the left
    //on the right of the entries there is a scroll bar (for if entries take to much place)
    //by default, nothing is selected
    //by default, the width and height is not modifiable
    //the array list of values is cloned on creation (by design)

    public static class EditableTextListHandler {
        final protected ArrayList<String> texts;
        final protected ArrayList<Pair<TextAndImageButton, Button>> entries = new ArrayList<>();
        protected int scroll = 0;
        final protected TextEdit editor;
        final protected TextAndImageButton validate;
        final protected VerticalSlider slider;
        final protected Screen screen;
        final protected Consumer<EditableTextListHandler> onChange;

        protected EditableTextListHandler(Screen screen, int x, int y, int w, int h, List<String> values, Consumer<EditableTextListHandler> onChange) {
            this.screen = screen;
            editor = new TextEdit(mc.font, x + 1, y + 1, w - 18, 14, "", t->this.validateText());
            validate = TextAndImageButton.builder(Component.empty(), WIDGETS, t->this.validateText()).texStart(CHECK_MARK_X, CHECK_MARK_HEIGHT).yDiffTex(0).textureSize(256, 256).usedTextureSize(CHECK_MARK_WIDTH, CHECK_MARK_HEIGHT).offset(0, 4).build();
            validate.setX(x + w - 16);
            validate.setY(y);
            validate.setWidth(16);
            validate.setHeight(16);
            int slots = (h - 16) / 16;
            slider = new VerticalSlider(x + w - 16, y + 16, 16, slots * 16 + 1, 0., s->this.scroll(s.getValue()));
            addWidget(screen, editor);
            addWidget(screen, validate);
            addWidget(screen, slider);
            for (int i = 0; i < slots; ++i) {
                TextAndImageButton remove = TextAndImageButton.builder(Component.empty(), WIDGETS, this::remove).texStart(CHECK_MARK_X, 0).yDiffTex(0).textureSize(256, 256).usedTextureSize(CHECK_MARK_WIDTH, CHECK_MARK_HEIGHT).offset(0, 4).build();
                remove.setX(x);
                remove.setY(y + 16 + 16 * i);
                remove.setWidth(16);
                remove.setHeight(16);
                Button entry = Button.builder(i < values.size() ? Component.literal(values.get(i)) : Component.empty(), this::select).bounds(x + 16, y + 16 + 16 * i, w - 32, 16).build();
                entries.add(new Pair<>(remove, entry));
                addWidget(screen, remove);
                addWidget(screen, entry);
            }
            texts = new ArrayList<>(values);
            this.onChange = onChange;
        }

        protected void validateText() {
            String text = editor.getValue();
            if (text.isBlank() || texts.contains(text)) return;
            int i = texts.size();
            texts.add(text);
            if (i >= scroll && i < scroll + entries.size())
                entries.get(i + scroll).getSecond().setMessage(Component.literal(text));
            if (onChange != null)
                onChange.accept(this);
        }

        protected void scroll(double v) {
            if (texts.size() <= entries.size()) {
                scroll = 0;
                return;
            }
            scroll = (int)Math.round(v * (texts.size() - entries.size()));
            for (int i = 0; i < entries.size(); ++i)
                entries.get(i).getSecond().setMessage(Component.literal(texts.get(i + scroll)));
        }

        protected void remove(Button remove) {
            int i = 0;
            while (entries.get(i).getFirst() != remove)
                ++i;
            if (i + scroll < texts.size()) {
                texts.remove(i + scroll);
                while (i < entries.size()) {
                    entries.get(i).getSecond().setMessage(i + scroll >= 0 && i + scroll < texts.size() ? Component.literal(texts.get(i + scroll)) : Component.empty());
                    ++i;
                }
            }
            if (onChange != null)
                onChange.accept(this);
        }

        protected void select(Button entry) {
            editor.setValue(entry.getMessage().getString());
            screen.setFocused(editor);
        }

        public List<String> getEntries() { return texts; }
    }

    public static void editableTextList(Screen screen, int x, int y, int w, int h, List<String> values, Consumer<EditableTextListHandler> onChange) {
        if (w < 80) //minimum of 16 (remove button) + 16 (scroll bar/validate button) + arbitrary 3 blocks of 16 for text
            w = 80;
        if (h < 48) //minimum of 16 (text editor) + 2 * 16 (2 entries + scroll bar space)
            h = 48;
        new EditableTextListHandler(screen, x, y, w, h, values, onChange);
    }
}
