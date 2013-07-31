/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.ArrayList;

import junit.framework.TestCase;

public class LeftJoinUnfoldingTest extends TestCase {
	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	public void testUnfoldingWithMultipleSuccessfulResolutions() {

		// query rule
		DatalogProgram queryProgram = fac.getDatalogProgram();
		Function a = fac.getFunctionalTerm(fac.getClassPredicate("A"), fac.getVariable("x"));
		Function R = fac.getFunctionalTerm(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
		Function lj = fac.getFunctionalTerm(fac.getLeftJoinPredicate(), a, R);
		Function head = fac.getFunctionalTerm(fac.getPredicate("q", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE query = fac.getCQIE(head, lj);
		queryProgram.appendRule(query);

		// Mapping program
		DatalogProgram p = fac.getDatalogProgram();
		// A rule 1
		Function body = fac.getFunctionalTerm(fac.getPredicate("T1", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunctionalTerm(fac.getPredicate("A", 1), fac.getVariable("x"));
		CQIE rule1 = fac.getCQIE(head, body);
		p.appendRule(rule1);

		// A rule 2
		body = fac.getFunctionalTerm(fac.getPredicate("T2", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunctionalTerm(fac.getPredicate("R", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE rule2 = fac.getCQIE(head, body);
		p.appendRule(rule2);

		// A rule 3
		body = fac.getFunctionalTerm(fac.getPredicate("T3", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunctionalTerm(fac.getPredicate("R", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE rule3 = fac.getCQIE(head, body);
		p.appendRule(rule3);

		DatalogUnfolder unfolder = new DatalogUnfolder(p);
		DatalogProgram result = unfolder.unfold(queryProgram, "q");

		System.out.println(result);

		// Only one rule should be returned where y is null
		assertEquals(1, result.getRules().size());
		assertTrue(result.getRules().toString().contains("R"));
		assertTrue(result.getRules().toString().contains("T1"));
		assertFalse(result.getRules().toString().contains("T2"));
		assertFalse(result.getRules().toString().contains("T3"));
		assertFalse(result.getRules().toString().contains("A"));
		assertTrue(result.getRules().get(0).getBody().size() == 1);
	}

	public void testUnfoldingWithMultipleSuccessfulResolutionsAndMultipleUnfoldableAtomsBeforeAndAfterLeftJoin() {

		// query rule
		DatalogProgram queryProgram = fac.getDatalogProgram();
		Function a = fac.getFunctionalTerm(fac.getClassPredicate("A"), fac.getVariable("x"));
		Function R = fac.getFunctionalTerm(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
		Function lj = fac.getFunctionalTerm(fac.getLeftJoinPredicate(), a, R);
		Function head = fac.getFunctionalTerm(fac.getPredicate("q", 2), fac.getVariable("x"), fac.getVariable("y"));
		ArrayList<Function> bodyl = new ArrayList<Function>();
		bodyl.add(a);
		bodyl.add(lj);
		bodyl.add(a);
		bodyl.add(R);
		CQIE query = fac.getCQIE(head, bodyl);
		queryProgram.appendRule(query);

		// Mapping program
		DatalogProgram p = fac.getDatalogProgram();
		// A rule 1
		Function body = fac.getFunctionalTerm(fac.getPredicate("T1", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunctionalTerm(fac.getPredicate("A", 1), fac.getVariable("x"));
		CQIE rule1 = fac.getCQIE(head, body);
		p.appendRule(rule1);

		// A rule 2
		body = fac.getFunctionalTerm(fac.getPredicate("T2", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunctionalTerm(fac.getPredicate("R", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE rule2 = fac.getCQIE(head, body);
		p.appendRule(rule2);

		// A rule 3
		body = fac.getFunctionalTerm(fac.getPredicate("T3", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunctionalTerm(fac.getPredicate("R", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE rule3 = fac.getCQIE(head, body);
		p.appendRule(rule3);

		DatalogUnfolder unfolder = new DatalogUnfolder(p);
		DatalogProgram result = unfolder.unfold(queryProgram, "q");

		System.out.println(result);

		// Only one rule should be returned where y is null
		assertEquals(2, result.getRules().size());
		assertTrue(result.getRules().toString().contains("R("));
		assertTrue(result.getRules().toString().contains("T1("));
		assertTrue(result.getRules().toString().contains("T2("));
		assertTrue(result.getRules().toString().contains("T3("));
		assertTrue(result.getRules().toString().contains("LeftJoin("));
		assertFalse(result.getRules().toString().contains("A("));
		assertTrue(result.getRules().get(0).getBody().size() == 4);
		assertTrue(result.getRules().get(1).getBody().size() == 4);
	}

	public void testUnfoldingWithNoSuccessfulResolutions() {
		// query rule q(x,y) :- LF(A(x), R(x,y)
		
		DatalogProgram queryProgram = fac.getDatalogProgram();
		Function a = fac.getFunctionalTerm(fac.getClassPredicate("A"), fac.getVariable("x"));
		Function R = fac.getFunctionalTerm(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
		Function lj = fac.getFunctionalTerm(fac.getLeftJoinPredicate(), a, R);
		Function head = fac.getFunctionalTerm(fac.getPredicate("q", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE query = fac.getCQIE(head, lj);
		queryProgram.appendRule(query);

		// Mapping program
		DatalogProgram p = fac.getDatalogProgram();
		// A rule 1 A(uri(x)) :- T1(x,y)
		Function body = fac.getFunctionalTerm(fac.getPredicate("T1", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunctionalTerm(fac.getPredicate("A", 1), fac.getFunctionalTerm(fac.getPredicate("uri", 1), fac.getVariable("x")));
		CQIE rule1 = fac.getCQIE(head, body);
		p.appendRule(rule1);

		// A rule 2 R(f(x),y) :- T2(x,y)
		body = fac.getFunctionalTerm(fac.getPredicate("T2", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunctionalTerm(fac.getPredicate("R", 2), fac.getFunctionalTerm(fac.getPredicate("f", 1), fac.getVariable("x")), fac.getVariable("y"));
		CQIE rule2 = fac.getCQIE(head, body);
		p.appendRule(rule2);

		// A rule 3 R(g(x),y) :- T3(x,y)
		
		body = fac.getFunctionalTerm(fac.getPredicate("T3", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunctionalTerm(fac.getPredicate("R", 2), fac.getFunctionalTerm(fac.getPredicate("g", 1), fac.getVariable("x")), fac.getVariable("y"));
		CQIE rule3 = fac.getCQIE(head, body);
		p.appendRule(rule3);

		DatalogUnfolder unfolder = new DatalogUnfolder(p);
		DatalogProgram result = unfolder.unfold(queryProgram, "q");

		// Only one rule should be returned where y is null
		System.out.println(result);
		assertEquals(1, result.getRules().size());
		assertTrue(result.getRules().toString().contains("null"));
		assertTrue(result.getRules().toString().contains("T1("));
		assertFalse(result.getRules().toString().contains("A("));
		assertFalse(result.getRules().toString().contains("R("));
		assertFalse(result.getRules().toString().contains("LeftJoin("));
		assertTrue(result.getRules().get(0).getBody().size() == 1);
	}

	public void testUnfoldingWithOneSuccessfulResolutions() {
			// query rule q(x,y) :- LF(A(x), R(x,y)
			
			DatalogProgram queryProgram = fac.getDatalogProgram();
			Function a = fac.getFunctionalTerm(fac.getClassPredicate("A"), fac.getVariable("x"));
			Function R = fac.getFunctionalTerm(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
			Function lj = fac.getFunctionalTerm(fac.getLeftJoinPredicate(), a, R);
			Function head = fac.getFunctionalTerm(fac.getPredicate("q", 2), fac.getVariable("x"), fac.getVariable("y"));
			CQIE query = fac.getCQIE(head, lj);
			queryProgram.appendRule(query);

			// Mapping program
			DatalogProgram p = fac.getDatalogProgram();
			// A rule 1 A(uri(x)) :- T1(x,y)
			Function body = fac.getFunctionalTerm(fac.getPredicate("T1", 2), fac.getVariable("x"), fac.getVariable("y"));
			head = fac.getFunctionalTerm(fac.getPredicate("A", 1), fac.getFunctionalTerm(fac.getPredicate("uri", 1), fac.getVariable("x")));
			CQIE rule1 = fac.getCQIE(head, body);
			p.appendRule(rule1);

			// A rule 2 R(f(x),y) :- T2(x,y)
			body = fac.getFunctionalTerm(fac.getPredicate("T2", 2), fac.getVariable("x"), fac.getVariable("y"));
			head = fac.getFunctionalTerm(fac.getPredicate("R", 2), fac.getFunctionalTerm(fac.getPredicate("f", 1), fac.getVariable("x")), fac.getVariable("y"));
			CQIE rule2 = fac.getCQIE(head, body);
			p.appendRule(rule2);

			// A rule 3 R(uri(x),y) :- T3(x,y)
			
			body = fac.getFunctionalTerm(fac.getPredicate("T3", 2), fac.getVariable("x"), fac.getVariable("y"));
			head = fac.getFunctionalTerm(fac.getPredicate("R", 2), fac.getFunctionalTerm(fac.getPredicate("uri", 1), fac.getVariable("x")), fac.getVariable("y"));
			CQIE rule3 = fac.getCQIE(head, body);
			p.appendRule(rule3);

			DatalogUnfolder unfolder = new DatalogUnfolder(p);
			DatalogProgram result = unfolder.unfold(queryProgram, "q");

			// Only one rule should be returned where y is null
			System.out.println(result);
			assertEquals(1, result.getRules().size());
			assertTrue(result.getRules().toString().contains("T1("));
			assertTrue(result.getRules().toString().contains("T3("));
			assertTrue(result.getRules().toString().contains("uri("));
			assertTrue(result.getRules().toString().contains("LeftJoin("));
			assertTrue(result.getRules().toString().contains("LeftJoin("));
			
			assertFalse(result.getRules().toString().contains("A("));
			assertFalse(result.getRules().toString().contains("R("));
			assertFalse(result.getRules().toString().contains("T2("));
			
			assertFalse(result.getRules().toString().contains("null"));
			assertTrue(result.getRules().get(0).getBody().size() == 1);
		
	}

	public void testUnfoldingWithNoRulesForResolutions() {

		// A program that unifies with A, but not R, y should become null
		DatalogProgram p = fac.getDatalogProgram();
		Function body = fac.getFunctionalTerm(fac.getPredicate("T1", 2), fac.getVariable("x"), fac.getVariable("y"));
		Function head = fac.getFunctionalTerm(fac.getPredicate("A", 1), fac.getVariable("x"));
		CQIE rule2 = fac.getCQIE(head, body);
		p.appendRule(rule2);

		DatalogProgram query = fac.getDatalogProgram();
		// main rule q(x,y) :- LJ(A(x), R(x,y))
		Function a = fac.getFunctionalTerm(fac.getClassPredicate("A"), fac.getVariable("x"));
		Function R = fac.getFunctionalTerm(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
		Function lj = fac.getFunctionalTerm(fac.getLeftJoinPredicate(), a, R);
		head = fac.getFunctionalTerm(fac.getPredicate("q", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE rule1 = fac.getCQIE(head, lj);
		query.appendRule(rule1);

		DatalogUnfolder unfolder = new DatalogUnfolder(p);
		DatalogProgram result = unfolder.unfold(query, "q");

		// Only one rule should be returned where y is null
		System.out.println(result);
		assertEquals(1, result.getRules().size());
		assertTrue(result.getRules().toString().contains("null"));
		assertTrue(result.getRules().toString().contains("T1("));
		assertFalse(result.getRules().toString().contains("A("));
		assertFalse(result.getRules().toString().contains("R("));
		assertFalse(result.getRules().toString().contains("LeftJoin("));
		assertTrue(result.getRules().get(0).getBody().size() == 1);

	}
}
