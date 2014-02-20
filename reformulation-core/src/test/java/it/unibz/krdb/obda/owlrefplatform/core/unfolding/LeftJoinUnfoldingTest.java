package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

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
		Function a = fac.getFunction(fac.getClassPredicate("A"), fac.getVariable("x"));
		Function R = fac.getFunction(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
		Function lj = fac.getFunction(fac.getLeftJoinPredicate(), a, R);
		Function head = fac.getFunction(fac.getPredicate("q", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE query = fac.getCQIE(head, lj);
		queryProgram.appendRule(query);

		// Mapping program
		DatalogProgram p = fac.getDatalogProgram();
		// A rule 1
		Function body = fac.getFunction(fac.getPredicate("T1", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunction(fac.getPredicate("A", 1), fac.getVariable("x"));
		CQIE rule1 = fac.getCQIE(head, body);
		p.appendRule(rule1);

		// A rule 2
		body = fac.getFunction(fac.getPredicate("T2", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunction(fac.getPredicate("R", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE rule2 = fac.getCQIE(head, body);
		p.appendRule(rule2);

		// A rule 3
		body = fac.getFunction(fac.getPredicate("T3", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunction(fac.getPredicate("R", 2), fac.getVariable("x"), fac.getVariable("y"));
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
		Function a = fac.getFunction(fac.getClassPredicate("A"), fac.getVariable("x"));
		Function R = fac.getFunction(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
		Function lj = fac.getFunction(fac.getLeftJoinPredicate(), a, R);
		Function head = fac.getFunction(fac.getPredicate("q", 2), fac.getVariable("x"), fac.getVariable("y"));
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
		Function body = fac.getFunction(fac.getPredicate("T1", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunction(fac.getPredicate("A", 1), fac.getVariable("x"));
		CQIE rule1 = fac.getCQIE(head, body);
		p.appendRule(rule1);

		// A rule 2
		body = fac.getFunction(fac.getPredicate("T2", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunction(fac.getPredicate("R", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE rule2 = fac.getCQIE(head, body);
		p.appendRule(rule2);

		// A rule 3
		body = fac.getFunction(fac.getPredicate("T3", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunction(fac.getPredicate("R", 2), fac.getVariable("x"), fac.getVariable("y"));
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
		Function a = fac.getFunction(fac.getClassPredicate("A"), fac.getVariable("x"));
		Function R = fac.getFunction(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
		Function lj = fac.getFunction(fac.getLeftJoinPredicate(), a, R);
		Function head = fac.getFunction(fac.getPredicate("q", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE query = fac.getCQIE(head, lj);
		queryProgram.appendRule(query);

		// Mapping program
		DatalogProgram p = fac.getDatalogProgram();
		// A rule 1 A(uri(x)) :- T1(x,y)
		Function body = fac.getFunction(fac.getPredicate("T1", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunction(fac.getPredicate("A", 1), fac.getFunction(fac.getPredicate("uri", 1), fac.getVariable("x")));
		CQIE rule1 = fac.getCQIE(head, body);
		p.appendRule(rule1);

		// A rule 2 R(f(x),y) :- T2(x,y)
		body = fac.getFunction(fac.getPredicate("T2", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunction(fac.getPredicate("R", 2), fac.getFunction(fac.getPredicate("f", 1), fac.getVariable("x")), fac.getVariable("y"));
		CQIE rule2 = fac.getCQIE(head, body);
		p.appendRule(rule2);

		// A rule 3 R(g(x),y) :- T3(x,y)
		
		body = fac.getFunction(fac.getPredicate("T3", 2), fac.getVariable("x"), fac.getVariable("y"));
		head = fac.getFunction(fac.getPredicate("R", 2), fac.getFunction(fac.getPredicate("g", 1), fac.getVariable("x")), fac.getVariable("y"));
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
			Function a = fac.getFunction(fac.getClassPredicate("A"), fac.getVariable("x"));
			Function R = fac.getFunction(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
			Function lj = fac.getFunction(fac.getLeftJoinPredicate(), a, R);
			Function head = fac.getFunction(fac.getPredicate("q", 2), fac.getVariable("x"), fac.getVariable("y"));
			CQIE query = fac.getCQIE(head, lj);
			queryProgram.appendRule(query);

			// Mapping program
			DatalogProgram p = fac.getDatalogProgram();
			// A rule 1 A(uri(x)) :- T1(x,y)
			Function body = fac.getFunction(fac.getPredicate("T1", 2), fac.getVariable("x"), fac.getVariable("y"));
			head = fac.getFunction(fac.getPredicate("A", 1), fac.getFunction(fac.getPredicate("uri", 1), fac.getVariable("x")));
			CQIE rule1 = fac.getCQIE(head, body);
			p.appendRule(rule1);

			// A rule 2 R(f(x),y) :- T2(x,y)
			body = fac.getFunction(fac.getPredicate("T2", 2), fac.getVariable("x"), fac.getVariable("y"));
			head = fac.getFunction(fac.getPredicate("R", 2), fac.getFunction(fac.getPredicate("f", 1), fac.getVariable("x")), fac.getVariable("y"));
			CQIE rule2 = fac.getCQIE(head, body);
			p.appendRule(rule2);

			// A rule 3 R(uri(x),y) :- T3(x,y)
			
			body = fac.getFunction(fac.getPredicate("T3", 2), fac.getVariable("x"), fac.getVariable("y"));
			head = fac.getFunction(fac.getPredicate("R", 2), fac.getFunction(fac.getPredicate("uri", 1), fac.getVariable("x")), fac.getVariable("y"));
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
		Function body = fac.getFunction(fac.getPredicate("T1", 2), fac.getVariable("x"), fac.getVariable("y"));
		Function head = fac.getFunction(fac.getPredicate("A", 1), fac.getVariable("x"));
		CQIE rule2 = fac.getCQIE(head, body);
		p.appendRule(rule2);

		DatalogProgram query = fac.getDatalogProgram();
		// main rule q(x,y) :- LJ(A(x), R(x,y))
		Function a = fac.getFunction(fac.getClassPredicate("A"), fac.getVariable("x"));
		Function R = fac.getFunction(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
		Function lj = fac.getFunction(fac.getLeftJoinPredicate(), a, R);
		head = fac.getFunction(fac.getPredicate("q", 2), fac.getVariable("x"), fac.getVariable("y"));
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
