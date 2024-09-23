package ru.clevertec.reflection.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonToObjectConverter {

    private static final String REGEXP_FOR_STRING_VAL = "\\\"%s\\\":\\s*\\\"(.*?)\\\"";
    private static final String REGEXP_FOR_LIST_VAL = "\\\"%s\\\":\\s*\\[(.*?)\\]";

    public static Object createObject(Map<String, Object> objectsMap, Class clazz) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Object object = clazz.getDeclaredConstructor().newInstance();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Object value = objectsMap.get(field.getName());
            if (value != null) {
                field.setAccessible(true);
                field.set(object, value);
            }
        }
        return object;
    }

    public static Map<String, Object> createMapFromJsonString(String jsonAsString, Class clazz) {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        String after = jsonAsString.trim().replace("\n", "")
                .replaceAll(" +", " ");
        for (Field field : fields) {
            Pattern pattern;
            Class type = field.getType();
            String name = field.getName();
            if (type == List.class) {
                pattern = Pattern.compile(String.format(REGEXP_FOR_LIST_VAL, name));
            } else {
                pattern = Pattern.compile(String.format(REGEXP_FOR_STRING_VAL, name));
                Matcher matcher = pattern.matcher(after);
                String s = null;
                while (matcher.find())
                    s = matcher.group();
                String[] str = s.split("\":");
                str[1] = str[1].trim().replaceAll("\"", "");
                if (type == UUID.class) {
                    map.put(name, UUID.fromString(str[1]));
                } else if (type == OffsetDateTime.class) {
                    map.put(name, OffsetDateTime.parse(str[1]));
                } else if (type.isPrimitive()) {
                    map.put(name, str[1]);
                } else {
                    return createMapFromJsonString(str[1], type);
                }
            }
        }
        return map;
    }
}
