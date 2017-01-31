package it.unibz.inf.ontop.sql;

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

import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBConnection;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBStatement;
import it.unibz.inf.ontop.injection.QuestCoreSettings;
import it.unibz.inf.ontop.rdf4j.repository.OntopVirtualRepository;

import java.util.Properties;

import org.junit.After;
import org.junit.Test;

/***
 * Tests that the SPARQL LIMIT statement is correctly translated to WHERE ROWNUM <= x in oracle
 * Tests with both valid versions of the oracle driverClass string in the SourceDeclaration of the obda file
 */
public class OracleSesameLIMITTest  {

	private QuestDBConnection conn;
	private QuestDBStatement qst;

	final String owlfile = "resources/oraclesql/o.owl";
	final String r2rmlfile = "resources/oraclesql/o.ttl";

	/*
	 * 	prepare ontop for rewriting and unfolding steps 
	 */
	public void init(String jdbc_driver_class)  throws Exception {
		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		Properties p = new Properties();
		p.setProperty(QuestCoreSettings.JDBC_NAME, "db");
		p.setProperty(QuestCoreSettings.JDBC_URL, "jdbc:oracle:thin:@//10.7.20.91:1521/xe");
		p.setProperty(QuestCoreSettings.JDBC_USER, "system");
		p.setProperty(QuestCoreSettings.JDBC_PASSWORD, "obdaps83");
		p.setProperty(QuestCoreSettings.JDBC_DRIVER, jdbc_driver_class);
		p.put(OntopOBDASettings.OPTIMIZE_EQUIVALENCES, false);

		QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
				.enableExistentialReasoning(true)
				.dbMetadata(getMeta(jdbc_driver_class))
				.ontologyFile(owlfile)
				.r2rmlMappingFile(r2rmlfile)
				.properties(p)
				.build();

		OntopVirtualRepository qest1 = new OntopVirtualRepository("dbname", configuration);
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

	private void defTable(RDBMetadata dbMetadata, String schema, String name) {
		QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
		DatabaseRelationDefinition tableDefinition = dbMetadata.createDatabaseRelation(idfac.createRelationID(schema, name));
		tableDefinition.addAttribute(idfac.createAttributeID("country_name"), java.sql.Types.VARCHAR, null, false);
	}
	private RDBMetadata getMeta(String driver_class){
		RDBMetadata dbMetadata = RDBMetadataExtractionTools.createDummyMetadata(driver_class);
		QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
		defTable(dbMetadata, "hr", "countries");
		defTable(dbMetadata, "HR", "countries");
		dbMetadata.createDatabaseRelation(idfac.createRelationID(null, "dual"));
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

