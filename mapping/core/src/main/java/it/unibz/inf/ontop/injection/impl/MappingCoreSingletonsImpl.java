package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.*;

public class MappingCoreSingletonsImpl implements MappingCoreSingletons {
    private final CoreSingletons coreSingletons;
    private final OptimizationSingletons optimizationSingletons;
    private final SpecificationFactory specificationFactory;
    private final TargetQueryParserFactory targetQueryParserFactory;

    @Inject
    private MappingCoreSingletonsImpl(CoreSingletons coreSingletons, OptimizationSingletons optimizationSingletons,
                                     SpecificationFactory specificationFactory,
                                     TargetQueryParserFactory targetQueryParserFactory) {
        this.coreSingletons = coreSingletons;
        this.optimizationSingletons = optimizationSingletons;
        this.specificationFactory = specificationFactory;
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
    public TargetQueryParserFactory getTargetQueryParserFactory() {
        return targetQueryParserFactory;
    }
}
