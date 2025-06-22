package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.type.impl.AbstractTermTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.AbstractDBNonStrictEqOperator;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.DefaultDBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * Some dialects like SQLServer and Oracle don't support expressions like `<expression> = true`, but they may appear in
 * the generated queries under certain conditions. This normalizer searches such cases and replaces them by just `<expression>`.
 * `<expression> = false` is handled similarly.
 */
public class AvoidEqualsBoolNormalizer implements DialectExtraNormalizer {

    private final IQTreeTransformer transformer;

    @Inject
    protected AvoidEqualsBoolNormalizer(CoreSingletons coreSingletons) {
        this.transformer = new TermTransformer(coreSingletons.getTermFactory())
                .treeTransformer(coreSingletons.getIQFactory(), coreSingletons.getIQTreeTools());
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transformer.transform(tree);
    }

    private static class TermTransformer extends AbstractTermTransformer {
        protected TermTransformer(TermFactory termFactory) {
            super(termFactory);
        }

        @Override
        protected Optional<ImmutableFunctionalTerm> replaceFunctionSymbol(FunctionSymbol functionSymbol, ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            if ((functionSymbol instanceof AbstractDBNonStrictEqOperator) || (functionSymbol instanceof DefaultDBStrictEqFunctionSymbol)) {

                if (newTerms.size() != 2)
                    return Optional.empty();

                var otherTerm = newTerms.stream()
                        .filter(t -> t instanceof ImmutableExpression)
                        .map(t -> (ImmutableExpression) t)
                        .findFirst();

                var constantTerm = newTerms.stream()
                        .filter(t -> t instanceof Constant)
                        .map(t -> (Constant) t)
                        .filter(t -> t.equals(termFactory.getDBBooleanConstant(true))
                                || t.equals(termFactory.getDBBooleanConstant(false)))
                        .findFirst();

                if (otherTerm.isEmpty() || constantTerm.isEmpty())
                    return Optional.empty();

                var requiresTrue = constantTerm.get().equals(termFactory.getDBBooleanConstant(true));
                if (requiresTrue)
                    return Optional.of(otherTerm.get());
                return Optional.of(otherTerm.get().negate(termFactory));
            }
            return Optional.empty();
        }
    }
}
