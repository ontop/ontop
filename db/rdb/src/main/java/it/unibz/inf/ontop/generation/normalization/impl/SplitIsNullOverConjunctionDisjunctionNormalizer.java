package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.type.impl.AbstractTermTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.term.impl.NonGroundExpressionImpl;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.stream.Stream;

/**
Translates expressions of the form `(<expr1> OR|AND <expr2>) IS [NOT] NULL` into the expression
CASE WHEN <expr1> OR|AND <expr2> THEN FALSE[TRUE] WHEN NOT <expr1> OR|AND <expr2> THEN FALSE[TRUE] ELSE FALSE END
for dialects such as Denodo that do not allow IS [NOT] NULL to be executed on conjunctions/disjunctions.
 */
@Singleton
public class SplitIsNullOverConjunctionDisjunctionNormalizer implements DialectExtraNormalizer {

    private final IQVisitor<IQTree> expressionTransformer;

    @Inject
    protected SplitIsNullOverConjunctionDisjunctionNormalizer(CoreSingletons coreSingletons) {
        this.expressionTransformer = new ExpressionTransformer(coreSingletons);
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(expressionTransformer);
    }

    protected static class ExpressionTransformer extends AbstractTermTransformer {

        protected ExpressionTransformer(CoreSingletons coreSingletons) {
            super(coreSingletons);
        }

        @Override
        protected Optional<ImmutableFunctionalTerm> replaceFunctionSymbol(FunctionSymbol functionSymbol,
                                                                          ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            if (functionSymbol instanceof DBIsNullOrNotFunctionSymbol) {
                ImmutableTerm subTerm = newTerms.get(0);
                var isNullOrNotFunctionSymbol = (DBIsNullOrNotFunctionSymbol) functionSymbol;
                if (!(subTerm instanceof NonGroundExpressionImpl))
                    return Optional.of(termFactory.getImmutableFunctionalTerm(functionSymbol, newTerms));

                NonGroundExpressionImpl subTermNonGroundExpression = (NonGroundExpressionImpl) subTerm;
                FunctionSymbol subTermFunctionSymbol = subTermNonGroundExpression.getFunctionSymbol();
                if (!(subTermFunctionSymbol instanceof DBOrFunctionSymbol || subTermFunctionSymbol instanceof DBAndFunctionSymbol))
                    return Optional.of(termFactory.getImmutableFunctionalTerm(functionSymbol, newTerms));

                var whenNotNullTerm = termFactory.getDBBooleanConstant(!isNullOrNotFunctionSymbol.isTrueWhenNull());
                var whenNullTerm = termFactory.getDBBooleanConstant(isNullOrNotFunctionSymbol.isTrueWhenNull());
                return Optional.of(termFactory.getDBCase(
                        Stream.of(
                                Maps.immutableEntry(subTermNonGroundExpression, whenNotNullTerm),
                                Maps.immutableEntry(termFactory.getDBNot(subTermNonGroundExpression), whenNotNullTerm)),
                        whenNullTerm, false));
            }
            return Optional.empty();
        }
    }
}
