package de.linushoja.essensrest.server.swagger.objects;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

@SuppressWarnings("unused") // gson
public class SwaggerDocumentation {
    private final Map<String, SwaggerSecurityDefinition> securityDefinitions;

    @SerializedName("swagger")
    private final String swaggerVersion = "2.0";

    @SerializedName("info")
    private final SwaggerInfo swaggerInfo;
    private final String host;
    private final String basePath;

    @SerializedName("schemes")
    private final String[] swaggerSchemes;

    private final String[] consumes;
    private final String[] produces;

    @SerializedName("paths")
    private final Map<String, Map<String, SwaggerRestMethod>> restMethods;

    public SwaggerDocumentation(Map<String, SwaggerSecurityDefinition> securityDefinitions, SwaggerInfo swaggerInfo, String host, String basePath, String[] swaggerSchemes, String[] consumes, String[] produces, Map<String, Map<String, SwaggerRestMethod>> restMethods) {
        this.securityDefinitions = securityDefinitions;
        this.swaggerInfo = swaggerInfo;
        this.host = host;
        this.basePath = basePath;
        this.swaggerSchemes = swaggerSchemes;
        this.consumes = consumes;
        this.produces = produces;
        this.restMethods = restMethods;
    }

    public static SwaggerDocumentationBuilder getBuilder(SwaggerInfo info) {
        return new SwaggerDocumentationBuilder(info);
    }
}
