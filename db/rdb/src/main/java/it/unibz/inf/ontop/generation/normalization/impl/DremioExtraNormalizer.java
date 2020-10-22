package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class DremioExtraNormalizer implements DialectExtraNormalizer {

    private final TypingNullsDialectExtraNormalizer typingNullNormalizer;

    @Inject
    protected DremioExtraNormalizer(TypingNullsDialectExtraNormalizer typingNullNormalizer) {
        this.typingNullNormalizer = typingNullNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
              return   typingNullNormalizer.transform(tree, variableGenerator);
    }
}
