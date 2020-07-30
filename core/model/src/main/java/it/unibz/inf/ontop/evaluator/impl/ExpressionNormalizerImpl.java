package it.unibz.inf.ontop.evaluator.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.evaluator.ExpressionNormalizer;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


@Singleton
public class ExpressionNormalizerImpl implements ExpressionNormalizer {

    private final TermFactory termFactory;

    @Inject
    private ExpressionNormalizerImpl(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    @Override
    public ImmutableExpression normalize(ImmutableExpression expression) {

        BooleanFunctionSymbol functionSymbol = expression.getFunctionSymbol();

//        if (functionSymbol instanceof BooleanExpressionOperation) {
//
//            switch((BooleanExpressionOperation)functionSymbol) {
//                //case AND:
//                //case OR:
//                //case EQ:
//                //case NEQ:
//                //    return normalizeCommutative(functionSymbol, expression.getTerms());
//                default:
//                    return normalizeArguments(functionSymbol, expression.getTerms());
//            }
//        }
//        else {
//            return expression;
//        }
        return expression;
    }

    private ImmutableExpression normalizeArguments(BooleanFunctionSymbol functionSymbol,
                                                   ImmutableList<? extends ImmutableTerm> arguments) {
        return termFactory.getImmutableExpression(
                functionSymbol,
                arguments.stream()
                    .map(this::normalizeArgument)
                    .collect(ImmutableCollectors.toList()));
    }

    private ImmutableTerm normalizeArgument(ImmutableTerm immutableTerm) {
        if (immutableTerm instanceof ImmutableExpression) {
            return normalize((ImmutableExpression) immutableTerm);
        }
        else {
            return immutableTerm;
        }
    }

    private ImmutableExpression normalizeCommutative(BooleanFunctionSymbol functionSymbol,
                                                     ImmutableList<? extends ImmutableTerm> arguments) {
        return termFactory.getImmutableExpression(functionSymbol, sortArguments(arguments));
    }

    private ImmutableList<? extends ImmutableTerm> sortArguments(ImmutableList<? extends ImmutableTerm> arguments) {
        return arguments.stream()
                .sorted(ExpressionNormalizerImpl::compareArguments)
                .collect(ImmutableCollectors.toList());
    }

    private static int compareArguments(ImmutableTerm a1, ImmutableTerm a2) {
        if (a1 instanceof ImmutableFunctionalTerm) {
            return compareFunctionalTerm((ImmutableFunctionalTerm) a1, a2);
        }
        else if (a1 instanceof Variable) {
            return compareVariable((Variable)a1, a2);
        }
        else if (a1 instanceof Constant) {
            return compareConstant((Constant) a1, a2);
        }
        else {
            throw new IllegalStateException("Unexpected term:" + a1);
        }
    }

    private static int compareConstant(Constant c1, ImmutableTerm a2) {
        if (a2 instanceof Constant) {
            return c1.getValue().hashCode() - ((Constant)a2).getValue().hashCode();
        }
        else {
            return -1 * compareArguments(a2, c1);
        }
    }

    private static int compareVariable(Variable v1, ImmutableTerm a2) {
        if (a2 instanceof Variable)
            // TODO: should we normalize?
            return v1.getName().hashCode() - ((Variable)a2).getName().hashCode();
        else if (a2 instanceof Constant) {
            return -1;
        }
        else if (a2 instanceof ImmutableFunctionalTerm) {
            return 1;
        }
        else {
            throw new IllegalStateException("Unexpected term:" + a2);
        }
    }

    private static int compareFunctionalTerm(ImmutableFunctionalTerm f1, ImmutableTerm a2) {
        if (a2 instanceof Constant)
            return -2;
        else if (a2 instanceof Variable)
            return -1;
        else if (a2 instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm f2 = (ImmutableFunctionalTerm) a2;

            /*
             * Non-ground first (seems to be required by the current ExpressionEvaluator...)
             */
            if (f1.isGround() && (!f2.isGround()))
                return 1;
            else if ((!f1.isGround()) && f2.isGround())
                return -1;

            int functionSymbolComparison = f1.getFunctionSymbol().hashCode() - f2.getFunctionSymbol().hashCode();
            if (functionSymbolComparison != 0)
                return functionSymbolComparison;

            ImmutableList<? extends ImmutableTerm> arguments1 = f1.getTerms();
            ImmutableList<? extends ImmutableTerm> arguments2 = f2.getTerms();

            int arityDifference = arguments2.size() - arguments1.size();
            if (arityDifference != 0)
                return arityDifference;
            for(int i=0; i < arguments1.size(); i++) {
                /*
                 * Recursive (not tail)
                 */
                int argComparison = compareArguments(arguments1.get(i), arguments2.get(i));
                if (argComparison != 0)
                    return argComparison;
            }
            return 0;
        }
        else {
            throw new IllegalStateException("Unexpected node:" + a2);
        }
    }
}
