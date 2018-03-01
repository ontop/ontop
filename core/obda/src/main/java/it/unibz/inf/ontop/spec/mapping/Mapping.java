package it.unibz.inf.ontop.spec.mapping;


import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomPredicate;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface Mapping {

    MappingMetadata getMetadata();

    @Deprecated
    Optional<IntermediateQuery> getDefinition(AtomPredicate predicate);

    // Optional<IntermediateQuery> getRDFPropertyDefinition(IRI propertyIRI);
    // Optional<IntermediateQuery> getRDFClassDefinition(IRI classIRI);

    @Deprecated
    ImmutableSet<AtomPredicate> getPredicates();

    /**
     * EXCLUDE rdf:type from it?
     */
    // ImmutableSet<IRI> getRDFProperties();
    // ImmutableSet<IRI> getRDFClasses();

    ImmutableCollection<IntermediateQuery> getQueries();

    /**
     * TODO: remove it when the conversion to Datalog will not be needed anymore
     */
    ExecutorRegistry getExecutorRegistry();
}
