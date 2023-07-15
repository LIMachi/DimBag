package com.limachi.dim_bag.entities.utils;

import com.limachi.lim_lib.reflection.Methods;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class EntityObserver {
    public static final HashMap<String, BiFunction<Double, Double, Double>> DOUBLE_OPERATIONS = new HashMap<>();
    static {
        DOUBLE_OPERATIONS.put("+", Double::sum);
        DOUBLE_OPERATIONS.put("-", (i, p)->i - p);
        DOUBLE_OPERATIONS.put("*", (i, p)->i * p);
        DOUBLE_OPERATIONS.put("%", (i, p)->p != 0. ? i % p : 0.);
        DOUBLE_OPERATIONS.put("/", (i, p)->p != 0. ? i / p : 0.);
        DOUBLE_OPERATIONS.put("==", (i, p)-> Objects.equals(i, p) ? 1. : 0.);
        DOUBLE_OPERATIONS.put("!=", (i, p)-> !Objects.equals(i, p) ? 1. : 0.);
        DOUBLE_OPERATIONS.put("<=", (i, p)->i <= p ? 1. : 0.);
        DOUBLE_OPERATIONS.put(">=", (i, p)->i >= p ? 1. : 0.);
        DOUBLE_OPERATIONS.put("<", (i, p)->i < p ? 1. : 0.);
        DOUBLE_OPERATIONS.put(">", (i, p)->i > p ? 1. : 0.);
        DOUBLE_OPERATIONS.put("&&", (i, p)->i != 0. && p != 0. ? 1. : 0.);
        DOUBLE_OPERATIONS.put("||", (i, p)->i != 0. || p != 0. ? 1. : 0.);
    }

    public static final String[] ENTITY_BOOLEAN_GET_COMMANDS = {"isSpectator", "isOnPortalCooldown", "onGround", "isSilent", "isNoGravity", "dampensVibrations", "fireImmune", "isInWater", "isInWaterOrRain", "isInWaterRainOrBubble", "isInWaterOrBubble", "isUnderWater", "canSpawnSprintParticle", "isInLava", "canBeHitByProjectile", "isPickable", "isPushable", "isAlive", "isInWall", "canBeCollidedWith", "showVehicleHealth", "isOnFire", "isPassenger", "isVehicle", "dismountsUnderwater", "isShiftKeyDown", "isSteppingCarefully", "isSuppressingBounce", "isDiscrete", "isDescending", "isCrouching", "isSprinting", "isSwimming", "isVisuallySwimming", "isVisuallyCrawling", "isCurrentlyGlowing", "isInvisible", "isOnRails", "isFullyFrozen", "isAttackable", "isInvulnerable", "canChangeDimensions", "isIgnoringBlockTriggers", "displayFireAnimation", "isPushedByFluid", "hasCustomName", "isCustomNameVisible", "shouldShowName", "ignoreExplosion", "onlyOpCanSetNbt", "hasExactlyOnePlayerPassenger", "isControlledByLocalInstance", "isEffectiveAi", "acceptsSuccess", "acceptsFailure", "shouldInformAdmins", "touchingUnloadedChunk", "canFreeze", "isFreezing", "canSprint", "shouldBeSaved", "isAlwaysTicking", "canUpdate", "emitsAnything", "emitsEvents", "emitsSounds", "shouldDestroy", "shouldSave", "alwaysAccepts"};
    public static final String[] LIVING_ENTITY_BOOLEAN_GET_COMMANDS = {"canBreatheUnderwater", "canSpawnSoulSpeedParticle", "isBaby", "shouldDropExperience", "shouldDiscardFriction", "canBeSeenAsEnemy", "canBeSeenByAnyone", "removeAllEffects", "isInvertedHealAndHarm", "isDeadOrDying", "wasExperienceConsumed", "onClimbable", "isSensitiveToWater", "isAutoSpinAttack", "isUsingItem", "isBlocking", "isSuppressingSlidingDownLadder", "isFallFlying", "isAffectedByPotions", "attackable", "isSleeping", "canDisableShield"};
    public static final String[] PLAYER_BOOLEAN_GET_COMMANDS = {"isSecondaryUseActive", "isTextFilteringEnabled", "isAffectedByFluids", "isLocalPlayer", "hasContainerOpen", "isSleepingLongEnough", "tryToStartFallFlying", "isHurt", "mayBuild", "shouldShowName", "canBeHitByProjectile", "isSwimming", "isPushedByFluid", "isReducedDebugInfo", "canUseGameMasterBlocks", "isAlwaysTicking", "isScoping", "shouldBeSaved", "canSprint"};
    public static final String[] ENTITY_INT_GET_COMMANDS = {"getTeamColor", "getId", "hashCode", "getPortalCooldown", "getPortalWaitTime", "getRemainingFireTicks", "getDimensionChangingDelay", "getMaxAirSupply", "getAirSupply", "getTicksFrozen", "getTicksRequiredToFreeze", "getMaxFallDistance"};
    public static final String[] LIVING_ENTITY_INT_GET_COMMANDS = {"getExperienceReward", "getLastHurtByMobTimestamp", "getLastHurtMobTimestamp", "getNoActionTime", "getArmorValue", "getUseItemRemainingTicks", "getTicksUsingItem", "getFallFlyingTicks"};
    public static final String[] PLAYER_INT_GET_COMMANDS = {"getScore", "getSleepTimer", "getEnchantmentSeed", "getXpNeededForNextLevel"};
    public static final String[] ENTITY_DOUBLE_GET_COMMANDS = {"getMyRidingOffset", "getPassengersRidingOffset", "getFluidJumpThreshold", "getRandomY", "getEyeY"};
    public static final String[] ENTITY_FLOAT_GET_COMMANDS = {"getLightLevelDependentMagicValue", "getPickRadius", "getPercentFrozen", "getYHeadRot", "getNameTagOffsetY", "getYRot", "getVisualRotationYInDegrees", "getXRot", "maxUpStep"};
    public static final String[] LIVING_ENTITY_FLOAT_GET_COMMANDS = {"getScale", "getHealth", "getHurtDir", "getArmorCoverPercentage", "getVoicePitch", "getJumpBoostPower", "getSpeed", "getAbsorptionAmount"};
    public static final String[] PLAYER_FLOAT_GET_COMMANDS = {"getCurrentItemAttackStrengthDelay", "getLuck"};
    public static final List<String> COMBINED_GETTERS = new ArrayList<>();
    static {
        COMBINED_GETTERS.addAll(List.of(ENTITY_BOOLEAN_GET_COMMANDS));
        COMBINED_GETTERS.addAll(List.of(LIVING_ENTITY_BOOLEAN_GET_COMMANDS));
        COMBINED_GETTERS.addAll(List.of(PLAYER_BOOLEAN_GET_COMMANDS));
        COMBINED_GETTERS.addAll(List.of(ENTITY_INT_GET_COMMANDS));
        COMBINED_GETTERS.addAll(List.of(LIVING_ENTITY_INT_GET_COMMANDS));
        COMBINED_GETTERS.addAll(List.of(PLAYER_INT_GET_COMMANDS));
        COMBINED_GETTERS.addAll(List.of(ENTITY_DOUBLE_GET_COMMANDS));
        COMBINED_GETTERS.addAll(List.of(ENTITY_FLOAT_GET_COMMANDS));
        COMBINED_GETTERS.addAll(List.of(LIVING_ENTITY_FLOAT_GET_COMMANDS));
        COMBINED_GETTERS.addAll(List.of(PLAYER_FLOAT_GET_COMMANDS));
    }
    public static final String[] ENTITY_BOOLEAN_GET_COMMANDS_OBFUSCATED = {"m_5833_", "m_20092_", "m_20096_", "m_20067_", "m_20068_", "m_213854_", "m_20672_", "m_20069_", "m_20070_", "m_20071_", "m_20072_", "m_5842_", "m_5843_", "m_20077_", "m_271807_", "m_6087_", "m_60204_", "m_107276_", "m_5830_", "m_5829_", "m_20152_", "m_6060_", "m_20159_", "m_20160_", "m_275843_", "m_134360_", "m_20161_", "m_20162_", "m_20163_", "m_20164_", "m_6047_", "m_20142_", "m_6069_", "m_6067_", "m_20143_", "m_142038_", "m_20145_", "m_288188_", "m_146890_", "m_6097_", "m_132674_", "m_6072_", "m_6090_", "m_6051_", "m_6063_", "m_8077_", "m_20151_", "m_6052_", "m_6128_", "m_6127_", "m_146898_", "m_6109_", "m_21515_", "m_6999_", "m_7028_", "m_6102_", "m_146899_", "m_142079_", "m_203117_", "m_264410_", "m_142391_", "m_142389_", "canUpdate", "m_146944_", "m_146945_", "m_146946_", "m_146965_", "m_146966_", "m_142559_", };
    public static final String[] LIVING_ENTITY_BOOLEAN_GET_COMMANDS_OBFUSCATED = {"m_6040_", "m_6039_", "m_6162_", "m_6149_", "m_147223_", "m_142066_", "m_142065_", "m_21219_", "m_21222_", "m_21224_", "m_217046_", "m_6147_", "m_6126_", "m_21209_", "m_6117_", "m_21254_", "m_5791_", "m_21255_", "m_5801_", "m_5789_", "m_5803_", "m_213824_", };
    public static final String[] PLAYER_BOOLEAN_GET_COMMANDS_OBFUSCATED = {"m_36341_", "m_143387_", "m_6129_", "m_7578_", "m_242612_", "m_36317_", "m_36319_", "m_24697_", "m_36326_", "m_6052_", "m_271807_", "m_6069_", "m_6063_", "m_36330_", "m_36337_", "m_142389_", "m_150108_", "m_142391_", "m_264410_", };
    public static final String[] ENTITY_INT_GET_COMMANDS_OBFUSCATED = {"m_19876_", "m_10446_", "hashCode", "m_287149_", "m_6078_", "m_20094_", "m_6045_", "m_6062_", "m_20146_", "m_146888_", "m_146891_", "m_6056_", };
    public static final String[] LIVING_ENTITY_INT_GET_COMMANDS_OBFUSCATED = {"m_213860_", "m_21213_", "m_21215_", "m_21216_", "m_21230_", "m_21212_", "m_21252_", "m_21256_", };
    public static final String[] PLAYER_INT_GET_COMMANDS_OBFUSCATED = {"m_133343_", "m_36318_", "m_36322_", "m_36323_", };
    public static final String[] ENTITY_DOUBLE_GET_COMMANDS_OBFUSCATED = {"m_6049_", "m_6048_", "m_20204_", "m_20187_", "m_20188_", };
    public static final String[] ENTITY_FLOAT_GET_COMMANDS_OBFUSCATED = {"m_213856_", "m_6143_", "m_146889_", "m_132977_", "m_278726_", "m_132596_", "m_213816_", "m_132597_", "m_274421_", };
    public static final String[] LIVING_ENTITY_FLOAT_GET_COMMANDS_OBFUSCATED = {"m_175813_", "m_133247_", "m_264297_", "m_21207_", "m_6100_", "m_285755_", "m_6113_", "m_6103_", };
    public static final String[] PLAYER_FLOAT_GET_COMMANDS_OBFUSCATED = {"m_36333_", "m_287164_", };

    protected final Supplier<Double>[] lines;

    protected ListTag commands;
    protected Entity target;

    protected Supplier<Double> getMethodSupplier(String command, double scalar) {
        for (int i = 0; i < PLAYER_BOOLEAN_GET_COMMANDS.length; ++i)
            if (PLAYER_BOOLEAN_GET_COMMANDS[i].equals(command)) {
                final String ob = PLAYER_BOOLEAN_GET_COMMANDS_OBFUSCATED[i];
                final String un = PLAYER_BOOLEAN_GET_COMMANDS[i];
                return () -> target instanceof Player ? (Methods.invokeMethod(target, ob, un) ? scalar : 0.) : 0.;
            }
        for (int i = 0; i < PLAYER_INT_GET_COMMANDS.length; ++i)
            if (PLAYER_INT_GET_COMMANDS[i].equals(command)) {
                final String ob = PLAYER_INT_GET_COMMANDS_OBFUSCATED[i];
                final String un = PLAYER_INT_GET_COMMANDS[i];
                return () -> target instanceof Player ? scalar * (int)Methods.invokeMethod(target, ob, un) : 0.;
            }
        for (int i = 0; i < PLAYER_FLOAT_GET_COMMANDS.length; ++i)
            if (PLAYER_FLOAT_GET_COMMANDS[i].equals(command)) {
                final String ob = PLAYER_FLOAT_GET_COMMANDS_OBFUSCATED[i];
                final String un = PLAYER_FLOAT_GET_COMMANDS[i];
                return () -> target instanceof Player ? scalar * (float)Methods.invokeMethod(target, ob, un) : 0.;
            }
        for (int i = 0; i < LIVING_ENTITY_BOOLEAN_GET_COMMANDS.length; ++i)
            if (LIVING_ENTITY_BOOLEAN_GET_COMMANDS[i].equals(command)) {
                final String ob = LIVING_ENTITY_BOOLEAN_GET_COMMANDS_OBFUSCATED[i];
                final String un = LIVING_ENTITY_BOOLEAN_GET_COMMANDS[i];
                return () -> target instanceof LivingEntity ? (Methods.invokeMethod(target, ob, un) ? scalar : 0.) : 0.;
            }
        for (int i = 0; i < LIVING_ENTITY_INT_GET_COMMANDS.length; ++i)
            if (LIVING_ENTITY_INT_GET_COMMANDS[i].equals(command)) {
                final String ob = LIVING_ENTITY_INT_GET_COMMANDS_OBFUSCATED[i];
                final String un = LIVING_ENTITY_INT_GET_COMMANDS[i];
                return () -> target instanceof LivingEntity ? Methods.invokeMethod(target, ob, un) : 0.;
            }
        for (int i = 0; i < LIVING_ENTITY_FLOAT_GET_COMMANDS.length; ++i)
            if (LIVING_ENTITY_FLOAT_GET_COMMANDS[i].equals(command)) {
                final String ob = LIVING_ENTITY_FLOAT_GET_COMMANDS_OBFUSCATED[i];
                final String un = LIVING_ENTITY_FLOAT_GET_COMMANDS[i];
                return () -> target instanceof LivingEntity ? scalar * (float)Methods.invokeMethod(target, ob, un) : 0.;
            }
        for (int i = 0; i < ENTITY_BOOLEAN_GET_COMMANDS.length; ++i)
            if (ENTITY_BOOLEAN_GET_COMMANDS[i].equals(command)) {
                final String ob = ENTITY_BOOLEAN_GET_COMMANDS_OBFUSCATED[i];
                final String un = ENTITY_BOOLEAN_GET_COMMANDS[i];
                return () -> target != null ? (Methods.invokeMethod(target, ob, un) ? scalar : 0.) : 0.;
            }
        for (int i = 0; i < ENTITY_INT_GET_COMMANDS.length; ++i)
            if (ENTITY_INT_GET_COMMANDS[i].equals(command)) {
                final String ob = ENTITY_INT_GET_COMMANDS_OBFUSCATED[i];
                final String un = ENTITY_INT_GET_COMMANDS[i];
                return () -> target != null ? scalar * (int)Methods.invokeMethod(target, ob, un) : 0.;
            }
        for (int i = 0; i < ENTITY_FLOAT_GET_COMMANDS.length; ++i)
            if (ENTITY_FLOAT_GET_COMMANDS[i].equals(command)) {
                final String ob = ENTITY_FLOAT_GET_COMMANDS_OBFUSCATED[i];
                final String un = ENTITY_FLOAT_GET_COMMANDS[i];
                return () -> target != null ? scalar * (float)Methods.invokeMethod(target, ob, un) : 0.;
            }
        for (int i = 0; i < ENTITY_DOUBLE_GET_COMMANDS.length; ++i)
            if (ENTITY_DOUBLE_GET_COMMANDS[i].equals(command)) {
                final String ob = ENTITY_DOUBLE_GET_COMMANDS_OBFUSCATED[i];
                final String un = ENTITY_DOUBLE_GET_COMMANDS[i];
                return () -> target != null ? scalar * (double)Methods.invokeMethod(target, ob, un) : 0.;
            }
        return ()->0.;
    }

    //topology:
    //list:
    //  compound:
    //    kind: str (one of 'obs', 'op', 'lit', 'rs')
    //    mtd: str (kind 'obs' only, one of [A=Z_]+_GET_COMMANDS)
    //    mul: double (kind 'obs' and 'rs')
    //    op: str (kind 'op' only, one of DOUBLE_OPERATIONS)
    //    a: int (kind 'op' only, index of first operand)
    //    b: int (kind 'op' only, index of second operand)
    //    val: double (kind 'lit' only)
    //    side: int (kind 'rs' only, 0-5)

    //only the last line and the lines referenced by it will be evaluated (composition)
    //we should limit the number of lines to around 5, maximum 9?

    protected void extractCommand(ListTag list) {
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag entry = list.getCompound(i);
            String kind = entry.getString("kind");
            switch (kind) {
                case "obs" -> lines[i] = getMethodSupplier(entry.getString("mtd"), entry.getDouble("mul"));
                case "op" -> {
                    final BiFunction<Double, Double, Double> op = DOUBLE_OPERATIONS.get(entry.getString("op"));
                    final int a = entry.getInt("a");
                    final int b = entry.getInt("b");
                    if (op != null && a != i && b != i && a >= 0 && a < lines.length && b >= 0 && b < lines.length)
                        lines[i] = () -> op.apply(lines[a].get(), lines[b].get());
                    else
                        lines[i] = () -> 0.;
                }
                case "lit" -> {
                    final double val = entry.getDouble("val");
                    lines[i] = () -> val;
                }
                case "rs" -> {
                    final double mul = entry.getDouble("mul");
                    final int dir = entry.getInt("dir");
                    lines[i] = () -> mul; //FIXME: need to implement redstone support
                }
                default -> lines[i] = () -> 0.;
            }
        }
    }

    public EntityObserver(ListTag commands) {
        if (commands != null && !commands.isEmpty()) {
            this.lines = new Supplier[commands.size()];
            this.commands = commands.copy();
            extractCommand(this.commands);
        } else {
            this.commands = new ListTag();
            this.lines = new Supplier[]{};
        }
    }

    public double run(Entity entity) {
        target = entity;
        if (lines.length > 0)
            return lines[lines.length - 1].get();
        return 0.;
    }

    public ListTag getCommands() { return commands; }
}
