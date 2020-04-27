package it.unibz.inf.ontop.constraints;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.constraints.impl.BasicLinearInclusionDependenciesImpl;
import it.unibz.inf.ontop.constraints.impl.LinearInclusionDependenciesImpl;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQSyntacticContainmentCheck;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import org.apache.commons.rdf.api.IRI;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;
import static org.junit.Assert.*;

public class CQCUtilitiesTest {

	IRI propertyP = RDF_FACTORY.createIRI("http://example.com/P");
	IRI propertyR = RDF_FACTORY.createIRI("http://example.com/R");
	IRI propertyS = RDF_FACTORY.createIRI("http://example.com/S");
	IRI propertyT = RDF_FACTORY.createIRI("http://example.com/T");
	IRI classA = RDF_FACTORY.createIRI("http://example.com/A");
    IRI classB = RDF_FACTORY.createIRI("http://example.com/B");
	IRI classC = RDF_FACTORY.createIRI("http://example.com/C");

    Variable x = TERM_FACTORY.getVariable("x");
    Variable y = TERM_FACTORY.getVariable("y");
    Variable z = TERM_FACTORY.getVariable("z");
    Variable s = TERM_FACTORY.getVariable("s");
    Variable t = TERM_FACTORY.getVariable("t");
    Variable n = TERM_FACTORY.getVariable("n");
    Variable m = TERM_FACTORY.getVariable("m");
    Variable o = TERM_FACTORY.getVariable("o");


    @Test
	public void testContainment1() {

		// Query 1 - q(x,y) :- R(x,y), R(y,z)
		ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x, y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y),
                ATOM_FACTORY.getIntensionalTripleAtom(y, propertyR, x)));

		// Query 2 - q(y,y) :- R(y,y)
        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(y, y), ImmutableList.of(
		        ATOM_FACTORY.getIntensionalTripleAtom(y, propertyR, y)));

		// Query 3 - q(m,n) :- R(m,n)
        ImmutableCQ<AtomPredicate> q3 = new ImmutableCQ<>(ImmutableList.of(m, n), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(m, propertyR, n)));

		// Query 4 - q(m,n) :- S(m,n) R(m,n)
        ImmutableCQ<AtomPredicate> q4 = new ImmutableCQ<>(ImmutableList.of(m, n), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(m, propertyS, n),
                ATOM_FACTORY.getIntensionalTripleAtom(m, propertyR, n)));

		// Query 5 - q() :- S(x,y)
        ImmutableCQ<AtomPredicate> q5 = new ImmutableCQ<>(ImmutableList.of(), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyS, y)));

		// Query 6 - q() :- S(_,_)
        ImmutableCQ<AtomPredicate> q6 = new ImmutableCQ<>(ImmutableList.of(), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(m, propertyS, n)));

		// Query 7 - q(x,y) :- R(x,y), P(y,_)
        ImmutableCQ<AtomPredicate> q7 = new ImmutableCQ<>(ImmutableList.of(x, y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y),
                ATOM_FACTORY.getIntensionalTripleAtom(y, propertyP, m)));

		// Query 8 - q(x,y) :- R(x,y), P(_,_)
        ImmutableCQ<AtomPredicate> q8 = new ImmutableCQ<>(ImmutableList.of(x, y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y),
                ATOM_FACTORY.getIntensionalTripleAtom(m, propertyP, n)));

		// Query 9 - q() :- R(x,m), R(x,y), S(m,n), S(y,z),T(n,o),T(z,x)
        ImmutableCQ<AtomPredicate> q9 = new ImmutableCQ<>(ImmutableList.of(), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, m),
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y),
                ATOM_FACTORY.getIntensionalTripleAtom(m, propertyS, n),
                ATOM_FACTORY.getIntensionalTripleAtom(y, propertyS, z),
                ATOM_FACTORY.getIntensionalTripleAtom(n, propertyT, o),
                ATOM_FACTORY.getIntensionalTripleAtom(z, propertyT, x)));

		// Query 10 - q() :- R(i,j), S(j,k), T(k,i)
        ImmutableCQ<AtomPredicate> q10 = new ImmutableCQ<>(ImmutableList.of(), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(m, propertyR, n),
                ATOM_FACTORY.getIntensionalTripleAtom(n, propertyS, o),
                ATOM_FACTORY.getIntensionalTripleAtom(o, propertyT, m)));

		ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqcu = new ImmutableCQContainmentCheckUnderLIDs<>(LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY).build());

		assertTrue(cqcu.isContainedIn(q6, q5));
		assertTrue(cqcu.isContainedIn(q5, q6));
		assertTrue(cqcu.isContainedIn(q7, q8));
		assertFalse(cqcu.isContainedIn(q8, q7));
		assertTrue(cqcu.isContainedIn(q2, q1));
		assertFalse(cqcu.isContainedIn(q1, q2));
		assertTrue(cqcu.isContainedIn(q1, q3));
		assertFalse(cqcu.isContainedIn(q3, q1));
		assertFalse(cqcu.isContainedIn(q1, q4));
		assertFalse(cqcu.isContainedIn(q4, q1));
		assertTrue(cqcu.isContainedIn(q9, q10));
		assertTrue(cqcu.isContainedIn(q10, q9));
	}

    @Test
	public void testSyntacticContainmentCheck() {

		// Query 1 - q(x) :- R(x,y), R(y,z), A(x)
        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y),
                ATOM_FACTORY.getIntensionalTripleAtom(y, propertyR, z),
                ATOM_FACTORY.getIntensionalTripleAtom(x, classA)));

        // Query 2 - q(x) :- R(x,y)
        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y)));

		// Query 3 - q(x) :- A(x)
        ImmutableCQ<AtomPredicate> q3 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, classA)));

        ImmutableCQSyntacticContainmentCheck<AtomPredicate> cqc = new ImmutableCQSyntacticContainmentCheck<>();

		assertTrue(cqc.isContainedIn(q1, q2));
		assertTrue(cqc.isContainedIn(q1, q3));
		assertFalse(cqc.isContainedIn(q2, q1));
		assertFalse(cqc.isContainedIn(q3, q1));
	}

    @Test
	public void testRemovalOfSyntacticContainmentCheck() {

        // Query 1 - q(x) :- R(x,y), R(y,z), A(x)
        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y),
                ATOM_FACTORY.getIntensionalTripleAtom(y, propertyR, z),
                ATOM_FACTORY.getIntensionalTripleAtom(x, classA)));

        // Query 2 - q(x) :- R(x,y)
        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y)));

        // Query 3 - q(x) :- A(x)
        ImmutableCQ<AtomPredicate> q3 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, classA)));

        ImmutableCQSyntacticContainmentCheck<AtomPredicate> cqc = new ImmutableCQSyntacticContainmentCheck<>();

        // q1 is redundant whenever q2 or q3 are present

        List<ImmutableCQ<AtomPredicate>> Q1 = new LinkedList<>(ImmutableList.of(q1, q2));
        cqc.removeContainedQueries(Q1);
        assertEquals(1, Q1.size());
        assertTrue(Q1.contains(q2));

        List<ImmutableCQ<AtomPredicate>> Q2 = new LinkedList<>(ImmutableList.of(q1, q3));
        cqc.removeContainedQueries(Q2);
        assertEquals(1,Q2.size());
        assertTrue(Q2.contains(q3));

        List<ImmutableCQ<AtomPredicate>> Q3 = new LinkedList<>(ImmutableList.of(q2, q3));
        cqc.removeContainedQueries(Q3);
        assertEquals(2, Q3.size());
        assertTrue(Q3.contains(q2));
        assertTrue(Q3.contains(q3));

        List<ImmutableCQ<AtomPredicate>> Q4 = new LinkedList<>(ImmutableList.of(q1, q2, q3));
        cqc.removeContainedQueries(Q4);
        assertEquals(2, Q4.size());
        assertTrue(Q4.contains(q2));
        assertTrue(Q4.contains(q3));
	}

    @Test
    public void testSemanticContainment_1()  {
        // q(x) :- A(x), q(y) :- C(y), with A ISA C

        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, classA)));

        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(y, classC)));

        BasicLinearInclusionDependenciesImpl<AtomPredicate> dependencies =
                LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY)
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(s, classC),
                                ATOM_FACTORY.getIntensionalTripleAtom(s, classA))
                        .build();

        ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc = new ImmutableCQContainmentCheckUnderLIDs<>(dependencies);

        assertTrue(cqc.isContainedIn(q1, q2));
        assertFalse(cqc.isContainedIn(q2, q1));
    }

    @Test
    public void testSemanticContainment_2() {
        // q(x) :- A(x), q(y) :- R(y,z), with A ISA exists R

        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, classA)));

        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(y, propertyR, z)));

        BasicLinearInclusionDependenciesImpl<AtomPredicate> dependencies =
                LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY)
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(s, propertyR, t),
                                ATOM_FACTORY.getIntensionalTripleAtom(s, classA))
                        .build();

        ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc = new ImmutableCQContainmentCheckUnderLIDs<>(dependencies);

        assertTrue(cqc.isContainedIn(q1, q2));
        assertFalse(cqc.isContainedIn(q2, q1));
    }

    @Test
    public void testSemanticContainment_3() {
        // q(x) :- A(x), q(y) :- R(z,y), with A ISA exists inv(R)

        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, classA)));

        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(z, propertyR, y)));

        BasicLinearInclusionDependenciesImpl<AtomPredicate> dependencies =
                LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY)
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(t, propertyR, s),
                                ATOM_FACTORY.getIntensionalTripleAtom(s, classA))
                        .build();

        ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc = new ImmutableCQContainmentCheckUnderLIDs<>(dependencies);

        assertTrue(cqc.isContainedIn(q1, q2));
        assertFalse(cqc.isContainedIn(q2, q1));
    }

    @Test
    public void testSemanticContainment_4() {
        // q(x) :- R(x,y), q(z) :- A(z), with exists R ISA A

        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y)));

        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(z), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(z, classA)));

        BasicLinearInclusionDependenciesImpl<AtomPredicate> dependencies =
                LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY)
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(s, classA),
                                ATOM_FACTORY.getIntensionalTripleAtom(s, propertyR, t))
                        .build();

        ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc = new ImmutableCQContainmentCheckUnderLIDs<>(dependencies);

        assertTrue(cqc.isContainedIn(q1, q2));
        assertFalse(cqc.isContainedIn(q2, q1));
    }

    @Test
    public void testSemanticContainment_5() {
        // q(y) :- R(x,y), q(z) :- A(z), with exists inv(R) ISA A

        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y)));

        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(z), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(z, classA)));

        BasicLinearInclusionDependenciesImpl<AtomPredicate> dependencies =
                LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY)
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(s, classA),
                                ATOM_FACTORY.getIntensionalTripleAtom(t, propertyR, s))
                        .build();

        ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc = new ImmutableCQContainmentCheckUnderLIDs<>(dependencies);

        assertTrue(cqc.isContainedIn(q1, q2));
        assertFalse(cqc.isContainedIn(q2, q1));
    }

    @Test
    public void testSemanticContainment_6() {
        // q(x) :- A(x), q(y) :- C(y), with A ISA B, B ISA C

        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, classA)));

        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(y, classC)));

        BasicLinearInclusionDependenciesImpl<AtomPredicate> dependencies =
                LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY)
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(s, classB),
                                ATOM_FACTORY.getIntensionalTripleAtom(s, classA))
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(s, classC),
                                ATOM_FACTORY.getIntensionalTripleAtom(s, classB))
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(s, classC),
                                ATOM_FACTORY.getIntensionalTripleAtom(s, classA))
                        .build();

        ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc = new ImmutableCQContainmentCheckUnderLIDs<>(dependencies);

        assertTrue(cqc.isContainedIn(q1, q2));
        assertFalse(cqc.isContainedIn(q2, q1));
    }

    @Test
    public void testSemanticContainment_7() {
        // q(x) :- A(x), q(y) :- C(y), with A ISA exists R, exists R ISA C

        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, classA)));

        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(y, classC)));

        BasicLinearInclusionDependenciesImpl<AtomPredicate> dependencies =
                LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY)
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(s, propertyR, t),
                                ATOM_FACTORY.getIntensionalTripleAtom(s, classA))
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(s, classC),
                                ATOM_FACTORY.getIntensionalTripleAtom(s, propertyR, t))
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(s, classC),
                                ATOM_FACTORY.getIntensionalTripleAtom(s, classA))
                        .build();

        ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc = new ImmutableCQContainmentCheckUnderLIDs<>(dependencies);

        assertTrue(cqc.isContainedIn(q1, q2));
        assertFalse(cqc.isContainedIn(q2, q1));
    }

    @Test
    public void testSemanticContainment_8() {
        // q(x) :- A(x), q(y) :- C(y), with A ISA exists inv(R), exists inv(R) ISA C

        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, classA)));

        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(y, classC)));

        BasicLinearInclusionDependenciesImpl<AtomPredicate> dependencies =
                LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY)
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(t, propertyR, s),
                                ATOM_FACTORY.getIntensionalTripleAtom(s, classA))
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(s, classC),
                                ATOM_FACTORY.getIntensionalTripleAtom(t, propertyR, s))
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(s, classC),
                                ATOM_FACTORY.getIntensionalTripleAtom(s, classA))
                        .build();

        ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc = new ImmutableCQContainmentCheckUnderLIDs<>(dependencies);

        assertTrue(cqc.isContainedIn(q1, q2));
        assertFalse(cqc.isContainedIn(q2, q1));
    }

    @Test
    public void testSemanticContainment_9() {
        // q(x,y) :- R(x,y), q(s,t) :- S(s,t), with R ISA S

        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x, y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y)));

        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(s, t), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(s, propertyS, t)));

        BasicLinearInclusionDependenciesImpl<AtomPredicate> dependencies =
                LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY)
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(n, propertyS, o),
                                ATOM_FACTORY.getIntensionalTripleAtom(n, propertyR, o))
                        .build();

        ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc = new ImmutableCQContainmentCheckUnderLIDs<>(dependencies);

        assertTrue(cqc.isContainedIn(q1, q2));
        assertFalse(cqc.isContainedIn(q2, q1));
    }

    @Test
    public void testSemanticContainment_10() {
        // q(x,y) :- R(x,y), q(s,t) :- S(s,t), with R ISA T, T ISA S

        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x, y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y)));

        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(s, t), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(s, propertyS, t)));

        BasicLinearInclusionDependenciesImpl<AtomPredicate> dependencies =
                LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY)
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(n, propertyT, o),
                                ATOM_FACTORY.getIntensionalTripleAtom(n, propertyR, o))
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(n, propertyS, o),
                                ATOM_FACTORY.getIntensionalTripleAtom(n, propertyT, o))
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(n, propertyS, o),
                                ATOM_FACTORY.getIntensionalTripleAtom(n, propertyR, o))
                        .build();

        ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc = new ImmutableCQContainmentCheckUnderLIDs<>(dependencies);

        assertTrue(cqc.isContainedIn(q1, q2));
        assertFalse(cqc.isContainedIn(q2, q1));
    }

    @Test
    public void testSemanticContainment_11() {
        // q(x,y) :- R(x,y), q(s,t) :- S(s,t), with R ISA inv(T), inv(T) ISA S

        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x, y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y)));

        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(s, t), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(s, propertyS, t)));

        BasicLinearInclusionDependenciesImpl<AtomPredicate> dependencies =
                LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY)
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(o, propertyT, n),
                                ATOM_FACTORY.getIntensionalTripleAtom(n, propertyR, o))
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(n, propertyS, o),
                                ATOM_FACTORY.getIntensionalTripleAtom(o, propertyT, n))
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(n, propertyS, o),
                                ATOM_FACTORY.getIntensionalTripleAtom(n, propertyR, o))
                        .build();

        ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc = new ImmutableCQContainmentCheckUnderLIDs<>(dependencies);

        assertTrue(cqc.isContainedIn(q1, q2));
        assertFalse(cqc.isContainedIn(q2, q1));
    }

    @Test
    public void testSemanticContainment_12() {
        // q(x,y) :- R(x,y), q(s,t) :- S(s,t), with inv(R) ISA T, T ISA inv(S)

        ImmutableCQ<AtomPredicate> q1 = new ImmutableCQ<>(ImmutableList.of(x, y), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(x, propertyR, y)));

        ImmutableCQ<AtomPredicate> q2 = new ImmutableCQ<>(ImmutableList.of(s, t), ImmutableList.of(
                ATOM_FACTORY.getIntensionalTripleAtom(s, propertyS, t)));

        BasicLinearInclusionDependenciesImpl<AtomPredicate> dependencies =
                LinearInclusionDependenciesImpl.builder(CORE_UTILS_FACTORY, ATOM_FACTORY)
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(n, propertyT, o),
                                ATOM_FACTORY.getIntensionalTripleAtom(o, propertyR, n))
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(o, propertyS, n),
                                ATOM_FACTORY.getIntensionalTripleAtom(n, propertyT, o))
                        .add(ATOM_FACTORY.getIntensionalTripleAtom(o, propertyS, n),
                                ATOM_FACTORY.getIntensionalTripleAtom(o, propertyR, n))
                        .build();

        ImmutableCQContainmentCheckUnderLIDs<AtomPredicate> cqc = new ImmutableCQContainmentCheckUnderLIDs<>(dependencies);

        assertTrue(cqc.isContainedIn(q1, q2));
        assertFalse(cqc.isContainedIn(q2, q1));
    }
/*
    //Facts should not be removed by the CQC_UTILITIES
    // ROMAN (18 Sep 2018): do not understand the purpose of the test
    @Test
    public void testFacts() throws Exception {

        // q(x) :- , q(x) :- R(x,y), A(x)

        OntologyBuilder builder = OntologyBuilderImpl.builder(RDF_FACTORY);
        OClass left = builder.declareClass(classA.getIRIString());
        ObjectPropertyExpression pleft = builder.declareObjectProperty(propertyR.getIRIString());

        ObjectSomeValuesFrom right = pleft.getDomain();
        builder.addSubClassOfAxiom(left, right);
		ClassifiedTBox sigma = builder.build().tbox();

        // Query 1 q(x) :- R(x,y), A(x)
        Function head = getFunction("q", x);

        List<Function> body = new LinkedList<>();

        body.add(ATOM_FACTORY.getMutableTripleBodyAtom(
				TERM_FACTORY.getVariable("x"), propertyR, TERM_FACTORY.getVariable("y")));

        body.add(ATOM_FACTORY.getMutableTripleBodyAtom(TERM_FACTORY.getVariable("x"), classA));

        CQIE query1 = DATALOG_FACTORY.getCQIE(head, body);

        // Query 2 q(x) :- (with empty body)

        head = getFunction("q", TERM_FACTORY.getVariable("x"));
        body = new LinkedList<>();
        CQIE query2 = DATALOG_FACTORY.getCQIE(head, body);

		ImmutableList<LinearInclusionDependency> dep = INCLUSION_DEPENDENCY_TOOLS.getABoxDependencies(sigma, false);
		CQContainmentCheckUnderLIDs cqc = new CQContainmentCheckUnderLIDs(dep, DATALOG_FACTORY, UNIFIER_UTILITIES,
				SUBSTITUTION_UTILITIES, TERM_FACTORY);
				
        assertTrue(cqc.isContainedIn(query1, query2));  // ROMAN: changed from False

        assertFalse(cqc.isContainedIn(query2, query1));

        assertTrue(new CQContainmentCheckSyntactic().isContainedIn(query1, query2)); // ROMAN: changed from False
        
        assertFalse(new CQContainmentCheckSyntactic().isContainedIn(query2, query1));
    }
*/
}
