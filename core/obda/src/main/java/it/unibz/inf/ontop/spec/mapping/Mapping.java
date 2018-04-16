package it.unibz.inf.ontop.spec.mapping;


import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface Mapping {

    MappingMetadata getMetadata();

    Optional<IQ> getRDFPropertyDefinition(IRI propertyIRI);
    Optional<IQ> getRDFClassDefinition(IRI classIRI);

    /**
     * EXCLUDE rdf:type from it?
     */
     ImmutableSet<IRI> getRDFProperties();
     ImmutableSet<IRI> getRDFClasses();

    ImmutableCollection<IQ> getQueries();

    /**
     * TODO: remove it when the conversion to Datalog will not be needed anymore
     */
    ExecutorRegistry getExecutorRegistry();
}
