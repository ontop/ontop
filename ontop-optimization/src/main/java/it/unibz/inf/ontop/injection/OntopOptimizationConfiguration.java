package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopOptimizationConfigurationImpl;

public interface OntopOptimizationConfiguration extends OntopModelConfiguration {

    @Override
    OntopOptimizationSettings getSettings();

    /**
     * Default builder
     */
    static OntopOptimizationConfiguration.Builder<OntopOptimizationConfiguration.Builder> defaultBuilder() {
        return new OntopOptimizationConfigurationImpl.BuilderImpl<>();
    }

    /**
     * TODO: add some configuration methods for end-users
     */
    interface OntopOptimizationBuilderFragment<B extends OntopOptimizationConfiguration.Builder> {
    }


    interface Builder<B extends Builder> extends OntopOptimizationBuilderFragment<B>, OntopModelConfiguration.Builder<B> {

        @Override
        OntopOptimizationConfiguration build();
    }

}
