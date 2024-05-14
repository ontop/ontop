package it.unibz.inf.ontop.owlapi;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.si.OntopSemanticIndexLoader;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Class;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.*;

public class TBoxClassificationFromFileTest {

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
        // TODO: load the ontology from resources directory, not using absolute path
        String filePath = "/Users/bendik/Documents/UiB/Master/INFO320-Semantic/ontop/protege/plugin/src/test/resources/test/university.ttl";
        try {
            ontology =  manager.loadOntologyFromOntologyDocument(new File(filePath));
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
        try {
            startReasoner();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    public void testClassifyClasses(){
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(OWLThing(), true);
        subClasses.forEach(System.out::println);
    }

}
