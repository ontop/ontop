package it.unibz.inf.ontop.spec.fact;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.ontology.*;

import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import org.junit.Ignore;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.utils.MappingTestingTools.*;
import static org.junit.Assert.assertTrue;

public class FactExtractorTest {

    private static ImmutableSet<RDFFact> facts;

    private static final IRIConstant BEN, JOHN, JANE, HAS_SPOUSE, PLAYS_INSTRUMENT, A;
    private static final IRIConstant SPOUSE, HUSBAND, WIFE, PERSON, MUSICIAN;

    static {
        BEN = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#Ben");
        JOHN = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#John");
        JANE = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#Jane");
        HAS_SPOUSE = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#hasSpouse");
        PLAYS_INSTRUMENT = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#playsInstrument");
        A = TERM_FACTORY.getConstantIRI(RDF.TYPE);
        SPOUSE = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#Spouse");
        HUSBAND = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#Husband");
        WIFE = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#Wife");
        PERSON = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#Person");
        MUSICIAN = TERM_FACTORY.getConstantIRI("http://example.org/marriage/voc#Musician");
    }
    /**
     * This setUp method creates the marriage ontology and applies the fact extractor.
     *
     */
    @BeforeClass
    public static void setUp() throws InconsistentOntologyException {
        OntologyBuilder builder = OntologyBuilderImpl.builder(RDF_FACTORY, TERM_FACTORY);
        OClass spouse = builder.declareClass(SPOUSE.getIRI());
        OClass husband = builder.declareClass(HUSBAND.getIRI());
        OClass wife = builder.declareClass(WIFE.getIRI());
        OClass person = builder.declareClass(PERSON.getIRI());
        OClass musician = builder.declareClass(MUSICIAN.getIRI());

        ObjectPropertyExpression hasSpouse = builder.declareObjectProperty(HAS_SPOUSE.getIRI());
        DataPropertyExpression playsInstrument = builder.declareDataProperty(PLAYS_INSTRUMENT.getIRI());

        builder.addSubClassOfAxiom(spouse, person);
        builder.addSubClassOfAxiom(husband, spouse);
        builder.addSubClassOfAxiom(wife, spouse);


        // Test 1
        builder.addClassAssertion(husband, BEN);
        // Test 2
        // TODO: Add :hasSpouse range/domain :Spouse
        builder.addObjectPropertyAssertion(hasSpouse, JOHN, JANE);

        // Test 3
        // TODO: Add :playsInstrument domain :Musician
        builder.addDataPropertyAssertion(
                playsInstrument,
                JOHN,
                TERM_FACTORY.getRDFLiteralConstant("Violin", TYPE_FACTORY.getXsdStringDatatype()));


        Ontology ontology = builder.build();
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
        assertTrue(facts.contains(RDFFact.createTripleFact(BEN, A, SPOUSE)) &&
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
