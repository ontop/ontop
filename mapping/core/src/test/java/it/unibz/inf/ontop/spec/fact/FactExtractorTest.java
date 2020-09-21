package it.unibz.inf.ontop.spec.fact;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.ontology.Ontology;

import it.unibz.inf.ontop.spec.ontology.RDFFact;

import it.unibz.inf.ontop.spec.ontology.impl.RDFFactImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.Optional;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.assertTrue;

public class FactExtractorTest {

    private static final String ONTOLOGY_FILE = "src/test/resources/factMarriage/marriage.ttl";
    private ImmutableSet<RDFFact> facts;

    private static final IRIConstant BEN = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#Ben");
    private static final IRIConstant JOHN = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#John");
    private static final IRIConstant JANE = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#Jane");
    private static final IRIConstant A = TERM_FACTORY.getConstantIRI(RDF.TYPE);
    private static final IRIConstant SPOUSE = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#Spouse");
    private static final IRIConstant PERSON = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#Person");
    private static final IRIConstant MUSICIAN = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#Musician");


    @Before
    public void setUp() throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology owl = man.loadOntologyFromOntologyDocument(new File(ONTOLOGY_FILE));
        Ontology ontology = OWLAPI_TRANSLATOR.translateAndClassify(owl);
        facts = FACT_EXTRACTOR.extractAndSelect(Optional.of(ontology));
        System.out.print("Facts: ");
        System.out.println(facts);
    }


    /**
     * This tests that we can reason over rdfs:subclassOf.
     * Ontology contains;   A-box: :Ben a :Husband
     *                      T-box: :Husband rdfs:subclassOf :Spouse
     *                             :Spouse rdfs:subclassOf :Person
     *                      -> :Ben a :Spouse; a :Person.
     */
    @Ignore
    @Test
    public void subclassOf() {
        assertTrue(facts.contains(RDFFactImpl.createTripleFact(BEN, A, SPOUSE)) &&
                facts.contains(RDFFact.createTripleFact(BEN, A, PERSON)));
    }

    /**
     * This tests that we can infer type from object
     * property domain & range.
     * Ontology contains;   A-box: :John :hasSpouse :Jane
     *                      T-box: :hasSpouse domain :Spouse; range :Spouse.
     *                      ->  :John a :Spouse.
     *                          :Jane a :Spouse.
     */
    @Ignore
    @Test
    public void objectSomeValueOf() {
        assertTrue(facts.contains(RDFFact.createTripleFact(JOHN, A, SPOUSE)) &&
                facts.contains(RDFFact.createTripleFact(JANE, A, SPOUSE)));
    }

    /**
     * This tests that we can infer type from data
     * property domain.
     * Ontology contains;   A-box: :John :playsInstrument "Violin"^^xsd:string
     *                      T-box: :playsInstrument domain :Musician
     *                      ->  :John a :Musician.
     */
    @Ignore
    @Test
    public void dataSomeValueOf() {
        assertTrue(facts.contains(RDFFact.createTripleFact(JOHN, A, MUSICIAN)));
    }

}
