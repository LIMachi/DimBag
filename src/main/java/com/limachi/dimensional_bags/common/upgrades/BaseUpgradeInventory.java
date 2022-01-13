package com.limachi.dimensional_bags.common.upgrades;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagBattery.BatteryTileEntity;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagTank.TankTileEntity;
import com.limachi.dimensional_bags.lib.common.tileentities.IInstallUpgradeTE;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot.SlotTileEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class BaseUpgradeInventory extends Item {

    public final UpgradeTarget target;
    protected boolean isVoid = false;
    public static final Properties DEFAULT_PROPS = new Properties().tab(DimBag.ITEM_GROUP);

    public BaseUpgradeInventory(UpgradeTarget target, Properties props) {
        super(props);
        this.target = target;
    }

    abstract public String upgradeName();

    public enum UpgradeTarget {
        PILLAR,
        FOUNTAIN,
        BATTERY
    }

    @Override
    public @Nonnull
    ActionResultType useOn(@Nonnull ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (DimBag.isServer(context.getLevel()) && player != null && !(player instanceof FakePlayer) && KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) {
            ItemStack stack = context.getItemInHand();
            UpgradeTarget target = ((BaseUpgradeInventory)stack.getItem()).target;
            if (stack.getItem() instanceof BaseUpgradeInventory && stack.getCount() > 0) {
                TileEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
                if ((te instanceof SlotTileEntity && target == UpgradeTarget.PILLAR)
                        || (te instanceof TankTileEntity && target == UpgradeTarget.FOUNTAIN)
                        || (te instanceof BatteryTileEntity && target == UpgradeTarget.BATTERY)) {
                    player.setItemInHand(context.getHand(), ((IInstallUpgradeTE)te).installUpgrades(player, stack));
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return super.useOn(context);
    }

    /**
     * void excess item/fluid/energy, used by void and creative upgrades
     */
    public boolean isVoid() { return isVoid; }

    public abstract boolean applySequentialUpgrades(int count, Object target);
    abstract public boolean canBeInstalled();

    @Nonnull
    protected IFormattableTextComponent upgradeInfo() {
        return new TranslationTextComponent("tooltip." + target.name() + ".upgrades." + upgradeName());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (!((BaseUpgradeInventory) stack.getItem()).canBeInstalled())
            tooltip.add(new TranslationTextComponent("tooltip.upgrade.disabled").withStyle(TextFormatting.BOLD, TextFormatting.RED));
        if (Screen.hasShiftDown()) {
            tooltip.add(upgradeInfo().withStyle(TextFormatting.YELLOW));
            tooltip.add(new TranslationTextComponent("tooltip." + target.name() + ".upgrades.install_info").withStyle(TextFormatting.AQUA));
        }  else
            tooltip.add(new TranslationTextComponent("tooltip.shift_for_info"));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
