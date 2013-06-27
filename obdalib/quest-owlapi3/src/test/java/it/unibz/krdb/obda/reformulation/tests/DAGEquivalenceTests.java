package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.OntologyFactory;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.dag.Interval;
import it.unibz.krdb.obda.owlrefplatform.core.dag.SemanticIndexRange;

import java.io.File;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.hp.hpl.jena.iri.IRIFactory;

public class DAGEquivalenceTests extends TestCase {

	/**
	 * R1 = R2^- = R3, S1 = S2^- = S3, R1 ISA S1
	 */
	private final String testEquivalenceRoles = "src/test/resources/test/dag/role-equivalence.owl";
	
	/**
	 * A1 = A2^- = A3, B1 = B2^- = B3, C1 = C2^- = C3, C1 ISA B1 ISA A1
	 */
	private final String testEquivalenceRolesInverse = "src/test/resources/test/dag/test-equivalence-roles-inverse.owl";

	/**
	 * A1 = A2 = A3, B1 = B2 = B3, B1 ISA A1
	 */
	private final String testEquivalenceClasses	= "src/test/resources/test/dag/test-equivalence-classes.owl";

	public void setUp() {
		// NO-OP
	}

	public void testIndexClasses() throws Exception {
		String testURI = "http://it.unibz.krdb/obda/ontologies/test.owl#";
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(testEquivalenceClasses));
		Ontology onto = t.translate(owlonto);
		DAG dag = DAGConstructor.getISADAG(onto);

		DAG pureIsa = DAGConstructor.filterPureISA(dag);
		pureIsa.clean();
		pureIsa.index();

		OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		IRIFactory ifac = OBDADataFactoryImpl.getIRIFactory();

		DAGNode node = pureIsa.getClassNode(ofac.createClass(ifac.construct(testURI + "B1")));
		SemanticIndexRange range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		Interval interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getClassNode(ofac.createClass(ifac.construct(testURI + "B2")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getClassNode(ofac.createClass(ifac.construct(testURI + "B3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getClassNode(ofac.createClass(ifac.construct(testURI + "A1")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getClassNode(ofac.createClass(ifac.construct(testURI + "A2")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getClassNode(ofac.createClass(ifac.construct(testURI + "A3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);
	}

	public void testIntervalsRoles() throws Exception {
		String testURI = "http://it.unibz.krdb/obda/ontologies/Ontology1314774461138.owl#";
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(testEquivalenceRoles));
		Ontology onto = t.translate(owlonto);
		DAG dag = DAGConstructor.getISADAG(onto);

		DAG pureIsa = DAGConstructor.filterPureISA(dag);
		pureIsa.clean();
		pureIsa.index();

		OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		IRIFactory ifac = OBDADataFactoryImpl.getIRIFactory();

		DAGNode node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "R1")));
		SemanticIndexRange range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		Interval interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "R2")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "R3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 2);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "S1")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "S2")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);

		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "S3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(interval.getStart(), 1);
		assertEquals(interval.getEnd(), 2);
	}
	
	public void testIntervalsRolesWithInverse() throws Exception {
		String testURI = "http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#";
		OWLAPI3Translator t = new OWLAPI3Translator();
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology owlonto = man.loadOntologyFromOntologyDocument(new File(testEquivalenceRolesInverse));
		Ontology onto = t.translate(owlonto);
		DAG dag = DAGConstructor.getISADAG(onto);
		
		dag.clean();

		DAG pureIsa = DAGConstructor.filterPureISA(dag);
		pureIsa.clean();
		pureIsa.index();
		
		OntologyFactory ofac = OntologyFactoryImpl.getInstance();
		IRIFactory ifac = OBDADataFactoryImpl.getIRIFactory();
		
		DAGNode node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "A1")));
		SemanticIndexRange range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		Interval interval = range.getIntervals().get(0);
		assertEquals(1,interval.getStart());
		assertEquals(3,interval.getEnd());

		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "A2")));
		assertTrue(node == null);
		
		Description d = pureIsa.equi_mappings.get(ofac.createObjectProperty(ifac.construct(testURI + "A2")));
		assertTrue(d.equals(ofac.createObjectProperty(ifac.construct(testURI + "A3"),true)));
		
		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "A3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(1,interval.getStart());
		assertEquals(3,interval.getEnd());

		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "C1")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(3,interval.getStart());
		assertEquals(3,interval.getEnd());

		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "C2")));
		d = pureIsa.equi_mappings.get(ofac.createObjectProperty(ifac.construct(testURI + "C2")));
		assertTrue(d.equals(ofac.createObjectProperty(ifac.construct(testURI + "C3"),true)));
		
		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "C3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(3,interval.getStart());
		assertEquals(3,interval.getEnd());

		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "B1")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(2,interval.getStart());
		assertEquals(3,interval.getEnd());

		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "B2")));
		d = pureIsa.equi_mappings.get(ofac.createObjectProperty(ifac.construct(testURI + "B2")));
		assertTrue(d.equals(ofac.createObjectProperty(ifac.construct(testURI + "B1"),true)));

		node = pureIsa.getRoleNode(ofac.createObjectProperty(ifac.construct(testURI + "B3")));
		range = node.getRange();
		assertEquals(range.getIntervals().size(), 1);
		interval = range.getIntervals().get(0);
		assertEquals(2,interval.getStart());
		assertEquals(3,interval.getEnd());
	}
}
