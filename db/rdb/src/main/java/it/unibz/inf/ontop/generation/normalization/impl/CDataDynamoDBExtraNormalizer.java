package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;

@Singleton
public class CDataDynamoDBExtraNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected CDataDynamoDBExtraNormalizer(ReformulateConjunctionDisjunctionNormalizer reformulateConjunctionDisjunctionNormalizer) {
        super(reformulateConjunctionDisjunctionNormalizer);
    }
}
