package it.unibz.inf.ontop.generation.normalization.impl;

import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SQLServerExtraNormalizer implements DialectExtraNormalizer {

    private final DialectExtraNormalizer projectOrderByTermsNormalizer;
    private final DialectExtraNormalizer projectionWrapper;
    private final DialectExtraNormalizer limitOffsetOldVersionNormalizer;
    private final DialectExtraNormalizer insertOrderByInSlizeNormalizer;

    @Inject
    protected SQLServerExtraNormalizer(AlwaysProjectOrderByTermsNormalizer projectOrderByTermsNormalizer,
                                       WrapProjectedOrOrderByExpressionNormalizer projectionWrapper,
                                       SQLServerLimitOffsetOldVersionNormalizer limitOffsetOldVersionNormalizer,
                                       SQLServerInsertOrderByInSliceNormalizer insertOrderByInSlizeNormalizer) {
        this.projectOrderByTermsNormalizer = projectOrderByTermsNormalizer;
        this.projectionWrapper = projectionWrapper;
        this.limitOffsetOldVersionNormalizer = limitOffsetOldVersionNormalizer;
        this.insertOrderByInSlizeNormalizer = insertOrderByInSlizeNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return insertOrderByInSlizeNormalizer.transform(
                limitOffsetOldVersionNormalizer.transform(
                    projectOrderByTermsNormalizer.transform(
                        projectionWrapper.transform(tree, variableGenerator),
                    variableGenerator), variableGenerator),
                variableGenerator);
    }
}
