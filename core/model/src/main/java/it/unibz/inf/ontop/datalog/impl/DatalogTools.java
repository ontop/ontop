package it.unibz.inf.ontop.datalog.impl;

import com.google.inject.Inject;
import fj.data.List;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.DatatypePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.vocabulary.XSD;



/**
 * Tool methods when manipulate some Datalog programs and their rules.
 */
public class DatalogTools {
    private final TermFactory termFactory;

    private final Expression TRUE_EQ;

    @Inject
    private DatalogTools(TermFactory termFactory) {
        this.termFactory = termFactory;
        ValueConstant valueTrue = termFactory.getBooleanConstant(true);
        TRUE_EQ = termFactory.getFunctionEQ(valueTrue, valueTrue);
    }

    /**
     * Folds a list of boolean atoms into one AND(AND(...)) boolean atom.
     */
    public Expression foldBooleanConditions(java.util.List<Function> booleanAtoms) {
        return foldBooleanConditions(List.iterableList(booleanAtoms));
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
        if (predicate instanceof OperationPredicate)
            return termFactory.getExpression((OperationPredicate)predicate, atom.getTerms());

        if (isXsdBoolean(predicate))
            return termFactory.getExpression(ExpressionOperation.IS_TRUE, atom);

        throw new IllegalArgumentException(atom + " is not a boolean atom");
    }


    public static boolean isXsdBoolean(Predicate predicate) {
        return (predicate instanceof DatatypePredicate)
                && ((DatatypePredicate) predicate).getReturnedType().isA(XSD.BOOLEAN);
    }
}
