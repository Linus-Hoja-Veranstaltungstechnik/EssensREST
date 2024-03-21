import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.server.swagger.objects.*;
import de.linushoja.essensrest.shared.auth.AuthorizationType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class TestSwaggerDocumentation {

    private final static File SWAGGER_FILE = new File("essensrest/swagger.json");
    private final Gson gson;

    public TestSwaggerDocumentation() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.setPrettyPrinting().enableComplexMapKeySerialization().create();

        SwaggerDocumentationBuilder builder = SwaggerDocumentation.getBuilder(new SwaggerInfo("Test", "1.0"));
        builder.withBasePath("/essensrest/test");
        builder.withSecurityDefinition("APIKeyHeader", new SwaggerSecurityDefinition(AuthorizationType.API_KEY, SwaggerSecurityDefinition.InLocation.HEADER, "X-API-Key"));
        builder.withRestMethod(new SwaggerRestMethod("/enum/{enum}", RestMethod.GET).withDescription("A method to test enum compatibility.").withParameters(new SwaggerRestMethodParameter("enum").withDescription("Enum value of RestMethod.").withEnumValues(Arrays.stream(RestMethod.values()).map(RestMethod::toString).toArray(String[]::new))).withSecurity("APIKeyHeader"));
        builder.withSchemes(SwaggerScheme.HTTPS, SwaggerScheme.HTTP);
        SwaggerDocumentation swaggerDocumentation = builder.build();

        storeSwaggerDocumentationToLocalStorage(swaggerDocumentation);
    }

    private void storeSwaggerDocumentationToLocalStorage(SwaggerDocumentation swaggerDocumentation) {
        try {
            if (!SWAGGER_FILE.exists()) {
                SWAGGER_FILE.getParentFile().mkdirs();
                SWAGGER_FILE.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileWriter fileWriter = new FileWriter(SWAGGER_FILE)) {
            gson.toJson(swaggerDocumentation, fileWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
