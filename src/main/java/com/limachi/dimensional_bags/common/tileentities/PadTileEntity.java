package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Pad;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.utils.NBTUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.limachi.dimensional_bags.StaticInit;
import net.minecraft.util.math.AxisAlignedBB;

import static net.minecraft.potion.Effects.*;

@StaticInit
public class PadTileEntity extends BaseTileEntity implements IisBagTE {

    public static final String NAME = "pad";

    protected boolean needUpdate = true;

    public static final int TICK_RATE = 8;

    static {
        Registries.registerTileEntity(NAME, PadTileEntity::new, ()->Registries.getBlock(Pad.NAME), null);
    }

    public PadTileEntity() { super(Registries.getTileEntityType(NAME)); }

    public void needUpdate() { this.needUpdate = true; }

    public Stream<String> getNameList() { return getTileData().getList("List", 8).stream().map(INBT::getString); }

    static final Effect[] randomEffectsPopulator = {SPEED,
            SLOWNESS,
            HASTE,
            MINING_FATIGUE,
            STRENGTH,
            INSTANT_HEALTH,
            INSTANT_DAMAGE,
            JUMP_BOOST,
            NAUSEA,
            REGENERATION,
            RESISTANCE,
            FIRE_RESISTANCE,
            WATER_BREATHING,
            INVISIBILITY,
            BLINDNESS,
            NIGHT_VISION,
            HUNGER,
            WEAKNESS,
            POISON,
            WITHER,
            HEALTH_BOOST,
            ABSORPTION,
            SATURATION,
            GLOWING,
            LEVITATION,
            LUCK,
            UNLUCK,
            SLOW_FALLING};
    static final List<EffectInstance> randomEffects = Arrays.stream(randomEffectsPopulator).map(e->new EffectInstance(e, 0, e.isInstant() ? 0 : TICK_RATE * 32, true, false, true)).collect(Collectors.toList());

    private void hiddenCreatureBuff(SubRoomsManager sm) { //dolphin -> dolphin grace to holder, brown mooshroom -> low chance of random potion effect, cow -> low chance of removing a potion effect, mooshroom -> low chance of saturation
        Entity e = HolderData.execute(sm.getEyeId(), hd->hd.getEntity(), null);
        if (e instanceof LivingEntity) {
            List<LivingEntity> el = world.getLoadedEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(pos.up()), t -> t instanceof DolphinEntity || t instanceof MooshroomEntity);
            if (!el.isEmpty()) {
                LivingEntity entity = el.get(0);
                if (entity instanceof DolphinEntity && e.isWet())
                    ((LivingEntity) e).addPotionEffect(new EffectInstance(DOLPHINS_GRACE, TICK_RATE * 32, 0, true, false, true));
                else if (entity instanceof MooshroomEntity && EventManager.RANDOM.nextDouble() > 0.8) {
                    if (((MooshroomEntity)entity).getMooshroomType() == MooshroomEntity.Type.BROWN)
                        ((LivingEntity) e).addPotionEffect(randomEffects.get(EventManager.RANDOM.nextInt(randomEffects.size())));
                    else if (((MooshroomEntity)entity).getMooshroomType() == MooshroomEntity.Type.RED)
                        ((LivingEntity) e).addPotionEffect(new EffectInstance(SATURATION, 400, 1, true, false, true));
                }
            }
        }
    }

    public <T extends Entity> T getEntity(Class<T> clazz) {
        List<T> l = world.getLoadedEntitiesWithinAABB(clazz, new AxisAlignedBB(pos.up()));
        return l.isEmpty() ? null : l.get(0);
    }

    @Override
    public void tick(int tick) {
        if (world == null || !DimBag.isServer(world) || tick % TICK_RATE != 0 || !(world.getBlockState(pos).getBlock() instanceof Pad)) return;
        SubRoomsManager sm = SubRoomsManager.getInstance(SubRoomsManager.getEyeId(world, pos, false));
        if (sm == null) return;
        if (needUpdate) {
            if (Pad.isPowered(world.getBlockState(pos)))
                sm.activatePad(pos);
            else
                sm.deactivatePad(pos);
            needUpdate = false;
        }
        if (tick % (TICK_RATE * 16) == 0)
            hiddenCreatureBuff(sm);
    }

    public boolean isWhitelist() { return getTileData().getBoolean("IsWhitelist"); }

    public void updateList(boolean whitelist, Collection<String> names) {
        getTileData().putBoolean("IsWhitelist", whitelist);
        getTileData().put("List", NBTUtils.toNBT(names));
        markDirty();
    }

    public boolean isValidEntity(Entity entity) {
        String name = entity.getName().getUnformattedComponentText();
        if (name.isEmpty())
            name = entity.getName().getString();
        DimBag.LOGGER.info("validating entity entering the bag with name: " + name);
        String finalName = name;
        return getNameList().anyMatch(s->s.equals(finalName)) == isWhitelist();
    }
}
