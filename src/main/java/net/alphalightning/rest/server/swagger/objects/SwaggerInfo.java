package net.alphalightning.rest.server.swagger.objects;

@SuppressWarnings("unused")
public record SwaggerInfo(String title, String version, String description) {
    public SwaggerInfo(String title, String version) {
        this(title, version, "");
    }
}
