package it.unibz.inf.ontop.generation.normalization.impl;

import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OracleExtraNormalizer implements DialectExtraNormalizer {

    private final DialectExtraNormalizer orderByNormalizer;
    private final DialectExtraNormalizer expressionWrapper;
    private final ConvertValuesToUnionNormalizer toUnionNormalizer;
    private final UnquoteFlattenResultsNormalizer unquoteFlattenResultsNormalizer;
    private final DialectExtraNormalizer avoidEqualsBoolNormalizer;

    @Inject
    protected OracleExtraNormalizer(OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer orderByNormalizer,
                                    WrapProjectedOrOrderByExpressionNormalizer expressionWrapper,
                                    ConvertValuesToUnionNormalizer toUnionNormalizer,
                                    UnquoteFlattenResultsNormalizer unquoteFlattenResultsNormalizer,
                                    AvoidEqualsBoolNormalizer avoidEqualsBoolNormalizer) {
        this.orderByNormalizer = orderByNormalizer;
        this.expressionWrapper = expressionWrapper;
        this.toUnionNormalizer = toUnionNormalizer;
        this.unquoteFlattenResultsNormalizer = unquoteFlattenResultsNormalizer;
        this.avoidEqualsBoolNormalizer = avoidEqualsBoolNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return avoidEqualsBoolNormalizer.transform(
            unquoteFlattenResultsNormalizer.transform(
                toUnionNormalizer.transform(
                    orderByNormalizer.transform(
                            expressionWrapper.transform(tree, variableGenerator),
                            variableGenerator),
                    variableGenerator),
                variableGenerator),
            variableGenerator);
    }
}
