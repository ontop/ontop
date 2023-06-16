package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.*;
import it.unibz.inf.ontop.generation.algebra.*;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
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
        bindFromSettings(LensNormalizer.class);
        bindFromSettings(LensFKSaturator.class);

        Module sqlAlgebraFactory = buildFactory(
                ImmutableList.of(
                        SelectFromWhereWithModifiers.class,
                        SQLSerializedQuery.class,
                        SQLTable.class,
                        SQLInnerJoinExpression.class,
                        SQLLeftJoinExpression.class,
                        SQLNaryJoinExpression.class,
                        SQLUnionExpression.class,
                        SQLFlattenExpression.class,
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
                        LensMetadataProvider.class
                ),
                LensMetadataProvider.Factory.class);
        install(ontopViewMetadataProviderFactory);

        Module mdProvider = buildFactory(ImmutableList.of(DBMetadataProvider.class), JDBCMetadataProviderFactory.class);
        install(mdProvider);

        for (var c : ImmutableList.of(
                BigQueryQuotedIDFactory.class,
                DremioQuotedIDFactory.class,
                DuckDBQuotedIDFactory.class,
                MySQLCaseNotSensitiveTableNamesQuotedIDFactory.class,
                MySQLCaseSensitiveTableNamesQuotedIDFactory.class,
                PostgreSQLQuotedIDFactory.class,
                SQLServerQuotedIDFactory.class,
                SparkSQLQuotedIDFactory.class,
                TeiidQuotedIDFactory.class,
                TrinoQuotedIDFactory.class
        )) {
            String idFactoryType = QuotedIDFactory.getIDFactoryType(c);
            bindFromSettings(Key.get(QuotedIDFactory.class, Names.named(idFactoryType)), c);
        }
    }

}
