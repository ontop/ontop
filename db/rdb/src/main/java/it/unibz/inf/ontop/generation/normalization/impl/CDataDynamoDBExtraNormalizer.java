package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class CDataDynamoDBExtraNormalizer implements DialectExtraNormalizer {

    private final ReformulateConjunctionDisjunctionNormalizer reformulateConjunctionDisjunctionNormalizer;


    @Inject
    protected CDataDynamoDBExtraNormalizer(ReformulateConjunctionDisjunctionNormalizer reformulateConjunctionDisjunctionNormalizer) {
        this.reformulateConjunctionDisjunctionNormalizer = reformulateConjunctionDisjunctionNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
              return reformulateConjunctionDisjunctionNormalizer.transform(tree, variableGenerator);
    }
}
