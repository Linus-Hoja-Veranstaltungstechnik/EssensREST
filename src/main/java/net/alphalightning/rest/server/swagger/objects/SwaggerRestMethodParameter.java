package net.alphalightning.rest.server.swagger.objects;

import com.google.gson.annotations.SerializedName;
@SuppressWarnings("unused") // gson
public class SwaggerRestMethodParameter {
    private final String name;
    private String in = "path";
    private String description;
    private String type = "string";
    private boolean required = true;
    private Long minimum = null;
    private Long maximum = null;
    @SerializedName("enum")
    private String[] enumValues = null;

    public SwaggerRestMethodParameter(String name) {
        this.name = name;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public SwaggerRestMethodParameter withIn(String in) {
        setIn(in);
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SwaggerRestMethodParameter withDescription(String description) {
        setDescription(description);
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public SwaggerRestMethodParameter withRequired(boolean required) {
        setRequired(required);
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type.getSimpleName().toLowerCase();
    }

    public SwaggerRestMethodParameter withType(Class<?> type) {
        setType(type);
        return this;
    }

    public Long getMinimum() {
        return minimum;
    }

    public void setMinimum(Long minimum) {
        this.minimum = minimum;
    }

    public SwaggerRestMethodParameter withMinimum(Long minimum) {
        setMinimum(minimum);
        return this;
    }

    public Long getMaximum() {
        return maximum;
    }

    public void setMaximum(Long maximum) {
        this.maximum = maximum;
    }

    public SwaggerRestMethodParameter withMaximum(Long maximum) {
        setMaximum(maximum);
        return this;
    }

    public String[] getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(String[] enumValues) {
        this.enumValues = enumValues;
    }

    public SwaggerRestMethodParameter withEnumValues(String... enumValues) {
        setEnumValues(enumValues);
        return this;
    }
}
