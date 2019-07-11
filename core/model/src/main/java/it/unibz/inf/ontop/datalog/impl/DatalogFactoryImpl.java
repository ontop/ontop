package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.stream.IntStream;


@Singleton
public class DatalogFactoryImpl implements DatalogFactory {

    private static final String SUBQUERY_PRED_PREFIX = "ontopSubquery";
    private final AlgebraOperatorPredicate sparqlJoinPredicate;
    private final AlgebraOperatorPredicate sparqlLeftjoinPredicate;
    private final AlgebraOperatorPredicate sparqlGroupPredicate;
    private final AlgebraOperatorPredicate sparqlHavingPredicate;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;

    @Inject
    private DatalogFactoryImpl(TypeFactory typeFactory, TermFactory termFactory) {
        sparqlJoinPredicate = new AlgebraOperatorPredicateImpl("Join");
        sparqlLeftjoinPredicate = new AlgebraOperatorPredicateImpl("LeftJoin");
        sparqlGroupPredicate = new AlgebraOperatorPredicateImpl("Group");
        sparqlHavingPredicate = new AlgebraOperatorPredicateImpl("Having");
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
    }


    @Override
    public CQIE getCQIE(Function head, Function... body) {
        return new CQIEImpl(head, body);
    }

    @Override
    public CQIE getCQIE(Function head, List<Function> body) {
        return new CQIEImpl(head, body);
    }

    @Override
    public DatalogProgram getDatalogProgram() {
        return new DatalogProgramImpl();
    }

    @Override
    public DatalogProgram getDatalogProgram(MutableQueryModifiers modifiers) {
        DatalogProgram p = new DatalogProgramImpl(modifiers);
        return p;
    }

    @Override
    public DatalogProgram getDatalogProgram(MutableQueryModifiers modifiers, Collection<CQIE> rules) {
        DatalogProgram p = new DatalogProgramImpl(modifiers);
        p.appendRule(rules);
        return p;
    }

    @Override
    public Function getSPARQLJoin(Function t1, Function t2) {
        return termFactory.getFunction(sparqlJoinPredicate, t1, t2);
    }

    @Override
    public Function getSPARQLJoin(Function t1, Function t2, Function joinCondition) {
        return termFactory.getFunction(sparqlJoinPredicate, t1, t2, joinCondition);
    }


    @Override
    public Function getSPARQLLeftJoin(List<Function> leftAtoms, List<Function> rightAtoms,
                                      Optional<Function> optionalCondition){

        if (leftAtoms.isEmpty() || rightAtoms.isEmpty()) {
            throw new IllegalArgumentException("Atoms on the left and right sides are required");
        }

        List<Term> joinTerms = new ArrayList<>(leftAtoms);

        joinTerms.addAll(rightAtoms);

        /**
         * The joining condition goes with the right part
         */
        optionalCondition.ifPresent(joinTerms::add);

        return termFactory.getFunction(sparqlLeftjoinPredicate, joinTerms);
    }

    @Override
    public Function getSPARQLLeftJoin(Term t1, Term t2) {
        return termFactory.getFunction(sparqlLeftjoinPredicate, t1, t2);
    }

    @Override
    public AlgebraOperatorPredicate getSparqlJoinPredicate() {
        return sparqlJoinPredicate;
    }

    @Override
    public AlgebraOperatorPredicate getSparqlLeftJoinPredicate() {
        return sparqlLeftjoinPredicate;
    }

    @Override
    public AlgebraOperatorPredicate getSparqlGroupPredicate() {
        return sparqlGroupPredicate;
    }

    @Override
    public AlgebraOperatorPredicate getSparqlHavingPredicate() {
        return sparqlHavingPredicate;
    }

    @Override
    public AtomPredicate getSubqueryPredicate(String suffix, int arity) {
        return new DatalogAtomPredicate(SUBQUERY_PRED_PREFIX + suffix, arity, typeFactory);
    }

    @Override
    public AtomPredicate getDummyPredicate(int suffix) {
        return new DatalogAtomPredicate("dummy" + suffix, 0, typeFactory);
    }



    /**
     * Used for intermediate datalog rules
     */
    private static class DatalogAtomPredicate extends AtomPredicateImpl {

        private DatalogAtomPredicate(String name, int arity, TypeFactory typeFactory) {
            super(name, createExpectedBaseTypes(arity, typeFactory));
        }

        private static ImmutableList<TermType> createExpectedBaseTypes( int arity, TypeFactory typeFactory) {
            TermType rootType = typeFactory.getAbstractAtomicTermType();
            return IntStream.range(0, arity)
                    .boxed()
                    .map(i -> rootType)
                    .collect(ImmutableCollectors.toList());
        }
    }
}
