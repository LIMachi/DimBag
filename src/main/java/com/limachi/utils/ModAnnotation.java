package com.limachi.utils;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation.EnumHolder;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * helper class to manipulate annotation using the scan data provided by forge
 */
@SuppressWarnings("unused")
public class ModAnnotation {
    /**
     * helper function to create an iterable set of ModAnnotation for the files included by 'modId'
     * the given 'clazz' will be used to filter the annotations, null will get all annotation used by 'modId'
     */
    public static Set<ModAnnotation> iterModAnnotations(String modId, @Nullable Class<?> clazz) {
        Type type = clazz == null ? null : Type.getType(clazz);
        return ModList.get().getAllScanData().stream().filter(fsd -> fsd.getTargets().containsKey(modId)).map(ModFileScanData::getAnnotations).flatMap(Collection::stream).filter(a-> type == null || type.equals(a.annotationType())).map(ModAnnotation::new).collect(Collectors.toSet());
    }

    protected ModFileScanData.AnnotationData annotation;

    public ModAnnotation(ModFileScanData.AnnotationData annotation) { this.annotation = annotation; }

    /**
     * helper function to test if this ModAnnotation is of the given 'clazz' type
     */
    public boolean match(Class<?> clazz) { return annotation.annotationType().equals(Type.getType(clazz)); }

    /**
     * helper function to get data from the annotation (aka annotation parameters).
     * variant that work with enums
     */
    public <T extends Enum<T>> T getData(String key, T def) {
        return annotation.annotationData().containsKey(key) ? Enum.valueOf((Class<T>)def.getClass(), ((EnumHolder)annotation.annotationData().get(key)).getValue()) : def;
    }

    /**
     * helper function to get data from the annotation (aka annotation parameters)
     */
    public <T> T getData(String key, T def) {
        return (T)annotation.annotationData().getOrDefault(key, def);
    }

    /**
     * get a Class of the annotated class, we recommend using the functions to directly use fields and method
     */
    public Class<?> getAnnotatedClass() {
        try {
            return Class.forName(annotation.clazz().getClassName());
        } catch (ClassNotFoundException e) {
            Log.error("Class not found for annotation: " + annotation);
            return null;
        }
    }

    public <T> Constructor<T> getAnnotatedClassConstructor(Class<?> ... paramTypes) {
        try {
            return (Constructor<T>)getAnnotatedClass().getConstructor(paramTypes);
        } catch (Exception e) {
            return null;
        }
    }

    public <T> T getNewAnnotatedClass(Object ... parameters) {
        Class<?>[] paramTypes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; ++i)
            paramTypes[i] = parameters[i].getClass();
        try {
            return (T)getAnnotatedClass().getConstructor(paramTypes).newInstance(parameters);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * get a named 'field' field from the annotated class or null if the field does not exist
     */
    public Field getFieldFromAnnotatedClass(String field) {
        try {
            Field out = getAnnotatedClass().getField(field);
            out.setAccessible(true);
            return out;
        } catch (NoSuchFieldException e) {
            Log.error("Field: '" + field + "' does not exist for annotated class: " + getAnnotatedClass(), 1);
            return null;
        }
    }

    /**
     * directly get value of named 'field' static field from the annotated class or null if the field does not exist
     */
    public <T> T getStaticFieldDataFromAnnotatedClass(String field) { return getFieldDataFromAnnotatedClass(field, null); }

    /**
     * directly get value of named 'field' field from the given 'instance' or null if the field does not exist
     */
    public <T> T getFieldDataFromAnnotatedClass(String field, Object instance) {
        try {
            Field out = getAnnotatedClass().getField(field);
            out.setAccessible(true);
            return (T)out.get(instance);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Log.error((instance == null ? "Static field" : "Field") + ": '" + field + "' cannot be accessed from annotated class: " + getAnnotatedClass() + (instance != null ? " -> " + instance : ""), 1);
            return null;
        }
    }

    /**
     * directly set the named 'field' static field from the annotated class to the given 'value'
     */
    public void setStaticFieldDataFromAnnotatedClass(String field, Object value) { setFieldDataFromAnnotatedClass(field, null, value); }

    /**
     * directly set the named 'field' field from the 'instance' to the given 'value'
     */
    public void setFieldDataFromAnnotatedClass(String field, Object instance, Object value) {
        try {
            Field out = getAnnotatedClass().getField(field);
            out.setAccessible(true);
            out.set(instance, value);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {
            Log.error("Could not set " + (instance == null ? "static " : "") + "field: '" + field + "' with value: '" + value + "' for annotated class: " + getAnnotatedClass() + (instance != null ? " -> " + instance : ""), 1);
        }
    }

    /**
     * get the annotated field, or null if invalid
     */
    public Field getAnnotatedField() {
        try {
            Field out = getAnnotatedClass().getField(annotation.memberName());
            out.setAccessible(true);
            return out;
        } catch (NoSuchFieldException e) {
            Log.error("Could not get annotated field for annotation: " + annotation, 1);
            return null;
        }
    }

    /**
     * directly get the stored value in the annotated field
     */
    public <T> T getAnnotatedStaticFieldData() { return getAnnotatedFieldData(null); }

    /**
     * directly get the stored value in the annotated field of 'instance'
     */
    public <T> T getAnnotatedFieldData(Object instance) {
        try {
            Field out = getAnnotatedClass().getField(annotation.memberName());
            out.setAccessible(true);
            return (T)out.get(instance);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Log.error("Could not get annotated " + (instance == null ? "static " : "") + "field data for annotation: " + annotation + (instance != null ? " -> " + instance : ""), 1);
            return null;
        }
    }

    /**
     * directly set the static annotated field to the given 'value'.
     */
    public void setAnnotatedStaticFieldData(Object value) { setAnnotatedFieldData(null, value); }

    /**
     * directly set the annotated field of 'instance' to the given 'value'.
     */
    public void setAnnotatedFieldData(Object instance, Object value) {
        try {
            Field out = getAnnotatedClass().getField(annotation.memberName());
            out.setAccessible(true);
            out.set(instance, value);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {
            Log.error("Could not set annotated " + (instance == null ? "static " : "") + "field data with:'" + value + "' for annotation: " + annotation + (instance != null ? " -> " + instance : ""), 1);
        }
    }

    /**
     * return a method (not necessarily the annotated one) from the class this annotation is used on, with matching parameter types.
     * return null on error.
     */

    public Method getMethodFromAnnotatedClass(String method, Class<?> ... parameters) {
        try {
            Method out = getAnnotatedClass().getMethod(method, parameters);
            out.setAccessible(true);
            return out;
        } catch (NoSuchMethodException | SecurityException e) {
            Log.error("Could not get method: '" + method + "' with parameters: '" + Arrays.toString(parameters) + "' for annotated class: " + getAnnotatedClass(), 1);
            return null;
        }
    }

    /**
     * run a static method (not necessarily the annotated one) from the class this annotation is used on, using the given parameters.
     * since the parameters types of the method are the ones of the parameters, they need to match EXACTLY.
     * return null on error.
     */
    public <T> T invokeStaticMethodFromAnnotatedClass(String method, Object ... parameters) { return invokeMethodFromAnnotatedClass(method, null, parameters); }

    /**
     * run a method (not necessarily the annotated one) from the class this annotation is used on, given an instance and parameters.
     * since the parameters types of the method are the ones of the parameters, they need to match EXACTLY.
     * return null on error.
     */
    public <T> T invokeMethodFromAnnotatedClass(String method, Object instance, Object ... parameters) {
        Class<?>[] paramTypes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; ++i)
            paramTypes[i] = parameters[i].getClass();
        try {
            Method out = getAnnotatedClass().getMethod(method, paramTypes);
            out.setAccessible(true);
            return (T)out.invoke(instance, parameters);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Log.error("Could not invoke method: '" + method + "' with parameters: '" + Arrays.toString(parameters) + "' for annotated class: " + getAnnotatedClass(), 1);
            return null;
        }
    }

    /**
     * cut a member_name (fully qualified annotated method with type and parameters) to only get the name of the method.
     */
    public static String method_name(String member_name) {
        if (member_name == null || member_name.isEmpty()) return "";
        String[] tp = member_name.split("[(]");
        return tp.length > 0 ? tp[0] : member_name;
    }

    /**
     * get the annotated method or null if the parameters are invalid or the annotation is not a method.
     */
    public Method getAnnotatedMethod(Class<?> ... parameters) {
        try {
            Method out = getAnnotatedClass().getMethod(method_name(annotation.memberName()), parameters);
            out.setAccessible(true);
            return out;
        } catch (NoSuchMethodException | SecurityException e) {
            Log.error("Could not get annotated method with parameters: '" + Arrays.toString(parameters) + "' for annotation: " + annotation, 1);
            return null;
        }
    }

    /**
     * run the annotated method as if it was static with the given parameters, return null on error.
     * since the parameters types of the method are the ones of the parameters, they need to match EXACTLY.
     */
    public <T> T invokeStaticAnnotatedMethod(Object ... params) { return invokeAnnotatedMethod(null, params); }

    /**
     * run the annotated method on the given instance with the given parameters, return null on error.
     * since the parameters types of the method are the ones of the parameters, they need to match EXACTLY.
     */
    public <T> T invokeAnnotatedMethod(Object instance, Object ... parameters) {
        Class<?>[] paramTypes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; ++i)
            paramTypes[i] = parameters[i].getClass();
        try {
            Method out = getAnnotatedClass().getMethod(method_name(annotation.memberName()), paramTypes);
            out.setAccessible(true);
            return (T)out.invoke(instance, parameters);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Log.error("Could not invoke annotated method with parameters: '" + Arrays.toString(parameters) + "' for annotation: " + annotation + (instance != null ? " -> " + instance : ""), 1);
            return null;
        }
    }
}
