package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Class;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.*;

public class InconsistencyCheckingVirtualTest {

	private static final String owlFile = "/mysql/example/BooksNoAxioms.owl";
	private static final String obdaFile = "/mysql/example/exampleBooks.obda";
	private static final  String propertyFile = "/mysql/example/exampleBooks.properties";
	private OntopOWLReasoner reasoner;
	private OWLOntology ontology;
	private OWLOntologyManager manager;

	private static final String prefix = "http://meraka/moss/exampleBooks.owl#";
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
		manager = OWLManager.createOWLOntologyManager();
		try (InputStream ontologyFile =  this.getClass().getResourceAsStream(owlFile)) {
			ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
		}
	}
	
	@Test
	public void testInitialConsistency() throws OWLException {
		//initially the ontology is consistent
		startReasoner();
		assertTrue(reasoner.isConsistent());
	}
	
	private void startReasoner() throws OWLOntologyCreationException {
		String obdaFileName =  this.getClass().getResource(obdaFile).toString();
		String propertyFileName =  this.getClass().getResource(propertyFile).toString();

        // Creating a new instance of the reasoner
        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFileName)
				.ontology(ontology)
				.propertyFile(propertyFileName)
				.enableTestMode()
				.build();

	    reasoner = factory.createReasoner(config);
	}
	
	@Test
	public void testDisjointClassInconsistency() throws OWLException {

		//Male(a), Female(a), disjoint(Male, Female)
		
		manager.addAxiom(ontology, DisjointClasses(c1, c2));
		
		startReasoner();
		
		boolean consistent = reasoner.isConsistent();
		assertFalse(consistent);
		
		manager.removeAxiom(ontology, DisjointClasses(c1, c2));

	} 
	
	@Test
	public void testDisjointObjectPropInconsistency() throws OWLException {

		//hasMother(a, b), hasFather(a,b), disjoint(hasMother, hasFather)
		manager.addAxiom(ontology,ObjectPropertyAssertion(r1, a, b)); //
		manager.addAxiom(ontology,ObjectPropertyAssertion(r2, a, b)); //
		manager.addAxiom(ontology, DisjointObjectProperties(r1, r2));
		
		startReasoner();
		
		boolean consistent = reasoner.isConsistent();
		assertFalse(consistent);

		manager.removeAxiom(ontology,ObjectPropertyAssertion(r1, a, b)); //
		manager.removeAxiom(ontology,ObjectPropertyAssertion(r2, a, b)); //
		manager.removeAxiom(ontology, DisjointObjectProperties(r1, r2));
	} 
	
	//@Test
	public void testDisjointDataPropInconsistency() throws OWLException {

		//hasAgeFirst(a, 21), hasAge(a, 21), disjoint(hasAgeFirst, hasAge)
		manager.addAxiom(ontology, DataPropertyAssertion(d1, a, Literal(21)));
		manager.addAxiom(ontology, DataPropertyAssertion(d2, a, Literal(21)));
		manager.addAxiom(ontology, DisjointDataProperties(d1, d2));
		
		startReasoner();
		
		boolean consistent = reasoner.isConsistent();
		assertFalse(consistent);
		
		manager.removeAxiom(ontology, DataPropertyAssertion(d1, a, Literal(21)));
		manager.removeAxiom(ontology, DataPropertyAssertion(d2, a, Literal(21)));
		manager.removeAxiom(ontology, DisjointDataProperties(d1, d2));

	} 
	
	@Test
	public void testFunctionalObjPropInconsistency() throws OWLException {

		//hasMother(a,b), hasMother(a,c), func(hasMother)
		manager.addAxiom(ontology,ObjectPropertyAssertion(r1, a, b)); //
		manager.addAxiom(ontology,ObjectPropertyAssertion(r1, a, c)); //
		manager.addAxiom(ontology, FunctionalObjectProperty(r1));
		
		startReasoner();
		
		boolean consistent = reasoner.isConsistent();
		assertFalse(consistent);

		manager.removeAxiom(ontology,ObjectPropertyAssertion(r1, a, b)); //
		manager.removeAxiom(ontology,ObjectPropertyAssertion(r1, a, c)); //
		manager.removeAxiom(ontology, FunctionalObjectProperty(r1));
	} 
	
	//@Test
	public void testFunctionalDataPropInconsistency() throws OWLException {

		//hasAge(a, 18), hasAge(a, 21), func(hasAge)
		manager.addAxiom(ontology, DataPropertyAssertion(d2, book, Literal("Jules Verne")));
	//	manager.addAxiom(ontology, DataPropertyAssertion(d1, a, Literal(21)));
		manager.addAxiom(ontology, FunctionalDataProperty(d2));
		
		startReasoner();
		
		boolean consistent = reasoner.isConsistent();
		assertFalse(consistent);
		
		manager.removeAxiom(ontology, DataPropertyAssertion(d1, a, Literal(18)));
		manager.removeAxiom(ontology, DataPropertyAssertion(d1, a, Literal(21)));
		manager.removeAxiom(ontology, FunctionalDataProperty(d1));
	} 
	
}
