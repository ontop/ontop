package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopRuntimeConfigurationImpl;

public interface OntopRuntimeConfiguration extends OntopOBDAConfiguration, OntopOptimizationConfiguration {

    @Override
    OntopRuntimeSettings getSettings();

    static Builder<? extends Builder> defaultBuilder() {
        return new OntopRuntimeConfigurationImpl.BuilderImpl<>();
    }


    interface OntopRuntimeBuilderFragment<B extends Builder<B>> {
        /**
         * In the case of SQL, inserts REPLACE functions in the generated query
         */
        B enableIRISafeEncoding(boolean enable);

        B enableExistentialReasoning(boolean enable);
    }

    interface Builder<B extends Builder<B>> extends OntopRuntimeBuilderFragment<B>, OntopOBDAConfiguration.Builder<B>,
            OntopOptimizationConfiguration.Builder<B> {

        @Override
        OntopRuntimeConfiguration build();
    }

}
