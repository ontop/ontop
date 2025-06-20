package it.unibz.inf.ontop.iq.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.type.PartiallyTypedSimpleCastTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.inject.Inject;
import java.util.Optional;

public class PartiallyTypedSimpleCastTransformerImpl implements PartiallyTypedSimpleCastTransformer {

    private final IQVisitor<IQTree> expressionTransformer;

    @Inject
    protected PartiallyTypedSimpleCastTransformerImpl(IntermediateQueryFactory iqFactory,
                                                      SingleTermTypeExtractor typeExtractor,
                                                      TermFactory termFactory,
                                                      IQTreeTools iqTreeTools) {
        this.expressionTransformer = new ExpressionTransformer(iqFactory,
                                                                typeExtractor,
                                                                termFactory, iqTreeTools);
    }

    @Override
    public IQTree transform(IQTree tree) {
        return tree.acceptVisitor(expressionTransformer);
    }


    protected static class ExpressionTransformer extends AbstractTermTransformer {

        protected ExpressionTransformer(IntermediateQueryFactory iqFactory,
                                        SingleTermTypeExtractor typeExtractor,
                                        TermFactory termFactory,
                                        IQTreeTools iqTreeTools) {
            super(iqFactory, typeExtractor, termFactory, iqTreeTools);
        }

        @Override
        protected boolean isFunctionSymbolToReplace(FunctionSymbol functionSymbol) {
            if (!(functionSymbol instanceof DBTypeConversionFunctionSymbol))
                return false;
            DBTypeConversionFunctionSymbol conversionFunctionSymbol = (DBTypeConversionFunctionSymbol) functionSymbol;
            return conversionFunctionSymbol.isSimple()
                    && (!conversionFunctionSymbol.isTemporary())
                    && (!conversionFunctionSymbol.getInputType().isPresent())
                    && conversionFunctionSymbol.getArity() == 1
                    // Temporary HACK (preventing TIMESTAMPTZ to DATE to be considered as injective)
                    // TODO: refactor the approach around "simple" casts
                    && conversionFunctionSymbol.getTargetType().getCategory() != DBTermType.Category.DATE;
        }

        @Override
        protected ImmutableFunctionalTerm replaceFunctionSymbol(FunctionSymbol functionSymbol,
                                                                ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            ImmutableTerm subTerm = newTerms.get(0);
            Optional<DBTermType> inputType = typeExtractor.extractSingleTermType(subTerm, tree)
                    .filter(t -> t instanceof DBTermType)
                    .map(t -> (DBTermType) t);
            return inputType
                    .map(t -> termFactory.getDBCastFunctionalTerm(
                            t,
                            ((DBTypeConversionFunctionSymbol)functionSymbol).getTargetType(),
                            subTerm))
                    .orElseGet(() -> termFactory.getImmutableFunctionalTerm(functionSymbol, newTerms));
        }
    }

}
