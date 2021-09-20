package it.unibz.inf.ontop.generation.normalization.impl;

import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;

public class DB2ExtraNormalizer implements DialectExtraNormalizer {

    private final DialectExtraNormalizer enforceNullOrderNormalizer;
    private final DialectExtraNormalizer projectOrderByNormalizer;

    @Inject
    protected DB2ExtraNormalizer(EnforceNullOrderNormalizer enforceNullOrderNormalizer,
                                 OnlyInPresenceOfDistinctProjectOrderByTermsNormalizer projectOrderByNormalizer) {
        this.enforceNullOrderNormalizer = enforceNullOrderNormalizer;
        this.projectOrderByNormalizer = projectOrderByNormalizer;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return projectOrderByNormalizer.transform(
                enforceNullOrderNormalizer.transform(tree, variableGenerator),
                variableGenerator);
    }
}
