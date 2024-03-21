package de.linushoja.essensrest.server.swagger.objects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class SwaggerDocumentationBuilder {

    public final static String APPLICATION_JSON = "application/json";
    private final SwaggerInfo swaggerInfo;
    private final Map<Integer, SwaggerRestResponse> responses = new HashMap<>();
    private final Map<String, SwaggerSecurityDefinition> securityDefinitions = new HashMap<>();
    private final Map<String, Map<String, SwaggerRestMethod>> restMethods = new HashMap<>();
    private String basePath = "/";
    private String host = "localhost:8000";
    private String[] swaggerSchemes = new String[]{};
    private String[] consumes;
    private String[] produces;

    protected SwaggerDocumentationBuilder(SwaggerInfo swaggerInfo) {
        this.swaggerInfo = swaggerInfo;
        withSchemes(SwaggerScheme.HTTP, SwaggerScheme.HTTPS);
        consumes(APPLICATION_JSON);
        produces(APPLICATION_JSON);
    }

    public SwaggerDocumentationBuilder withSchemes(SwaggerScheme... schemes) {
        swaggerSchemes = Arrays.stream(schemes).map(SwaggerScheme::toString).toList().toArray(String[]::new);
        return this;
    }

    public SwaggerDocumentationBuilder withRestMethod(SwaggerRestMethod method) {
        Map<String, SwaggerRestMethod> methodMap = new HashMap<>();
        methodMap.put(method.getRestMethod(), method);
        restMethods.put(method.getRestPath(), methodMap);
        return this;
    }

    public SwaggerDocumentationBuilder consumes(String... consumes) {
        this.consumes = consumes;
        return this;
    }

    public SwaggerDocumentationBuilder produces(String... produces) {
        this.produces = produces;
        return this;
    }

    public SwaggerDocumentationBuilder withBasePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public SwaggerDocumentationBuilder withHost(String host) {
        this.host = host;
        return this;
    }

    public SwaggerDocumentationBuilder withSecurityDefinition(String identifier, SwaggerSecurityDefinition securityDefinition) {
        securityDefinitions.put(identifier, securityDefinition);
        return this;
    }

    public SwaggerDocumentation build() {
        return new SwaggerDocumentation(securityDefinitions, swaggerInfo, host, basePath, swaggerSchemes, consumes, produces, restMethods);
    }
}
