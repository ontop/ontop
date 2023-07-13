package it.unibz.inf.ontop.iq.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.type.NotYetTypedBinaryMathOperationTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.DefaultUntypedDBMathBinaryOperator;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.inject.Inject;
import java.util.Optional;

public class NotYetTypedBinaryMathOperationTransformerImpl implements NotYetTypedBinaryMathOperationTransformer {

    private final IQTreeTransformer expressionTransformer;

    @Inject
    protected NotYetTypedBinaryMathOperationTransformerImpl(IntermediateQueryFactory iqFactory,
                                                            SingleTermTypeExtractor typeExtractor,
                                                            TermFactory termFactory) {
        this.expressionTransformer = new ExpressionTransformer(iqFactory,
                                                                typeExtractor,
                                                                termFactory);
    }

    @Override
    public IQTree transform(IQTree tree) {
        return expressionTransformer.transform(tree);
    }


    protected static class ExpressionTransformer extends AbstractExpressionTransformer {

        protected ExpressionTransformer(IntermediateQueryFactory iqFactory,
                                        SingleTermTypeExtractor typeExtractor,
                                        TermFactory termFactory) {
            super(iqFactory, typeExtractor, termFactory);
        }

        @Override
        protected boolean isFunctionSymbolToReplace(FunctionSymbol functionSymbol) {
            return functionSymbol instanceof DefaultUntypedDBMathBinaryOperator;
        }

        @Override
        protected ImmutableFunctionalTerm replaceFunctionSymbol(FunctionSymbol functionSymbol,
                                                                ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            if (newTerms.size() != 2)
                throw new MinorOntopInternalBugException("Was expecting untyped math operations to be binary");

            DefaultUntypedDBMathBinaryOperator operator = (DefaultUntypedDBMathBinaryOperator)functionSymbol;

            ImmutableTerm term1 = newTerms.get(0);
            ImmutableTerm term2 = newTerms.get(1);

            ImmutableList<Optional<TermType>> extractedTypes = newTerms.stream()
                    .map(t -> typeExtractor.extractSingleTermType(t, tree))
                    .collect(ImmutableCollectors.toList());

            if (extractedTypes.stream()
                    .allMatch(type -> type
                            .filter(t -> t instanceof DBTermType)
                            .isPresent())) {
                ImmutableList<DBTermType> types = extractedTypes.stream()
                        .map(Optional::get)
                        .map(t -> (DBTermType) t)
                        .collect(ImmutableCollectors.toList());

                DBTermType type1 = types.get(0);
                DBTermType type2 = types.get(1);

                return termFactory.getDBBinaryNumericFunctionalTerm(
                        operator.getMathOperatorString(),
                        type1,
                        type2,
                        term1,
                        term2);
            }
            else
                return termFactory.getImmutableFunctionalTerm(functionSymbol, term1, term2);
        }
    }

}
