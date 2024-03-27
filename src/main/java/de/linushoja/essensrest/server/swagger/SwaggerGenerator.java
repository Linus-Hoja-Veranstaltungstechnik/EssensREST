package de.linushoja.essensrest.server.swagger;

import de.linushoja.essensrest.gson.GsonHelper;
import de.linushoja.essensrest.server.RestApplication;
import de.linushoja.essensrest.server.swagger.annotations.*;
import de.linushoja.essensrest.server.swagger.objects.*;
import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.server.annotations.RestApplicationPath;
import de.linushoja.essensrest.shared.annotations.*;
import de.linushoja.essensrest.shared.InType;
import de.linushoja.essensrest.shared.auth.AuthorizationType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SwaggerGenerator {
    private static final String API_KEY_AUTHENTICATION_IDENTIFIER = "APIKeyHeader";
    private final RestApplication application;
    private final File swaggerFile;
    private final String basePath;

    public SwaggerGenerator(RestApplication application) {
        this.application = application;
        this.basePath = application.getClass().getAnnotation(RestApplicationPath.class).value();

        assert basePath != null;
        assert !basePath.isEmpty();

        this.swaggerFile = new File(String.format("essensrest%sswagger%s%s%sswagger.json", File.separator, File.separator, basePath, File.separator));
    }

    public File generate() {
        try {
            String title = application.getClass().isAnnotationPresent(SwaggerTitle.class) ? application.getClass().getAnnotation(SwaggerTitle.class).value() : application.getClass().getSimpleName();

            SwaggerDocumentationBuilder builder = SwaggerDocumentation.getBuilder(new SwaggerInfo(title, "1.0"));

            builder.withBasePath(application.getClass().getAnnotation(RestApplicationPath.class).value());
            builder.withSchemes(SwaggerScheme.HTTP);
            builder.withSecurityDefinition(API_KEY_AUTHENTICATION_IDENTIFIER, new SwaggerSecurityDefinition(AuthorizationType.API_KEY, SwaggerSecurityDefinition.InLocation.HEADER, "X-API-Key"));
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

    private void addRestMethods(SwaggerDocumentationBuilder builder) throws IOException {
        for (Method method : application.getClass().getMethods()) {
            if (method.isAnnotationPresent(Path.class)) {
                String description = method.isAnnotationPresent(SwaggerDescription.class) ? method.getAnnotation(SwaggerDescription.class).value() : "";

                RestMethod restMethod = RestMethod.GET;
                restMethod = method.isAnnotationPresent(GET.class) ? RestMethod.GET : restMethod;
                restMethod = method.isAnnotationPresent(PUT.class) ? RestMethod.PUT : restMethod;
                restMethod = method.isAnnotationPresent(DELETE.class) ? RestMethod.DELETE : restMethod;
                restMethod = method.isAnnotationPresent(POST.class) ? RestMethod.POST : restMethod;

                SwaggerRestMethod swaggerRestMethod = new SwaggerRestMethod(method.getAnnotation(Path.class).value(), restMethod);

                swaggerRestMethod.withDescription(description);
                swaggerRestMethod.withParameters(addRestParameters(method));
                swaggerRestMethod.withResponses(addResponses(method));
                swaggerRestMethod.withSecurity(API_KEY_AUTHENTICATION_IDENTIFIER);
                builder.withRestMethod(swaggerRestMethod);
            }
        }
    }

    private SwaggerRestResponse[] addResponses(Method method) {
        List<SwaggerRestResponse> responses = new LinkedList<>();

        if (method.isAnnotationPresent(SwaggerResponse.class)) {
            for (SwaggerResponse responseAnnotation : method.getAnnotationsByType(SwaggerResponse.class)) {
                SwaggerRestResponse response = new SwaggerRestResponse(responseAnnotation.code());
                response.withDescription(responseAnnotation.description());
                responses.add(response);
            }
        }

        return responses.toArray(SwaggerRestResponse[]::new);
    }

    private SwaggerRestMethodParameter[] addRestParameters(Method method) throws IOException {
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

                if(type == int.class){
                    restParam.setType(Integer.class);
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

            if (methodParam.isAnnotationPresent(Entity.class)){
                String example = "";
                if(methodParam.isAnnotationPresent(SwaggerExample.class)) {
                    SwaggerExample swaggerExample = methodParam.getAnnotation(SwaggerExample.class);
                    example = swaggerExample.value();
                    if(!swaggerExample.exampleJson().isBlank()) example = Files.readString(new File(swaggerExample.exampleJson()).toPath());
                }
                SwaggerRestMethodParameter restParameter = new SwaggerRestMethodParameter(methodParam.getAnnotation(Entity.class).name())
                        .withIn(InType.BODY)
                        .withExample(example);
                parameters.add(restParameter);
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
            GsonHelper.getGson().toJson(swaggerDocumentation, fileWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createSwaggerFileIfNotExisting() throws IOException {
        if (!swaggerFile.exists()) {
            swaggerFile.getParentFile().mkdirs();
            swaggerFile.createNewFile();
        }
    }
}
