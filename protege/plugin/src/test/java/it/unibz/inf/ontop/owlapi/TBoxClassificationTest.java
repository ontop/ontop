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

import static org.junit.Assert.*;
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
//        assertTrue(subClasses.containsEntity(OWLNothing()));
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
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(ObjectSomeValuesFrom(r1, OWLThing()), false); //Also add test for inverse
        //subClasses.forEach(System.out::println);
        assertTrue(subClasses.containsEntity(A));
//        assertTrue(subClasses.containsEntity(OWLNothing())); //Maybe add it to QuestOWL??
    }

    @Test
    public void testSubsumptionWithSomeValues2() throws Exception {
        // reset/recreate the ontology in each test
        manager.addAxiom(ontology, SubClassOf(A, C));
        manager.addAxiom(ontology, SubClassOf(B, C));
        manager.addAxiom(ontology, EquivalentClasses(A, B));

        startReasoner();
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(C, false);
        //subClasses.forEach(System.out::println);
        assertTrue(subClasses.containsEntity(A));
        assertTrue(subClasses.containsEntity(B));
        assertEquals(1, subClasses.getNodes().size()); //Flatmap means we dont get a node for each equivalent class
//        assertTrue(subClasses.containsEntity(OWLNothing())); //Maybe add it to QuestOWL??
    }

    @Test
    public void testSubsumptionWithSomeValues3() throws Exception {
        // reset/recreate the ontology in each test
        manager.addAxiom(ontology, SubClassOf(A, C));
        manager.addAxiom(ontology, SubClassOf(B, C));
        manager.addAxiom(ontology, EquivalentClasses(A, B));
        manager.addAxiom(ontology, EquivalentClasses(C, D));

        startReasoner();
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(C, false);
        //subClasses.forEach(System.out::println);
        assertTrue(subClasses.containsEntity(A));
        assertTrue(subClasses.containsEntity(B));
        assertEquals(1, subClasses.getNodes().size()); //Flatmap means we dont get a node for each equivalent class
//        assertTrue(subClasses.containsEntity(OWLNothing())); //Maybe add it to QuestOWL??
    }

    @Test
    public void testSubsumptionWithSomeValues4() throws Exception {
        // reset/recreate the ontology in each test
        manager.addAxiom(ontology, SubClassOf(A, C));
        manager.addAxiom(ontology, SubClassOf(B, C));
        manager.addAxiom(ontology, EquivalentClasses(A, B));
        manager.addAxiom(ontology, EquivalentClasses(C, D));
        manager.addAxiom(ontology, EquivalentClasses(ObjectSomeValuesFrom(r1, OWLThing()), B));

        startReasoner();
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(C, false);
        //subClasses.forEach(System.out::println);
        assertTrue(subClasses.containsEntity(A));
        assertTrue(subClasses.containsEntity(B));
        assertEquals(1, subClasses.getNodes().size()); //Flatmap means we dont get a node for each equivalent class
//        assertTrue(subClasses.containsEntity(OWLNothing())); //Maybe add it to QuestOWL??
    }

    @Test
    public void testBottom() throws Exception {
        manager.addAxiom(ontology, SubClassOf(Bottom, OWLNothing()));
        startReasoner();
        Node<OWLClass> bottomNode = reasoner.getBottomClassNode();
        assertTrue(bottomNode.contains(OWLNothing()));
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
        manager.addAxiom(ontology, SubClassOf(A, E));
        startReasoner();
        NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(A, false);
        assertTrue(superClasses.containsEntity(B));
        assertTrue(superClasses.containsEntity(C));
        assertTrue(superClasses.containsEntity(D));
        assertTrue(superClasses.containsEntity(E));

        assertFalse(superClasses.containsEntity(A)); //A is not a superclass of itself
//        assertTrue(superClasses.containsEntity(OWLThing())); //OWLThing is a superclass of everything
    }

    @Test
    public void testDirectSuperClasses() throws Exception {
        manager.addAxiom(ontology, SubClassOf(A, B));
        manager.addAxiom(ontology, SubClassOf(B, C));
        manager.addAxiom(ontology, SubClassOf(C, D));
        manager.addAxiom(ontology, SubClassOf(A, E));

        startReasoner();
        NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(A, true);
        assertTrue(superClasses.containsEntity(B));
        assertTrue(superClasses.containsEntity(E));

        assertFalse(superClasses.containsEntity(C));
        assertFalse(superClasses.containsEntity(D));
    }

    @Test
    public void testDataPropertySubsumption() throws Exception {
        manager.addAxiom(ontology, SubDataPropertyOf(d1, d2));
        startReasoner();
        NodeSet<OWLDataProperty> subProperties = reasoner.getSubDataProperties(d2, false);
        assertTrue(subProperties.containsEntity(d1));
    }

    @Test
    public void testObjectPropertySubsumption() throws Exception {
        manager.addAxiom(ontology, SubObjectPropertyOf(r1, r2));
        startReasoner();
        NodeSet<OWLObjectPropertyExpression> subProperties = reasoner.getSubObjectProperties(r2, false);
        assertTrue(subProperties.containsEntity(r1));
    }

//    10:30:02 From Guohui Xiao to Everyone:
//    Some(r1) subClassOf Some(r2)
//            10:30:26 From Guohui Xiao to Everyone:
//    Give me all the subClassesOf Some(r2-)
//10:30:33 From Guohui Xiao to Everyone:
//    Some(r1-)
//10:30:54 From Guohui Xiao to Everyone:
//    Some(r1-) subClassOf Some(r2-)
//            10:31:37 From Guohui Xiao to Everyone:
//    Some(r3) subClassOf Some(r4-)
//            10:46:11 From Guohui Xiao to Everyone:
//    Test1:
//            10:46:21 From Guohui Xiao to Everyone:
//    A1 subClassOf B
//10:46:26 From Guohui Xiao to Everyone:
//    A2 subClassOf B
//10:46:34 From Guohui Xiao to Everyone:
//    A1 = A2
//10:46:52 From Guohui Xiao to Everyone:
//    getSubClassesof(B) = {A1, A2}

    @Test
    public void testObjectPropertySubsumption1() throws Exception {
        //FIXME: still work in progress
//        OWLObjectProperty r1Inverse = ObjectProperty(IRI.create(prefix + "r1Inverse"));
        manager.addAxiom(ontology, SubClassOf(ObjectSomeValuesFrom(r1, OWLThing()), ObjectSomeValuesFrom(r2, OWLThing())));


        OWLObjectPropertyExpression r2Inverse = r2.getInverseProperty();

//        manager.addAxiom(ontology, InverseObjectProperties(r1, r1Inverse));
//        manager.addAxiom(ontology, SubObjectPropertyOf(r1, r2));

        startReasoner();
        NodeSet<OWLObjectPropertyExpression> subProperties = reasoner.getSubObjectProperties(r2Inverse, false);
//        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(ObjectSomeValuesFrom(r2Inverse, OWLThing()), false);
//        assertTrue(subClasses.containsEntity(r1));
    }

    // Newly created (simple) tests
    @Test
    public void testInverseObjectProperty() throws Exception {
        manager.addAxiom(ontology, InverseObjectProperties(r1, r2));
        startReasoner();
        Node<OWLObjectPropertyExpression> inverseObjectProperties = reasoner.getInverseObjectProperties(r1);
        assertTrue(inverseObjectProperties.contains(r2));
    }

    @Test
    public void testObjectPropertyDomain() throws Exception {
        manager.addAxiom(ontology, ObjectPropertyDomain(r1, A));
        startReasoner();
        NodeSet<OWLClass> domains = reasoner.getObjectPropertyDomains(r1, false);
        assertTrue(domains.containsEntity(A));
    }

    @Test
    public void testObjectPropertyRange() throws Exception {
        manager.addAxiom(ontology, ObjectPropertyRange(r1, A));
        startReasoner();
        NodeSet<OWLClass> ranges = reasoner.getObjectPropertyRanges(r1, false);
        assertTrue(ranges.containsEntity(A));
    }

    // Individuals
    @Test
    public void testSameIndividual() throws Exception {
        manager.addAxiom(ontology, SameIndividual(a, b));
        startReasoner();
        Node<OWLNamedIndividual> sameIndividuals = reasoner.getSameIndividuals(a);
        assertTrue(sameIndividuals.contains(b));
    }

    @Test
    public void testDifferentIndividuals() throws Exception {
        manager.addAxiom(ontology, DifferentIndividuals(a, b));
        startReasoner();
        NodeSet<OWLNamedIndividual> differentIndividuals = reasoner.getDifferentIndividuals(a);
        assertTrue(differentIndividuals.containsEntity(b));
    }

//    @Test
//    public void testJustClassAssertion() throws Exception {
//        manager.addAxiom(ontology, ClassAssertion(A, a));
//        startReasoner();
//        NodeSet<OWLClass> types = reasoner.getTypes(a, false);
//        assertTrue(types.containsEntity(A));
//    }


}
