package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.exception.DatalogConversionException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.BindingLiftOptimizer;
import it.unibz.inf.ontop.iq.optimizer.ConstructionNodeCleaner;
import it.unibz.inf.ontop.iq.optimizer.MappingIQNormalizer;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;

public class MappingIQNormalizerImpl implements MappingIQNormalizer {


    private final BindingLiftOptimizer bindingLifter;
    private final ConstructionNodeCleaner constructionNodeCleaner;
    private final MappingUnionNormalizerImpl mappingUnionNormalizer;
    private final NoNullValueEnforcer noNullValueEnforcer;

    @Inject
    private MappingIQNormalizerImpl(BindingLiftOptimizer bindingLifter, ConstructionNodeCleaner constructionNodeCleaner,
                                   MappingUnionNormalizerImpl mappingUnionNormalizer, NoNullValueEnforcer noNullValueEnforcer) {
        this.bindingLifter = bindingLifter;
        this.constructionNodeCleaner = constructionNodeCleaner;
        this.mappingUnionNormalizer = mappingUnionNormalizer;
        this.noNullValueEnforcer = noNullValueEnforcer;
    }

    /**
     * Lift substitutions and query modifiers, and get rid of resulting idle construction nodes.
     * Then flatten nested unions, and enforce non-nullability
     */
    @Override
    public IntermediateQuery normalize(IntermediateQuery query) {
        IntermediateQuery queryAfterUnionNormalization;
        try {
            IntermediateQuery queryAfterBindingLift = bindingLifter.optimize(query);
            IntermediateQuery queryAfterCNodeCleaning = constructionNodeCleaner.optimize(queryAfterBindingLift);
            queryAfterUnionNormalization = mappingUnionNormalizer.optimize(queryAfterCNodeCleaning);
        } catch (EmptyQueryException e) {
            throw new DatalogConversionException("The query should not become empty");
        }
        return noNullValueEnforcer.transform(queryAfterUnionNormalization);
    }
}
