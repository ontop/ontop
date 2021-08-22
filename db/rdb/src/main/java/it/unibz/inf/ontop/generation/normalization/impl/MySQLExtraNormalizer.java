package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

@Singleton
public class MySQLExtraNormalizer implements DialectExtraNormalizer {

    private final OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer orderByNormalizer;
    private final ReplaceProvenanceConstantByNonGroundTermNormalizer provenanceNormalizer;
    private final ConvertValuesToUnionNormalizer toUnionNormalizer;

    @Inject
    private MySQLExtraNormalizer(OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer orderByNormalizer,
                                 ReplaceProvenanceConstantByNonGroundTermNormalizer provenanceNormalizer,
                                 ConvertValuesToUnionNormalizer toUnionNormalizer) {

        this.orderByNormalizer = orderByNormalizer;
        this.provenanceNormalizer = provenanceNormalizer;
        this.toUnionNormalizer = toUnionNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return toUnionNormalizer.transform(
                    provenanceNormalizer.transform(
                        orderByNormalizer.transform(tree, variableGenerator),
                    variableGenerator),
                variableGenerator);
    }
}
