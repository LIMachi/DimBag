package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.ConfigManager;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.common.tileentities.IisBagTE;
import com.limachi.dimensional_bags.utils.WorldUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.ConduitTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

@StaticInit
public class TimeDistortionUpgrade extends BaseUpgrade {

    public static final String NAME = "time_distortion_upgrade";

    @ConfigManager.Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    @ConfigManager.Config(cmt = "the time at will be distorted in the range 2^[-MAX_FACTOR, MAX_FACTOR] (0 = vanilla time, 6 = 64x the vanilla time, -6 = 1/64x vanilla time)")
    public static int MAX_FACTOR = 6;

    @ConfigManager.Config(cmt = "how much energy should be used per extra tick and per TE", min = "0")
    public static int ENERGY_CONSUMPTION = 0;

    @ConfigManager.Config(cmt = "how much energy should be created per skipped tick and per TE", min = "0")
    public static int ENERGY_PRODUCTION = 16;

    static {
        UpgradeManager.registerUpgrade(NAME, TimeDistortionUpgrade::new);
    }

    @Override
    public void initSettings(SettingsData.SettingsReader settingsReader) {
        settingsReader.integer("factor", 1, -MAX_FACTOR, MAX_FACTOR, true);
    }

    public TimeDistortionUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    @Override
    public int installUpgrade(UpgradeManager manager, int qty) { return qty; }

    /**
     * this a broad removal of TE (does not check if the TE is inside a room, only it's X coordinate)
     */
    public static void removeTickingTEInBag(int eye, World world) {
        world.tickableTileEntities.removeIf(te->!(te instanceof IisBagTE || te instanceof BeaconTileEntity || te instanceof ConduitTileEntity) && (te.getPos().getX() - SubRoomsManager.ROOM_OFFSET_X + SubRoomsManager.HALF_ROOM) / SubRoomsManager.ROOM_SPACING + 1 == eye);
    }

    /**
     * expect TE to already have been removed from the ticking list (search by iterating on world entities, broad search, only X coordinate)
     */
    public static Stream<TileEntity> iterTickingTEInBag(int eye, World world) {
        return world.loadedTileEntityList.stream().filter(te->!(te instanceof IisBagTE || te instanceof BeaconTileEntity || te instanceof ConduitTileEntity) && te instanceof ITickableTileEntity && ((te.getPos().getX() - SubRoomsManager.ROOM_OFFSET_X + SubRoomsManager.HALF_ROOM) / SubRoomsManager.ROOM_SPACING + 1 == eye));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (stack.getItem() instanceof BaseUpgrade && ((BaseUpgrade)stack.getItem()).canBeInstalled())
            tooltip.add(new TranslationTextComponent("tooltip.upgrade.time_distortion_upgrade.warning").mergeStyle(TextFormatting.RED));
    }

    @Override
    public ActionResultType upgradeEntityTick(int eyeId, World worldIn, Entity entity) {
        final World world = worldIn.getDimensionKey() == WorldUtils.DimBagRiftKey ? worldIn : WorldUtils.getRiftWorld();
        if (world == null) return ActionResultType.PASS;
        TimeDistortionUpgrade upgrade = getInstance(NAME);
        if (!upgrade.isActive(eyeId)) return ActionResultType.PASS;
        int factor = upgrade.getSetting(eyeId, "factor");
        removeTickingTEInBag(eyeId, world);
        if (factor >= 0 || EventManager.tick % Math.pow(2, -factor) == 0) {
            final int mi = factor > 0 ? (int)Math.pow(2, factor) : 1;
            iterTickingTEInBag(eyeId, world).forEach(te->{
                for (int i = 0; i < mi; ++i) {
                    if (i > 0) {
                        if (EnergyData.extractEnergy(eyeId, ENERGY_CONSUMPTION) != ENERGY_CONSUMPTION)
                            continue;
                    }
                    ((ITickableTileEntity) te).tick();
                }
            });
        } else
            EnergyData.receiveEnergy(eyeId, (int) (ENERGY_PRODUCTION * iterTickingTEInBag(eyeId, world).count()));
        return ActionResultType.SUCCESS;
    }
}
