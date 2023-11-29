package net.alphalightning.rest;

import net.alphalightning.rest.annotations.GET;
import net.alphalightning.rest.annotations.POST;
import net.alphalightning.rest.annotations.PUT;
import net.alphalightning.rest.annotations.PATCH;
import net.alphalightning.rest.annotations.DELETE;

import java.lang.annotation.Annotation;

public enum RestMethod {
    GET(GET.class),
    POST(POST.class),
    PUT(PUT.class),
    PATCH(PATCH.class),
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
