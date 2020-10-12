package com.limachi.dimensional_bags;

import java.lang.reflect.Method;

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
}
