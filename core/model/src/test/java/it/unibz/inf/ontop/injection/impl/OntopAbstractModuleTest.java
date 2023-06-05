package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class OntopAbstractModuleTest {

    /**
     * Tests {{@link OntopAbstractModule#getSettingsProperty(Key)}} for different types of {@link Key}.
     */
    @Test
    public void testGetSettingsProperty() {

        // Consider Key(Type, ...) where Type is from following <"expected_property_prefix", Type> map
        Map<String, Type> types = ImmutableMap.of(
                "java.lang.String", String.class,
                "java.util.List<java.lang.String>", Types.newParameterizedType(List.class, String.class),
                "it.unibz.inf.ontop.injection.impl.OntopAbstractModuleTest.Container<java.lang.String>", Types
                        .newParameterizedTypeWithOwner(OntopAbstractModuleTest.class, Container.class, String.class)
        );

        // Consider Key(..., Qualifier) where Qualifier is from following <"expected_property_suffix", Qualifier> map
        Map<String, Object> qualifiers = ImmutableMap.of(
                "-name", Names.named("name"),
                "@it.unibz.inf.ontop.injection.impl.OntopAbstractModuleTest.MarkerAnnotation", getMarkerAnnotation(),
                "@it.unibz.inf.ontop.injection.impl.OntopAbstractModuleTest.NonMarkerAnnotation(42,value)", getNonMarkerAnnotation()
        );

        // Test on all <Type, Qualifier> pairs from above maps, trying both Key(Type) alone and Key(Type, Qualifier),
        // output should be "expected_property_prefix" + "expected_property_suffix"
        OntopAbstractModule module = create();
        for (Entry<String, Type> te : types.entrySet()) {
            Assert.assertEquals(te.getKey(), module.getSettingsProperty(Key.get(te.getValue())));
            for (Entry<String, Object> qe : qualifiers.entrySet()) {
                @SuppressWarnings("unchecked")
                Key<?> key = qe.getValue() instanceof Annotation
                        ? Key.get(te.getValue(), (Annotation) qe.getValue())
                        : Key.get(te.getValue(), (Class<? extends Annotation>) qe.getValue());
                Assert.assertEquals(te.getKey() + qe.getKey(), module.getSettingsProperty(key));
            }
        }
    }

    /**
     * Helper method instantiating an {@link OntopAbstractModule} anonymous subclass with the given custom settings.
     *
     * @param keyValueProperties vararg array of key, value pairs
     * @return the instantiated module
     */
    private static OntopAbstractModule create(String... keyValueProperties) {
        var properties = new Properties();
        for (int i = 0; i < keyValueProperties.length; i += 2) {
            properties.setProperty(keyValueProperties[i], keyValueProperties[i + 1]);
        }
        var settings = new OntopModelSettingsImpl(properties);
        return new OntopAbstractModule(settings) {
        };
    }

    /**
     * Helper method returning an instance of {@link MarkerAnnotation} for testing.
     *
     * @return the annotation instance
     */
    @MarkerAnnotation
    private static MarkerAnnotation getMarkerAnnotation() {
        try {
            return OntopAbstractModuleTest.class
                    .getDeclaredMethod("getMarkerAnnotation")
                    .getAnnotation(MarkerAnnotation.class);
        } catch (NoSuchMethodException ex) {
            throw new Error(ex);
        }
    }

    /**
     * Helper method returning an instance of {@link NonMarkerAnnotation} for testing.
     *
     * @return the annotation instance
     */
    @NonMarkerAnnotation(stringValue = "value", intValue = 42)
    private static NonMarkerAnnotation getNonMarkerAnnotation() {
        try {
            return OntopAbstractModuleTest.class
                    .getDeclaredMethod("getNonMarkerAnnotation")
                    .getAnnotation(NonMarkerAnnotation.class);
        } catch (NoSuchMethodException ex) {
            throw new Error(ex);
        }
    }

    /**
     * Example of 'marker' annotation, that is, annotation without attributes.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @BindingAnnotation
    @interface MarkerAnnotation {

    }

    /**
     * Example of 'non-marker' annotation, that is, annotation with attributes.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @BindingAnnotation
    @interface NonMarkerAnnotation {

        String stringValue();

        int intValue();

    }

    /**
     * Example of generic nested abstract type.
     *
     * @param <T>
     */
    @SuppressWarnings("unused")
    private interface Container<T> {

    }

}
