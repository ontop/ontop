package it.unibz.inf.ontop.model.term;

import it.unibz.inf.ontop.OntopModelTestingTools;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RDFStarTripleTermTest {

    private static final TermFactory TERM_FACTORY = OntopModelTestingTools.TERM_FACTORY;
    private static final RDFTermType TRIPLE_TYPE = OntopModelTestingTools.TYPE_FACTORY.getRDFStarTripleTermType();

    @Test
    public void tripleTypeIsConcrete() {
        assertNotNull(TRIPLE_TYPE);
        assertFalse(TRIPLE_TYPE.isAbstract());
    }

    @Test
    public void tripleTermFromConstantsSimplifiesLexicalPart() {
        ImmutableFunctionalTerm triple = TERM_FACTORY.getRDFStarTripleFunctionalTerm(
                TERM_FACTORY.getConstantIRI("http://example.com/s"),
                TERM_FACTORY.getConstantIRI("http://example.com/p"),
                TERM_FACTORY.getRDFLiteralConstant("42", XSD.INTEGER));

        ImmutableTerm typeTerm = triple.getTerm(1);
        assertEquals(TERM_FACTORY.getRDFTermTypeConstant(TRIPLE_TYPE), typeTerm);

        ImmutableTerm lexical = triple.getTerm(0).simplify();
        assertTrue(lexical instanceof DBConstant);
        assertEquals("<<{IRI|http://example.com/s}|{IRI|http://example.com/p}|{http://www.w3.org/2001/XMLSchema#integer|42}>>",
                ((DBConstant) lexical).getValue());
    }

    @Test
    public void lexicalEscapingIsStable() {
        ImmutableFunctionalTerm triple = TERM_FACTORY.getRDFStarTripleFunctionalTerm(
                TERM_FACTORY.getConstantIRI("http://example.com/special"),
                TERM_FACTORY.getConstantIRI("http://example.com/p"),
                TERM_FACTORY.getRDFLiteralConstant("value|with{chars}\\", XSD.STRING));

        ImmutableTerm lexical = triple.getTerm(0).simplify();
        assertTrue(lexical instanceof DBConstant);
        assertEquals("<<{IRI|http://example.com/special}|{IRI|http://example.com/p}|{http://www.w3.org/2001/XMLSchema#string|value\\|with\\{chars\\}\\\\}>>",
                ((DBConstant) lexical).getValue());
    }

    @Test
    public void nestedTripleUsesTripleTypeCode() {
        ImmutableFunctionalTerm innerTriple = TERM_FACTORY.getRDFStarTripleFunctionalTerm(
                TERM_FACTORY.getConstantIRI("http://example.com/a"),
                TERM_FACTORY.getConstantIRI("http://example.com/b"),
                TERM_FACTORY.getConstantIRI("http://example.com/c"));

        ImmutableFunctionalTerm outerTriple = TERM_FACTORY.getRDFStarTripleFunctionalTerm(
                TERM_FACTORY.getConstantIRI("http://example.com/s"),
                TERM_FACTORY.getConstantIRI("http://example.com/p"),
                innerTriple);

        ImmutableTerm lexical = outerTriple.getTerm(0).simplify();
        assertTrue(lexical instanceof DBConstant);
        assertTrue(((DBConstant) lexical).getValue().contains("{TRIPLE|<<"));
    }

        @Test
        public void tripleFunctionalTermSimplifiesToConstant() {
                ImmutableTerm simplified = TERM_FACTORY.getRDFStarTripleFunctionalTerm(
                                TERM_FACTORY.getConstantIRI("http://example.com/s"),
                                TERM_FACTORY.getConstantIRI("http://example.com/p"),
                                TERM_FACTORY.getRDFLiteralConstant("42", XSD.INTEGER)).simplify();

                assertTrue(simplified instanceof RDFStarTripleConstant);
                RDFStarTripleConstant tripleConstant = (RDFStarTripleConstant) simplified;
                assertEquals("http://example.com/s",
                                ((IRIConstant) tripleConstant.getSubject()).getIRI().getIRIString());
                assertEquals("http://example.com/p", tripleConstant.getPredicate().getIRI().getIRIString());
                assertEquals("42", ((RDFLiteralConstant) tripleConstant.getObject()).getValue());
        }

        @Test
        public void deserializeTripleConstantFromLexicalValue() {
                ImmutableFunctionalTerm innerTriple = TERM_FACTORY.getRDFStarTripleFunctionalTerm(
                                TERM_FACTORY.getConstantIRI("http://example.com/a"),
                                TERM_FACTORY.getConstantIRI("http://example.com/b"),
                                TERM_FACTORY.getConstantIRI("http://example.com/c"));

                ImmutableFunctionalTerm outerTriple = TERM_FACTORY.getRDFStarTripleFunctionalTerm(
                                TERM_FACTORY.getConstantIRI("http://example.com/s"),
                                TERM_FACTORY.getConstantIRI("http://example.com/p"),
                                innerTriple);

                String lexical = ((DBConstant) outerTriple.getTerm(0).simplify()).getValue();
                RDFConstant reconstructed = TERM_FACTORY.getRDFConstant(lexical, TRIPLE_TYPE);
                assertTrue(reconstructed instanceof RDFStarTripleConstant);
                RDFStarTripleConstant tripleConstant = (RDFStarTripleConstant) reconstructed;
                assertTrue(tripleConstant.getObject() instanceof RDFStarTripleConstant);
        }
}
