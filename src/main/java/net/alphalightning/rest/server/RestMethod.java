package net.alphalightning.rest.server;

import net.alphalightning.rest.shared.annotations.DELETE;
import net.alphalightning.rest.shared.annotations.GET;
import net.alphalightning.rest.shared.annotations.POST;
import net.alphalightning.rest.shared.annotations.PUT;

import java.lang.annotation.Annotation;

public enum RestMethod {
    GET(GET.class),
    POST(POST.class),
    PUT(PUT.class),
    DELETE(DELETE.class);

    private final Class<? extends Annotation> annotation;

    RestMethod(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    public Class<? extends Annotation> getAnnotation() {
        return annotation;
    }

    public static RestMethod getMethodByAnnotation(Class<? extends Annotation> annotation){
        for(RestMethod restMethod : values()){
            if(restMethod.annotation.isAssignableFrom(annotation)) return restMethod;
        }
        return null;
    }

    @Override
    public String toString() {
        return super.name();
    }
}
