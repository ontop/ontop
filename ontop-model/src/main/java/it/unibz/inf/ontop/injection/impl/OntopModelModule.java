package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.DatatypeFactory;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.validation.IntermediateQueryValidator;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.*;

public class OntopModelModule extends OntopAbstractModule {

    protected OntopModelModule(OntopModelConfiguration configuration) {
        super(configuration.getSettings());
    }

    @Override
    protected void configure() {
        configureCoreConfiguration();

        // Core factories: Too central to be overloaded from the properties
        bind(DatatypeFactory.class).toInstance(DATATYPE_FACTORY);
        bind(OBDADataFactory.class).toInstance(DATA_FACTORY);

        bindFromPreferences(IntermediateQueryValidator.class);

        Module modelFactoryModule = buildFactory(ImmutableList.<Class>of(
                IntermediateQueryBuilder.class
                ),
                IntermediateQueryFactory.class);
        install(modelFactoryModule);
    }
}
