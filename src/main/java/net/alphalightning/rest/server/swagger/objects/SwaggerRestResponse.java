package net.alphalightning.rest.server.swagger.objects;

public class SwaggerRestResponse {
    private final transient int code;
    private String description = "";

    public SwaggerRestResponse(int code) {
        this.code = code;
    }

    public SwaggerRestResponse withDescription(String description) {
        setDescription(description);
        return this;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
