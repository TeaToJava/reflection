package ru.clevertec.reflection.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Converter {
    private static final String LEFT_CR_BRACE = "{";
    private static final String RIGHT_CR_BRACE = "}";
    private static final String LEFT_SQ_BRACE = "[";
    private static final String RIGHT_SQ_BRACE = "]";
    static StringBuilder stringBuilder = new StringBuilder();

    public static Map<String, Object> parse(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Class c = obj.getClass();
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            map.put(field.getName(), value);
        }
        return map;
    }

    public static StringBuilder createJson(Object obj) throws IllegalAccessException {
        Map<String, Object> map = parse(obj);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                stringBuilder.append("\"" + entry.getKey() + "\":");
                appendValue(value);
                stringBuilder.append(",");
            }
        }
        return stringBuilder;
    }

    public static String formatJson(StringBuilder stringBuilder) {
        stringBuilder.append(RIGHT_CR_BRACE);
        stringBuilder.insert(0, LEFT_CR_BRACE);
        return stringBuilder.toString().replaceAll(",]", "]")
                .replaceAll(",+}", "}")
                .replaceAll(",,", ",");

    }

    private static void appendValue(Object object) throws IllegalAccessException {
        if (object == null) {
            stringBuilder.append("");
        } else {
            Class cl = object.getClass();
            if (cl.isPrimitive()) {
                stringBuilder.append(object);
            } else if (object instanceof List<?>) {
                stringBuilder.append(LEFT_SQ_BRACE);
                for (Object obj : (List) object) {
                    stringBuilder.append(LEFT_CR_BRACE);
                    createJson(obj);
                    stringBuilder.append(RIGHT_CR_BRACE);
                    stringBuilder.append(",");
                }
                stringBuilder.append(RIGHT_SQ_BRACE);
            } else if (object instanceof Map<?, ?>) {
                for (Map.Entry entry : ((Map<?, ?>) object).entrySet()) {
                    stringBuilder.append(LEFT_CR_BRACE);
                    stringBuilder.append("\"" + entry.getKey().toString() + "\":");
                    stringBuilder.append(entry.getValue().toString());
                    stringBuilder.append(RIGHT_CR_BRACE);
                    stringBuilder.append(",");
                }

            } else {
                stringBuilder.append("\"" + object + "\"");
            }
        }
    }

}


