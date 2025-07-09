package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PostgresDialectExtraNormalizer extends CompositeDialectNormalizer {

    @Inject
    protected PostgresDialectExtraNormalizer(TypingNullsInUnionDialectExtraNormalizer typingNullNormalizer,
                                             OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer projectionNormalizer) {
        super(ImmutableList.of(
                typingNullNormalizer,
                projectionNormalizer));
    }
}
