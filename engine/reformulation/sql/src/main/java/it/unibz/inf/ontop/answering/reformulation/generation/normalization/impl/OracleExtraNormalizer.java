package it.unibz.inf.ontop.answering.reformulation.generation.normalization.impl;

import it.unibz.inf.ontop.answering.reformulation.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OracleExtraNormalizer implements DialectExtraNormalizer {

    private final DialectExtraNormalizer orderByNormalizer;
    private final DialectExtraNormalizer expressionWrapper;

    @Inject
    protected OracleExtraNormalizer(OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer orderByNormalizer,
                                    WrapProjectedOrOrderByExpressionNormalizer expressionWrapper) {
        this.orderByNormalizer = orderByNormalizer;
        this.expressionWrapper = expressionWrapper;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return orderByNormalizer.transform(
                expressionWrapper.transform(tree, variableGenerator),
                variableGenerator);
    }
}
