package it.unibz.inf.ontop.iq.type.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.NotYetTypedEqualityFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.iq.type.NotYetTypedEqualityTransformer;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Stream;

public class NotYetTypedEqualityTransformerImpl implements NotYetTypedEqualityTransformer {

    private final IQTreeTransformer expressionTransformer;

    @Inject
    protected NotYetTypedEqualityTransformerImpl(IntermediateQueryFactory iqFactory,
                                                 SingleTermTypeExtractor typeExtractor,
                                                 TermFactory termFactory, IQTreeTools iqTreeTools) {
        this.expressionTransformer = new ExpressionTransformer(typeExtractor, termFactory)
                .treeTransformer(iqFactory, iqTreeTools);
    }

    @Override
    public IQTree transform(IQTree tree) {
        return expressionTransformer.transform(tree);
    }

    private static class ExpressionTransformer extends AbstractTypedTermTransformer {
        protected ExpressionTransformer(SingleTermTypeExtractor typeExtractor, TermFactory termFactory) {
            super(termFactory, typeExtractor);
        }

        /**
         * NB: It tries to reduce equalities into strict equalities.
         *
         * Essential for integers and strings as these kinds of types are often used to build IRIs.
         */
        @Override
        protected Optional<ImmutableFunctionalTerm> replaceFunctionSymbol(FunctionSymbol functionSymbol,
                                                                ImmutableList<ImmutableTerm> newTerms, IQTree tree) {
            if (functionSymbol instanceof NotYetTypedEqualityFunctionSymbol) {
                if (newTerms.size() != 2)
                    throw new MinorOntopInternalBugException("Was expecting the not yet typed equalities to be binary");

                ImmutableTerm term1 = newTerms.get(0);
                ImmutableTerm term2 = newTerms.get(1);

                Optional<DBTermType> optionalType1 = getDBTermType(term1, tree);
                Optional<DBTermType> optionalType2 = getDBTermType(term2, tree);

                if (optionalType1.isPresent() && optionalType2.isPresent()) {
                    DBTermType type1 = optionalType1.get();
                    DBTermType type2 = optionalType2.get();

                    return Optional.of(type1.equals(type2)
                            ? transformSameTypeEquality(type1, term1, term2, tree)
                            : transformDifferentTypesEquality(type1, type2, term1, term2));
                }
                return Optional.of(termFactory.getDBNonStrictDefaultEquality(term1, term2));
            }
            return Optional.empty();
        }

        private ImmutableExpression transformSameTypeEquality(DBTermType type, ImmutableTerm term1, ImmutableTerm term2,
                                                              IQTree tree) {
            if (type.areEqualitiesStrict())
                return termFactory.getStrictEquality(term1, term2);

            if (areIndependentFromConstants(term1, term2, tree) && type.areEqualitiesBetweenTwoDBAttributesStrict()) {
                return termFactory.getStrictEquality(term1, term2);
            }

            /*
             * Tries to reuse an existing typed non-strict equality
             */
            switch (type.getCategory()) {
                case DECIMAL:
                case FLOAT_DOUBLE:
                    return termFactory.getDBNonStrictNumericEquality(term1, term2);
                case DATETIME:
                    return termFactory.getDBNonStrictDatetimeEquality(term1, term2);
                default:
                    return termFactory.getDBNonStrictDefaultEquality(term1, term2);
            }
        }

        private ImmutableExpression transformDifferentTypesEquality(DBTermType type1, DBTermType type2,
                                                                      ImmutableTerm term1, ImmutableTerm term2) {
            /*
             * If not type declares that the equality cannot be reduced to a strict equality
             */
            if (areCompatibleForStrictEq(type1, type2)) {
                return termFactory.getStrictEquality(term1, term2);
            }

            /*
             * Tries to reuse an existing typed non-strict equality
             */
            DBTermType.Category category1 = type1.getCategory();
            DBTermType.Category category2 = type2.getCategory();

            switch (category1) {
                case STRING:
                case INTEGER:
                    switch (category2) {
                        case DECIMAL:
                        case FLOAT_DOUBLE:
                            return termFactory.getDBNonStrictNumericEquality(term1, term2);
                        default:
                            break;
                    }
                    break;
                case DECIMAL:
                case FLOAT_DOUBLE:
                    switch (category2) {
                        case INTEGER:
                        case DECIMAL:
                        case FLOAT_DOUBLE:
                            return termFactory.getDBNonStrictNumericEquality(term1, term2);
                        default:
                            break;
                    }
                    break;
                case DATETIME:
                    if (category2 == DBTermType.Category.DATETIME) {
                        return termFactory.getDBNonStrictDatetimeEquality(term1, term2);
                    }
                    break;
                case BOOLEAN:
                case OTHER:
                default:
                    break;
            }
            // By default
            return termFactory.getDBNonStrictDefaultEquality(term1, term2);
        }


        /*
         * TODO: make it robust so as to make sure the term does not depend on a constant introduced in the mapping.
         *
         * Constants in the mapping are indeed uncontrolled and may have a different lexical value
         * that the ones returned by the DB, which would make the test fail.
         */
        private boolean areIndependentFromConstants(ImmutableTerm term1, ImmutableTerm term2, IQTree tree) {
            return !((term1 instanceof DBConstant) || (term2 instanceof DBConstant));
        }

        private boolean areCompatibleForStrictEq(DBTermType type1, DBTermType type2) {
            return Stream.of(type1.areEqualitiesStrict(type2), type2.areEqualitiesStrict(type1))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .reduce((b1, b2) -> b1 && b2)
                    .orElse(false);
        }
    }

}
