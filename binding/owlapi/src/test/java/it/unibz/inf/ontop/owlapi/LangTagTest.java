package it.unibz.inf.ontop.owlapi;
/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import static junit.framework.TestCase.*;


/***
 * Reproduces Issue #242.
 *
 * Checks that language tags are handled correctly if they appear in assertions in the .owl file.
 *
 */
public class LangTagTest {

    private static final String owlFile = "src/test/resources/test/langTag/langTag.owl";
    private static final String obdaFile = "src/test/resources/test/langTag/langTag.obda";
    private static final String propertyfile = "src/test/resources/test/langTag/langTag.properties";
    private static final String createTablesFile = "src/test/resources/test/langTag/create-h2.sql";
    private static final String dropTablesFile = "src/test/resources/test/langTag/drop-h2.sql";
    private static final String queryFile = "src/test/resources/test/langTag/query.rq";

    private OntopOWLReasoner reasoner;
    private OWLConnection conn;
    Connection sqlConnection;


    @Before
    public void setUp() throws Exception {

        sqlConnection = DriverManager.getConnection("jdbc:h2:mem:langTag","sa", "sa");
        java.sql.Statement s = sqlConnection.createStatement();
        String text = new Scanner( new File(createTablesFile)).useDelimiter("\\A").next();
        s.execute(text);
        s.close();

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(owlFile)
                .nativeOntopMappingFile(obdaFile)
                .propertyFile(propertyfile)
                .enableOntologyAnnotationQuerying(true)
                .build();

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();

        reasoner = factory.createReasoner(config);
        conn = reasoner.getConnection();
    }

    @After
    public void tearDown() throws Exception {
        try {
            dropTables();
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void dropTables() throws SQLException, IOException {

        Statement st = sqlConnection.createStatement();
        String command = new Scanner( new File(dropTablesFile)).useDelimiter("\\A").next();
        st.executeUpdate(command);
        st.close();
        sqlConnection.commit();
    }

    @Test
    public void runTest() throws Exception {

        String query = new Scanner( new File(queryFile)).useDelimiter("\\A").next();

        OWLStatement st = conn.createStatement();

        try {

            System.out.println(query);

            TupleOWLResultSet rs2 = st.executeSelectQuery(query);
            assertTrue(rs2.hasNext());
            final OWLBindingSet bindingSet = rs2.next();
            IRI iri = bindingSet.getOWLIndividual("instancia").asOWLNamedIndividual().getIRI();
            OWLLiteral lit = bindingSet.getOWLLiteral("comment");
            assertEquals("http://www.basecia.es/ontologia#", iri.getNamespace());
            assertEquals("CuentaContableActivos", iri.getRemainder().get());
            assertEquals("Cuenta bancaria interna de activos.", lit.getLiteral());
            assertEquals("es", lit.getLang());

            assertFalse(rs2.hasNext());

        } catch (Exception e) {
            throw e;
        } finally {
            try {

            } catch (Exception e) {
                st.close();
            }
        }
    }
}
