package com.limachi.dim_bag.menus.slots;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.utils.SimpleTank;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.IFluidTank;

import java.util.function.Function;
import java.util.function.Supplier;

public class BagTankSlot extends TankSlot {
    public static final ResourceLocation SELECTED_FLUID_SLOT = new ResourceLocation(DimBag.MOD_ID, "textures/screen/slots/selected_fluid_slot_overlay.png");

    public int bag;
    public BlockPos tank;
    LazyOptional<SimpleTank> tankAccess;
    Supplier<Boolean> isSelected;

    public BagTankSlot(int xPosition, int yPosition, Function<TankSlot, Boolean> isActive, Supplier<Boolean> isSelected) {
        super(Integer.MAX_VALUE, xPosition, yPosition, isActive);
        bag = 0;
        tank = null;
        this.isSelected = isSelected;
    }

    public BagTankSlot(int bag, BlockPos tank, int xPosition, int yPosition, Function<TankSlot, Boolean> isActive, Supplier<Boolean> isSelected) {
        super(()->SimpleTank.NULL_TANK, xPosition, yPosition, isActive);
        this.bag = bag;
        this.tank = tank;
        this.isSelected = isSelected;
        BagsData.runOnBag(bag, b->tankAccess = b.tankHandle(tank));
    }

    @Override
    public IFluidTank getTankHandler() {
        if (bag > 0 && (tankAccess == null || !tankAccess.isPresent()))
            BagsData.runOnBag(bag, b->tankAccess = b.tankHandle(tank));
        if (tankAccess != null)
            return tankAccess.orElse(SimpleTank.NULL_TANK);
        return bag == 0 ? super.getTankHandler() : SimpleTank.NULL_TANK;
    }

    public void changeTankServerSide(BlockPos tank) {
        this.tank = tank;
        tankAccess = null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderSlot(GuiGraphics gui) {
        super.renderSlot(gui);
        if (isSelected.get())
            gui.blit(SELECTED_FLUID_SLOT, x - 1, y - 1, 100, 0, 0, 18, 18, 18, 18);
    }
}
