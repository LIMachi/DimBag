package com.limachi.dimensional_bags.common.readers;

import com.limachi.dimensional_bags.common.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;

public class EntityReader<T extends Entity> {

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

    static protected HashMap<EntityType<?>, EntityReader<?>> cache = new HashMap<>();
    protected T entity;
    protected HashMap<String, EntityValueReader> suppliers = new HashMap<>();

    public EntityReader(T entity) {
        this.entity = entity;
        EntityType<?> type = entity.getType();
        EntityReader<?> cached = cache.get(type);
        if (cached == null) {
            initSuppliers();
            cache.put(type, this);
        }
        else
        {
            EntityReader<T> cast = (EntityReader<T>)cached;
            this.suppliers = cast.suppliers;
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

    public enum ValueType {
        STRING,
        INTEGER,
        DOUBLE,
        BOOLEAN,
        INVALID
    }

    public class EntityValueReader {
        private final ValueType valueType;
        private final Function<T, Object> supplier;
        private final Class<? extends Entity> entityClass;

        protected EntityValueReader(ValueType valueType, Function<T, Object> supplier, Class<? extends Entity> entityClass) {
            this.valueType = valueType;
            this.supplier = supplier;
            this.entityClass = entityClass;
        }

        public boolean matchClass(T entity) { return entityClass.isInstance(entity); }
        public ValueType getValueType() { return valueType; }
        public Object getValue(T entity) { return supplier.apply(entity); }

        public ITextComponent getPrintable(String key, T entity) { return new TranslationTextComponent("entity_value_reader.printable", entityClass.toString(), key, getValue(entity), valueType.name()); }
    }

    protected void initSuppliers() {
        suppliers.put("type", new EntityValueReader(ValueType.STRING, t->t.getType().toString(), Entity.class));
        suppliers.put("ride_cooldown", new EntityValueReader(ValueType.INTEGER, t->((CommonEntityReader)t).getRideCooldown(), Entity.class));
        suppliers.put("world", new EntityValueReader(ValueType.STRING, t-> WorldUtils.worldRKFromWorld(t.world).toString(), Entity.class));
        suppliers.put("previous_position_x", new EntityValueReader(ValueType.DOUBLE, t->t.prevPosX, Entity.class));
        suppliers.put("previous_position_y", new EntityValueReader(ValueType.DOUBLE, t->t.prevPosY, Entity.class));
        suppliers.put("previous_position_z", new EntityValueReader(ValueType.DOUBLE, t->t.prevPosZ, Entity.class));
        suppliers.put("position_x", new EntityValueReader(ValueType.DOUBLE, Entity::getPosX, Entity.class));
        suppliers.put("position_y", new EntityValueReader(ValueType.DOUBLE, Entity::getPosY, Entity.class));
        suppliers.put("position_z", new EntityValueReader(ValueType.DOUBLE, Entity::getPosZ, Entity.class));
        suppliers.put("block_position_x", new EntityValueReader(ValueType.INTEGER, t->t.getPosition().getX(), Entity.class));
        suppliers.put("block_position_y", new EntityValueReader(ValueType.INTEGER, t->t.getPosition().getY(), Entity.class));
        suppliers.put("block_position_z", new EntityValueReader(ValueType.INTEGER, t->t.getPosition().getZ(), Entity.class));
        suppliers.put("motion_x", new EntityValueReader(ValueType.DOUBLE, t->t.getMotion().x, Entity.class));
        suppliers.put("motion_y", new EntityValueReader(ValueType.DOUBLE, t->t.getMotion().y, Entity.class));
        suppliers.put("motion_z", new EntityValueReader(ValueType.DOUBLE, t->t.getMotion().z, Entity.class));
        suppliers.put("rotation_yaw", new EntityValueReader(ValueType.DOUBLE, t->(double) t.rotationYaw, Entity.class));
        suppliers.put("rotation_pitch", new EntityValueReader(ValueType.DOUBLE, t-> (double) t.rotationPitch, Entity.class));
        suppliers.put("previous_rotation_yaw", new EntityValueReader(ValueType.DOUBLE, t-> (double) t.prevRotationYaw, Entity.class));
        suppliers.put("previous_rotation_pitch", new EntityValueReader(ValueType.DOUBLE, t-> (double) t.prevRotationPitch, Entity.class));
        suppliers.put("is_on_ground", new EntityValueReader(ValueType.BOOLEAN, Entity::isOnGround, Entity.class));
        suppliers.put("is_colliding_horizontally", new EntityValueReader(ValueType.BOOLEAN, t-> t.collidedHorizontally, Entity.class));
        suppliers.put("is_colliding_vertically", new EntityValueReader(ValueType.BOOLEAN, t-> t.collidedVertically, Entity.class));
        suppliers.put("did_velocity_chang", new EntityValueReader(ValueType.BOOLEAN, t-> t.velocityChanged, Entity.class));
        suppliers.put("previous_distance_walked", new EntityValueReader(ValueType.DOUBLE, t-> (double) t.prevDistanceWalkedModified, Entity.class));
        suppliers.put("distance_walked", new EntityValueReader(ValueType.DOUBLE, t-> (double) t.distanceWalkedModified, Entity.class));
        suppliers.put("distance_walked_step", new EntityValueReader(ValueType.DOUBLE, t-> (double) t.distanceWalkedOnStepModified, Entity.class));
        suppliers.put("fall_distance", new EntityValueReader(ValueType.DOUBLE, t-> (double) t.fallDistance, Entity.class));
        suppliers.put("step_height", new EntityValueReader(ValueType.DOUBLE, t-> (double) t.stepHeight, Entity.class));
        suppliers.put("is_no_clip", new EntityValueReader(ValueType.BOOLEAN, t-> t.noClip, Entity.class));
        suppliers.put("ticks_existed", new EntityValueReader(ValueType.INTEGER, t-> t.ticksExisted, Entity.class));
        suppliers.put("fire_tick", new EntityValueReader(ValueType.INTEGER, Entity::getFireTimer, Entity.class));
        suppliers.put("is_immune_to_fire", new EntityValueReader(ValueType.BOOLEAN, Entity::isImmuneToFire, Entity.class));
        suppliers.put("is_in_water", new EntityValueReader(ValueType.BOOLEAN, Entity::isInWater, Entity.class));
        suppliers.put("is_in_lava", new EntityValueReader(ValueType.BOOLEAN, Entity::isInLava, Entity.class));
        suppliers.put("hurt_resistance_timer", new EntityValueReader(ValueType.INTEGER, t->t.hurtResistantTime, Entity.class));
        suppliers.put("air", new EntityValueReader(ValueType.INTEGER, Entity::getAir, Entity.class));
        suppliers.put("maximum_air", new EntityValueReader(ValueType.INTEGER, Entity::getMaxAir, Entity.class));
        suppliers.put("is_name_visible", new EntityValueReader(ValueType.BOOLEAN, Entity::isCustomNameVisible, Entity.class));
        suppliers.put("is_silent", new EntityValueReader(ValueType.BOOLEAN, Entity::isSilent, Entity.class));
        suppliers.put("has_no_gravity", new EntityValueReader(ValueType.BOOLEAN, Entity::hasNoGravity, Entity.class));
        suppliers.put("chunk_coordinates_x", new EntityValueReader(ValueType.INTEGER, t->t.chunkCoordX, Entity.class));
        suppliers.put("chunk_coordinates_y", new EntityValueReader(ValueType.INTEGER, t->t.chunkCoordY, Entity.class));
        suppliers.put("chunk_coordinates_z", new EntityValueReader(ValueType.INTEGER, t->t.chunkCoordZ, Entity.class));
        suppliers.put("is_airborne", new EntityValueReader(ValueType.BOOLEAN, t->t.isAirBorne, Entity.class));
        suppliers.put("is_in_portal", new EntityValueReader(ValueType.BOOLEAN, t->((CommonEntityReader)t).isInPortal(), Entity.class));
        suppliers.put("portal_counter", new EntityValueReader(ValueType.INTEGER, t->((CommonEntityReader)t).getPortalCounter(), Entity.class));
        suppliers.put("is_invulnerable", new EntityValueReader(ValueType.BOOLEAN, Entity::isInvulnerable, Entity.class));
        suppliers.put("UUID", new EntityValueReader(ValueType.STRING, Entity::getCachedUniqueIdString, Entity.class));
        suppliers.put("is_glowing", new EntityValueReader(ValueType.BOOLEAN, t->((CommonEntityReader)t).isGlowing(), Entity.class));
        suppliers.put("eye_height", new EntityValueReader(ValueType.DOUBLE, t->(double) t.getEyeHeight(), Entity.class));
        suppliers.put("width", new EntityValueReader(ValueType.DOUBLE, t->(double) t.getSize(t.getPose()).width, Entity.class));
        suppliers.put("height", new EntityValueReader(ValueType.DOUBLE, t->(double) t.getSize(t.getPose()).height, Entity.class));
        suppliers.put("can_passenger_steer", new EntityValueReader(ValueType.BOOLEAN, Entity::canPassengerSteer, Entity.class));
        suppliers.put("push_reaction", new EntityValueReader(ValueType.STRING, t->t.getPushReaction().toString(), Entity.class));
        suppliers.put("sound_category", new EntityValueReader(ValueType.STRING, t->t.getSoundCategory().getName(), Entity.class));
        suppliers.put("is_vulnerable_to_fall", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.FALL), Entity.class));
        suppliers.put("is_vulnerable_to_anvil", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.ANVIL), Entity.class));
        suppliers.put("is_vulnerable_to_cactus", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.CACTUS), Entity.class));
        suppliers.put("is_vulnerable_to_craming", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.CRAMMING), Entity.class));
        suppliers.put("is_vulnerable_to_dragon_breath", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.DRAGON_BREATH), Entity.class));
        suppliers.put("is_vulnerable_to_drown", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.DROWN), Entity.class));
        suppliers.put("is_vulnerable_to_dryout", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.DRYOUT), Entity.class));
        suppliers.put("is_vulnerable_to_falling_block", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.FALLING_BLOCK), Entity.class));
        suppliers.put("is_vulnerable_to_fly_into_wall", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.FLY_INTO_WALL), Entity.class));
        suppliers.put("is_vulnerable_to_generic", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.GENERIC), Entity.class));
        suppliers.put("is_vulnerable_to_hot_floor", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.HOT_FLOOR), Entity.class));
        suppliers.put("is_vulnerable_to_in_fire", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.IN_FIRE), Entity.class));
        suppliers.put("is_vulnerable_to_in_wall", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.IN_WALL), Entity.class));
        suppliers.put("is_vulnerable_to_lava", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.LAVA), Entity.class));
        suppliers.put("is_vulnerable_to_lightning_bolt", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.LIGHTNING_BOLT), Entity.class));
        suppliers.put("is_vulnerable_to_magic", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.MAGIC), Entity.class));
        suppliers.put("is_vulnerable_to_on_fire", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.ON_FIRE), Entity.class));
        suppliers.put("is_vulnerable_to_out_of_world", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.OUT_OF_WORLD), Entity.class));
        suppliers.put("is_vulnerable_to_starve", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.STARVE), Entity.class));
        suppliers.put("is_vulnerable_to_sweet_berry_bush", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.SWEET_BERRY_BUSH), Entity.class));
        suppliers.put("is_vulnerable_to_wither", new EntityValueReader(ValueType.BOOLEAN, t->t.isInvulnerableTo(DamageSource.WITHER), Entity.class));
        suppliers.put("is_spectator", new EntityValueReader(ValueType.BOOLEAN, Entity::isSpectator, Entity.class));
        suppliers.put("pose", new EntityValueReader(ValueType.STRING, t->t.getPose().name(), Entity.class));
        suppliers.put("jump_factor", new EntityValueReader(ValueType.DOUBLE, t->(double)((CommonEntityReader)t).getJumpFactor(), Entity.class));
        suppliers.put("speed_factor", new EntityValueReader(ValueType.DOUBLE, t->(double)((CommonEntityReader)t).getSpeedFactor(), Entity.class));
        suppliers.put("is_in_rain", new EntityValueReader(ValueType.BOOLEAN, t->((CommonEntityReader)t).isInRain(), Entity.class));
        suppliers.put("is_in_bubble_column", new EntityValueReader(ValueType.BOOLEAN, t->((CommonEntityReader)t).isInBubbleColumn(), Entity.class));
        suppliers.put("can_swim", new EntityValueReader(ValueType.BOOLEAN, Entity::canSwim, Entity.class));
        suppliers.put("entity_string", new EntityValueReader(ValueType.STRING, Entity::getEntityString, Entity.class));
        suppliers.put("is_alive", new EntityValueReader(ValueType.BOOLEAN, Entity::isAlive, Entity.class));
        suppliers.put("is_inside_opaque_block", new EntityValueReader(ValueType.BOOLEAN, Entity::isEntityInsideOpaqueBlock, Entity.class));
        suppliers.put("is_being_ridden", new EntityValueReader(ValueType.BOOLEAN, Entity::isBeingRidden, Entity.class));
        suppliers.put("is_passenger", new EntityValueReader(ValueType.BOOLEAN, Entity::isPassenger, Entity.class));
        suppliers.put("can_be_ridden_in_water", new EntityValueReader(ValueType.BOOLEAN, Entity::canBeRiddenInWater, Entity.class));
        suppliers.put("is_sprinting", new EntityValueReader(ValueType.BOOLEAN, Entity::isSprinting, Entity.class));
        suppliers.put("is_swiming", new EntityValueReader(ValueType.BOOLEAN, Entity::isSwimming, Entity.class));
        suppliers.put("is_invisible", new EntityValueReader(ValueType.BOOLEAN, Entity::isInvisible, Entity.class));
        suppliers.put("team", new EntityValueReader(ValueType.STRING, t->t.getTeam() != null ? t.getTeam().getName() : "", Entity.class));
        suppliers.put("team_color", new EntityValueReader(ValueType.STRING, t->t.getTeam() != null ? t.getTeam().getColor().getFriendlyName() : "", Entity.class));
        suppliers.put("string", new EntityValueReader(ValueType.STRING, Entity::toString, Entity.class));
        suppliers.put("is_non_boss", new EntityValueReader(ValueType.BOOLEAN, Entity::isNonBoss, Entity.class));
        suppliers.put("max_fall_height", new EntityValueReader(ValueType.INTEGER, Entity::getMaxFallHeight, Entity.class));
        suppliers.put("does_not_trigger_pressure_plate", new EntityValueReader(ValueType.BOOLEAN, Entity::doesEntityNotTriggerPressurePlate, Entity.class));
        suppliers.put("can_be_pushed_by_water", new EntityValueReader(ValueType.BOOLEAN, Entity::isPushedByWater, Entity.class));
        suppliers.put("server", new EntityValueReader(ValueType.STRING, t->t.getServer() != null ? t.getServer().getServerHostname() : "", Entity.class));
        suppliers.put("is_one_player_riding", new EntityValueReader(ValueType.BOOLEAN, Entity::isOnePlayerRiding, Entity.class));
        suppliers.put("permission_level", new EntityValueReader(ValueType.INTEGER, t->((CommonEntityReader)t).getPermissionLevel(), Entity.class));
    }

    protected EntityValueReader getValueReader(String key) {
        EntityValueReader reader = suppliers.get(key);
        if (reader != null && reader.matchClass(entity))
                return reader;
        return null;
    }

    protected boolean compare2keys(Comparator comparator, String key1, String key2) {
        EntityValueReader reader1 = getValueReader(key1);
        EntityValueReader reader2 = getValueReader(key2);
        if (reader1 == null || reader2 == null || reader1.valueType != reader2.valueType) return comparator == Comparator.NOT_EQUAL || comparator == Comparator.MORE || comparator == Comparator.LESS;
        switch (reader1.valueType) {
            case STRING:
                String str1 = (String)reader1.getValue(entity);
                String str2 = (String)reader2.getValue(entity);
                switch (comparator) {
                    case EQUAL:
                        return str1.equals(str2);
                    case NOT_EQUAL:
                        return !str1.equals(str2);
                    case MORE:
                    case MORE_OR_EQUAL:
                        return str1.contains(str2);
                    case LESS:
                    case LESS_OR_EQUAL:
                        return str2.contains(str1);
                    default:
                        return false;
                }
            case INTEGER:
                int i1 = (Integer)reader1.getValue(entity);
                int i2 = (Integer)reader2.getValue(entity);
                switch (comparator) {
                    case EQUAL: return i1 == i2;
                    case NOT_EQUAL: return i1 != i2;
                    case MORE: return i1 > i2;
                    case MORE_OR_EQUAL: return i1 >= i2;
                    case LESS: return i1 < i2;
                    case LESS_OR_EQUAL: return i1 <= i2;
                    default: return false;
                }
            case DOUBLE:
                double d1 = (Double)reader1.getValue(entity);
                double d2 = (Double)reader2.getValue(entity);
                switch (comparator) {
                    case EQUAL: return d1 == d2;
                    case NOT_EQUAL: return d1 != d2;
                    case MORE: return d1 > d2;
                    case MORE_OR_EQUAL: return d1 >= d2;
                    case LESS: return d1 < d2;
                    case LESS_OR_EQUAL: return d1 <= d2;
                    default: return false;
                }
            case BOOLEAN:
                boolean b1 = (Boolean)reader1.getValue(entity);
                boolean b2 = (Boolean)reader2.getValue(entity);
                switch (comparator) {
                    case EQUAL:
                    case MORE_OR_EQUAL:
                    case LESS_OR_EQUAL:
                        return b1 == b2;
                    case NOT_EQUAL:
                    case MORE:
                    case LESS:
                        return b1 != b2;
                    default:
                        return false;
                }
            }
        return comparator == Comparator.NOT_EQUAL || comparator == Comparator.MORE || comparator == Comparator.LESS;
    }

    protected boolean compare1key1constant(Comparator comparator, String key, String constant) {
        EntityValueReader reader = getValueReader(key);
        if (reader == null) return comparator == Comparator.NOT_EQUAL || comparator == Comparator.MORE || comparator == Comparator.LESS;
        switch (reader.valueType) {
            case STRING:
                String str = (String)reader.getValue(entity);
                switch (comparator) {
                    case EQUAL:
                        return str.equals(constant);
                    case NOT_EQUAL:
                        return !str.equals(constant);
                    case MORE:
                    case MORE_OR_EQUAL:
                        return str.contains(constant);
                    case LESS:
                    case LESS_OR_EQUAL:
                        return constant.contains(str);
                    default:
                        return false;
                }
            case BOOLEAN:
                boolean b1 = (Boolean)reader.getValue(entity);
                boolean b2 = constant.equals("true");
                switch (comparator) {
                    case EQUAL:
                    case MORE_OR_EQUAL:
                    case LESS_OR_EQUAL:
                        return b1 == b2;
                    case NOT_EQUAL:
                    case MORE:
                    case LESS:
                        return b1 != b2;
                    default:
                        return false;
                }
            case INTEGER:
                int i1 = (Integer)reader.getValue(entity);
                int i2;
                try {
                    i2 = Integer.parseInt(constant);
                } catch (NumberFormatException e) {
                    return false;
                }
                switch (comparator) {
                    case EQUAL:
                        return i1 == i2;
                    case NOT_EQUAL:
                        return i1 != i2;
                    case MORE:
                        return i1 > i2;
                    case MORE_OR_EQUAL:
                        return i1 >= i2;
                    case LESS:
                        return i1 < i2;
                    case LESS_OR_EQUAL:
                        return i1 <= i2;
                    default:
                        return false;
                }
            case DOUBLE:
                double d1 = (Double)reader.getValue(entity);
                double d2;
                try {
                    d2 = Double.parseDouble(constant);
                } catch (NumberFormatException e) {
                    return false;
                }
                switch (comparator) {
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
                        return false;
                }
            }
        return comparator == Comparator.NOT_EQUAL || comparator == Comparator.MORE || comparator == Comparator.LESS;
    }

    protected int rangeInt(String key, int start, int end) {
        EntityValueReader reader = getValueReader(key);
        if (reader != null && reader.valueType == ValueType.INTEGER) {
            int k = (Integer)reader.getValue(entity);
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
        EntityValueReader reader = getValueReader(key);
        if (reader != null && reader.valueType == ValueType.DOUBLE) {
            double k = (Double)reader.getValue(entity);
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

    public int comparisonScore1(String search, String key) {
        if (suppliers.get(key).matchClass(entity))
            return StringUtils.getFuzzyDistance(search, key, Locale.ENGLISH); //could use GameSettings.language as locale
        else return Integer.MIN_VALUE;
    }

    public int comparisonScore2(String search, String key) {
        if (suppliers.get(key).matchClass(entity))
            return (int)(StringUtils.getJaroWinklerDistance(search, key) * 100.0D);
        else return Integer.MIN_VALUE;
    }

    public ArrayList<String> sortedKeys(String currentSearch) {
        ArrayList<String> test = new ArrayList(suppliers.keySet());
        test.sort(new java.util.Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (currentSearch != null && !currentSearch.isEmpty()) {
                    int s1 = comparisonScore1(currentSearch, o1);
                    int s2 = comparisonScore1(currentSearch, o2);
                    if (s1 == s2) {
                        s1 = comparisonScore2(currentSearch, o1);
                        s2 = comparisonScore2(currentSearch, o2);
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
