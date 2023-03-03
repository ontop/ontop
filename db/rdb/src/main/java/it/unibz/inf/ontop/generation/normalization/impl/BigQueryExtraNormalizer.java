package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeExtendedTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class BigQueryExtraNormalizer implements DialectExtraNormalizer {

    private final PushProjectedOrderByTermsNormalizer pushProjectedOrderByTermsNormalizer;
    private final ConvertValuesToUnionNormalizer convertValuesToUnionNormalizer;

    @Inject
    protected BigQueryExtraNormalizer(PushProjectedOrderByTermsNormalizer pushProjectedOrderByTermsNormalizer,
                                      ConvertValuesToUnionNormalizer convertValuesToUnionNormalizer) {
        this.pushProjectedOrderByTermsNormalizer = pushProjectedOrderByTermsNormalizer;
        this.convertValuesToUnionNormalizer = convertValuesToUnionNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
              return pushProjectedOrderByTermsNormalizer.transform(
                      convertValuesToUnionNormalizer.transform(tree, variableGenerator),
                      variableGenerator);
    }
}
