package it.unibz.krdb.obda.owlrefplatform.core.translator;

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

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAQueryModifiers;
import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class DatalogToSparqlTranslatorTest {

	private DatalogToSparqlTranslator datalogTranslator;
	private PrefixManager prefixManager;

	private static OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();

	@Before
	public void setup() {
		prefixManager = new SimplePrefixManager();
		prefixManager.addPrefix(":", "http://example.org/");
		datalogTranslator = new DatalogToSparqlTranslator(prefixManager);
	}

	@Test
	public void testSimpleQuery() {
		
		Function ans1 = createQuery(x);
		
		/**
		 * ans1(x) :- Student(x)
		 */
		CQIE query = dataFactory.getCQIE(ans1, student);
		DatalogProgram datalog = createDatalogProgram(query);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Simple Query", datalog);
	}

	@Test
	public void testAnotherSimpleQuery() {
		
		Function ans1 = createQuery(x, a);
		
		/**
		 * ans1(x) :- Student(x), firstName(x,a)
		 */
		CQIE query = dataFactory.getCQIE(ans1, student, firstName);
		DatalogProgram datalog = createDatalogProgram(query);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Another Simple Query", datalog);
	}

	@Test
	public void testSimpleRule() {
		
		Function ans1 = createQuery(x, a);
		Function ans2 = createRule(ANS2, x, a);
		
		/**
		 * ans1(x,a) :- ans2(x,a)
		 * ans2(x,a) :- Student(x), firstName(x,a)
		 */
		CQIE query = dataFactory.getCQIE(ans1, ans2);
		CQIE rule1 = dataFactory.getCQIE(ans2, student, firstName);
		DatalogProgram datalog = createDatalogProgram(query, rule1);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Simple Rule", datalog);
	}

	@Test
	public void testTwoSimpleRules() {
		
		Function ans1 = createQuery(x, a, b);
		Function ans2 = createRule(ANS2, x, a, b);
		Function ans3 = createRule(ANS3, x, a, b);
		
		/**
		 * ans1(x,a,b) :- ans2(x,a,b)
		 * ans2(x,a,b) :- Student(x), ans3(x,a,b)
		 * ans3(x,a,b) :- firstName(x,a), lastName(x,b)
		 */
		CQIE query = dataFactory.getCQIE(ans1, ans2);
		CQIE rule1 = dataFactory.getCQIE(ans2, student, ans3);
		CQIE rule2 = dataFactory.getCQIE(ans3, firstName, lastName);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Two Simple Rules", datalog);
	}

	@Test
	public void testSimpleQueryWithCondition() {
		
		Function ans1 = createQuery(x, a);
		Function cond = dataFactory.getFunctionEQ(a, c1);
		
		/**
		 * ans1(x) :- Student(x), firstName(x,a), EQ(a,"John")
		 */
		CQIE query = dataFactory.getCQIE(ans1, student, firstName, cond);
		DatalogProgram datalog = createDatalogProgram(query);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Simple Query with Condition", datalog);
	}

	@Test
	public void testSimpleQueryWithMoreConditions() {
		
		Function ans1 = createQuery(x, a);
		Function cond1 = dataFactory.getFunctionEQ(a, c1);
		Function cond2 = dataFactory.getFunctionNEQ(b, c2);
		Function cond3 = dataFactory.getFunctionGT(c, c3);
		Function cond4 = dataFactory.getFunctionGTE(d, c4);
		Function cond5 = dataFactory.getFunctionLT(e, c5);
		
		/**
		 * ans1(x) :- Student(x), firstName(x,a), lastName(x,b), age(x,c), grade(x,d), enrollmentDate(x,e),
		 * 			  EQ(a,"John"), NEQ(b,"Smith"), GT(c,22), GTE(d,35), LT(e,"2012-01-01 00:00:00")
		 */
		CQIE query = dataFactory.getCQIE(ans1, student, firstName, lastName, age, grade, enrollmentDate, cond1, cond2, cond3, cond4, cond5);
		DatalogProgram datalog = createDatalogProgram(query);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Simple Query with More Conditions", datalog);
	}

	@Test
	public void testSimpleRuleWithCondition() {
		
		Function ans1 = createQuery(x, a);
		Function ans2 = createRule(ANS2, x, a);
		Function cond = dataFactory.getFunctionEQ(a, c1);
		
		/**
		 * ans1(x,a) :- Student(x), ans2(x,a)
		 * ans2(x,a) :- firstName(x,a), EQ(a,"John)
		 */
		CQIE query = dataFactory.getCQIE(ans1, student, ans2);
		CQIE rule1 = dataFactory.getCQIE(ans2, firstName, cond);
		DatalogProgram datalog = createDatalogProgram(query, rule1);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Simple Rule with Condition", datalog);
	}

	@Test
	public void testFiveSimpleRulesWithCondition() {
		
		Function ans1 = createQuery(x);
		Function ans2 = createRule(ANS2, x, a);
		Function ans3 = createRule(ANS3, x, b);
		Function ans4 = createRule(ANS4, x, c);
		Function ans5 = createRule(ANS5, x, d);
		Function ans6 = createRule(ANS6, x, e);
		Function cond1 = dataFactory.getFunctionEQ(a, c1);
		Function cond2 = dataFactory.getFunctionNEQ(b, c2);
		Function cond3 = dataFactory.getFunctionGT(c, c3);
		Function cond4 = dataFactory.getFunctionGTE(d, c4);
		Function cond5 = dataFactory.getFunctionLT(e, c5);
		
		/**
		 * ans1(x) :- Student(x), ans2(x,a)
		 * ans2(x,a) :- firstName(x,a), EQ(a,"John"), ans3(x,b)
		 * ans3(x,b) :- lastName(x,b), NEQ(b,"Smith"), ans4(x,b)
		 * ans4(x,c) :- age(x,c), GT(c,22), ans5(x,c)
		 * ans5(x,d) :- grade(x,d), GTE(d,35), ans6(x,e)
		 * ans6(x,e) :- enrollmentDate(x,e), LT(e,"2012-01-01 00:00:00")
		 */
		CQIE query = dataFactory.getCQIE(ans1, student, ans2);
		CQIE rule1 = dataFactory.getCQIE(ans2, firstName, cond1, ans3);
		CQIE rule2 = dataFactory.getCQIE(ans3, lastName, cond2, ans4);
		CQIE rule3 = dataFactory.getCQIE(ans4, age, cond3, ans5);
		CQIE rule4 = dataFactory.getCQIE(ans5, grade, cond4, ans6);
		CQIE rule5 = dataFactory.getCQIE(ans6, enrollmentDate, cond5);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2, rule3, rule4, rule5);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Five Simple Rules with Condition", datalog);
	}

	@Test
	public void testSimpleQueryWithNestedConditions() {
		
		Function ans1 = createQuery(x, a);
		Function cond1 = dataFactory.getFunctionEQ(a, c1);
		Function cond2 = dataFactory.getFunctionNEQ(b, c2);
		Function cond3 = dataFactory.getFunctionGT(c, c3);
		Function cond4 = dataFactory.getFunctionGTE(d, c4);
		Function cond5 = dataFactory.getFunctionLT(e, c5);
		Function cond6 = dataFactory.getFunctionAND(cond3, cond4);
		Function cond7 = dataFactory.getFunctionOR(cond6, cond5);
		
		/**
		 * ans1(x) :- Student(x), firstName(x,a), lastName(x,b), age(x,c), grade(x,d), enrollmentDate(x,e),
		 * 			  EQ(a,"John"), NEQ(b,"Smith"), 
		 * 			  OR(AND(GT(c,22), GTE(d,35)), LT(e,"2012-01-01 00:00:00"))
		 */
		CQIE query = dataFactory.getCQIE(ans1, student, firstName, lastName, age, grade, enrollmentDate, cond1, cond2, cond7);
		DatalogProgram datalog = createDatalogProgram(query);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Simple Query with Nested Conditions", datalog);
	}

	@Test
	public void testSameRules() {
		
		Function ans1 = createQuery(x);
		Function ans2 = createRule(ANS2, x);
		
		/**
		 * ans1(x) :- ans2(x)
		 * ans2(x) :- BachelorStudent(x)
		 * ans2(x) :- MasterStudent(x)
		 * ans2(x) :- DoctoralStudent(x)
		 */
		CQIE query = dataFactory.getCQIE(ans1, ans2);
		CQIE rule1 = dataFactory.getCQIE(ans2, bachelorStudent);
		CQIE rule2 = dataFactory.getCQIE(ans2, masterStudent);
		CQIE rule3 = dataFactory.getCQIE(ans2, doctoralStudent);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2, rule3);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Same Rules", datalog);
	}

	@Test
	public void testMultipleSameRules() {
		
		Function ans1 = createQuery(x, y);
		Function ans2 = createRule(ANS2, x);
		Function ans3 = createRule(ANS3, y);
		
		/**
		 * ans1(x,y) :- ans2(x), ans3(x,y)
		 * ans2(x) :- BachelorStudent(x)
		 * ans2(x) :- MasterStudent(x)
		 * ans2(x) :- DoctoralStudent(x)
		 * ans3(x,y) :- hasElementaryCourse(x,y)
		 * ans3(x,y) :- hasAdvancedCourse(x,y)
		 */
		CQIE query = dataFactory.getCQIE(ans1, ans2, ans3);
		CQIE rule1 = dataFactory.getCQIE(ans2, bachelorStudent);
		CQIE rule2 = dataFactory.getCQIE(ans2, masterStudent);
		CQIE rule3 = dataFactory.getCQIE(ans2, doctoralStudent);
		CQIE rule4 = dataFactory.getCQIE(ans3, hasElementaryCourse);
		CQIE rule5 = dataFactory.getCQIE(ans3, hasAdvancedCourse);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2, rule3, rule4, rule5);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Multiple Same Rules", datalog);
	}

	@Test
	public void testSameQueries() {
		
		Function ans1 = createQuery(x);
		Function ans2 = createRule(ANS2, x);
		Function ans3 = createRule(ANS3, x);
		Function ans4 = createRule(ANS4, x);
		
		/**
		 * ans1(x) :- ans2(x)
		 * ans1(x) :- ans3(x)
		 * ans1(x) :- ans4(x)
		 * ans2(x) :- BachelorStudent(x)
		 * ans3(x) :- MasterStudent(x)
		 * ans4(x) :- DoctoralStudent(x)
		 */
		CQIE query1 = dataFactory.getCQIE(ans1, ans2);
		CQIE query2 = dataFactory.getCQIE(ans1, ans3);
		CQIE query3 = dataFactory.getCQIE(ans1, ans4);
		CQIE rule1 = dataFactory.getCQIE(ans2, bachelorStudent);
		CQIE rule2 = dataFactory.getCQIE(ans3, masterStudent);
		CQIE rule3 = dataFactory.getCQIE(ans4, doctoralStudent);
		
		DatalogProgram datalog = createDatalogProgram(query1, query2, query3, rule1, rule2, rule3);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Same Queries", datalog);
	}

	@Test
	public void testJoinPredicate() {
		
		Function ans1 = createQuery(x, a, b);
		Function ans2 = createRule(ANS2, x, a);
		Function ans3 = createRule(ANS3, x, b);
		Function join = dataFactory.getSPARQLJoin(ans2, ans3);
		
		/**
		 * ans1(x,a,b) :- Join(ans2(x,a), ans3(x,b))
		 * ans2(x,a) :- firstName(x,a)
		 * ans3(x,b) :- lastName(x,b)
		 */
		CQIE query = dataFactory.getCQIE(ans1, join);
		CQIE rule1 = dataFactory.getCQIE(ans2, firstName);
		CQIE rule2 = dataFactory.getCQIE(ans3, lastName);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Simple Join Predicate", datalog);
	}

	@Test
	public void testNestedJoinPredicate() {
		
		Function ans1 = createQuery(x, a, b);
		Function ans2 = createRule(ANS2, x, a);
		Function ans3 = createRule(ANS3, x, b, c);
		Function ans4 = createRule(ANS4, x, b);
		Function ans5 = createRule(ANS5, x, c);
		Function join1 = dataFactory.getSPARQLJoin(ans2, ans3);
		Function join2 = dataFactory.getSPARQLJoin(ans4, ans5);
		
		/**
		 * ans1(x,a,b) :- Join(ans2(x,a), ans3(x,b,c))
		 * ans2(x,a) :- firstName(x,a)
		 * ans3(x,b,c) :- Join(ans4(x,b), ans5(x,c))
		 * ans4(x,b) :- lastName(x,b)
		 * ans5(x,c) :- age(x,c)
		 */
		CQIE query = dataFactory.getCQIE(ans1, join1);
		CQIE rule1 = dataFactory.getCQIE(ans2, firstName);
		CQIE rule2 = dataFactory.getCQIE(ans3, join2);
		CQIE rule3 = dataFactory.getCQIE(ans4, lastName);
		CQIE rule4 = dataFactory.getCQIE(ans5, age);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2, rule3, rule4);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Nested Join Predicate", datalog);
	}

	@Test
	public void testLeftJoinPredicate() {
		
		Function ans1 = createQuery(x, a, b);
		Function ans2 = createRule(ANS2, x, a);
		Function ans3 = createRule(ANS3, x, b);
		Function leftJoin = dataFactory.getSPARQLLeftJoin(ans2, ans3);
		
		/**
		 * ans1(x,a,b) :- LeftJoin(ans2(x,a), ans3(x,b))
		 * ans2(x,a) :- firstName(x,a)
		 * ans3(x,b) :- lastName(x,b)
		 */
		CQIE query = dataFactory.getCQIE(ans1, leftJoin);
		CQIE rule1 = dataFactory.getCQIE(ans2, firstName);
		CQIE rule2 = dataFactory.getCQIE(ans3, lastName);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Simple Left-Join Predicate", datalog);
	}

	@Test
	public void testNestedLeftJoinPredicate() {
		
		Function ans1 = createQuery(x, a, b);
		Function ans2 = createRule(ANS2, x, a);
		Function ans3 = createRule(ANS3, x, b, c);
		Function ans4 = createRule(ANS4, x, b);
		Function ans5 = createRule(ANS5, x, c);
		Function join1 = dataFactory.getSPARQLLeftJoin(ans2, ans3);
		Function join2 = dataFactory.getSPARQLLeftJoin(ans4, ans5);
		
		/**
		 * ans1(x,a,b) :- LeftJoin(ans2(x,a), ans3(x,b,c))
		 * ans2(x,a) :- firstName(x,a)
		 * ans3(x,b,c) :- LeftJoin(ans4(x,b), ans5(x,c))
		 * ans4(x,b) :- lastName(x,b)
		 * ans5(x,c) :- age(x,c)
		 */
		CQIE query = dataFactory.getCQIE(ans1, join1);
		CQIE rule1 = dataFactory.getCQIE(ans2, firstName);
		CQIE rule2 = dataFactory.getCQIE(ans3, join2);
		CQIE rule3 = dataFactory.getCQIE(ans4, lastName);
		CQIE rule4 = dataFactory.getCQIE(ans5, age);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2, rule3, rule4);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Nested Left-Join Predicate", datalog);
	}

	@Test
	public void testMixedJoinPredicates() {
		
		Function ans1 = createQuery(x, a, b);
		Function ans2 = createRule(ANS2, x, a);
		Function ans3 = createRule(ANS3, x, b, c);
		Function ans4 = createRule(ANS4, x, b);
		Function ans5 = createRule(ANS5, x, c);
		Function join1 = dataFactory.getSPARQLLeftJoin(ans2, ans3);
		Function join2 = dataFactory.getSPARQLJoin(ans4, ans5);
		
		/**
		 * ans1(x,a,b) :- LeftJoin(ans2(x,a), ans3(x,b,c))
		 * ans2(x,a) :- firstName(x,a)
		 * ans3(x,b,c) :- Join(ans4(x,b), ans5(x,c))
		 * ans4(x,b) :- lastName(x,b)
		 * ans5(x,c) :- age(x,c)
		 */
		CQIE query = dataFactory.getCQIE(ans1, join1);
		CQIE rule1 = dataFactory.getCQIE(ans2, firstName);
		CQIE rule2 = dataFactory.getCQIE(ans3, join2);
		CQIE rule3 = dataFactory.getCQIE(ans4, lastName);
		CQIE rule4 = dataFactory.getCQIE(ans5, age);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2, rule3, rule4);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Nested Left-Join Predicate", datalog);
	}

	@Test
	public void testSameRulesWithJoin() {
		
		Function ans1 = createQuery(x);
		Function ans2 = createRule(ANS2, x);
		Function ans3 = createRule(ANS3, x, d);
		Function join = dataFactory.getSPARQLJoin(ans2, ans3);
		
		/**
		 * ans1(x) :- Join(ans2(x), ans3(x,d))
		 * ans2(x) :- BachelorStudent(x)
		 * ans2(x) :- MasterStudent(x)
		 * ans2(x) :- DoctoralStudent(x)
		 * ans3(x,d) :- grade(x,d)
		 */
		CQIE query = dataFactory.getCQIE(ans1, join);
		CQIE rule1 = dataFactory.getCQIE(ans2, bachelorStudent);
		CQIE rule2 = dataFactory.getCQIE(ans2, masterStudent);
		CQIE rule3 = dataFactory.getCQIE(ans2, doctoralStudent);
		CQIE rule4 = dataFactory.getCQIE(ans3, grade);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2, rule3, rule4);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Same Rules with Join", datalog);
	}

	@Test
	public void testSameRulesWithNestedJoins() {
		
		Function ans1 = createQuery(x, a, b);
		Function ans2 = createRule(ANS2, x);
		Function ans3 = createRule(ANS3, x, a, b);
		Function ans4 = createRule(ANS4, x, a);
		Function ans5 = createRule(ANS5, x, b);
		Function join1 = dataFactory.getSPARQLJoin(ans2, ans3);
		Function join2 = dataFactory.getSPARQLJoin(ans4, ans5);
		
		/**
		 * ans1(x,a,b) :- Join(ans2(x), ans3(x,a,b))
		 * ans2(x) :- BachelorStudent(x)
		 * ans2(x) :- MasterStudent(x)
		 * ans2(x) :- DoctoralStudent(x)
		 * ans3(x,a,b) :- Join(ans4(x,a), ans5(x,b))
		 * ans4(x,a) :- firstName(x,a)
		 * ans5(x,b) :- lastName(x,b)
		 */
		CQIE query = dataFactory.getCQIE(ans1, join1);
		CQIE rule1 = dataFactory.getCQIE(ans2, bachelorStudent);
		CQIE rule2 = dataFactory.getCQIE(ans2, masterStudent);
		CQIE rule3 = dataFactory.getCQIE(ans2, doctoralStudent);
		CQIE rule4 = dataFactory.getCQIE(ans3, join2);
		CQIE rule5 = dataFactory.getCQIE(ans4, firstName);
		CQIE rule6 = dataFactory.getCQIE(ans5, lastName);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2, rule3, rule4, rule5, rule6);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Same Rules with Nested Joins", datalog);
	}

	@Test
	public void testSameRulesWithLeftJoin() {
		
		Function ans1 = createQuery(x);
		Function ans2 = createRule(ANS2, x);
		Function ans3 = createRule(ANS3, x, d);
		Function join = dataFactory.getSPARQLLeftJoin(ans2, ans3);
		
		/**
		 * ans1(x) :- LeftJoin(ans2(x), ans3(x,d))
		 * ans2(x) :- BachelorStudent(x)
		 * ans2(x) :- MasterStudent(x)
		 * ans2(x) :- DoctoralStudent(x)
		 * ans3(x,d) :- grade(x,d)
		 */
		CQIE query = dataFactory.getCQIE(ans1, join);
		CQIE rule1 = dataFactory.getCQIE(ans2, bachelorStudent);
		CQIE rule2 = dataFactory.getCQIE(ans2, masterStudent);
		CQIE rule3 = dataFactory.getCQIE(ans2, doctoralStudent);
		CQIE rule4 = dataFactory.getCQIE(ans3, grade);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2, rule3, rule4);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Same Rules with Left-Join", datalog);
	}

	@Test
	public void testSameRulesWithNestedLeftJoins() {
		
		Function ans1 = createQuery(x, a, b);
		Function ans2 = createRule(ANS2, x);
		Function ans3 = createRule(ANS3, x, a, b);
		Function ans4 = createRule(ANS4, x, a);
		Function ans5 = createRule(ANS5, x, b);
		Function join1 = dataFactory.getSPARQLLeftJoin(ans2, ans3);
		Function join2 = dataFactory.getSPARQLLeftJoin(ans4, ans5);
		
		/**
		 * ans1(x,a,b) :- LeftJoin(ans2(x), ans3(x,a,b))
		 * ans2(x) :- BachelorStudent(x)
		 * ans2(x) :- MasterStudent(x)
		 * ans2(x) :- DoctoralStudent(x)
		 * ans3(x,a,b) :- LeftJoin(ans4(x,a), ans5(x,b))
		 * ans4(x,a) :- firstName(x,a)
		 * ans5(x,b) :- lastName(x,b)
		 */
		CQIE query = dataFactory.getCQIE(ans1, join1);
		CQIE rule1 = dataFactory.getCQIE(ans2, bachelorStudent);
		CQIE rule2 = dataFactory.getCQIE(ans2, masterStudent);
		CQIE rule3 = dataFactory.getCQIE(ans2, doctoralStudent);
		CQIE rule4 = dataFactory.getCQIE(ans3, join2);
		CQIE rule5 = dataFactory.getCQIE(ans4, firstName);
		CQIE rule6 = dataFactory.getCQIE(ans5, lastName);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2, rule3, rule4, rule5, rule6);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Same Rules with Nested Left-Joins", datalog);
	}

	@Test
	public void testSameRulesWithMixedJoins() {
		
		Function ans1 = createQuery(x, a, b);
		Function ans2 = createRule(ANS2, x);
		Function ans3 = createRule(ANS3, x, a, b);
		Function ans4 = createRule(ANS4, x, a);
		Function ans5 = createRule(ANS5, x, b);
		Function join1 = dataFactory.getSPARQLJoin(ans2, ans3);
		Function join2 = dataFactory.getSPARQLLeftJoin(ans4, ans5);
		
		/**
		 * ans1(x,a,b) :- Join(ans2(x), ans3(x,a,b))
		 * ans2(x) :- BachelorStudent(x)
		 * ans2(x) :- MasterStudent(x)
		 * ans2(x) :- DoctoralStudent(x)
		 * ans3(x,a,b) :- LeftJoin(ans4(x,a), ans5(x,b))
		 * ans4(x,a) :- firstName(x,a)
		 * ans5(x,b) :- lastName(x,b)
		 */
		CQIE query = dataFactory.getCQIE(ans1, join1);
		CQIE rule1 = dataFactory.getCQIE(ans2, bachelorStudent);
		CQIE rule2 = dataFactory.getCQIE(ans2, masterStudent);
		CQIE rule3 = dataFactory.getCQIE(ans2, doctoralStudent);
		CQIE rule4 = dataFactory.getCQIE(ans3, join2);
		CQIE rule5 = dataFactory.getCQIE(ans4, firstName);
		CQIE rule6 = dataFactory.getCQIE(ans5, lastName);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2, rule3, rule4, rule5, rule6);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Same Rules with Mixed Joins", datalog);
	}

	@Test
	public void testQueryModifiers() {
		
		Function ans1 = createQuery(x, a, b);
		Function ans2 = createRule(ANS2, x, a, b);
		Function ans3 = createRule(ANS3, x, a, b);
		
		/**
		 * ans1(x,a,b) :- ans2(x,a,b)
		 * ans2(x,a,b) :- Student(x), ans3(x,a,b)
		 * ans3(x,a,b) :- firstName(x,a), lastName(x,b)
		 */
		CQIE query = dataFactory.getCQIE(ans1, ans2);
		CQIE rule1 = dataFactory.getCQIE(ans2, student, ans3);
		CQIE rule2 = dataFactory.getCQIE(ans3, firstName, lastName);
		
		DatalogProgram datalog = createDatalogProgram(query, rule1, rule2);
		
		OBDAQueryModifiers modifiers = new OBDAQueryModifiers();
		modifiers.setDistinct();
		modifiers.setLimit(100);
		modifiers.setOffset(20);
		modifiers.addOrderCondition(a, OrderCondition.ORDER_DESCENDING);
		datalog.setQueryModifiers(modifiers);
		
		// Translate the datalog and display the returned SPARQL string
		translateAndDisplayOutput("Query Modifiers", datalog);
	}

	private Function createQuery(Variable... vars) {
		int arity = vars.length;
		Predicate queryPredicate = dataFactory.getPredicate(OBDAVocabulary.QUEST_QUERY, arity);
		return dataFactory.getFunction(queryPredicate, vars);
	}

	private Function createRule(String ruleName, Variable... vars) {
		int arity = vars.length;
		Predicate rulePredicate = dataFactory.getPredicate(ruleName, arity);
		return dataFactory.getFunction(rulePredicate, vars);
	}

	private DatalogProgram createDatalogProgram(CQIE... queryAndRules) {
		List<CQIE> program = Arrays.asList(queryAndRules);
		return dataFactory.getDatalogProgram(program);
	}

	private void translateAndDisplayOutput(String title, DatalogProgram datalog) {
		final String sparqlOutput = datalogTranslator.translate(datalog);
		StringBuilder sb = new StringBuilder();
		sb.append("\n\n" + title);
		sb.append("\n====================================================================================\n");
		sb.append(datalog);
		sb.append("\n------------------------------------------------------------------------------------\n");
		sb.append(sparqlOutput);
		sb.append("\n====================================================================================\n");
		System.out.println(sb.toString());
	}

	// Reused static fields
	
	private static final String ANS2 = "ans2";
	private static final String ANS3 = "ans3";
	private static final String ANS4 = "ans4";
	private static final String ANS5 = "ans5";
	private static final String ANS6 = "ans6";

	private static Predicate predStudent;
	private static Predicate predBachelorStudent;
	private static Predicate predMasterStudent;
	private static Predicate predDoctoralStudent;
	
	private static Predicate predFirstName;
	private static Predicate predLastName;
	private static Predicate predAge;
	private static Predicate predGrade;
	private static Predicate predEnrollmentDate;
	
	private static Predicate predHasCourse;
	private static Predicate predHasElementaryCourse;
	private static Predicate predHasAdvancedCourse;
	
	static {
		predStudent = dataFactory.getClassPredicate("http://example.org/Student");
		predBachelorStudent = dataFactory.getClassPredicate("http://example.org/BachelorStudent");
		predMasterStudent = dataFactory.getClassPredicate("http://example.org/MasterStudent");
		predDoctoralStudent = dataFactory.getClassPredicate("http://example.org/DoctoralStudent");
		
		predFirstName = dataFactory.getDataPropertyPredicate("http://example.org/firstName", COL_TYPE.STRING);
		predLastName = dataFactory.getDataPropertyPredicate("http://example.org/lastName", COL_TYPE.STRING);
		predAge = dataFactory.getDataPropertyPredicate("http://example.org/age", COL_TYPE.INTEGER);
		predGrade = dataFactory.getDataPropertyPredicate("http://example.org/grade", COL_TYPE.DECIMAL);
		predEnrollmentDate = dataFactory.getDataPropertyPredicate("http://example.org/enrollmentDate", COL_TYPE.DATETIME);
		
		predHasCourse = dataFactory.getObjectPropertyPredicate("http://example.org/hasCourse");
		predHasElementaryCourse = dataFactory.getObjectPropertyPredicate("http://example.org/hasElementaryCourse");
		predHasAdvancedCourse = dataFactory.getObjectPropertyPredicate("http://example.org/hasAdvancedCourse");
	}
	
	private static Variable x;
	private static Variable y;
	private static Variable z;
	
	private static Variable a;
	private static Variable b;
	private static Variable c;
	private static Variable d;
	private static Variable e;
	private static Variable f;
	
	static {
		x = dataFactory.getVariable("x");
		y = dataFactory.getVariable("y");
		z = dataFactory.getVariable("z");
		
		a = dataFactory.getVariable("a");
		b = dataFactory.getVariable("b");
		c = dataFactory.getVariable("c");
		d = dataFactory.getVariable("d");
		e = dataFactory.getVariable("e");
		f = dataFactory.getVariable("f");
	}
	
	private static Constant c1;
	private static Constant c2;
	private static Constant c3;
	private static Constant c4;
	private static Constant c5;
	
	static {
		c1 = dataFactory.getConstantLiteral("John", COL_TYPE.STRING);
		c2 = dataFactory.getConstantLiteral("Smith", COL_TYPE.STRING);
		c3 = dataFactory.getConstantLiteral("25", COL_TYPE.INTEGER);
		c4 = dataFactory.getConstantLiteral("48.50", COL_TYPE.DECIMAL);
		c5 = dataFactory.getConstantLiteral("2012-03-20 00:00:00", COL_TYPE.DATETIME);
	}
	
	private static Function student;
	private static Function bachelorStudent;
	private static Function masterStudent;
	private static Function doctoralStudent;
	
	private static Function firstName;
	private static Function lastName;
	private static Function age;
	private static Function grade;
	private static Function enrollmentDate;
	
	private static Function hasCourse;
	private static Function hasElementaryCourse;
	private static Function hasAdvancedCourse;
	
	static {
		student = dataFactory.getFunction(predStudent, x);
		bachelorStudent = dataFactory.getFunction(predBachelorStudent, x);
		masterStudent = dataFactory.getFunction(predMasterStudent, x);
		doctoralStudent = dataFactory.getFunction(predDoctoralStudent, x);
		
		firstName = dataFactory.getFunction(predFirstName, x, a);
		lastName = dataFactory.getFunction(predLastName, x, b);
		age = dataFactory.getFunction(predAge, x, c);
		grade = dataFactory.getFunction(predGrade, x, d);
		enrollmentDate = dataFactory.getFunction(predEnrollmentDate, x, e);
		
		hasCourse = dataFactory.getFunction(predHasCourse, x, y);
		hasElementaryCourse = dataFactory.getFunction(predHasElementaryCourse, x, y);
		hasAdvancedCourse = dataFactory.getFunction(predHasAdvancedCourse, x, y);
	}
}
