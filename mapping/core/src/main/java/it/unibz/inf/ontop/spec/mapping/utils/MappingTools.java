package it.unibz.inf.ontop.spec.mapping.utils;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.function.Function;

/**
 * To deal with the extraction of the predicates IRI from the MappingAssertion
 * and the distinction between predicates and classes
 */
public class MappingTools {

    public static RDFPredicateInfo extractRDFPredicate(IQ mappingAssertion) {
        DistinctVariableOnlyDataAtom projectionAtom = mappingAssertion.getProjectionAtom();
        RDFAtomPredicate rdfAtomPredicate = Optional.of(projectionAtom.getPredicate())
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> (RDFAtomPredicate) p)
                .orElseThrow(() -> new MappingPredicateIRIExtractionException("The following mapping assertion " +
                        "is not having a RDFAtomPredicate: " + mappingAssertion));

        ImmutableSet<ImmutableList<? extends ImmutableTerm>> possibleSubstitutedArguments
                = mappingAssertion.getTree().getPossibleVariableDefinitions().stream()
                .map(s -> s.apply(projectionAtom.getArguments()))
                .collect(ImmutableCollectors.toSet());

        IRI propertyIRI = extractIRI(possibleSubstitutedArguments, rdfAtomPredicate::getPropertyIRI);

        return propertyIRI.equals(RDF.TYPE)
                ? new RDFPredicateInfo(true, extractIRI(possibleSubstitutedArguments, rdfAtomPredicate::getClassIRI))
                : new RDFPredicateInfo(false, propertyIRI);
    }

    private static IRI extractIRI(ImmutableSet<ImmutableList<? extends ImmutableTerm>> possibleSubstitutedArguments,
                                  Function<ImmutableList<? extends ImmutableTerm>, Optional<IRI>> iriExtractor) {
        ImmutableList<Optional<IRI>> possibleIris = possibleSubstitutedArguments.stream()
                .map(iriExtractor)
                .distinct()
                .collect(ImmutableCollectors.toList());

        if (!possibleIris.stream().allMatch(Optional::isPresent))
            throw new MappingPredicateIRIExtractionException("The definition of the predicate is not always a ground term");

        if (possibleIris.size() != 1)
            throw new MappingPredicateIRIExtractionException("The definition of the predicate is not unique: " + possibleIris + " from " + possibleSubstitutedArguments);

        return possibleIris.stream()
                .map(Optional::get)
                .findFirst()
                .get();
    }


    private static class MappingPredicateIRIExtractionException extends OntopInternalBugException {

        private MappingPredicateIRIExtractionException(String message) {
            super("Internal bug: " + message);
        }
    }

    public static class RDFPredicateInfo {
        private final boolean isClass;
        private final IRI iri;

        public RDFPredicateInfo(boolean isClass, IRI iri) {
            this.isClass = isClass;
            this.iri = iri;
        }

        public boolean isClass() {
            return isClass;
        }

        public IRI getIri() {
            return iri;
        }

        @Override
        public int hashCode() { return iri.hashCode(); }

        @Override
        public boolean equals(Object o) {
            if (o instanceof RDFPredicateInfo) {
                RDFPredicateInfo other = (RDFPredicateInfo)o;
                return iri.equals(other.iri) && isClass == other.isClass;
            }
            return false;
        }

        @Override
        public String toString() { return (isClass ? "C/" : "P/") + iri; }
    }


}
