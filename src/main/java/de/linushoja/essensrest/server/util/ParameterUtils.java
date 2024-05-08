package de.linushoja.essensrest.server.util;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public class ParameterUtils {


    @SuppressWarnings("unchecked")
    public static <T> T transformObject(String value, Class<T> type) {
        if(value == null) return null;

        T result;

        if(type.isAssignableFrom(Integer.class)){
            result = (T) Integer.valueOf(value);
        } else if(type.isAssignableFrom(Double.class)){
            result = (T) Double.valueOf(value);
        } else if (type.isAssignableFrom(Boolean.class)) {
            result = (T) Boolean.valueOf(value);
        } else if (type.isAssignableFrom(Long.class)) {
            result = (T) Long.valueOf(value);
        } else if (type.isAssignableFrom(Float.class)) {
            result = (T) Float.valueOf(value);
        } else if (type.isEnum()) {
            Class<? extends Enum> enumType = (Class<? extends Enum>) type;
            result = (T) Enum.valueOf(enumType, value.toUpperCase());
        } else {
            result = (T) value;
        }

        return result;
    }

    public static TypeToken<?> getType(Parameter parameter) {
        if (parameter.getType().isArray()) {
            return TypeToken.get(parameter.getType());
        }
        if (List.class.isAssignableFrom(parameter.getType())) {
            return TypeToken.getParameterized(List.class,
                    ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0]);
        }
        return TypeToken.get(parameter.getType());
    }

}
