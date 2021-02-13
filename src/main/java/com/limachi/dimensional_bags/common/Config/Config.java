package com.limachi.dimensional_bags.common.Config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * those annotation should only used on static fields of class and of supported type
 */
public class Config {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface IntRange {
        int def() default 0;
        int min() default Integer.MIN_VALUE;
        int max() default Integer.MAX_VALUE;
        java.lang.String cmt() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface String {
        java.lang.String def() default "";
        java.lang.String[] valid() default {};
        java.lang.String cmt() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Boolean {
        boolean def() default false;
        boolean[] valid() default {true, false};
        java.lang.String cmt() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Long {
        long def() default 0L;
        long[] valid() default {};
        java.lang.String cmt() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Int {
        int def() default 0;
        int[] valid() default {};
        java.lang.String cmt() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Enum {
        /**
         * index of the enum (enumVar.val.ordinal()), as java does not allow enum directly inside of annotations
         */
        int ord();
        int[] valid() default {};
        java.lang.String cmt() default "";
    }
}
