package it.unibz.inf.ontop.reformulation.tests;

/*
 * #%L
 * ontop-quest-owlapi
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;


@Ignore
public class MarriageTest {

	private Connection conn;;

	private static final String ONTOLOGY_FILE = "src/test/resources/marriage/marriage.ttl";
	private static final String OBDA_FILE = "src/test/resources/marriage/marriage.obda";
    private static final String CREATE_DB_FILE = "src/test/resources/marriage/create-db.sql";
    private static final String DROP_DB_FILE = "src/test/resources/marriage/drop-db.sql";
	private static final String JDBC_URL = "jdbc:h2:mem:questjunitdb";
	private static final String JDBC_USER = "sa";
	private static final String JDBC_PASSWORD = "";


    @Before
	public void setUp() throws Exception {

		conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);


		Statement st = conn.createStatement();

		FileReader reader = new FileReader(CREATE_DB_FILE);
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}
		in.close();

		st.executeUpdate(bf.toString());
		conn.commit();
	}

	@After
	public void tearDown() throws Exception {

		  dropTables();
			conn.close();

	}

	private void dropTables() throws SQLException, IOException {

		Statement st = conn.createStatement();

		FileReader reader = new FileReader(DROP_DB_FILE);
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}
		in.close();

		st.executeUpdate(bf.toString());
		st.close();
		conn.commit();
	}


    /**
     * Tests that all the persons are not married.
     *
     * This test has been added to make sure the mapping saturation handles correctly domain
     * and nullable columns.
     *
     */
	@Test
    public void testSpouse() throws Exception {
        String queryBind = "PREFIX : <http://example.org/marriage/voc#>\n" +
                "\n" +
                "SELECT ?x \n" +
                "WHERE {\n" +
                "  ?x a :Spouse .\n" +
                "}";

        ImmutableList<String> expectedValues = ImmutableList.of(
                "http://example.com/person/1",
                "http://example.com/person/2"
        );
        checkReturnedValues(queryBind, expectedValues);
    }

    private void checkReturnedValues(String query, List<String> expectedValues) throws Exception {

		QuestOWLFactory factory = new QuestOWLFactory();
		OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
				.nativeOntopMappingFile(OBDA_FILE)
				.ontologyFile(ONTOLOGY_FILE)
				.jdbcUrl(JDBC_URL)
				.jdbcUser(JDBC_USER)
				.jdbcPassword(JDBC_PASSWORD)
				.build();
		QuestOWL reasoner = factory.createReasoner(config);


        // Now we are ready for querying
        OntopOWLConnection conn = reasoner.getConnection();
        OntopOWLStatement st = conn.createStatement();

        int i = 0;
        List<String> returnedValues = new ArrayList<>();
        try {
            QuestOWLResultSet rs = st.executeSelectQuery(query);
            while (rs.nextRow()) {
                OWLObject ind1 = rs.getOWLObject("x");
                // log.debug(ind1.toString());
                returnedValues.add(ind1.toString());
                System.out.println(ind1);
                i++;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            conn.close();
            reasoner.dispose();
        }
        assertTrue(String.format("%s instead of \n %s", returnedValues.toString(), expectedValues.toString()),
                returnedValues.equals(expectedValues));
        assertTrue(String.format("Wrong size: %d (expected %d)", i, expectedValues.size()), expectedValues.size() == i);

    }


}
