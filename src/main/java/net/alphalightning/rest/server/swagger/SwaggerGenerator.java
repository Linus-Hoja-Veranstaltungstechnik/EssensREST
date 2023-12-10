package net.alphalightning.rest.server.swagger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.alphalightning.rest.server.RestApplication;
import net.alphalightning.rest.server.RestMethod;
import net.alphalightning.rest.server.annotations.RestApplicationPath;
import net.alphalightning.rest.server.swagger.annotations.*;
import net.alphalightning.rest.server.swagger.objects.*;
import net.alphalightning.rest.shared.annotations.Path;
import net.alphalightning.rest.shared.annotations.PathParam;
import net.alphalightning.rest.shared.annotations.RestServerInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SwaggerGenerator {
    private static final String API_KEY_AUTHENTICATION_IDENTIFIER = "APIKeyHeader";
    private final Gson gson;
    private final RestApplication application;
    private final File swaggerFile;
    private final String basePath;

    public SwaggerGenerator(RestApplication application) {
        gson = initSwagger();
        this.application = application;
        this.basePath = application.getClass().getAnnotation(RestApplicationPath.class).value();

        assert basePath != null;
        assert !basePath.isEmpty();

        this.swaggerFile = new File(String.format("alpharest%sswagger%s%s%sswagger.json", File.separator, File.separator, basePath, File.separator));
    }

    public File generate() {
        try {
            String title = application.getClass().isAnnotationPresent(SwaggerTitle.class) ? application.getClass().getAnnotation(SwaggerTitle.class).value() : application.getClass().getSimpleName();

            SwaggerDocumentationBuilder builder = SwaggerDocumentation.getBuilder(new SwaggerInfo(title, "1.0"));

            builder.withBasePath(application.getClass().getAnnotation(RestApplicationPath.class).value());
            builder.withSchemes(SwaggerScheme.HTTP);
            builder.withSecurityDefinition(API_KEY_AUTHENTICATION_IDENTIFIER, new SwaggerSecurityDefinition(SwaggerSecurityDefinition.AuthorizationType.API_KEY, SwaggerSecurityDefinition.InLocation.HEADER, "X-API-Key"));
            addRestMethods(builder);

            String host = application.getClass().isAnnotationPresent(RestServerInfo.class) ? application.getClass().getAnnotation(RestServerInfo.class).host() : (String) RestServerInfo.class.getMethod("host").getDefaultValue();
            int port = application.getClass().isAnnotationPresent(RestServerInfo.class) ? application.getClass().getAnnotation(RestServerInfo.class).httpPort() : (int) RestServerInfo.class.getMethod("httpPort").getDefaultValue();

            builder.withHost(host + ":" + port);

            SwaggerDocumentation swaggerDocumentation = builder.build();

            store(swaggerDocumentation);

            return swaggerFile;
        } catch (Exception e) {
            throw new RuntimeException("Could not generate Swagger!", e);
        }
    }

    private void addRestMethods(SwaggerDocumentationBuilder builder) {
        for (Method method : application.getClass().getMethods()) {
            if (method.isAnnotationPresent(Path.class)) {
                String description = method.isAnnotationPresent(SwaggerDescription.class) ? method.getAnnotation(SwaggerDescription.class).value() : "";

                SwaggerRestMethod restMethod = new SwaggerRestMethod(method.getAnnotation(Path.class).value(), RestMethod.GET);

                restMethod.withDescription(description);
                restMethod.withParameters(addRestParameters(method));
                restMethod.withResponses(addResponses(method));
                restMethod.withSecurity(API_KEY_AUTHENTICATION_IDENTIFIER);
                builder.withRestMethod(restMethod);
            }
        }
    }

    private SwaggerRestResponse[] addResponses(Method method) {
        List<SwaggerRestResponse> responses = new LinkedList<>();

        if (method.isAnnotationPresent(SwaggerResponse.class)) {
            for (SwaggerResponse responseAnnotation : method.getAnnotation(SwaggerResponses.class).value()) {
                SwaggerRestResponse response = new SwaggerRestResponse(responseAnnotation.code());
                response.withDescription(responseAnnotation.description());
                responses.add(response);
            }
        }

        return responses.toArray(SwaggerRestResponse[]::new);
    }

    private SwaggerRestMethodParameter[] addRestParameters(Method method) {
        List<SwaggerRestMethodParameter> parameters = new ArrayList<>();

        for (Parameter methodParam : method.getParameters()) {
            if (methodParam.isAnnotationPresent(PathParam.class)) {
                SwaggerRestMethodParameter restParam = new SwaggerRestMethodParameter(methodParam.getAnnotation(PathParam.class).value());

                Class<?> type = methodParam.getType();
                restParam.setType(type);

                if (type.isEnum()) {
                    Class<? extends Enum> enumType = (Class<? extends Enum>) type;
                    Enum[] enumValues = enumType.getEnumConstants();
                    String[] enumStrings = Arrays.stream(enumValues).map(Enum::name).toArray(String[]::new);
                    restParam.setEnumValues(enumStrings);
                    restParam.setType(String.class);
                }

                if (methodParam.isAnnotationPresent(SwaggerParameter.class)) {
                    SwaggerParameter swaggerParameter = methodParam.getAnnotation(SwaggerParameter.class);

                    restParam.setDescription(swaggerParameter.description());
                    restParam.setRequired(swaggerParameter.required());
                    if (!type.isEnum() && swaggerParameter.possibleValues().length > 0)
                        restParam.setEnumValues(swaggerParameter.possibleValues());
                    restParam.setMaximum(swaggerParameter.max());
                    restParam.setMinimum(swaggerParameter.min());
                }

                parameters.add(restParam);
            }
        }

        return parameters.toArray(SwaggerRestMethodParameter[]::new);
    }

    private void store(SwaggerDocumentation swaggerDocumentation) {
        try {
            createSwaggerFileIfNotExisting();
        } catch (IOException e) {
            throw new RuntimeException("Could not create Swagger file!", e);
        }

        try (FileWriter fileWriter = new FileWriter(swaggerFile)) {
            gson.toJson(swaggerDocumentation, fileWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Gson initSwagger() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder.setPrettyPrinting().enableComplexMapKeySerialization().create();
    }

    private void createSwaggerFileIfNotExisting() throws IOException {
        if (!swaggerFile.exists()) {
            swaggerFile.getParentFile().mkdirs();
            swaggerFile.createNewFile();
        }
    }
}
