package com.exadel.aem.toolkit.api.annotations.injectors;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component(property = Constants.SERVICE_RANKING+":Integer=10000", service = Injector.class)
public class ChildrenInjector implements Injector {
    public static final String NAME = "child-resources";

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

    @ChildResource
    @CheckForNull
    @Override
    public Object getValue(@Nonnull Object adaptable, String name, @Nonnull Type type, @Nonnull AnnotatedElement annotatedElement, @Nonnull DisposalCallbackRegistry disposalCallbackRegistry) {
        Children annotation = annotatedElement.getAnnotation(Children.class);
        Resource resource = extractResource(adaptable);

        if (annotation == null || resource == null) {
            return adaptable;
        }

        return StreamSupport.stream(resource.getChildren().spliterator(), false)
            .collect(Collectors.toList());

    }

    private static Resource extractResource(Object adaptable) {
        if (adaptable instanceof Resource) {
            return (Resource) adaptable;
        } else if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable).getResource();
        }
        return null;
    }
}
