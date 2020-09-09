package com.limachi.dimensional_bags.common.readers;

import com.limachi.dimensional_bags.common.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.function.Function;

public class EntityReader<T extends Entity> {

    static protected HashMap<EntityType<?>, EntityReader<?>> cache = new HashMap<>();

    protected T entity;

    protected HashMap<String, Function<T, String>> suppliersString = new HashMap<>();
    protected HashMap<String, Function<T, Integer>> suppliersInteger = new HashMap<>();
    protected HashMap<String, Function<T, Double>> suppliersDouble = new HashMap<>();
    protected HashMap<String, Function<T, Boolean>> suppliersBoolean = new HashMap<>();
    protected HashMap<String, HashMap<String, ?>> suppliers = new HashMap<>();

    public EntityReader(T entity) {
        this.entity = entity;
        EntityType<?> type = entity.getType();
        EntityReader<?> cached = cache.get(type);
        if (cached == null) {
            suppliers.put("String", suppliersString);
            suppliers.put("Integer", suppliersInteger);
            suppliers.put("Double", suppliersDouble);
            suppliers.put("Boolean", suppliersBoolean);
            initSuppliers();
            cache.put(type, this);
        }
        else
        {
            EntityReader<T> cast = (EntityReader<T>)cached;
            this.suppliers = cast.suppliers;
            this.suppliersInteger = cast.suppliersInteger;
            this.suppliersDouble = cast.suppliersDouble;
            this.suppliersBoolean = cast.suppliersBoolean;
            this.suppliersString = cast.suppliersString;
        }
    }

    protected abstract class CommonEntityReader extends Entity {
        public CommonEntityReader(EntityType<?> entityTypeIn, World worldIn) {
            super(entityTypeIn, worldIn);
        }
        public int getRideCooldown() { return rideCooldown; }
        public int getPortalCounter() { return portalCounter; }
        public boolean isInPortal() { return inPortal; }
        @Override
        public float getJumpFactor() { return super.getJumpFactor(); }
        @Override
        public float getSpeedFactor() { return super.getSpeedFactor(); }
        public boolean isInRain() {
            BlockPos blockpos = this.getPosition();
            return this.world.isRainingAt(blockpos) || this.world.isRainingAt(new BlockPos((double)blockpos.getX(), this.getBoundingBox().maxY, (double)blockpos.getZ()));
        }
        private boolean isInBubbleColumn() {
            return this.world.getBlockState(this.getPosition()).isIn(Blocks.BUBBLE_COLUMN);
        }

        @Override
        public int getPermissionLevel() { return super.getPermissionLevel(); }
    }

    protected void initSuppliers() {
        suppliersString.put("type", t->t.getType().toString());
        suppliersInteger.put("ride_cooldown", t->((CommonEntityReader)t).getRideCooldown());
        suppliersString.put("world", t-> WorldUtils.worldRKFromWorld(t.world).toString());
        suppliersDouble.put("previous_position_x", t->t.prevPosX);
        suppliersDouble.put("previous_position_y", t->t.prevPosY);
        suppliersDouble.put("previous_position_z", t->t.prevPosZ);
        suppliersDouble.put("position_x", Entity::getPosX);
        suppliersDouble.put("position_y", Entity::getPosY);
        suppliersDouble.put("position_z", Entity::getPosZ);
        suppliersInteger.put("block_position_x", t->t.getPosition().getX());
        suppliersInteger.put("block_position_y", t->t.getPosition().getY());
        suppliersInteger.put("block_position_z", t->t.getPosition().getZ());
        suppliersDouble.put("motion_x", t->t.getMotion().x);
        suppliersDouble.put("motion_y", t->t.getMotion().y);
        suppliersDouble.put("motion_z", t->t.getMotion().z);
        suppliersDouble.put("rotation_yaw", t->(double) t.rotationYaw);
        suppliersDouble.put("rotation_pitch", t-> (double) t.rotationPitch);
        suppliersDouble.put("previous_rotation_yaw", t-> (double) t.prevRotationYaw);
        suppliersDouble.put("previous_rotation_pitch", t-> (double) t.prevRotationPitch);
        suppliersBoolean.put("is_on_ground", Entity::isOnGround);
        suppliersBoolean.put("is_colliding_horizontally", t-> t.collidedHorizontally);
        suppliersBoolean.put("is_colliding_vertically", t-> t.collidedVertically);
        suppliersBoolean.put("did_velocity_chang", t-> t.velocityChanged);
        suppliersDouble.put("previous_distance_walked", t-> (double) t.prevDistanceWalkedModified);
        suppliersDouble.put("distance_walked", t-> (double) t.distanceWalkedModified);
        suppliersDouble.put("distance_walked_step", t-> (double) t.distanceWalkedOnStepModified);
        suppliersDouble.put("fall_distance", t-> (double) t.fallDistance);
        suppliersDouble.put("step_height", t-> (double) t.stepHeight);
        suppliersBoolean.put("is_no_clip", t-> t.noClip);
        suppliersInteger.put("ticks_existed", t-> t.ticksExisted);
        suppliersInteger.put("fire_tick", Entity::getFireTimer);
        suppliersBoolean.put("is_immune_to_fire", Entity::isImmuneToFire);
        suppliersBoolean.put("is_in_water", Entity::isInWater);
        suppliersBoolean.put("is_in_lava", Entity::isInLava);
        suppliersInteger.put("hurt_resistance_timer", t->t.hurtResistantTime);
        suppliersInteger.put("air", Entity::getAir);
        suppliersInteger.put("maximum_air", Entity::getMaxAir);
        suppliersBoolean.put("is_name_visible", Entity::isCustomNameVisible);
        suppliersBoolean.put("is_silent", Entity::isSilent);
        suppliersBoolean.put("has_no_gravity", Entity::hasNoGravity);
        suppliersInteger.put("chunk_coordinates_x", t->t.chunkCoordX);
        suppliersInteger.put("chunk_coordinates_y", t->t.chunkCoordY);
        suppliersInteger.put("chunk_coordinates_z", t->t.chunkCoordZ);
        suppliersBoolean.put("is_airborne", t->t.isAirBorne);
        suppliersBoolean.put("is_in_portal", t->((CommonEntityReader)t).isInPortal());
        suppliersInteger.put("portal_counter", t->((CommonEntityReader)t).getPortalCounter());
        suppliersBoolean.put("is_invulnerable", Entity::isInvulnerable);
        suppliersString.put("UUID", Entity::getCachedUniqueIdString);
        suppliersBoolean.put("is_glowing", t->((CommonEntityReader)t).isGlowing());
        suppliersDouble.put("eye_height", t->(double) t.getEyeHeight());
        suppliersDouble.put("width", t->(double) t.getSize(t.getPose()).width);
        suppliersDouble.put("height", t->(double) t.getSize(t.getPose()).height);
        suppliersBoolean.put("can_passenger_steer", Entity::canPassengerSteer);
        suppliersString.put("push_reaction", t->t.getPushReaction().toString());
        suppliersString.put("sound_category", t->t.getSoundCategory().getName());
        suppliersBoolean.put("is_vulnerable_to_fall", t->t.isInvulnerableTo(DamageSource.FALL));
        suppliersBoolean.put("is_vulnerable_to_anvil", t->t.isInvulnerableTo(DamageSource.ANVIL));
        suppliersBoolean.put("is_vulnerable_to_cactus", t->t.isInvulnerableTo(DamageSource.CACTUS));
        suppliersBoolean.put("is_vulnerable_to_craming", t->t.isInvulnerableTo(DamageSource.CRAMMING));
        suppliersBoolean.put("is_vulnerable_to_dragon_breath", t->t.isInvulnerableTo(DamageSource.DRAGON_BREATH));
        suppliersBoolean.put("is_vulnerable_to_drown", t->t.isInvulnerableTo(DamageSource.DROWN));
        suppliersBoolean.put("is_vulnerable_to_dryout", t->t.isInvulnerableTo(DamageSource.DRYOUT));
        suppliersBoolean.put("is_vulnerable_to_falling_block", t->t.isInvulnerableTo(DamageSource.FALLING_BLOCK));
        suppliersBoolean.put("is_vulnerable_to_fly_into_wall", t->t.isInvulnerableTo(DamageSource.FLY_INTO_WALL));
        suppliersBoolean.put("is_vulnerable_to_generic", t->t.isInvulnerableTo(DamageSource.GENERIC));
        suppliersBoolean.put("is_vulnerable_to_hot_floor", t->t.isInvulnerableTo(DamageSource.HOT_FLOOR));
        suppliersBoolean.put("is_vulnerable_to_in_fire", t->t.isInvulnerableTo(DamageSource.IN_FIRE));
        suppliersBoolean.put("is_vulnerable_to_in_wall", t->t.isInvulnerableTo(DamageSource.IN_WALL));
        suppliersBoolean.put("is_vulnerable_to_lava", t->t.isInvulnerableTo(DamageSource.LAVA));
        suppliersBoolean.put("is_vulnerable_to_lightning_bolt", t->t.isInvulnerableTo(DamageSource.LIGHTNING_BOLT));
        suppliersBoolean.put("is_vulnerable_to_magic", t->t.isInvulnerableTo(DamageSource.MAGIC));
        suppliersBoolean.put("is_vulnerable_to_on_fire", t->t.isInvulnerableTo(DamageSource.ON_FIRE));
        suppliersBoolean.put("is_vulnerable_to_out_of_world", t->t.isInvulnerableTo(DamageSource.OUT_OF_WORLD));
        suppliersBoolean.put("is_vulnerable_to_starve", t->t.isInvulnerableTo(DamageSource.STARVE));
        suppliersBoolean.put("is_vulnerable_to_sweet_berry_bush", t->t.isInvulnerableTo(DamageSource.SWEET_BERRY_BUSH));
        suppliersBoolean.put("is_vulnerable_to_wither", t->t.isInvulnerableTo(DamageSource.WITHER));
        suppliersBoolean.put("is_spectator", Entity::isSpectator);
        suppliersString.put("pose", t->t.getPose().name());
        suppliersDouble.put("jump_factor", t->(double)((CommonEntityReader)t).getJumpFactor());
        suppliersDouble.put("speed_factor", t->(double)((CommonEntityReader)t).getSpeedFactor());
        suppliersBoolean.put("is_in_rain", t->((CommonEntityReader)t).isInRain());
        suppliersBoolean.put("is_in_bubble_column", t->((CommonEntityReader)t).isInBubbleColumn());
        suppliersBoolean.put("can_swim", Entity::canSwim);
        suppliersString.put("entity_string", Entity::getEntityString);
        suppliersBoolean.put("is_alive", Entity::isAlive);
        suppliersBoolean.put("is_inside_opaque_block", Entity::isEntityInsideOpaqueBlock);
        suppliersBoolean.put("is_being_ridden", Entity::isBeingRidden);
        suppliersBoolean.put("is_passenger", Entity::isPassenger);
        suppliersBoolean.put("can_be_ridden_in_water", Entity::canBeRiddenInWater);
        suppliersBoolean.put("is_sprinting", Entity::isSprinting);
        suppliersBoolean.put("is_swiming", Entity::isSwimming);
        suppliersBoolean.put("is_invisible", Entity::isInvisible);
        suppliersString.put("team", t->t.getTeam() != null ? t.getTeam().getName() : "");
        suppliersString.put("team_color", t->t.getTeam() != null ? t.getTeam().getColor().getFriendlyName() : "");
        suppliersString.put("string", Entity::toString);
        suppliersBoolean.put("is_non_boss", Entity::isNonBoss);
        suppliersInteger.put("max_fall_height", Entity::getMaxFallHeight);
        suppliersBoolean.put("does_not_trigger_pressure_plate", Entity::doesEntityNotTriggerPressurePlate);
        suppliersBoolean.put("can_be_pushed_by_water", Entity::isPushedByWater);
        suppliersString.put("server", t->t.getServer() != null ? t.getServer().getServerHostname() : "");
        suppliersBoolean.put("is_one_player_riding", Entity::isOnePlayerRiding);
        suppliersInteger.put("permission_level", t->((CommonEntityReader)t).getPermissionLevel());
    }

    protected String keyType(String key) {
        for (String group : suppliers.keySet())
            if (suppliers.get(group).containsKey(key))
                return group;
        return "InvalidGroup";
    }

    public enum Comparator {
        EQUAL,
        NOT_EQUAL,
        LESS,
        MORE,
        LESS_OR_EQUAL,
        MORE_OR_EQUAL;
    }

    public enum Commands {
        RANGE_DOUBLE,
        RANGE_INTEGER,
        COMPARE_KEYS,
        COMPARE_KEY_CONSTANT;
    }

    protected boolean compare2keys(Comparator comparator, String key1, String key2) {
        String group1 = keyType(key1);
        String group2 = keyType(key2);
        if (group1.equals("InvalidGroup") || group2.equals("InvalidGroup") || !group1.equals(group2)) return comparator == Comparator.NOT_EQUAL || comparator == Comparator.MORE || comparator == Comparator.LESS;
        if (group1.equals("String")) {
            String str1 = suppliersString.get(key1).apply(entity);
            String str2 = suppliersString.get(key2).apply(entity);
            switch (comparator) {
                case EQUAL: return str1.equals(str2);
                case NOT_EQUAL: return !str1.equals(str2);
                case MORE: case MORE_OR_EQUAL: return str1.contains(str2);
                case LESS: case LESS_OR_EQUAL: return str2.contains(str1);
                default: return false;
            }
        } else if (group1.equals("Integer")) {
            int i1 = suppliersInteger.get(key1).apply(entity);
            int i2 = suppliersInteger.get(key2).apply(entity);
            switch (comparator) {
                case EQUAL: return i1 == i2;
                case NOT_EQUAL: return i1 != i2;
                case MORE: return i1 > i2;
                case MORE_OR_EQUAL: return i1 >= i2;
                case LESS: return i1 < i2;
                case LESS_OR_EQUAL: return i1 <= i2;
                default: return false;
            }
        } else if (group1.equals("Double")) {
            double i1 = suppliersDouble.get(key1).apply(entity);
            double i2 = suppliersDouble.get(key2).apply(entity);
            switch (comparator) {
                case EQUAL: return i1 == i2;
                case NOT_EQUAL: return i1 != i2;
                case MORE: return i1 > i2;
                case MORE_OR_EQUAL: return i1 >= i2;
                case LESS: return i1 < i2;
                case LESS_OR_EQUAL: return i1 <= i2;
                default: return false;
            }
        } else if (group1.equals("Boolean")) {
            boolean i1 = suppliersBoolean.get(key1).apply(entity);
            boolean i2 = suppliersBoolean.get(key2).apply(entity);
            switch (comparator) {
                case EQUAL: case MORE_OR_EQUAL: case LESS_OR_EQUAL: return i1 == i2;
                case NOT_EQUAL: case MORE: case LESS: return i1 != i2;
                default: return false;
            }
        }
        return comparator == Comparator.NOT_EQUAL || comparator == Comparator.MORE || comparator == Comparator.LESS;
    }

    protected boolean compare1key1constant(Comparator comparator, String key, String constant) {
        String group = keyType(key);
        if (group.equals("InvalidGroup")) return comparator == Comparator.NOT_EQUAL || comparator == Comparator.MORE || comparator == Comparator.LESS;
        if (group.equals("String")) {
            String str = suppliersString.get(key).apply(entity);
            switch (comparator) {
                case EQUAL: return str.equals(constant);
                case NOT_EQUAL: return !str.equals(constant);
                case MORE: case MORE_OR_EQUAL: return str.contains(constant);
                case LESS: case LESS_OR_EQUAL: return constant.contains(str);
                default: return false;
            }
        } else if (group.equals("Boolean")) {
            boolean k = suppliersBoolean.get(key).apply(entity);
            boolean b = constant.equals("true");
            switch (comparator) {
                case EQUAL: case MORE_OR_EQUAL: case LESS_OR_EQUAL: return k == b;
                case NOT_EQUAL: case MORE: case LESS: return k != b;
                default: return false;
            }
        } else if (group.equals("Integer")) {
            int k = suppliersInteger.get(key).apply(entity);
            int c;
            try {
                c = Integer.parseInt(constant);
            } catch (NumberFormatException e) {
                return false;
            }
            switch (comparator) {
                case EQUAL: return k == c;
                case NOT_EQUAL: return k != c;
                case MORE: return k > c;
                case MORE_OR_EQUAL: return k >= c;
                case LESS: return k < c;
                case LESS_OR_EQUAL: return k <= c;
                default: return false;
            }
        } else if (group.equals("Double")) {
            double k = suppliersDouble.get(key).apply(entity);
            double d;
            try {
                d = Double.parseDouble(constant);
            } catch (NumberFormatException e) {
                return false;
            }
            switch (comparator) {
                case EQUAL: return k == d;
                case NOT_EQUAL: return k != d;
                case MORE: return k > d;
                case MORE_OR_EQUAL: return k >= d;
                case LESS: return k < d;
                case LESS_OR_EQUAL: return k <= d;
                default: return false;
            }
        }
        return comparator == Comparator.NOT_EQUAL || comparator == Comparator.MORE || comparator == Comparator.LESS;
    }

    protected int rangeInt(String key, int start, int end) {
        if (suppliersInteger.containsKey(key)) {
            int k = suppliersInteger.get(key).apply(entity);
            if (start < end) {
                if (k < start || k > end) return 0;
                return (int)Math.round((15.0d / ((double)(end - start))) * (double)(k - start));
            } else {
                if (k < end || k > start) return 0;
                return -(int)Math.round((15.0d / ((double)(end - start))) * (double)(k - end));
            }
        }
        return 0;
    }

    protected int rangeDouble(String key, double start, double end) {
        if (suppliersDouble.containsKey(key)) {
            double k = suppliersDouble.get(key).apply(entity);
            if (start < end) {
                if (k < start || k > end) return 0;
                return (int)Math.round((15.0d / (end - start)) * (k - start));
            } else {
                if (k < end || k > start) return 0;
                return -(int)Math.round((15.0d / (end - start)) * (k - end));
            }
        }
        return 0;
    }

    public int redstoneFromCommand(String command) {
        String[] s = command.split(";");
        if (s.length == 4) {
            try {
                switch (Commands.valueOf(s[0])) {
                    case RANGE_DOUBLE:
                        try {
                            return rangeDouble(s[1], Double.parseDouble(s[2]), Double.parseDouble(s[3]));
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    case RANGE_INTEGER:
                        try {
                            return rangeInt(s[1], Integer.parseInt(s[2]), Integer.parseInt(s[3]));
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    case COMPARE_KEYS:
                        return compare2keys(Comparator.valueOf(s[2]), s[1], s[3]) ? 15 : 0;
                    case COMPARE_KEY_CONSTANT:
                        return compare1key1constant(Comparator.valueOf(s[2]), s[1], s[3]) ? 15 : 0;
                    default: return 0;
                }
            } catch (IllegalArgumentException e) {} //invalid command/comparator
        }
        return 0;
    }
}
