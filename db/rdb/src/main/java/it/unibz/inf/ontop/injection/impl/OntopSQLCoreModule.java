package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.generation.algebra.*;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.iq.transform.IQTree2NativeNodeGenerator;

public class OntopSQLCoreModule extends OntopAbstractModule {

    private final OntopSQLCoreSettings settings;

    protected OntopSQLCoreModule(OntopSQLCoreConfiguration configuration) {
        super(configuration.getSettings());
        this.settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopSQLCoreSettings.class).toInstance(settings);

        bindFromSettings(SelectFromWhereSerializer.class);
        bindFromSettings(IQTree2SelectFromWhereConverter.class);
        bindFromSettings(DialectExtraNormalizer.class);
        bindFromSettings(IQTree2NativeNodeGenerator.class);
        bindFromSettings(OntopViewNormalizer.class);
        bindFromSettings(OntopViewFKSaturator.class);

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
                        SQLValuesExpression.class,
                        SQLOrderComparator.class
                ),
                SQLAlgebraFactory.class);
        install(sqlAlgebraFactory);

        Module serializedMetadataProviderFactory = buildFactory(
                ImmutableList.of(
                        SerializedMetadataProvider.class
                ),
                SerializedMetadataProvider.Factory.class);
        install(serializedMetadataProviderFactory);

        Module ontopViewMetadataProviderFactory = buildFactory(
                ImmutableList.of(
                        OntopViewMetadataProvider.class
                ),
                OntopViewMetadataProvider.Factory.class);
        install(ontopViewMetadataProviderFactory);

        Module mdProvider = buildFactory(ImmutableList.of(DBMetadataProvider.class), JDBCMetadataProviderFactory.class);
        install(mdProvider);
    }
}
