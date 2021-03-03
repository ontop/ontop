package it.unibz.inf.ontop.protege.core;


import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.OntopReformulationConfiguration;
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

    private final OBDAModel obdaModel;
    // settings are loaded once in the constructor and not modified afterwards
    private final Properties settings = new Properties();

    @Nullable
    private File implicitDBConstraintFile;

    @Nullable
    private File dbMetadataFile;

    OntopConfigurationManager(@Nonnull OBDAModel obdaModel, DisposableProperties standardProperties) {
        this.obdaModel = obdaModel;
        this.settings.putAll(standardProperties);
        this.implicitDBConstraintFile = null;
        this.dbMetadataFile = null;
    }

    public void clear() {
        this.implicitDBConstraintFile = null;
        this.dbMetadataFile = null;
    }

    /**
        Can be called twice in a row without clear(), at least for now.
     */

    public void load(String owlName) {
        File implicitDBConstraintFile = new File(URI.create(owlName + DBPREFS_EXT));
        this.implicitDBConstraintFile = implicitDBConstraintFile.exists()
                ? implicitDBConstraintFile
                : null;

        File dbMetadataFile = new File(URI.create(owlName + DBMETADATA_EXT));
        this.dbMetadataFile = dbMetadataFile.exists()
                ? dbMetadataFile
                : null;
    }

    public SQLMappingParser getSQLMappingParser(Reader mappingReader) {
        return constructBuilder(OntopMappingSQLAllConfiguration.defaultBuilder())
                    .nativeOntopMappingReader(mappingReader)
                    .build()
                .getInjector()
                .getInstance(SQLMappingParser.class);
    }

    public OntopMappingSQLAllConfiguration buildR2RMLConfiguration(File file) {
        return constructBuilder(OntopMappingSQLAllConfiguration.defaultBuilder())
                .r2rmlMappingFile(file).build();
    }

    public OntopSQLOWLAPIConfiguration buildOntopSQLOWLAPIConfiguration() {
        return constructBuilder(OntopSQLOWLAPIConfiguration.defaultBuilder())
                .ppMapping(obdaModel.getTriplesMapCollection().generatePPMapping())
                .ontology(obdaModel.getOntology()).build();
    }

    public OntopSQLOWLAPIConfiguration getBasicConfiguration() {
        return constructBuilder(OntopSQLOWLAPIConfiguration.defaultBuilder())
                .ontology(obdaModel.getOntology()).build();
    }

    private <B extends OntopMappingSQLAllConfiguration.Builder<?>> B constructBuilder(B builder) {

        Properties properties = new Properties();
        properties.putAll(settings);
        properties.putAll(obdaModel.getDataSource().asProperties());

        builder.properties(properties);

        Optional.ofNullable(implicitDBConstraintFile)
                .ifPresent(builder::basicImplicitConstraintFile);

        Optional.ofNullable(dbMetadataFile)
                .ifPresent(builder::dbMetadataFile);

        return builder;
    }
}
