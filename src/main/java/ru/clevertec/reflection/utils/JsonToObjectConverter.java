package ru.clevertec.reflection.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
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
            if (s!= null) {
                s = s.substring(s.indexOf("\":") + INDENTATION)
                        .trim().replaceAll("\"", "");

                if (type == UUID.class) {
                    map.put(name, UUID.fromString(s));
                } else if (type == OffsetDateTime.class) {
                    map.put(name, OffsetDateTime.parse(s));
                } else if (type.isPrimitive()) {
                    map.put(name, s);
                } else if (type == List.class) {
                    List<Object> list = new ArrayList<>();
                    s = s.replace("[", "")
                            .replace("]", "");
                    String key = name;
                    String[] arr = s.split("\\}\s*,\s*\\{");
                    ParameterizedType listType = (ParameterizedType) field.getGenericType();
                    Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
                    for (String str : arr) {
                        Map<String, Object> map2 = createMapFromJsonString(str, listClass);
                        list.add(createObject(map2, listClass));
                    }
                    map.put(key, list);
                } else {
                    map.put(name, null);
                }
            }
        }
        return map;
    }

}
