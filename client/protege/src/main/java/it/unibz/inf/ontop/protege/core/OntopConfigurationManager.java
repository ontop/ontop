package it.unibz.inf.ontop.protege.core;


import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.protege.connection.DataSource;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;

/**
 * TODO: find a better name
 */
public class OntopConfigurationManager {

    public static final String DBPREFS_EXT = ".db_prefs"; // The default db_prefs (currently only user constraints) file extension.
    public static final String DBMETADATA_EXT = ".json"; // The default db-metadata file extension.

    private final OBDAModelManager obdaModelManager;
    private final Properties settings = new Properties();

    @Nullable
    private File implicitDBConstraintFile;

    @Nullable
    private File dbMetadataFile;

    OntopConfigurationManager(@Nonnull OBDAModelManager obdaModelManager, DisposableProperties standardProperties) {
        this.obdaModelManager = obdaModelManager;
        this.settings.putAll(standardProperties);
        this.implicitDBConstraintFile = null;
        this.dbMetadataFile = null;
    }

    public void load(String owlName) {
        File implicitDBConstraintFile = new File(URI.create(owlName + DBPREFS_EXT));
        if (implicitDBConstraintFile.exists())
            this.implicitDBConstraintFile = implicitDBConstraintFile;

        File dbMetadataFile = new File(URI.create(owlName + DBMETADATA_EXT));
        if (dbMetadataFile.exists())
            this.dbMetadataFile = dbMetadataFile;
    }

    private static Properties union(Properties settings, DataSource datasource) {
        Properties properties = new Properties();
        properties.putAll(settings);
        properties.putAll(datasource.asProperties());
        return properties;
    }

    public SQLMappingParser getSQLMappingParser(DataSource datasource, Reader mappingReader) {
        OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
                .properties(union(settings, datasource))
                .nativeOntopMappingReader(mappingReader)
                .build();

        return configuration.getInjector().getInstance(SQLMappingParser.class);
    }

    public OntopMappingSQLAllConfiguration buildR2RMLConfiguration(DataSource datasource, File file) {
        return OntopMappingSQLAllConfiguration.defaultBuilder()
                .properties(datasource.asProperties())
                .r2rmlMappingFile(file)
                .build();
    }

    public OntopSQLOWLAPIConfiguration buildOntopSQLOWLAPIConfiguration(OWLOntology ontology) {

        OBDAModel obdaModel = obdaModelManager.getOBDAModel(ontology);

        OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .properties(union(settings, obdaModel.getDataSource()))
                .ppMapping(obdaModel.getTriplesMapCollection().generatePPMapping());

        Optional.ofNullable(implicitDBConstraintFile)
                .ifPresent(builder::basicImplicitConstraintFile);

        Optional.ofNullable(dbMetadataFile)
                .ifPresent(builder::dbMetadataFile);

        builder.ontology(obdaModel.getOntology());

        return builder.build();
    }

    public OntopSQLOWLAPIConfiguration getBasicConfiguration(OBDAModel obdaModel) {
        OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .properties(union(settings, obdaModel.getDataSource()));

        Optional.ofNullable(implicitDBConstraintFile)
                .ifPresent(builder::basicImplicitConstraintFile);

        Optional.ofNullable(dbMetadataFile)
                .ifPresent(builder::dbMetadataFile);

        builder.ontology(obdaModel.getOntology());

        return builder.build();
    }
}
