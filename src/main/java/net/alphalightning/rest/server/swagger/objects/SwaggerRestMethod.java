package net.alphalightning.rest.server.swagger.objects;

import net.alphalightning.rest.server.RestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class SwaggerRestMethod {
    private final transient String restPath;
    private final transient String restMethod;
    private final Map<Integer, SwaggerRestResponse> responses = new HashMap<>();
    private String description = "";
    private String operationId = "";
    private SwaggerRestMethodParameter[] parameters = new SwaggerRestMethodParameter[0];
    private List<Map<String, String[]>> security = null;

    public SwaggerRestMethod(String restPath, RestMethod method) {
        this.restPath = restPath;
        this.restMethod = method.toString().toLowerCase();
    }

    public String getRestPath() {
        return restPath;
    }

    public String getRestMethod() {
        return restMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SwaggerRestMethod withDescription(String description) {
        setDescription(description);
        return this;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public SwaggerRestMethod withOperationId(String operationId) {
        setOperationId(operationId);
        return this;
    }

    public SwaggerRestMethodParameter[] getParameters() {
        return parameters;
    }

    public void setParameters(SwaggerRestMethodParameter[] parameters) {
        this.parameters = parameters;
    }

    public SwaggerRestMethod withParameters(SwaggerRestMethodParameter... parameters) {
        setParameters(parameters);
        return this;
    }

    public SwaggerRestMethod withResponses(SwaggerRestResponse... responses) {
        for (SwaggerRestResponse response : responses) {
            this.responses.put(response.getCode(), response);
        }
        return this;
    }

    public SwaggerRestMethod withSecurity(String identifier) {
        setSecurity(identifier);
        return this;
    }

    private void setSecurity(String identifier) {
        if (security == null) security = new ArrayList<>();
        Map<String, String[]> mapValue = new HashMap<>();
        mapValue.put(identifier, new String[0]);
        security.add(mapValue);
    }
}
