package de.linushoja.essensrest.server;

import de.linushoja.essensrest.shared.annotations.DELETE;
import de.linushoja.essensrest.shared.annotations.GET;
import de.linushoja.essensrest.shared.annotations.POST;
import de.linushoja.essensrest.shared.annotations.PUT;

import java.lang.annotation.Annotation;

public enum RestMethod {
    GET(GET.class),
    POST(POST.class),
    PUT(PUT.class),
    DELETE(DELETE.class),
    OPTIONS();

    private final Class<? extends Annotation> annotation;

    RestMethod(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    RestMethod() {
        this.annotation = null;
    }

    public Class<? extends Annotation> getAnnotation() {
        return annotation;
    }

    public static RestMethod getMethodByAnnotation(Class<? extends Annotation> annotation){
        for(RestMethod restMethod : values()){
            if (restMethod.annotation == null) {
                continue;
            }
            if (!restMethod.annotation.isAssignableFrom(annotation)) {
                continue;
            }
            return restMethod;
        }
        return null;
    }

    @Override
    public String toString() {
        return super.name();
    }
}
