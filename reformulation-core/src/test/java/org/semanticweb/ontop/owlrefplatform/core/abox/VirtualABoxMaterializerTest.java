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
import java.util.*;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.semanticweb.ontop.ontology.Assertion;
import org.semanticweb.ontop.sql.JDBCConnectionManager;

import junit.framework.TestCase;

public class VirtualABoxMaterializerTest extends TestCase {

    private final OBDADataFactory oldFactory = OBDADataFactoryImpl.getInstance();
	private final NativeQueryLanguageComponentFactory nativeQLFactory;

    public VirtualABoxMaterializerTest() {
        Injector injector = Guice.createInjector(new OBDACoreModule(new OBDAProperties()));
        nativeQLFactory = injector.getInstance(NativeQueryLanguageComponentFactory.class);
    }

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testNoSource() throws Exception {
        try{
                OBDAModel model = nativeQLFactory.create(new HashSet<OBDADataSource>(),
                        new HashMap<URI, ImmutableList<OBDAMappingAxiom>>(),
                        nativeQLFactory.create(new HashMap<String, String>()));

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

		OBDADataSource source = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb1"));
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

		Predicate q = oldFactory.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(oldFactory.getVariable("fn"));
		headTerms.add(oldFactory.getVariable("ln"));
		headTerms.add(oldFactory.getVariable("age"));
		headTerms.add(oldFactory.getVariable("schooluri"));

		Function head = oldFactory.getFunction(q, headTerms);

		Term objectTerm = oldFactory.getFunction(oldFactory.getPredicate("http://schools.com/persons", 2), oldFactory.getVariable("fn"),
				oldFactory.getVariable("ln"));

		List<Function> body = new LinkedList<Function>();
		Predicate person = oldFactory.getPredicate("Person", 1);
		Predicate fn = oldFactory.getPredicate("fn", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate ln = oldFactory.getPredicate("ln", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate age = oldFactory.getPredicate("age", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate hasschool = oldFactory.getPredicate("hasschool", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		Predicate school = oldFactory.getPredicate("School", 1);
		body.add(oldFactory.getFunction(person, objectTerm));
		body.add(oldFactory.getFunction(fn, objectTerm, oldFactory.getVariable("fn")));
		body.add(oldFactory.getFunction(ln, objectTerm, oldFactory.getVariable("ln")));
		body.add(oldFactory.getFunction(age, objectTerm, oldFactory.getVariable("age")));
		body.add(oldFactory.getFunction(hasschool, objectTerm, oldFactory.getVariable("schooluri")));
		body.add(oldFactory.getFunction(school, oldFactory.getVariable("schooluri")));

		OBDAMappingAxiom map1 = oldFactory.getRDBMSMappingAxiom(sql, oldFactory.getCQIE(head, body));

        Set<OBDADataSource> dataSources = new HashSet<>();
        Map<URI, ImmutableList<OBDAMappingAxiom>> mappings = new HashMap<>();

        //model.addSource(source);
        //model.addMapping(source.getSourceID(), map1);
        dataSources.add(source);
        mappings.put(source.getSourceID(), ImmutableList.of(map1));

        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
		OBDAModel model = nativeQLFactory.create(dataSources, mappings, prefixManager);

        //TODO: remove this dangerous cast
		QuestMaterializer materializer = new QuestMaterializer(model);

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

            OBDADataSource source = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb3"));
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

            dataSources.add(source);

            OBDADataSource source2 = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb2"));
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

            Predicate q = oldFactory.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
            List<Term> headTerms = new LinkedList<Term>();
            headTerms.add(oldFactory.getVariable("fn"));
            headTerms.add(oldFactory.getVariable("ln"));
            headTerms.add(oldFactory.getVariable("age"));
            headTerms.add(oldFactory.getVariable("schooluri"));

            Function head = oldFactory.getFunction(q, headTerms);

            Term objectTerm = oldFactory.getFunction(oldFactory.getPredicate("http://schools.com/persons", 2), oldFactory.getVariable("fn"),
                    oldFactory.getVariable("ln"));

            List<Function> body = new LinkedList<Function>();
            Predicate person = oldFactory.getPredicate("Person", 1);
            Predicate fn = oldFactory.getPredicate("fn", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
            Predicate ln = oldFactory.getPredicate("ln", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
            Predicate age = oldFactory.getPredicate("age", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
            Predicate hasschool = oldFactory.getPredicate("hasschool", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
            Predicate school = oldFactory.getPredicate("School", 1);
            body.add(oldFactory.getFunction(person, objectTerm));
            body.add(oldFactory.getFunction(fn, objectTerm, oldFactory.getVariable("fn")));
            body.add(oldFactory.getFunction(ln, objectTerm, oldFactory.getVariable("ln")));
            body.add(oldFactory.getFunction(age, objectTerm, oldFactory.getVariable("age")));
            body.add(oldFactory.getFunction(hasschool, objectTerm, oldFactory.getVariable("schooluri")));
            body.add(oldFactory.getFunction(school, oldFactory.getVariable("schooluri")));

            OBDAMappingAxiom map1 = oldFactory.getRDBMSMappingAxiom(sql, oldFactory.getCQIE(head, body));

            mappingIndex.put(source.getSourceID(), ImmutableList.of(map1));
            mappingIndex.put(source2.getSourceID(), ImmutableList.of(map1));

            PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
            OBDAModel model = nativeQLFactory.create(dataSources, mappingIndex, prefixManager);

            QuestMaterializer materializer = new QuestMaterializer(model);

            List<Assertion> assertions = materializer.getAssertionList();

            conn.close();

        } catch(Exception e) {
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

		OBDADataSource source = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb4"));
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

        dataSources.add(source);

		OBDADataSource source2 = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb5"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
        dataSources.add(source2);

		OBDADataSource source3 = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb6"));
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

		Predicate q = oldFactory.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(oldFactory.getVariable("fn"));
		headTerms.add(oldFactory.getVariable("ln"));
		headTerms.add(oldFactory.getVariable("age"));
		headTerms.add(oldFactory.getVariable("schooluri"));

		Function head = oldFactory.getFunction(q, headTerms);

		Term objectTerm = oldFactory.getFunction(oldFactory.getPredicate("http://schools.com/persons", 2), oldFactory.getVariable("fn"),
				oldFactory.getVariable("ln"));

		List<Function> body = new LinkedList<Function>();
		Predicate person = oldFactory.getPredicate("Person", 1);
		Predicate fn = oldFactory.getPredicate("fn", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate ln = oldFactory.getPredicate("ln", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate age = oldFactory.getPredicate("age", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate hasschool = oldFactory.getPredicate("hasschool", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		Predicate school = oldFactory.getPredicate("School", 1);
		body.add(oldFactory.getFunction(person, objectTerm));
		body.add(oldFactory.getFunction(fn, objectTerm, oldFactory.getVariable("fn")));
		body.add(oldFactory.getFunction(ln, objectTerm, oldFactory.getVariable("ln")));
		body.add(oldFactory.getFunction(age, objectTerm, oldFactory.getVariable("age")));
		body.add(oldFactory.getFunction(hasschool, objectTerm, oldFactory.getVariable("schooluri")));
		body.add(oldFactory.getFunction(school, oldFactory.getVariable("schooluri")));

		OBDAMappingAxiom map1 = oldFactory.getRDBMSMappingAxiom(sql, oldFactory.getCQIE(head, body));

        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
        OBDAModel model = nativeQLFactory.create(dataSources, mappingIndex, prefixManager);

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
        final Set<OBDADataSource> dataSources = new HashSet<>();
        final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump7";
		String username = "sa";
		String password = "";

		OBDADataSource source = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb7"));
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

        dataSources.add(source);

		OBDADataSource source2 = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb8"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
        dataSources.add(source2);

		OBDADataSource source3 = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb9"));
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source3.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source3.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
        dataSources.add(source3);

        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
        OBDAModel model = nativeQLFactory.create(dataSources, mappingIndex, prefixManager);
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
        final Set<OBDADataSource> dataSources = new HashSet<>();
        final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump5";
		String username = "sa";
		String password = "";

		OBDADataSource source = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb11"));
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

		dataSources.add(source);

		OBDADataSource source2 = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb12"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
        dataSources.add(source2);

		OBDADataSource source3 = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb13"));
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

		Predicate q = oldFactory.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(oldFactory.getVariable("fn"));
		headTerms.add(oldFactory.getVariable("ln"));
		headTerms.add(oldFactory.getVariable("age"));
		headTerms.add(oldFactory.getVariable("schooluri"));

		Function head = oldFactory.getFunction(q, headTerms);

		Term objectTerm = oldFactory.getFunction(oldFactory.getPredicate("http://schools.com/persons", 2), oldFactory.getVariable("fn"),
				oldFactory.getVariable("ln"));

		List<Function> body = new LinkedList<Function>();
		Predicate person = oldFactory.getPredicate("Person", 1);
		Predicate fn = oldFactory.getPredicate("fn", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate ln = oldFactory.getPredicate("ln", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate age = oldFactory.getPredicate("age", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate hasschool = oldFactory.getPredicate("hasschool", 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		Predicate school = oldFactory.getPredicate("School", 1);
		body.add(oldFactory.getFunction(person, objectTerm));
		body.add(oldFactory.getFunction(fn, objectTerm, oldFactory.getVariable("fn")));
		body.add(oldFactory.getFunction(ln, objectTerm, oldFactory.getVariable("ln")));
		body.add(oldFactory.getFunction(age, objectTerm, oldFactory.getVariable("age")));
		body.add(oldFactory.getFunction(hasschool, objectTerm, oldFactory.getVariable("schooluri")));
		body.add(oldFactory.getFunction(school, oldFactory.getVariable("schooluri")));

		OBDAMappingAxiom map1 = oldFactory.getRDBMSMappingAxiom(sql, oldFactory.getCQIE(head, body));

        mappingIndex.put(source2.getSourceID(), ImmutableList.of(map1));

        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
        OBDAModel model = nativeQLFactory.create(dataSources, mappingIndex, prefixManager);

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

        final Set<OBDADataSource> dataSources = new HashSet<>();
        final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();

		OBDADataSource source = oldFactory.getDataSource(URI.create("http://www.obda.org/testdb100"));
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

		Predicate q = oldFactory.getPredicate(OBDALibConstants.QUERY_HEAD, 4);
		List<Term> headTerms = new LinkedList<Term>();
		
		final Term firstNameVariable = oldFactory.getFunction(oldFactory.getDataTypePredicateString(), oldFactory.getVariable("fn"));
		final Term lastNameVariable = oldFactory.getFunction(oldFactory.getDataTypePredicateString(), oldFactory.getVariable("ln"));
		final Term ageVariable = oldFactory.getFunction(oldFactory.getDataTypePredicateInteger(), oldFactory.getVariable("age"));
		final Term schoolUriVariable = oldFactory.getFunction(oldFactory.getDataTypePredicateString(), oldFactory.getVariable("schooluri"));
		
		headTerms.add(firstNameVariable);
		headTerms.add(lastNameVariable);
		headTerms.add(ageVariable);
		headTerms.add(schoolUriVariable);

		Function head = oldFactory.getFunction(q, headTerms);

		Term objectTerm = oldFactory.getFunction(oldFactory.getUriTemplatePredicate(2),
				oldFactory.getConstantLiteral("http://schools.com/persons{}{}"),
				firstNameVariable,
				lastNameVariable);

//		List<Function> body = new LinkedList<Function>();
		Predicate person = oldFactory.getClassPredicate("Person");
		Predicate fn = oldFactory.getDataPropertyPredicate("firstn");
		Predicate ln = oldFactory.getDataPropertyPredicate("lastn");
		Predicate age = oldFactory.getDataPropertyPredicate("agee");
		Predicate hasschool = oldFactory.getDataPropertyPredicate("hasschool");
		Predicate school = oldFactory.getClassPredicate("School");
//		body.add(oldFactory.getFunctionalTerm(person, objectTerm));
//		body.add(oldFactory.getFunctionalTerm(fn, objectTerm, oldFactory.getVariable("fn")));
//		body.add(oldFactory.getFunctionalTerm(ln, objectTerm, oldFactory.getVariable("ln")));
//		body.add(oldFactory.getFunctionalTerm(age, objectTerm, oldFactory.getVariable("age")));
//		body.add(oldFactory.getFunctionalTerm(hasschool, objectTerm, oldFactory.getVariable("schooluri")));
//		body.add(oldFactory.getFunctionalTerm(school, oldFactory.getVariable("schooluri")));

		
		OBDAMappingAxiom map1 = oldFactory.getRDBMSMappingAxiom(sql1, oldFactory.getCQIE(head, oldFactory.getFunction(person, objectTerm)));
		OBDAMappingAxiom map2 = oldFactory.getRDBMSMappingAxiom(sql2, oldFactory.getCQIE(head, oldFactory.getFunction(fn, objectTerm, firstNameVariable)));
		OBDAMappingAxiom map3 = oldFactory.getRDBMSMappingAxiom(sql3, oldFactory.getCQIE(head, oldFactory.getFunction(ln, objectTerm, lastNameVariable)));
		OBDAMappingAxiom map4 = oldFactory.getRDBMSMappingAxiom(sql4, oldFactory.getCQIE(head, oldFactory.getFunction(age, objectTerm, ageVariable)));
		OBDAMappingAxiom map5 = oldFactory.getRDBMSMappingAxiom(sql5, oldFactory.getCQIE(head, oldFactory.getFunction(hasschool, objectTerm, schoolUriVariable)));
		OBDAMappingAxiom map6 = oldFactory.getRDBMSMappingAxiom(sql6, oldFactory.getCQIE(head, oldFactory.getFunction(school, schoolUriVariable)));

        dataSources.add(source);
        mappingIndex.put(source.getSourceID(), ImmutableList.of(map1, map2, map3, map4, map5, map6));

        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
        OBDAModel model = nativeQLFactory.create(dataSources, mappingIndex, prefixManager);

		QuestMaterializer materializer = new QuestMaterializer(model);

		List<Assertion> assertions = materializer.getAssertionList();
		int count = materializer.getTripleCount();
		
		assertEquals(0, count);

		conn.close();

	}
}
