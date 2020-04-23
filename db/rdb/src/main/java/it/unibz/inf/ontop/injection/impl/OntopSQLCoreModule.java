package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.dbschema.DBMetadataProvider;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
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

        Module mdProvider = buildFactory(ImmutableList.of(DBMetadataProvider.class), JDBCMetadataProviderFactory.class);
        install(mdProvider);
    }
}
