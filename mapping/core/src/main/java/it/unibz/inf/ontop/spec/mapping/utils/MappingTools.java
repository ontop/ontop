package it.unibz.inf.ontop.spec.mapping.utils;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.*;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;

import java.util.Optional;

/**
 * To deal with the extraction of the predicates IRI from the MappingAssertion
 * and the distinction between predicates and classes
 */
public class MappingTools {

    private final static RDF rdfFactory = new SimpleRDF();

    public static IRI extractPredicateTerm(IQ mappingAssertion, Variable predicateVariable)  {

        ImmutableTerm predicateTerm = Optional.of(mappingAssertion.getTree().getRootNode())
                .filter(n -> n instanceof ConstructionNode)
                .map((n) -> (ConstructionNode) n)
                .map(ConstructionNode::getSubstitution)
                .flatMap(s -> Optional.ofNullable(s.get(predicateVariable)))
                .filter(s -> s instanceof ImmutableFunctionalTerm)
                .map(s -> ((ImmutableFunctionalTerm)s).getTerm(0))
                .orElseThrow(() -> new TriplePredicateTypeException( mappingAssertion , predicateVariable , "The variable is not defined in the root node (expected for a mapping assertion)"));

        if (predicateTerm instanceof IRIConstant) {
            return ((IRIConstant) predicateTerm).getIRI();
        }
        // TODO: remove this
        else if (predicateTerm instanceof ValueConstant) {
            return rdfFactory.createIRI( ((ValueConstant) predicateTerm).getValue());
        }
        else throw new MappingTools.TriplePredicateTypeException(mappingAssertion , "Predicate is not defined as a constant (expected for a mapping assertion)");
    }

    private static class TriplePredicateTypeException extends OntopInternalBugException {
        TriplePredicateTypeException(IQ mappingAssertion, Variable triplePredicateVariable,
                                     String reason) {
            super("Internal bug: cannot retrieve  " + triplePredicateVariable + " in: \n" + mappingAssertion
                    + "\n Reason: " + reason);
        }

        TriplePredicateTypeException(IQ mappingAssertion, String reason) {
            super("Internal bug: cannot retrieve the predicate IRI in: \n" + mappingAssertion
                    + "\n Reason: " + reason);
        }

    }


    //method to retrieve the class IRI from the mapping assertion.
    public static IRI extractClassIRI(IQ mappingAssertion) {

        ImmutableList<Variable> projectedVariables = mappingAssertion.getProjectionAtom().getArguments();
        if(projectedVariables.size()!=3){
            throw new MappingTools.TriplePredicateTypeException(mappingAssertion , "Expected a projection atom with 3 variables for a mapping assertion)");
        }
        IRI predicateIRI = extractPredicateTerm(mappingAssertion, projectedVariables.get(1));

        if (predicateIRI.equals(it.unibz.inf.ontop.model.vocabulary.RDF.TYPE)) {
            return extractPredicateTerm(mappingAssertion, projectedVariables.get(2));
        }
        throw new MappingTools.TriplePredicateTypeException(mappingAssertion, "Expected a property IRI not a class IRI");

    }

    //method to retrieve the object or data properties IRI from the mapping assertion.
    public static IRI extractPropertiesIRI(IQ mappingAssertion)  {

        ImmutableList<Variable> projectedVariables = mappingAssertion.getProjectionAtom().getArguments();
        if(projectedVariables.size()!=3){
            throw new MappingTools.TriplePredicateTypeException(mappingAssertion , "Expected a projection atom with 3 variables for a mapping assertion)");
        }
        IRI predicateIRI = extractPredicateTerm(mappingAssertion, projectedVariables.get(1));

        if (predicateIRI.equals(it.unibz.inf.ontop.model.vocabulary.RDF.TYPE)) {
            throw new MappingTools.TriplePredicateTypeException(mappingAssertion, "Expected a property IRI not a class IRI");
        }
        return predicateIRI;

    }


}
