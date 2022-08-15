package com.limachi.utils;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Annotations {
    public static Set<Annotations> iterModAnnotations(String modId, @Nullable Class<?> clazz) {
        Type type = clazz == null ? null : Type.getType(clazz);
        return ModList.get().getAllScanData().stream().filter(fsd -> fsd.getTargets().containsKey(modId)).map(ModFileScanData::getAnnotations).flatMap(Collection::stream).filter(a-> type == null || type.equals(a.annotationType())).map(Annotations::new).collect(Collectors.toSet());
    }
    protected ModFileScanData.AnnotationData annotation;
    public Annotations(ModFileScanData.AnnotationData annotation) { this.annotation = annotation; }
    public boolean match(Class<?> clazz) { return annotation.annotationType().equals(Type.getType(clazz)); }
    public <T> T getData(String key, T def) { return (T)annotation.annotationData().getOrDefault(key, def); }
    public Class<?> getAnnotatedClass() {
        try {
            return Class.forName(annotation.clazz().getClassName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    public Field getFieldFromAnnotatedClass(String field) {
        try {
            Field out = getAnnotatedClass().getField(field);
            out.setAccessible(true);
            return out;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
    public Field getAnnotatedField() {
        try {
            Field out = getAnnotatedClass().getField(annotation.memberName());
            out.setAccessible(true);
            return out;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
