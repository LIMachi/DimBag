package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.common.tileentities.BagProxyTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
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
public abstract class BaseUpgrade<T extends BaseUpgrade<?>> extends Item {

    public static final String SETTING_GROUP = "Upgrades";

    public final SettingsData.SettingsReader settingsReader = new SettingsData.SettingsReader(SETTING_GROUP, upgradeName(), ()->new ItemStack(getInstance(upgradeName())));

//    public static SettingsData.Settings getSettings(String name) { return UpgradeManager.getUpgrade(name).settings; }

    public <T> T getSetting(int eye, String label) { return settingsReader.get(label, SettingsData.getInstance(eye)); }

    public <T> void setSetting(int eye, String label, T value) { settingsReader.set(label, SettingsData.getInstance(eye), value); }

//    public static <T> T getSetting(int eye, String name, String label, T def) { return SettingsData.execute(eye, sd->UpgradeManager.getUpgrade(name).settings.get(label, sd), def); }

    public boolean isInstalled(int eyeId) { return UpgradeManager.execute(eyeId, um->um.getInstalledUpgrades().contains(upgradeName()), false); }

    public boolean isActive(int eyeId) { return canBeInstalled() && isInstalled(eyeId) && SettingsData.execute(eyeId, sd->settingsReader.getOrDefault("active", sd, true), true); }

    public static <T extends BaseUpgrade> T getInstance(String name) { return (T)UpgradeManager.getUpgrade(name); }

    /**
     * here you should use the settings method to populate the settings, reading and writing should be done elsewhere
     * @param settingsReader
     */
    public void initSettings(SettingsData.SettingsReader settingsReader) {
        settingsReader.bool("active", true);
    }

    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity> void onRenderEquippedBag(int eyeId, BipedModel<T> entityModel, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {}

    @OnlyIn(Dist.CLIENT)
    public void onRenderBagEntity(int eyeId, BagEntity entity, float yaw, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int packedLight) {}

    public ActionResultType upgradeEntityTick(int eyeId, World world, Entity entity) { return ActionResultType.PASS; } //called every X ticks by the bag manager

    private BaseUpgrade() {
        super(DimBag.DEFAULT_PROPERTIES);
    }

    protected BaseUpgrade(Item.Properties props) {
        super(props.stacksTo(new BaseUpgrade<T>() {
            @Override
            public boolean canBeInstalled() {
                return false;
            }

            @Override
            public String upgradeName() {
                return "";
            }
        }.getMaxCount()));
        initSettings(settingsReader);
        settingsReader.build();
    }

    public int getMaxCount() { return 1; }

    public String getMemoryKey() { return upgradeName(); }

    public CompoundNBT getMemory(UpgradeManager manager) { return manager.getMemory(getMemoryKey(), false); }
    public int getCount(UpgradeManager manager) { return manager.getUpgradeCount(upgradeName()); }

    public void addCount(UpgradeManager manager, int count) {
        CompoundNBT nbt = manager.getMemory(getMemoryKey(), true);
        int pc = nbt.getInt(UpgradeManager.COUNT_NBT_KEY);
        nbt.putInt(UpgradeManager.COUNT_NBT_KEY, pc + count);
        manager.setDirty();
    }

    /**
     * install the upgrade on the targeted bag entity
     */
    @SubscribeEvent
    public static void installUpgradeOnBagEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().getItem() instanceof BaseUpgrade && event.getItemStack().getCount() > 0 && event.getTarget() instanceof BagEntity) {
            event.setCanceled(true);
            event.setCancellationResult(ActionResultType.SUCCESS);
            if (DimBag.isServer(event.getWorld()))
                event.getPlayer().setItemInHand(event.getHand(), installUpgrades(event.getPlayer(), ((BagEntity) event.getTarget()).getEyeId(), event.getItemStack()));
        }
    }

    /**
     * if inside a bag: install on the bag
     * else: install the upgrade on the equipped bag (if present)
     */
    @ParametersAreNonnullByDefault
    @Override
    public @Nonnull ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (DimBag.isServer(worldIn) && !(playerIn instanceof FakePlayer) && KeyMapController.KeyBindings.SNEAK_KEY.getState(playerIn)) {
            ItemStack stack = playerIn.getItemInHand(handIn);
            if (stack.getItem() instanceof BaseUpgrade && stack.getCount() > 0) {
                int eyeId = SubRoomsManager.getEyeId(worldIn, playerIn.blockPosition(), false);
                if (eyeId == 0)
                    eyeId = Bag.getBag(playerIn, 0, true, false);
                if (eyeId > 0)
                    return ActionResult.success(installUpgrades(playerIn, eyeId, stack));
            }
        }
        return super.use(worldIn, playerIn, handIn);
    }

    /**
     * if targeting a proxy: install the upgrade on the bag targeted by the proxy
     * if inside a bag: install on the bag
     * else: install on the equipped bag (if present)
     */
    @Override
    public @Nonnull ActionResultType useOn(@Nonnull ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (DimBag.isServer(context.getLevel()) && player != null && !(player instanceof FakePlayer) && KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) {
            ItemStack stack = context.getItemInHand();
            if (stack.getItem() instanceof BaseUpgrade && stack.getCount() > 0) {
                TileEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
                int eyeId = te instanceof BagProxyTileEntity ? ((BagProxyTileEntity)te).eyeId() : SubRoomsManager.getEyeId(player.level, player.blockPosition(), false);
                if (eyeId == 0)
                    eyeId = Bag.getBag(player, 0, true, false);
                if (eyeId > 0) {
                    player.setItemInHand(context.getHand(), installUpgrades(player, eyeId, stack));
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return super.useOn(context);
    }

    private static ItemStack installUpgrades(PlayerEntity player, int eyeId, ItemStack stack) {
        BaseUpgrade upgrade = (BaseUpgrade) stack.getItem();
        if (!upgrade.canBeInstalled()) {
            player.displayClientMessage(new TranslationTextComponent("notification.upgrade.trying_to_install_disabled_upgrade").withStyle(TextFormatting.BOLD, TextFormatting.RED), true);
            return stack;
        }
        UpgradeManager um = UpgradeManager.getInstance(eyeId);
        ItemStack out = stack.copy();
        if (um.getMemory(upgrade.getMemoryKey(), true).getInt(UpgradeManager.COUNT_NBT_KEY) < upgrade.getMaxCount()) {
            int nc = ((BaseUpgrade) stack.getItem()).installUpgrade(um, stack.getCount());
            if (nc > 0) {
                out.shrink(nc);
                upgrade.addCount(um, nc);
                player.displayClientMessage(new TranslationTextComponent("notification.upgrade.installed_X_upgrades", nc, upgrade.upgradeName()), true);
            } else
                player.displayClientMessage(new TranslationTextComponent("notification.upgrade.max_upgrades_reached", upgrade.upgradeName()), true);
        } else
            player.displayClientMessage(new TranslationTextComponent("notification.upgrade.max_upgrades_reached", upgrade.upgradeName()), true);
        return out;
    }

    @Nonnull
    protected IFormattableTextComponent upgradeInfo() {
        return new TranslationTextComponent("tooltip.upgrades." + upgradeName());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (!((BaseUpgrade) stack.getItem()).canBeInstalled())
            tooltip.add(new TranslationTextComponent("tooltip.upgrade.disabled").withStyle(TextFormatting.BOLD, TextFormatting.RED));
        if (Screen.hasShiftDown()) {
            tooltip.add(upgradeInfo().withStyle(TextFormatting.YELLOW));
            tooltip.add(new TranslationTextComponent("tooltip.upgrades.install_info").withStyle(TextFormatting.AQUA));
        }  else
            tooltip.add(new TranslationTextComponent("tooltip.shift_for_info"));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    abstract public boolean canBeInstalled();

    /**
     * usually the item name of the upgrade
     * @return
     */
    abstract public String upgradeName();

    /**
     * @param manager the upgrade manager
     * @param qty how many of this upgrades the player is trying to install at once
     * @return the number of upgrades installed
     */
    public int installUpgrade(UpgradeManager manager, int qty) { return qty; }

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
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) { //might be used at some point to add hud ellements FIXME
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            PlayerEntity player = DimBag.getPlayer();
            int bag = Bag.getBag(player, 0, false, false);
            if (bag > 0) {
                ModeManager.execute(bag, mm -> mm.onRenderHud(player, event.getWindow(), event.getMatrixStack(), event.getPartialTicks()));
            }
            /*
//            ClientDataManager[] data = {null};
            CuriosIntegration.searchItem(player, Item.class, s->{
                if (s.getItem() instanceof Bag || s.getItem() instanceof GhostBag) {
//                    data[0] = ClientDataManager.getInstance(s);
                    return true;
                }
                return false;
            });
//            if (data[0] != null)
//                data[0].onRenderHud((ClientPlayerEntity)player, event.getWindow(), event.getMatrixStack(), event.getPartialTicks());
             */
        }
    }
}
