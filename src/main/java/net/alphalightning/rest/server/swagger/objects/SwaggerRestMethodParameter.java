package net.alphalightning.rest.server.swagger.objects;

import com.google.gson.annotations.SerializedName;
import net.alphalightning.rest.shared.InType;

@SuppressWarnings("unused") // gson
public class SwaggerRestMethodParameter {
    private final String name;
    private String in = InType.PATH.toString();
    private String description;
    private String type = "string";
    private boolean required = true;
    private Long minimum = null;
    private Long maximum = null;
    private String example = null;
    @SerializedName("enum")
    private String[] enumValues = null;

    public SwaggerRestMethodParameter(String name) {
        this.name = name;
    }

    public InType getIn() {
        return InType.valueOf(in.toUpperCase());
    }

    public void setIn(InType in) {
        this.in = in.toString();
    }

    public SwaggerRestMethodParameter withIn(InType in) {
        setIn(in);
        return this;
    }

    public SwaggerRestMethodParameter withExample(String example) {
        setExample(example);
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

    public String getExample() {
        return example;
    }

    public String getName() {
        return name;
    }

    public void setExample(String example) {
        this.example = example;
    }
}
