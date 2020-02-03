package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public interface MappingInTransformation {

    Mapping getMapping();

    Optional<IQ> getRDFPropertyDefinition(RDFAtomPredicate rdfAtomPredicate, IRI propertyIRI);
    Optional<IQ> getRDFClassDefinition(RDFAtomPredicate rdfAtomPredicate, IRI classIRI);

    ImmutableSet<Table.Cell<RDFAtomPredicate, IRI, IQ>> getRDFPropertyQueries();
    ImmutableSet<Table.Cell<RDFAtomPredicate, IRI, IQ>> getRDFClassQueries();

    ImmutableMap<MappingAssertionIndex, IQ> getAssertions();

    /**
     * TriplePredicate, QuadPredicate, etc.
     */
    ImmutableSet<RDFAtomPredicate> getRDFAtomPredicates();

    /**
     * Inserts (and overwrites if necessary) a mapping definition for a pair (IRI, RDFAtomPredicate)
     *
     * Returns a new (immutable) MappingInTransformation
     */
    MappingInTransformation update(ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyUpdateMap,
                                   ImmutableTable<RDFAtomPredicate, IRI, IQ> classUpdateMap);
}
