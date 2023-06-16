package it.unibz.inf.ontop.injection.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import it.unibz.inf.ontop.injection.OntopModelSettings;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class OntopAbstractModule extends AbstractModule {

    /**
     * Interface not found in the settings or impossibility to load the
     * declared implementation class.
     */
    public static class UnknownClassException extends RuntimeException {
        public UnknownClassException(String message) {
            super(message);
        }
    }

    private final OntopModelSettings settings;

    protected OntopAbstractModule(OntopModelSettings settings) {
        this.settings = settings;
    }

    protected final <T> Optional<Class<? extends T>> getImplementation(Key<T> qualifiedAbstractType) {
        return settings.getProperty(getSettingsProperty(qualifiedAbstractType)).map(implementationClassName -> {
            try {
                @SuppressWarnings("unchecked")
                Class<T> abstractClass = (Class<T>) qualifiedAbstractType.getTypeLiteral().getRawType();
                return Class.forName(implementationClassName).asSubclass(abstractClass);
            } catch (ClassNotFoundException e) {
                throw new UnknownClassException(e.getMessage());
            }
        });
    }

    /**
     * Suggest replacing calls to this method with (more compact calls to {@link #buildFactory(Class, Class[])}
     */
    protected final Module buildFactory(Iterable<Class<?>> abstractTypes, Class<?> factoryInterface) {
        return buildFactory(
                Key.get(factoryInterface),
                StreamSupport.stream(abstractTypes.spliterator(), false)
                        .map(Key::get).<Key<?>>toArray(Key[]::new));
    }

    protected final Module buildFactory(Class<?> factoryInterface, Class<?>... abstractTypes) {
        return buildFactory(Key.get(factoryInterface), Stream.of(abstractTypes).map(Key::get).<Key<?>>toArray(Key[]::new));
    }

    protected final Module buildFactory(Key<?> qualifiedFactoryInterface, Key<?>... qualifiedAbstractTypes) {
        FactoryModuleBuilder builder = new FactoryModuleBuilder();

        // Types to be implemented by the factory
        for (Key<?> qualifiedAbstractType : qualifiedAbstractTypes) {
            // getImplementation() is guaranteed to return a subtype (if binding defined in the settings)
            Class<?> implementationClass = getImplementation((Key<?>) qualifiedAbstractType)
                    .orElseThrow(() -> new UnknownClassException(String.format(
                            "Cannot build factory %s: no property %s in the settings for abstract type %s.",
                            qualifiedFactoryInterface.getTypeLiteral().toString(),
                            getSettingsProperty(qualifiedAbstractType),
                            qualifiedAbstractType.getTypeLiteral().toString())));
            //noinspection unchecked,rawtypes
            builder = builder.implement((Key) qualifiedAbstractType, implementationClass);
        }
        return builder.build(qualifiedFactoryInterface);
    }

    /**
     * To be called by sub-classes, inside the {@link #configure()} method.
     */
    protected final void configureCoreConfiguration() {
        bind(OntopModelSettings.class).toInstance(settings);
    }

    protected final OntopModelSettings getSettings() {
        return settings;
    }

    /**
     * Returns the settings property associated to the given Guice key, which can be employed by users to configure the
     * implementation class to use for that key.
     * <p>
     * A Guice key is a {@code <type, qualifier>} pair, where the type is a class or a generic type and the qualifier
     * is either missing, an annotation instance or an annotation type. The method handles all the possible cases
     * producing a property name that is the concatenation of the <i>encoding of the type</i> and of the <i>encoding of
     * the qualifier</i>, where:
     * <ul>
     * <li>the returned property name is the concatenation of <i>type encoding</i> and <i>qualifier encoding</i></li>
     * <li>the <i>type encoding</i> has the form {@code my.package.Class.NestedClass<optional.generic.type.Name>}</li>
     * <li>the <i>qualifier encoding</i> is {@code -name} in the typical case of a {@link Named}{@code ("name")}
     * annotation, otherwise it is {@code @my.package.AnnotationClass.AnnotationNestedClass} possibly followed by
     * {@code (value1,value2,...)} with the (toString) values of annotation attributes, lexicographically sorted</li>
     * </ul>
     * </p>
     *
     * @param qualifiedAbstractType the key to map to a settings property
     * @return the settings property for the key
     */
    protected final String getSettingsProperty(Key<?> qualifiedAbstractType) {

        // The first part of the property is given by the key type, including generics information if present
        Type type = qualifiedAbstractType.getTypeLiteral().getType();
        String typeName = type instanceof Class<?>
                ? ((Class<?>) type).getCanonicalName()
                : type.toString().replace('$', '.'); // convert: pkg.Class$Nested -> pkg.Class.Nested

        // The second part depends on the presence of an annotation/annotation type. Four possible exhaustive cases:
        if (qualifiedAbstractType.getAnnotationType() == null) {
            // (1) Key(type, null) -> return "typeName" (incl. generics info)
            return typeName;

        } else if (!qualifiedAbstractType.hasAttributes()) {
            // (2) Key(type, annotationType) -> return "typeName@annotationTypeName"
            return typeName + "@" + qualifiedAbstractType.getAnnotationType().getCanonicalName();

        } else if (qualifiedAbstractType.getAnnotation() instanceof Named) {
            // (3) Key(type, @Named(name)) -> return "typeName-name"
            // note: need checking only @Named from Guice, as @Named from javax.inject is mapped to the former
            return typeName + "-" + ((Named) qualifiedAbstractType.getAnnotation()).value();

        } else {
            // (4) Key(type, annotation) -> return "typeName@annotationTypeName(args)" (args guaranteed to be present)
            var annotation = qualifiedAbstractType.getAnnotation();
            var annotationType = qualifiedAbstractType.getAnnotationType();
            String sortedCommaSeparatedArgs = Arrays.stream(annotationType.getDeclaredMethods())
                    .sorted(Comparator.comparing(Method::getName))
                    .map(m -> {
                        try {
                            return String.valueOf(m.invoke(annotation));
                        } catch (Throwable ex) {
                            throw new Error("Could not access annotation value", ex); // should not happen
                        }
                    })
                    .collect(Collectors.joining(","));
            return typeName + "@" + annotationType.getCanonicalName() + "(" + sortedCommaSeparatedArgs + ")";
        }
    }

    /**
     * Bind the default implementation mapped to property {@code abstractClass.getCanonicalName()} in the settings.
     * Mainly used to bind classes with default constructors (although guice can deal with other constructors and forms
     * of injection).
     */
    protected final <T> void bindFromSettings(Class<T> abstractType) {
        bindFromSettings(Key.get(abstractType)); // delegate
    }

    protected final <T> void bindFromSettings(Key<T> qualifiedAbstractType) {
        bind(qualifiedAbstractType).to(getImplementation(qualifiedAbstractType)
                .orElseThrow(() -> new UnknownClassException(String.format(
                        "No property %s in the settings for abstract type %s.",
                        getSettingsProperty(qualifiedAbstractType),
                        qualifiedAbstractType.getTypeLiteral().toString()))));
    }

    protected final <T> void bindFromSettings(Key<T> qualifiedAbstractType,
                                              Class<? extends T> defaultImplementationClass) {
        Objects.requireNonNull(defaultImplementationClass);
        bind(qualifiedAbstractType).to(getImplementation(qualifiedAbstractType).orElse(defaultImplementationClass));
    }

}
