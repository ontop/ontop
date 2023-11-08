package it.unibz.inf.ontop.generation.normalization.impl;

import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DenodoExtraNormalizer implements DialectExtraNormalizer {

    private final EliminateLimitsFromSubQueriesNormalizer eliminateLimitsFromSubQueriesNormalizer;
    private final SplitIsNullOverConjunctionDisjunctionNormalizer splitIsNullOverConjunctionDisjunctionNormalizer;
    private final AlwaysProjectOrderByTermsNormalizer alwaysProjectOrderByTermsNormalizer;
    private final ConvertValuesToUnionNormalizer toUnionNormalizer;
    private final AlwaysPushProjectedOrderByTermsNormalizer pushProjectedOrderByTermsNormalizer;
    private final SubQueryFromComplexLeftJoinExtraNormalizer complexLeftJoinNormalizer;

    @Inject
    protected DenodoExtraNormalizer(AlwaysProjectOrderByTermsNormalizer alwaysProjectOrderByTermsNormalizer,
                                    ConvertValuesToUnionNormalizer toUnionNormalizer, AlwaysPushProjectedOrderByTermsNormalizer pushProjectedOrderByTermsNormalizer,
                                    SplitIsNullOverConjunctionDisjunctionNormalizer splitIsNullOverConjunctionDisjunctionNormalizer,
                                    EliminateLimitsFromSubQueriesNormalizer eliminateLimitsFromSubQueriesNormalizer,
                                    SubQueryFromComplexLeftJoinExtraNormalizer complexLeftJoinNormalizer) {
        this.alwaysProjectOrderByTermsNormalizer = alwaysProjectOrderByTermsNormalizer;
        this.toUnionNormalizer = toUnionNormalizer;
        this.pushProjectedOrderByTermsNormalizer = pushProjectedOrderByTermsNormalizer;
        this.splitIsNullOverConjunctionDisjunctionNormalizer = splitIsNullOverConjunctionDisjunctionNormalizer;
        this.eliminateLimitsFromSubQueriesNormalizer = eliminateLimitsFromSubQueriesNormalizer;
        this.complexLeftJoinNormalizer = complexLeftJoinNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return eliminateLimitsFromSubQueriesNormalizer.transform(
                splitIsNullOverConjunctionDisjunctionNormalizer.transform(
                        pushProjectedOrderByTermsNormalizer.transform(
                                complexLeftJoinNormalizer.transform(
                                        toUnionNormalizer.transform(
                                                alwaysProjectOrderByTermsNormalizer.transform(tree, variableGenerator),
                                                variableGenerator),
                                        variableGenerator),
                                variableGenerator),
                        variableGenerator),
                variableGenerator);
    }

}

