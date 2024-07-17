package it.unibz.inf.ontop.spec.mapping;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.db.ObjectStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

/**
 * TODO: explain
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

    Optional<IQ> getCompatibleDefinitions(RDFAtomPredicate rdfAtomPredicate,
                                          RDFAtomIndexPattern RDFAtomIndexPattern,
                                          ObjectStringTemplateFunctionSymbol templateFunctionSymbol,
                                          VariableGenerator variableGenerator);

    Optional<IQ> getMergedDefinitions(RDFAtomPredicate rdfAtomPredicate);

    Optional<IQ> getMergedClassDefinitions(RDFAtomPredicate rdfAtomPredicate);

    /**
     * For generic triple/quad patterns like ?s ?p ?o, ?s a ?c, etc.
     */
    enum RDFAtomIndexPattern {
        /**
         * Subject of ?s ?p ?o or ?s ?p ?o ?g
         */
        SUBJECT_OF_ALL_DEFINITIONS(0),
        /**
         * Object of ?s ?p ?o or ?s ?p ?o ?g
         */
        OBJECT_OF_ALL_DEFINITIONS(2),
        /**
         * Subject of ?s a ?c or ?s a ?c ?g
         */
        SUBJECT_OF_ALL_CLASSES(0);

        private final int position;

        RDFAtomIndexPattern(int position) {
            this.position = position;
        }

        /**
         * Starts with 0
         */
        public int getPosition() {
            return position;
        }
    }
}