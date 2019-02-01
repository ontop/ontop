package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLAlgebraFactory;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLSerializedQuery;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhere;
import it.unibz.inf.ontop.injection.OntopReformulationSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;

public class OntopReformulationSQLModule extends OntopAbstractModule {

    private final OntopReformulationSQLSettings settings;

    protected OntopReformulationSQLModule(OntopReformulationSQLConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopReformulationSQLSettings.class).toInstance(settings);

        Module sqlAlgebraFactory = buildFactory(
                ImmutableList.of(
                        SelectFromWhere.class,
                        SQLSerializedQuery.class),
                SQLAlgebraFactory.class);
        install(sqlAlgebraFactory);
    }
}
