package com.limachi.dim_bag.entities.utils;

import com.limachi.lim_lib.Log;
import com.limachi.lim_lib.reflection.Methods;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityObserverOld {
    public static final Class<?>[] SUPPORTED_CLASSES = {Entity.class, LivingEntity.class, Player.class}; //might support other classes at some point? tis already pretty good imo
    //duplicates removed using: "("[a-zA-Z]+")(.*\n.*)+\1" aka find a section delimited by 2 text in quotes and that include at least 1 new line (just remove the last text in quotes to remove the duplicate)

    public static final HashMap<String, PropertyDispatch.TriFunction<EntityObserverOld, Integer, Integer, Integer>> INTEGER_OPERATIONS = new HashMap<>();
    //priority is not applied for * % / operators (contrary to standard math systems, could be solved but it adds too much complexity)
    static {
        INTEGER_OPERATIONS.put("+", (o, i, p)->i + p);
        INTEGER_OPERATIONS.put("-", (o, i, p)->i - p);
        INTEGER_OPERATIONS.put("*", (o, i, p)->i * p);
        INTEGER_OPERATIONS.put("%", (o, i, p)->p != 0 ? i % p : 0);
        INTEGER_OPERATIONS.put("/", (o, i, p)->p != 0 ? i / p : 0);
        INTEGER_OPERATIONS.put("==", (o, i, p)-> Objects.equals(i, p) ? o.getScale() : 0);
        INTEGER_OPERATIONS.put("!=", (o, i, p)-> !Objects.equals(i, p) ? o.getScale() : 0);
        INTEGER_OPERATIONS.put("<=", (o, i, p)->i <= p ? o.getScale() : 0);
        INTEGER_OPERATIONS.put(">=", (o, i, p)->i >= p ? o.getScale() : 0);
        INTEGER_OPERATIONS.put("<", (o, i, p)->i < p ? o.getScale() : 0);
        INTEGER_OPERATIONS.put(">", (o, i, p)->i > p ? o.getScale() : 0);
        INTEGER_OPERATIONS.put(">>", (o, i, p)->i >> p);
        INTEGER_OPERATIONS.put("<<", (o, i, p)->i << p);
        INTEGER_OPERATIONS.put("&", (o, i, p)->i & p);
        INTEGER_OPERATIONS.put("|", (o, i, p)->i | p);
        INTEGER_OPERATIONS.put("^", (o, i, p)->i ^ p);
        INTEGER_OPERATIONS.put("&&", (o, i, p)->i != 0 && p != 0 ? o.getScale() : 0);
        INTEGER_OPERATIONS.put("||", (o, i, p)->i != 0 || p != 0 ? o.getScale() : 0);
    }

    //extracted using: "public boolean ([a-zA-Z]+)\(\)" aka any method that return boolean and does not require a parameter
    public static final String[] ENTITY_BOOLEAN_GET_COMMANDS = {"isSpectator", "isOnPortalCooldown", "onGround", "isSilent", "isNoGravity", "dampensVibrations", "fireImmune", "isInWater", "isInWaterOrRain", "isInWaterRainOrBubble", "isInWaterOrBubble", "isUnderWater", "canSpawnSprintParticle", "isInLava", "canBeHitByProjectile", "isPickable", "isPushable", "isAlive", "isInWall", "canBeCollidedWith", "showVehicleHealth", "isOnFire", "isPassenger", "isVehicle", "dismountsUnderwater", "isShiftKeyDown", "isSteppingCarefully", "isSuppressingBounce", "isDiscrete", "isDescending", "isCrouching", "isSprinting", "isSwimming", "isVisuallySwimming", "isVisuallyCrawling", "isCurrentlyGlowing", "isInvisible", "isOnRails", "isFullyFrozen", "isAttackable", "isInvulnerable", "canChangeDimensions", "isIgnoringBlockTriggers", "displayFireAnimation", "isPushedByFluid", "hasCustomName", "isCustomNameVisible", "shouldShowName", "ignoreExplosion", "onlyOpCanSetNbt", "hasExactlyOnePlayerPassenger", "isControlledByLocalInstance", "isEffectiveAi", "acceptsSuccess", "acceptsFailure", "shouldInformAdmins", "touchingUnloadedChunk", "canFreeze", "isFreezing", "canSprint", "shouldBeSaved", "isAlwaysTicking", "canUpdate", "emitsAnything", "emitsEvents", "emitsSounds", "shouldDestroy", "shouldSave", "alwaysAccepts"};
    public static final String[] LIVING_ENTITY_BOOLEAN_GET_COMMANDS = {"canBreatheUnderwater", "canSpawnSoulSpeedParticle", "isBaby", "shouldDropExperience", "shouldDiscardFriction", "canBeSeenAsEnemy", "canBeSeenByAnyone", "removeAllEffects", "isInvertedHealAndHarm", "isDeadOrDying", "wasExperienceConsumed", "onClimbable", "isSensitiveToWater", "isAutoSpinAttack", "isUsingItem", "isBlocking", "isSuppressingSlidingDownLadder", "isFallFlying", "isAffectedByPotions", "attackable", "isSleeping", "canDisableShield"};
    public static final String[] PLAYER_BOOLEAN_GET_COMMANDS = {"isSecondaryUseActive", "isTextFilteringEnabled", "isAffectedByFluids", "isLocalPlayer", "hasContainerOpen", "isSleepingLongEnough", "tryToStartFallFlying", "isHurt", "mayBuild", "shouldShowName", "canBeHitByProjectile", "isSwimming", "isPushedByFluid", "isReducedDebugInfo", "canUseGameMasterBlocks", "isAlwaysTicking", "isScoping", "shouldBeSaved", "canSprint"};

    //extracted using: "public int ([a-zA-Z]+)\(\)" (same as above but for int)
    public static final String[] ENTITY_INT_GET_COMMANDS = {"getTeamColor", "getId", "hashCode", "getPortalCooldown", "getPortalWaitTime", "getRemainingFireTicks", "getDimensionChangingDelay", "getMaxAirSupply", "getAirSupply", "getTicksFrozen", "getTicksRequiredToFreeze", "getMaxFallDistance"};
    public static final String[] LIVING_ENTITY_INT_GET_COMMANDS = {"getExperienceReward", "getLastHurtByMobTimestamp", "getLastHurtMobTimestamp", "getNoActionTime", "getArmorValue", "getUseItemRemainingTicks", "getTicksUsingItem", "getFallFlyingTicks"};
    public static final String[] PLAYER_INT_GET_COMMANDS = {"getScore", "getSleepTimer", "getEnchantmentSeed", "getXpNeededForNextLevel"};

    //extracted using: "public double ([a-zA-Z]+)\(\)" (same as above but for double)

    public static final String[] ENTITY_DOUBLE_GET_COMMANDS = {"getMyRidingOffset", "getPassengersRidingOffset", "getFluidJumpThreshold", "getRandomY", "getEyeY"};

    //extracted using: "public float ([a-zA-Z]+)\(\)" (same as above but for float)

    public static final String[] ENTITY_FLOAT_GET_COMMANDS = {"getLightLevelDependentMagicValue", "getPickRadius", "getPercentFrozen", "getYHeadRot", "getNameTagOffsetY", "getYRot", "getVisualRotationYInDegrees", "getXRot", "maxUpStep"};
    public static final String[] LIVING_ENTITY_FLOAT_GET_COMMANDS = {"getScale", "getHealth", "getHurtDir", "getArmorCoverPercentage", "getVoicePitch", "getJumpBoostPower", "getSpeed", "getAbsorptionAmount"};
    public static final String[] PLAYER_FLOAT_GET_COMMANDS = {"getCurrentItemAttackStrengthDelay", "getLuck"};

    public static final String[] FIELDS_NAMES = {
            "ENTITY_DOUBLE_GET_COMMANDS",
            "ENTITY_FLOAT_GET_COMMANDS",
            "LIVING_ENTITY_FLOAT_GET_COMMANDS",
            "PLAYER_FLOAT_GET_COMMANDS"
    };

    public static final String[][] FIELDS = {
            ENTITY_DOUBLE_GET_COMMANDS,
            ENTITY_FLOAT_GET_COMMANDS,
            LIVING_ENTITY_FLOAT_GET_COMMANDS,
            PLAYER_FLOAT_GET_COMMANDS
    };

//    @StaticInit
    public static void test() {
        File methods_csv = new File(FMLPaths.CONFIGDIR.get().toFile().getParentFile().getParentFile().listFiles((f, s)->s.equals("ignore"))[0], "methods.csv");
        String file = "";
        try {
            file = Files.readString(methods_csv.toPath());
        } catch (Exception e) {}
        for (int t = 0; t < 6; ++t) {
            StringBuilder builder = new StringBuilder("public static final String[] ");
            builder.append(FIELDS_NAMES[t]);
            builder.append("_OBFUSCATED = {");
            String[] field = FIELDS[t];
            int l = field.length;
            for (int i = 0; i < l; ++i) {
                builder.append("\"");
                Pattern pattern = Pattern.compile("(m_[0-9]+_)," + field[i] + ",");
                Matcher matcher = pattern.matcher(file);
                if (matcher.find())
                    builder.append(matcher.group(1));
                else {
                    Log.error("cannot find obfuscated name for " + field[i]);
                    builder.append(field[i]);
                }
                builder.append("\", ");
            }
            builder.append("};\n");
            Log.warn(builder.toString());
        }
        Log.warn("Nice script, right?");
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


    //test string: "onGround==isLocalPlayer" -> true if local player and onGround or if non local player and not onGround
    //test string "2*(3+2)" -> 10 (parentheses should be parsed first, forced by * operator)

    //groups: 1->highest parenthesis (require recalculation), 2->number, 3->method, 4->operator (greedy in length), 5->scale operator, 6->spaces, 7->invalid characters found,
    public static final Pattern COMMAND_SOLVER_PATTERN = Pattern.compile("\\((.+)\\)|([0-9]+)|([a-zA-Z]+)|([=!<>]=|<<|>>|&&|\\|\\||[-+*%/<>&|])|(\\$)|( +)|(.+?)");

    protected final Supplier<Integer> finalRunner;
    protected final String command;
    protected int scale = 15;
    protected Entity target;

    public int getScale() { return scale; }

    protected Supplier<Integer> getMethodSupplier(String command) {
        for (int i = 0; i < PLAYER_BOOLEAN_GET_COMMANDS.length; ++i)
            if (PLAYER_BOOLEAN_GET_COMMANDS[i].equals(command)) {
                final String ob = PLAYER_BOOLEAN_GET_COMMANDS_OBFUSCATED[i];
                final String un = PLAYER_BOOLEAN_GET_COMMANDS[i];
                return () -> target instanceof Player ? (Methods.invokeMethod(target, ob, un) ? Mth.clamp(getScale(), 1, 15) : 0) : 0;
            }
        for (int i = 0; i < PLAYER_INT_GET_COMMANDS.length; ++i)
            if (PLAYER_INT_GET_COMMANDS[i].equals(command)) {
                final String ob = PLAYER_INT_GET_COMMANDS_OBFUSCATED[i];
                final String un = PLAYER_INT_GET_COMMANDS[i];
                return () -> target instanceof Player ? Methods.invokeMethod(target, ob, un) : 0;
            }
        for (int i = 0; i < PLAYER_FLOAT_GET_COMMANDS.length; ++i)
            if (PLAYER_FLOAT_GET_COMMANDS[i].equals(command)) {
                final String ob = PLAYER_FLOAT_GET_COMMANDS_OBFUSCATED[i];
                final String un = PLAYER_FLOAT_GET_COMMANDS[i];
                return () -> target instanceof Player ? (int)((float)Methods.invokeMethod(target, ob, un) * (float)getScale()) : 0;
            }
        for (int i = 0; i < LIVING_ENTITY_BOOLEAN_GET_COMMANDS.length; ++i)
            if (LIVING_ENTITY_BOOLEAN_GET_COMMANDS[i].equals(command)) {
                final String ob = LIVING_ENTITY_BOOLEAN_GET_COMMANDS_OBFUSCATED[i];
                final String un = LIVING_ENTITY_BOOLEAN_GET_COMMANDS[i];
                return () -> target instanceof LivingEntity ? (Methods.invokeMethod(target, ob, un) ? Mth.clamp(getScale(), 1, 15) : 0) : 0;
            }
        for (int i = 0; i < LIVING_ENTITY_INT_GET_COMMANDS.length; ++i)
            if (LIVING_ENTITY_INT_GET_COMMANDS[i].equals(command)) {
                final String ob = LIVING_ENTITY_INT_GET_COMMANDS_OBFUSCATED[i];
                final String un = LIVING_ENTITY_INT_GET_COMMANDS[i];
                return () -> target instanceof LivingEntity ? Methods.invokeMethod(target, ob, un) : 0;
            }
        for (int i = 0; i < LIVING_ENTITY_FLOAT_GET_COMMANDS.length; ++i)
            if (LIVING_ENTITY_FLOAT_GET_COMMANDS[i].equals(command)) {
                final String ob = LIVING_ENTITY_FLOAT_GET_COMMANDS_OBFUSCATED[i];
                final String un = LIVING_ENTITY_FLOAT_GET_COMMANDS[i];
                return () -> target instanceof LivingEntity ? (int)((float)Methods.invokeMethod(target, ob, un) * (float)getScale()) : 0;
            }
        for (int i = 0; i < ENTITY_BOOLEAN_GET_COMMANDS.length; ++i)
            if (ENTITY_BOOLEAN_GET_COMMANDS[i].equals(command)) {
                final String ob = ENTITY_BOOLEAN_GET_COMMANDS_OBFUSCATED[i];
                final String un = ENTITY_BOOLEAN_GET_COMMANDS[i];
                return () -> target != null ? (Methods.invokeMethod(target, ob, un) ? Mth.clamp(getScale(), 1, 15) : 0) : 0;
            }
        for (int i = 0; i < ENTITY_INT_GET_COMMANDS.length; ++i)
            if (ENTITY_INT_GET_COMMANDS[i].equals(command)) {
                final String ob = ENTITY_INT_GET_COMMANDS_OBFUSCATED[i];
                final String un = ENTITY_INT_GET_COMMANDS[i];
                return () -> target != null ? Methods.invokeMethod(target, ob, un) : 0;
            }
        for (int i = 0; i < ENTITY_FLOAT_GET_COMMANDS.length; ++i)
            if (ENTITY_FLOAT_GET_COMMANDS[i].equals(command)) {
                final String ob = ENTITY_FLOAT_GET_COMMANDS_OBFUSCATED[i];
                final String un = ENTITY_FLOAT_GET_COMMANDS[i];
                return () -> target != null ? (int)((float)Methods.invokeMethod(target, ob, un) * (float)getScale()) : 0;
            }
        for (int i = 0; i < ENTITY_DOUBLE_GET_COMMANDS.length; ++i)
            if (ENTITY_DOUBLE_GET_COMMANDS[i].equals(command)) {
                final String ob = ENTITY_DOUBLE_GET_COMMANDS_OBFUSCATED[i];
                final String un = ENTITY_DOUBLE_GET_COMMANDS[i];
                return () -> target != null ? (int)((double)Methods.invokeMethod(target, ob, un) * (double)getScale()) : 0;
            }
        return ()->0;
    }

    protected Supplier<Integer> extractCommand(String command, LinkedList<Supplier<Integer>> stack) {
        Matcher m = COMMAND_SOLVER_PATTERN.matcher(command);
        int last_calc = -1;
        int last_scale = -1;
        while (m.find()) {
            String s = "0";
            int g = 2;
            for (int i = 1; i <= 7; ++i)
                try {
                    s = m.group(i);
                    if (s != null) {
                        g = i;
                        break;
                    }
                } catch (IndexOutOfBoundsException ignore) {
                }
            switch (g) {
                case 1 -> { stack.addLast(extractCommand(s, new LinkedList<>())); }
                case 2 -> {
                    final int out = Integer.parseInt(s);
                    stack.addLast(() -> out);
                }
                case 3 -> { stack.addLast(getMethodSupplier(s)); }
                case 4 -> {
                    final int n = stack.size() + 1;
                    final int p = last_calc >= 0 ? last_calc : stack.size() - 1;
                    if (p >= 0) {
                        final PropertyDispatch.TriFunction<EntityObserverOld, Integer, Integer, Integer> r = INTEGER_OPERATIONS.get(s);
                        stack.addLast(() -> r.apply(this, stack.get(p).get(), stack.size() > n ? stack.get(n).get() : 0));
                    } else {
                        //ERROR missing operand before call
                        stack.addLast(()->0);
                    }
                    last_calc = stack.size() - 1;
                }
                case 5 -> {
                    final int p = last_calc >= 0 ? last_calc : stack.size() - 1;
                    if (p >= 0)
                        stack.addLast(()->scale = stack.get(p).get());
                    else
                        stack.addLast(()->scale = 15);
                    last_scale = stack.size() - 1;
                }
                case 6 -> { /*do nothing, spaces*/ }
                case 7 -> { /*do nothing, should log an error though*/ }
            }
            if (g != 5 && last_scale != -1) {
                final int p = last_calc >= 0 ? last_calc : stack.size() - 1;
                final int fs = last_scale;
                if (p >= 0)
                    stack.addLast(()->{stack.get(fs).get(); return stack.get(p).get();});
                last_scale = -1;
                last_calc = stack.size() - 1;
            }
        }
        if (stack.size() > 1 && last_calc >= stack.size() - 2)
            return stack.get(last_calc);
        return stack.size() > 0 ? stack.getLast() : ()->0; //should probably send an error when stack is empty
    }

    public EntityObserverOld(String command) {
        if (command != null && !command.isEmpty() && !command.isBlank()) {
            this.command = command;
            finalRunner = extractCommand(command, new LinkedList<>());
        } else {
            this.command = "";
            finalRunner = () -> 0;
        }
    }

    public int run(Entity entity) {
        target = entity;
        scale = 15;
        if (finalRunner != null) {
            int out = finalRunner.get();
            if (out < 0)
                return 0;
            if (out > 15)
                return 15;
            return out;
        }
        return 0;
    }

    public String getCommand() { return command; }
}
