package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.pivotalrepr.OptimizationConfiguration;
import it.unibz.inf.ontop.pivotalrepr.validation.IntermediateQueryValidator;

public class OntopModelModule extends OntopAbstractModule {

    private OntopModelConfiguration configuration;

    protected OntopModelModule(OntopModelConfiguration configuration) {
        super(configuration.getOntopModelProperties());
        // Temporary
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        configureCoreConfiguration();

        bind(OptimizationConfiguration.class).toInstance(configuration.getOptimizationConfiguration());
        bindFromPreferences(IntermediateQueryValidator.class);

        this.configuration = null;
    }
}
