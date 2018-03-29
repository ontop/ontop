package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.exception.DatalogConversionException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.BindingLiftOptimizer;
import it.unibz.inf.ontop.iq.optimizer.MappingIQNormalizer;
import it.unibz.inf.ontop.iq.optimizer.MappingUnionNormalizer;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.iq.transform.NoNullValueEnforcer;

public class MappingIQNormalizerImpl implements MappingIQNormalizer {


    private final BindingLiftOptimizer bindingLifter;
    private final MappingUnionNormalizer mappingUnionNormalizer;
    private final NoNullValueEnforcer noNullValueEnforcer;
    private final IQConverter iqConverter;

    @Inject
    private MappingIQNormalizerImpl(BindingLiftOptimizer bindingLifter,
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
    public IntermediateQuery normalize(IntermediateQuery query) {
        IntermediateQuery queryAfterBindingLift;
        try {
             queryAfterBindingLift = bindingLifter.optimize(query);
//            IQ normalizedIQ = mappingUnionNormalizer.optimize(iqConverter.convert(queryAfterBindingLift));
//            queryAfterUnionNormalization = iqConverter.convert(normalizedIQ, queryAfterBindingLift.getDBMetadata(),
//                    queryAfterBindingLift.getExecutorRegistry());
        } catch (EmptyQueryException e) {
            throw new DatalogConversionException("The query should not become empty");
        }
        return noNullValueEnforcer.transform(queryAfterBindingLift);
    }
}
