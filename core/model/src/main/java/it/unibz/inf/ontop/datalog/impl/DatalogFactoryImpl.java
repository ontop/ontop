package it.unibz.inf.ontop.datalog.impl;

import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.*;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;


public class DatalogFactoryImpl implements DatalogFactory {

    private static final DatalogFactory INSTANCE = new DatalogFactoryImpl();
    private final AlgebraOperatorPredicate sparqlJoinPredicate;
    private final AlgebraOperatorPredicate sparqlLeftjoinPredicate;
    private final AlgebraOperatorPredicate sparqlGroupPredicate;
    private final AlgebraOperatorPredicate sparqlHavingPredicate;

    private DatalogFactoryImpl() {
        TypeFactory typeFactory = TYPE_FACTORY;
        sparqlJoinPredicate = new AlgebraOperatorPredicateImpl("Join", typeFactory);
        sparqlLeftjoinPredicate = new AlgebraOperatorPredicateImpl("LeftJoin", typeFactory);
        sparqlGroupPredicate = new AlgebraOperatorPredicateImpl("Group", typeFactory);
        sparqlHavingPredicate = new AlgebraOperatorPredicateImpl("Having", typeFactory);
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
        DatalogProgram p = new DatalogProgramImpl();
        p.getQueryModifiers().copy(modifiers);
        return p;
    }

    @Override
    public DatalogProgram getDatalogProgram(MutableQueryModifiers modifiers, Collection<CQIE> rules) {
        DatalogProgram p = new DatalogProgramImpl();
        p.appendRule(rules);
        p.getQueryModifiers().copy(modifiers);
        return p;
    }

    @Override
    public Function getSPARQLJoin(Function t1, Function t2) {
        return TERM_FACTORY.getFunction(sparqlJoinPredicate, t1, t2);
    }

    @Override
    public Function getSPARQLJoin(Function t1, Function t2, Function joinCondition) {
        return TERM_FACTORY.getFunction(sparqlJoinPredicate, t1, t2, joinCondition);
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

        return TERM_FACTORY.getFunction(sparqlLeftjoinPredicate, joinTerms);
    }

    @Override
    public Function getSPARQLLeftJoin(Term t1, Term t2) {
        return TERM_FACTORY.getFunction(sparqlLeftjoinPredicate, t1, t2);
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


    /***
     * Replaces each variable 'v' in the query for a new variable constructed
     * using the name of the original variable plus the counter. For example
     *
     * <pre>
     * q(x) :- C(x)
     *
     * results in
     *
     * q(x_1) :- C(x_1)
     *
     * if counter = 1.
     * </pre>
     *
     * <p>
     * This method can be used to generate "fresh" rules from a datalog program
     * so that it can be used during a resolution step.
     * suffix
     *            The integer that will be apended to every variable name
     * @param rule
     * @return
     */
    @Override
    public CQIE getFreshCQIECopy(CQIE rule) {

        int suff = ++suffix;

        // This method doesn't support nested functional terms
        CQIE freshRule = rule.clone();
        Function head = freshRule.getHead();
        List<Term> headTerms = head.getTerms();
        for (int i = 0; i < headTerms.size(); i++) {
            Term term = headTerms.get(i);
            Term newTerm = getFreshTerm(term, suff);
            headTerms.set(i, newTerm);
        }

        List<Function> body = freshRule.getBody();
        for (Function atom : body) {
            List<Term> atomTerms = atom.getTerms();
            for (int i = 0; i < atomTerms.size(); i++) {
                Term term = atomTerms.get(i);
                Term newTerm = getFreshTerm(term, suff);
                atomTerms.set(i, newTerm);
            }
        }
        return freshRule;
    }

    private int suffix = 0;

    private Term getFreshTerm(Term term, int suff) {
        Term newTerm;
        if (term instanceof Variable) {
            Variable variable = (Variable) term;
            newTerm = TERM_FACTORY.getVariable(variable.getName() + "_" + suff);
        }
        else if (term instanceof Function) {
            Function functionalTerm = (Function) term;
            List<Term> innerTerms = functionalTerm.getTerms();
            List<Term> newInnerTerms = new LinkedList<>();
            for (int j = 0; j < innerTerms.size(); j++) {
                Term innerTerm = innerTerms.get(j);
                newInnerTerms.add(getFreshTerm(innerTerm, suff));
            }
            Predicate newFunctionSymbol = functionalTerm.getFunctionSymbol();
            Function newFunctionalTerm = TERM_FACTORY.getFunction(newFunctionSymbol, newInnerTerms);
            newTerm = newFunctionalTerm;
        }
        else if (term instanceof Constant) {
            newTerm = term.clone();
        }
        else {
            throw new RuntimeException("Unsupported term: " + term);
        }
        return newTerm;
    }

    public static DatalogFactory getInstance() {
        return INSTANCE;
    }
}
