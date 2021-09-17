package it.unibz.inf.ontop.protege.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLObjectTransformer;

public final class Adapter {

    private static final Class<?> class_Predicate;

    private static final Class<?> class_Function;

    private static final Method method_Predicate_apply;

    private static final Method method_Function_apply;

    private static final Method method_Optional_orNull;

    private static final Method method_Multimap_entries;

    private static final Method method_IRI_geRemainder;

    private static final Method method_OWLAnnotationValue_asIRI;

    private static final Method method_OWLAnnotationValue_asLiteral;

    private static final Method method_OWLAnnotationValue_asAnonymousIndividual;

    private static final Method method_OWLOntologyID_getOntologyIRI;

    private static final Method method_OWLOntologyID_getVersionIRI;

    private static final Method method_OWLOntologyID_getDefaultDocumentIRI;

    private static final Method method_EntitySearcher_getDataPropertyValues;

    private static final Method method_EntitySearcher_getDataPropertyValues_Iterable;

    private static final Method method_EntitySearcher_getObjectPropertyValues;

    private static final Method method_EntitySearcher_getObjectPropertyValues_Iterable;

    private static final Method method_EntitySearcher_getNegativeDataPropertyValues;

    private static final Method method_EntitySearcher_getNegativeDataPropertyValues_Iterable;

    private static final Method method_EntitySearcher_getNegativeObjectPropertyValues;

    private static final Method method_EntitySearcher_getNegativeObjectPropertyValues_Iterable;

    private static final Constructor<?> constructor_OWLObjectTransformer;

    static {
        try {
            method_IRI_geRemainder = IRI.class.getMethod("getRemainder");

            method_OWLAnnotationValue_asIRI = OWLAnnotationValue.class.getMethod("asIRI");
            method_OWLAnnotationValue_asLiteral = OWLAnnotationValue.class.getMethod("asLiteral");
            method_OWLAnnotationValue_asAnonymousIndividual = OWLAnnotationValue.class
                    .getMethod("asAnonymousIndividual");

            method_OWLOntologyID_getOntologyIRI = OWLOntologyID.class.getMethod("getOntologyIRI");
            method_OWLOntologyID_getVersionIRI = OWLOntologyID.class.getMethod("getVersionIRI");
            method_OWLOntologyID_getDefaultDocumentIRI = OWLOntologyID.class
                    .getMethod("getDefaultDocumentIRI");

            method_EntitySearcher_getDataPropertyValues = EntitySearcher.class
                    .getMethod("getDataPropertyValues", OWLIndividual.class, OWLOntology.class);
            method_EntitySearcher_getDataPropertyValues_Iterable = EntitySearcher.class
                    .getMethod("getDataPropertyValues", OWLIndividual.class, Iterable.class);
            method_EntitySearcher_getObjectPropertyValues = EntitySearcher.class
                    .getMethod("getObjectPropertyValues", OWLIndividual.class, OWLOntology.class);
            method_EntitySearcher_getObjectPropertyValues_Iterable = EntitySearcher.class
                    .getMethod("getObjectPropertyValues", OWLIndividual.class, Iterable.class);
            method_EntitySearcher_getNegativeDataPropertyValues = EntitySearcher.class.getMethod(
                    "getNegativeDataPropertyValues", OWLIndividual.class, OWLOntology.class);
            method_EntitySearcher_getNegativeDataPropertyValues_Iterable = EntitySearcher.class
                    .getMethod("getNegativeDataPropertyValues", OWLIndividual.class,
                            Iterable.class);
            method_EntitySearcher_getNegativeObjectPropertyValues = EntitySearcher.class.getMethod(
                    "getNegativeObjectPropertyValues", OWLIndividual.class, OWLOntology.class);
            method_EntitySearcher_getNegativeObjectPropertyValues_Iterable = EntitySearcher.class
                    .getMethod("getNegativeObjectPropertyValues", OWLIndividual.class,
                            Iterable.class);

            constructor_OWLObjectTransformer = OWLObjectTransformer.class.getConstructors()[0];

            method_Optional_orNull = method_OWLOntologyID_getOntologyIRI.getReturnType()
                    .getMethod("orNull");

            method_Multimap_entries = method_EntitySearcher_getDataPropertyValues.getReturnType()
                    .getMethod("entries");

            class_Predicate = constructor_OWLObjectTransformer.getParameterTypes()[0];
            method_Predicate_apply = class_Predicate.getMethod("apply", Object.class);

            class_Function = constructor_OWLObjectTransformer.getParameterTypes()[1];
            method_Function_apply = class_Function.getMethod("apply", Object.class);

        } catch (final Throwable ex) {
            throw new Error(ex);
        }
    }

    public static Optional<String> getRemainder(final IRI iri) {
        return invokeOptionalMethod(Objects.requireNonNull(iri), method_IRI_geRemainder);
    }

    public static Optional<IRI> asIRI(final OWLOntologyID id) {
        return invokeOptionalMethod(Objects.requireNonNull(id), method_OWLAnnotationValue_asIRI);
    }

    public static Optional<OWLLiteral> asLiteral(final OWLOntologyID id) {
        return invokeOptionalMethod(Objects.requireNonNull(id),
                method_OWLAnnotationValue_asLiteral);
    }

    public static Optional<OWLAnonymousIndividual> asAnonymousIndividual(final OWLOntologyID id) {
        return invokeOptionalMethod(Objects.requireNonNull(id),
                method_OWLAnnotationValue_asAnonymousIndividual);
    }

    public static Optional<IRI> getOntologyIRI(final OWLOntologyID id) {
        return invokeOptionalMethod(Objects.requireNonNull(id),
                method_OWLOntologyID_getOntologyIRI);
    }

    public static Optional<IRI> getVersionIRI(final OWLOntologyID id) {
        return invokeOptionalMethod(Objects.requireNonNull(id),
                method_OWLOntologyID_getVersionIRI);
    }

    public static Optional<IRI> getDefaultDocumentIRI(final OWLOntologyID id) {
        return invokeOptionalMethod(Objects.requireNonNull(id),
                method_OWLOntologyID_getDefaultDocumentIRI);
    }

    public static Multimap<OWLDataPropertyExpression, OWLLiteral> getDataPropertyValues(
            final OWLIndividual i, final OWLOntology ontology) {
        return invokeMultimapMethod(null, method_EntitySearcher_getDataPropertyValues, i,
                ontology);
    }

    public static Multimap<OWLDataPropertyExpression, OWLLiteral> getDataPropertyValues(
            final OWLIndividual i, final Iterable<OWLOntology> ontologies) {
        return invokeMultimapMethod(null, method_EntitySearcher_getDataPropertyValues_Iterable, i,
                ontologies);
    }

    public static Multimap<OWLObjectPropertyExpression, OWLIndividual> getObjectPropertyValues(
            final OWLIndividual i, final OWLOntology ontology) {
        return invokeMultimapMethod(null, method_EntitySearcher_getObjectPropertyValues, i,
                ontology);
    }

    public static Multimap<OWLObjectPropertyExpression, OWLIndividual> getObjectPropertyValues(
            final OWLIndividual i, final Iterable<OWLOntology> ontologies) {
        return invokeMultimapMethod(null, method_EntitySearcher_getObjectPropertyValues_Iterable,
                i, ontologies);
    }

    public static Multimap<OWLDataPropertyExpression, OWLLiteral> getNegativeDataPropertyValues(
            final OWLIndividual i, final OWLOntology ontology) {
        return invokeMultimapMethod(null, method_EntitySearcher_getNegativeDataPropertyValues, i,
                ontology);
    }

    public static Multimap<OWLDataPropertyExpression, OWLLiteral> getNegativeDataPropertyValues(
            final OWLIndividual i, final Iterable<OWLOntology> ontologies) {
        return invokeMultimapMethod(null,
                method_EntitySearcher_getNegativeDataPropertyValues_Iterable, i, ontologies);
    }

    public static Multimap<OWLObjectPropertyExpression, OWLIndividual> getNegativeObjectPropertyValues(
            final OWLIndividual i, final OWLOntology ontology) {
        return invokeMultimapMethod(null, method_EntitySearcher_getNegativeObjectPropertyValues, i,
                ontology);
    }

    public static Multimap<OWLObjectPropertyExpression, OWLIndividual> getNegativeObjectPropertyValues(
            final OWLIndividual i, final Iterable<OWLOntology> ontologies) {
        return invokeMultimapMethod(null,
                method_EntitySearcher_getNegativeObjectPropertyValues_Iterable, i, ontologies);
    }

    public static <T> OWLObjectTransformer<T> newOWLObjectTransformer(
            final Predicate<Object> predicate, final Function<T, T> transformer,
            final OWLDataFactory df, final Class<T> witness) {
        return invokeConstructor(constructor_OWLObjectTransformer, convertPredicate(predicate),
                convertFunction(transformer), df, witness);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object convertPredicate(final Predicate<?> predicate) {
        return Proxy.newProxyInstance(class_Predicate.getClassLoader(),
                new Class<?>[] { class_Predicate }, (proxy, method, args) -> {
                    if (method.equals(method_Predicate_apply)) {
                        return ((Predicate) predicate).apply(args[0]);
                    } else {
                        return method.invoke(predicate, args); // toString, equals, hashCode, ...
                    }
                });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object convertFunction(final Function<?, ?> function) {
        return Proxy.newProxyInstance(class_Function.getClassLoader(),
                new Class<?>[] { class_Function }, (proxy, method, args) -> {
                    if (method.equals(method_Function_apply)) {
                        return ((Function) function).apply(args[0]);
                    } else {
                        return method.invoke(function, args); // toString, equals, hashCode, ...
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Multimap<K, V> invokeMultimapMethod(@Nullable final Object object,
            final Method method, final Object... parameters) {
        try {
            final Object multimap = method.invoke(object, parameters);
            if (multimap == null) {
                return null;
            }
            final Multimap<K, V> result = LinkedListMultimap.create();
            for (final Entry<K, V> entry : (Collection<Entry<K, V>>) method_Multimap_entries
                    .invoke(multimap)) {
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        } catch (final Throwable ex) {
            throw new Error(ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <T> Optional<T> invokeOptionalMethod(final Object object, final Method method) {
        try {
            final Object optional = method.invoke(object);
            return optional != null
                    ? Optional.fromNullable((T) method_Optional_orNull.invoke(optional))
                    : null;
        } catch (final Throwable ex) {
            throw new Error(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokeConstructor(final Constructor<?> constructor,
            final Object... parameters) {
        try {
            return (T) constructor.newInstance(parameters);
        } catch (final Throwable ex) {
            throw new Error(ex);
        }
    }

    private Adapter() {
        throw new Error();
    }

}
