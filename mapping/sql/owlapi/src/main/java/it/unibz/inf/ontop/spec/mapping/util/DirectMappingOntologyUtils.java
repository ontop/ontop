package it.unibz.inf.ontop.spec.mapping.util;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.bootstrap.engines.impl.DirectMappingEngine;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.semanticweb.owlapi.model.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Isolated from class {@link DirectMappingEngine} (bootstrap), because
 * the method is used both for bootstrap and R2RML import
 */
public class DirectMappingOntologyUtils {

    /** Transform each targetAtom in the stream into an OWL axiom.
     * (E.g., into an axiom such as "Declaration(Class(<http://meraka/moss/exampleBooks.owl#Author>))")
     */
    public static Set<OWLDeclarationAxiom> extractDeclarationAxioms(OWLOntologyManager manager,
                                                                    Stream<TargetAtom> targetAtoms,
                                                                    TypeFactory typeFactory,
                                                                    boolean bootstrappedMapping) {

        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        Set<OWLDeclarationAxiom> result = targetAtoms
                .map(ta -> extractEntity(ta, dataFactory, typeFactory, bootstrappedMapping))
                .map(dataFactory::getOWLDeclarationAxiom)
                .collect(ImmutableCollectors.toSet());
        return result;
    }

    private static OWLEntity extractEntity(TargetAtom targetAtom, OWLDataFactory dataFactory,
                                           TypeFactory typeFactory, boolean bootstrappedMapping) {

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
            ImmutableFunctionalTerm objectFunctionalTerm = (ImmutableFunctionalTerm) objectTerm;

            TermType termType = objectFunctionalTerm.inferType()
                    .flatMap(TermTypeInference::getTermType)
                    .filter(t -> t.isA(typeFactory.getAbstractRDFTermType()))
                    .orElseThrow(() -> new MinorOntopInternalBugException(
                            "Could not infer the RDF type of " + objectFunctionalTerm));

            return (termType.isA(typeFactory.getAbstractRDFSLiteral()))
                    ? dataFactory.getOWLDataProperty(iri)
                    : dataFactory.getOWLObjectProperty(iri);
        }
        if (bootstrappedMapping) {
            throw new MinorOntopInternalBugException("A functional term was expected for the object: " + objectTerm);
        }
        return dataFactory.getOWLDataProperty(iri);
    }
}
