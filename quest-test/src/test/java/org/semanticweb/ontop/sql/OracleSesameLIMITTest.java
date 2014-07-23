package org.semanticweb.ontop.sql;

/*
 * #%L
 * ontop-test
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

import static org.junit.Assert.assertTrue;

import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestDBConnection;
import org.semanticweb.ontop.owlrefplatform.core.QuestDBStatement;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLConnection;
import org.semanticweb.ontop.r2rml.R2RMLManager;
import org.semanticweb.ontop.sesame.SesameVirtualRepo;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.TableDefinition;
import org.semanticweb.ontop.sql.api.Attribute;

import java.io.File;

import org.junit.After;
import org.junit.Test;
import org.openrdf.model.Model;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/***
 * Tests that the SPARQL LIMIT statement is correctly translated to WHERE ROWNUM <= x in oracle
 * Tests with both valid versions of the oracle driverClass string in the SourceDeclaration of the obda file
 */
public class OracleSesameLIMITTest  {

	private QuestDBConnection conn;
	private QuestDBStatement qst;

	Logger log = LoggerFactory.getLogger(this.getClass());

	final String owlfile = "resources/oraclesql/o.owl";
	final String r2rmlfile = "resources/oraclesql/o.ttl";

	/*
	 * 	prepare ontop for rewriting and unfolding steps 
	 */
	public void init(String jdbc_driver_class)  throws Exception {

		DBMetadata dbMetadata;
		QuestPreferences preference;
		OWLOntology ontology;
		Model model;

		/*
		 * Load the ontology from an external .owl file.
		 */
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));

		/*
		 * Load the OBDA model from an external .r2rml file
		 */
		R2RMLManager rmanager = new R2RMLManager(r2rmlfile);
		model = rmanager.getModel();
		/*
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
		 */
		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		preference = new QuestPreferences();
		preference.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		preference.setCurrentValueOf(QuestPreferences.REWRITE, "true");
		preference.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "false");
		preference.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE,
				QuestConstants.TW);

		preference.setCurrentValueOf(QuestPreferences.DBNAME, "db");
		preference.setCurrentValueOf(QuestPreferences.JDBC_URL, "jdbc:oracle:thin:@//10.7.20.91:1521/xe");
		preference.setCurrentValueOf(QuestPreferences.DBUSER, "system");
		preference.setCurrentValueOf(QuestPreferences.DBPASSWORD, "obdaps83");
		preference.setCurrentValueOf(QuestPreferences.JDBC_DRIVER, jdbc_driver_class);

		dbMetadata = getMeta(jdbc_driver_class);
		SesameVirtualRepo qest1;

		qest1 = new SesameVirtualRepo("dbname", ontology, model, dbMetadata, preference);
		qest1.initialize();
		/*
		 * Prepare the data connection for querying.
		 */
		conn  = qest1.getQuestConnection();
		qst = conn.createStatement();	
	}

	@After
	public void tearDown() throws Exception{
		qst.close();
		conn.close();
	}

	private TableDefinition defTable(String name){
		TableDefinition tableDefinition = new TableDefinition(name);
		Attribute attribute = null;
		//It starts from 1 !!!
		attribute = new Attribute("country_name", java.sql.Types.VARCHAR, false, null);
		tableDefinition.setAttribute(1, attribute);
		return tableDefinition;
	}
	private DBMetadata getMeta(String driver_class){
		DBMetadata dbMetadata = new DBMetadata();
		dbMetadata.add(defTable("hr.countries"));
		dbMetadata.add(defTable("HR.countries"));
		dbMetadata.setDriverName(driver_class);
		return dbMetadata;
	}


	private void runQuery() throws Exception{

		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x a :Country} LIMIT 10";

		String sql = qst.getSQL(query);
		boolean m = sql.matches("(?ms)(.*)WHERE ROWNUM <= 10(.*)");
		assertTrue(m);
	}


	/**
	 * Test that LIMIT is correctly translated to WHERE ROWNUM <= 10 in oracle
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWithShortDriverString() throws Exception {
		init("oracle.jdbc.OracleDriver");
		runQuery();
	}


	/**
	 * Test that LIMIT is correctly translated to WHERE ROWNUM <= 10 in oracle
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWithLongDriverString() throws Exception {
		init("oracle.jdbc.driver.OracleDriver");
		runQuery();
	}

}

