package com.exadel.aem.toolkit.core.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.core.processor.models.ElementDefinition;

//@SupportedAnnotationTypes("com.exadel.aem.toolkit.api.annotations.main.AemComponent")
@SupportedAnnotationTypes("com.exadel.aem.toolkit.api.annotations.*")
public class EToolboxProcessor extends AbstractProcessor {

    private static final String EAK_ANNOTATION_PREFIX = "com.exadel.aem.toolkit.api.annotations";
    private static Elements elementUtils;
    private Filer filer;
    private static Types typeUtils;
    private Gson gson;
    private FileObject properties;
    private static TypeElement object;
    private long time;
    private Messager messager;
    public static Set<String> customAnnotationNames;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        object = elementUtils.getTypeElement("java.lang.Object");
        this.messager = processingEnv.getMessager();
        this.gson = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
        try {
            this.properties = filer.getResource(StandardLocation.CLASS_OUTPUT, "", "etoolbox.properties");
        } catch (IOException e) {
            // ignored
        }

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        EToolboxProcessor.customAnnotationNames = roundEnv
            .getElementsAnnotatedWith(Handles.class)
            .stream()
            .map(element -> element.getAnnotation(Handles.class))
            .flatMap(this::getCustomAnnotationNames)
            .collect(Collectors.toSet());

        Set<? extends Element> elementsAnnotatedWithAemComponent = roundEnv
            .getElementsAnnotatedWith(AemComponent.class);

        elementsAnnotatedWithAemComponent
            .forEach(this::writeElementToTarget);

        return true;
    }

    public static boolean isProcessableAnnotationPresents(Element element) {
        return element.getAnnotationMirrors()
            .stream()
            .map(AnnotationMirror::getAnnotationType)
            .map(Objects::toString)
            .anyMatch(annotationName -> StringUtils.startsWith(annotationName, EAK_ANNOTATION_PREFIX)
                || customAnnotationNames.contains(annotationName));

    }

    // Retrieves class names as stream from Handles#value method.
    private Stream<String> getCustomAnnotationNames(Handles handles) {
        try {
            return Arrays.stream(handles.value())
                .map(Class::getCanonicalName);
        } catch (MirroredTypesException typesException) {
            return typesException.getTypeMirrors()
                .stream()
                .map(TypeMirror::toString);
        }
    }

    private void writeElementToTarget(Element element) {
        ComponentFacade.process(element)
            .forEach((key, value) -> writeElementDefinitionToTarget(value, key));
    }

    private void writeElementDefinitionToTarget(ElementDefinition elementDefinition, String path) {
        try {
            FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "etoolbox", path + ".json");
            Writer writer = fileObject.openWriter();
            writer.write(gson.toJson(elementDefinition));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public static <T extends Annotation> T createInstance(Class<T> type, Map<String, Object> values) {
        Map<String, BiFunction<Annotation, Object[], Object>> methods = new HashMap<>();
        for (Method method : type.getDeclaredMethods()) {
            BiFunction<Annotation, Object[], Object> methodFunction = (src, args) -> {
                if (values != null && values.containsKey(method.getName())) {
                    Object o = values.get(method.getName());
                    if (o.getClass().isArray() && ((Object[]) o)[0] instanceof AnnotationInfo) {
                        AnnotationInfo annotationInfo = (AnnotationInfo) ((Object[]) o)[0];
                        return castToArray(Arrays.stream(((Object[]) o))
                            .map(AnnotationInfo.class::cast)
                            .map(qwe -> createInstance(getClass(qwe), qwe.getValues()))
                            .toArray(), getClass(annotationInfo));
                    }
                    if (o instanceof AnnotationInfo) {
                        return createInstance(getClass((AnnotationInfo) o), ((AnnotationInfo) o).getValues());
                    } else {
                        return o;
                    }
                }
                return method.getDefaultValue();
            };
            methods.put(method.getName(), methodFunction);
        }
        return genericModify(null, type, methods);
    }

    private static Class getClass(AnnotationInfo annotationInfo) {
        try {
            return Class.forName(annotationInfo.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <T, U, R> U genericModify(T value, Class<U> modification, Map<String, BiFunction<T, Object[], R>> methods) {
        if (modification == null) {
            return null;
        }
        if (methods == null || methods.isEmpty()) {
            try {
                return modification.cast(value);
            } catch (ClassCastException e) {
                return null;
            }
        }
        Object result = Proxy.newProxyInstance(modification.getClassLoader(),
            new Class[]{modification},
            new AnnotationUtil.ExtensionInvocationHandler<>(value, modification, methods));
        return modification.cast(result);
    }

    private static <T, U> U[] castToArray(T[] source, Class<U> targetType) {
        U[] result = (U[]) Array.newInstance(targetType, source.length);
        for (int i = 0; i < source.length; i++) {
            result[i] = (U) source[i];
        }
        return result;
    }*/

    public static Types getTypeUtils() {
        return typeUtils;
    }

    public static Elements getElementUtils() {
        return elementUtils;
    }

    public static boolean isObject(Element element) {
        return object.equals(element);
    }
}
