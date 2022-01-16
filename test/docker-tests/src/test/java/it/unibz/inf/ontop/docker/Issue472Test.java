package it.unibz.inf.ontop.docker;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.R2RMLMappingSerializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Issue472Test {
    private static final String owlFile = "src/test/resources/issue472/ontology.owl";
    private static final String obdaFile = "src/test/resources/issue472/mapping.obda";
    private static final String propertiesFile = "src/test/resources/issue472/mapping.properties";
    private static final String databaseFile = "src/test/resources/issue472/database.sql";

    @BeforeClass
    public static void before() throws OWLOntologyCreationException, SQLException {

        Connection sqlConnection = DriverManager.getConnection("jdbc:h2:mem:questjunitdb", "sa", "");
        try (java.sql.Statement s = sqlConnection.createStatement()) {
            String text = new Scanner(new File(databaseFile)).useDelimiter("\\A").next();
            s.execute(text);
        }
        catch (SQLException | FileNotFoundException e) {
            System.out.println("Exception in creating db from script:" + e);
        }
    }

    @AfterClass
    public static void after() throws OWLException {
    }

    @Test
    public void test_load() throws Exception {
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFile)
                .ontologyFile(owlFile)
                .propertyFile(propertiesFile)
                .enableTestMode()
                .build();

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        factory.createReasoner(config);
    }
}
