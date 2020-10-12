package com.limachi.dimensional_bags.common.readers;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class EntityReader<T extends Entity> {

    protected static class SuppliersMap<O extends Entity> {
        private Class<O> clazz;
        private final HashMap<String, Function<O, Object>> map = new HashMap<>();

        public SuppliersMap(Class<O> clazz) { this.clazz = clazz; }

//        public SuppliersMap(Class<O> clazz, SuppliersMap<?> original) {
//            this(clazz);
//            if (original.clazz.isAssignableFrom(clazz))
//                map.putAll((Map<String, ? extends Function<O, Object>>) original.map);
//        }

        public boolean isValidMethod(String name, Object entity) {
            return entity instanceof Entity && clazz.isInstance(entity) && map.containsKey(name);
        }

        public Set<String> methods() { return map.keySet(); }

        public Object get(String name, Object entity) {
            if (isValidMethod(name, entity))
                return map.get(name).apply(clazz.cast(entity));
            return null;
        }

        public void register(String name, Function<O, Object> supplier) { map.put(name, supplier); }
    }

    static final protected HashMap<Class<? extends Entity>, SuppliersMap<?>> cache = new HashMap<>();

    protected SuppliersMap<T> suppliers;
    protected final Entity entity;

    public EntityReader(T entity) {
        this.entity = entity;
        Class<? extends Entity> clazz = entity.getClass();
        SuppliersMap<T> cached = (SuppliersMap<T>)cache.get(clazz);
        if (cached == null) {
            suppliers = new SuppliersMap<T>((Class<T>)entity.getClass());
            registerSuppliers();
            cache.put(clazz, suppliers);
        } else
            suppliers = cached;
    }

    protected void registerSuppliers() {
        suppliers.register("type", t -> t.getType().toString());
        suppliers.register("world", t -> t.world.getDimensionKey().toString());
        suppliers.register("previous_position_x", t -> t.prevPosX);
        suppliers.register("previous_position_y", t -> t.prevPosY);
        suppliers.register("previous_position_z", t -> t.prevPosZ);
        suppliers.register("position_x", Entity::getPosX);
        suppliers.register("position_y", Entity::getPosY);
        suppliers.register("position_z", Entity::getPosZ);
        suppliers.register("block_position_x", t -> t.getPosition().getX());
        suppliers.register("block_position_y", t -> t.getPosition().getY());
        suppliers.register("block_position_z", t -> t.getPosition().getZ());
        suppliers.register("motion_x", t -> t.getMotion().x);
        suppliers.register("motion_y", t -> t.getMotion().y);
        suppliers.register("motion_z", t -> t.getMotion().z);
        suppliers.register("rotation_yaw", t -> t.rotationYaw);
        suppliers.register("rotation_pitch", t -> t.rotationPitch);
        suppliers.register("previous_rotation_yaw", t -> t.prevRotationYaw);
        suppliers.register("previous_rotation_pitch", t -> t.prevRotationPitch);
        suppliers.register("is_on_ground", Entity::isOnGround);
        suppliers.register("is_colliding_horizontally", t -> t.collidedHorizontally);
        suppliers.register("is_colliding_vertically", t -> t.collidedVertically);
        suppliers.register("did_velocity_chang", t -> t.velocityChanged);
        suppliers.register("previous_distance_walked", t -> t.prevDistanceWalkedModified);
        suppliers.register("distance_walked", t -> t.distanceWalkedModified);
        suppliers.register("distance_walked_step", t -> t.distanceWalkedOnStepModified);
        suppliers.register("fall_distance", t -> t.fallDistance);
        suppliers.register("step_height", t -> t.stepHeight);
        suppliers.register("is_no_clip", t -> t.noClip);
        suppliers.register("ticks_existed", t -> t.ticksExisted);
        suppliers.register("fire_tick", Entity::getFireTimer);
        suppliers.register("is_immune_to_fire", Entity::isImmuneToFire);
        suppliers.register("is_in_water", Entity::isInWater);
        suppliers.register("is_in_lava", Entity::isInLava);
        suppliers.register("hurt_resistance_timer", t -> t.hurtResistantTime);
        suppliers.register("air", Entity::getAir);
        suppliers.register("maximum_air", Entity::getMaxAir);
        suppliers.register("is_name_visible", Entity::isCustomNameVisible);
        suppliers.register("is_silent", Entity::isSilent);
        suppliers.register("has_no_gravity", Entity::hasNoGravity);
        suppliers.register("chunk_coordinates_x", t -> t.chunkCoordX);
        suppliers.register("chunk_coordinates_y", t -> t.chunkCoordY);
        suppliers.register("chunk_coordinates_z", t -> t.chunkCoordZ);
        suppliers.register("is_airborne", t -> t.isAirBorne);
        suppliers.register("is_invulnerable", Entity::isInvulnerable);
        suppliers.register("UUID", Entity::getCachedUniqueIdString);
        suppliers.register("eye_height", Entity::getEyeHeight);
        suppliers.register("width", t -> t.getSize(t.getPose()).width);
        suppliers.register("height", t -> t.getSize(t.getPose()).height);
        suppliers.register("can_passenger_steer", Entity::canPassengerSteer);
        suppliers.register("push_reaction", t -> t.getPushReaction().toString());
        suppliers.register("sound_category", t -> t.getSoundCategory().getName());
        suppliers.register("is_vulnerable_to_fall", t -> t.isInvulnerableTo(DamageSource.FALL));
        suppliers.register("is_vulnerable_to_anvil", t -> t.isInvulnerableTo(DamageSource.ANVIL));
        suppliers.register("is_vulnerable_to_cactus", t -> t.isInvulnerableTo(DamageSource.CACTUS));
        suppliers.register("is_vulnerable_to_craming", t -> t.isInvulnerableTo(DamageSource.CRAMMING));
        suppliers.register("is_vulnerable_to_dragon_breath", t -> t.isInvulnerableTo(DamageSource.DRAGON_BREATH));
        suppliers.register("is_vulnerable_to_drown", t -> t.isInvulnerableTo(DamageSource.DROWN));
        suppliers.register("is_vulnerable_to_drysuppliers", t -> t.isInvulnerableTo(DamageSource.DRYOUT));
        suppliers.register("is_vulnerable_to_falling_block", t -> t.isInvulnerableTo(DamageSource.FALLING_BLOCK));
        suppliers.register("is_vulnerable_to_fly_into_wall", t -> t.isInvulnerableTo(DamageSource.FLY_INTO_WALL));
        suppliers.register("is_vulnerable_to_generic", t -> t.isInvulnerableTo(DamageSource.GENERIC));
        suppliers.register("is_vulnerable_to_hot_floor", t -> t.isInvulnerableTo(DamageSource.HOT_FLOOR));
        suppliers.register("is_vulnerable_to_in_fire", t -> t.isInvulnerableTo(DamageSource.IN_FIRE));
        suppliers.register("is_vulnerable_to_in_wall", t -> t.isInvulnerableTo(DamageSource.IN_WALL));
        suppliers.register("is_vulnerable_to_lava", t -> t.isInvulnerableTo(DamageSource.LAVA));
        suppliers.register("is_vulnerable_to_lightning_bolt", t -> t.isInvulnerableTo(DamageSource.LIGHTNING_BOLT));
        suppliers.register("is_vulnerable_to_magic", t -> t.isInvulnerableTo(DamageSource.MAGIC));
        suppliers.register("is_vulnerable_to_on_fire", t -> t.isInvulnerableTo(DamageSource.ON_FIRE));
        suppliers.register("is_vulnerable_to_suppliers_of_world", t -> t.isInvulnerableTo(DamageSource.OUT_OF_WORLD));
        suppliers.register("is_vulnerable_to_starve", t -> t.isInvulnerableTo(DamageSource.STARVE));
        suppliers.register("is_vulnerable_to_sweet_berry_bush", t -> t.isInvulnerableTo(DamageSource.SWEET_BERRY_BUSH));
        suppliers.register("is_vulnerable_to_wither", t -> t.isInvulnerableTo(DamageSource.WITHER));
        suppliers.register("is_spectator", Entity::isSpectator);
        suppliers.register("pose", t -> t.getPose().name());
        suppliers.register("can_swim", Entity::canSwim);
        suppliers.register("is_alive", Entity::isAlive);
        suppliers.register("is_inside_opaque_block", Entity::isEntityInsideOpaqueBlock);
        suppliers.register("is_being_ridden", Entity::isBeingRidden);
        suppliers.register("is_passenger", Entity::isPassenger);
        suppliers.register("can_be_ridden_in_water", Entity::canBeRiddenInWater);
        suppliers.register("is_sprinting", Entity::isSprinting);
        suppliers.register("is_swiming", Entity::isSwimming);
        suppliers.register("is_invisible", Entity::isInvisible);
        suppliers.register("team", t -> t.getTeam() != null ? t.getTeam().getName() : "");
        suppliers.register("team_color", t -> t.getTeam() != null ? t.getTeam().getColor().getFriendlyName() : "");
        suppliers.register("string", Entity::toString);
        suppliers.register("is_non_boss", Entity::isNonBoss);
        suppliers.register("max_fall_height", Entity::getMaxFallHeight);
        suppliers.register("does_not_trigger_pressure_plate", Entity::doesEntityNotTriggerPressurePlate);
        suppliers.register("can_be_pushed_by_water", Entity::isPushedByWater);
        suppliers.register("is_one_player_riding", Entity::isOnePlayerRiding);
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

    protected boolean compare(String key, Comparator cmp, String constant) {
        Object o = suppliers.get(key, entity);
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

    protected int range(String key, double start, double end) {
        Object o = suppliers.get(key, entity);
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

    public int redstoneFromCommand(String command) {
        String[] s = command.split(";");
        if (s.length == 4) {
            try {
                switch (Commands.valueOf(s[0])) {
                    case RANGE:
                        try {
                            return range(s[1], Double.parseDouble(s[2]), Double.parseDouble(s[3]));
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    case COMPARE:
                        return compare(s[1], Comparator.valueOf(s[2]), s[3]) ? 15 : 0;
                }
            } catch (IllegalArgumentException e) {} //invalid command/comparator
        }
        return 0;
    }

    protected int comparisonScore1(String search, String key) {
        if (suppliers.isValidMethod(key, entity))
            return StringUtils.getFuzzyDistance(search, key, Locale.ENGLISH); //could use GameSettings.language as locale
        else return Integer.MIN_VALUE;
    }

    protected int comparisonScore2(String search, String key) {
        if (suppliers.isValidMethod(key, entity))
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
