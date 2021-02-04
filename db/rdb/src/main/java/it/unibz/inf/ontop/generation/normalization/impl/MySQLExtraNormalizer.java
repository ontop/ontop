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

    @Inject
    private MySQLExtraNormalizer(OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer orderByNormalizer,
                                 ReplaceProvenanceConstantByNonGroundTermNormalizer provenanceNormalizer) {

        this.orderByNormalizer = orderByNormalizer;
        this.provenanceNormalizer = provenanceNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return provenanceNormalizer.transform(
                orderByNormalizer.transform(tree, variableGenerator), variableGenerator);
    }
}
