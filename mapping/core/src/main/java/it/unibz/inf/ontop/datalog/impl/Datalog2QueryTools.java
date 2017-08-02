package it.unibz.inf.ontop.datalog.impl;


import it.unibz.inf.ontop.datalog.AlgebraOperatorPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;

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
}
