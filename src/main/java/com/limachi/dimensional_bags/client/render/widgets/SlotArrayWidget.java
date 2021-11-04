package com.limachi.dimensional_bags.client.render.widgets;

import com.limachi.dimensional_bags.client.render.screen.SimpleContainerScreen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.inventory.container.Slot;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
/*
public class SlotArrayWidget extends BaseWidget {
    private static int[] widthHeight(List<Slot> slots) {
        int width = 0;
        int height = 0;
        for (Slot slot : slots) {
            width = Math.max(slot.x + 18, width);
            height = Math.max(slot.y + 18, height);
        }
        return new int[]{width, height};
    }

    protected List<Slot> slots;
    protected List<Integer[]> originalSlotPos;

    private SlotArrayWidget(int x, int y, List<Slot> slots, int[] widthHeight) {
        super(x, y, widthHeight[0], widthHeight[1]);
        this.slots = slots;
        originalSlotPos = new ArrayList<>();
        for (Slot slot : slots)
            originalSlotPos.add(new Integer[]{slot.x, slot.y});
        enableClickBehavior = false;
        renderTitle = false;
        renderStandardBackground = false;
        isToggle = false;
    }

    public SlotArrayWidget(int x, int y, List<Slot> slots) { this(x, y, slots, widthHeight(slots)); }

    @Override
    public void renderButton(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        SimpleContainerScreen<?> screen = getScreen();
        if (screen != null)
            for (Slot slot : slots)
                screen.renderSlot(matrixStack, slot);
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }

//    @Override
//    public void init() {
//        int rx = x();
//        int ry = y();
//        for (int i = 0; i < slots.size(); ++i) {
//            slots.get(i).x = rx + originalSlotPos.get(i)[0];
//            slots.get(i).y = ry + originalSlotPos.get(i)[1];
//        }
//        super.init();
//    }
}
*/