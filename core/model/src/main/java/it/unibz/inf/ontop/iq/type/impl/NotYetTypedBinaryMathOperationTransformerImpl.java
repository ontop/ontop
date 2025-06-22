package it.unibz.inf.ontop.iq.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
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
                                                            TermFactory termFactory,
                                                            IQTreeTools iqTreeTools) {
        this.expressionTransformer = new ExpressionTransformer(iqFactory, typeExtractor, termFactory, iqTreeTools)
                .treeTransformer();
    }

    @Override
    public IQTree transform(IQTree tree) {
        return expressionTransformer.transform(tree);
    }


    protected static class ExpressionTransformer extends AbstractTermTransformer {

        protected ExpressionTransformer(IntermediateQueryFactory iqFactory,
                                        SingleTermTypeExtractor typeExtractor,
                                        TermFactory termFactory, IQTreeTools iqTreeTools) {
            super(iqFactory, typeExtractor, termFactory, iqTreeTools);
        }

        @Override
        protected Optional<ImmutableFunctionalTerm> replaceFunctionSymbol(FunctionSymbol functionSymbol,
                                                                ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            if (functionSymbol instanceof DefaultUntypedDBMathBinaryOperator) {
                DefaultUntypedDBMathBinaryOperator operator = (DefaultUntypedDBMathBinaryOperator) functionSymbol;

                if (newTerms.size() != 2)
                    throw new MinorOntopInternalBugException("Was expecting untyped math operations to be binary");

                ImmutableTerm term1 = newTerms.get(0);
                ImmutableTerm term2 = newTerms.get(1);

                Optional<DBTermType> optionalType1 = getDBTermType(term1, tree);
                Optional<DBTermType> optionalType2 = getDBTermType(term2, tree);

                if (optionalType1.isPresent() && optionalType2.isPresent()) {
                    return Optional.of(termFactory.getDBBinaryNumericFunctionalTerm(
                            operator.getMathOperatorString(), optionalType1.get(), optionalType2.get(), term1, term2));
                }
            }
            return Optional.empty();
        }
    }
}
