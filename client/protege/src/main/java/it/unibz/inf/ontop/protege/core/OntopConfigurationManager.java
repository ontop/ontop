package it.unibz.inf.ontop.protege.core;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import static it.unibz.inf.ontop.injection.OntopSQLCoreSettings.*;
import static it.unibz.inf.ontop.injection.OntopSQLCredentialSettings.*;

/**
 * TODO: find a better name
 */
public class OntopConfigurationManager {

    private final OBDAModel obdaModel;
    private final DisposableProperties settings;
    private final DisposableProperties userSettings;

    // Nullable
    @Nullable
    private File implicitDBConstraintFile;

    @Nullable
    private File dbMetadataFile;

    OntopConfigurationManager(@Nonnull OBDAModel obdaModel, @Nonnull DisposableProperties internalSettings) {
        this.obdaModel = obdaModel;
        this.settings = internalSettings;
        this.implicitDBConstraintFile = null;
        this.dbMetadataFile = null;
        this.userSettings = new DisposableProperties();
    }

    Properties snapshotProperties() {
        Properties properties = settings.clone();
        properties.putAll(userSettings.clone());
        properties.putAll(obdaModel.getDatasource().asProperties());
        return properties;
    }

    Properties snapshotUserProperties() {
        Properties properties = userSettings.clone();
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

    void setImplicitDBConstraintFile(File implicitDBConstraintFile) {
        this.implicitDBConstraintFile = implicitDBConstraintFile;
    }

    void clearImplicitDBConstraintFile() {
        this.implicitDBConstraintFile = null;
    }

    void setDBMetadataFile(File dbMetadataFile) {
        this.dbMetadataFile = dbMetadataFile;
    }

    void clearDBMetadataFile() { this.dbMetadataFile = null; }
    /**
     * Loads the properties in the global settings and in data source.
     */
    public void loadPropertyFile(File propertyFile) throws IOException {
        userSettings.load(new FileReader(propertyFile));
        loadDataSource(obdaModel, userSettings);
    }

    void resetProperties(DisposableProperties settings) {
        this.settings.clear();
        this.settings.putAll(settings);
        this.userSettings.clear();

        OBDADataSource dataSource = obdaModel.getDatasource();
        dataSource.setURL("");
        dataSource.setUsername("");
        dataSource.setPassword("");
        dataSource.setDriver("");
    }

    /**
     * Loads the properties in the global settings and in data source.
     */
    public void loadProperties(Properties properties) throws IOException {
        userSettings.putAll(properties);
        loadDataSource(obdaModel, userSettings);
    }

    private static void loadDataSource(OBDAModel obdaModel, DisposableProperties properties) {
        OBDADataSource dataSource = obdaModel.getDatasource();

        properties.getOptionalProperty(JDBC_URL).ifPresent(dataSource::setURL);
        properties.getOptionalProperty(JDBC_USER).ifPresent(dataSource::setUsername);
        properties.getOptionalProperty(JDBC_PASSWORD).ifPresent(dataSource::setPassword);
        properties.getOptionalProperty(JDBC_DRIVER).ifPresent(dataSource::setDriver);
    }
}
