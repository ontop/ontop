package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.DataSource;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ABoxAssertion;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

public class VirtualABoxMaterializerTest extends TestCase {

	OBDADataFactory	fac	= OBDADataFactoryImpl.getInstance();

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testNoSource() throws Exception {

		OBDAModel model = fac.getOBDAModel();

		/*
		 * Setting the database;
		 */

		VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(model);

		List<ABoxAssertion> assertions = materializer.getAssertionList();
		for (ABoxAssertion a : assertions) {
			System.out.println(a.toString());
		}
		assertTrue(assertions.size() == 0);

		int count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 0);

	}

	public void testOneSource() throws Exception {

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump";
		String username = "sa";
		String password = "";

		DataSource source = fac.getDataSource(URI.create("http://www.obda.org/testdb1"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(source);
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

		String sql = "SELECT fn, ln, age, schooluri FROM data";

		Predicate q = fac.getPredicate(URI.create("q"), 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(fac.getVariable("fn"));
		headTerms.add(fac.getVariable("ln"));
		headTerms.add(fac.getVariable("age"));
		headTerms.add(fac.getVariable("schooluri"));

		Atom head = fac.getAtom(q, headTerms);

		Term objectTerm = fac.getFunctionalTerm(fac.getPredicate(URI.create("http://schools.com/persons"), 2), fac.getVariable("fn"),
				fac.getVariable("ln"));

		List<Atom> body = new LinkedList<Atom>();
		Predicate person = fac.getPredicate(URI.create("Person"), 1);
		Predicate fn = fac.getPredicate(URI.create("fn"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate ln = fac.getPredicate(URI.create("ln"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate age = fac.getPredicate(URI.create("age"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate hasschool = fac.getPredicate(URI.create("hasschool"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		Predicate school = fac.getPredicate(URI.create("School"), 1);
		body.add(fac.getAtom(person, objectTerm));
		body.add(fac.getAtom(fn, objectTerm, fac.getVariable("fn")));
		body.add(fac.getAtom(ln, objectTerm, fac.getVariable("ln")));
		body.add(fac.getAtom(age, objectTerm, fac.getVariable("age")));
		body.add(fac.getAtom(hasschool, objectTerm, fac.getVariable("schooluri")));
		body.add(fac.getAtom(school, fac.getVariable("schooluri")));

		OBDAMappingAxiom map1 = fac.getRDBMSMappingAxiom(sql, fac.getCQIE(head, body));

		OBDAModel model = fac.getOBDAModel();
		model.addSource(source);
		model.addMapping(source.getSourceID(), map1);

		VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(model);

		List<ABoxAssertion> assertions = materializer.getAssertionList();
		for (ABoxAssertion a : assertions) {
			System.out.println(a.toString());
		}
		assertTrue(assertions.size() == 18);

		int count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 18);

		conn.close();

	}

	public void testTwoSources() throws Exception {

		OBDAModel model = fac.getOBDAModel();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump2";
		String username = "sa";
		String password = "";

		DataSource source = fac.getDataSource(URI.create("http://www.obda.org/testdb1"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(source);
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

		DataSource source2 = fac.getDataSource(URI.create("http://www.obda.org/testdb2"));
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

		String sql = "SELECT fn, ln, age, schooluri FROM data";

		Predicate q = fac.getPredicate(URI.create("q"), 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(fac.getVariable("fn"));
		headTerms.add(fac.getVariable("ln"));
		headTerms.add(fac.getVariable("age"));
		headTerms.add(fac.getVariable("schooluri"));

		Atom head = fac.getAtom(q, headTerms);

		Term objectTerm = fac.getFunctionalTerm(fac.getPredicate(URI.create("http://schools.com/persons"), 2), fac.getVariable("fn"),
				fac.getVariable("ln"));

		List<Atom> body = new LinkedList<Atom>();
		Predicate person = fac.getPredicate(URI.create("Person"), 1);
		Predicate fn = fac.getPredicate(URI.create("fn"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate ln = fac.getPredicate(URI.create("ln"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate age = fac.getPredicate(URI.create("age"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate hasschool = fac.getPredicate(URI.create("hasschool"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		Predicate school = fac.getPredicate(URI.create("School"), 1);
		body.add(fac.getAtom(person, objectTerm));
		body.add(fac.getAtom(fn, objectTerm, fac.getVariable("fn")));
		body.add(fac.getAtom(ln, objectTerm, fac.getVariable("ln")));
		body.add(fac.getAtom(age, objectTerm, fac.getVariable("age")));
		body.add(fac.getAtom(hasschool, objectTerm, fac.getVariable("schooluri")));
		body.add(fac.getAtom(school, fac.getVariable("schooluri")));

		OBDAMappingAxiom map1 = fac.getRDBMSMappingAxiom(sql, fac.getCQIE(head, body));

		model.addMapping(source.getSourceID(), map1);
		model.addMapping(source2.getSourceID(), map1);

		VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(model);

		List<ABoxAssertion> assertions = materializer.getAssertionList();
		for (ABoxAssertion a : assertions) {
			System.out.println(a.toString());
		}
		assertTrue(assertions.size() == 36);

		int count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 36);

		conn.close();

	}

	public void testThreeSources() throws Exception {

		OBDAModel model = fac.getOBDAModel();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump3";
		String username = "sa";
		String password = "";

		DataSource source = fac.getDataSource(URI.create("http://www.obda.org/testdb1"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(source);
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

		DataSource source2 = fac.getDataSource(URI.create("http://www.obda.org/testdb2"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
		model.addSource(source2);

		DataSource source3 = fac.getDataSource(URI.create("http://www.obda.org/testdb3"));
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

		String sql = "SELECT fn, ln, age, schooluri FROM data";

		Predicate q = fac.getPredicate(URI.create("q"), 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(fac.getVariable("fn"));
		headTerms.add(fac.getVariable("ln"));
		headTerms.add(fac.getVariable("age"));
		headTerms.add(fac.getVariable("schooluri"));

		Atom head = fac.getAtom(q, headTerms);

		Term objectTerm = fac.getFunctionalTerm(fac.getPredicate(URI.create("http://schools.com/persons"), 2), fac.getVariable("fn"),
				fac.getVariable("ln"));

		List<Atom> body = new LinkedList<Atom>();
		Predicate person = fac.getPredicate(URI.create("Person"), 1);
		Predicate fn = fac.getPredicate(URI.create("fn"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate ln = fac.getPredicate(URI.create("ln"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate age = fac.getPredicate(URI.create("age"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate hasschool = fac.getPredicate(URI.create("hasschool"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		Predicate school = fac.getPredicate(URI.create("School"), 1);
		body.add(fac.getAtom(person, objectTerm));
		body.add(fac.getAtom(fn, objectTerm, fac.getVariable("fn")));
		body.add(fac.getAtom(ln, objectTerm, fac.getVariable("ln")));
		body.add(fac.getAtom(age, objectTerm, fac.getVariable("age")));
		body.add(fac.getAtom(hasschool, objectTerm, fac.getVariable("schooluri")));
		body.add(fac.getAtom(school, fac.getVariable("schooluri")));

		OBDAMappingAxiom map1 = fac.getRDBMSMappingAxiom(sql, fac.getCQIE(head, body));

		model.addMapping(source.getSourceID(), map1);
		model.addMapping(source2.getSourceID(), map1);
		model.addMapping(source3.getSourceID(), map1);

		VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(model);

		List<ABoxAssertion> assertions = materializer.getAssertionList();
		for (ABoxAssertion a : assertions) {
			System.out.println(a.toString());
		}
		assertTrue(assertions.size() == 54);

		int count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 54);

		conn.close();

	}

	public void testThreeSourcesNoMappings() throws Exception {

		OBDAModel model = fac.getOBDAModel();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump4";
		String username = "sa";
		String password = "";

		DataSource source = fac.getDataSource(URI.create("http://www.obda.org/testdb1"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(source);
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

		DataSource source2 = fac.getDataSource(URI.create("http://www.obda.org/testdb2"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
		model.addSource(source2);

		DataSource source3 = fac.getDataSource(URI.create("http://www.obda.org/testdb3"));
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source3.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source3.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source3.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
		model.addSource(source3);

		VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(model);

		List<ABoxAssertion> assertions = materializer.getAssertionList();
		for (ABoxAssertion a : assertions) {
			System.out.println(a.toString());
		}
		assertTrue(assertions.size() == 0);

		int count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 0);

		conn.close();

	}

	public void testThreeSourcesNoMappingsFor1And3() throws Exception {

		OBDAModel model = fac.getOBDAModel();

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump5";
		String username = "sa";
		String password = "";

		DataSource source = fac.getDataSource(URI.create("http://www.obda.org/testdb1"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(source);
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

		DataSource source2 = fac.getDataSource(URI.create("http://www.obda.org/testdb2"));
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
		model.addSource(source2);

		DataSource source3 = fac.getDataSource(URI.create("http://www.obda.org/testdb3"));
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

		String sql = "SELECT fn, ln, age, schooluri FROM data";

		Predicate q = fac.getPredicate(URI.create("q"), 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(fac.getVariable("fn"));
		headTerms.add(fac.getVariable("ln"));
		headTerms.add(fac.getVariable("age"));
		headTerms.add(fac.getVariable("schooluri"));

		Atom head = fac.getAtom(q, headTerms);

		Term objectTerm = fac.getFunctionalTerm(fac.getPredicate(URI.create("http://schools.com/persons"), 2), fac.getVariable("fn"),
				fac.getVariable("ln"));

		List<Atom> body = new LinkedList<Atom>();
		Predicate person = fac.getPredicate(URI.create("Person"), 1);
		Predicate fn = fac.getPredicate(URI.create("fn"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate ln = fac.getPredicate(URI.create("ln"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate age = fac.getPredicate(URI.create("age"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate hasschool = fac.getPredicate(URI.create("hasschool"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		Predicate school = fac.getPredicate(URI.create("School"), 1);
		body.add(fac.getAtom(person, objectTerm));
		body.add(fac.getAtom(fn, objectTerm, fac.getVariable("fn")));
		body.add(fac.getAtom(ln, objectTerm, fac.getVariable("ln")));
		body.add(fac.getAtom(age, objectTerm, fac.getVariable("age")));
		body.add(fac.getAtom(hasschool, objectTerm, fac.getVariable("schooluri")));
		body.add(fac.getAtom(school, fac.getVariable("schooluri")));

		OBDAMappingAxiom map1 = fac.getRDBMSMappingAxiom(sql, fac.getCQIE(head, body));

		model.addMapping(source2.getSourceID(), map1);

		VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(model);

		List<ABoxAssertion> assertions = materializer.getAssertionList();
		for (ABoxAssertion a : assertions) {
			System.out.println(a.toString());
		}
		assertTrue(assertions.size() == 18);

		int count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 18);

		conn.close();

	}
	
	public void testMultipleMappingsOneSource() throws Exception {

		/*
		 * Setting the database;
		 */

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:aboxdump10";
		String username = "sa";
		String password = "";

		DataSource source = fac.getDataSource(URI.create("http://www.obda.org/testdb1"));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(source);
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

		String sql1 = "SELECT fn, ln FROM data";
		String sql2 = "SELECT fn, ln FROM data";
		String sql3 = "SELECT fn, ln FROM data";
		String sql4 = "SELECT fn, ln, age FROM data";
		String sql5 = "SELECT fn, ln, schooluri FROM data";
		String sql6 = "SELECT fn, ln, schooluri FROM data";

		Predicate q = fac.getPredicate(URI.create("q"), 4);
		List<Term> headTerms = new LinkedList<Term>();
		headTerms.add(fac.getVariable("fn"));
		headTerms.add(fac.getVariable("ln"));
		headTerms.add(fac.getVariable("age"));
		headTerms.add(fac.getVariable("schooluri"));

		Atom head = fac.getAtom(q, headTerms);

		Term objectTerm = fac.getFunctionalTerm(fac.getPredicate(URI.create("http://schools.com/persons"), 2), fac.getVariable("fn"),
				fac.getVariable("ln"));

//		List<Atom> body = new LinkedList<Atom>();
		Predicate person = fac.getPredicate(URI.create("Person"), 1);
		Predicate fn = fac.getPredicate(URI.create("fn"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate ln = fac.getPredicate(URI.create("ln"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate age = fac.getPredicate(URI.create("age"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.LITERAL });
		Predicate hasschool = fac.getPredicate(URI.create("hasschool"), 2, new COL_TYPE[] { COL_TYPE.OBJECT, COL_TYPE.OBJECT });
		Predicate school = fac.getPredicate(URI.create("School"), 1);
//		body.add(fac.getAtom(person, objectTerm));
//		body.add(fac.getAtom(fn, objectTerm, fac.getVariable("fn")));
//		body.add(fac.getAtom(ln, objectTerm, fac.getVariable("ln")));
//		body.add(fac.getAtom(age, objectTerm, fac.getVariable("age")));
//		body.add(fac.getAtom(hasschool, objectTerm, fac.getVariable("schooluri")));
//		body.add(fac.getAtom(school, fac.getVariable("schooluri")));

		
		OBDAMappingAxiom map1 = fac.getRDBMSMappingAxiom(sql1, fac.getCQIE(head, fac.getAtom(person, objectTerm)));
		OBDAMappingAxiom map2 = fac.getRDBMSMappingAxiom(sql2, fac.getCQIE(head, fac.getAtom(fn, objectTerm, fac.getVariable("fn"))));
		OBDAMappingAxiom map3 = fac.getRDBMSMappingAxiom(sql3, fac.getCQIE(head, fac.getAtom(ln, objectTerm, fac.getVariable("ln"))));
		OBDAMappingAxiom map4 = fac.getRDBMSMappingAxiom(sql4, fac.getCQIE(head, fac.getAtom(age, objectTerm, fac.getVariable("age"))));
		OBDAMappingAxiom map5 = fac.getRDBMSMappingAxiom(sql5, fac.getCQIE(head, fac.getAtom(hasschool, objectTerm, fac.getVariable("schooluri"))));
		OBDAMappingAxiom map6 = fac.getRDBMSMappingAxiom(sql6, fac.getCQIE(head, fac.getAtom(school, fac.getVariable("schooluri"))));
		
		

		OBDAModel model = fac.getOBDAModel();
		model.addSource(source);
		model.addMapping(source.getSourceID(), map1);
		model.addMapping(source.getSourceID(), map2);
		model.addMapping(source.getSourceID(), map3);
		model.addMapping(source.getSourceID(), map4);
		model.addMapping(source.getSourceID(), map5);
		model.addMapping(source.getSourceID(), map6);

		VirtualABoxMaterializer materializer = new VirtualABoxMaterializer(model);

		List<ABoxAssertion> assertions = materializer.getAssertionList();
		for (ABoxAssertion a : assertions) {
			System.out.println(a.toString());
		}
		assertTrue(assertions.size() == 18);

		int count = materializer.getTripleCount();
		assertTrue("count: " + count, count == 18);

		conn.close();

	}
}
