package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class DremioExtraNormalizer implements DialectExtraNormalizer {

    private final TypingNullsInUnionDialectExtraNormalizer typingNullInUnionNormalizer;
    private final TypingNullsInConstructionNodeDialectExtraNormalizer typingNullInConstructionNormalizer;
    private final SubQueryFromComplexJoinExtraNormalizer complexJoinNormalizer;

    @Inject
    protected DremioExtraNormalizer(TypingNullsInUnionDialectExtraNormalizer typingNullInUnionNormalizer,
                                    TypingNullsInConstructionNodeDialectExtraNormalizer typingNullInConstructionNormalizer,
                                    SubQueryFromComplexJoinExtraNormalizer complexJoinNormalizer) {
        this.typingNullInUnionNormalizer = typingNullInUnionNormalizer;
        this.typingNullInConstructionNormalizer = typingNullInConstructionNormalizer;
        this.complexJoinNormalizer = complexJoinNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
              return complexJoinNormalizer.transform(
                      typingNullInConstructionNormalizer.transform(
                              typingNullInUnionNormalizer.transform(tree, variableGenerator),
                              variableGenerator),
                      variableGenerator);
    }
}
