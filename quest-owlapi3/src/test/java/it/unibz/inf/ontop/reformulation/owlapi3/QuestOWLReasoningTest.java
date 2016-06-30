package it.unibz.inf.ontop.reformulation.owlapi3;

import static org.junit.Assert.*;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Class;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.ClassAssertion;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.DataProperty;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Declaration;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.DisjointClasses;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.NamedIndividual;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.ObjectProperty;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Ontology;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.Node;

import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWL;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLFactory;

/**
 * Test implementation of OWL reasoner in QuestOWL.
 * {@link QuestOWL}
 */
public class QuestOWLReasoningTest {
	
	private QuestOWL reasoner;
	private OWLOntology ontology;
	private OWLOntologyManager manager;

	/*
	 * Create Objects that will be use in ontology assertions.
	 */
	String prefix = "http://www.example.org/";
	OWLClass cDirect = Class(IRI.create(prefix + "Direct"));
	OWLClass cNotDirect = Class(IRI.create(prefix + "NotDirect"));

	OWLClass cA = Class(IRI.create(prefix + "A"));
	OWLClass cB = Class(IRI.create(prefix + "B"));
	OWLClass cC = Class(IRI.create(prefix + "C"));
	
	
	@Before
	public void setUp() throws Exception {
		manager = OWLManager.createOWLOntologyManager();
		ontology = Ontology(manager, 
				Declaration(cDirect),
				Declaration(cNotDirect),
				Declaration(cA),
				Declaration(cB),
				Declaration(cC) 
				);
	}
	
	/** 
	 * TODO: Check if it is a good configuration.
	 * */
	private void startReasoner() {
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");

		QuestOWLFactory factory = new QuestOWLFactory();
        QuestOWLConfiguration config = QuestOWLConfiguration.builder().preferences(p).build();
        reasoner = factory.createReasoner(ontology, config);
	}

	
	@Test
	public void testGetSubClasses() throws OWLOntologyCreationException {

		//Direct subclass
		manager.addAxiom(ontology, OWLFunctionalSyntaxFactory.SubClassOf(cDirect,cB));
		//Not direct subclass
		manager.addAxiom(ontology, OWLFunctionalSyntaxFactory.SubClassOf(cNotDirect,cDirect));

		startReasoner();
		
		//Get only direct subclass
		NodeSet<OWLClass> subClasses = reasoner.getSubClasses(cB, true);
		assertTrue(subClasses.containsEntity(cDirect));
		assertFalse(subClasses.containsEntity(cNotDirect));
		
		//Get all subclass
		NodeSet<OWLClass> subNotDirectClasses = reasoner.getSubClasses(cB, false);
		assertTrue(subNotDirectClasses.containsEntity(cDirect));
		assertTrue(subNotDirectClasses.containsEntity(cNotDirect));
	} 
	
	@Test
	public void testGetSuperClasses() throws OWLOntologyCreationException {

		//Direct superclass
		manager.addAxiom(ontology, OWLFunctionalSyntaxFactory.SubClassOf(cB, cDirect));
		//Not direct subclass
		manager.addAxiom(ontology, OWLFunctionalSyntaxFactory.SubClassOf(cDirect, cNotDirect));

		startReasoner();
		
		//Get only direct superclass
		NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(cB, true);
		assertTrue(superClasses.containsEntity(cDirect));
		assertFalse(superClasses.containsEntity(cNotDirect));
		
		//Get all superclass
		NodeSet<OWLClass> superNotDirectClasses = reasoner.getSuperClasses(cB, false);
		assertTrue(superNotDirectClasses.containsEntity(cDirect));
		assertTrue(superNotDirectClasses.containsEntity(cNotDirect));
	} 
	

}
