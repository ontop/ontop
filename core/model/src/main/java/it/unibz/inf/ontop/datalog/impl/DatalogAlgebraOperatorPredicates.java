package it.unibz.inf.ontop.datalog.impl;


import it.unibz.inf.ontop.datalog.AlgebraOperatorPredicate;

public class DatalogAlgebraOperatorPredicates {

    public static final AlgebraOperatorPredicate SPARQL_JOIN = new AlgebraOperatorPredicateImpl("Join");
    public static final AlgebraOperatorPredicate SPARQL_LEFTJOIN = new AlgebraOperatorPredicateImpl("LeftJoin");
    public static final AlgebraOperatorPredicate SPARQL_GROUP = new AlgebraOperatorPredicateImpl("Group");
    public static final AlgebraOperatorPredicate SPARQL_HAVING = new AlgebraOperatorPredicateImpl("Having");
}
