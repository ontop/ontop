package it.unibz.inf.ontop.spec.mapping.pp.impl;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

/**
 * To deal with the extraction of the predicates IRI from the MappingAssertion
 * and the distinction between predicates and classes
 */
public class MappingTools {

    @Deprecated
    static MappingAssertionIndex extractRDFPredicate(IQTree iq, DistinctVariableOnlyDataAtom projectionAtom) {
        RDFAtomPredicate rdfAtomPredicate = Optional.of(projectionAtom.getPredicate())
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> (RDFAtomPredicate) p)
                .orElseThrow(() -> new MappingPredicateIRIExtractionException("The mapping assertion does not have an RDFAtomPredicate"));

        ConstructionNode node = (ConstructionNode)(((UnaryIQTree)iq).getRootNode());

        ImmutableList<? extends ImmutableTerm> substitutedArguments =
                node.getSubstitution().apply(projectionAtom.getArguments());

        IRI propertyIRI = rdfAtomPredicate.getPropertyIRI(substitutedArguments)
                .orElseThrow(() -> new MappingPredicateIRIExtractionException("The definition of the predicate is not always a ground term"));

        return propertyIRI.equals(RDF.TYPE)
                ? MappingAssertionIndex.ofClass(rdfAtomPredicate, rdfAtomPredicate.getClassIRI(substitutedArguments)
                        .orElseThrow(() -> new MappingPredicateIRIExtractionException("The definition of the predicate is not always a ground term")))
                : MappingAssertionIndex.ofProperty(rdfAtomPredicate, propertyIRI);
    }

    public static class MappingPredicateIRIExtractionException extends OntopInternalBugException {
        private MappingPredicateIRIExtractionException(String message) {
            super("Internal bug: " + message);
        }
    }
}
