package it.unibz.krdb.obda.owlrefplatform.core.abox.tests;

import it.unibz.krdb.obda.owlrefplatform.core.GraphGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.dag.SemanticIndexRange;
import it.unibz.krdb.obda.owlrefplatform.core.dag.SemanticIndexRange.Interval;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2Translator;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import junit.framework.TestCase;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class DAGEquivalenceTests extends TestCase {

	/***
	 * R1 = R2^- = R3, S1 = S2^- = S3, R1 ISA S1
	 */
	private final String	testEquivalenceRoles		= "src/test/resources/test/dag/role-equivalence.owl";

	
	/***
	 * A1 = A2^- = A3, B1 = B2^- = B3, C1 = C2^- = C3, C1 ISA B1 ISA A1
	 */
	private final String	testEquivalenceRolesInverse		= "src/test/resources/test/dag/test-equivalence-roles-inverse.owl";

	
	/**
	 * A1 = A2 = A3, B1 = B2 = B3, B1 ISA A1
	 */
	private final String	testEquivalenceClasses		= "src/test/resources/test/dag/test-equivalence-classes.owl";

//	private final String	testEquivalenceClassesInv	= "src/test/resources/test/semanticIndex_ontologies/equivalence-classes-with-exists-inverse.owl";

	public void setUp() {

	}

	public void testIndexClasses() throws Exception {

		String testURI = "http://it.unibz.krdb/obda/ontologies/test.owl#";
		OWLAPI2Translator t = new OWLAPI2Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromPhysicalURI(new File(testEquivalenceClasses).toURI());
		Ontology onto = t.translate(owlonto);
		DAG dag = DAGConstructor.getISADAG(onto);

		DAG pureIsa = DAGConstructor.filterPureISA(dag);
//		GraphGenerator.dumpISA(pureIsa, "original");
		pureIsa.clean();
		pureIsa.index();
//		GraphGenerator.dumpISA(pureIsa, "no-cycles");

		OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		DAGNode node = pureIsa.getClassNode(ofac.createClass(URI.create(testURI + "B1")));
		SemanticIndexRange range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		Interval interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getClassNode(ofac.createClass(URI.create(testURI + "B2")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getClassNode(ofac.createClass(URI.create(testURI + "B3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getClassNode(ofac.createClass(URI.create(testURI + "A1")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getClassNode(ofac.createClass(URI.create(testURI + "A2")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getClassNode(ofac.createClass(URI.create(testURI + "A3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

	}

	public void testIntervalsRoles() throws Exception {

		String testURI = "http://it.unibz.krdb/obda/ontologies/Ontology1314774461138.owl#";
		OWLAPI2Translator t = new OWLAPI2Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromPhysicalURI(new File(testEquivalenceRoles).toURI());
		Ontology onto = t.translate(owlonto);
		DAG dag = DAGConstructor.getISADAG(onto);

		DAG pureIsa = DAGConstructor.filterPureISA(dag);
//		GraphGenerator.dumpISA(pureIsa, "original");
		pureIsa.clean();
		pureIsa.index();
//		GraphGenerator.dumpISA(pureIsa, "no-cycles");

		OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		DAGNode node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "R1")));
		SemanticIndexRange range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		Interval interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "R2")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "R3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "S1")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "S2")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "S3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		

	}
	
	public void testIntervalsRolesWithInverse() throws Exception {

		String testURI = "http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#";
		OWLAPI2Translator t = new OWLAPI2Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromPhysicalURI(new File(testEquivalenceRolesInverse).toURI());
		Ontology onto = t.translate(owlonto);
		DAG dag = DAGConstructor.getISADAG(onto);
		
		
		dag.clean();

		DAG pureIsa = DAGConstructor.filterPureISA(dag);
		pureIsa.clean();
		pureIsa.index();
		
		try {
			GraphGenerator.dumpISA(pureIsa, "isa");
		} catch (IOException ex) {
			// Does nothing. This is to avoid trivial test failure "Cannot run program /usr/bin/dot".
		}
		
		OntologyFactory ofac = OntologyFactoryImpl.getInstance();

		DAGNode equi = pureIsa.getRoleNode((Property)pureIsa.equi_mappings.get(ofac.createObjectProperty(URI.create(testURI + "A1"))));
		
		DAGNode node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "A1")));
		SemanticIndexRange range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		Interval interval = range.getIntervals().get(0);
		assertEquals(1,interval.getStart());
		assertEquals(3,interval.getEnd());

		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "A2")));
		assertTrue(node == null);
		
		Description d = pureIsa.equi_mappings.get(ofac.createObjectProperty(URI.create(testURI + "A2")));
		assertTrue(d.equals(ofac.createObjectProperty(URI.create(testURI + "A3"),true)));
//		node = pureIsa.getRoleNode(d);
//		range = node.getRange();
//		assertEquals(range.getIntervals().size(), 1);
//		interval = range.getIntervals().get(0);
//		assertEquals(1,interval.getStart());
//		assertEquals(3,interval.getEnd());
		
		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "A3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(1,interval.getStart());
		assertEquals(3,interval.getEnd());

		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "C1")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(3,interval.getStart());
		assertEquals(3,interval.getEnd());

		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "C2")));
		d = pureIsa.equi_mappings.get(ofac.createObjectProperty(URI.create(testURI + "C2")));
		assertTrue(d.equals(ofac.createObjectProperty(URI.create(testURI + "C3"),true)));
		
		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "C3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(3,interval.getStart());
		assertEquals(3,interval.getEnd());

		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "B1")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(2,interval.getStart());
		assertEquals(3,interval.getEnd());

		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "B2")));
		d = pureIsa.equi_mappings.get(ofac.createObjectProperty(URI.create(testURI + "B2")));
		assertTrue(d.equals(ofac.createObjectProperty(URI.create(testURI + "B1"),true)));

		node = pureIsa.getRoleNode(ofac.createObjectProperty(URI.create(testURI + "B3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(2,interval.getStart());
		assertEquals(3,interval.getEnd());

	
	}
}
