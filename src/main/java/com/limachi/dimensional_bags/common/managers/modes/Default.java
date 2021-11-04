package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.ConfigManager;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.container.PillarContainer;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.Mode;
import com.limachi.dimensional_bags.utils.WorldUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.ConduitTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

public class Default extends Mode {

    public static final String ID = "Default";

    public Default() { super(ID, true, true); } //will always be called last (if no mode consumed the event first)

    @Override
    public void initSettings(SettingsData.SettingsReader settingsReader) {
        settingsReader.bool("should_show_energy", true);
        settingsReader.string("bag_name", new TranslationTextComponent("item.dim_bag.bag").getString(), null);
        settingsReader.bool("quick_enter", false);
        settingsReader.bool("quick_reequip", false);
    }

    @Override
    public ActionResultType onItemUse(int eyeId, World world, PlayerEntity player, BlockRayTraceResult ray) { //called when the bag is right clicked on something, before the bag does anything
        if (player == null || !KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) return ActionResultType.PASS; //only test a player croushing
        int x = Math.abs(ray.getBlockPos().getX() - player.blockPosition().getX());
        int y = Math.abs(ray.getBlockPos().getY() - player.blockPosition().getY());
        int z = Math.abs(ray.getBlockPos().getZ() - player.blockPosition().getZ());
        if (x > 1 || y > 2 || z > 1) return ActionResultType.PASS; //only validate if the click is close enough to the player
        if (getSetting(eyeId, "quick_enter"))
            SubRoomsManager.execute(eyeId, srm->srm.enterBag(player));
        else
            Bag.unequipBags(player, eyeId, ray.getBlockPos().above(), null);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onActivateItem(int eyeId, PlayerEntity player) {
        if (!KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) {
            if (player instanceof ServerPlayerEntity)
                PillarContainer.open(player, eyeId, null);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
    @Override
    public void onRenderHud(int eyeId, boolean isSelected, PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {
        if (getSetting(eyeId, "should_show_energy"))
            EnergyData.execute(eyeId, energyData->{
                if (energyData.getMaxEnergyStored() > 0 || energyData.getEnergyStored() > 0)
                    RenderUtils.drawString(matrixStack, Minecraft.getInstance().font, "Energy: " + energyData.getEnergyStored() + " / " + energyData.getMaxEnergyStored(), new Box2d(10, 10, 100, 10), 0xFFFFFFFF, true, false);});
    }

    public static Stream<TileEntity> iterBeaconAndConduit(int eye, World world) {
        return world.blockEntityList.stream().filter(te->(te instanceof BeaconTileEntity || te instanceof ConduitTileEntity) && ((te.getBlockPos().getX() - SubRoomsManager.ROOM_OFFSET_X + SubRoomsManager.HALF_ROOM) / SubRoomsManager.ROOM_SPACING + 1 == eye));
    }

    public static void applyBeaconEffect(CompoundNBT beaconNBT, LivingEntity entity) {
        int levels = beaconNBT.getInt("Levels");
        Effect primaryEffect = Effect.byId(beaconNBT.getInt("Primary"));
        Effect secondaryEffect = Effect.byId(beaconNBT.getInt("Secondary"));
        if (primaryEffect != null) {
            double d0 = levels * 10 + 10;
            int i = 0;
            if (levels >= 4 && primaryEffect == secondaryEffect)
                i = 1;
            int j = (9 + levels * 2) * 20;
            entity.addEffect(new EffectInstance(primaryEffect, j, i, true, false, true));
            if (levels >= 4 && primaryEffect != secondaryEffect && secondaryEffect != null)
                entity.addEffect(new EffectInstance(secondaryEffect, j, 0, true, false, true));
        }
    }

    private boolean beaconAndConduitTickBehavior(int eyeId, Entity entity) {
        if (EventManager.tick % 100 != 0 || !(entity instanceof LivingEntity)) return false;
        World w = WorldUtils.getRiftWorld();
        if (w == null) return false;
        iterBeaconAndConduit(eyeId, w).forEach(te->{
            if (te instanceof BeaconTileEntity)
                applyBeaconEffect(te.save(new CompoundNBT()), (LivingEntity) entity);
            if (te instanceof ConduitTileEntity && ((ConduitTileEntity)te).isActive() && entity.isInWaterOrRain())
                ((LivingEntity)entity).addEffect(new EffectInstance(Effects.CONDUIT_POWER, 260, 0, true, false, true));
        });
        return true;
    }

    private static final Random RANDOM = new Random();

    @ConfigManager.Config
    public static int ONE_IN = 20;

    private boolean randomMobErrorBehavior(int eyeId, Entity entity) { //adds a random (configurable) chance that an entity close to the bag (unequiped) will be sucked inside the bag
        if (EventManager.tick % 2000 != 0 || !(entity instanceof BagEntity) || ONE_IN <= 0) return false;
        DimBag.debug("randomMobErrorBehavior");
        Optional<MobEntity> f = entity.level.getEntitiesOfClass(MobEntity.class, new AxisAlignedBB(entity.blockPosition().offset(-2, -2, -2), entity.blockPosition().offset(2, 2, 2)), e->!(e instanceof WitherEntity || e instanceof EnderDragonEntity || e instanceof BagEntity)).stream().findFirst();
        if (f.isPresent() && (((double)RANDOM.nextInt(ONE_IN)) / (double)ONE_IN) * f.get().position().distanceTo(entity.position()) < 1.d / (double)ONE_IN)
            SubRoomsManager.execute(eyeId, srm->srm.enterBag(f.get()));
        return true;
    }

    @Override
    public ActionResultType onEntityTick(int eyeId, World world, Entity entity) {
        beaconAndConduitTickBehavior(eyeId, entity);
        randomMobErrorBehavior(eyeId, entity);
        return ActionResultType.SUCCESS;
    }
}
