package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.limachi.dimensional_bags.client.render.screen.SimpleContainerScreen;
import javafx.util.Pair;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class StringListDropDownWidget extends ParentToggleWidget {

    protected int maxHeight;

    private static final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/widgets/buttons.png");
    private static final TextureCutout validate_texture = new TextureCutout(texture, 32, 40, 16, 16);
    private static final TextureCutout remove_texture = new TextureCutout(texture, 64, 40, 16, 16);

    private final boolean isEditor;
    private static int selected = -1;
    private boolean hasBar = false;
    private final ArrayList<Pair<TextWidget, ImageWidget>> entries = new ArrayList<>();

    private TextFieldWidget input = null;

    /**
     * behavior:
     * when closed, only a button with it's title as text (this button is still visible and usable to close the box in open mode)
     * when opened and edit mode: first line input box + validation, other lines: toggle entry + remove button + scroll bar (if needed)
     * when opened and view mode: all lines: entry (do nothing on click) + scroll bar (if needed)
     * we suggest to not use the list while it is given to this widget (only use the list for sync when you close the screen)
     */

    public StringListDropDownWidget(int x, int y, int width, int height, ITextComponent title, int maxHeight, SimpleContainerScreen<?> parentScreen, boolean isEditor, Collection<String> list) {
        super(x, y, width, height, title, height, parentScreen);
        this.maxHeight = maxHeight;
        this.isEditor = isEditor;
        if (isEditor) {
            input = new TextFieldWidget(x, y + height, width - 16, 16, MINECRAFT.fontRenderer, null, null, this::onValidation);
            addChild(input);
            addChild(new ImageWidget(x + width - 16, y + height, 16, 16, validate_texture){
                @Override
                public void onClick(double mouseX, double mouseY) {
                    onValidation(input);
                }
            }.enableButtonRenderBehavior(true));
        }
        for (String entry : list)
            addEntry(entry, false);
    }

    @Override
    public void addChild(BaseWidget widget) {
        heightOpened = Integer.min(maxHeight, widget.y + widget.getHeight());
        super.addChild(widget);
    }

    @Override
    public void removeChild(BaseWidget widget) {
        if (widget.y + widget.getHeight() >= heightOpened) {
            //should test if other widgets are still in the bottom of the opened parent
            heightOpened = Integer.max(heightClosed, widget.y - 1);
        }
        super.removeChild(widget);
    }

    private void addEntry(String str, boolean reset) {
        int yw = entries.size() > 0 ? entries.get(entries.size() - 1).getKey().y + 16 : y + heightClosed + 16;
        int dw = (isEditor ? 16 : 0) + (hasBar ? 16 : 0);
        Pair<TextWidget, ImageWidget> entry = new Pair<>(new TextWidget(x, yw, width - dw, 16, str){
            @Override
            public void onClick(double mouseX, double mouseY) {
                if (isEditor)
                    selectEntry(isSelected ? this : null);
            }
        }.enableToggleBehavior(true), isEditor ? new ImageWidget(x + width - (hasBar ? 16 : 0) - 16, yw, 16, 16, remove_texture){
            @Override
            public void onClick(double mouseX, double mouseY) {
                removeEntry(this);
            }
        }.enableButtonRenderBehavior(true) : null);
        addChild(entry.getKey());
        addChild(entry.getValue());
        entries.add(entry);
    }

    public Stream<String> finalEntries() { return entries.stream().map(p->p.getKey().text.getString()); }

    private void onValidation(TextFieldWidget field) {
        String input = field.getText();
        if (selected >= 0 && selected < entries.size()) {
            Pair<TextWidget, ImageWidget> entry = entries.get(selected);
            entry.getKey().text = new StringTextComponent(input);
        } else if (!input.isEmpty() && finalEntries().noneMatch(s->s.equals(input))) {
            addEntry(input, false);
        }
    }

    private void removeEntry(ImageWidget widget) {
        int f = 0;
        for (Pair<TextWidget, ImageWidget> entry : entries)
            if (entry.getValue() == widget)
                break;
            else
                ++f;
        TextWidget t = entries.get(f).getKey();
        ImageWidget b = entries.get(f).getValue();
        removeChild(t);
        removeChild(b);
        entries.remove(f);
        for (; f < entries.size(); ++f) {
            Pair<TextWidget, ImageWidget> entry = entries.get(f);
            entry.getKey().y -= 16;
            entry.getValue().y -= 16;
        }
    }

    public int getSelected() { return selected; }

    public void reselectEntry(int entry) {
        if (isEditor)
            for (int f = 0; f < entries.size(); ++f)
                entries.get(f).getKey().setSelected(f == entry);
    }

    private void selectEntry(TextWidget widget) {
        int f = 0;
        selected = -1;
        for (Pair<TextWidget, ImageWidget> entry : entries) {
            TextWidget k = entry.getKey();
            if (k == widget) {
                k.setSelected(true);
                selected = f;
                input.setText(entry.getKey().text.getString());
            }
            else
                k.setSelected(false);
            ++f;
        }
    }
}
