package it.unibz.inf.ontop.protege.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.github.jsonldjava.shaded.com.google.common.base.Joiner;
import com.github.jsonldjava.shaded.com.google.common.base.Preconditions;
import com.github.jsonldjava.shaded.com.google.common.base.Throwables;
import com.github.jsonldjava.shaded.com.google.common.collect.ImmutableMap;
import com.github.jsonldjava.shaded.com.google.common.collect.Maps;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter class for calling certain OWL API methods involving a conflicting version of Guava.
 * <p>
 * Ontop and Protégé use different versions of Guava (resp. v30+ and v18), which cause clashes in
 * the Ontop Protégé plugin where those versions coexist, one provided by Protégé bundle and the
 * other by Ontop bundle, using different OSGI {@link ClassLoader}s. As the Ontop Protégé plugin
 * bundle is configured to use Ontop's Guava version (v30+), directly calling from the Ontop
 * Protégé plugin any OWL API method that has a Guava class as argument or result will result in a
 * runtime {@link LinkageError}, because the Ontop Protégé plugin compiler sees a version of Guava
 * classes that is different from the one seen at runtime and coming from the Protégé bundle.
 * </p>
 * <p>
 * The Guava version clash problem is overcome by calling OWL API methods involving Guava classes
 * via reflection using this adapter class, which takes care of performing the necessary
 * conversions between method arguments/results of mismatching Guava classes (same class name,
 * different ClassLoader). This adapter class defines methods for wrapping only OWL API methods
 * needed by the Ontop Protégé plugin. There are other unused OWL API methods that need
 * adaptations, and they can be handled here by easily extending this adapter class. The following
 * table reports a comprehensive list of OWL API methods requiring adaptation, along with the
 * corresponding wrapper method definitions to be added to this adapter class (implementation
 * note: besides adding these method definitions to this interface, the developer should also add
 * a corresponding mapping from the wrapper method to the target wrapped method in private map
 * {@code targets}, following instructions in source code comments).
 * </p>
 * <table>
 * <tr>
 * <th>OWL API method requiring adaptation</th>
 * <th>Corresponding adapter method to be defined</th>
 * </tr>
 * <tr>
 * <td>{@link IRI#getRemainder()}</td>
 * <td>{@code Optional<String> getRemainder(IRI self)}</td>
 * </tr>
 * <tr>
 * <td>{@link OWLAnnotationValue#asIRI()}</td>
 * <td>{@code Optional<IRI> asIRI(OWLAnnotationValue self)}</td>
 * </tr>
 * <tr>
 * <td>{@link OWLAnnotationValue#asLiteral()}</td>
 * <td>{@code Optional<OWLLiteral> asLiteral(OWLAnnotationValue self)}</td>
 * </tr>
 * <tr>
 * <td>{@link OWLAnnotationValue#asAnonymousIndividual()}</td>
 * <td>{@code Optional<OWLAnonymousIndividual> asAnonymousIndividual(OWLAnnotationValue self)}</td>
 * </tr>
 * <tr>
 * <td>{@link OWLOntologyID#getOntologyIRI()}</td>
 * <td>{@code Optional<IRI> getOntologyIRI(final OWLOntologyID self)}</td>
 * </tr>
 * <tr>
 * <td>{@link OWLOntologyID#getVersionIRI()}</td>
 * <td>{@code Optional<IRI> getVersionIRI(OWLOntologyID self)}</td>
 * </tr>
 * <tr>
 * <td>{@link OWLOntologyID#getDefaultDocumentIRI()}</td>
 * <td>{@code Optional<IRI> getDefaultDocumentIRI(OWLOntologyID self)}</td>
 * </tr>
 * <tr>
 * <td>{@link EntitySearcher#getDataPropertyValues(OWLIndividual, OWLOntology)}</td>
 * <td>{@code Multimap<OWLDataPropertyExpression, OWLLiteral> getDataPropertyValues(OWLIndividual i,
 * OWLOntology ontology)}</td>
 * </tr>
 * <tr>
 * <td>{@link EntitySearcher#getDataPropertyValues(OWLIndividual, Iterable)}</td>
 * <td>{@code Multimap<OWLDataPropertyExpression, OWLLiteral> getDataPropertyValues(OWLIndividual i,
 * Iterable<OWLOntology> ontologies)}</td>
 * </tr>
 * <tr>
 * <td>{@link EntitySearcher#getObjectPropertyValues(OWLIndividual, OWLOntology)}</td>
 * <td>{@code Multimap<OWLObjectPropertyExpression, OWLIndividual> getObjectPropertyValues(OWLIndividual
 * i, OWLOntology ontology)}</td>
 * </tr>
 * <tr>
 * <td>{@link EntitySearcher#getObjectPropertyValues(OWLIndividual, Iterable)}</td>
 * <td>{@code Multimap<OWLObjectPropertyExpression, OWLIndividual> getObjectPropertyValues(OWLIndividual
 * i, Iterable<OWLOntology> ontologies)}</td>
 * </tr>
 * <tr>
 * <td>{@link EntitySearcher#getNegativeDataPropertyValues(OWLIndividual, OWLOntology)}</td>
 * <td>{@code Multimap<OWLDataPropertyExpression, OWLLiteral> getNegativeDataPropertyValues(OWLIndividual
 * i, OWLOntology ontology)}</td>
 * </tr>
 * <tr>
 * <td>{@link EntitySearcher#getNegativeDataPropertyValues(OWLIndividual, Iterable)}</td>
 * <td>{@code Multimap<OWLDataPropertyExpression, OWLLiteral> getNegativeDataPropertyValues(OWLIndividual
 * i, Iterable<OWLOntology> ontologies)}</td>
 * </tr>
 * <tr>
 * <td>{@link EntitySearcher#getNegativeObjectPropertyValues(OWLIndividual, OWLOntology)}</td>
 * <td>{@code Multimap<OWLObjectPropertyExpression, OWLIndividual>
 * getNegativeObjectPropertyValues(OWLIndividual i, OWLOntology ontology)}</td>
 * </tr>
 * <tr>
 * <td>{@link EntitySearcher#getNegativeObjectPropertyValues(OWLIndividual, Iterable)}</td>
 * <td>{@code Multimap<OWLObjectPropertyExpression, OWLIndividual>
 * getNegativeObjectPropertyValues(OWLIndividual i, Iterable<OWLOntology> ontologies)}</td>
 * </tr>
 * <tr>
 * <td>{@link OWLObjectTransformer#OWLObjectTransformer(Predicate, Function, OWLDataFactory, Class)}</td>
 * <td>{@code <T> OWLObjectTransformer<T> newOWLObjectTransformer(Predicate<Object> predicate,
 * Function<T, T> transformer, OWLDataFactory df, Class<T> witness)}</td>
 * </tr>
 */
public interface OWLAPIAdapter {

    /**
     * Adapter for {@link OWLOntologyID#getOntologyIRI()}.
     */
    Optional<IRI> getOntologyIRI(final OWLOntologyID self);

    // EXTENSION POINT: add here other adapter methods for invoking target instance/static methods
    // or constructors whose arguments or result classes may use different ClassLoaders than the
    // one of the adapter. For each new method, add an entry to Map 'targets' below and (if
    // needed) implement necessary argument/result conversion logic in method 'newConverter'

    /**
     * Default implementation of adapter interface, based on Java {@link Proxy} mechanism.
     */
    final OWLAPIAdapter INSTANCE = (OWLAPIAdapter) Proxy.newProxyInstance( //
            OWLAPIAdapter.class.getClassLoader(), //
            new Class<?>[] { OWLAPIAdapter.class }, //
            new InvocationHandler() {

                /**
                 * Logger object, mainly useful for debugging.
                 */
                private final Logger logger = LoggerFactory.getLogger(OWLAPIAdapter.class);

                /**
                 * Mappings from adapter method names to target methods/constructors to invoke.
                 * Each entry here should match a method in the adapter interface.
                 */
                private final Map<String, Executable> targets = ImmutableMap
                        .<String, Executable>builder() //
                        .put("getOntologyIRI", resolve(OWLOntologyID.class, "getOntologyIRI")) //
                        // EXTENSION POINT: add here mappings for further adapter methods
                        .build();

                /**
                 * Method handlers, created at first access and then cached.
                 */
                private final Map<Method, BiFunction<Object, Object[], Object>> handlers = Maps
                        .newConcurrentMap();

                /**
                 * {@inheritDoc} Implements the call to an adapter interface method by delegating
                 * to the corresponding target method, implementing the necessary argument /
                 * result conversions.
                 */
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args)
                        throws Throwable {

                    // Delegate to handler associated to invoked method, creating it if needed
                    return this.handlers
                            .computeIfAbsent(method,
                                    m -> newHandler(m, this.targets.get(m.getName()))) //
                            .apply(null, args);
                }

                /**
                 * Resolves the public executable member (method or constructor) exposed by the
                 * specified class and matching the specified name (null if a constructor) and
                 * argument classes. Note that argument classes are matched by name, ignoring the
                 * ClassLoader defining them.
                 *
                 * @param clazz
                 *            the class exposing the executable member
                 * @param name
                 *            the name of the executable member, in case it is a method
                 * @param argClasses
                 *            the classes of the arguments accepted by the executable member
                 * @return the resolved executable member
                 * @throws Error
                 *             if a matching executable member cannot be resolved
                 */
                private Executable resolve(final Class<?> clazz, @Nullable final String name,
                        final Class<?>... argClasses) {

                    // Check all public constructors or methods
                    for (final Executable e : name == null ? clazz.getConstructors()
                            : clazz.getMethods()) {

                        // Skip candidate member if argument count does not match
                        if (e.getParameterCount() != argClasses.length) {
                            continue;
                        }

                        // Skip candidate member if name does not match
                        if (name != null && !name.equals(e.getName())) {
                            continue;
                        }

                        // Skip candidate member if one argument class does not match (name wise)
                        for (int i = 0; i < argClasses.length; ++i) {
                            if (!e.getParameterTypes()[i].getName()
                                    .equals(argClasses[i].getName())) {
                                continue;
                            }
                        }

                        // Otherwise, return resolved executable member
                        return e;
                    }

                    // Should not happen
                    throw new Error("Could not resolve member " + clazz.getName() + "."
                            + (name != null ? name : "<constructor>") + "("
                            + Joiner.on(", ").join(argClasses) + ")");
                }

                /**
                 * Calls the executable member (a method or constructor) on the {@code self}
                 * object specified (null if not needed) using the arguments supplied.
                 *
                 * @param executable
                 *            the executable to call
                 * @param self
                 *            the object on which to call the executable, in case it is an
                 *            instance method
                 * @param args
                 *            the arguments to supply to the executable
                 * @return the result of the call
                 */
                private Object call(final Executable executable, @Nullable final Object self,
                        final Object... args) {

                    try {
                        // Log executable call
                        if (this.logger.isDebugEnabled()) {
                            final StringBuilder sb = new StringBuilder();
                            sb.append("Calling ").append(executable);
                            if (self != null) {
                                sb.append(" on ").append(self.getClass().getName()).append('@')
                                        .append(System.identityHashCode(self));
                            }
                            sb.append(" with ").append(args.length).append(" arguments (");
                            Joiner.on(", ").appendTo(sb, args);
                            sb.append(")");
                            this.logger.debug(sb.toString());
                        }

                        // Handle constructors and methods calls separately
                        if (executable instanceof Constructor<?>) {
                            return ((Constructor<?>) executable).newInstance(args);
                        } else {
                            return ((Method) executable).invoke(self, args);
                        }

                    } catch (final InvocationTargetException ex) {
                        // Unwrap the exception and propagate as runtime exception
                        Throwables.throwIfUnchecked(ex.getCause());
                        throw new RuntimeException(ex.getCause());

                    } catch (IllegalAccessException | InstantiationException ex) {
                        // Should not happen
                        throw new Error(ex);
                    }
                }

                /**
                 * Defines a 'handler' function that implements the supplied {@code source} method
                 * in terms of calling the supplied {@code target} executable member (method or
                 * constructor), performing the necessary argument and result conversions among
                 * classes having the same names but different ClassLoaders.
                 *
                 * @param source
                 *            the source method to implement in the handler
                 * @param target
                 *            the target executable (method or constructor) to call as part of the
                 *            source implementation
                 * @return the handler function
                 */
                private BiFunction<Object, Object[], Object> newHandler(final Method source,
                        final Executable target) {

                    // Log handler creation
                    this.logger.debug("Creating handler implementing {} by calling {}", source,
                            target);

                    // Determine # args to supply to target and their offset in source args
                    final int argsCount = target.getParameterCount();
                    final int argsOffset = source.getParameterCount() > argsCount ? 1 : 0;

                    // Define a converter function to transform source args into target args
                    final Function<Object[], Object[]> argsConverter;
                    if (argsCount == 0) {
                        final Object[] emptyArgs = new Object[] {};
                        argsConverter = args -> emptyArgs;
                    } else {
                        @SuppressWarnings("unchecked")
                        final Function<Object, Object>[] converters = new Function[argsCount];
                        for (int i = 0; i < argsCount; ++i) {
                            converters[i] = newConverter(
                                    source.getParameterTypes()[i + argsOffset],
                                    target.getParameterTypes()[i]);
                        }
                        argsConverter = args -> {
                            final Object[] convertedArgs = new Object[argsCount];
                            for (int i = 0; i < argsCount; ++i) {
                                convertedArgs[i] = converters[i].apply(args[i + argsOffset]);
                            }
                            return convertedArgs;
                        };
                    }

                    // Define a converter function to transform target result into source result
                    final Function<Object, Object> resultConverter = target instanceof Method
                            ? newConverter(((Method) target).getReturnType(),
                                    source.getReturnType())
                            : Functions.identity();

                    // Define a 'self object, source args -> source result' combining invocation
                    // of converters and target executable member
                    return (self, args) -> resultConverter.apply( //
                            call(target, argsOffset == 0 ? self : args[0],
                                    argsConverter.apply(args)));
                }

                /**
                 * Defines a 'converter' function from instances of {@code sourceClass} to
                 * instances of {@code targetClass}, these classes having the same name and
                 * compatible method definitions but using different ClassLoaders.
                 *
                 * @param sourceClass
                 *            the source class to convert from
                 * @param targetClass
                 *            the target class to convert to
                 * @return the converter function
                 */
                @SuppressWarnings("unchecked")
                private Function<Object, Object> newConverter(final Class<?> sourceClass,
                        final Class<?> targetClass) {

                    // Retrieve class name, which MUST be the same for source and target classes
                    // (only ClassLoader may change)
                    final String name = sourceClass.getName();
                    Preconditions.checkArgument(name.equals(targetClass.getName()));

                    // Handle various conversion cases:
                    if (targetClass.isAssignableFrom(sourceClass)) {
                        // (1) no conversion needed
                        return Functions.identity();

                    } else if (sourceClass.isInterface() && targetClass.isInterface()) {
                        // (2) interface conversion, handled using handlers + Java proxy mechanism
                        final Map<Method, BiFunction<Object, Object[], Object>> handlers;
                        handlers = Maps.newHashMap();
                        for (final Method method : targetClass.getMethods()) {
                            handlers.put(method, newHandler(method, resolve(sourceClass,
                                    method.getName(), method.getParameterTypes())));
                        }
                        return o -> Proxy.newProxyInstance(targetClass.getClassLoader(),
                                new Class<?>[] { targetClass },
                                (proxy, method, args) -> handlers.get(method).apply(o, args));

                    } else if (name.equals(Optional.class.getName())) {
                        // (3) guava Optional conversion
                        final Executable orNull = resolve(sourceClass, "orNull");
                        final Executable fromNullable = resolve(targetClass, "fromNullable",
                                Object.class);
                        return o -> call(fromNullable, null, call(orNull, o));

                    } else if (name.equals(Multimap.class.getName())
                            || name.equals(ListMultimap.class.getName())
                            || name.equals(LinkedListMultimap.class.getName())) {
                        // (4) guava Multimap conversion
                        final Class<?> targetImplClass;
                        try {
                            targetImplClass = Class.forName(LinkedListMultimap.class.getName(),
                                    true, targetClass.getClassLoader());
                        } catch (final ClassNotFoundException ex) {
                            throw new Error(ex);
                        }
                        final Executable entries = resolve(sourceClass, "entries");
                        final Executable create = resolve(targetImplClass, "create");
                        final Executable put = resolve(targetImplClass, "put", Object.class,
                                Object.class);
                        return o -> {
                            final Object result = call(create, null);
                            for (final Entry<?, ?> e : (Collection<Entry<?, ?>>) call(entries,
                                    o)) {
                                call(put, result, e.getKey(), e.getValue());
                            }
                            return result;
                        };
                    }

                    // EXTENSION POINT: add here other conversion cases as required

                    // Fail reporting unsupported conversion
                    throw new UnsupportedOperationException(
                            "No conversion implemented for class " + name);
                }

            });

}
