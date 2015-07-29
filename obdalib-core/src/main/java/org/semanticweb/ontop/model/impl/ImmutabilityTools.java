package org.semanticweb.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ImmutabilityTools {

    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

    /**
     * In case the term is functional, creates an immutable copy of it.
     */
    public static ImmutableTerm convertIntoImmutableTerm(Term term) {
        if (term instanceof Function) {
            if (term instanceof ImmutableFunctionalTerm) {
                return (ImmutableTerm) term;
            } else {
                Function functionalTerm = (Function) term;
                return new ImmutableFunctionalTermImpl(functionalTerm);
            }
        }
        /**
         * Other terms (constant and variable) are immutable.
         */
        return (ImmutableTerm) term;
    }

    public static ImmutableBooleanExpression convertIntoImmutableBooleanExpression(BooleanExpression expression) {
        return new ImmutableBooleanExpressionImpl(expression);
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

        BooleanOperationPredicate pred= (BooleanOperationPredicate) booleanExpression.getFunctionSymbol();
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
}
