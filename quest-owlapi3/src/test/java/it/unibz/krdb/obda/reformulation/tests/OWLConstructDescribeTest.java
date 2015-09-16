package it.unibz.krdb.obda.reformulation.tests;

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

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

/**
 * This unit test is for testing correctness of construct and describe queries
 * in ontop from the owl api. It is the same as SesameConstructDescribe for the
 * Sesame API, with the only difference that all abox data comes from the owl
 * file as declared named individuals and axioms, AND a property cannot have
 * both constant and uri objects. It must be clear if it's a data property or
 * object property.
 */
public class OWLConstructDescribeTest extends TestCase{

	Connection con = null;
	OWLOntology ontology = null;
	OBDAModel obdaModel = null;
	QuestOWL reasoner = null;
	QuestOWLConnection conn = null;
	QuestOWLStatement st = null;
	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	String owlFile = "src/test/resources/describeConstruct.owl";
	
	@Override
	public void setUp() throws Exception {
		
			// Loading the OWL file
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			ontology = manager.loadOntologyFromOntologyDocument(new File(owlFile));

//			String driver = "org.h2.Driver";
//			String url = "jdbc:h2:mem:aboxdumptestx1";
//			String username = "sa";
//			String password = "";
//
//			OBDADataSource source = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP1testx1"));
//			source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
//			source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
//			source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
//			source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
//			source.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
//			source.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
//
//			obdaModel = fac.getOBDAModel();
//			obdaModel.addSource(source);
			
			QuestPreferences p = new QuestPreferences();
			p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
			p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "false");
			p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "true");
			p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX); 
			p.setCurrentValueOf(QuestPreferences.STORAGE_LOCATION, QuestConstants.INMEMORY);
			p.setCurrentValueOf(QuestPreferences.REWRITE, "false");
			p.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
			QuestOWLFactory factory = new QuestOWLFactory();
//			factory.setOBDAController(obdaModel);
			factory.setPreferenceHolder(p);
			//reasoner.setPreferences(preferences);
			reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
			conn = reasoner.getConnection();
			st = conn.createStatement();
		

	}
	
	@Override
	public void tearDown() throws Exception {
		st.close();
		conn.close();
		reasoner.dispose();	
	}
	
	@Test
	public void testAInsertData() throws Exception {		
		String query = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(4, rs.size());
	}
	
	@Test
	public void testDescribeUri0() throws Exception {
		String query = "DESCRIBE <http://www.semanticweb.org/ontologies/test#p1>";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(0, rs.size());
	}
	
	@Test
	public void testDescribeUri1() throws Exception {
		String query = "DESCRIBE <http://example.org/D>";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(1, rs.size());
	}
	
	@Test
	public void testDescribeUri2() throws Exception {
		String query = "DESCRIBE <http://example.org/C>";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(2, rs.size());
	}
	
	@Test
	public void testDescribeVar0() throws Exception {
		String query = "DESCRIBE ?x WHERE {<http://example.org/C> ?x ?y }";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(0, rs.size());
	}
	
	@Test
	public void testDescribeVar1() throws Exception {
		String query = "DESCRIBE ?x WHERE {?x <http://www.semanticweb.org/ontologies/test#p2> <http://example.org/A>}";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(1, rs.size());
	}
	
	@Test
	public void testDescribeVar2() throws Exception {
		String query = "DESCRIBE ?x WHERE {?x <http://www.semanticweb.org/ontologies/test#p1> ?y}";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(2, rs.size());
	}
	
	@Test
	public void testConstruct0() throws Exception {
		String query = "CONSTRUCT {?s ?p <http://www.semanticweb.org/ontologies/test/p1>} WHERE {?s ?p <http://www.semanticweb.org/ontologies/test/p1>}";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(0, rs.size());
	}
	
	@Test
	public void testConstruct1() throws Exception {
		String query = "CONSTRUCT { ?s ?p <http://example.org/D> } WHERE { ?s ?p <http://example.org/D>}";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(1, rs.size());
	}
	
	@Test
	public void testConstruct2() throws Exception {
		String query = "CONSTRUCT {<http://example.org/C> ?p ?o} WHERE {<http://example.org/C> ?p ?o}";
		List<OWLAxiom> rs = st.executeGraph(query);
		Assert.assertEquals(2, rs.size());
	}
}
