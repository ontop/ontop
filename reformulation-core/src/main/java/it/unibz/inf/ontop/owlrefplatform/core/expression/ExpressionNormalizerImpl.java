package it.unibz.inf.ontop.owlrefplatform.core.expression;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


public class ExpressionNormalizerImpl implements ExpressionNormalizer {

    private final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();


    @Override
    public ImmutableExpression normalize(ImmutableExpression expression) {

        OperationPredicate functionSymbol = expression.getFunctionSymbol();

        if (functionSymbol instanceof ExpressionOperation) {
            switch((ExpressionOperation)functionSymbol) {
                case ADD:
                case MULTIPLY:
                //case AND:
                //case OR:
                case EQ:
                case NEQ:
                    return normalizeCommutative(functionSymbol, expression.getArguments());
                default:
                    return normalizeArguments(functionSymbol, expression.getArguments());
            }
        }
        else {
            return expression;
        }
    }

    private ImmutableExpression normalizeArguments(OperationPredicate functionSymbol,
                                                   ImmutableList<? extends ImmutableTerm> arguments) {
        return DATA_FACTORY.getImmutableExpression(
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

    private ImmutableExpression normalizeCommutative(OperationPredicate functionSymbol,
                                                     ImmutableList<? extends ImmutableTerm> arguments) {
        return DATA_FACTORY.getImmutableExpression(functionSymbol, sortArguments(arguments));
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
            int functionSymbolComparison = f1.getFunctionSymbol().hashCode() - f2.getFunctionSymbol().hashCode();
            if (functionSymbolComparison != 0)
                return functionSymbolComparison;

            ImmutableList<? extends ImmutableTerm> arguments1 = f1.getArguments();
            ImmutableList<? extends ImmutableTerm> arguments2 = f2.getArguments();

            int arityDifference = arguments2.size() - arguments1.size();
            if (arityDifference != 0)
                return arityDifference;
            for(int i=0; i < arguments1.size(); i++) {
                /**
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
