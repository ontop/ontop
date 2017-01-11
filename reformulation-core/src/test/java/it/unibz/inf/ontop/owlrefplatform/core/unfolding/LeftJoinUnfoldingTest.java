package it.unibz.inf.ontop.owlrefplatform.core.unfolding;

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

import it.unibz.inf.ontop.model.*;

import java.util.ArrayList;

import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import junit.framework.TestCase;
import org.junit.Ignore;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

/**
 * TODO: port it to the new IntermediateQuery data structure
 */
@Ignore
public class LeftJoinUnfoldingTest extends TestCase {

	@Ignore
	public void testUnfoldingWithMultipleSuccessfulResolutions() {

		// query rule
		DatalogProgram queryProgram = DATA_FACTORY.getDatalogProgram();
		Function a = DATA_FACTORY.getFunction(DATA_FACTORY.getClassPredicate("A"), DATA_FACTORY.getVariable("x"));
		Function R = DATA_FACTORY.getFunction(DATA_FACTORY.getObjectPropertyPredicate("R"), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		Function lj = DATA_FACTORY.getSPARQLLeftJoin(a, R);
		Function head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("q", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		CQIE query = DATA_FACTORY.getCQIE(head, lj);
		queryProgram.appendRule(query);

		// Mapping program
		DatalogProgram p = DATA_FACTORY.getDatalogProgram();
		// A rule 1
		Function body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T1", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("A", 1), DATA_FACTORY.getVariable("x"));
		CQIE rule1 = DATA_FACTORY.getCQIE(head, body);
		p.appendRule(rule1);

		// A rule 2
		body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T2", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("R", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		CQIE rule2 = DATA_FACTORY.getCQIE(head, body);
		p.appendRule(rule2);

		// A rule 3
		body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T3", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("R", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		CQIE rule3 = DATA_FACTORY.getCQIE(head, body);
		p.appendRule(rule3);

		DatalogUnfolder unfolder = new DatalogUnfolder(p.getRules());
		DatalogProgram result = unfolder.unfold(queryProgram, "q", QuestConstants.TDOWN, true);

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

	@Ignore
	public void testUnfoldingWithMultipleSuccessfulResolutionsAndMultipleUnfoldableAtomsBeforeAndAfterLeftJoin() {

		// query rule
		DatalogProgram queryProgram = DATA_FACTORY.getDatalogProgram();
		Function a = DATA_FACTORY.getFunction(DATA_FACTORY.getClassPredicate("A"), DATA_FACTORY.getVariable("x"));
		Function R = DATA_FACTORY.getFunction(DATA_FACTORY.getObjectPropertyPredicate("R"), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		Function lj = DATA_FACTORY.getSPARQLLeftJoin(a, R);
		Function head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("q", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		ArrayList<Function> bodyl = new ArrayList<Function>();
		bodyl.add(a);
		bodyl.add(lj);
		bodyl.add(a);
		bodyl.add(R);
		CQIE query = DATA_FACTORY.getCQIE(head, bodyl);
		queryProgram.appendRule(query);

		// Mapping program
		DatalogProgram p = DATA_FACTORY.getDatalogProgram();
		// A rule 1
		Function body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T1", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("A", 1), DATA_FACTORY.getVariable("x"));
		CQIE rule1 = DATA_FACTORY.getCQIE(head, body);
		p.appendRule(rule1);

		// A rule 2
		body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T2", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("R", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		CQIE rule2 = DATA_FACTORY.getCQIE(head, body);
		p.appendRule(rule2);

		// A rule 3
		body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T3", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("R", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		CQIE rule3 = DATA_FACTORY.getCQIE(head, body);
		p.appendRule(rule3);

		DatalogUnfolder unfolder = new DatalogUnfolder(p.getRules());

		DatalogProgram result = unfolder.unfold(queryProgram, "q",QuestConstants.TDOWN, true);

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

	@Ignore
	public void testUnfoldingWithNoSuccessfulResolutions() {
		// query rule q(x,y) :- LF(A(x), R(x,y)
		
		DatalogProgram queryProgram = DATA_FACTORY.getDatalogProgram();
		Function a = DATA_FACTORY.getFunction(DATA_FACTORY.getClassPredicate("A"), DATA_FACTORY.getVariable("x"));
		Function R = DATA_FACTORY.getFunction(DATA_FACTORY.getObjectPropertyPredicate("R"), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		Function lj = DATA_FACTORY.getSPARQLLeftJoin(a, R);
		Function head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("q", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		CQIE query = DATA_FACTORY.getCQIE(head, lj);
		queryProgram.appendRule(query);

		// Mapping program
		DatalogProgram p = DATA_FACTORY.getDatalogProgram();
		// A rule 1 A(uri(x)) :- T1(x,y)
		Function body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T1", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("A", 1), DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("uri", 1), DATA_FACTORY.getVariable("x")));
		CQIE rule1 = DATA_FACTORY.getCQIE(head, body);
		p.appendRule(rule1);

		// A rule 2 R(f(x),y) :- T2(x,y)
		body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T2", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("R", 2), DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("f", 1), DATA_FACTORY.getVariable("x")), DATA_FACTORY.getVariable("y"));
		CQIE rule2 = DATA_FACTORY.getCQIE(head, body);
		p.appendRule(rule2);

		// A rule 3 R(g(x),y) :- T3(x,y)
		
		body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T3", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("R", 2), DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("g", 1), DATA_FACTORY.getVariable("x")), DATA_FACTORY.getVariable("y"));
		CQIE rule3 = DATA_FACTORY.getCQIE(head, body);
		p.appendRule(rule3);

		DatalogUnfolder unfolder = new DatalogUnfolder(p.getRules());
		DatalogProgram result = unfolder.unfold(queryProgram, "q",QuestConstants.TDOWN, true) ;

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

	@Ignore
	public void testUnfoldingWithOneSuccessfulResolutions() {
			// query rule q(x,y) :- LF(A(x), R(x,y)
			
			DatalogProgram queryProgram = DATA_FACTORY.getDatalogProgram();
			Function a = DATA_FACTORY.getFunction(DATA_FACTORY.getClassPredicate("A"), DATA_FACTORY.getVariable("x"));
			Function R = DATA_FACTORY.getFunction(DATA_FACTORY.getObjectPropertyPredicate("R"), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
			Function lj = DATA_FACTORY.getSPARQLLeftJoin(a, R);
			Function head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("q", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
			CQIE query = DATA_FACTORY.getCQIE(head, lj);
			queryProgram.appendRule(query);

			// Mapping program
			DatalogProgram p = DATA_FACTORY.getDatalogProgram();
			// A rule 1 A(uri(x)) :- T1(x,y)
			Function body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T1", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
			head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("A", 1), DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("uri", 1), DATA_FACTORY.getVariable("x")));
			CQIE rule1 = DATA_FACTORY.getCQIE(head, body);
			p.appendRule(rule1);

			// A rule 2 R(f(x),y) :- T2(x,y)
			body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T2", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
			head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("R", 2), DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("f", 1), DATA_FACTORY.getVariable("x")), DATA_FACTORY.getVariable("y"));
			CQIE rule2 = DATA_FACTORY.getCQIE(head, body);
			p.appendRule(rule2);

			// A rule 3 R(uri(x),y) :- T3(x,y)
			
			body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T3", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
			head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("R", 2), DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("uri", 1), DATA_FACTORY.getVariable("x")), DATA_FACTORY.getVariable("y"));
			CQIE rule3 = DATA_FACTORY.getCQIE(head, body);
			p.appendRule(rule3);

			DatalogUnfolder unfolder = new DatalogUnfolder(p.getRules());

			DatalogProgram result = unfolder.unfold(queryProgram, "q",QuestConstants.TDOWN, true);

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
		DatalogProgram p = DATA_FACTORY.getDatalogProgram();
		Function body = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("T1", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		Function head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("A", 1), DATA_FACTORY.getVariable("x"));
		CQIE rule2 = DATA_FACTORY.getCQIE(head, body);
		p.appendRule(rule2);

		DatalogProgram query = DATA_FACTORY.getDatalogProgram();
		// main rule q(x,y) :- LJ(A(x), R(x,y))
		Function a = DATA_FACTORY.getFunction(DATA_FACTORY.getClassPredicate("A"), DATA_FACTORY.getVariable("x"));
		Function R = DATA_FACTORY.getFunction(DATA_FACTORY.getObjectPropertyPredicate("R"), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		Function lj = DATA_FACTORY.getSPARQLLeftJoin(a, R);
		head = DATA_FACTORY.getFunction(DATA_FACTORY.getPredicate("q", 2), DATA_FACTORY.getVariable("x"), DATA_FACTORY.getVariable("y"));
		CQIE rule1 = DATA_FACTORY.getCQIE(head, lj);
		query.appendRule(rule1);

		DatalogUnfolder unfolder = new DatalogUnfolder(p.getRules());

		DatalogProgram result = unfolder.unfold(query, "q",QuestConstants.TDOWN, true);

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
