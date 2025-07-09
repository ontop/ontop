package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CDataDynamoDBExtraNormalizer extends CompositeDialectNormalizer {

    @Inject
    protected CDataDynamoDBExtraNormalizer(ReformulateConjunctionDisjunctionNormalizer reformulateConjunctionDisjunctionNormalizer) {
        super(ImmutableList.of(reformulateConjunctionDisjunctionNormalizer));
    }
}
