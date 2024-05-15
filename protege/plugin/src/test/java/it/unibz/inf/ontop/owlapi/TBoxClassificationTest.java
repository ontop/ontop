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
import static org.junit.Assert.assertFalse;
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
        assertTrue(subClasses.containsEntity(C));
        assertTrue(subClasses.containsEntity(D));
        assertTrue(subClasses.containsEntity(A));
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
        assertTrue(subClasses.containsEntity(A));
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
        assertEquals(1, subClasses.getNodes().size());
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
        assertEquals(1, subClasses.getNodes().size());
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
        assertTrue(subClasses.containsEntity(A));
        assertTrue(subClasses.containsEntity(B));
        assertEquals(1, subClasses.getNodes().size());
    }

    @Test
    public void testSubsumptionWithOWLThing() throws Exception {
        manager.addAxiom(ontology, SubClassOf(Male, Person));
        manager.addAxiom(ontology, SubClassOf(Female, Person));
        startReasoner();
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(OWLThing(), false);
        assertTrue(subClasses.containsEntity(Male));
        assertTrue(subClasses.containsEntity(Female));
        assertTrue(subClasses.containsEntity(Person));
    }

    @Test
    public void testSubsumptionWithOWLThingDirect() throws Exception {
        manager.addAxiom(ontology, SubClassOf(Male, Person));
        manager.addAxiom(ontology, SubClassOf(Female, Person));
        startReasoner();
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(OWLThing(), true);
        assertFalse(subClasses.containsEntity(Male));
        assertFalse(subClasses.containsEntity(Female));
        assertTrue(subClasses.containsEntity(Person));
    }

//    @Test
//    public void testSubsumptionWithOWLThingDirect1() throws Exception {
//        // FIXME: Analyzing the resulting DAG displays weird results.
//        // Sub of A contains many elements. VertexIndex also now includes OWLThing.
//
//        manager.addAxiom(ontology, SubClassOf(Male, Person));
//        manager.addAxiom(ontology, SubClassOf(Female, Person));
//        manager.addAxiom(ontology, EquivalentClasses(A, OWLThing()));
//        startReasoner();
//        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(OWLThing(), true);
//        assertTrue(subClasses.containsEntity(A));
//        assertTrue(subClasses.containsEntity(OWLThing()));
//        assertFalse(subClasses.containsEntity(Person));
//        assertFalse(subClasses.containsEntity(Male));
//        assertFalse(subClasses.containsEntity(Female));
//    }

    @Test
    public void testSubsumptionWithSomeValues5() throws Exception {
        // A subclass of R some OWLThing
        manager.addAxiom(ontology, SubClassOf(A, ObjectSomeValuesFrom(r1, OWLThing())));
        // Domain(r1) is B
        manager.addAxiom(ontology, ObjectPropertyDomain(r1, B));
        startReasoner();
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(B, false);
        assertTrue(subClasses.containsEntity(A));
    }

    @Test
    public void testSubsumptionWithSomeValues6() throws Exception {
        // FIXME: Not complete
        // A subclass of R some OWLThing
        manager.addAxiom(ontology, SubClassOf(A, ObjectSomeValuesFrom(r1, OWLThing())));
        // Domain(r1) is B
        manager.addAxiom(ontology, ObjectPropertyDomain(r1, B));
        startReasoner();
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(ObjectSomeValuesFrom(r1, OWLThing()), false);
        assertTrue(subClasses.containsEntity(A));
    }

    @Test
    public void testBottom() throws Exception {
        manager.addAxiom(ontology, SubClassOf(Bottom, OWLNothing()));
        startReasoner();
        Node<OWLClass> bottomNode = reasoner.getBottomClassNode();
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

//    @Test
//    public void testDisjointClassesSomeValuesFrom() throws Exception {
//        OWLObjectSomeValuesFrom someR1 = ObjectSomeValuesFrom(r1, OWLThing());
//        OWLObjectSomeValuesFrom someR2 = ObjectSomeValuesFrom(r2, OWLThing());
//
//        manager.addAxiom(ontology, DisjointClasses(A, B, C));
//        manager.addAxiom(ontology, DisjointClasses(D, E, F));
//        manager.addAxiom(ontology, DisjointClasses(A, G));
//        manager.addAxiom(ontology, DisjointClasses(someR1, someR2));
//
//        startReasoner();
//        NodeSet<OWLClass> disjointClasses = reasoner.getDisjointClasses(someR1);
//        assertTrue(disjointClasses.containsEntity(someR2));
//    }

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
    public void testEquivalentClassesSomeValuesFrom() throws Exception {
        OWLObjectSomeValuesFrom someR1 = ObjectSomeValuesFrom(r1, OWLThing());
        OWLObjectSomeValuesFrom someR2 = ObjectSomeValuesFrom(r2, OWLThing());

        manager.addAxiom(ontology, EquivalentClasses(A, someR1));
        manager.addAxiom(ontology, EquivalentClasses(B, someR2));
        manager.addAxiom(ontology, EquivalentClasses(someR1, someR2));

        startReasoner();
        Node<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(someR1);
        assertTrue(equivalentClasses.contains(A));
        assertTrue(equivalentClasses.contains(B));
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
    public void testSuperClassesWithOWLNothing() throws Exception {
        manager.addAxiom(ontology, SubClassOf(A, B));
        startReasoner();
        NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(OWLNothing(), false);
        assertTrue(superClasses.containsEntity(A));
        assertTrue(superClasses.containsEntity(B));
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
    public void testDataPropertyEquivalence() throws Exception {
        manager.addAxiom(ontology, EquivalentDataProperties(d1, d2));
        startReasoner();
        Node<OWLDataProperty> equivalentProperties = reasoner.getEquivalentDataProperties(d1);
        assertTrue(equivalentProperties.contains(d2));
    }

    @Test
    public void testObjectPropertySubsumptionTopDirect() throws Exception {
        manager.addAxiom(ontology, SubObjectPropertyOf(r1, r2));
        startReasoner();

        OWLObjectProperty owlTopObjectProperty = manager.getOWLDataFactory().getOWLTopObjectProperty();

        NodeSet<OWLObjectPropertyExpression> subProperties = reasoner.getSubObjectProperties(owlTopObjectProperty, true);
        assertFalse(subProperties.containsEntity(r1));
        assertTrue(subProperties.containsEntity(r2));
        assertTrue(subProperties.containsEntity(r2.getInverseProperty()));
    }

    @Test
    public void testObjectPropertySubsumptionTop() throws Exception {
        manager.addAxiom(ontology, SubObjectPropertyOf(r1, r2));
        startReasoner();

        OWLObjectProperty owlTopObjectProperty = manager.getOWLDataFactory().getOWLTopObjectProperty();

        NodeSet<OWLObjectPropertyExpression> subProperties = reasoner.getSubObjectProperties(owlTopObjectProperty, false);
        assertTrue(subProperties.containsEntity(r1));
        assertTrue(subProperties.containsEntity(r1.getInverseProperty()));
        assertTrue(subProperties.containsEntity(r2));
        assertTrue(subProperties.containsEntity(r2.getInverseProperty()));
    }

    @Test
    public void testObjectPropertySubsumptionMissingTBox() throws Exception {
        manager.addAxiom(ontology, SubObjectPropertyOf(r1, r2));
        startReasoner();

        OWLObjectProperty r3 = ObjectProperty(IRI.create(prefix + "r3"));

        NodeSet<OWLObjectPropertyExpression> subProperties = reasoner.getSubObjectProperties(r3, false);
        assertTrue(subProperties.isEmpty());
    }

    @Test
    public void testObjectPropertySuperclasses() throws Exception {
        manager.addAxiom(ontology, SubObjectPropertyOf(r1, r2));
        startReasoner();
        NodeSet<OWLObjectPropertyExpression> superProperties = reasoner.getSuperObjectProperties(r1, true);
        assertTrue(superProperties.containsEntity(r2));
    }

    @Test
    public void testObjectPropertySuperclassesInverse() throws Exception {
        manager.addAxiom(ontology, SubObjectPropertyOf(r1, r2));
        startReasoner();
        NodeSet<OWLObjectPropertyExpression> superProperties = reasoner.getSuperObjectProperties(r1.getInverseProperty(), true);
        assertTrue(superProperties.containsEntity(r2.getInverseProperty()));
    }

    @Test
    public void testObjectPropertyEquivalence() throws Exception {
        manager.addAxiom(ontology, EquivalentObjectProperties(r1, r2));
        startReasoner();
        Node<OWLObjectPropertyExpression> equivalentProperties = reasoner.getEquivalentObjectProperties(r1);
        assertTrue(equivalentProperties.contains(r2));
    }

    @Test
    public void testObjectPropertyDisjointness() throws Exception {
        manager.addAxiom(ontology, DisjointObjectProperties(r1, r2));
        startReasoner();
        NodeSet<OWLObjectPropertyExpression> disjointProperties = reasoner.getDisjointObjectProperties(r1);
        assertTrue(disjointProperties.containsEntity(r2));
    }

    @Test
    public void testObjectPropertyInverseProperties() throws Exception {
        manager.addAxiom(ontology, InverseObjectProperties(r1, r2));
        startReasoner();
        Node<OWLObjectPropertyExpression> inverseProperties = reasoner.getInverseObjectProperties(r1);
        assertTrue(inverseProperties.contains(r1.getInverseProperty()));
        assertTrue(inverseProperties.contains(r2));
    }

    @Test
    public void testObjectPropertyInverseProperties1() throws Exception {
        manager.addAxiom(ontology, InverseObjectProperties(r1, r2));
        startReasoner();
        Node<OWLObjectPropertyExpression> inverseProperties = reasoner.getInverseObjectProperties(r1.getInverseProperty());
        assertTrue(inverseProperties.contains(r1));
        assertTrue(inverseProperties.contains(r2.getInverseProperty()));
    }

    @Test
    public void testObjectPropertyNotInOntology() throws Exception {
        OWLObjectProperty rx = ObjectProperty(IRI.create(prefix + "rx"));
        startReasoner();
        NodeSet<OWLObjectPropertyExpression> superProperties = reasoner.getSuperObjectProperties(rx, true);
        assertTrue(superProperties.isEmpty());
    }

    @Test
    public void testObjectPropertyDomains() throws Exception {
        manager.addAxiom(ontology, ObjectPropertyDomain(r1, A));
        manager.addAxiom(ontology, ObjectPropertyDomain(r1, B));
        manager.addAxiom(ontology, ObjectPropertyRange(r1, C));
        manager.addAxiom(ontology, ObjectPropertyRange(r1, D));
        startReasoner();
        NodeSet<OWLClass> domains = reasoner.getObjectPropertyDomains(r1, false);
        assertTrue(domains.containsEntity(A));
        assertTrue(domains.containsEntity(B));
        assertFalse(domains.containsEntity(C));
        assertFalse(domains.containsEntity(D));
    }

    @Test
    public void testObjectPropertyDomainsInverse() throws Exception {
        manager.addAxiom(ontology, ObjectPropertyDomain(r1, A));
        manager.addAxiom(ontology, ObjectPropertyDomain(r1, B));
        manager.addAxiom(ontology, ObjectPropertyRange(r1, C));
        manager.addAxiom(ontology, ObjectPropertyRange(r1, D));
        startReasoner();
        NodeSet<OWLClass> domains = reasoner.getObjectPropertyDomains(r1.getInverseProperty(), false);
        assertFalse(domains.containsEntity(A));
        assertFalse(domains.containsEntity(B));
        assertTrue(domains.containsEntity(C));
        assertTrue(domains.containsEntity(D));
    }

    @Test
    public void testObjectPropertyRanges() throws Exception {
        manager.addAxiom(ontology, ObjectPropertyDomain(r1, A));
        manager.addAxiom(ontology, ObjectPropertyDomain(r1, B));
        manager.addAxiom(ontology, ObjectPropertyRange(r1, C));
        manager.addAxiom(ontology, ObjectPropertyRange(r1, D));
        startReasoner();
        NodeSet<OWLClass> ranges = reasoner.getObjectPropertyRanges(r1, false);
        assertFalse(ranges.containsEntity(A));
        assertFalse(ranges.containsEntity(B));
        assertTrue(ranges.containsEntity(C));
        assertTrue(ranges.containsEntity(D));
    }

    @Test
    public void testObjectPropertyRangesInverse() throws Exception {
        manager.addAxiom(ontology, ObjectPropertyDomain(r1, A));
        manager.addAxiom(ontology, ObjectPropertyDomain(r1, B));
        manager.addAxiom(ontology, ObjectPropertyRange(r1, C));
        manager.addAxiom(ontology, ObjectPropertyRange(r1, D));
        startReasoner();
        NodeSet<OWLClass> ranges = reasoner.getObjectPropertyRanges(r1.getInverseProperty(), false);
        assertTrue(ranges.containsEntity(A));
        assertTrue(ranges.containsEntity(B));
        assertFalse(ranges.containsEntity(C));
        assertFalse(ranges.containsEntity(D));
    }

    @Test
    public void testObjectPropertyEquivalenceWithTop() throws Exception {

        String w3Namespace = OWLThing().getIRI().getNamespace();
        OWLObjectProperty owlBottomObjectProperty = ObjectProperty(IRI.create(w3Namespace + "bottomObjectProperty"));

        manager.addAxiom(ontology, EquivalentObjectProperties(r1, r2));
        startReasoner();
        Node<OWLObjectPropertyExpression> equivalentProperties = reasoner.getEquivalentObjectProperties(owlBottomObjectProperty);
        assertTrue(equivalentProperties.contains(r2));
        assertTrue(equivalentProperties.contains(r2.getInverseProperty()));
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
        OWLObjectSomeValuesFrom someR1 = ObjectSomeValuesFrom(r1, OWLThing());
        OWLObjectSomeValuesFrom someR2 = ObjectSomeValuesFrom(r2, OWLThing());

        manager.addAxiom(ontology, SubClassOf(someR1, someR2));


//        OWLObjectPropertyExpression r2Inverse = r2.getInverseProperty();

//        manager.addAxiom(ontology, InverseObjectProperties(r1, r1Inverse));
//        manager.addAxiom(ontology, SubObjectPropertyOf(r1, r2));

        startReasoner();
        NodeSet<OWLObjectPropertyExpression> subProperties = reasoner.getSubObjectProperties(r2, false);
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
}
