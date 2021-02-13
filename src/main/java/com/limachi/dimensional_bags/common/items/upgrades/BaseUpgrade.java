package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Mod.EventBusSubscriber
public abstract class BaseUpgrade extends Item {

    public BaseUpgrade(Item.Properties props) { super(props); }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().getItem() instanceof BaseUpgrade && event.getItemStack().getCount() > 0 && event.getTarget() instanceof BagEntity) {
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.SUCCESS);
            if (DimBag.isServer(event.getWorld()))
                event.getPlayer().setHeldItem(event.getHand(), installUpgrades(event.getPlayer(), ((BagEntity) event.getTarget()).getEyeId(), event.getItemStack()));
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public @Nonnull ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (DimBag.isServer(worldIn) && !(playerIn instanceof FakePlayer) && KeyMapController.KeyBindings.SNEAK_KEY.getState(playerIn)) {
            ItemStack stack = playerIn.getHeldItem(handIn);
            if (stack.getItem() instanceof BaseUpgrade && stack.getCount() > 0) {
                int eyeId = Bag.getBag(playerIn, 0);
                if (eyeId > 0)
                    return ActionResult.resultSuccess(installUpgrades(playerIn, eyeId, stack));
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public @Nonnull ActionResultType onItemUse(@Nonnull ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (DimBag.isServer(context.getWorld()) && player != null && !(player instanceof FakePlayer) && KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) {
            ItemStack stack = context.getItem();
            if (stack.getItem() instanceof BaseUpgrade && stack.getCount() > 0) {
                int eyeId = Bag.getBag(player, 0);
                if (eyeId > 0) {
                    player.setHeldItem(context.getHand(), installUpgrades(player, eyeId, stack));
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return super.onItemUse(context);
    }

    private static ItemStack installUpgrades(PlayerEntity player, int eyeId, ItemStack stack) {
        BaseUpgrade upgrade = (BaseUpgrade) stack.getItem();
        if (!upgrade.canBeInstalled()) {
            player.sendStatusMessage(new TranslationTextComponent("items.upgrades.trying_to_install_disabled_upgrade").mergeStyle(TextFormatting.BOLD, TextFormatting.RED), true);
            return stack;
        }
        ItemStack out = stack.copy();
        int nc = ((BaseUpgrade) stack.getItem()).installUpgrade(eyeId, stack.getCount());
        if (nc != stack.getCount()) {
            out.shrink(nc);
            UpgradeManager.execute(eyeId, um->{um.getUpgradesCountMap().put(upgrade.upgradeName(), um.getUpgradesCountMap().getOrDefault(upgrade.upgradeName(), 0) + stack.getCount() - nc); um.markDirty();});
            player.sendStatusMessage(new TranslationTextComponent("items.upgrades.installed_X_upgrades", stack.getCount() - nc, upgrade.upgradeName()), true);
        } else
            player.sendStatusMessage(new TranslationTextComponent("items.upgrades.max_upgrades_reached", upgrade.upgradeName()), true);
        return out;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (stack.getItem() instanceof BaseUpgrade && !((BaseUpgrade)stack.getItem()).canBeInstalled())
            tooltip.add(new TranslationTextComponent("items.upgrades.disable_upgrade").mergeStyle(TextFormatting.BOLD, TextFormatting.RED));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    abstract public boolean canBeInstalled();

    /**
     * usually the item name of the upgrade
     * @return
     */
    abstract public String upgradeName();

    /**
     * @param eyeId on which bag the upgrades are too be installed
     * @param qty how many of this upgrades the player is trying to install at once
     * @return the number of upgrades left after installation
     */
    abstract public int installUpgrade(int eyeId, int qty);

    /**
     * if this upgrade is installed and active, this function will be called every X ticks (by default every tick)
     * @param eyeId which bag is trying to print information on hud
     * @param player which player is using the bag and should receive a hud update
     * @param window
     * @param matrixStack
     * @param partialTicks
     */
    public void onRenderHud(int eyeId, PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {}

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) { //might be used at some point to add hud ellements
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            PlayerEntity player = DimBag.getPlayer();
            ItemStack mainHand = player.getHeldItemMainhand();
            if (!mainHand.isEmpty() && (mainHand.getItem() instanceof Bag || mainHand.getItem() instanceof GhostBag)) {
                ClientDataManager data = GhostBag.getClientData(mainHand, player);
                if (data != null) {
                    data.onRenderHud((ClientPlayerEntity)player, event.getWindow(), event.getMatrixStack(), event.getPartialTicks());
                    return;
                }
            }
            ItemStack offHand = player.getHeldItemOffhand();
            if (!offHand.isEmpty() && (offHand.getItem() instanceof Bag || offHand.getItem() instanceof GhostBag)) {
                ClientDataManager data = GhostBag.getClientData(offHand, player);
                if (data != null) {
                    data.onRenderHud((ClientPlayerEntity)player, event.getWindow(), event.getMatrixStack(), event.getPartialTicks());
                    return;
                }
            }
            ItemStack chestPlate = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (!chestPlate.isEmpty() && (chestPlate.getItem() instanceof Bag || chestPlate.getItem() instanceof GhostBag)) {
                ClientDataManager data = GhostBag.getClientData(chestPlate, player);
                if (data != null) {
                    data.onRenderHud((ClientPlayerEntity)player, event.getWindow(), event.getMatrixStack(), event.getPartialTicks());
                    return;
                }
            }
        }
    }
}
