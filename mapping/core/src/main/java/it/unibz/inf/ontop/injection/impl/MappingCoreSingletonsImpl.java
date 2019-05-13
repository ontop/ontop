package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.*;

public class MappingCoreSingletonsImpl implements MappingCoreSingletons {
    private final CoreSingletons coreSingletons;
    private final OptimizationSingletons optimizationSingletons;
    private final SpecificationFactory specificationFactory;
    private final ProvenanceMappingFactory provenanceMappingFactory;
    private final TargetQueryParserFactory targetQueryParserFactory;

    @Inject
    private MappingCoreSingletonsImpl(CoreSingletons coreSingletons, OptimizationSingletons optimizationSingletons,
                                     SpecificationFactory specificationFactory,
                                     ProvenanceMappingFactory provenanceMappingFactory,
                                     TargetQueryParserFactory targetQueryParserFactory) {
        this.coreSingletons = coreSingletons;
        this.optimizationSingletons = optimizationSingletons;
        this.specificationFactory = specificationFactory;
        this.provenanceMappingFactory = provenanceMappingFactory;
        this.targetQueryParserFactory = targetQueryParserFactory;
    }

    @Override
    public CoreSingletons getCoreSingletons() {
        return coreSingletons;
    }

    @Override
    public OptimizationSingletons getOptimizationSingletons() {
        return optimizationSingletons;
    }

    @Override
    public SpecificationFactory getSpecificationFactory() {
        return specificationFactory;
    }

    @Override
    public ProvenanceMappingFactory getProvenanceMappingFactory() {
        return provenanceMappingFactory;
    }

    @Override
    public TargetQueryParserFactory getTargetQueryParserFactory() {
        return targetQueryParserFactory;
    }
}
