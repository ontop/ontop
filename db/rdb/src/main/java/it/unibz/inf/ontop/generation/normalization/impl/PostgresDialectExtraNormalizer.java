package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class PostgresDialectExtraNormalizer implements DialectExtraNormalizer {

    private final TypingNullsDialectExtraNormalizer typingNullNormalizer;
    private final OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer projectionNormalizer;

    @Inject
    protected PostgresDialectExtraNormalizer(TypingNullsDialectExtraNormalizer typingNullNormalizer,
                                          OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer projectionNormalizer) {
        this.typingNullNormalizer = typingNullNormalizer;
        this.projectionNormalizer = projectionNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return projectionNormalizer.transform(
                typingNullNormalizer.transform(tree, variableGenerator),
                variableGenerator);
    }
}
