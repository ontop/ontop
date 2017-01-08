package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopModelFactory;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.validation.IntermediateQueryValidator;

public class OntopModelModule extends OntopAbstractModule {

    protected OntopModelModule(OntopModelConfiguration configuration) {
        super(configuration.getProperties());
    }

    @Override
    protected void configure() {
        configureCoreConfiguration();

        bindFromPreferences(IntermediateQueryValidator.class);

        Module modelFactoryModule = buildFactory(ImmutableList.<Class>of(
                IntermediateQueryBuilder.class
                ),
                OntopModelFactory.class);
        install(modelFactoryModule);
    }
}
