package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.generation.algebra.*;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;

public class OntopSQLCoreModule extends OntopAbstractModule {

    private final OntopSQLCoreSettings settings;

    protected OntopSQLCoreModule(OntopSQLCoreConfiguration configuration) {
        super(configuration.getSettings());
        this.settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopSQLCoreSettings.class).toInstance(settings);

        bindFromSettings(IQTree2SelectFromWhereConverter.class);

        Module sqlAlgebraFactory = buildFactory(
                ImmutableList.of(
                        SelectFromWhereWithModifiers.class,
                        SQLSerializedQuery.class,
                        SQLTable.class,
                        SQLInnerJoinExpression.class,
                        SQLLeftJoinExpression.class,
                        SQLNaryJoinExpression.class,
                        SQLUnionExpression.class,
                        SQLOneTupleDummyQueryExpression.class,
                        SQLOrderComparator.class
                ),
                SQLAlgebraFactory.class);
        install(sqlAlgebraFactory);
    }
}
