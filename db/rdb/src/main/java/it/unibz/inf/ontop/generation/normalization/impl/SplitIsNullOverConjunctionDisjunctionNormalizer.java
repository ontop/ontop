package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.AbstractIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.type.impl.AbstractExpressionTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.term.impl.NonGroundExpressionImpl;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;

/**
Translates expressions of the form `(<expr1> OR|AND <expr2>) IS [NOT] NULL` into the expression
CASE WHEN <expr1> OR|AND <expr2> THEN FALSE[TRUE] WHEN NOT <expr1> OR|AND <expr2> THEN FALSE[TRUE] ELSE FALSE END
for dialects such as Denodo that do not allow IS [NOT] NULL to be executed on conjunctions/disjunctions.
 */
@Singleton
public class SplitIsNullOverConjunctionDisjunctionNormalizer extends DefaultRecursiveIQTreeVisitingTransformer
        implements DialectExtraNormalizer {

    private final AbstractIQTreeVisitingTransformer expressionTransformer;

    @Inject
    protected SplitIsNullOverConjunctionDisjunctionNormalizer(CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory());
        this.expressionTransformer = new ExpressionTransformer(coreSingletons);
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(expressionTransformer);
    }

    protected static class ExpressionTransformer extends AbstractExpressionTransformer {

        protected ExpressionTransformer(CoreSingletons coreSingletons) {
            super(coreSingletons);
        }

        @Override
        protected boolean isFunctionSymbolToReplace(FunctionSymbol functionSymbol) {
            return (functionSymbol instanceof DBIsNullOrNotFunctionSymbol);
        }

        @Override
        protected ImmutableFunctionalTerm replaceFunctionSymbol(FunctionSymbol functionSymbol,
                                                                ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            ImmutableTerm subTerm = newTerms.get(0);
            var isNullOrNotFunctionSymbol = (DBIsNullOrNotFunctionSymbol)functionSymbol;
            if(!(subTerm instanceof NonGroundExpressionImpl))
                return termFactory.getImmutableFunctionalTerm(functionSymbol, newTerms);
            NonGroundExpressionImpl subTermNonGroundExpression = (NonGroundExpressionImpl)subTerm;
            FunctionSymbol subTermFunctionSymbol = subTermNonGroundExpression.getFunctionSymbol();
            if(subTermFunctionSymbol == null || !(subTermFunctionSymbol instanceof DBOrFunctionSymbol || subTermFunctionSymbol instanceof DBAndFunctionSymbol))
                return termFactory.getImmutableFunctionalTerm(functionSymbol, newTerms);
            var whenNotNullTerm = termFactory.getDBBooleanConstant(!isNullOrNotFunctionSymbol.isTrueWhenNull());
            var whenNullTerm = termFactory.getDBBooleanConstant(isNullOrNotFunctionSymbol.isTrueWhenNull());
            var newFunctionSymbol = termFactory.getDBCase(
                    Stream.of(
                            Maps.immutableEntry(subTermNonGroundExpression, whenNotNullTerm),
                            Maps.immutableEntry(termFactory.getDBNot(subTermNonGroundExpression), whenNotNullTerm)
                    ), whenNullTerm, false);
            return newFunctionSymbol;
        }
    }
}
