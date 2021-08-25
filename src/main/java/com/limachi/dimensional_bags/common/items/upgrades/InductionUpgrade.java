package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.common.tileentities.CrystalTileEntity;
import com.limachi.dimensional_bags.utils.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.stream.Stream;

@StaticInit
public class InductionUpgrade extends BaseUpgrade<InductionUpgrade> {

    public static final String NAME = "induction_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    static {
        UpgradeManager.registerUpgrade(NAME, InductionUpgrade::new);
    }

    public InductionUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    /**
     * broadly query all the TE in this bag that have an energy capability, ignoring crystals (would loop energy each tick)
     */
    public static Stream<TileEntity> iterEnergyTE(int eye, World world) {
        return world.blockEntityList.stream().filter(te->!(te instanceof CrystalTileEntity) && ((te.getBlockPos().getX() - SubRoomsManager.ROOM_OFFSET_X + SubRoomsManager.HALF_ROOM) / SubRoomsManager.ROOM_SPACING + 1 == eye) && te.getCapability(CapabilityEnergy.ENERGY).isPresent());
    }

    @Config(min = "0", cmt = "how much energy should be extracted from the crystals and into the TEs present in the bag")
    public static int TRANSFER_RATE = 64;

    @Override
    public ActionResultType upgradeEntityTick(int eyeId, World world, Entity entity) {
        if (UpgradeManager.getUpgrade(NAME).isActive(eyeId)) {
            EnergyData ed = EnergyData.getInstance(eyeId);
            if (ed == null) return ActionResultType.SUCCESS;
            World w = WorldUtils.getRiftWorld();
            if (w == null) return ActionResultType.SUCCESS;
            iterEnergyTE(eyeId, w).forEach(te->te.getCapability(CapabilityEnergy.ENERGY).ifPresent(es->es.receiveEnergy(ed.extractEnergy(es.receiveEnergy(Integer.min(ed.getEnergyStored(), TRANSFER_RATE), true), false), false)));
        }
        return ActionResultType.SUCCESS;
    }
}
