package com.example.oceanbase.demos.web.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class util {

    public static Map<String, Object> getEntityMap(Object entity) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();

        Class<?> clazz = entity.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(entity));
        }

        return map;
    }
}
