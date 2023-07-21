package com.limachi.dim_bag.menus.slots;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.utils.FluidItem;
import com.limachi.dim_bag.utils.SimpleTank;
import com.limachi.lim_lib.render.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

public class TankSlot extends Slot {
    protected Function<TankSlot, Boolean> isActive;
    Supplier<IFluidTank> tank;

    public TankSlot(int capacity, int xPosition, int yPosition, Function<TankSlot, Boolean> isActive) {
        super(new SimpleContainer(0), 0, xPosition, yPosition);
        final SimpleTank inner = new SimpleTank(capacity);
        tank = ()->inner;
        this.isActive = isActive;
    }

    public TankSlot(Supplier<IFluidTank> tank, int xPosition, int yPosition, Function<TankSlot, Boolean> isActive) {
        super(new SimpleContainer(0), 0, xPosition, yPosition);
        this.tank = tank;
        this.isActive = isActive;
    }

    public IFluidTank getTankHandler() {
        return tank.get();
    }

    @Nonnull
    @Override
    public ItemStack getItem() {
        FluidStack fluid = getFluid();
        if (fluid.isEmpty())
            return ItemStack.EMPTY;
        return FluidItem.fromFluid(fluid);
    }

    @Override
    public void set(ItemStack stack) {
        if (stack.getItem() instanceof FluidItem) {
            FluidStack fluid = FluidItem.getHandler(stack).getFluidInTank(0);
            FluidStack local = getTankHandler().getFluid();
            if (fluid.isFluidEqual(local)) {
                if (fluid.getAmount() > local.getAmount()) {
                    fluid.setAmount(fluid.getAmount() - local.getAmount());
                    getTankHandler().fill(fluid, IFluidHandler.FluidAction.EXECUTE);
                } else if (fluid.getAmount() < local.getAmount())
                    getTankHandler().drain(local.getAmount() - fluid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            } else {
                if (!local.isEmpty())
                    getTankHandler().drain(local.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                if (!fluid.isEmpty())
                    getTankHandler().fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    @Override
    public boolean hasItem() { return isActive.apply(this) && getTankHandler().getFluidAmount() > 0; }

    @Override
    public boolean isActive() { return isActive.apply(this); }

    @Override
    public boolean isHighlightable() { return isActive.apply(this); }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) { return false; }

    @Override
    public boolean mayPickup(@Nonnull Player playerIn) { return false; }

    @Override
    @Nonnull
    public ItemStack remove(int amount) { return ItemStack.EMPTY; }

    public FluidStack getFluid() { return getTankHandler().getFluid(); }

    public int getCapacity() { return getTankHandler().getCapacity(); }

    @OnlyIn(Dist.CLIENT)
    public void renderSlot(GuiGraphics gui) { FluidRenderer.renderSlot(gui, this); }

    @OnlyIn(Dist.CLIENT)
    public static class FluidRenderer {
        private static final Minecraft mc = Minecraft.getInstance();
        private static final ResourceLocation missing_texture = MissingTextureAtlasSprite.getLocation();

        public static final ResourceLocation FLUID_SLOT = new ResourceLocation(DimBag.MOD_ID, "textures/screen/slots/fluid_slot.png");
        public static final ResourceLocation FLUID_SLOT_OVERLAY = new ResourceLocation(DimBag.MOD_ID, "textures/screen/slots/fluid_slot_overlay.png");

        public static void renderFluid(GuiGraphics gui, FluidStack fluidStack, int depth, int x, int y, int w, int h) {
            if (fluidStack == null || fluidStack.isEmpty() || fluidStack.getFluid() == null) return;
            IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(fluidStack.getFluid());
            TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(renderProperties.getStillTexture(fluidStack));
            if (!missing_texture.equals(sprite.atlasLocation())) {
                Vector4f color = RenderUtils.expandColor(renderProperties.getTintColor(fluidStack), false);
                gui.blit(x, y, depth, w, h, sprite, color.x, color.y, color.z, color.w); //FIXME: should use a tiling technique instead of stretching the texture
            }
        }

        public static void renderSlot(GuiGraphics gui, TankSlot slot) {
            RenderSystem.enableBlend();
            gui.blit(FLUID_SLOT, slot.x - 1, slot.y - 1, 100, 0, 0, 18, 18, 18, 18);
            FluidStack fluid = slot.getFluid();
            renderFluid(gui, fluid, 150, slot.x, slot.y, 16, 16);
            gui.blit(FLUID_SLOT_OVERLAY, slot.x - 1, slot.y - 1, 200, 0, 0, 18, 18, 18, 18);
            int amountInMB = fluid.getAmount();
            if (amountInMB > 0) {
                String amount = amountInMB >= 1000 ? (amountInMB / 1000) + I18n.get("screen.fluid.bucket_acronym") : amountInMB + I18n.get("screen.fluid.milli_bucket_acronym");
                PoseStack pose = gui.pose();
                pose.pushPose();
                pose.translate(slot.x, slot.y, 250);
                if (amount.length() > 3) {
                    pose.scale(.5f, .5f, 1f);
                    gui.drawString(mc.font, amount, 31 - mc.font.width(amount), 23, 16777215);
                } else
                    gui.drawString(mc.font, amount, 17 - mc.font.width(amount), 9, 16777215);
                pose.popPose();
            }
            RenderSystem.disableBlend();
        }
    }
}
