package it.unibz.inf.ontop.injection;

public interface OntopKGQueryConfiguration extends OntopOBDAConfiguration, OntopOptimizationConfiguration {
    @Override
    OntopKGQuerySettings getSettings();

    interface Builder<B extends Builder<B>> extends OntopOBDAConfiguration.Builder<B>,
            OntopOptimizationConfiguration.Builder<B> {

        @Override
        OntopKGQueryConfiguration build();
    }
}
