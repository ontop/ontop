package it.unibz.inf.ontop.datalog.impl;


import it.unibz.inf.ontop.datalog.AlgebraOperatorPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.RDF;

import java.util.stream.Stream;

public class Datalog2QueryTools {

    public static Stream<Predicate> extractPredicates(Function atom) {
        Predicate currentpred = atom.getFunctionSymbol();
        if (currentpred instanceof OperationPredicate)
            return Stream.of();
        else if (currentpred instanceof AlgebraOperatorPredicate) {
            return atom.getTerms().stream()
                    .filter(t -> t instanceof Function)
                    // Recursive
                    .flatMap(t -> extractPredicates((Function) t));
        } else {
            return Stream.of(currentpred);
        }
    }

    /**
     * check if the term is {@code URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")}
     */
    public static boolean isURIRDFType(Term term, TermFactory termFactory, TypeFactory typeFactory) {
        if (term instanceof Function) {
            Function func = (Function) term;
            if (func.getFunctionSymbol() instanceof RDFTermFunctionSymbol) {
                Term lexicalTerm = func.getTerm(0);
                Term typeTerm = func.getTerm(1);
                // If typeTerm is a variable, we are unsure so we return false
                if (typeTerm.equals(termFactory.getRDFTermTypeConstant(typeFactory.getIRITermType()))
                        && (lexicalTerm instanceof RDFLiteralConstant))
                    return ((RDFLiteralConstant) lexicalTerm).getValue().equals(RDF.TYPE.getIRIString());
            }
        }
        else if (term instanceof IRIConstant) {
            return ((IRIConstant) term).getIRI().equals(RDF.TYPE);
        }
        return false;
    }


}
