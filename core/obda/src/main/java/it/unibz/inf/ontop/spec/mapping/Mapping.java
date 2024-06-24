package it.unibz.inf.ontop.spec.mapping;


import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

/**
 * TODO: explain
 *
 * For more complex indexing schemes (and a richer set of methods), feel free to create your own interface/class
 * for your specific needs (e.g. advanced query unfolding)
 *
 * Immutable
 *
 * See SpecificationFactory for creating a new instance.
 *
 */
public interface Mapping {
    /**
     * rdfAtomPredicate indicates if it is a triple, a quad (or something else)
     */
    Optional<IQ> getRDFPropertyDefinition(RDFAtomPredicate rdfAtomPredicate, IRI propertyIRI);

    Optional<IQ> getRDFClassDefinition(RDFAtomPredicate rdfAtomPredicate, IRI classIRI);

    /**
     * TriplePredicate, QuadPredicate, etc.
     */
    ImmutableSet<RDFAtomPredicate> getRDFAtomPredicates();

    /**
     * Properties used to define triples, quads, etc.
     * <p>
     * Does NOT contain rdf:type
     */
    ImmutableSet<IRI> getRDFProperties(RDFAtomPredicate rdfAtomPredicate);

    /**
     * Classes used to define triples, quads, etc.
     */
    ImmutableSet<IRI> getRDFClasses(RDFAtomPredicate rdfAtomPredicate);

    ImmutableCollection<IQ> getQueries(RDFAtomPredicate rdfAtomPredicate);

    Optional<IQ> getCompatibleDefinitionsFromIRITemplate(RDFAtomPredicate rdfAtomPredicate, ObjectStringTemplateFunctionSymbol template);

    void computeCompatibleDefinitions(RDFAtomPredicate rdfAtomPredicate);
}