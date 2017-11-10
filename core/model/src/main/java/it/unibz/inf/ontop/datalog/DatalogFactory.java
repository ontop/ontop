package it.unibz.inf.ontop.datalog;


import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DatalogFactory {

    public CQIE getCQIE(Function head, Function... body);

    public CQIE getCQIE(Function head, List<Function> body);

    public CQIE getFreshCQIECopy(CQIE rule);


    public DatalogProgram getDatalogProgram();

    public DatalogProgram getDatalogProgram(MutableQueryModifiers modifiers);

    public DatalogProgram getDatalogProgram(MutableQueryModifiers modifiers, Collection<CQIE> rules);

    	/* SPARQL meta-predicates */

    public Function getSPARQLJoin(Function t1, Function t2);

    public Function getSPARQLJoin(Function t1, Function t2, Function joinCondition);

    /**
     * Follows the ugly encoding of complex left-join expressions (with a filter on the left side)
     */
    public Function getSPARQLLeftJoin(List<Function> atoms, List<Function> atoms2, Optional<Function> optionalCondition);

    public Function getSPARQLLeftJoin(Term t1, Term t2);
}
