package it.unibz.inf.ontop.datalog;


import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DatalogFactory {

    CQIE getCQIE(Function head, Function... body);

    CQIE getCQIE(Function head, List<Function> body);


    DatalogProgram getDatalogProgram();

    DatalogProgram getDatalogProgram(MutableQueryModifiers modifiers);

    	/* SPARQL meta-predicates */

    Function getSPARQLJoin(Function t1, Function t2);

    Function getSPARQLJoin(Function t1, Function t2, Function joinCondition);

    /**
     * Follows the ugly encoding of complex left-join expressions (with a filter on the left side)
     */
    Function getSPARQLLeftJoin(List<Function> atoms, List<Function> atoms2, Optional<Function> optionalCondition);

    Function getSPARQLLeftJoin(Term t1, Term t2);

    AlgebraOperatorPredicate getSparqlJoinPredicate();
    AlgebraOperatorPredicate getSparqlLeftJoinPredicate();
    AlgebraOperatorPredicate getSparqlGroupPredicate();
    AlgebraOperatorPredicate getSparqlHavingPredicate();

    AtomPredicate getSubqueryPredicate(String suffix, int arity);

    /**
     * TODO: is suffix necessary?
     */
    AtomPredicate getDummyPredicate(int suffix);
}
