package it.unibz.inf.ontop.materialization;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;
import it.unibz.inf.ontop.spec.mapping.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.ontology.Assertion;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class OntopMaterializerTest {

	private static final String PREFIX = "http://example.com/vocab#";

	private static final String driver = "org.h2.Driver";
	private static String url = "jdbc:h2:mem:aboxdump";
	private static String username = "sa";
	private static String password = "";

	private final SQLMappingFactory mappingFactory;
	private final RDF rdfFactory;
	private final RDFDatatype xsdStringDt;

	private final ImmutableTerm type;
	private final ImmutableTerm person;
	private final ImmutableTerm fn;
	private final ImmutableTerm ln;
	private final ImmutableTerm age;
	private final ImmutableTerm hasschool;
	private final ImmutableTerm school;

	private final IRI personIRI;
	private final IRI fnIRI;
	private final IRI lnIRI;
	private final IRI ageIRI;
	private final IRI hasschoolIRI;
	private final IRI schoolIRI;
	

	private static final Logger LOGGER = LoggerFactory.getLogger(OntopMaterializerTest.class);
	private final TermFactory termFactory;
	private final TypeFactory typeFactory;
	private final TargetAtomFactory targetAtomFactory;
	AtomFactory atomFactory;
	public OntopMaterializerTest() {

		OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder()
				.enableTestMode()
				.build();

		Injector injector = defaultConfiguration.getInjector();
		termFactory = injector.getInstance(TermFactory.class);
		typeFactory = injector.getInstance(TypeFactory.class);
		atomFactory = injector.getInstance(AtomFactory.class);
		targetAtomFactory = injector.getInstance(TargetAtomFactory.class);
		rdfFactory = injector.getInstance(RDF.class);

		mappingFactory = SQLMappingFactoryImpl.getInstance();
		
		personIRI = rdfFactory.createIRI(PREFIX + "Person");
		fnIRI = rdfFactory.createIRI(PREFIX + "fn");
		lnIRI = rdfFactory.createIRI(PREFIX + "ln");
		ageIRI = rdfFactory.createIRI(PREFIX + "age");
		hasschoolIRI = rdfFactory.createIRI(PREFIX + "hasschool");
		schoolIRI = rdfFactory.createIRI(PREFIX + "School");

		xsdStringDt = typeFactory.getXsdStringDatatype();

		type = termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(it.unibz.inf.ontop.model.vocabulary.RDF.TYPE.getIRIString()));
		person = termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(personIRI.getIRIString()));
		fn = termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(fnIRI.getIRIString()));
		ln = termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(lnIRI.getIRIString()));
		age =termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(ageIRI.getIRIString()));
		hasschool = termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(hasschoolIRI.getIRIString()));
		school = termFactory.getImmutableUriTemplate(termFactory.getConstantLiteral(schoolIRI.getIRIString()));
    }

	private static OntopStandaloneSQLConfiguration.Builder<? extends OntopStandaloneSQLConfiguration.Builder> createAndInitConfiguration() {
		return OntopStandaloneSQLConfiguration.defaultBuilder()
				.jdbcUrl(url)
				.jdbcUser(username)
				.jdbcPassword(password)
				.jdbcDriver(driver);
	}

	@Test(expected = InvalidOntopConfigurationException.class)
	public void testNoSource()  {
		OntopStandaloneSQLConfiguration.defaultBuilder().build();
	}

	@Test
	public void testOneSource() throws Exception {

    	SQLPPMapping ppMapping = createMapping();

		OntopStandaloneSQLConfiguration configuration = createAndInitConfiguration()
				.ppMapping(ppMapping)
				.build();
		// source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		// source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");

		Connection conn = DriverManager.getConnection(url, username, password);
		Statement st = conn.createStatement();

		FileReader reader = new FileReader("src/test/resources/mapping-test-db.sql");
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

		ImmutableSet<IRI> vocabulary = Stream.of(fnIRI, lnIRI, ageIRI, hasschoolIRI, schoolIRI)
				.collect(ImmutableCollectors.toSet());

		OntopRDFMaterializer materializer = OntopRDFMaterializer.defaultMaterializer(configuration);

		try (MaterializedGraphResultSet materializationResultSet = materializer.materialize(vocabulary)) {

			ImmutableList.Builder<Assertion> rdfGraphBuilder = ImmutableList.builder();
			while (materializationResultSet.hasNext()) {
				rdfGraphBuilder.add(materializationResultSet.next());
			}
			ImmutableList<Assertion> assertions = rdfGraphBuilder.build();

			LOGGER.debug("Assertions: \n");
			assertions.forEach(a -> LOGGER.debug(a + "\n"));

			assertEquals(15, assertions.size());

			long count = materializationResultSet.getTripleCountSoFar();
			assertEquals(15, count);
		}

		conn.close();

	}



	private SQLPPMapping createMapping() throws DuplicateMappingException {

    	// TODO: we should not have to create an high-level configuration just for constructing these objects...
		OntopStandaloneSQLConfiguration configuration = createAndInitConfiguration()
				.build();
		Injector injector = configuration.getInjector();
		SpecificationFactory specificationFactory = injector.getInstance(SpecificationFactory.class);
		SQLPPMappingFactory ppMappingFactory = injector.getInstance(SQLPPMappingFactory.class);

    			/*
		 * Setting up the OBDA model and the mappings
		 */

		String sql = "SELECT \"fn\", \"ln\", \"age\", \"schooluri\" FROM \"data\"";

		ImmutableFunctionalTerm personTemplate = termFactory.getImmutableUriTemplate(
				termFactory.getConstantLiteral("http://schools.com/person/{}-{}"),
				termFactory.getVariable("fn"),
				termFactory.getVariable("ln"));

		ImmutableFunctionalTerm schoolTemplate = termFactory.getImmutableUriTemplate(
				termFactory.getConstantLiteral("{}"),
				termFactory.getVariable("schooluri"));

		RDFDatatype stringDatatype = xsdStringDt;

		ImmutableList.Builder<TargetAtom> bodyBuilder = ImmutableList.builder();

		bodyBuilder.add(getTripleTargetAtom(personTemplate, type, person));
		bodyBuilder.add(getTripleTargetAtom(personTemplate, fn, termFactory.getImmutableTypedTerm(termFactory.getVariable("fn"), stringDatatype)));
		bodyBuilder.add(getTripleTargetAtom(personTemplate, ln, termFactory.getImmutableTypedTerm(termFactory.getVariable("ln"), stringDatatype)));
		bodyBuilder.add(getTripleTargetAtom(personTemplate, age, termFactory.getImmutableTypedTerm(termFactory.getVariable("age"), stringDatatype)));
		bodyBuilder.add(getTripleTargetAtom(personTemplate, hasschool, schoolTemplate));
		bodyBuilder.add(getTripleTargetAtom(personTemplate, school, schoolTemplate));

		ImmutableList<TargetAtom> body = bodyBuilder.build();

		SQLPPTriplesMap map1 = new OntopNativeSQLPPTriplesMap(mappingFactory.getSQLQuery(sql), body);

		UriTemplateMatcher uriTemplateMatcher = UriTemplateMatcher.create(
				body.stream()
						.flatMap(atom -> atom.getSubstitution().getImmutableMap().values().stream())
						.filter(t -> t instanceof ImmutableFunctionalTerm)
						.map(t -> (ImmutableFunctionalTerm) t),
				termFactory);

		PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.of());
		MappingMetadata mappingMetadata = specificationFactory.createMetadata(prefixManager, uriTemplateMatcher);
		return ppMappingFactory.createSQLPreProcessedMapping(ImmutableList.of(map1), mappingMetadata);
	}

	private TargetAtom getTripleTargetAtom(ImmutableTerm s, ImmutableTerm p, ImmutableTerm o) {
		return targetAtomFactory.getTripleTargetAtom(s, p, o);
	}

//	public void testTwoSources() throws Exception {
//        try{
//            /*
//             * Setting the database;
//             */
//
//            String driver = "org.h2.Driver";
//            String url = "jdbc:h2:mem:aboxdump3";
//            String username = "sa";
//            String password = "";
//
//            OBDADataSource source = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb3"));
//            source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//            source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//            source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//            source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//            source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//            source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//
//            Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
//            Statement st = conn.createStatement();
//
//		FileReader reader = new FileReader("src/test/resources/test/mapping-test-db.sql");
//		BufferedReader in = new BufferedReader(reader);
//		StringBuilder bf = new StringBuilder();
//		String line = in.readLine();
//		while (line != null) {
//			bf.append(line);
//			line = in.readLine();
//		}
//		in.close();
//
//            st.executeUpdate(bf.toString());
//            conn.commit();
//
//            dataSources.add(source);
//
//            OBDADataSource source2 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb2"));
//            source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//            source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//            source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//            source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//            source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//            source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//            dataSources.add(source2);
//
//            /*
//             * Setting up the OBDA model and the mappings
//             */
//
//            String sql = "SELECT \"fn\", \"ln\", \"age\", \"schooluri\" FROM \"data\"";
//
//            Predicate q = termFactory.getIRI(OBDALibConstants.QUERY_HEAD, 4);
//            List<Term> headTerms = new LinkedList<Term>();
//            headTerms.add(termFactory.getVariable("fn"));
//            headTerms.add(termFactory.getVariable("ln"));
//            headTerms.add(termFactory.getVariable("age"));
//            headTerms.add(termFactory.getVariable("schooluri"));
//
//            Function head = termFactory.getFunction(q, headTerms);
//
//            Term objectTerm = termFactory.getFunction(termFactory.getIRI("http://schools.com/persons", 2), termFactory.getVariable("fn"),
//                    termFactory.getVariable("ln"));
//
//		List<Function> body = new LinkedList<Function>();
//		Predicate person = termFactory.getClassPredicate("Person");
//		Predicate fn = termFactory.getDataPropertyPredicate("fn", COL_TYPE.LITERAL);
//		Predicate ln = termFactory.getDataPropertyPredicate("ln", COL_TYPE.LITERAL);
//		Predicate age = termFactory.getDataPropertyPredicate("age", COL_TYPE.LITERAL);
//		Predicate hasschool = termFactory.getObjectPropertyPredicate("hasschool");
//		Predicate school = termFactory.getClassPredicate("School");
//		body.add(termFactory.getFunction(person, objectTerm));
//		body.add(termFactory.getFunction(fn, objectTerm, termFactory.getVariable("fn")));
//		body.add(termFactory.getFunction(ln, objectTerm, termFactory.getVariable("ln")));
//		body.add(termFactory.getFunction(age, objectTerm, termFactory.getVariable("age")));
//		body.add(termFactory.getFunction(hasschool, objectTerm, termFactory.getVariable("schooluri")));
//		body.add(termFactory.getFunction(school, termFactory.getVariable("schooluri")));
//
//            OBDAMappingAxiom map1 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql), body);
//
//            mappingIndex.put(source.getSourceID(), ImmutableList.of(map1));
//            mappingIndex.put(source2.getSourceID(), ImmutableList.of(map1));
//
//            PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
//            OBDAModel model = obdaFactory.createMapping(dataSources, mappingIndex, prefixManager,
//					new MutableOntologyVocabularyImpl());
//
//            QuestMaterializer materializer = new QuestMaterializer(model, false);
//
//            List<Assertion> assertions = materializer.getAssertionList();
//
//            conn.close();
//
//        } catch (DuplicateMappingException e) {
//        }
//        catch(Exception e) {
//            assertEquals("Cannot materialize with multiple data sources!", e.getMessage());
//        }
//
//	}
//
//	public void testThreeSources() throws Exception {
//    try{
//        final Set<OBDADataSource> dataSources = new HashSet<>();
//        final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();
//
//		/*
//		 * Setting the database;
//		 */
//
//		String driver = "org.h2.Driver";
//		String url = "jdbc:h2:mem:aboxdump4";
//		String username = "sa";
//		String password = "";
//
//		OBDADataSource source = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb4"));
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//
//		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
//		Statement st = conn.createStatement();
//
//		FileReader reader = new FileReader("src/test/resources/test/mapping-test-db.sql");
//		BufferedReader in = new BufferedReader(reader);
//		StringBuilder bf = new StringBuilder();
//		String line = in.readLine();
//		while (line != null) {
//			bf.append(line);
//			line = in.readLine();
//		}
//		in.close();
//
//		st.executeUpdate(bf.toString());
//		conn.commit();
//
//        dataSources.add(source);
//
//		OBDADataSource source2 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb5"));
//		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//        dataSources.add(source2);
//
//		OBDADataSource source3 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb6"));
//		source3.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//		source3.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//		source3.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//		source3.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//		source3.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//		source3.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//        dataSources.add(source3);
//
//		/*
//		 * Setting up the OBDA model and the mappings
//		 */
//
//		String sql = "SELECT \"fn\", \"ln\", \"age\", \"schooluri\" FROM \"data\"";
//
//		Predicate q = termFactory.getIRI(OBDALibConstants.QUERY_HEAD, 4);
//		List<Term> headTerms = new LinkedList<Term>();
//		headTerms.add(termFactory.getVariable("fn"));
//		headTerms.add(termFactory.getVariable("ln"));
//		headTerms.add(termFactory.getVariable("age"));
//		headTerms.add(termFactory.getVariable("schooluri"));
//
//		Function head = termFactory.getFunction(q, headTerms);
//
//		Term objectTerm = termFactory.getFunction(termFactory.getIRI("http://schools.com/persons", 2), termFactory.getVariable("fn"),
//				termFactory.getVariable("ln"));
//
//		List<Function> body = new LinkedList<Function>();
//		Predicate person = termFactory.getClassPredicate("Person");
//		Predicate fn = termFactory.getDataPropertyPredicate("fn", COL_TYPE.LITERAL);
//		Predicate ln = termFactory.getDataPropertyPredicate("ln", COL_TYPE.LITERAL);
//		Predicate age = termFactory.getDataPropertyPredicate("age", COL_TYPE.LITERAL);
//		Predicate hasschool = termFactory.getObjectPropertyPredicate("hasschool");
//		Predicate school = termFactory.getClassPredicate("School");
//		body.add(termFactory.getFunction(person, objectTerm));
//		body.add(termFactory.getFunction(fn, objectTerm, termFactory.getVariable("fn")));
//		body.add(termFactory.getFunction(ln, objectTerm, termFactory.getVariable("ln")));
//		body.add(termFactory.getFunction(age, objectTerm, termFactory.getVariable("age")));
//		body.add(termFactory.getFunction(hasschool, objectTerm, termFactory.getVariable("schooluri")));
//		body.add(termFactory.getFunction(school, termFactory.getVariable("schooluri")));
//
//		OBDAMappingAxiom map1 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql), body);
//
//        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
//        OBDAModel model = obdaFactory.createMapping(dataSources, mappingIndex, prefixManager, new MutableOntologyVocabularyImpl());
//
//		QuestMaterializer materializer = new QuestMaterializer(model, false);
//
//		List<Assertion> assertions = materializer.getAssertionList();
//		for (Assertion a : assertions) {
////			System.out.println(a.toString());
//		}
//} catch(Exception e) {
//	assertEquals("Cannot materialize with multiple data sources!", e.getMessage());
//}
//	}
//
//	public void testThreeSourcesNoMappings() throws Exception {
//    try{
//        final Set<OBDADataSource> dataSources = new HashSet<>();
//        final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();
//
//		/*
//		 * Setting the database;
//		 */
//
//		String driver = "org.h2.Driver";
//		String url = "jdbc:h2:mem:aboxdump7";
//		String username = "sa";
//		String password = "";
//
//		OBDADataSource source = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb7"));
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//
//		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
//		Statement st = conn.createStatement();
//
//		FileReader reader = new FileReader("src/test/resources/test/mapping-test-db.sql");
//		BufferedReader in = new BufferedReader(reader);
//		StringBuilder bf = new StringBuilder();
//		String line = in.readLine();
//		while (line != null) {
//			bf.append(line);
//			line = in.readLine();
//		}
//		in.close();
//
//		st.executeUpdate(bf.toString());
//		conn.commit();
//
//        dataSources.add(source);
//
//		OBDADataSource source2 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb8"));
//		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//        dataSources.add(source2);
//
//		OBDADataSource source3 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb9"));
//		source3.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//		source3.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//		source3.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//		source3.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//		source3.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//		source3.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//        dataSources.add(source3);
//
//        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
//        OBDAModel model = obdaFactory.createMapping(dataSources, mappingIndex, prefixManager, new MutableOntologyVocabularyImpl());
//		QuestMaterializer materializer = new QuestMaterializer(model, false);
//
//		List<Assertion> assertions = materializer.getAssertionList();
//		for (Assertion a : assertions) {
////			System.out.println(a.toString());
//		}
//        } catch(Exception e) {
//            assertEquals("Cannot materialize with multiple data sources!", e.getMessage());
//        }
//	}
//
//	public void testThreeSourcesNoMappingsFor1And3() throws Exception {
//    try{
//        final Set<OBDADataSource> dataSources = new HashSet<>();
//        final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();
//
//		/*
//		 * Setting the database;
//		 */
//
//		String driver = "org.h2.Driver";
//		String url = "jdbc:h2:mem:aboxdump5";
//		String username = "sa";
//		String password = "";
//
//		OBDADataSource source = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb11"));
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//
//		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
//		Statement st = conn.createStatement();
//
//		FileReader reader = new FileReader("src/test/resources/test/mapping-test-db.sql");
//		BufferedReader in = new BufferedReader(reader);
//		StringBuilder bf = new StringBuilder();
//		String line = in.readLine();
//		while (line != null) {
//			bf.append(line);
//			line = in.readLine();
//		}
//		in.close();
//
//		st.executeUpdate(bf.toString());
//		conn.commit();
//
//		dataSources.add(source);
//
//		OBDADataSource source2 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb12"));
//		source2.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//		source2.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//		source2.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//		source2.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//		source2.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//		source2.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//        dataSources.add(source2);
//
//		OBDADataSource source3 = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb13"));
//		source3.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//		source3.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//		source3.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//		source3.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//		source3.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//		source3.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//        dataSources.add(source3);
//
//		/*
//		 * Setting up the OBDA model and the mappings
//		 */
//
//		String sql = "SELECT \"fn\", \"ln\", \"age\", \"schooluri\" FROM \"data\"";
//
//		Predicate q = termFactory.getIRI(OBDALibConstants.QUERY_HEAD, 4);
//		List<Term> headTerms = new LinkedList<Term>();
//		headTerms.add(termFactory.getVariable("fn"));
//		headTerms.add(termFactory.getVariable("ln"));
//		headTerms.add(termFactory.getVariable("age"));
//		headTerms.add(termFactory.getVariable("schooluri"));
//
//		Function head = termFactory.getFunction(q, headTerms);
//
//		Term objectTerm = termFactory.getFunction(termFactory.getIRI("http://schools.com/persons", 2), termFactory.getVariable("fn"),
//				termFactory.getVariable("ln"));
//
//		List<Function> body = new LinkedList<Function>();
//		Predicate person = termFactory.getClassPredicate("Person");
//		Predicate fn = termFactory.getDataPropertyPredicate("fn", COL_TYPE.LITERAL);
//		Predicate ln = termFactory.getDataPropertyPredicate("ln", COL_TYPE.LITERAL);
//		Predicate age = termFactory.getDataPropertyPredicate("age", COL_TYPE.LITERAL);
//		Predicate hasschool = termFactory.getObjectPropertyPredicate("hasschool");
//		Predicate school = termFactory.getClassPredicate("School");
//		body.add(termFactory.getFunction(person, objectTerm));
//		body.add(termFactory.getFunction(fn, objectTerm, termFactory.getVariable("fn")));
//		body.add(termFactory.getFunction(ln, objectTerm, termFactory.getVariable("ln")));
//		body.add(termFactory.getFunction(age, objectTerm, termFactory.getVariable("age")));
//		body.add(termFactory.getFunction(hasschool, objectTerm, termFactory.getVariable("schooluri")));
//		body.add(termFactory.getFunction(school, termFactory.getVariable("schooluri")));
//
//		OBDAMappingAxiom map1 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql), body);
//
//        mappingIndex.put(source2.getSourceID(), ImmutableList.of(map1));
//
//        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
//        OBDAModel model = obdaFactory.createMapping(dataSources, mappingIndex, prefixManager,
//				new MutableOntologyVocabularyImpl());
//
//		QuestMaterializer materializer = new QuestMaterializer(model, false);
//
//		List<Assertion> assertions = materializer.getAssertionList();
//		for (Assertion a : assertions) {
////			System.out.println(a.toString());
//		}
//} catch(Exception e) {
//	assertEquals("Cannot materialize with multiple data sources!", e.getMessage());
//}
//	}
//
//	public void testMultipleMappingsOneSource() throws Exception {
//
//		/*
//		 * Setting the database;
//		 */
//
//		String driver = "org.h2.Driver";
//		String url = "jdbc:h2:mem:aboxdump10";
//		String username = "sa";
//		String password = "";
//
//        final Set<OBDADataSource> dataSources = new HashSet<>();
//        final Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>();
//
//		OBDADataSource source = MAPPING_FACTORY.getDataSource(URI.create("http://www.obda.org/testdb100"));
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//		source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//		source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//
//		Connection conn = JDBCConnectionManager.getJDBCConnectionManager().createConnection(source);
//		Statement st = conn.createStatement();
//
//		FileReader reader = new FileReader("src/test/resources/test/mapping-test-db.sql");
//		BufferedReader in = new BufferedReader(reader);
//		StringBuilder bf = new StringBuilder();
//		String line = in.readLine();
//		while (line != null) {
//			bf.append(line);
//			line = in.readLine();
//		}
//		in.close();
//
//		st.executeUpdate(bf.toString());
//		conn.commit();
//
//		/*
//		 * Setting up the OBDA model and the mappings
//		 */
//
//		String sql1 = "SELECT \"fn\", \"ln\" FROM \"data\"";
//		String sql2 = "SELECT \"fn\", \"ln\" FROM \"data\"";
//		String sql3 = "SELECT \"fn\", \"ln\" FROM \"data\"";
//		String sql4 = "SELECT \"fn\", \"ln\", \"age\" FROM \"data\"";
//		String sql5 = "SELECT \"fn\", \"ln\", \"schooluri\" FROM \"data\"";
//		String sql6 = "SELECT \"fn\", \"ln\", \"schooluri\" FROM \"data\"";
//
//		Predicate q = termFactory.getIRI(OBDALibConstants.QUERY_HEAD, 4);
//		List<Term> headTerms = new LinkedList<Term>();
//
//		final Term firstNameVariable = termFactory.getTypedTerm(termFactory.getVariable("fn"), COL_TYPE.STRING);
//		final Term lastNameVariable = termFactory.getTypedTerm(termFactory.getVariable("ln"), COL_TYPE.STRING);
//		final Term ageVariable = termFactory.getTypedTerm(termFactory.getVariable("age"), COL_TYPE.INTEGER);
//		final Term schoolUriVariable = termFactory.getTypedTerm(termFactory.getVariable("schooluri"), COL_TYPE.STRING);
//
//		headTerms.add(firstNameVariable);
//		headTerms.add(lastNameVariable);
//		headTerms.add(ageVariable);
//		headTerms.add(schoolUriVariable);
//
//		Function head = termFactory.getFunction(q, headTerms);
//
//		Term objectTerm = termFactory.getUriTemplate(termFactory.getConstantLiteral("http://schools.com/persons{}{}"),  // R: was binary -- why?
//				firstNameVariable,
//				lastNameVariable);
//
////		List<Function> body = new LinkedList<Function>();
//		Predicate person = termFactory.getClassPredicate("Person");
//		Predicate fn = termFactory.getDataPropertyPredicate("firstn", COL_TYPE.LITERAL);
//		Predicate ln = termFactory.getDataPropertyPredicate("lastn", COL_TYPE.LITERAL);
//		Predicate age = termFactory.getDataPropertyPredicate("agee", COL_TYPE.LITERAL);
//		Predicate hasschool = termFactory.getObjectPropertyPredicate("hasschool");
//		Predicate school = termFactory.getClassPredicate("School");
////		body.add(termFactory.getFunctionalTerm(person, objectTerm));
////		body.add(termFactory.getFunctionalTerm(fn, objectTerm, termFactory.getVariable("fn")));
////		body.add(termFactory.getFunctionalTerm(ln, objectTerm, termFactory.getVariable("ln")));
////		body.add(termFactory.getFunctionalTerm(age, objectTerm, termFactory.getVariable("age")));
////		body.add(termFactory.getFunctionalTerm(hasschool, objectTerm, termFactory.getVariable("schooluri")));
////		body.add(termFactory.getFunctionalTerm(school, termFactory.getVariable("schooluri")));
//
//
//		OBDAMappingAxiom map1 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql1), Arrays.asList(termFactory.getFunction(person, objectTerm)));
//		OBDAMappingAxiom map2 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql2), Arrays.asList(termFactory.getFunction(fn, objectTerm, firstNameVariable)));
//		OBDAMappingAxiom map3 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql3), Arrays.asList(termFactory.getFunction(ln, objectTerm, lastNameVariable)));
//		OBDAMappingAxiom map4 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql4), Arrays.asList(termFactory.getFunction(age, objectTerm, ageVariable)));
//		OBDAMappingAxiom map5 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql5), Arrays.asList(termFactory.getFunction(hasschool, objectTerm, schoolUriVariable)));
//		OBDAMappingAxiom map6 = nativeQLFactory.create(MAPPING_FACTORY.getSQLQuery(sql6), Arrays.asList(termFactory.getFunction(school, schoolUriVariable)));
//
//        dataSources.add(source);
//        mappingIndex.put(source.getSourceID(), ImmutableList.of(map1, map2, map3, map4, map5, map6));
//
//        PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
//        OBDAModel model = obdaFactory.createMapping(dataSources, mappingIndex, prefixManager, new MutableOntologyVocabularyImpl());
//
//		QuestMaterializer materializer = new QuestMaterializer(model, false);
//
//		List<Assertion> assertions = materializer.getAssertionList();
//		int count = materializer.getTripleCount();
//
//		assertEquals(0, count);
//
//		conn.close();
//
//	}

}
