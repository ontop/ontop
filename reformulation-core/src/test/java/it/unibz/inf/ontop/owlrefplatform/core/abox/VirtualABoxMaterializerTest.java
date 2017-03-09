package it.unibz.inf.ontop.owlrefplatform.core.abox;

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

import it.unibz.inf.ontop.model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.impl.MappingFactoryImpl;
import it.unibz.inf.ontop.model.impl.RDBMSourceParameterConstants;
import it.unibz.inf.ontop.ontology.Assertion;
import it.unibz.inf.ontop.ontology.impl.OntologyVocabularyImpl;
import it.unibz.inf.ontop.injection.QuestCoreConfiguration;
import it.unibz.inf.ontop.sql.JDBCConnectionManager;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

public class VirtualABoxMaterializerTest extends TestCase {

	private static final MappingFactory MAPPING_FACTORY = MappingFactoryImpl.getInstance();
	private final NativeQueryLanguageComponentFactory nativeQLFactory;
	private final OBDAFactoryWithException obdaFactory;

    public VirtualABoxMaterializerTest() {
        QuestCoreConfiguration configuration = QuestCoreConfiguration.defaultBuilder().build();
		Injector injector = configuration.getInjector();
        nativeQLFactory = injector.getInstance(NativeQueryLanguageComponentFactory.class);
        obdaFactory = injector.getInstance(OBDAFactoryWithException.class);
    }

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testNoSource() throws Exception {
        try{
                OBDAModel model = obdaFactory.createOBDAModel(new HashSet<OBDADataSource>(),
						new HashMap<URI, ImmutableList<OBDAMappingAxiom>>(),
						nativeQLFactory.create(new HashMap<String, String>()),
						new OntologyVocabularyImpl()
						);

		/*
		 * Setting the database;
		 */

		QuestMaterializer materializer = new QuestMaterializer(model, false);

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

		OBDADataSource source = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb1"));
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
		in.close();

		st.executeUpdate(bf.toString());
		conn.commit();

		/*
		 * Setting up the OBDA model and the mappings
		 */

		String sql = "SELECT \"fn\", \"ln\", \"age\", \"schooluri\" FROM \"data\"";

		Predicate q = DATA_FACTORY.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(DATA_FACTORY.getVariable("fn"));
		headTerms.add(DATA_FACTORY.getVariable("ln"));
		headTerms.add(DATA_FACTORY.getVariable("age"));
		headTerms.add(DATA_FACTORY.getVariable("schooluri"));

		Function head = DATA_FACTORY.getFunction(q, headTerms);

		Term objectTerm = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("http://schools.com/persons", 2), DATA_FACTORY.getVariable("fn"),
				DATA_FACTORY.getVariable("ln"));

		List<Function> body = new LinkedList<Function>();
		Predicate person = DATA_FACTORY.getClassPredicate("Person");
		Predicate fn = DATA_FACTORY.getDataPropertyPredicate("fn", COL_TYPE.LITERAL);
		Predicate ln = DATA_FACTORY.getDataPropertyPredicate("ln", COL_TYPE.LITERAL);
		Predicate age = DATA_FACTORY.getDataPropertyPredicate("age", COL_TYPE.LITERAL);
		Predicate hasschool = DATA_FACTORY.getObjectPropertyPredicate("hasschool");
		Predicate school = DATA_FACTORY.getClassPredicate("School");
		body.add(DATA_FACTORY.getFunction(person, objectTerm));
		body.add(DATA_FACTORY.getFunction(fn, objectTerm, DATA_FACTORY.getVariable("fn")));
		body.add(DATA_FACTORY.getFunction(ln, objectTerm, DATA_FACTORY.getVariable("ln")));
		body.add(DATA_FACTORY.getFunction(age, objectTerm, DATA_FACTORY.getVariable("age")));
		body.add(DATA_FACTORY.getFunction(hasschool, objectTerm, DATA_FACTORY.getVariable("schooluri")));
		body.add(DATA_FACTORY.getFunction(school, DATA_FACTORY.getVariable("schooluri")));

		OBDAMappingAxiom map1 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql), body);

        Set<OBDADataSource> dataSources = new HashSet<>();
        Map<URI, ImmutableList<OBDAMappingAxiom>> mappings = new HashMap<>();

        //model.addSource(source);
        //model.addMapping(source.getSourceID(), map1);
        dataSources.add(source);
        mappings.put(source.getSourceID(), ImmutableList.of(map1));

        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
		OBDAModel model = obdaFactory.createOBDAModel(dataSources, mappings, prefixManager, new OntologyVocabularyImpl());

        //TODO: remove this dangerous cast
		QuestMaterializer materializer = new QuestMaterializer(model, false);

		List<Assertion> assertions = materializer.getAssertionList();
		assertEquals(0, assertions.size());

		int count = materializer.getTripleCount();
		assertEquals(0, count);

		conn.close();

	}

	public void testTwoSources() throws Exception {
        try{
            final Set<OBDADataSource> dataSources = new HashSet<>();
            final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();

            /*
             * Setting the database;
             */

            String driver = "org.h2.Driver";
            String url = "jdbc:h2:mem:aboxdump3";
            String username = "sa";
            String password = "";

            OBDADataSource source = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb3"));
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
		in.close();

            st.executeUpdate(bf.toString());
            conn.commit();

            dataSources.add(source);

            OBDADataSource source2 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb2"));
            source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
            source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
            source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
            source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
            source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
            source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
            dataSources.add(source2);

            /*
             * Setting up the OBDA model and the mappings
             */

            String sql = "SELECT \"fn\", \"ln\", \"age\", \"schooluri\" FROM \"data\"";

            Predicate q = DATA_FACTORY.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
            List<Term> headTerms = new LinkedList<Term>();
            headTerms.add(DATA_FACTORY.getVariable("fn"));
            headTerms.add(DATA_FACTORY.getVariable("ln"));
            headTerms.add(DATA_FACTORY.getVariable("age"));
            headTerms.add(DATA_FACTORY.getVariable("schooluri"));

            Function head = DATA_FACTORY.getFunction(q, headTerms);

            Term objectTerm = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("http://schools.com/persons", 2), DATA_FACTORY.getVariable("fn"),
                    DATA_FACTORY.getVariable("ln"));

		List<Function> body = new LinkedList<Function>();
		Predicate person = DATA_FACTORY.getClassPredicate("Person");
		Predicate fn = DATA_FACTORY.getDataPropertyPredicate("fn", COL_TYPE.LITERAL);
		Predicate ln = DATA_FACTORY.getDataPropertyPredicate("ln", COL_TYPE.LITERAL);
		Predicate age = DATA_FACTORY.getDataPropertyPredicate("age", COL_TYPE.LITERAL);
		Predicate hasschool = DATA_FACTORY.getObjectPropertyPredicate("hasschool");
		Predicate school = DATA_FACTORY.getClassPredicate("School");
		body.add(DATA_FACTORY.getFunction(person, objectTerm));
		body.add(DATA_FACTORY.getFunction(fn, objectTerm, DATA_FACTORY.getVariable("fn")));
		body.add(DATA_FACTORY.getFunction(ln, objectTerm, DATA_FACTORY.getVariable("ln")));
		body.add(DATA_FACTORY.getFunction(age, objectTerm, DATA_FACTORY.getVariable("age")));
		body.add(DATA_FACTORY.getFunction(hasschool, objectTerm, DATA_FACTORY.getVariable("schooluri")));
		body.add(DATA_FACTORY.getFunction(school, DATA_FACTORY.getVariable("schooluri")));

            OBDAMappingAxiom map1 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql), body);

            mappingIndex.put(source.getSourceID(), ImmutableList.of(map1));
            mappingIndex.put(source2.getSourceID(), ImmutableList.of(map1));

            PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
            OBDAModel model = obdaFactory.createOBDAModel(dataSources, mappingIndex, prefixManager,
					new OntologyVocabularyImpl());

            QuestMaterializer materializer = new QuestMaterializer(model, false);

            List<Assertion> assertions = materializer.getAssertionList();

            conn.close();

        } catch (DuplicateMappingException e) {
        }
        catch(Exception e) {
            assertEquals("Cannot materialize with multiple data sources!", e.getMessage());
        }

	}

	public void testThreeSources() throws Exception {
    try{
        final Set<OBDADataSource> dataSources = new HashSet<>();
        final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump4";
		String username = "sa";
		String password = "";

		OBDADataSource source = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb4"));
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
		in.close();

		st.executeUpdate(bf.toString());
		conn.commit();

        dataSources.add(source);

		OBDADataSource source2 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb5"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
        dataSources.add(source2);

		OBDADataSource source3 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb6"));
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source3.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source3.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
        dataSources.add(source3);

		/*
		 * Setting up the OBDA model and the mappings
		 */

		String sql = "SELECT \"fn\", \"ln\", \"age\", \"schooluri\" FROM \"data\"";

		Predicate q = DATA_FACTORY.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(DATA_FACTORY.getVariable("fn"));
		headTerms.add(DATA_FACTORY.getVariable("ln"));
		headTerms.add(DATA_FACTORY.getVariable("age"));
		headTerms.add(DATA_FACTORY.getVariable("schooluri"));

		Function head = DATA_FACTORY.getFunction(q, headTerms);

		Term objectTerm = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("http://schools.com/persons", 2), DATA_FACTORY.getVariable("fn"),
				DATA_FACTORY.getVariable("ln"));

		List<Function> body = new LinkedList<Function>();
		Predicate person = DATA_FACTORY.getClassPredicate("Person");
		Predicate fn = DATA_FACTORY.getDataPropertyPredicate("fn", COL_TYPE.LITERAL);
		Predicate ln = DATA_FACTORY.getDataPropertyPredicate("ln", COL_TYPE.LITERAL);
		Predicate age = DATA_FACTORY.getDataPropertyPredicate("age", COL_TYPE.LITERAL);
		Predicate hasschool = DATA_FACTORY.getObjectPropertyPredicate("hasschool");
		Predicate school = DATA_FACTORY.getClassPredicate("School");
		body.add(DATA_FACTORY.getFunction(person, objectTerm));
		body.add(DATA_FACTORY.getFunction(fn, objectTerm, DATA_FACTORY.getVariable("fn")));
		body.add(DATA_FACTORY.getFunction(ln, objectTerm, DATA_FACTORY.getVariable("ln")));
		body.add(DATA_FACTORY.getFunction(age, objectTerm, DATA_FACTORY.getVariable("age")));
		body.add(DATA_FACTORY.getFunction(hasschool, objectTerm, DATA_FACTORY.getVariable("schooluri")));
		body.add(DATA_FACTORY.getFunction(school, DATA_FACTORY.getVariable("schooluri")));

		OBDAMappingAxiom map1 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql), body);

        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
        OBDAModel model = obdaFactory.createOBDAModel(dataSources, mappingIndex, prefixManager, new OntologyVocabularyImpl());

		QuestMaterializer materializer = new QuestMaterializer(model, false);

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
        final Set<OBDADataSource> dataSources = new HashSet<>();
        final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump7";
		String username = "sa";
		String password = "";

		OBDADataSource source = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb7"));
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
		in.close();

		st.executeUpdate(bf.toString());
		conn.commit();

        dataSources.add(source);

		OBDADataSource source2 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb8"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
        dataSources.add(source2);

		OBDADataSource source3 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb9"));
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source3.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source3.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
        dataSources.add(source3);

        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
        OBDAModel model = obdaFactory.createOBDAModel(dataSources, mappingIndex, prefixManager, new OntologyVocabularyImpl());
		QuestMaterializer materializer = new QuestMaterializer(model, false);

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
        final Set<OBDADataSource> dataSources = new HashSet<>();
        final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump5";
		String username = "sa";
		String password = "";

		OBDADataSource source = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb11"));
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
		in.close();

		st.executeUpdate(bf.toString());
		conn.commit();

		dataSources.add(source);

		OBDADataSource source2 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb12"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
        dataSources.add(source2);

		OBDADataSource source3 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb13"));
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source3.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source3.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
        dataSources.add(source3);

		/*
		 * Setting up the OBDA model and the mappings
		 */

		String sql = "SELECT \"fn\", \"ln\", \"age\", \"schooluri\" FROM \"data\"";

		Predicate q = DATA_FACTORY.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(DATA_FACTORY.getVariable("fn"));
		headTerms.add(DATA_FACTORY.getVariable("ln"));
		headTerms.add(DATA_FACTORY.getVariable("age"));
		headTerms.add(DATA_FACTORY.getVariable("schooluri"));

		Function head = DATA_FACTORY.getFunction(q, headTerms);

		Term objectTerm = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("http://schools.com/persons", 2), DATA_FACTORY.getVariable("fn"),
				DATA_FACTORY.getVariable("ln"));

		List<Function> body = new LinkedList<Function>();
		Predicate person = DATA_FACTORY.getClassPredicate("Person");
		Predicate fn = DATA_FACTORY.getDataPropertyPredicate("fn", COL_TYPE.LITERAL);
		Predicate ln = DATA_FACTORY.getDataPropertyPredicate("ln", COL_TYPE.LITERAL);
		Predicate age = DATA_FACTORY.getDataPropertyPredicate("age", COL_TYPE.LITERAL);
		Predicate hasschool = DATA_FACTORY.getObjectPropertyPredicate("hasschool");
		Predicate school = DATA_FACTORY.getClassPredicate("School");
		body.add(DATA_FACTORY.getFunction(person, objectTerm));
		body.add(DATA_FACTORY.getFunction(fn, objectTerm, DATA_FACTORY.getVariable("fn")));
		body.add(DATA_FACTORY.getFunction(ln, objectTerm, DATA_FACTORY.getVariable("ln")));
		body.add(DATA_FACTORY.getFunction(age, objectTerm, DATA_FACTORY.getVariable("age")));
		body.add(DATA_FACTORY.getFunction(hasschool, objectTerm, DATA_FACTORY.getVariable("schooluri")));
		body.add(DATA_FACTORY.getFunction(school, DATA_FACTORY.getVariable("schooluri")));

		OBDAMappingAxiom map1 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql), body);

        mappingIndex.put(source2.getSourceID(), ImmutableList.of(map1));

        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
        OBDAModel model = obdaFactory.createOBDAModel(dataSources, mappingIndex, prefixManager,
				new OntologyVocabularyImpl());

		QuestMaterializer materializer = new QuestMaterializer(model, false);
	
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

        final Set<OBDADataSource> dataSources = new HashSet<>();
        final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();

		OBDADataSource source = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb100"));
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
		in.close();

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

		Predicate q = DATA_FACTORY.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		
		final Term firstNameVariable = DATA_FACTORY.getTypedTerm(DATA_FACTORY.getVariable("fn"), COL_TYPE.STRING);
		final Term lastNameVariable = DATA_FACTORY.getTypedTerm(DATA_FACTORY.getVariable("ln"), COL_TYPE.STRING);
		final Term ageVariable = DATA_FACTORY.getTypedTerm(DATA_FACTORY.getVariable("age"), COL_TYPE.INTEGER);
		final Term schoolUriVariable = DATA_FACTORY.getTypedTerm(DATA_FACTORY.getVariable("schooluri"), COL_TYPE.STRING);
		
		headTerms.add(firstNameVariable);
		headTerms.add(lastNameVariable);
		headTerms.add(ageVariable);
		headTerms.add(schoolUriVariable);

		Function head = DATA_FACTORY.getFunction(q, headTerms);

		Term objectTerm = DATA_FACTORY.getUriTemplate(DATA_FACTORY.getConstantLiteral("http://schools.com/persons{}{}"),  // R: was binary -- why?
				firstNameVariable,
				lastNameVariable);

//		List<Function> body = new LinkedList<Function>();
		Predicate person = DATA_FACTORY.getClassPredicate("Person");
		Predicate fn = DATA_FACTORY.getDataPropertyPredicate("firstn", COL_TYPE.LITERAL);
		Predicate ln = DATA_FACTORY.getDataPropertyPredicate("lastn", COL_TYPE.LITERAL);
		Predicate age = DATA_FACTORY.getDataPropertyPredicate("agee", COL_TYPE.LITERAL);
		Predicate hasschool = DATA_FACTORY.getObjectPropertyPredicate("hasschool");
		Predicate school = DATA_FACTORY.getClassPredicate("School");
//		body.add(DATA_FACTORY.getFunctionalTerm(person, objectTerm));
//		body.add(DATA_FACTORY.getFunctionalTerm(fn, objectTerm, DATA_FACTORY.getVariable("fn")));
//		body.add(DATA_FACTORY.getFunctionalTerm(ln, objectTerm, DATA_FACTORY.getVariable("ln")));
//		body.add(DATA_FACTORY.getFunctionalTerm(age, objectTerm, DATA_FACTORY.getVariable("age")));
//		body.add(DATA_FACTORY.getFunctionalTerm(hasschool, objectTerm, DATA_FACTORY.getVariable("schooluri")));
//		body.add(DATA_FACTORY.getFunctionalTerm(school, DATA_FACTORY.getVariable("schooluri")));

		
		OBDAMappingAxiom map1 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql1), Arrays.asList(DATA_FACTORY.getFunction(person, objectTerm)));
		OBDAMappingAxiom map2 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql2), Arrays.asList(DATA_FACTORY.getFunction(fn, objectTerm, firstNameVariable)));
		OBDAMappingAxiom map3 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql3), Arrays.asList(DATA_FACTORY.getFunction(ln, objectTerm, lastNameVariable)));
		OBDAMappingAxiom map4 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql4), Arrays.asList(DATA_FACTORY.getFunction(age, objectTerm, ageVariable)));
		OBDAMappingAxiom map5 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql5), Arrays.asList(DATA_FACTORY.getFunction(hasschool, objectTerm, schoolUriVariable)));
		OBDAMappingAxiom map6 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql6), Arrays.asList(DATA_FACTORY.getFunction(school, schoolUriVariable)));

        dataSources.add(source);
        mappingIndex.put(source.getSourceID(), ImmutableList.of(map1, map2, map3, map4, map5, map6));

        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
        OBDAModel model = obdaFactory.createOBDAModel(dataSources, mappingIndex, prefixManager, new OntologyVocabularyImpl());

		QuestMaterializer materializer = new QuestMaterializer(model, false);

		List<Assertion> assertions = materializer.getAssertionList();
		int count = materializer.getTripleCount();
		
		assertEquals(0, count);

		conn.close();

	}
}
