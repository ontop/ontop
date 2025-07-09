package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DremioExtraNormalizer extends CompositeDialectNormalizer {

    @Inject
    protected DremioExtraNormalizer(TypingNullsInUnionDialectExtraNormalizer typingNullInUnionNormalizer,
                                    TypingNullsInConstructionNodeDialectExtraNormalizer typingNullInConstructionNormalizer,
                                    SubQueryFromComplexJoinExtraNormalizer complexJoinNormalizer) {
        super(ImmutableList.of(
                typingNullInUnionNormalizer,
                typingNullInConstructionNormalizer,
                complexJoinNormalizer));
    }
}
