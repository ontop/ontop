package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class DremioExtraNormalizer implements DialectExtraNormalizer {

    private final TypingNullsDialectExtraNormalizer typingNullNormalizer;
    private final SubQueryFromComplexJoinExtraNormalizer complexJoinNormalizer;

    @Inject
    protected DremioExtraNormalizer(TypingNullsDialectExtraNormalizer typingNullNormalizer,
                                    SubQueryFromComplexJoinExtraNormalizer complexJoinNormalizer) {
        this.typingNullNormalizer = typingNullNormalizer;
        this.complexJoinNormalizer = complexJoinNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
              return complexJoinNormalizer.transform(
                      typingNullNormalizer.transform(tree, variableGenerator),
                      variableGenerator);
    }
}
