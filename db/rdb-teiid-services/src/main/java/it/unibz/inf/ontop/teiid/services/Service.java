package it.unibz.inf.ontop.teiid.services;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.unibz.inf.ontop.teiid.services.util.Signature;
import it.unibz.inf.ontop.teiid.services.util.Tuple;

public interface Service {

    String getName();

    Signature getInputSignature();

    Signature getOutputSignature();

    Iterator<Tuple> invoke(final Tuple tuple);

    List<Iterator<Tuple>> invokeBatch(final Iterable<Tuple> tuples);

    static Service create(final Map<String, ?> properties) {

        // Read the implementation class from procedure option 'impl'
        final Class<?> implClass;
        final Object implClassProp = properties.get("impl");
        if (implClassProp instanceof Class<?>) {
            implClass = (Class<?>) implClassProp;
        } else if (implClassProp != null) {
            try {
                implClass = Class.forName(implClassProp.toString());
            } catch (final Throwable ex) {
                throw new IllegalArgumentException("Cannot resolve class " + implClassProp, ex);
            }
        } else {
            throw new IllegalArgumentException(
                    "Missing option 'impl' indicating the implementation class");
        }

        // Check that the class extends Service
        if (!Service.class.isAssignableFrom(implClass)) {
            throw new IllegalArgumentException(
                    "Implementation " + implClass + " does not extend " + Service.class.getName());
        }

        // Locate the constructor accepting a single Map argument
        final Constructor<?> implConstructor;
        try {
            implConstructor = implClass.getConstructor(Map.class);
        } catch (final Throwable ex) {
            throw new IllegalArgumentException(
                    "Cannot locate suitable constructor for " + implClass, ex);
        }

        // Instantiate the service by delegating to the located constructor
        try {
            return (Service) implConstructor.newInstance(properties);
        } catch (final Throwable ex) {
            throw new IllegalArgumentException("Cannot instantiate " + implClass, ex);
        }
    }

}
