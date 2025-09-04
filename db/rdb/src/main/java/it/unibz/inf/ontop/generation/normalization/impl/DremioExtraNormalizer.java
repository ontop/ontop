package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.transform.impl.CompositeIQTreeVariableGeneratorTransformer;

@Singleton
public class DremioExtraNormalizer extends CompositeIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected DremioExtraNormalizer(TypingNullsInUnionDialectExtraNormalizer typingNullInUnionNormalizer,
                                    TypingNullsInConstructionNodeDialectExtraNormalizer typingNullInConstructionNormalizer,
                                    SubQueryFromComplexJoinExtraNormalizer complexJoinNormalizer) {
        super(typingNullInUnionNormalizer,
                typingNullInConstructionNormalizer,
                complexJoinNormalizer);
    }
}
