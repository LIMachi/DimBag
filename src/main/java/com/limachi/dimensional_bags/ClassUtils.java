package com.limachi.dimensional_bags;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassUtils {
    public static Method getMethod(Class<?> clazz, String deobName, String obName, Class<?> ... paramTypes) {
        Method out = null;
        if (deobName != null && deobName.length() != 0) {
            try {
                out = clazz.getMethod(deobName, paramTypes);
            } catch (Exception e) {
            }
        }
        if (obName != null && obName.length() != 0) {
            try {
                out = clazz.getMethod(obName, paramTypes);
            } catch (Exception e) {
            }
        }
        return out;
    }

    public static Object callMethod(Method method, Object o, Object ... param) {
        if (method != null && o != null)
            try {
                return method.invoke(o,param);
            } catch (Exception e) {}
        return null;
    }

    static protected final HashMap<Class<?>, Function<String, Object>> PARSE = new HashMap<>();
    static {
        PARSE.put(Float.TYPE, Float::parseFloat);
        PARSE.put(Double.TYPE, Double::parseDouble);
        PARSE.put(Integer.TYPE, Integer::parseInt);
        PARSE.put(Long.TYPE, Long::parseLong);
        PARSE.put(Byte.TYPE, Byte::parseByte);
        PARSE.put(Short.TYPE, Short::parseShort);
        PARSE.put(Boolean.TYPE, v -> v.equalsIgnoreCase("true"));
        PARSE.put(String.class, v -> v);
    }

    public static boolean canParse(Class<?> clazz, String toParse) {
        Function<String, Object> pred = PARSE.get(clazz);
        if (pred == null) return false;
        try {
            pred.apply(toParse);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static <T> T parse(Class<T> clazz, String toParse) {
        Function<String, Object> pred = PARSE.get(clazz);
        if (pred == null) return null;
        try {
            return (T)pred.apply(toParse);
        } catch (Exception e) {
            return null;
        }
    }

    public static class Strings {
        /**
         * based on String#split, but will make sure the pattern supplied isn't in quotes (single or double)
         * @param input the string to split
         * @param regex the pattern that will be used as limits for the split (substracted)
         * @param limit the maximum amount of split (0 == as much as we can)
         * @return an array of strings representing the original string stripped of the pattern
         */
        public static String[] splitIgnoringQuotes(String input, String regex, int limit) {
            Pattern pattern = Pattern.compile("\\\"|\"(?:\\\"|[^\"])*\"|\\'|'(?:\\'|[^'])*'|(" + regex + ")");

            int index = 0;
            boolean matchLimited = limit > 0;
            ArrayList<String> matchList = new ArrayList<>();
            Matcher m = pattern.matcher(input);

            // Add segments before each match found
            while(m.find()) {
                if (!matchLimited || matchList.size() < limit - 1) {
                    if (index == 0 && index == m.start(1) && m.start(1) == m.end(1)) {
                        // no empty leading substring included for zero-width match
                        // at the beginning of the input char sequence.
                        continue;
                    }
                    String match = input.subSequence(index, m.start(1)).toString();
                    matchList.add(match);
                    index = m.end(1);
                } else if (matchList.size() == limit - 1) { // last one
                    String match = input.subSequence(index,
                            input.length()).toString();
                    matchList.add(match);
                    index = m.end(1);
                }
            }

            // If no match was found, return this
            if (index == 0)
                return new String[] {input.toString()};

            // Add remaining segment
            if (!matchLimited || matchList.size() < limit)
                matchList.add(input.subSequence(index, input.length()).toString());

            // Construct result
            int resultSize = matchList.size();
            if (limit == 0)
                while (resultSize > 0 && matchList.get(resultSize-1).equals(""))
                    resultSize--;
            String[] result = new String[resultSize];
            return matchList.subList(0, resultSize).toArray(result);
        }
    }
}
