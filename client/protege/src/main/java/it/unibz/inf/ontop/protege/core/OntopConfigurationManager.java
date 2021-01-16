package it.unibz.inf.ontop.protege.core;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;

import static it.unibz.inf.ontop.injection.OntopSQLCoreSettings.*;
import static it.unibz.inf.ontop.injection.OntopSQLCredentialSettings.*;

/**
 * TODO: find a better name
 */
public class OntopConfigurationManager {

    public static final String PROPERTY_EXT = ".properties"; // The default property file extension.
    public static final String DBPREFS_EXT = ".db_prefs"; // The default db_prefs (currently only user constraints) file extension.
    public static final String DBMETADATA_EXT = ".json"; // The default db-metadata file extension.

    private final OBDAModel obdaModel;
    private final Properties settings = new Properties();
    private final Properties userSettings = new Properties();

    @Nullable
    private File implicitDBConstraintFile;

    @Nullable
    private File dbMetadataFile;

    OntopConfigurationManager(@Nonnull OBDAModel obdaModel, @Nonnull DisposableProperties internalSettings) {
        this.obdaModel = obdaModel;
        this.settings.putAll(internalSettings);
        this.implicitDBConstraintFile = null;
        this.dbMetadataFile = null;
    }

    public void reset(DisposableProperties settings) {
        this.implicitDBConstraintFile = null;
        this.dbMetadataFile = null;

        this.settings.clear();
        this.settings.putAll(settings);
        this.userSettings.clear();

        OBDADataSource dataSource = obdaModel.getDatasource();
        dataSource.setURL("");
        dataSource.setUsername("");
        dataSource.setPassword("");
        dataSource.setDriver("");
    }

    public void loadNewConfiguration(String owlName) throws IOException {
        File implicitDBConstraintFile = new File(URI.create(owlName + DBPREFS_EXT));
        if (implicitDBConstraintFile.exists())
            this.implicitDBConstraintFile = implicitDBConstraintFile;

        File dbMetadataFile = new File(URI.create(owlName + DBMETADATA_EXT));
        if(dbMetadataFile.exists())
            this.dbMetadataFile = dbMetadataFile;

        File propertyFile = new File(URI.create(owlName + PROPERTY_EXT));
        if (propertyFile.exists()) {
            userSettings.load(new FileReader(propertyFile));
            loadDataSource(obdaModel, userSettings);
        }
    }

    Properties snapshotProperties() {
        Properties properties = new Properties();
        properties.putAll(settings);
        properties.putAll(userSettings);
        properties.putAll(obdaModel.getDatasource().asProperties());
        return properties;
    }

    Properties snapshotUserProperties() {
        Properties properties = new Properties();
        properties.putAll(userSettings);
        properties.putAll(obdaModel.getDatasource().asProperties());
        return properties;
    }

    public OntopSQLOWLAPIConfiguration buildOntopSQLOWLAPIConfiguration(OWLOntology currentOntology) {

        OntopSQLOWLAPIConfiguration.Builder builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .properties(snapshotProperties())
                .ppMapping(obdaModel.generatePPMapping());

        Optional.ofNullable(implicitDBConstraintFile)
                .ifPresent(builder::basicImplicitConstraintFile);

        Optional.ofNullable(dbMetadataFile)
                .ifPresent(builder::basicDBMetadataFile);

        builder.ontology(currentOntology);

        return builder.build();
    }

    /**
     * Loads the properties in the global settings and in data source.
     */
    public void loadProperties(Properties properties) {
        userSettings.putAll(properties);
        loadDataSource(obdaModel, userSettings);
    }

    private static void loadDataSource(OBDAModel obdaModel, Properties properties) {
        OBDADataSource dataSource = obdaModel.getDatasource();

        Optional.ofNullable(properties.getProperty(JDBC_URL))
                .ifPresent(dataSource::setURL);

        Optional.ofNullable(properties.getProperty(JDBC_USER))
                .ifPresent(dataSource::setUsername);

        Optional.ofNullable(properties.getProperty(JDBC_PASSWORD))
                .ifPresent(dataSource::setPassword);

        Optional.ofNullable(properties.getProperty(JDBC_DRIVER))
                .ifPresent(dataSource::setDriver);
    }
}
