package net.alphalightning.rest.server.swagger.objects;

import net.alphalightning.rest.shared.auth.AuthorizationType;

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
