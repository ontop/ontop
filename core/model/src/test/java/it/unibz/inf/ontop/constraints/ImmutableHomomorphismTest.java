package it.unibz.inf.ontop.constraints;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.DataAtom;
import junit.framework.TestCase;
import org.junit.Test;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;

public class ImmutableHomomorphismTest extends TestCase {

    @Test
    public void test_empty_from() {
        ImmutableHomomorphism h = ImmutableHomomorphism.builder().extend(TERM_FACTORY.getVariable("x"), TERM_FACTORY.getConstantLiteral("a")).build();
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
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getConstantLiteral("a"), RDF_FACTORY.createIRI("http://P"),  TERM_FACTORY.getConstantLiteral("b")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getConstantLiteral("b"), RDF_FACTORY.createIRI("http://Q"), TERM_FACTORY.getConstantLiteral("c")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getConstantLiteral("a0"), RDF_FACTORY.createIRI("http://P"),  TERM_FACTORY.getConstantLiteral("b0")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getConstantLiteral("b0"), RDF_FACTORY.createIRI("http://Q"), TERM_FACTORY.getConstantLiteral("c0")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getConstantLiteral("c0"), RDF_FACTORY.createIRI("http://R"), TERM_FACTORY.getConstantLiteral("d0")));

        ImmutableHomomorphismIterator i = new ImmutableHomomorphismIterator(h, from, to);
        assertTrue(i.hasNext());
        assertTrue(i.hasNext());
        assertEquals(ImmutableHomomorphism.builder()
                .extend(TERM_FACTORY.getVariable("x"), TERM_FACTORY.getConstantLiteral("a0"))
                .extend(TERM_FACTORY.getVariable("y"), TERM_FACTORY.getConstantLiteral("b0"))
                .extend(TERM_FACTORY.getVariable("z"), TERM_FACTORY.getConstantLiteral("c0"))
                .extend(TERM_FACTORY.getVariable("w"), TERM_FACTORY.getConstantLiteral("d0"))
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
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getConstantLiteral("a"), RDF_FACTORY.createIRI("http://P"),  TERM_FACTORY.getConstantLiteral("b")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getConstantLiteral("b"), RDF_FACTORY.createIRI("http://Q"), TERM_FACTORY.getConstantLiteral("c")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getConstantLiteral("c"), RDF_FACTORY.createIRI("http://R"), TERM_FACTORY.getConstantLiteral("d")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getConstantLiteral("a"), RDF_FACTORY.createIRI("http://P"),  TERM_FACTORY.getConstantLiteral("b0")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getConstantLiteral("b0"), RDF_FACTORY.createIRI("http://Q"), TERM_FACTORY.getConstantLiteral("c0")),
                ATOM_FACTORY.getIntensionalTripleAtom(TERM_FACTORY.getConstantLiteral("c0"), RDF_FACTORY.createIRI("http://R"), TERM_FACTORY.getConstantLiteral("d0")));

        ImmutableHomomorphismIterator i = new ImmutableHomomorphismIterator(h, from, to);
        assertTrue(i.hasNext());
        assertTrue(i.hasNext());
        assertEquals(ImmutableHomomorphism.builder()
                .extend(TERM_FACTORY.getVariable("x"), TERM_FACTORY.getConstantLiteral("a"))
                .extend(TERM_FACTORY.getVariable("y"), TERM_FACTORY.getConstantLiteral("b"))
                .extend(TERM_FACTORY.getVariable("z"), TERM_FACTORY.getConstantLiteral("c"))
                .extend(TERM_FACTORY.getVariable("w"), TERM_FACTORY.getConstantLiteral("d"))
                .build(), i.next());
        assertTrue(i.hasNext());
        assertTrue(i.hasNext());
        assertEquals(ImmutableHomomorphism.builder()
                .extend(TERM_FACTORY.getVariable("x"), TERM_FACTORY.getConstantLiteral("a"))
                .extend(TERM_FACTORY.getVariable("y"), TERM_FACTORY.getConstantLiteral("b0"))
                .extend(TERM_FACTORY.getVariable("z"), TERM_FACTORY.getConstantLiteral("c0"))
                .extend(TERM_FACTORY.getVariable("w"), TERM_FACTORY.getConstantLiteral("d0"))
                .build(), i.next());
        assertFalse(i.hasNext());
        assertFalse(i.hasNext());
    }
}
