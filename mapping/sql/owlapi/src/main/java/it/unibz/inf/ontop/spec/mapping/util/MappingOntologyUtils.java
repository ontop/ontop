package it.unibz.inf.ontop.spec.mapping.util;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.DirectMappingEngine;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.semanticweb.owlapi.model.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Isolated from class {@link DirectMappingEngine} (bootstrap), because
 * the method is used both for bootstrap and R2RML import
 */
public class MappingOntologyUtils {

    public static Set<OWLDeclarationAxiom> extractDeclarationAxioms(OWLOntologyManager manager,
                                                                    Stream<TargetAtom> targetAtoms,
                                                                    boolean bootstrappedMapping) {

        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        return targetAtoms
                .map(ta -> extractEntity(ta, dataFactory, bootstrappedMapping))
                .map(e -> dataFactory.getOWLDeclarationAxiom(e))
                .collect(ImmutableCollectors.toSet());
    }

    private static OWLEntity extractEntity(TargetAtom targetAtom,
                                           OWLDataFactory dataFactory, boolean bootstrappedMapping) {

        ImmutableList<ImmutableTerm> terms = targetAtom.getSubstitutedTerms();
        RDFAtomPredicate predicate = (RDFAtomPredicate) targetAtom.getProjectionAtom().getPredicate();

        Optional<org.apache.commons.rdf.api.IRI> classIRI = predicate.getClassIRI(terms);
        Optional<org.apache.commons.rdf.api.IRI> propertyIRI = predicate.getPropertyIRI(terms);

        if (classIRI.isPresent()) {
            return dataFactory.getOWLClass(IRI.create(classIRI.get().getIRIString()));
        }
        if (!propertyIRI.isPresent()) {
            throw new MinorOntopInternalBugException("No IRI could be extracted from " + targetAtom);
        }

        IRI iri = IRI.create(propertyIRI.get().getIRIString());
        ImmutableTerm objectTerm = predicate.getObject(terms);
        if (objectTerm instanceof ImmutableFunctionalTerm) {
            /*
             * Temporary (later we will use the type of the RDF function)
             */
            Predicate objectFunctionSymbol = ((ImmutableFunctionalTerm) objectTerm).getFunctionSymbol();
            //FIXME
            //            //if (objectFunctionSymbol instanceof DatatypePredicate) {
//                return dataFactory.getOWLDataProperty(iri);
//            }
            return dataFactory.getOWLObjectProperty(iri);
        }
        if (bootstrappedMapping) {
            throw new MinorOntopInternalBugException("A functional term was expected for the object: " + objectTerm);
        }
        return dataFactory.getOWLDataProperty(iri);
    }
}
