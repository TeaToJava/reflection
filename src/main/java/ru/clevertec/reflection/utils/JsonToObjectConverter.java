package ru.clevertec.reflection.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonToObjectConverter {

    private static final String REGEXP_FOR_STRING_VAL = "\\\"%s\\\":\\s*\\\"(.*?)\\\"";
    private static final String REGEXP_FOR_LIST_VAL = "\\\"%s\\\":\\s*\\[(.*?)\\]";

    private static final int INDENTATION = 2;

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

    public static Map<String, Object> createMapFromJsonString(String jsonAsString, Class clazz) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        String after = jsonAsString.trim().replace("\n", "")
                .replaceAll(" +", " ");
        Object value = null;
        for (Field field : fields) {
            Pattern pattern;
            Class type = field.getType();
            String name = field.getName();
            if (type == List.class) {
                pattern = Pattern.compile(String.format(REGEXP_FOR_LIST_VAL, name));
            } else {
                pattern = Pattern.compile(String.format(REGEXP_FOR_STRING_VAL, name));
            }
            Matcher matcher = pattern.matcher(after);
            String s = null;
            while (matcher.find())
                s = matcher.group();
            if (s != null) {
                s = s.substring(s.indexOf("\":") + INDENTATION)
                        .trim();
                if (type == List.class) {
                    List<Object> list = new ArrayList<>();
                    s = s.replace("[", "")
                            .replace("]", "");
                    String[] arr = s.split("\\}\s*,\s*\\{");
                    ParameterizedType listType = (ParameterizedType) field.getGenericType();
                    Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
                    for (String str : arr) {
                        Map<String, Object> map2 = createMapFromJsonString(str, listClass);
                        list.add(createObject(map2, listClass));
                    }
                    value = list;
                } else if (type == Map.class) {
                    ParameterizedType mapType = (ParameterizedType) field.getGenericType();
                    Class<?> keyClass = (Class<?>) mapType.getActualTypeArguments()[0];
                    Class<?> valueClass = (Class<?>) mapType.getActualTypeArguments()[1];
                    Map<Object, Object> values = new HashMap<>();
                    String keyString = null;
                    String valueString = null;
                    //not implemented yet
                    for (Map.Entry entry : values.entrySet()) {
                        Object objectObjectMapKey = returnValue(keyClass, keyString);
                        Object objectObjectMapValue = returnValue(valueClass, valueString);
                        values.put(objectObjectMapKey, objectObjectMapValue);
                    }
                    value = values;
                } else {
                    value = returnValue(type, s);
                }
                if (value == null) {
                    createMapFromJsonString(s, type);
                }
                map.put(name, value);
            }
        }
        return map;
    }

    private static Object returnValue(Class type, String string) {
        if (string != null) {
            String s = string.replaceAll("\"", "");
            if (type == String.class) {
                return s;
            } else if (type == UUID.class) {
                return UUID.fromString(s);
            } else if (type == OffsetDateTime.class) {
                return OffsetDateTime.parse(s);
            } else if (type == Double.class) {
                return Double.valueOf(s);
            } else if (type == BigDecimal.class) {
                return new BigDecimal(s);
            } else {
                return null;
            }
        }
        return null;
    }
}
