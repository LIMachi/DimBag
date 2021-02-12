package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.limachi.dimensional_bags.client.render.Vector2d;
import com.limachi.dimensional_bags.client.render.screen.BaseScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class Button extends Base {
    public static final ResourceLocation VANILLA_TEXTURE = new ResourceLocation(MOD_ID, "textures/widgets/button.png");

    public static final TextureCutout VANILLA_BACKGROUND = new TextureCutout(VANILLA_TEXTURE, 0, 20, 200, 20);
    public static final TextureCutout VANILLA_HOVER = new TextureCutout(VANILLA_TEXTURE, 0, 40, 200, 20);
    public static final TextureCutout VANILLA_SELECTED = new TextureCutout(VANILLA_TEXTURE, 0, 0, 200, 20);

    public static final ResourceLocation SMALL_BUTTON_TEXTURE = new ResourceLocation(MOD_ID,"textures/widgets/view_port.png");

    public static final TextureCutout SMALL_BUTTON__BACKGROUND = new TextureCutout(SMALL_BUTTON_TEXTURE, 11, 203, 11, 11);
    public static final TextureCutout SMALL_BUTTON__HOVER = new TextureCutout(SMALL_BUTTON_TEXTURE, 22, 203, 11, 11);
    public static final TextureCutout SMALL_BUTTON__SELECTED = new TextureCutout(SMALL_BUTTON_TEXTURE, 0, 203, 11, 11);

    public static final TextureCutout ARROW_RIGHT = new TextureCutout(SMALL_BUTTON_TEXTURE, 0, 214, 11, 11);
    public static final TextureCutout ARROW_DOWN = new TextureCutout(SMALL_BUTTON_TEXTURE, 11, 214, 11, 11);
    public static final TextureCutout ARROW_LEFT = new TextureCutout(SMALL_BUTTON_TEXTURE, 22, 214, 11, 11);
    public static final TextureCutout ARROW_UP = new TextureCutout(SMALL_BUTTON_TEXTURE, 33, 214, 11, 11);
    public static final TextureCutout VIEW_DRAGGER = new TextureCutout(SMALL_BUTTON_TEXTURE, 44, 214, 11, 11);

    protected final TextureCutout background;
    protected final TextureCutout hover;
    protected final TextureCutout selected;
    protected final Function<Integer, Boolean> onClick;

    /**
     * dynamic button that switch it's render and can call a function upon being pressed
     */
    public Button(BaseScreen<?> screen, Base parent, double x, double y, double width, double height, TextureCutout background, TextureCutout hover, TextureCutout selected, Function<Integer, Boolean> onClick) {
        super(screen, parent, x, y, width, height, true);
        this.background = background;
        this.hover = hover;
        this.selected = selected;
        if (onClick != null)
            this.onClick = onClick;
        else
            this.onClick = i->true;
    }

    /**
     * vanilla scaled button
     */
    public Button(BaseScreen<?> screen, Base parent, double x, double y, double width, double height, Function<Integer, Boolean> onClick) {
        this(screen, parent, x, y, width, height, VANILLA_BACKGROUND, VANILLA_HOVER, VANILLA_SELECTED, onClick);
    }

    /**
     * vanilla full button
     */
    public Button(BaseScreen<?> screen, Base parent, double x, double y, Function<Integer, Boolean> onClick) { this(screen, parent, x, y, 200, 20, onClick); }

    /**
     * my buttons used by scrollbar and viewport
     */
    public static Button smallButton(BaseScreen<?> screen, Base parent, double x, double y, TextureCutout topper, Function<Integer, Boolean> onClick) {
        Button b = new Button(screen, parent, x, y, 11, 11, SMALL_BUTTON__BACKGROUND, SMALL_BUTTON__HOVER, SMALL_BUTTON__SELECTED, onClick);
        if (topper != null)
            new Image(screen, b, 0, 0, 11, 11, topper);
        return b;
    }

    public static class DragButton extends Button {

        protected final BiFunction<Double, Double, Boolean> onDrag;

        public DragButton(BaseScreen<?> screen, Base parent, double x, double y, BiFunction<Double, Double, Boolean> onDrag) {
            super(screen, parent, x, y, 11, 11, SMALL_BUTTON__BACKGROUND, SMALL_BUTTON__HOVER, SMALL_BUTTON__SELECTED, null);
            this.onDrag = onDrag;
        }

        @Override
        public void onRender(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            super.onRender(matrixStack, mouseX, mouseY, partialTicks);
            VIEW_DRAGGER.blit(matrixStack, coords, screen.getBlitOffset(), true, TextureCutout.TextureApplicationPattern.STRETCH);
        }

        @Override
        public boolean onMouseDragged(double mouseX, double mouseY, int button, double initialDragX, double initialDragY, double dragScrollX, double dragScrollY) {
            if (onDrag != null) {
                Vector2d v = getUVpos(mouseX, mouseY);
                return onDrag.apply(v.x, v.y);
            }
            return false;
        }
    }

    @Override
    public void onRender(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        TextureCutout texture = background;
        if (isDragged())
            texture = selected;
        else if (isHovered())
            texture = hover;
        texture.blit(matrixStack, coords, screen.getBlitOffset(), true, TextureCutout.TextureApplicationPattern.MIDDLE_EXPANSION);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        return onClick != null && isHovered() && onClick.apply(button);
    }
}
