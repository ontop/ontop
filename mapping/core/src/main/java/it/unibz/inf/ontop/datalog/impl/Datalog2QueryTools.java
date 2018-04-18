package it.unibz.inf.ontop.datalog.impl;


import it.unibz.inf.ontop.datalog.AlgebraOperatorPredicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
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

    public static boolean isURIRDFType(Term term) {
        if (term instanceof Function) {
            Function func = (Function) term;
            if (func.getArity() == 1 && (func.getFunctionSymbol() instanceof URITemplatePredicate)) {
                Term t0 = func.getTerm(0);
                if (t0 instanceof IRIConstant)
                    return ((IRIConstant) t0).getIRI().equals(RDF.TYPE);
                // UGLY!! TODO: remove it
                else if (t0 instanceof ValueConstant)
                    return ((ValueConstant) t0).getValue().equals(RDF.TYPE.getIRIString());
            }
        }
        return false;
    }


}
