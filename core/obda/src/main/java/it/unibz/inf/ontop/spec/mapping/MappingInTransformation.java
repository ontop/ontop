package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public interface MappingInTransformation {

    Mapping getMapping();

    Optional<IQ> getRDFPropertyDefinition(RDFAtomPredicate rdfAtomPredicate, IRI propertyIRI);
    Optional<IQ> getRDFClassDefinition(RDFAtomPredicate rdfAtomPredicate, IRI classIRI);

    /**
     * Properties used to define triples, quads, etc.
     *
     * Does NOT contain rdf:type
     */
    ImmutableSet<IRI> getRDFProperties(RDFAtomPredicate rdfAtomPredicate);

    /**
     * Classes used to define triples, quads, etc.
     */
    ImmutableSet<IRI> getRDFClasses(RDFAtomPredicate rdfAtomPredicate);

    ImmutableSet<Table.Cell<RDFAtomPredicate, IRI, IQ>> getRDFPropertyQueries();
    ImmutableSet<Table.Cell<RDFAtomPredicate, IRI, IQ>> getRDFClassQueries();

    ImmutableCollection<IQ> getQueries(RDFAtomPredicate rdfAtomPredicate);

    /**
     * TriplePredicate, QuadPredicate, etc.
     */
    ImmutableSet<RDFAtomPredicate> getRDFAtomPredicates();

    /**
     * Inserts (and overwrite if necessary) a mapping definition for a pair (IRI, RDFAtomPredicate)
     *
     * Returns a new (immutable) Mapping
     */
    MappingInTransformation update(ImmutableTable<RDFAtomPredicate, IRI, IQ> propertyUpdateMap,
                                   ImmutableTable<RDFAtomPredicate, IRI, IQ> classUpdateMap);
}
