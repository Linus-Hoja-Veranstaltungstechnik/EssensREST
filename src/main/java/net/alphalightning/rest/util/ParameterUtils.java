package net.alphalightning.rest.util;

public class ParameterUtils {


    @SuppressWarnings("unchecked")
    public static <T> T transformObject(String value, Class<T> type) {
        if(value == null) return null;

        T result;

        if(type.isAssignableFrom(Integer.class)){
            result = (T) Integer.valueOf(value);
        } else if(type.isAssignableFrom(Double.class)){
            result = (T) Double.valueOf(value);
        } else if(type.isAssignableFrom(Boolean.class)){
            result = (T) Boolean.valueOf(value);
        } else if(type.isAssignableFrom(Long.class)){
            result = (T) Long.valueOf(value);
        } else if(type.isAssignableFrom(Float.class)){
            result = (T) Float.valueOf(value);
        } else {
            try {
                result = (T) value;
            } catch (ClassCastException e) {
                throw new RuntimeException("Unrecognized parameter type: " + type.getCanonicalName());
            }
        }

        return result;
    }

}
