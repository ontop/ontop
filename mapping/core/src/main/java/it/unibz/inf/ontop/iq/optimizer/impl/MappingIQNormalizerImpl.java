package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.exception.DatalogConversionException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.BindingLiftOptimizer;
import it.unibz.inf.ontop.iq.optimizer.MappingIQNormalizer;
import it.unibz.inf.ontop.iq.optimizer.MappingUnionNormalizer;
import it.unibz.inf.ontop.iq.optimizer.UnionAndBindingLiftOptimizer;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;

public class MappingIQNormalizerImpl implements MappingIQNormalizer {


    private final UnionAndBindingLiftOptimizer bindingLifter;
    private final MappingUnionNormalizer mappingUnionNormalizer;
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final IQConverter iqConverter;

    @Inject
    private MappingIQNormalizerImpl(UnionAndBindingLiftOptimizer bindingLifter,
                                    MappingUnionNormalizer mappingUnionNormalizer, NoNullValueEnforcer noNullValueEnforcer,
                                    IQConverter iqConverter) {
        this.bindingLifter = bindingLifter;
        this.mappingUnionNormalizer = mappingUnionNormalizer;
        this.noNullValueEnforcer = noNullValueEnforcer;
        this.iqConverter = iqConverter;
    }

    /**
     * Lift substitutions and query modifiers, and get rid of resulting idle construction nodes.
     * Then flatten nested unions, and enforce non-nullability
     */
    @Override
    public IQ normalize(IQ query) {
        IQ queryWithoutNull = noNullValueEnforcer.transform(query);

        IQ  queryAfterBindingLift = bindingLifter.optimize(queryWithoutNull);
//            IQ normalizedIQ = mappingUnionNormalizer.optimize(iqConverter.convert(queryAfterBindingLift));
//            queryAfterUnionNormalization = iqConverter.convert(normalizedIQ, queryAfterBindingLift.getDBMetadata(),
//                    queryAfterBindingLift.getExecutorRegistry());
        return queryAfterBindingLift;
    }
}
