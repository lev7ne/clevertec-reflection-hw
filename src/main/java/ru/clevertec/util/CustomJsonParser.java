package ru.clevertec.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;


public class CustomJsonParser {

    private static final Map<Class<?>, Function<String, Object>> parsers = new HashMap<>();

    static {
        parsers.put(String.class, CustomJsonParser::removeQuotes);
        parsers.put(Integer.class, Integer::parseInt);
        parsers.put(int.class, Integer::parseInt);
        parsers.put(Double.class, Double::parseDouble);
        parsers.put(double.class, Double::parseDouble);
        parsers.put(Boolean.class, Boolean::parseBoolean);
        parsers.put(boolean.class, Boolean::parseBoolean);
        parsers.put(UUID.class, value -> UUID.fromString(removeQuotes(value)));
        parsers.put(BigDecimal.class, value -> new BigDecimal(removeQuotes(value)));
        parsers.put(LocalDate.class, value -> LocalDate.parse(removeQuotes(value)));
        parsers.put(OffsetDateTime.class, value -> OffsetDateTime.parse(removeQuotes(value)));
    }

    /**
     *
     */
    public static <T> T toObjectFromJson(String json, Class<T> clazz) throws Exception {
        json = json.trim();
        if (json.startsWith("{")) {
            return parseObject(json, clazz);
        } else {
            throw new IllegalArgumentException("JSON должен начинаться с '{'");
        }
    }

    private static <T> T parseObject(String json, Class<T> clazz) throws Exception {
        json = json.substring(1, json.length() - 1).trim(); // Отрезаем { и }
        List<String> keyValuePairs = splitJsonFields(json);
        T instance = clazz.getDeclaredConstructor().newInstance();

        for (String pair : keyValuePairs) {
            String[] entry = pair.split(":", 2);
            if (entry.length != 2) {
                continue; // Если пара некорректная, пропускаем и идем дальше
            }
            String key = removeQuotes(entry[0].trim());
            String value = entry[1].trim();

            try {
                Field field = getField(clazz, key);
                if (field == null) {
                    System.out.println("Поле '" + key + "' не найдено в классе " + clazz.getName());
                    continue;
                }
                field.setAccessible(true);
                Object parsedValue = parseValue(value, field);
                field.set(instance, parsedValue);
            } catch (NoSuchFieldException e) {
                System.out.println("Поле '" + key + "' не найдено в классе " + clazz.getName());
            }
        }
        return instance;
    }

    private static List<String> splitJsonFields(String json) {
        List<String> result = new ArrayList<>();
        int braceCount = 0, bracketCount = 0;
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (!inQuotes) {
                if (c == '{') braceCount++;
                if (c == '}') braceCount--;
                if (c == '[') bracketCount++;
                if (c == ']') bracketCount--;
            }
            if (c == ',' && braceCount == 0 && bracketCount == 0 && !inQuotes) {
                result.add(currentField.toString().trim());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }

        if (currentField.length() > 0) {
            result.add(currentField.toString().trim());
        }
        return result;
    }

    private static String removeQuotes(String str) {
        str = str.trim();
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    private static Field getField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private static Object parseValue(String value, Field field) throws Exception {
        Class<?> fieldType = field.getType();
        if (parsers.containsKey(fieldType)) {
            return parsers.get(fieldType).apply(value);
        } else if (List.class.isAssignableFrom(fieldType)) {
            return parseList(value, field);
        } else if (Map.class.isAssignableFrom(fieldType)) {
            return parseMap(value, field);
        } else {
            return parseObject(value, fieldType);
        }
    }

    private static List<?> parseList(String json, Field field) throws Exception {
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) {
            throw new IllegalArgumentException("Некорректный формат списка: " + json);
        }
        json = json.substring(1, json.length() - 1).trim(); // Отрезаем [ и ]
        List<String> elements = splitJsonFields(json);
        List<Object> list = new ArrayList<>();
        Type genericType = field.getGenericType();

        if (!(genericType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("List поле должно иметь параметризованный тип");
        }

        ParameterizedType pType = (ParameterizedType) genericType;
        Class<?> itemClass = (Class<?>) pType.getActualTypeArguments()[0];

        for (String element : elements) {
            Object parsedElement = (parsers.containsKey(itemClass)) ?
                    parsers.get(itemClass).apply(element.trim()) :
                    parseObject(element.trim(), itemClass);
            list.add(parsedElement);
        }
        return list;
    }

    private static Map<?, ?> parseMap(String json, Field field) throws Exception {
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("Некорректный формат карты: " + json);
        }
        json = json.substring(1, json.length() - 1).trim(); // Отрезаем { и }
        List<String> keyValuePairs = splitJsonFields(json);
        Map<Object, Object> map = new HashMap<>();
        Type genericType = field.getGenericType();

        if (!(genericType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Map поле должно иметь параметризованный тип");
        }

        ParameterizedType pType = (ParameterizedType) genericType;
        Class<?> keyClass = (Class<?>) pType.getActualTypeArguments()[0];
        Class<?> valueClass = (Class<?>) pType.getActualTypeArguments()[1];

        for (String pair : keyValuePairs) {
            String[] entry = pair.split(":", 2);
            if (entry.length != 2) {
                continue; // Если пара некорректная, пропускаем и идем дальше
            }
            Object parsedKey = parsePrimitive(entry[0].trim(), keyClass);
            Object parsedValue = (parsers.containsKey(valueClass)) ?
                    parsers.get(valueClass).apply(entry[1].trim()) :
                    parseObject(entry[1].trim(), valueClass);
            map.put(parsedKey, parsedValue);
        }
        return map;
    }

    private static Object parsePrimitive(String value, Class<?> clazz) {
        return parsers.get(clazz).apply(value);
    }

    /**
     *
     */
    public static String toJson(Object obj) throws IllegalAccessException {
        if (obj == null) {
            return "null";
        }

        StringBuilder jsonBuilder = new StringBuilder();
        Class<?> clazz = obj.getClass();

        if (clazz.isArray()) {
            arrayToJson(jsonBuilder, obj);
        } else if (obj instanceof List<?>) {
            listToJson(jsonBuilder, (List<?>) obj);
        } else if (obj instanceof Map<?, ?>) {
            mapToJson(jsonBuilder, (Map<?, ?>) obj);
        } else {
            objectToJson(jsonBuilder, obj);
        }

        return jsonBuilder.toString();
    }

    private static void arrayToJson(StringBuilder jsonBuilder, Object array) throws IllegalAccessException {
        jsonBuilder.append("[");
        int length = java.lang.reflect.Array.getLength(array);
        for (int i = 0; i < length; i++) {
            jsonBuilder.append(toJson(java.lang.reflect.Array.get(array, i)));
            if (i < length - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
    }

    private static void listToJson(StringBuilder jsonBuilder, List<?> list) throws IllegalAccessException {
        jsonBuilder.append("[");
        for (int i = 0; i < list.size(); i++) {
            jsonBuilder.append(toJson(list.get(i)));
            if (i < list.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
    }

    private static void mapToJson(StringBuilder jsonBuilder, Map<?, ?> map) throws IllegalAccessException {
        jsonBuilder.append("{");
        int count = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            jsonBuilder.append(toJson(entry.getKey())).append(":").append(toJson(entry.getValue()));
            if (count < map.size() - 1) {
                jsonBuilder.append(",");
            }
            count++;
        }
        jsonBuilder.append("}");
    }

    private static void objectToJson(StringBuilder jsonBuilder, Object obj) throws IllegalAccessException {
        jsonBuilder.append("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        int count = 0;

        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            jsonBuilder.append("\"").append(field.getName()).append("\":").append(toJson(value));
            if (count < fields.length - 1) {
                jsonBuilder.append(",");
            }
            count++;
        }
        jsonBuilder.append("}");
    }
}
