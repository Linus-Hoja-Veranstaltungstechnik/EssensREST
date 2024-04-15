package de.linushoja.essensrest.server.swagger.objects;

@SuppressWarnings("unused") // gson
public class SwaggerSecurityDefinition {
    private final String type;
    private final String in;
    private final String name;

    public SwaggerSecurityDefinition(String identifier, InLocation in, String name) {
        this.type = identifier;
        this.in = in.name().toLowerCase();
        this.name = name;
    }

    public enum InLocation {
        HEADER,
        QUERY
    }

    public String getType() {
        return type;
    }

    public String getIn() {
        return in;
    }

    public String getName() {
        return name;
    }
}
