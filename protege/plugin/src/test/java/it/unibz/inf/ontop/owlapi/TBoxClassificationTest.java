package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.*;

public class TBoxClassificationTest {

    private OntopOWLReasoner reasoner;
    private OWLOntology ontology;
    private OWLOntologyManager manager;

    private static final String prefix = "http://www.example.org/";
    private final OWLClass Male = Class(IRI.create(prefix + "Male"));
    private final OWLClass Female = Class(IRI.create(prefix + "Female"));
    private final OWLClass Person = Class(IRI.create(prefix + "Person"));

    private final OWLClass Bottom = Class(IRI.create(prefix + "Bottom"));

    private final OWLClass A = Class(IRI.create(prefix + "A"));
    private final OWLClass B = Class(IRI.create(prefix + "B"));
    private final OWLClass C = Class(IRI.create(prefix + "C"));
    private final OWLClass D = Class(IRI.create(prefix + "D"));
    private final OWLClass E = Class(IRI.create(prefix + "E"));
    private final OWLClass F = Class(IRI.create(prefix + "F"));
    private final OWLClass G = Class(IRI.create(prefix + "G"));
    private final OWLObjectProperty r1 = ObjectProperty(IRI.create(prefix + "r1"));
    private final OWLObjectProperty r2 = ObjectProperty(IRI.create(prefix + "r2"));

    private final OWLDataProperty d1 = DataProperty(IRI.create(prefix + "hasAgeFirst"));
    private final OWLDataProperty d2 = DataProperty(IRI.create(prefix + "hasAge"));

    private final OWLNamedIndividual a = NamedIndividual(IRI.create(prefix + "a"));
    private final OWLNamedIndividual b = NamedIndividual(IRI.create(prefix + "b"));
    private final OWLNamedIndividual c = NamedIndividual(IRI.create(prefix + "c"));

    @Before
    public void setUp()  {
        manager = OWLManager.createOWLOntologyManager();
        ontology = Ontology(manager, //
                Declaration(Male),
                Declaration(Female),
                Declaration(r1), //
                Declaration(r2), //
                Declaration(d1), //
                Declaration(d2)
        );
    }

    private void startReasoner() throws Exception {
        Properties properties = new Properties();

        try (OntopSemanticIndexLoader siLoader = OntopSemanticIndexLoader.loadOntologyIndividuals(ontology, properties)) {
            OntopOWLFactory ontopOWLFactory = OntopOWLFactory.defaultFactory();
            OntopSQLOWLAPIConfiguration configuration = siLoader.getConfiguration();

            OWLOntology ontology = configuration.loadInputOntology()
                    .orElseThrow(() -> new RuntimeException("Was expecting an ontology"));

            reasoner = ontopOWLFactory.createReasoner(ontology, configuration);
        }
    }

    @Test
    public void testInitialConsistency() throws Exception {
        //initially the ontology is consistent
        startReasoner();
    }


    @Test
    public void testSimple() throws Exception {
        manager.addAxiom(ontology, SubClassOf(Male, Person));
        manager.addAxiom(ontology, SubClassOf(Female, Person));
        startReasoner();
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(Person, false);
        assertTrue(subClasses.containsEntity(Male));
        assertTrue(subClasses.containsEntity(Female));
        assertTrue(subClasses.containsEntity(OWLNothing()));
    }
    
    @Test
    public void testSubsumptionWithSomeValues() throws Exception {
        // A subClassOf (\exists r1)
        manager.addAxiom(ontology, SubClassOf(A, ObjectSomeValuesFrom(r1, OWLThing())));
        // (\exists r1) subClassOf B
        manager.addAxiom(ontology, SubClassOf(ObjectSomeValuesFrom(r1, OWLThing()), B));
        
        manager.addAxiom(ontology, SubClassOf(D, C));
        manager.addAxiom(ontology, SubClassOf(C, B));
        
        startReasoner();
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(B, false);
        //subClasses.forEach(System.out::println);
        assertTrue(subClasses.containsEntity(C));
        assertTrue(subClasses.containsEntity(D));
        assertTrue(subClasses.containsEntity(A));
//        assertTrue(subClasses.containsEntity(OWLNothing())); //Maybe add it to QuestOWL??
    }

    @Test
    public void testSubsumptionWithSomeValues1() throws Exception {
        // A subClassOf (\exists r1)
        manager.addAxiom(ontology, SubClassOf(A, ObjectSomeValuesFrom(r1, OWLThing())));
        // (\exists r1) subClassOf B
        manager.addAxiom(ontology, SubClassOf(ObjectSomeValuesFrom(r1, OWLThing()), B));

        manager.addAxiom(ontology, SubClassOf(D, C));
        manager.addAxiom(ontology, SubClassOf(C, B));

        startReasoner();
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(ObjectSomeValuesFrom(r1, OWLThing()), false);
        //subClasses.forEach(System.out::println);
        assertTrue(subClasses.containsEntity(A));
        assertTrue(subClasses.containsEntity(OWLNothing())); //Maybe add it to QuestOWL??
    }

    @Test
    public void testBottom() throws Exception {
        OWLClass owlNothing = manager.getOWLDataFactory().getOWLNothing();
        manager.addAxiom(ontology, SubClassOf(Bottom, owlNothing));
        startReasoner();
        Node<OWLClass> bottomNode = reasoner.getBottomClassNode();
        assertTrue(bottomNode.contains(owlNothing));
        assertTrue(bottomNode.contains(Bottom));
    }

    @Test
    public void testDisjointClasses() throws Exception {
        manager.addAxiom(ontology, DisjointClasses(A, B, C));
        manager.addAxiom(ontology, DisjointClasses(D, E, F));
        manager.addAxiom(ontology, DisjointClasses(A, G));


        startReasoner();
        NodeSet<OWLClass> disjointClasses = reasoner.getDisjointClasses(A);
        assertTrue(disjointClasses.containsEntity(B));
        assertTrue(disjointClasses.containsEntity(C));
        assertTrue(disjointClasses.containsEntity(G));
    }

    @Test
    public void testEquivalentClasses() throws Exception {
        manager.addAxiom(ontology, EquivalentClasses(A, B, C));
        manager.addAxiom(ontology, EquivalentClasses(D, E, F));
        manager.addAxiom(ontology, EquivalentClasses(A, G));


        startReasoner();
        Node<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(A);
        assertTrue(equivalentClasses.contains(B));
        assertTrue(equivalentClasses.contains(C));
        assertTrue(equivalentClasses.contains(G));
    }

    @Test
    public void testDisjointObjectProperties() throws Exception {
        manager.addAxiom(ontology, DisjointObjectProperties(r1, r2));
        startReasoner();
        NodeSet<OWLObjectPropertyExpression> disjointProperties = reasoner.getDisjointObjectProperties(r1);
        assertTrue(disjointProperties.containsEntity(r2));
    }

    @Test
    public void testDisjointDataProperties() throws Exception {
        manager.addAxiom(ontology, DisjointDataProperties(d1, d2));
        startReasoner();
        NodeSet<OWLDataProperty> disjointProperties = reasoner.getDisjointDataProperties(d1);
        assertTrue(disjointProperties.containsEntity(d2));
    }

    @Test
    public void testSuperClasses() throws Exception {
        manager.addAxiom(ontology, SubClassOf(A, B));
        manager.addAxiom(ontology, SubClassOf(B, C));
        manager.addAxiom(ontology, SubClassOf(C, D));
        startReasoner();
        NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(A, false);
        assertTrue(superClasses.containsEntity(B));
        assertTrue(superClasses.containsEntity(C));
        assertTrue(superClasses.containsEntity(D));

        assertFalse(superClasses.containsEntity(A)); //A is not a superclass of itself
        assertTrue(superClasses.containsEntity(OWLThing())); //OWLThing is a superclass of everything
    }
}
