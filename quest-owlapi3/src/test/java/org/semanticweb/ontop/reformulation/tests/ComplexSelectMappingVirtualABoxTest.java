package org.semanticweb.ontop.reformulation.tests;

/*
 * #%L
 * ontop-quest-owlapi3
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

import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
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
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for CONCAT and REPLACE in SQL query
 */

public class ComplexSelectMappingVirtualABoxTest  {

	private OBDADataFactory fac;
	private Connection conn;

	String query = null;
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/complexmapping.owl";
	final String obdafile = "src/test/resources/test/complexmapping.obda";

	@Before
	public void setUp() throws Exception {
		
		
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
		// String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questjunitdb";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/complexmapping-create-h2.sql");
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

//   test for self join count the number of occurrences
	private String runTests(Properties p) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

        OWLLiteral val;
		try {
			String sql = st.getUnfolding(this.query);
			Pattern pat = Pattern.compile("TABLE1 ");
		    Matcher m = pat.matcher(sql);
		    int num_joins = 0;
		    while (m.find()){
		    	num_joins +=1;
		    }
//		    System.out.println(sql);
			assertEquals("Self-join detected in: \n" + sql, 1, num_joins);
			QuestOWLResultSet rs = st.executeTuple(query);
			assertTrue(rs.nextRow());
			OWLIndividual ind1 = rs.getOWLIndividual("x");
			val = rs.getOWLLiteral("z");
			assertEquals("<http://it.unibz.krdb/obda/test/simple#uri%201>", ind1.toString());

			//assertEquals("\"value1\"", val.toString());

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
				throw e;
			}
			conn.close();
			reasoner.dispose();
		}
        return val.toString();
	}

    @Test
	public void testReplace() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);

		this.query = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x :U ?z. }";

        String val = runTests(p);
        assertEquals("\"value1\"", val);

	}

    @Test
    public void testReplaceValue() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        this.query = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x :U4 ?z . }";

        String val = runTests(p);
        assertEquals("\"ualue1\"", val);
    }

    @Test
	public void testConcat() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		this.query = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x :U2 ?z. }";

        String val = runTests(p);
        assertEquals("\"NO value1\"", val);
	}

    @Test
	public void testDoubleConcat() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

		this.query = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x :U2 ?z; :U3 ?w. }";

        String val = runTests(p);
        assertEquals("\"NO value1\"", val);
	}

    @Test
    public void testConcat2() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

        this.query = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x :U5 ?z. }";

        String val = runTests(p);
        assertEquals("\"value1test\"^^xsd:string", val);
    }

    @Test
    public void testConcat3() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

        this.query = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x :U6 ?z. }";

        String val = runTests(p);
        assertEquals("\"value1test\"", val);
    }

    @Test
    public void testConcat4() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

        this.query = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x :U7 ?z. }";

        String val = runTests(p);
        assertEquals("\"value1touri 1\"^^xsd:string", val);
    }

    @Test
    public void testConcat5() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

        this.query = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x :U8 ?z. }";

        String val = runTests(p);
        assertEquals("\"value1test\"", val);
    }

    @Test
    public void testConcatAndReplaceUri() throws Exception {

        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

        this.query = "PREFIX : <http://it.unibz.krdb/obda/test/simple#> SELECT * WHERE { ?x :U9 ?z. }";

        String val = runTests(p);
        assertEquals("\"value1\"^^xsd:string", val);
    }


	
}
