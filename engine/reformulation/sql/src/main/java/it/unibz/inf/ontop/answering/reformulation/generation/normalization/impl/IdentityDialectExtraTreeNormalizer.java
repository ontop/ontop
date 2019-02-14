package it.unibz.inf.ontop.answering.reformulation.generation.normalization.impl;

import it.unibz.inf.ontop.answering.reformulation.generation.normalization.DialectExtraTreeNormalizer;
import it.unibz.inf.ontop.iq.IQTree;

/**
 * Does nothing
 */
public class IdentityDialectExtraTreeNormalizer implements DialectExtraTreeNormalizer {

    @Override
    public IQTree transform(IQTree tree) {
        return tree;
    }
}
