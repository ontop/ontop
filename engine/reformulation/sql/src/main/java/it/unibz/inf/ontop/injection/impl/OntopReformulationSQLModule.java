package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.*;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;
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

        bindFromSettings(SQLDialectAdapter.class);
        bindFromSettings(SelectFromWhereSerializer.class);
        bindFromSettings(IQTree2SelectFromWhereConverter.class);
        bindFromSettings(SQLTermSerializer.class);
        bindFromSettings(DialectExtraNormalizer.class);

        Module sqlAlgebraFactory = buildFactory(
                ImmutableList.of(
                        SelectFromWhereWithModifiers.class,
                        SQLSerializedQuery.class,
                        SQLTable.class),
                SQLAlgebraFactory.class);
        install(sqlAlgebraFactory);
    }
}
