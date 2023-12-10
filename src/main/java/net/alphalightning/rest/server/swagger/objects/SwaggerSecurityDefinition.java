package net.alphalightning.rest.server.swagger.objects;

@SuppressWarnings("unused") // gson
public class SwaggerSecurityDefinition {
    private final String type;
    private final String in;
    private final String name;

    public SwaggerSecurityDefinition(AuthorizationType type, InLocation in, String name) {
        this.type = type.value();
        this.in = in.name().toLowerCase();
        this.name = name;
    }

    public enum InLocation {
        HEADER,
        QUERY
    }

    public enum AuthorizationType {
        API_KEY("apiKey");

        final String value;

        AuthorizationType(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
