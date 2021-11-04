package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.TextureCutout;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class ToggleWidget extends ImageWidget {

    private static final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/widgets/buttons.png");
    private static final TextureCutout validate_texture = new TextureCutout(texture, 32, 40, 16, 16);

    protected final Consumer<ToggleWidget> onStateChange;

    public ToggleWidget(int x, int y, int width, int height, boolean initialState, Consumer<ToggleWidget> onStateChange) {
        super(x, y, width, height, validate_texture);
        buttonRender = true;
        isToggle = true;
        this.onStateChange = onStateChange;
        this.isSelected = initialState;
    }

    @Override
    public void onClick(double mouseX, double mouseY) { onStateChange.accept(this); }

    public static class MutuallyExclusiveToggleWidget extends ToggleWidget {

        public MutuallyExclusiveToggleWidget(int x, int y, int width, int height, boolean initialState, Consumer<ToggleWidget> onStateChange, int group_id) {
            super(x, y, width, height, initialState, onStateChange);
            this.group_id = group_id;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (isSelected)
                runOnGroup(group_id, w->{
                    if (w != this && w instanceof MutuallyExclusiveToggleWidget) {
                        w.setSelected(false);
                        ((MutuallyExclusiveToggleWidget)w).onStateChange.accept(this);
                    }
                });
            super.onClick(mouseX, mouseY);
        }
    }
}
