package com.limachi.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class Log {
    public static final Logger LOGGER = LogManager.getLogger();

    /**
     * will debug methods actually log something
     */
    public static final boolean DO_DEBUG = true;
    /**
     * how debug will be logged (LOGGER::info or LOGGER::debug)
     */
    public static final Consumer<String> DEBUG = LOGGER::info;

    public static <T> T debug(T v, String s) { if (DO_DEBUG) DEBUG.accept(Thread.currentThread().getStackTrace()[2].toString() + " V: " + v + " : "+ s); return v; }
    public static <T> T debug(T v) { if (DO_DEBUG) DEBUG.accept(Thread.currentThread().getStackTrace()[2].toString() + " V: " + v); return v; }
    public static <T> T debug(T v, int depth) { if (DO_DEBUG) DEBUG.accept(Thread.currentThread().getStackTrace()[2 + depth].toString() + " V: " + v); return v; }
    public static <T> T debug(T v, int depth, String s) { if (DO_DEBUG) DEBUG.accept(Thread.currentThread().getStackTrace()[2 + depth].toString() + " V: " + v + " : "+ s); return v; }

    public static <T> T warn(T v, String s) { LOGGER.warn(Thread.currentThread().getStackTrace()[2].toString() + " V: " + v + " : "+ s); return v; }
    public static <T> T warn(T v) { LOGGER.warn(Thread.currentThread().getStackTrace()[2].toString() + " V: " + v); return v; }
    public static <T> T warn(T v, int depth) { LOGGER.warn(Thread.currentThread().getStackTrace()[2 + depth].toString() + " V: " + v); return v; }
    public static <T> T warn(T v, int depth, String s) { LOGGER.warn(Thread.currentThread().getStackTrace()[2 + depth].toString() + " V: " + v + " : "+ s); return v; }

    public static <T> T error(T v, String s) { LOGGER.error(Thread.currentThread().getStackTrace()[2].toString() + " V: " + v + " : "+ s); return v; }
    public static <T> T error(T v) { LOGGER.error(Thread.currentThread().getStackTrace()[2].toString() + " V: " + v); return v; }
    public static <T> T error(T v, int depth) { LOGGER.error(Thread.currentThread().getStackTrace()[2 + depth].toString() + " V: " + v); return v; }
    public static <T> T error(T v, int depth, String s) { LOGGER.error(Thread.currentThread().getStackTrace()[2 + depth].toString() + " V: " + v + " : "+ s); return v; }
}
