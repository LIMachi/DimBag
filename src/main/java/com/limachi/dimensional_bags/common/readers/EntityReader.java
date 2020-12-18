package com.limachi.dimensional_bags.common.readers;

import com.limachi.dimensional_bags.common.managers.ModeManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class EntityReader {

    protected static class SuppliersMap<O extends Entity> {
        private final Class<O> clazz;
        private final HashMap<String, BiFunction<O, Integer, Object>> map = new HashMap<>();

        public SuppliersMap(Class<O> clazz) { this.clazz = clazz; }

        public boolean isValidMethod(String name, Object entity) {
            return entity instanceof Entity && clazz.isInstance(entity) && map.containsKey(name);
        }

        public Set<String> methods() { return map.keySet(); }

        public Object get(String name, Object entity, int eyeId) {
            if (isValidMethod(name, entity))
                return map.get(name).apply(clazz.cast(entity), eyeId);
            return null;
        }

        public void register(String name, BiFunction<O, Integer, Object> supplier) { map.put(name, supplier); }
    }

    static final protected HashMap<Class<? extends Entity>, SuppliersMap<?>> cache = new HashMap<>();

    static public <O extends Entity> void register(Class<O> clazz, String name, BiFunction<O, Integer, Object> supplier) {
        SuppliersMap<O> cached = (SuppliersMap<O>)cache.get(clazz);
        if (cached == null)
            cache.put(clazz, cached = new SuppliersMap<>(clazz));
        cached.register(name, supplier);
    }

    static public <O extends Entity> void register(Class<O> clazz, String name, Function<O, Object> supplier) {
        register(clazz, name, (e, i)->supplier.apply(e));
    }

    static public <O extends Entity> void register(Class<O> clazz, String name, Supplier<Object> supplier) {
        register(clazz, name, (e, i)->supplier.get());
    }

    protected final Entity entity;

    public <T extends Entity> EntityReader(T entity) {
        this.entity = entity;
    }

    static {
        register(Entity.class, "type", e -> e.getType().toString());
        register(Entity.class, "world", e -> e.world.getDimensionKey().toString());
        register(Entity.class, "previous_position_x", e -> e.prevPosX);
        register(Entity.class, "previous_position_y", e -> e.prevPosY);
        register(Entity.class, "previous_position_z", e -> e.prevPosZ);
        register(Entity.class, "position_x", Entity::getPosX);
        register(Entity.class, "position_y", Entity::getPosY);
        register(Entity.class, "position_z", Entity::getPosZ);
        register(Entity.class, "block_position_x", e -> e.getPosition().getX());
        register(Entity.class, "block_position_y", e -> e.getPosition().getY());
        register(Entity.class, "block_position_z", e -> e.getPosition().getZ());
        register(Entity.class, "motion_x", e -> e.getMotion().x);
        register(Entity.class, "motion_y", e -> e.getMotion().y);
        register(Entity.class, "motion_z", e -> e.getMotion().z);
        register(Entity.class, "rotation_yaw", e -> e.rotationYaw);
        register(Entity.class, "rotation_pitch", e -> e.rotationPitch);
        register(Entity.class, "previous_rotation_yaw", e -> e.prevRotationYaw);
        register(Entity.class, "previous_rotation_pitch", e -> e.prevRotationPitch);
        register(Entity.class, "is_on_ground", Entity::isOnGround);
        register(Entity.class, "is_colliding_horizontally", e -> e.collidedHorizontally);
        register(Entity.class, "is_colliding_vertically", e -> e.collidedVertically);
        register(Entity.class, "did_velocity_chang", e -> e.velocityChanged);
        register(Entity.class, "previous_distance_walked", e -> e.prevDistanceWalkedModified);
        register(Entity.class, "distance_walked", e -> e.distanceWalkedModified);
        register(Entity.class, "distance_walked_step", e -> e.distanceWalkedOnStepModified);
        register(Entity.class, "fall_distance", e -> e.fallDistance);
        register(Entity.class, "step_height", e -> e.stepHeight);
        register(Entity.class, "is_no_clip", e -> e.noClip);
        register(Entity.class, "ticks_existed", e -> e.ticksExisted);
        register(Entity.class, "fire_tick", Entity::getFireTimer);
        register(Entity.class, "is_immune_to_fire", Entity::isImmuneToFire);
        register(Entity.class, "is_in_water", Entity::isInWater);
        register(Entity.class, "is_in_lava", Entity::isInLava);
        register(Entity.class, "hurt_resistance_timer", e -> e.hurtResistantTime);
        register(Entity.class, "air", Entity::getAir);
        register(Entity.class, "maximum_air", Entity::getMaxAir);
        register(Entity.class, "is_name_visible", Entity::isCustomNameVisible);
        register(Entity.class, "is_silent", Entity::isSilent);
        register(Entity.class, "has_no_gravity", Entity::hasNoGravity);
        register(Entity.class, "chunk_coordinates_x", e -> e.chunkCoordX);
        register(Entity.class, "chunk_coordinates_y", e -> e.chunkCoordY);
        register(Entity.class, "chunk_coordinates_z", e -> e.chunkCoordZ);
        register(Entity.class, "is_airborne", e -> e.isAirBorne);
        register(Entity.class, "is_invulnerable", Entity::isInvulnerable);
        register(Entity.class, "UUID", Entity::getCachedUniqueIdString);
        register(Entity.class, "eye_height", e -> e.getEyeHeight());
        register(Entity.class, "width", e -> e.getSize(e.getPose()).width);
        register(Entity.class, "height", e -> e.getSize(e.getPose()).height);
        register(Entity.class, "can_passenger_steer", Entity::canPassengerSteer);
        register(Entity.class, "push_reaction", e -> e.getPushReaction().toString());
        register(Entity.class, "sound_category", e -> e.getSoundCategory().getName());
        register(Entity.class, "is_vulnerable_to_fall", e -> e.isInvulnerableTo(DamageSource.FALL));
        register(Entity.class, "is_vulnerable_to_anvil", e -> e.isInvulnerableTo(DamageSource.ANVIL));
        register(Entity.class, "is_vulnerable_to_cactus", e -> e.isInvulnerableTo(DamageSource.CACTUS));
        register(Entity.class, "is_vulnerable_to_craming", e -> e.isInvulnerableTo(DamageSource.CRAMMING));
        register(Entity.class, "is_vulnerable_to_dragon_breath", e -> e.isInvulnerableTo(DamageSource.DRAGON_BREATH));
        register(Entity.class, "is_vulnerable_to_drown", e -> e.isInvulnerableTo(DamageSource.DROWN));
        register(Entity.class, "is_vulnerable_to_drysuppliers", e -> e.isInvulnerableTo(DamageSource.DRYOUT));
        register(Entity.class, "is_vulnerable_to_falling_block", e -> e.isInvulnerableTo(DamageSource.FALLING_BLOCK));
        register(Entity.class, "is_vulnerable_to_fly_into_wall", e -> e.isInvulnerableTo(DamageSource.FLY_INTO_WALL));
        register(Entity.class, "is_vulnerable_to_generic", e -> e.isInvulnerableTo(DamageSource.GENERIC));
        register(Entity.class, "is_vulnerable_to_hot_floor", e -> e.isInvulnerableTo(DamageSource.HOT_FLOOR));
        register(Entity.class, "is_vulnerable_to_in_fire", e -> e.isInvulnerableTo(DamageSource.IN_FIRE));
        register(Entity.class, "is_vulnerable_to_in_wall", e -> e.isInvulnerableTo(DamageSource.IN_WALL));
        register(Entity.class, "is_vulnerable_to_lava", e -> e.isInvulnerableTo(DamageSource.LAVA));
        register(Entity.class, "is_vulnerable_to_lightning_bolt", e -> e.isInvulnerableTo(DamageSource.LIGHTNING_BOLT));
        register(Entity.class, "is_vulnerable_to_magic", e -> e.isInvulnerableTo(DamageSource.MAGIC));
        register(Entity.class, "is_vulnerable_to_on_fire", e -> e.isInvulnerableTo(DamageSource.ON_FIRE));
        register(Entity.class, "is_vulnerable_to_suppliers_of_world", e -> e.isInvulnerableTo(DamageSource.OUT_OF_WORLD));
        register(Entity.class, "is_vulnerable_to_starve", e -> e.isInvulnerableTo(DamageSource.STARVE));
        register(Entity.class, "is_vulnerable_to_sweet_berry_bush", e -> e.isInvulnerableTo(DamageSource.SWEET_BERRY_BUSH));
        register(Entity.class, "is_vulnerable_to_wither", e -> e.isInvulnerableTo(DamageSource.WITHER));
        register(Entity.class, "is_spectator", Entity::isSpectator);
        register(Entity.class, "pose", e -> e.getPose().name());
        register(Entity.class, "can_swim", Entity::canSwim);
        register(Entity.class, "is_alive", Entity::isAlive);
        register(Entity.class, "is_inside_opaque_block", Entity::isEntityInsideOpaqueBlock);
        register(Entity.class, "is_being_ridden", Entity::isBeingRidden);
        register(Entity.class, "is_passenger", e -> e.isPassenger());
        register(Entity.class, "can_be_ridden_in_water", e -> e.canBeRiddenInWater());
        register(Entity.class, "is_sprinting", Entity::isSprinting);
        register(Entity.class, "is_swiming", Entity::isSwimming);
        register(Entity.class, "is_invisible", Entity::isInvisible);
        register(Entity.class, "team", e -> e.getTeam() != null ? e.getTeam().getName() : "");
        register(Entity.class, "team_color", e -> e.getTeam() != null ? e.getTeam().getColor().getFriendlyName() : "");
        register(Entity.class, "string", Entity::toString);
        register(Entity.class, "is_non_boss", Entity::isNonBoss);
        register(Entity.class, "max_fall_height", Entity::getMaxFallHeight);
        register(Entity.class, "does_not_trigger_pressure_plate", Entity::doesEntityNotTriggerPressurePlate);
        register(Entity.class, "can_be_pushed_by_water", Entity::isPushedByWater);
        register(Entity.class, "is_one_player_riding", Entity::isOnePlayerRiding);

        register(Entity.class, "automation_upgrade_click", (e, i) -> ModeManager.execute(i, modeManager -> {boolean out = modeManager.getModesNBT().getCompound("Automation").getInt("state") == 1; modeManager.getModesNBT().getCompound("Automation").putInt("state", 0); return out;}, false));
        register(Entity.class, "automation_upgrade_use_on_block", (e, i) -> ModeManager.execute(i, modeManager -> {boolean out = modeManager.getModesNBT().getCompound("Automation").getInt("state") == 3; modeManager.getModesNBT().getCompound("Automation").putInt("state", 0); return out;}, false));
        register(Entity.class, "automation_upgrade_attack", (e, i) -> ModeManager.execute(i, modeManager -> {boolean out = modeManager.getModesNBT().getCompound("Automation").getInt("state") == 2; modeManager.getModesNBT().getCompound("Automation").putInt("state", 0); return out;}, false));
    }

    public enum Commands {
        RANGE,
        COMPARE
    }

    public enum Comparator {
        EQUAL,
        NOT_EQUAL,
        LESS,
        MORE,
        LESS_OR_EQUAL,
        MORE_OR_EQUAL
    }

    protected Optional<Boolean> boolFromString(String value) {
        if (value.equalsIgnoreCase("true"))
            return Optional.of(true);
        else if (value.equalsIgnoreCase("false"))
            return Optional.of(false);
        return Optional.empty();
    }

    protected Object getValue(String key, int eyeId) {
        for (Map.Entry<Class<? extends Entity>, SuppliersMap<?>> entry : cache.entrySet())
            if (entry.getValue().isValidMethod(key, entity))
                return entry.getValue().get(key, entity, eyeId);
        return null;
    }

    protected boolean isValidMethod(String key) {
        for (Map.Entry<Class<? extends Entity>, SuppliersMap<?>> entry : cache.entrySet())
            if (entry.getValue().isValidMethod(key, entity))
                return true;
        return false;
    }

    protected boolean compare(String key, Comparator cmp, String constant, int eyeId) {
        Object o = getValue(key, eyeId);
        if (o == null) return false;
        String s = o.toString();
        if (s == null) return false;
        Optional<Boolean> ob1 = boolFromString(s);
        Optional<Boolean> ob2 = boolFromString(constant);
        if (ob1.isPresent() && ob2.isPresent()) {
            switch (cmp) {
                case EQUAL:
                case MORE_OR_EQUAL:
                case LESS_OR_EQUAL:
                    return ob1.get() == ob2.get();
                case NOT_EQUAL:
                case MORE:
                case LESS:
                    return ob1.get() != ob2.get();
                default:
                    return false;
            }
        }
        try {
            double d1 = Double.parseDouble(s);
            double d2 = Double.parseDouble(constant);
            switch (cmp) {
                case EQUAL:
                    return d1 == d2;
                case NOT_EQUAL:
                    return d1 != d2;
                case MORE:
                    return d1 > d2;
                case MORE_OR_EQUAL:
                    return d1 >= d2;
                case LESS:
                    return d1 < d2;
                case LESS_OR_EQUAL:
                    return d1 <= d2;
                default:
            }
            return false;
        } catch (NumberFormatException e) {}
        switch (cmp) {
            case EQUAL:
                return s.matches(constant);
            case NOT_EQUAL:
                return !s.matches(constant);
            case MORE:
            case MORE_OR_EQUAL:
                return Pattern.compile(constant).matcher(s).find();
            case LESS:
            case LESS_OR_EQUAL:
                return Pattern.compile(s).matcher(constant).find();
        }
        return false;
    }

    protected int range(String key, double start, double end, int eyeId) {
        Object o = getValue(key, eyeId);
        if (o == null) return 0;
        String s = o.toString();
        if (s == null) return 0;
        double d;
        try {
            d = Double.parseDouble(s);
        } catch (NumberFormatException e) { return 0; }
        if (start < end) {
            if (d < start || d > end) return 0;
            return (int)Math.round((15.0d / (end - start)) * (d - start));
        } else {
            if (d < end || d > start) return 0;
            return -(int)Math.round((15.0d / (end - start)) * (d - end));
        }
    }

    public int redstoneFromCommand(int eyeId, String command) {
        String[] s = command.split(";");
        if (s.length == 4) {
            try {
                switch (Commands.valueOf(s[0])) {
                    case RANGE:
                        try {
                            return range(s[1], Double.parseDouble(s[2]), Double.parseDouble(s[3]), eyeId);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    case COMPARE:
                        return compare(s[1], Comparator.valueOf(s[2]), s[3], eyeId) ? 15 : 0;
                }
            } catch (IllegalArgumentException e) {} //invalid command/comparator
        }
        return 0;
    }

    protected int comparisonScore1(String search, String key) {
        if (isValidMethod(key))
            return StringUtils.getFuzzyDistance(search, key, Locale.ENGLISH); //could use GameSettings.language as locale
        else return Integer.MIN_VALUE;
    }

    protected int comparisonScore2(String search, String key) {
        if (isValidMethod(key))
            return (int)(StringUtils.getJaroWinklerDistance(search, key) * 100.0D);
        else return Integer.MIN_VALUE;
    }

    public ArrayList<String> sortedKeys(String currentSearch) {
        ArrayList<String> test = new ArrayList<>();
        for (Class<? extends Entity> clazz : cache.keySet())
            test.addAll(cache.get(clazz).methods());
        test.sort(new java.util.Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (currentSearch != null && !currentSearch.isEmpty()) {
                    int s1 = comparisonScore2(currentSearch, o1);
                    int s2 = comparisonScore2(currentSearch, o2);
                    if (s1 == s2) {
                        s1 = comparisonScore1(currentSearch, o1);
                        s2 = comparisonScore1(currentSearch, o2);
                    }
                    if (s1 != s2)
                        return s2 - s1;
                }
                return o1.compareTo(o2);
            }
        });
        return test;
    }
}
