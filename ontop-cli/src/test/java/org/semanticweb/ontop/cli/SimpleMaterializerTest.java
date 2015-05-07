package org.semanticweb.ontop.cli;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

public class SimpleMaterializerTest {

    // TODO We need to extend this test to import the contents of the mappings
    // into OWL and repeat everything taking form OWL

    private OBDADataFactory fac;
    private Connection conn;

    Logger log = LoggerFactory.getLogger(this.getClass());
    private OBDAModel obdaModel;
    private OWLOntology ontology;

    final String owlfile = "src/test/resources/test/simplemapping.owl";
    final String obdafile = "src/test/resources/test/simplemapping.obda";

    @Before
    public void setUp() throws Exception {
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
        // String driver = "org.h2.Driver";
        String url = "jdbc:h2:mem:materialization_test";
        String username = "sa";
        String password = "";

        fac = OBDADataFactoryImpl.getInstance();

        conn = DriverManager.getConnection(url, username, password);
        Statement st = conn.createStatement();

        FileReader reader = new FileReader("src/test/resources/test/simplemapping-create-h2.sql");
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }

        st.executeUpdate(bf.toString());
        conn.commit();

        // Loading the OWL file
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

        // Loading the OBDA data
        obdaModel = fac.getOBDAModel();

        ModelIOManager ioManager = new ModelIOManager(obdaModel);
        ioManager.load(obdafile);

    }

    @After
    public void tearDown() throws Exception {
        dropTables();
        conn.close();
    }

    private void dropTables() throws SQLException, IOException {

        Statement st = conn.createStatement();

        FileReader reader = new FileReader("src/test/resources/test/simplemapping-drop-h2.sql");
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }

        st.executeUpdate(bf.toString());
        st.close();
        conn.commit();
    }

    @Test
    public void runMaterializationWithReasoning() throws Exception {
        String outFile = "src/test/resources/test/out.owl";
        String ontoFile = "src/test/resources/test/simplemapping.owl";
        String mappingFile = "src/test/resources/test/simplemapping.obda";
        OntopMaterialize.main("-obda", mappingFile, "-onto", ontoFile,
                "--out", outFile, "--enable-reasoning");
        assertEquals(5, numOfClassAssertions(outFile));
        assertEquals(0, numOfObjectPropertyAssertions(outFile));
    }

    @Test
    public void runMaterializationWithoutReasoning() throws Exception {
        String outFile = "src/test/resources/test/out.owl";
        String ontoFile = "src/test/resources/test/simplemapping.owl";
        String mappingFile = "src/test/resources/test/simplemapping.obda";
        OntopMaterialize.main("-obda", mappingFile, "-onto", ontoFile,
                "--out", outFile, "--disable-reasoning");
        assertEquals(3, numOfClassAssertions(outFile));
        assertEquals(0, numOfObjectPropertyAssertions(outFile));
    }

    public int numOfClassAssertions(String owlFile) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlFile));
        return ontology.getAxioms(AxiomType.CLASS_ASSERTION).size();
    }

    public int numOfObjectPropertyAssertions(String owlFile) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(owlFile));
        return ontology.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION).size();
    }

}
