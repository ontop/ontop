package it.unibz.inf.ontop.datalog.impl;

import com.google.inject.Inject;
import fj.data.List;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;


/**
 * Tool methods when manipulate some Datalog programs and their rules.
 */
public class DatalogTools {
    private final TermFactory termFactory;

    private final Expression TRUE_EQ;

    @Inject
    private DatalogTools(TermFactory termFactory) {
        this.termFactory = termFactory;
        DBConstant valueTrue = termFactory.getDBBooleanConstant(true);
        TRUE_EQ = termFactory.getFunctionEQ(valueTrue, valueTrue);
    }

    public Expression foldBooleanConditions(List<Function> booleanAtoms) {
        if (booleanAtoms.length() == 0)
            return TRUE_EQ;

        Expression firstBooleanAtom = convertOrCastIntoBooleanAtom(booleanAtoms.head());
        return booleanAtoms.tail().foldLeft(termFactory::getFunctionAND, firstBooleanAtom);
    }

    private Expression convertOrCastIntoBooleanAtom(Function atom) {
        if (atom instanceof Expression)
            return (Expression) atom;

        Predicate predicate = atom.getFunctionSymbol();
        if (predicate instanceof BooleanFunctionSymbol)
            return termFactory.getExpression((BooleanFunctionSymbol) predicate,
                    atom.getTerms());

        throw new IllegalArgumentException(atom + " is not a boolean atom");
    }

    public Expression foldBooleanConditions(java.util.List<Function> booleanAtoms) {
        return foldBooleanConditions(List.iterableList(booleanAtoms));
    }
}
