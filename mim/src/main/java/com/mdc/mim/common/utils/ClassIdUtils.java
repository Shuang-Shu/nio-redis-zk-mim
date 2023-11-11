package com.mdc.mim.common.utils;

import java.util.HashMap;
import java.util.Map;

public class ClassIdUtils {
    private static Map<Class<?>, Integer> classIdMap = new HashMap<>();

    public static void registerVersion(Class<?> clazz, int version) {
        classIdMap.put(clazz, version);
    }

    public static int generateClassId(Class<?> clazz) {
        if (!classIdMap.containsKey(clazz)) {
            throw new IllegalStateException("class version is not registered");
        }
        int versionId = classIdMap.get(clazz);
        return versionId << (3 * 8) | (clazz.getName().hashCode() >>> 8);
    }

    public static int generateClassId(Class<?> clazz, int version) {
        classIdMap.put(clazz, version);
        return generateClassId(clazz);
    }
}
