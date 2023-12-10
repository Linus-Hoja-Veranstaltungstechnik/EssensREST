package net.alphalightning.rest.server.swagger.objects;

public enum SwaggerScheme {
    HTTP("http"),
    HTTPS("https");

    private final String schemeString;

    SwaggerScheme(String schemeString) {
        this.schemeString = schemeString;
    }

    @Override
    public String toString() {
        return schemeString;
    }
}
