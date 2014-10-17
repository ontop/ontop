package org.semanticweb.ontop.sesame.tests.general;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Class;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.DataProperty;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.DataPropertyAssertion;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.DisjointClasses;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.DisjointDataProperties;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.DisjointObjectProperties;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.FunctionalDataProperty;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.FunctionalObjectProperty;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Literal;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.NamedIndividual;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.ObjectProperty;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.ObjectPropertyAssertion;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.ontop.io.SQLMappingParser;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWL;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class InconsistencyCheckingVirtualTest {

	private String owlfile = "src/test/resources/example/BooksNoAxioms.owl";
	private String obdafile = "src/test/resources/example/exampleBooks.obda";
	private QuestOWL reasoner;
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OBDAModel obdaModel;
	
	QuestPreferences p;
	
	String prefix = "http://meraka/moss/exampleBooks.owl#";
	OWLClass c1 = Class(IRI.create(prefix + "AudioBook"));
	OWLClass c2 = Class(IRI.create(prefix + "Book"));
	
	OWLObjectProperty r1 = ObjectProperty(IRI.create(prefix + "hasMother"));
	OWLObjectProperty r2 = ObjectProperty(IRI.create(prefix + "hasFather"));
	
	OWLDataProperty d1 = DataProperty(IRI.create(prefix + "title"));
	OWLDataProperty d2 = DataProperty(IRI.create(prefix + "name"));

	OWLNamedIndividual a = NamedIndividual(IRI.create(prefix + "a"));
	OWLNamedIndividual b = NamedIndividual(IRI.create(prefix + "b"));
	OWLNamedIndividual c = NamedIndividual(IRI.create(prefix + "c"));
	OWLNamedIndividual book = NamedIndividual(IRI.create(prefix + "book/23/"));
	
	@Before
	public void setUp() throws Exception {

		p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, QuestConstants.TRUE);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, QuestConstants.TRUE);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, QuestConstants.TRUE);
		
		manager = OWLManager.createOWLOntologyManager();
		try {
			ontology = manager.loadOntologyFromOntologyDocument(new File(owlfile));
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testInitialConsistency() {
		//initially the ontology is consistent
		startReasoner();
		assertTrue(reasoner.isQuestConsistent());
	}
	
	private void startReasoner(){
		QuestOWLFactory questOWLFactory = new QuestOWLFactory();
		questOWLFactory.setPreferenceHolder(p);
		obdaModel = OBDADataFactoryImpl.getInstance().getOBDAModel();
		SQLMappingParser mng = new SQLMappingParser(obdaModel);
		try {
			mng.load(new File(obdafile));
			questOWLFactory.setOBDAController(obdaModel);
			reasoner = (QuestOWL) questOWLFactory.createReasoner(ontology);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testDisjointClassInconsistency() throws OWLOntologyCreationException {

		//Male(a), Female(a), disjoint(Male, Female)
		
		manager.addAxiom(ontology, DisjointClasses(c1, c2));
		
		startReasoner();
		
		boolean consistent = reasoner.isQuestConsistent();
		assertFalse(consistent);
		
		manager.removeAxiom(ontology, DisjointClasses(c1, c2));

	} 
	
	@Test
	public void testDisjointObjectPropInconsistency() throws OWLOntologyCreationException {

		//hasMother(a, b), hasFather(a,b), disjoint(hasMother, hasFather)
		manager.addAxiom(ontology,ObjectPropertyAssertion(r1, a, b)); //
		manager.addAxiom(ontology,ObjectPropertyAssertion(r2, a, b)); //
		manager.addAxiom(ontology, DisjointObjectProperties(r1, r2));
		
		startReasoner();
		
		boolean consistent = reasoner.isQuestConsistent();
		assertFalse(consistent);

		manager.removeAxiom(ontology,ObjectPropertyAssertion(r1, a, b)); //
		manager.removeAxiom(ontology,ObjectPropertyAssertion(r2, a, b)); //
		manager.removeAxiom(ontology, DisjointObjectProperties(r1, r2));
	} 
	
	//@Test
	public void testDisjointDataPropInconsistency() throws OWLOntologyCreationException {

		//hasAgeFirst(a, 21), hasAge(a, 21), disjoint(hasAgeFirst, hasAge)
		manager.addAxiom(ontology, DataPropertyAssertion(d1, a, Literal(21)));
		manager.addAxiom(ontology, DataPropertyAssertion(d2, a, Literal(21)));
		manager.addAxiom(ontology, DisjointDataProperties(d1, d2));
		
		startReasoner();
		
		boolean consistent = reasoner.isQuestConsistent();
		assertFalse(consistent);
		
		manager.removeAxiom(ontology, DataPropertyAssertion(d1, a, Literal(21)));
		manager.removeAxiom(ontology, DataPropertyAssertion(d2, a, Literal(21)));
		manager.removeAxiom(ontology, DisjointDataProperties(d1, d2));

	} 
	
	@Test
	public void testFunctionalObjPropInconsistency() throws OWLOntologyCreationException {

		//hasMother(a,b), hasMother(a,c), func(hasMother)
		manager.addAxiom(ontology,ObjectPropertyAssertion(r1, a, b)); //
		manager.addAxiom(ontology,ObjectPropertyAssertion(r1, a, c)); //
		manager.addAxiom(ontology, FunctionalObjectProperty(r1));
		
		startReasoner();
		
		boolean consistent = reasoner.isQuestConsistent();
		assertFalse(consistent);

		manager.removeAxiom(ontology,ObjectPropertyAssertion(r1, a, b)); //
		manager.removeAxiom(ontology,ObjectPropertyAssertion(r1, a, c)); //
		manager.removeAxiom(ontology, FunctionalObjectProperty(r1));
	} 
	
	//@Test
	public void testFunctionalDataPropInconsistency() throws OWLOntologyCreationException {

		//hasAge(a, 18), hasAge(a, 21), func(hasAge)
		manager.addAxiom(ontology, DataPropertyAssertion(d2, book, Literal("Jules Verne")));
	//	manager.addAxiom(ontology, DataPropertyAssertion(d1, a, Literal(21)));
		manager.addAxiom(ontology, FunctionalDataProperty(d2));
		
		startReasoner();
		
		boolean consistent = reasoner.isQuestConsistent();
		assertFalse(consistent);
		
		manager.removeAxiom(ontology, DataPropertyAssertion(d1, a, Literal(18)));
		manager.removeAxiom(ontology, DataPropertyAssertion(d1, a, Literal(21)));
		manager.removeAxiom(ontology, FunctionalDataProperty(d1));
	} 
	
}
