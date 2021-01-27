package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl;

public interface OntopOptimizationConfiguration extends OntopModelConfiguration {

    @Override
    OntopOptimizationSettings getSettings();

    /**
     * Default builder
     */
    static Builder<? extends Builder<?>> defaultBuilder() {
        return new OntopOptimizationConfigurationImpl.BuilderImpl<>();
    }

    /**
     * TODO: add some configuration methods for end-users
     */
    interface OntopOptimizationBuilderFragment<B extends OntopOptimizationConfiguration.Builder<B>> {
    }


    interface Builder<B extends Builder<B>> extends OntopOptimizationBuilderFragment<B>, OntopModelConfiguration.Builder<B> {

        @Override
        OntopOptimizationConfiguration build();
    }

}
