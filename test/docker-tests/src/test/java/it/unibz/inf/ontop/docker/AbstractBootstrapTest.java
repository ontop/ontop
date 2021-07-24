package it.unibz.inf.ontop.docker;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.spec.mapping.bootstrap.DirectMappingBootstrapper;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class AbstractBootstrapTest {

    /**
     *
     * @param owlFile
     * @param obdaFile
     * @param propertyFile
     */
    public static OWLStatement loadGeneratedFiles(String owlFile, String obdaFile, String propertyFile) {

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlFile)
                .nativeOntopMappingFile(obdaFile)
                .propertyFile(propertyFile)
                .enableTestMode()
                .build();
        try {
            OntopOWLReasoner reasoner = factory.createReasoner(config);
            OWLConnection conn = reasoner.getConnection();
            return conn.createStatement();
        }
        catch (OWLException e) {
            throw new RuntimeException("Error occurred while loading bootstrapped files: " + e);
        }
    }

    /**
     *
     * @param propertyFile
     * @param baseIRI
     * @param outputOwlFile
     * @param outputObdaFile
     * @throws Exception
     */
    public static void bootstrap(String propertyFile, String baseIRI, String outputOwlFile, String outputObdaFile) throws Exception {
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .propertyFile(propertyFile)
                .build();

        DirectMappingBootstrapper bootstrapper = DirectMappingBootstrapper.defaultBootstrapper();
        DirectMappingBootstrapper.BootstrappingResults results = bootstrapper.bootstrap(config, baseIRI);

        File obdaFile = new File(outputObdaFile);
        OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
        writer.write(obdaFile, results.getPPMapping());

        File ontologyFile = new File(outputOwlFile);
        OWLOntology onto = results.getOntology();
        onto.getOWLOntologyManager().saveOntology(onto, new FileDocumentTarget(ontologyFile));
    }

    private Connection connection;

    protected void createTables(String sqlFile, String jdbcUrl, String dbUser, String dbPassword) throws IOException, SQLException {
        connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(readFile(sqlFile));
        }
        connection.commit();
    }

    protected void dropTables(String sqlFile) throws SQLException, IOException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(readFile(sqlFile));
        }
        connection.commit();
        connection.close();
    }

    private static String readFile(String file) throws IOException {
        try (FileReader reader = new FileReader(file);
             BufferedReader in = new BufferedReader(reader)) {
            StringBuilder bf = new StringBuilder();
            String line = in.readLine();
            while (line != null) {
                bf.append(line);
                line = in.readLine();
            }
            return bf.toString();
        }
    }
}
