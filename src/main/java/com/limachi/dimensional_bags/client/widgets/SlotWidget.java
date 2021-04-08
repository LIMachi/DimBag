package com.limachi.dimensional_bags.client.widgets;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.client.render.TextureCutout;
import com.limachi.dimensional_bags.utils.ReflectionUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Slot;

import java.util.function.Function;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

public class SlotWidget extends Base {

    public static final TextureCutout VANILLA_SLOT = new TextureCutout(SLOT, SLOT_SIZE_X, SLOT_SIZE_Y, 0, 0, SLOT_SIZE_X, SLOT_SIZE_Y);
    public static final int VANILLA_SLOT_SELECTION_TINT = -2130706433;

    protected final Slot slot;
    protected final TextureCutout background;
    protected final Function<Integer, Boolean> onClick;

    public SlotWidget(Slot slot) {
        this(slot, slot.xPos, slot.yPos, true, VANILLA_SLOT, i->false);
    }

    public SlotWidget(Slot slot, int x, int y) {
        this(slot, x, y, true, VANILLA_SLOT, i->false);
    }

    public SlotWidget(Slot slot, int x, int y, boolean isCut, TextureCutout background, Function<Integer, Boolean> onClick) {
        super(new Box2d(x, y, SLOT_SIZE_X, SLOT_SIZE_Y), isCut);
        if (slot.xPos != x)
            ReflectionUtils.setField(slot, "xPos", "field_75223_e", x); //hacky way of forcing a new position for the slot (to not have to change the click system)
        if (slot.yPos != y)
            ReflectionUtils.setField(slot, "yPos", "field_75221_f", y);
        this.slot = slot;
        this.background = background;
        this.onClick = onClick;
    }

    @Override
    public void onRender(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (slot.isEnabled())
            ReflectionUtils.runMethod(getScreen(), "moveItems", "func_238746_a_", matrixStack, slot);
        background.blit(matrixStack, coords, getScreen().getBlitOffset(), TextureCutout.TextureApplicationPattern.TILE);
        screen(s->s.setBlitOffset(200));
        Minecraft.getInstance().getItemRenderer().zLevel = 200.0F;
        Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(Minecraft.getInstance().player, slot.getStack(), slot.xPos + 1, slot.yPos + 1);
        Minecraft.getInstance().getItemRenderer().renderItemOverlayIntoGUI(getScreen().getFont(), slot.getStack(), slot.xPos + 1, slot.yPos + 1, null);
        Minecraft.getInstance().getItemRenderer().zLevel = 0.0F;
        screen(s->s.setBlitOffset(0));
        if (isHovered() && slot.isEnabled())
            RenderUtils.drawBox(matrixStack, coords.copy().move(1, 1).expand(-2, -2), VANILLA_SLOT_SELECTION_TINT, 700);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button) {
        return onClick != null && isHovered() && onClick.apply(button);
    }
}
