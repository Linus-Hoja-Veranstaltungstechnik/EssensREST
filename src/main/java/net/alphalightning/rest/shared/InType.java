package net.alphalightning.rest.shared;

public enum InType {
    BODY("body"),
    QUERY("query"),
    PATH("path");

    private final String name;
    InType(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
