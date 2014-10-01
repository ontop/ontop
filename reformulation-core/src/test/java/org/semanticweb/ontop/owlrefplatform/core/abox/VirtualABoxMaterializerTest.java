package org.semanticweb.ontop.owlrefplatform.core.abox;

/*
 * #%L
 * ontop-reformulation-core
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.SQLOBDAModel;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.sql.JDBCConnectionManager;

import junit.framework.TestCase;

public class VirtualABoxMaterializerTest extends TestCase {

	OBDADataFactory	fac	= OBDADataFactoryImpl.getInstance();

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testNoSource() throws Exception {
try{
		SQLOBDAModel model = fac.getOBDAModel();

		/*
		 * Setting the database;
		 */

		QuestMaterializer materializer = new QuestMaterializer(model);

		List<Assertion> assertions = materializer.getAssertionList();
		for (Assertion a : assertions) {
//			System.out.println(a.toString());
		}
}catch(Exception e)
{ 	boolean error = e.getMessage().contains("No datasource has been defined.");
	assertEquals(true, error);
}
	}

	public void testOneSource() throws Exception {

		/* Setting the database */
		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump";
		String username = "sa";
		String password = "";

		OBDADataSource source = fac.getDataSource(URI.create("http://www.obda.org/testdb1"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/mapping-test-db.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();

		/*
		 * Setting up the OBDA model and the mappings
		 */

		String sql = "SELECT \"fn\", \"ln\", \"age\", \"schooluri\" FROM \"data\"";

		Predicate q = fac.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(fac.getVariable("fn"));
		headTerms.add(fac.getVariable("ln"));
		headTerms.add(fac.getVariable("age"));
		headTerms.add(fac.getVariable("schooluri"));

		Function head = fac.getFunction(q, headTerms);

		Term objectTerm = fac.getFunction(fac.getPredicate("http://schools.com/persons", 2), fac.getVariable("fn"),
				fac.getVariable("ln"));

		List<Function> body = new LinkedList<Function>();
		Predicate person = fac.getPredicate("Person", 1);
		Predicate fn = fac.getPredicate("fn", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate ln = fac.getPredicate("ln", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate age = fac.getPredicate("age", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate hasschool = fac.getPredicate("hasschool", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		Predicate school = fac.getPredicate("School", 1);
		body.add(fac.getFunction(person, objectTerm));
		body.add(fac.getFunction(fn, objectTerm, fac.getVariable("fn")));
		body.add(fac.getFunction(ln, objectTerm, fac.getVariable("ln")));
		body.add(fac.getFunction(age, objectTerm, fac.getVariable("age")));
		body.add(fac.getFunction(hasschool, objectTerm, fac.getVariable("schooluri")));
		body.add(fac.getFunction(school, fac.getVariable("schooluri")));

		OBDAMappingAxiom map1 = fac.getRDBMSMappingAxiom(sql, fac.getCQIE(head, body));

		SQLOBDAModel model = fac.getOBDAModel();
		model.addSource(source);
		model.addMapping(source.getSourceID(), map1);

		QuestMaterializer materializer = new QuestMaterializer(model);

		List<Assertion> assertions = materializer.getAssertionList();
		assertEquals(0, assertions.size());

		int count = materializer.getTripleCount();
		assertEquals(0, count);

		conn.close();

	}

	public void testTwoSources() throws Exception {
try{
		SQLOBDAModel model = fac.getOBDAModel();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump3";
		String username = "sa";
		String password = "";

		OBDADataSource source = fac.getDataSource(URI.create("http://www.obda.org/testdb3"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/mapping-test-db.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();

		model.addSource(source);

		OBDADataSource source2 = fac.getDataSource(URI.create("http://www.obda.org/testdb2"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
		model.addSource(source2);

		/*
		 * Setting up the OBDA model and the mappings
		 */

		String sql = "SELECT \"fn\", \"ln\", \"age\", \"schooluri\" FROM \"data\"";

		Predicate q = fac.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(fac.getVariable("fn"));
		headTerms.add(fac.getVariable("ln"));
		headTerms.add(fac.getVariable("age"));
		headTerms.add(fac.getVariable("schooluri"));

		Function head = fac.getFunction(q, headTerms);

		Term objectTerm = fac.getFunction(fac.getPredicate("http://schools.com/persons", 2), fac.getVariable("fn"),
				fac.getVariable("ln"));

		List<Function> body = new LinkedList<Function>();
		Predicate person = fac.getPredicate("Person", 1);
		Predicate fn = fac.getPredicate("fn", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate ln = fac.getPredicate("ln", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate age = fac.getPredicate("age", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate hasschool = fac.getPredicate("hasschool", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		Predicate school = fac.getPredicate("School", 1);
		body.add(fac.getFunction(person, objectTerm));
		body.add(fac.getFunction(fn, objectTerm, fac.getVariable("fn")));
		body.add(fac.getFunction(ln, objectTerm, fac.getVariable("ln")));
		body.add(fac.getFunction(age, objectTerm, fac.getVariable("age")));
		body.add(fac.getFunction(hasschool, objectTerm, fac.getVariable("schooluri")));
		body.add(fac.getFunction(school, fac.getVariable("schooluri")));

		OBDAMappingAxiom map1 = fac.getRDBMSMappingAxiom(sql, fac.getCQIE(head, body));

		model.addMapping(source.getSourceID(), map1);
		model.addMapping(source2.getSourceID(), map1);

		QuestMaterializer materializer = new QuestMaterializer(model);

		List<Assertion> assertions = materializer.getAssertionList();
		
		conn.close();
		
} catch(Exception e) {
	assertEquals("Cannot materialize with multiple data sources!", e.getMessage());
}

	}

	public void testThreeSources() throws Exception {
try{
		SQLOBDAModel model = fac.getOBDAModel();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump4";
		String username = "sa";
		String password = "";

		OBDADataSource source = fac.getDataSource(URI.create("http://www.obda.org/testdb4"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/mapping-test-db.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();

		model.addSource(source);

		OBDADataSource source2 = fac.getDataSource(URI.create("http://www.obda.org/testdb5"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
		model.addSource(source2);

		OBDADataSource source3 = fac.getDataSource(URI.create("http://www.obda.org/testdb6"));
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source3.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source3.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
		model.addSource(source3);

		/*
		 * Setting up the OBDA model and the mappings
		 */

		String sql = "SELECT \"fn\", \"ln\", \"age\", \"schooluri\" FROM \"data\"";

		Predicate q = fac.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(fac.getVariable("fn"));
		headTerms.add(fac.getVariable("ln"));
		headTerms.add(fac.getVariable("age"));
		headTerms.add(fac.getVariable("schooluri"));

		Function head = fac.getFunction(q, headTerms);

		Term objectTerm = fac.getFunction(fac.getPredicate("http://schools.com/persons", 2), fac.getVariable("fn"),
				fac.getVariable("ln"));

		List<Function> body = new LinkedList<Function>();
		Predicate person = fac.getPredicate("Person", 1);
		Predicate fn = fac.getPredicate("fn", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate ln = fac.getPredicate("ln", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate age = fac.getPredicate("age", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate hasschool = fac.getPredicate("hasschool", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		Predicate school = fac.getPredicate("School", 1);
		body.add(fac.getFunction(person, objectTerm));
		body.add(fac.getFunction(fn, objectTerm, fac.getVariable("fn")));
		body.add(fac.getFunction(ln, objectTerm, fac.getVariable("ln")));
		body.add(fac.getFunction(age, objectTerm, fac.getVariable("age")));
		body.add(fac.getFunction(hasschool, objectTerm, fac.getVariable("schooluri")));
		body.add(fac.getFunction(school, fac.getVariable("schooluri")));

		OBDAMappingAxiom map1 = fac.getRDBMSMappingAxiom(sql, fac.getCQIE(head, body));

		model.addMapping(source.getSourceID(), map1);
		model.addMapping(source2.getSourceID(), map1);
		model.addMapping(source3.getSourceID(), map1);

		QuestMaterializer materializer = new QuestMaterializer(model);

		List<Assertion> assertions = materializer.getAssertionList();
		for (Assertion a : assertions) {
//			System.out.println(a.toString());
		}
} catch(Exception e) {
	assertEquals("Cannot materialize with multiple data sources!", e.getMessage());
}
	}

	public void testThreeSourcesNoMappings() throws Exception {
try{
		SQLOBDAModel model = fac.getOBDAModel();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump7";
		String username = "sa";
		String password = "";

		OBDADataSource source = fac.getDataSource(URI.create("http://www.obda.org/testdb7"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/mapping-test-db.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();

		model.addSource(source);

		OBDADataSource source2 = fac.getDataSource(URI.create("http://www.obda.org/testdb8"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
		model.addSource(source2);

		OBDADataSource source3 = fac.getDataSource(URI.create("http://www.obda.org/testdb9"));
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source3.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source3.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
		model.addSource(source3);

		QuestMaterializer materializer = new QuestMaterializer(model);

		List<Assertion> assertions = materializer.getAssertionList();
		for (Assertion a : assertions) {
//			System.out.println(a.toString());
		}
} catch(Exception e) {
	assertEquals("Cannot materialize with multiple data sources!", e.getMessage());
}
	}

	public void testThreeSourcesNoMappingsFor1And3() throws Exception {
try{
		SQLOBDAModel model = fac.getOBDAModel();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump5";
		String username = "sa";
		String password = "";

		OBDADataSource source = fac.getDataSource(URI.create("http://www.obda.org/testdb11"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/mapping-test-db.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();

		model.addSource(source);

		OBDADataSource source2 = fac.getDataSource(URI.create("http://www.obda.org/testdb12"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
		model.addSource(source2);

		OBDADataSource source3 = fac.getDataSource(URI.create("http://www.obda.org/testdb13"));
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source3.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source3.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
		model.addSource(source3);

		/*
		 * Setting up the OBDA model and the mappings
		 */

		String sql = "SELECT \"fn\", \"ln\", \"age\", \"schooluri\" FROM \"data\"";

		Predicate q = fac.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(fac.getVariable("fn"));
		headTerms.add(fac.getVariable("ln"));
		headTerms.add(fac.getVariable("age"));
		headTerms.add(fac.getVariable("schooluri"));

		Function head = fac.getFunction(q, headTerms);

		Term objectTerm = fac.getFunction(fac.getPredicate("http://schools.com/persons", 2), fac.getVariable("fn"),
				fac.getVariable("ln"));

		List<Function> body = new LinkedList<Function>();
		Predicate person = fac.getPredicate("Person", 1);
		Predicate fn = fac.getPredicate("fn", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate ln = fac.getPredicate("ln", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate age = fac.getPredicate("age", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate hasschool = fac.getPredicate("hasschool", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		Predicate school = fac.getPredicate("School", 1);
		body.add(fac.getFunction(person, objectTerm));
		body.add(fac.getFunction(fn, objectTerm, fac.getVariable("fn")));
		body.add(fac.getFunction(ln, objectTerm, fac.getVariable("ln")));
		body.add(fac.getFunction(age, objectTerm, fac.getVariable("age")));
		body.add(fac.getFunction(hasschool, objectTerm, fac.getVariable("schooluri")));
		body.add(fac.getFunction(school, fac.getVariable("schooluri")));

		OBDAMappingAxiom map1 = fac.getRDBMSMappingAxiom(sql, fac.getCQIE(head, body));

		model.addMapping(source2.getSourceID(), map1);
		
		QuestMaterializer materializer = new QuestMaterializer(model);
	
		List<Assertion> assertions = materializer.getAssertionList();
		for (Assertion a : assertions) {
//			System.out.println(a.toString());
		}
} catch(Exception e) {
	assertEquals("Cannot materialize with multiple data sources!", e.getMessage());
}
	}
	
	public void testMultipleMappingsOneSource() throws Exception {

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump10";
		String username = "sa";
		String password = "";

		OBDADataSource source = fac.getDataSource(URI.create("http://www.obda.org/testdb100"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/test/mapping-test-db.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		conn.commit();

		/*
		 * Setting up the OBDA model and the mappings
		 */

		String sql1 = "SELECT \"fn\", \"ln\" FROM \"data\"";
		String sql2 = "SELECT \"fn\", \"ln\" FROM \"data\"";
		String sql3 = "SELECT \"fn\", \"ln\" FROM \"data\"";
		String sql4 = "SELECT \"fn\", \"ln\", \"age\" FROM \"data\"";
		String sql5 = "SELECT \"fn\", \"ln\", \"schooluri\" FROM \"data\"";
		String sql6 = "SELECT \"fn\", \"ln\", \"schooluri\" FROM \"data\"";

		Predicate q = fac.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		
		final Term firstNameVariable = fac.getFunction(fac.getDataTypePredicateString(), fac.getVariable("fn"));
		final Term lastNameVariable = fac.getFunction(fac.getDataTypePredicateString(), fac.getVariable("ln"));
		final Term ageVariable = fac.getFunction(fac.getDataTypePredicateInteger(), fac.getVariable("age"));
		final Term schoolUriVariable = fac.getFunction(fac.getDataTypePredicateString(), fac.getVariable("schooluri"));
		
		headTerms.add(firstNameVariable);
		headTerms.add(lastNameVariable);
		headTerms.add(ageVariable);
		headTerms.add(schoolUriVariable);

		Function head = fac.getFunction(q, headTerms);

		Term objectTerm = fac.getFunction(fac.getUriTemplatePredicate(2),
				fac.getConstantLiteral("http://schools.com/persons{}{}"), 
				firstNameVariable,
				lastNameVariable);

//		List<Function> body = new LinkedList<Function>();
		Predicate person = fac.getClassPredicate("Person");
		Predicate fn = fac.getDataPropertyPredicate("firstn");
		Predicate ln = fac.getDataPropertyPredicate("lastn");
		Predicate age = fac.getDataPropertyPredicate("agee");
		Predicate hasschool = fac.getDataPropertyPredicate("hasschool");
		Predicate school = fac.getClassPredicate("School");
//		body.add(fac.getFunctionalTerm(person, objectTerm));
//		body.add(fac.getFunctionalTerm(fn, objectTerm, fac.getVariable("fn")));
//		body.add(fac.getFunctionalTerm(ln, objectTerm, fac.getVariable("ln")));
//		body.add(fac.getFunctionalTerm(age, objectTerm, fac.getVariable("age")));
//		body.add(fac.getFunctionalTerm(hasschool, objectTerm, fac.getVariable("schooluri")));
//		body.add(fac.getFunctionalTerm(school, fac.getVariable("schooluri")));

		
		OBDAMappingAxiom map1 = fac.getRDBMSMappingAxiom(sql1, fac.getCQIE(head, fac.getFunction(person, objectTerm)));
		OBDAMappingAxiom map2 = fac.getRDBMSMappingAxiom(sql2, fac.getCQIE(head, fac.getFunction(fn, objectTerm, firstNameVariable)));
		OBDAMappingAxiom map3 = fac.getRDBMSMappingAxiom(sql3, fac.getCQIE(head, fac.getFunction(ln, objectTerm, lastNameVariable)));
		OBDAMappingAxiom map4 = fac.getRDBMSMappingAxiom(sql4, fac.getCQIE(head, fac.getFunction(age, objectTerm, ageVariable)));
		OBDAMappingAxiom map5 = fac.getRDBMSMappingAxiom(sql5, fac.getCQIE(head, fac.getFunction(hasschool, objectTerm, schoolUriVariable)));
		OBDAMappingAxiom map6 = fac.getRDBMSMappingAxiom(sql6, fac.getCQIE(head, fac.getFunction(school, schoolUriVariable)));

		SQLOBDAModel model = fac.getOBDAModel();
		model.addSource(source);
		model.addMapping(source.getSourceID(), map1);
		model.addMapping(source.getSourceID(), map2);
		model.addMapping(source.getSourceID(), map3);
		model.addMapping(source.getSourceID(), map4);
		model.addMapping(source.getSourceID(), map5);
		model.addMapping(source.getSourceID(), map6);
		
		QuestMaterializer materializer = new QuestMaterializer(model);

		List<Assertion> assertions = materializer.getAssertionList();
		int count = materializer.getTripleCount();
		
		assertEquals(0, count);

		conn.close();

	}
}
