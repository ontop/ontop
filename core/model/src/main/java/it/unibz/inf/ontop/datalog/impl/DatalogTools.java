package it.unibz.inf.ontop.datalog.impl;

import com.google.inject.Inject;
import fj.F;
import fj.F2;
import fj.data.List;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.TypeFactory;


import java.util.ArrayList;


/**
 * Tool methods when manipulate some Datalog programs and their rules.
 */
public class DatalogTools {
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final DatalogFactory datalogFactory;

    private final Expression TRUE_EQ;

    private final static  F<Function, Boolean> IS_DATA_OR_LJ_OR_JOIN_ATOM_FCT = DatalogTools::isDataOrLeftJoinOrJoinAtom;
    private final static  F<Function, Boolean> IS_NOT_DATA_OR_COMPOSITE_ATOM_FCT = atom -> !isDataOrLeftJoinOrJoinAtom(atom);
    private final  F<Function, Boolean> IS_BOOLEAN_ATOM_FCT;

    @Inject
    private DatalogTools(TermFactory termFactory, TypeFactory typeFactory, DatalogFactory datalogFactory) {
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.datalogFactory = datalogFactory;
        TRUE_EQ = termFactory.getFunctionEQ(TermConstants.TRUE, TermConstants.TRUE);
        IS_BOOLEAN_ATOM_FCT = atom -> atom.isOperation() || typeFactory.isBoolean(atom.getFunctionSymbol());
    }

    public static Boolean isDataOrLeftJoinOrJoinAtom(Function atom) {
        return atom.isDataFunction() || isLeftJoinOrJoinAtom(atom);
    }

    public static Boolean isLeftJoinOrJoinAtom(Function atom) {
        Predicate predicate = atom.getFunctionSymbol();
        return predicate.equals(DatalogAlgebraOperatorPredicates.SPARQL_LEFTJOIN) ||
                predicate.equals(DatalogAlgebraOperatorPredicates.SPARQL_JOIN);
    }

    public static List<Function> filterDataAndCompositeAtoms(List<Function> atoms) {
        return atoms.filter(IS_DATA_OR_LJ_OR_JOIN_ATOM_FCT);
    }

    public static List<Function> filterNonDataAndCompositeAtoms(List<Function> atoms) {
        return atoms.filter(IS_NOT_DATA_OR_COMPOSITE_ATOM_FCT);
    }

    public List<Function> filterBooleanAtoms(List<Function> atoms) {
        return atoms.filter(IS_BOOLEAN_ATOM_FCT);
    }

    /**
     * Folds a list of boolean atoms into one AND(AND(...)) boolean atom.
     */
    public Expression foldBooleanConditions(List<Function> booleanAtoms) {
        if (booleanAtoms.length() == 0)
            return TRUE_EQ;

        Expression firstBooleanAtom = convertOrCastIntoBooleanAtom( booleanAtoms.head());

        return booleanAtoms.tail().foldLeft(new F2<Expression, Function, Expression>() {
            @Override
            public Expression f(Expression previousAtom, Function currentAtom) {
                return termFactory.getFunctionAND(previousAtom, currentAtom);
            }
        }, firstBooleanAtom);
    }

    private Expression convertOrCastIntoBooleanAtom(Function atom) {
        if (atom instanceof Expression)
            return (Expression) atom;

        Predicate predicate = atom.getFunctionSymbol();
        if (predicate instanceof OperationPredicate)
            return termFactory.getExpression((OperationPredicate)predicate,
                    atom.getTerms());
        // XSD:BOOLEAN case
        else if (typeFactory.isBoolean(predicate)) {
            return termFactory.getExpression(ExpressionOperation.IS_TRUE, atom);
        }

        throw new IllegalArgumentException(atom + " is not a boolean atom");
    }

    public Expression foldBooleanConditions(java.util.List<Function> booleanAtoms) {
        return foldBooleanConditions(List.iterableList(booleanAtoms));
    }

    /**
     * Folds a list of data/composite atoms and joining conditions into a JOIN(...) with a 3-arity.
     *
     */
    public Function foldJoin(List<Function> dataOrCompositeAtoms, Function joiningCondition) {
        int length = dataOrCompositeAtoms.length();
        if (length < 2) {
            throw new IllegalArgumentException("At least two atoms should be given");
        }

        Function firstAtom = dataOrCompositeAtoms.head();
        Function secondAtom = foldJoin(dataOrCompositeAtoms.tail());

        return datalogFactory.getSPARQLJoin(firstAtom, secondAtom, joiningCondition);
    }

    /**
     * Folds a list of data/composite atoms into a JOIN (if necessary)
     * by adding EQ(t,t) as a joining condition.
     */
    private Function foldJoin(List<Function> dataOrCompositeAtoms) {
        int length = dataOrCompositeAtoms.length();
        if (length == 1)
            return dataOrCompositeAtoms.head();
        else if (length == 0)
            throw new IllegalArgumentException("At least one atom should be given.");
        else {
            Function firstAtom = dataOrCompositeAtoms.head();
            /**
             * Folds the data/composite atom list into a JOIN(JOIN(...)) meta-atom.
             */
            return dataOrCompositeAtoms.tail().foldLeft(new F2<Function, Function, Function>() {
                @Override
                public Function f(Function firstAtom, Function secondAtom) {
                    return datalogFactory.getSPARQLJoin(firstAtom, secondAtom, TRUE_EQ);
                }
            }, firstAtom);
        }
    }

    /**
     * Practical criterion for detecting a real join: having more data/composite atoms.
     *
     * May produces some false negatives for crazy abusive nested joins of boolean atoms (using JOIN instead of AND).
     */
    public static boolean isRealJoin(List<Function> subAtoms) {
        //TODO: reuse a shared static filter fct object.
        List<Function> dataAndCompositeAtoms = subAtoms.filter(new F<Function, Boolean>() {
            @Override
            public Boolean f(Function atom) {
                return isDataOrLeftJoinOrJoinAtom(atom);
            }
        });

        return dataAndCompositeAtoms.length() > 1;
    }

    public Function constructNewFunction(List<Function> subAtoms, Predicate functionSymbol) {
        List<Term> subTerms = (List<Term>)(List<?>) subAtoms;
        return constructNewFunction(functionSymbol, subTerms);
    }

    public Function constructNewFunction(Predicate functionSymbol, List<Term> subTerms) {
        return termFactory.getFunction(functionSymbol, new ArrayList<>(subTerms.toCollection()));
    }
}
