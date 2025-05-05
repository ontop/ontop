package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.iq.type.impl.AbstractExpressionTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.AbstractDBNonStrictEqOperator;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.DefaultDBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.utils.VariableGenerator;

/**
 * Some dialects like SQLServer and Oracle don't support expressions like `<expression> = true`, but they may appear in
 * the generated queries under certain conditions. This normalizer searches such cases and replaces them by just `<expression>`.
 * `<expression> = false` is handled similarly.
 */
public class AvoidEqualsBoolNormalizer implements DialectExtraNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SingleTermTypeExtractor typeExtractor;

    @Inject
    protected AvoidEqualsBoolNormalizer(IntermediateQueryFactory iqFactory, SingleTermTypeExtractor typeExtractor,
                                        TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.typeExtractor = typeExtractor;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {

        return tree.acceptTransformer(new AbstractExpressionTransformer(iqFactory, typeExtractor, termFactory) {
            @Override
            protected boolean isFunctionSymbolToReplace(FunctionSymbol functionSymbol) {
                return (functionSymbol instanceof AbstractDBNonStrictEqOperator) || (functionSymbol instanceof DefaultDBStrictEqFunctionSymbol);
            }

            @Override
            protected ImmutableFunctionalTerm replaceFunctionSymbol(FunctionSymbol functionSymbol, ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
                if (newTerms.size() != 2)
                    return noReplace(functionSymbol, newTerms);

                var otherTerm = newTerms.stream()
                        .filter(t -> t instanceof ImmutableExpression)
                        .map(t -> (ImmutableExpression) t)
                        .findFirst();

                var constantTerm = newTerms.stream()
                        .filter(t -> t instanceof Constant)
                        .map(t -> (Constant) t)
                        .filter(t -> t.equals(termFactory.getDBBooleanConstant(true)) || t.equals(termFactory.getDBBooleanConstant(false)))
                        .findFirst();

                if (otherTerm.isEmpty() || constantTerm.isEmpty())
                    return noReplace(functionSymbol, newTerms);

                var requiresTrue = constantTerm.get().equals(termFactory.getDBBooleanConstant(true));

                if (requiresTrue)
                    return otherTerm.get();
                return otherTerm.get().negate(termFactory);
            }

            private ImmutableFunctionalTerm noReplace(FunctionSymbol functionSymbol, ImmutableList<ImmutableTerm> newTerms) {
                return termFactory.getImmutableExpression((BooleanFunctionSymbol) functionSymbol, newTerms);
            }
        });
    }
}
