package it.unibz.inf.ontop.iq.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.type.PartiallyTypedSimpleCastTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.inject.Inject;
import java.util.Optional;

public class PartiallyTypedSimpleCastTransformerImpl implements PartiallyTypedSimpleCastTransformer {

    private final IQTreeTransformer expressionTransformer;

    @Inject
    protected PartiallyTypedSimpleCastTransformerImpl(IntermediateQueryFactory iqFactory,
                                                      TermFactory termFactory,
                                                      SingleTermTypeExtractor typeExtractor) {
        this.expressionTransformer = new ExpressionTransformer(iqFactory, termFactory, typeExtractor)
                .treeTransformer();
    }

    @Override
    public IQTree transform(IQTree tree) {
        return expressionTransformer.transform(tree);
    }

    private static class ExpressionTransformer extends AbstractTypedTermTransformer {
        ExpressionTransformer(IntermediateQueryFactory iqFactory, TermFactory termFactory, SingleTermTypeExtractor typeExtractor) {
            super(iqFactory, termFactory, typeExtractor);
        }

        @Override
        protected Optional<ImmutableFunctionalTerm> replaceFunctionSymbol(FunctionSymbol functionSymbol,
                                                                ImmutableList<ImmutableTerm> newTerms, IQTree tree) {

            if (functionSymbol instanceof DBTypeConversionFunctionSymbol) {
                DBTypeConversionFunctionSymbol conversionFunctionSymbol = (DBTypeConversionFunctionSymbol) functionSymbol;
                if (conversionFunctionSymbol.isSimple()
                        && (!conversionFunctionSymbol.isTemporary())
                        && (!conversionFunctionSymbol.getInputType().isPresent())
                        && conversionFunctionSymbol.getArity() == 1
                        // Temporary HACK (preventing TIMESTAMPTZ to DATE to be considered as injective)
                        // TODO: refactor the approach around "simple" casts
                        && conversionFunctionSymbol.getTargetType().getCategory() != DBTermType.Category.DATE) {

                    ImmutableTerm subTerm = newTerms.get(0);
                    Optional<DBTermType> inputType = getDBTermType(subTerm, tree);
                    return inputType
                            .map(t -> termFactory.getDBCastFunctionalTerm(
                                    t,
                                    conversionFunctionSymbol.getTargetType(),
                                    subTerm));
                }
            }
            return Optional.empty();
        }
    }
}
