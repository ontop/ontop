package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import junit.framework.TestCase;
import org.junit.Test;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;

public class ImmutableHomomorphismTest extends TestCase {

    @Test
    public void test_empty_from() {
        ImmutableHomomorphism h = ImmutableHomomorphism.builder().extend(TERM_FACTORY.getVariable("x"),
                TERM_FACTORY.getRDFLiteralConstant("a", XSD.STRING)).build();
        ImmutableHomomorphismIterator i = new ImmutableHomomorphismIterator(h, ImmutableList.of(), ImmutableList.of());
        assertTrue(i.hasNext());
        assertTrue(i.hasNext());
        assertEquals(h, i.next());
        assertFalse(i.hasNext());
        assertFalse(i.hasNext());
    }

    @Test
    public void test_backtrack() {
        ImmutableHomomorphism h = ImmutableHomomorphism.builder().build();
        ImmutableList<DataAtom> from = ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getVariable("x"), RDF_FACTORY.createIRI("http://P"),  TERM_FACTORY.getVariable("y")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getVariable("y"), RDF_FACTORY.createIRI("http://Q"), TERM_FACTORY.getVariable("z")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getVariable("z"), RDF_FACTORY.createIRI("http://R"), TERM_FACTORY.getVariable("w")));
        ImmutableList<DataAtom> to = ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getRDFLiteralConstant("a", XSD.STRING),
                        RDF_FACTORY.createIRI("http://P"),  TERM_FACTORY.getRDFLiteralConstant("b", XSD.STRING)),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getRDFLiteralConstant("b", XSD.STRING),
                        RDF_FACTORY.createIRI("http://Q"), TERM_FACTORY.getRDFLiteralConstant("c", XSD.STRING)),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getRDFLiteralConstant("a0", XSD.STRING),
                        RDF_FACTORY.createIRI("http://P"),  TERM_FACTORY.getRDFLiteralConstant("b0", XSD.STRING)),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getRDFLiteralConstant("b0", XSD.STRING),
                        RDF_FACTORY.createIRI("http://Q"), TERM_FACTORY.getRDFLiteralConstant("c0", XSD.STRING)),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getRDFLiteralConstant("c0", XSD.STRING),
                        RDF_FACTORY.createIRI("http://R"), TERM_FACTORY.getRDFLiteralConstant("d0", XSD.STRING)));

        ImmutableHomomorphismIterator i = new ImmutableHomomorphismIterator(h, from, to);
        assertTrue(i.hasNext());
        assertTrue(i.hasNext());
        assertEquals(ImmutableHomomorphism.builder()
                .extend(TERM_FACTORY.getVariable("x"), TERM_FACTORY.getRDFLiteralConstant("a0", XSD.STRING))
                .extend(TERM_FACTORY.getVariable("y"), TERM_FACTORY.getRDFLiteralConstant("b0", XSD.STRING))
                .extend(TERM_FACTORY.getVariable("z"), TERM_FACTORY.getRDFLiteralConstant("c0", XSD.STRING))
                .extend(TERM_FACTORY.getVariable("w"), TERM_FACTORY.getRDFLiteralConstant("d0", XSD.STRING))
                .build(), i.next());
        assertFalse(i.hasNext());
        assertFalse(i.hasNext());
    }

    @Test
    public void test_multiple() {
        ImmutableHomomorphism h = ImmutableHomomorphism.builder().build();
        ImmutableList<DataAtom> from = ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getVariable("x"), RDF_FACTORY.createIRI("http://P"),  TERM_FACTORY.getVariable("y")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getVariable("y"), RDF_FACTORY.createIRI("http://Q"), TERM_FACTORY.getVariable("z")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getVariable("z"), RDF_FACTORY.createIRI("http://R"), TERM_FACTORY.getVariable("w")));
        ImmutableList<DataAtom> to = ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getRDFLiteralConstant("a", XSD.STRING),
                        RDF_FACTORY.createIRI("http://P"),  TERM_FACTORY.getRDFLiteralConstant("b", XSD.STRING)),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getRDFLiteralConstant("b", XSD.STRING),
                        RDF_FACTORY.createIRI("http://Q"), TERM_FACTORY.getRDFLiteralConstant("c", XSD.STRING)),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getRDFLiteralConstant("c", XSD.STRING),
                        RDF_FACTORY.createIRI("http://R"), TERM_FACTORY.getRDFLiteralConstant("d", XSD.STRING)),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getRDFLiteralConstant("a", XSD.STRING),
                        RDF_FACTORY.createIRI("http://P"),  TERM_FACTORY.getRDFLiteralConstant("b0", XSD.STRING)),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getRDFLiteralConstant("b0", XSD.STRING),
                        RDF_FACTORY.createIRI("http://Q"), TERM_FACTORY.getRDFLiteralConstant("c0", XSD.STRING)),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getRDFLiteralConstant("c0", XSD.STRING),
                        RDF_FACTORY.createIRI("http://R"), TERM_FACTORY.getRDFLiteralConstant("d0", XSD.STRING)));

        ImmutableHomomorphismIterator i = new ImmutableHomomorphismIterator(h, from, to);
        assertTrue(i.hasNext());
        assertTrue(i.hasNext());
        assertEquals(ImmutableHomomorphism.builder()
                .extend(TERM_FACTORY.getVariable("x"), TERM_FACTORY.getRDFLiteralConstant("a", XSD.STRING))
                .extend(TERM_FACTORY.getVariable("y"), TERM_FACTORY.getRDFLiteralConstant("b", XSD.STRING))
                .extend(TERM_FACTORY.getVariable("z"), TERM_FACTORY.getRDFLiteralConstant("c", XSD.STRING))
                .extend(TERM_FACTORY.getVariable("w"), TERM_FACTORY.getRDFLiteralConstant("d", XSD.STRING))
                .build(), i.next());
        assertTrue(i.hasNext());
        assertTrue(i.hasNext());
        assertEquals(ImmutableHomomorphism.builder()
                .extend(TERM_FACTORY.getVariable("x"), TERM_FACTORY.getRDFLiteralConstant("a", XSD.STRING))
                .extend(TERM_FACTORY.getVariable("y"), TERM_FACTORY.getRDFLiteralConstant("b0", XSD.STRING))
                .extend(TERM_FACTORY.getVariable("z"), TERM_FACTORY.getRDFLiteralConstant("c0", XSD.STRING))
                .extend(TERM_FACTORY.getVariable("w"), TERM_FACTORY.getRDFLiteralConstant("d0", XSD.STRING))
                .build(), i.next());
        assertFalse(i.hasNext());
        assertFalse(i.hasNext());
    }
}
