package it.unibz.inf.ontop.docker;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.exception.R2RMLSerializationException;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.R2RMLMappingSerializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Issue461Test {
    private static final String owlFile = "src/test/resources/issue461/ontology.owl";
    private static final String obdaFile = "src/test/resources/issue461/mapping.obda";
    private static final String propertiesFile = "src/test/resources/issue461/mapping.properties";
    private static final String databaseFile = "src/test/resources/issue461/database.sql";
    private static final String outputMappingFile = "src/test/resources/issue461/mapping.r2rml";
    private static final String obdaFilewithDuplicates = "src/test/resources/issue461/mappingwithduplicates.obda";

    @BeforeClass
    public static void before() throws SQLException, FileNotFoundException {

        Connection sqlConnection = DriverManager.getConnection("jdbc:h2:mem:questjunitdb", "sa", "");
        try (java.sql.Statement s = sqlConnection.createStatement()) {
            String text = new Scanner(new File(databaseFile)).useDelimiter("\\A").next();
            s.execute(text);
        }
    }

    @AfterClass
    public static void after() {
    }

    @Test
    public void testConvert() throws Exception {
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFile)
                .ontologyFile(owlFile)
                .propertyFile(propertiesFile)
                .enableTestMode()
                .build();

        R2RMLMappingSerializer converter = new R2RMLMappingSerializer(config.getRdfFactory());
        converter.write(new File(outputMappingFile), config.loadProvidedPPMapping());

        OntopSQLOWLAPIConfiguration config2 = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .r2rmlMappingFile(outputMappingFile)
                .ontologyFile(owlFile)
                .propertyFile(propertiesFile)
                .enableTestMode()
                .build();
        config2.loadProvidedPPMapping();
    }

    // r2rml conversion will fail when the mapping file contains duplicates
    @Test(expected = R2RMLSerializationException.class)
    public void testConvertwithDuplicates() throws Exception {
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFilewithDuplicates)
                .ontologyFile(owlFile)
                .propertyFile(propertiesFile)
                .enableTestMode()
                .build();

        R2RMLMappingSerializer converter = new R2RMLMappingSerializer(config.getRdfFactory());
        converter.write(new File(outputMappingFile), config.loadProvidedPPMapping());

        OntopSQLOWLAPIConfiguration config2 = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .r2rmlMappingFile(outputMappingFile)
                .ontologyFile(owlFile)
                .propertyFile(propertiesFile)
                .enableTestMode()
                .build();
        config2.loadProvidedPPMapping();
    }
}
