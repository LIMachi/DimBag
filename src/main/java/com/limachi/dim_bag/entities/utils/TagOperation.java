package com.limachi.dim_bag.entities.utils;

import com.google.common.collect.ImmutableList;
import com.limachi.dim_bag.utils.Tags;
import net.minecraft.nbt.*;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * New paradigm: access to serialized entity (CompoundTag)
 * Observer screen will have access to the player interacting (client side?) and the user entity (synced) as an example
 * Path to data will be encoded in object access style ([index] for lists and .key for maps)
 * Operations on data will include string matching (to convert to boolean)
 * Only 1 operation per data
 * Only 1 query per observer
 * Ease of selection in screen (if you can see your target in the tree, just click it to get the path in the query field)
 */

public class TagOperation {
    public static final Pattern TAG_PATH_PATTERN = Pattern.compile("\\.?([a-zA-Z][a-zA-Z_0-9]*)|\\[([0-9]+)]|.+");

    protected ImmutableList<Function<Tag, Tag>> walkPath(String path) {
        Matcher matcher = TAG_PATH_PATTERN.matcher(path);
        ArrayList<Function<Tag, Tag>> extractedPath = new ArrayList<>();
        while (matcher.find()) {
            String group = matcher.group(1);
            if (group != null) {
                final String key = group;
                extractedPath.add(t->t instanceof CompoundTag c ? c.get(key) : EndTag.INSTANCE);
            } else {
                group = matcher.group(2);
                if (group != null) {
                    final int i = Integer.parseInt(group);
                    extractedPath.add(t->t instanceof CollectionTag<?> list ? list.get(i) : EndTag.INSTANCE);
                } else {
                    return ImmutableList.of();
                }
            }
        }
        return ImmutableList.copyOf(extractedPath);
    }

    protected Function<Tag, Double> operator(String objectType, String operator, String operand) {
        Function<Tag, Double> numeric = null;
        Function<Tag, String> text = null;
        Object val = operand;
        if (!"string".equals(objectType))
            try {
                val = Double.parseDouble(operand);
            } catch (NumberFormatException ignore) {
                if ("true".equals(val) || "false".equals(val))
                    val = "true".equals(val) ? 1. : 0.;
                else
                    return t->0.;
            }
        switch (objectType) {
            case "string" -> text = t->t instanceof StringTag s ? s.getAsString() : "";
            case "byte" -> numeric = t->t instanceof ByteTag b ? (double)b.getAsByte() : 0.;
            case "short" -> numeric = t->t instanceof ShortTag s ? (double)s.getAsShort() : 0.;
            case "int" -> numeric = t->t instanceof IntTag b ? (double)b.getAsInt() : 0.;
            case "long" -> numeric = t->t instanceof LongTag l ? (double)l.getAsLong() : 0.;
            case "float" -> numeric = t->t instanceof FloatTag f ? (double)f.getAsFloat() : 0.;
            case "double" -> numeric = t->t instanceof DoubleTag d ? (double)d.getAsDouble() : 0.;
            case "uuid" -> text = t->{
                try {
                    return t instanceof IntArrayTag a ? NbtUtils.loadUUID(a).toString() : "";
                } catch (IllegalArgumentException ignore) {
                    return "";
                }
            };
            default -> { return t->0.; }
        }
        Function<Tag, Double> finalNumeric = numeric;
        Object finalVal = val;
        switch (operator) {
            case "regex" -> {
                if (text == null) return t->0.;
                Function<Tag, String> finalText = text;
                return t->finalText.apply(t).matches((String)finalVal) ? getBooleanScalar() : 0.;
            }
            case "==" -> {
                if (text != null) {
                    Function<Tag, String> finalText = text;
                    return t->finalVal.equals(finalText.apply(t)) ? getBooleanScalar() : 0.;
                } else
                    return t->finalVal.equals(finalNumeric.apply(t)) ? getBooleanScalar() : 0.;
            }
            case "!=" -> {
                if (text != null) {
                    Function<Tag, String> finalText = text;
                    return t->finalVal.equals(finalText.apply(t)) ? 0. : getBooleanScalar();
                } else
                    return t->finalVal.equals(finalNumeric.apply(t)) ? 0. : getBooleanScalar();
            }
            case "+" -> { return finalVal == null ? t->0. : t->finalNumeric.apply(t) + (double)finalVal; }
            case "-" -> { return finalVal == null ? t->0. : t->finalNumeric.apply(t) - (double)finalVal; }
            case "*" -> { return finalVal == null ? t->0. : t->finalNumeric.apply(t) * (double)finalVal; }
            case "/" -> { return finalVal == null ? t->0. : t->finalNumeric.apply(t) / (double)finalVal; }
            case "%" -> { return finalVal == null ? t->0. : t->finalNumeric.apply(t) % (double)finalVal; }
            case "<" -> { return finalVal == null ? t->0. : t->finalNumeric.apply(t) < (double)finalVal ? getBooleanScalar() : 0.; }
            case ">" -> { return finalVal == null ? t->0. : t->finalNumeric.apply(t) > (double)finalVal ? getBooleanScalar() : 0.; }
            case "<=" -> { return finalVal == null ? t->0. : t->finalNumeric.apply(t) <= (double)finalVal ? getBooleanScalar() : 0.; }
            case ">=" -> { return finalVal == null ? t->0. : t->finalNumeric.apply(t) >= (double)finalVal ? getBooleanScalar() : 0.; }
            default -> { return t->0.; }
        }
    }

    protected final CompoundTag original;
    public final ImmutableList<Function<Tag, Tag>> path;
    public final Function<Tag, Double> operator;
    protected double booleanScalar = 1.;

    public double getBooleanScalar() { return booleanScalar; }

    public TagOperation(CompoundTag command) {
        if (command == null) {
            original = new CompoundTag();
            path = ImmutableList.of();
            operator = t->0.;
        } else {
            original = command.copy();
            path = walkPath(command.getString("path"));
            operator = operator(command.getString("type"), command.getString("operator"), command.getString("operand"));
        }
    }

    public CompoundTag getOriginal() { return original; }

    public double run(CompoundTag target, double booleanScalar) {
        if (path.isEmpty()) return 0;
        this.booleanScalar = booleanScalar;
        Tag tmp = target;
        for (Function<Tag, Tag> advance : path)
            if (tmp instanceof EndTag)
                return 0;
            else
                tmp = advance.apply(tmp);
        return operator.apply(tmp);
    }

    public int run(CompoundTag target) { return Mth.clamp((int)Math.round(run(target, 15.)), 0, 15); }

    /*
    @StaticInit
    public static void test() {
        CompoundTag test = new CompoundTag();
        test.put("sub_compound", new CompoundTag());
        test.putString("string", "other_test");
        test.put("list", new ListTag());
        test.getList("list", Tag.TAG_STRING).add(StringTag.valueOf("string in list"));
        test.getCompound("sub_compound").putString("test_string", "test");
        CompoundTag op = new CompoundTag();
        op.putString("type", "string");
        op.putString("operator", "==");
        op.putString("operand", "string in list");
        op.putString("path", "list[0]");
        EntityObserverNew observer = new EntityObserverNew(op);
        int result = observer.run(test);
        List<String> pathSuggestions = pathSuggestions(test);
        for (String suggestion : pathSuggestions)
            Log.error("suggestion: "+ suggestion);
        List<String> testNonCompound = pathSuggestions(StringTag.valueOf("test"));
        Log.error("testNonCompound: " + testNonCompound.get(0));
        Log.error("result: " + result);
    }
    */

    private static List<String> innerPathSuggestions(Tag target, int depth) {
        List<String> out = new ArrayList<>();
        if (target instanceof CompoundTag compound) {
            if (depth > 1)
                for (String key : compound.getAllKeys())
                    innerPathSuggestions(compound.get(key), depth - 1).forEach(s -> out.add("." + key + s));
            else
                out.add("{...}");
        }
        else if (target instanceof CollectionTag<?> list) {
            if (list instanceof IntArrayTag && list.size() == 4)
                try {
                    String uuid = NbtUtils.loadUUID(list).toString();
                    out.add(": " + uuid);
                    return out;
                } catch (IllegalArgumentException ignore) {}
            if (depth > 1)
                for (int i = 0; i < list.size(); ++i) {
                    int finalI = i;
                    innerPathSuggestions(list.get(i), depth - 1).forEach(s -> out.add("[" + finalI + "]" + s));
                }
            else
                out.add("[...]");
        } else if (target != null)
            out.add(": " + target);
        return out;
    }

    public static List<String> pathSuggestions(Tag target, String path, int depth) {
        String[] split = path.split(":");
        if (split.length > 0) {
            depth += split[0].split("\\.").length;
            depth += split[0].split("\\[").length;
        }
        return innerPathSuggestions(target, depth).stream().map(s->s.startsWith(": ") ? s.substring(2) : s.startsWith(".") ? s.substring(1) : s).filter(s->s.startsWith(path)).sorted().toList();
    }

    public static String getTypeForPath(Tag target, String path) {
        TagOperation inner = new TagOperation(Tags.singleton("path", path));
        Tag tmp = target;
        for (Function<Tag, Tag> advance : inner.path)
            if (tmp instanceof EndTag)
                return "unknown";
            else
                tmp = advance.apply(tmp);
        if (tmp instanceof ByteTag b) return "byte";
        if (tmp instanceof ShortTag b) return "short";
        if (tmp instanceof IntTag b) return "int";
        if (tmp instanceof LongTag b) return "long";
        if (tmp instanceof FloatTag b) return "float";
        if (tmp instanceof DoubleTag b) return "double";
        try {
            return (tmp instanceof IntArrayTag a ? NbtUtils.loadUUID(a).toString() : "").equals("") ? "string" : "uuid";
        } catch (IllegalArgumentException ignore) {
            return "string";
        }
    }
}
