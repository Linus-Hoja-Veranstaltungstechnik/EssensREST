package de.linushoja.essensrest.server.swagger;

import de.linushoja.essensrest.gson.GsonHelper;
import de.linushoja.essensrest.server.RestApplication;
import de.linushoja.essensrest.server.RestMethod;
import de.linushoja.essensrest.server.annotations.Auth;
import de.linushoja.essensrest.server.annotations.RestApplicationPath;
import de.linushoja.essensrest.server.auth.SingleValueAuthenticator;
import de.linushoja.essensrest.server.swagger.annotations.SwaggerDescription;
import de.linushoja.essensrest.server.swagger.annotations.SwaggerExample;
import de.linushoja.essensrest.server.swagger.annotations.SwaggerParameter;
import de.linushoja.essensrest.server.swagger.annotations.SwaggerResponse;
import de.linushoja.essensrest.server.swagger.annotations.SwaggerTitle;
import de.linushoja.essensrest.server.swagger.objects.SwaggerDocumentation;
import de.linushoja.essensrest.server.swagger.objects.SwaggerDocumentationBuilder;
import de.linushoja.essensrest.server.swagger.objects.SwaggerInfo;
import de.linushoja.essensrest.server.swagger.objects.SwaggerRestMethod;
import de.linushoja.essensrest.server.swagger.objects.SwaggerRestMethodParameter;
import de.linushoja.essensrest.server.swagger.objects.SwaggerRestResponse;
import de.linushoja.essensrest.server.swagger.objects.SwaggerScheme;
import de.linushoja.essensrest.server.swagger.objects.SwaggerSecurityDefinition;
import de.linushoja.essensrest.shared.InType;
import de.linushoja.essensrest.shared.annotations.DELETE;
import de.linushoja.essensrest.shared.annotations.Entity;
import de.linushoja.essensrest.shared.annotations.GET;
import de.linushoja.essensrest.shared.annotations.POST;
import de.linushoja.essensrest.shared.annotations.PUT;
import de.linushoja.essensrest.shared.annotations.Path;
import de.linushoja.essensrest.shared.annotations.PathParam;
import de.linushoja.essensrest.shared.annotations.RestServerInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SwaggerGenerator {
    private final RestApplication application;
    private final File swaggerFile;

    public SwaggerGenerator(RestApplication application) {
        this.application = application;
        String basePath = application.getClass().getAnnotation(RestApplicationPath.class).value();

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
            addAuthenticationsToSwagger(builder, application);
            addRestMethods(builder, application);

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

    private void addAuthenticationsToSwagger(SwaggerDocumentationBuilder builder, RestApplication application) throws
                                                                                                               NoSuchMethodException,
                                                                                                               InvocationTargetException,
                                                                                                               InstantiationException,
                                                                                                               IllegalAccessException {
        if (!application.getClass().isAnnotationPresent(Auth.class)) {
            return;
        }
        for (Class<? extends SingleValueAuthenticator> authenticator :
                application.getClass().getAnnotation(Auth.class).value()) {
            SingleValueAuthenticator authenticatorInstance = authenticator.getConstructor().newInstance();
            String realm = authenticatorInstance.getRealm();
            String headerName = authenticatorInstance.getHeaderName();
            builder.withSecurityDefinition(realm,
                    new SwaggerSecurityDefinition(realm, SwaggerSecurityDefinition.InLocation.HEADER, headerName));
        }
    }

    private void addRestMethods(SwaggerDocumentationBuilder builder, RestApplication restApplication) throws
                                                                                                      IOException,
                                                                                                      InvocationTargetException,
                                                                                                      NoSuchMethodException,
                                                                                                      InstantiationException,
                                                                                                      IllegalAccessException {
        for (Method method : application.getClass().getMethods()) {
            if (method.isAnnotationPresent(Path.class)) {
                String description = method.isAnnotationPresent(SwaggerDescription.class) ? method.getAnnotation(SwaggerDescription.class).value() : "";

                SwaggerRestMethod swaggerRestMethod = getSwaggerRestMethod(method, description);
                swaggerRestMethod.withParameters(addRestParameters(method));
                swaggerRestMethod.withResponses(addResponses(method));
                addAuthenticationsToRestMethod(swaggerRestMethod, restApplication);
                builder.withRestMethod(swaggerRestMethod);
            }
        }
    }

    private static SwaggerRestMethod getSwaggerRestMethod(Method method, String description) {
        RestMethod restMethod = RestMethod.OPTIONS;
        restMethod = method.isAnnotationPresent(GET.class) ? RestMethod.GET : restMethod;
        restMethod = method.isAnnotationPresent(PUT.class) ? RestMethod.PUT : restMethod;
        restMethod = method.isAnnotationPresent(DELETE.class) ? RestMethod.DELETE : restMethod;
        restMethod = method.isAnnotationPresent(POST.class) ? RestMethod.POST : restMethod;

        SwaggerRestMethod swaggerRestMethod = new SwaggerRestMethod(method.getAnnotation(Path.class).value(),
                restMethod);

        swaggerRestMethod.withDescription(description);
        return swaggerRestMethod;
    }

    private void addAuthenticationsToRestMethod(SwaggerRestMethod swaggerRestMethod, RestApplication application) throws
                                                                                                                  NoSuchMethodException,
                                                                                                                  InvocationTargetException,
                                                                                                                  InstantiationException,
                                                                                                                  IllegalAccessException {
        if (!application.getClass().isAnnotationPresent(Auth.class)) {
            return;
        }
        for (Class<? extends SingleValueAuthenticator> authenticator :
                application.getClass().getAnnotation(Auth.class).value()) {
            SingleValueAuthenticator authenticatorInstance = authenticator.getConstructor().newInstance();
            String realm = authenticatorInstance.getRealm();
            swaggerRestMethod.withSecurity(realm);
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
                    @SuppressWarnings("unchecked")
                    Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) type;
                    Enum<?>[] enumValues = enumType.getEnumConstants();
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
            if (!(swaggerFile.getParentFile().mkdirs() && swaggerFile.createNewFile())) {
                throw new RuntimeException("Could not create swagger file!");
            }
        }
    }
}
