package it.unibz.inf.ontop.model.impl;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static it.unibz.inf.ontop.model.impl.GroundTermTools.castIntoGroundTerm;
import static it.unibz.inf.ontop.model.impl.GroundTermTools.isGroundTerm;

public class ImmutabilityTools {

    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

    /**
     * In case the term is functional, creates an immutable copy of it.
     */
    public static ImmutableTerm convertIntoImmutableTerm(Term term) {
        if (term instanceof Function) {
            if (term instanceof ImmutableFunctionalTerm) {
                return (ImmutableTerm) term;
            } else if (term instanceof BooleanExpression) {
                BooleanExpression booleanExpression = (BooleanExpression) term;
                return DATA_FACTORY.getImmutableBooleanExpression(booleanExpression);
            }
            else {
                Function functionalTerm = (Function) term;
                return DATA_FACTORY.getImmutableFunctionalTerm(functionalTerm);
            }
        }
        /**
         * Other terms (constant and variable) are immutable.
         */
        return (ImmutableTerm) term;
    }

    public static VariableOrGroundTerm convertIntoVariableOrGroundTerm(Term term) {
        if (term instanceof Variable) {
            return (Variable) term;
        }
        else if (isGroundTerm(term)) {
            return castIntoGroundTerm(term);
        }
        else {
            throw new IllegalArgumentException("Not a variable nor a ground term: " + term);
        }
    }

    /**
     * This method takes a immutable term and convert it into an old mutable function.
     *
     */
    public static Function convertToMutableFunction(ImmutableFunctionalTerm functionalTerm) {

        Predicate pred= functionalTerm.getFunctionSymbol();
        ImmutableList<Term> otherTerms =  functionalTerm.getTerms();
        List<Term> mutableList = new ArrayList<>();
        Iterator<Term> iterator = otherTerms.iterator();
        while (iterator.hasNext()){

            Term nextTerm = iterator.next();
            if (nextTerm instanceof ImmutableFunctionalTerm ){
                ImmutableFunctionalTerm term2Change = (ImmutableFunctionalTerm) nextTerm;
                Function newTerm = convertToMutableFunction(term2Change);
                mutableList.add(newTerm);
            } else{
                mutableList.add(nextTerm);
            }

        }
        Function mutFunc = DATA_FACTORY.getFunction(pred, mutableList);
        return mutFunc;

    }

    /**
     * This method takes a immutable boolean term and convert it into an old mutable boolean function.
     *
     */
    public static BooleanExpression convertToMutableBooleanExpression(ImmutableBooleanExpression booleanExpression) {

        OperationPredicate pred= (OperationPredicate) booleanExpression.getFunctionSymbol();
        ImmutableList<Term> otherTerms =  booleanExpression.getTerms();
        List<Term> mutableList = new ArrayList<>();

        Iterator<Term> iterator = otherTerms.iterator();
        while ( iterator.hasNext()){

            Term nextTerm = iterator.next();
            if (nextTerm instanceof ImmutableFunctionalTerm ){
                ImmutableFunctionalTerm term2Change = (ImmutableFunctionalTerm) nextTerm;
                Function newTerm = convertToMutableFunction(term2Change);
                mutableList.add(newTerm);
            } else{
                mutableList.add(nextTerm);
            }

        }
        BooleanExpression mutFunc = DATA_FACTORY.getBooleanExpression(pred,mutableList);
        return mutFunc;

    }

    public static Optional<ImmutableBooleanExpression> foldBooleanExpressions(
            ImmutableList<ImmutableBooleanExpression> conjunctionOfExpressions) {
        final int size = conjunctionOfExpressions.size();
        switch (size) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(conjunctionOfExpressions.get(0));
            case 2:
                return Optional.of(DATA_FACTORY.getImmutableBooleanExpression(
                        ExpressionOperation.AND,
                        conjunctionOfExpressions));
            default:
                // Non-final
                ImmutableBooleanExpression cumulativeExpression = DATA_FACTORY.getImmutableBooleanExpression(
                        ExpressionOperation.AND,
                        conjunctionOfExpressions.get(0),
                        conjunctionOfExpressions.get(1));
                for (int i = 2; i < size; i++) {
                    cumulativeExpression =  DATA_FACTORY.getImmutableBooleanExpression(
                            ExpressionOperation.AND,
                            cumulativeExpression,
                            conjunctionOfExpressions.get(i));
                }
                return Optional.of(cumulativeExpression);
        }
    }

    public static Optional<ImmutableBooleanExpression> foldBooleanExpressions(
           ImmutableBooleanExpression... conjunctionOfExpressions) {
        return foldBooleanExpressions(ImmutableList.copyOf(conjunctionOfExpressions));
    }

}
