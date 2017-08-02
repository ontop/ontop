package it.unibz.inf.ontop.protege.core;


import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.parser.DataSource2PropertiesConvertor;
import it.unibz.inf.ontop.protege.core.impl.RDBMSourceParameterConstants;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import static it.unibz.inf.ontop.injection.OntopSQLCoreSettings.*;

/**
 * TODO: find a better name
 */
public class OntopConfigurationManager {

    private final OBDAModel obdaModel;
    private final DisposableProperties settings;

    // Nullable
    @Nullable
    private File implicitDBConstraintFile;

    OntopConfigurationManager(@Nonnull OBDAModel obdaModel, @Nonnull DisposableProperties settings) {
        this.obdaModel = obdaModel;
        this.settings = settings;
        this.implicitDBConstraintFile = null;
    }

    Properties snapshotProperties() {
        Properties properties = settings.clone();
        properties.putAll(DataSource2PropertiesConvertor.convert(obdaModel.getDatasource()));
        return properties;
    }

    public OntopSQLOWLAPIConfiguration buildOntopSQLOWLAPIConfiguration(OWLOntology currentOntology) {
        OntopSQLOWLAPIConfiguration.Builder builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .properties(snapshotProperties())
                .ppMapping(obdaModel.generatePPMapping());

        Optional.ofNullable(implicitDBConstraintFile)
                .ifPresent(builder::basicImplicitConstraintFile);

        builder.ontology(currentOntology);

        return builder.build();
    }

    void setImplicitDBConstraintFile(File implicitDBConstraintFile) {
        this.implicitDBConstraintFile = implicitDBConstraintFile;
    }

    void clearImplicitDBConstraintFile() {
        this.implicitDBConstraintFile = null;
    }

    /**
     * Loads the properties in the global settings and in data source.
     */
    public void loadPropertyFile(File propertyFile) throws IOException {
        settings.load(new FileReader(propertyFile));
        loadDataSource(obdaModel, settings);
    }

    void resetProperties(DisposableProperties settings){
        this.settings.clear();
        this.settings.putAll(settings);

        OBDADataSource dataSource = obdaModel.getDatasource();
        dataSource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, "");
        dataSource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, "");
        dataSource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, "");
        dataSource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, "");

    }

    /**
     * Loads the properties in the global settings and in data source.
     */
    public void loadProperties(Properties properties) throws IOException {
        settings.putAll(properties);
        loadDataSource(obdaModel, settings);
    }

    private static void loadDataSource(OBDAModel obdaModel, DisposableProperties properties) {
        OBDADataSource dataSource = obdaModel.getDatasource();

        properties.getOptionalProperty(JDBC_URL)
                .ifPresent(v -> dataSource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, v));
        properties.getOptionalProperty(JDBC_USER)
                .ifPresent(v -> dataSource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, v));
        properties.getOptionalProperty(JDBC_PASSWORD)
                .ifPresent(v -> dataSource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, v));
        properties.getOptionalProperty(JDBC_DRIVER)
                .ifPresent(v -> dataSource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, v));

    }
}
