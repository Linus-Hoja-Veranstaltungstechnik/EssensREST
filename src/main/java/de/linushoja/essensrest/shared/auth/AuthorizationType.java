package de.linushoja.essensrest.shared.auth;

public enum AuthorizationType {
    API_KEY("apiKey", "X-API-Key");

    final String value;
    final String headerName;

    AuthorizationType(String value, String headerName) {
        this.value = value;
        this.headerName = headerName;
    }

    public String value() {
        return value;
    }
    public String getHeaderName() {
        return headerName;
    }
}
