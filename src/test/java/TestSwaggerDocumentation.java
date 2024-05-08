import de.linushoja.essensrest.gson.GsonHelper;
import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.server.swagger.objects.SwaggerDocumentation;
import de.linushoja.essensrest.server.swagger.objects.SwaggerDocumentationBuilder;
import de.linushoja.essensrest.server.swagger.objects.SwaggerInfo;
import de.linushoja.essensrest.server.swagger.objects.SwaggerRestMethod;
import de.linushoja.essensrest.server.swagger.objects.SwaggerRestMethodParameter;
import de.linushoja.essensrest.server.swagger.objects.SwaggerScheme;
import de.linushoja.essensrest.server.swagger.objects.SwaggerSecurityDefinition;
import de.linushoja.essensrest.shared.auth.AuthorizationType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class TestSwaggerDocumentation {

    private final static File SWAGGER_FILE = new File("essensrest/swagger.json");

    public TestSwaggerDocumentation() {

        SwaggerDocumentationBuilder builder = SwaggerDocumentation.getBuilder(new SwaggerInfo("Test", "1.0"));
        builder.withBasePath("/essensrest/test");
        builder.withSecurityDefinition("APIKeyHeader",
                new SwaggerSecurityDefinition(AuthorizationType.API_KEY.value(),
                        SwaggerSecurityDefinition.InLocation.HEADER, "X-API-Key"));
        builder.withRestMethod(new SwaggerRestMethod("/enum/{enum}", RestMethod.GET).withDescription("A method to test enum compatibility.").withParameters(new SwaggerRestMethodParameter("enum").withDescription("Enum value of RestMethod.").withEnumValues(Arrays.stream(RestMethod.values()).map(RestMethod::toString).toArray(String[]::new))).withSecurity("APIKeyHeader"));
        builder.withSchemes(SwaggerScheme.HTTPS, SwaggerScheme.HTTP);
        SwaggerDocumentation swaggerDocumentation = builder.build();

        storeSwaggerDocumentationToLocalStorage(swaggerDocumentation);
    }

    private void storeSwaggerDocumentationToLocalStorage(SwaggerDocumentation swaggerDocumentation) {
        try {
            if (!SWAGGER_FILE.exists()) {
                if (!(SWAGGER_FILE.getParentFile().mkdirs() && SWAGGER_FILE.createNewFile())) {
                    throw new RuntimeException("Could not create swagger file!");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileWriter fileWriter = new FileWriter(SWAGGER_FILE)) {
            GsonHelper.getGson().toJson(swaggerDocumentation, fileWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
