package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class InequalitiesSatisfiabilityTests extends TestCase {
	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	public void test1() {
		// evaluate: x = 0, x = 1
		List<Function> body = new ArrayList<>();
		Constant t0, t1;
		
		Variable x = fac.getVariable("x");
		t0 = fac.getConstantLiteral("0", COL_TYPE.INTEGER);
		t1 = fac.getConstantLiteral("1", COL_TYPE.INTEGER);
		
		body.add(fac.getFunctionEQ(x, t0));
		body.add(fac.getFunctionEQ(x, t1));
		
		CQIE q = fac.getCQIE(null, body);
		
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		assertTrue(evaluator.evaluateExpressions(q));
	}

	public void test2() {
		// evaluate: x < 0, x > 1
		List<Function> body = new ArrayList<>();
		Constant t0, t1;
		
		Variable x = fac.getVariable("x");
		t0 = fac.getConstantLiteral("0", COL_TYPE.INTEGER);
		t1 = fac.getConstantLiteral("1", COL_TYPE.INTEGER);
		
		body.add(fac.getFunctionLT(x, t0));
		body.add(fac.getFunctionGT(x, t1));
		
		CQIE q = fac.getCQIE(null, body);
		
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		assertTrue(evaluator.evaluateExpressions(q));
	}

	public void test3() {
		// evaluate: x =< y, y != x
		List<Function> body = new ArrayList<>();
		
		Variable x = fac.getVariable("x");
		Variable y = fac.getVariable("y");
		
		body.add(fac.getFunctionLTE(x, y));
		body.add(fac.getFunctionLTE(y, x));
		body.add(fac.getFunctionNEQ(y, x));
		
		CQIE q = fac.getCQIE(null, body);
		
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		assertTrue(evaluator.evaluateExpressions(q));
	}
	
	public void test4() {
		// evaluate: x < y, y < 10, x < 2
		List<Function> body = new ArrayList<>();
		
		Variable x = fac.getVariable("x");
		Variable y = fac.getVariable("y");
		
		Constant c10 = fac.getConstantLiteral("10", COL_TYPE.INTEGER);
		Constant  c2 = fac.getConstantLiteral( "2", COL_TYPE.INTEGER);
		
		body.add(fac.getFunctionLT(x,   y));
		body.add(fac.getFunctionLT(y, c10));
		body.add(fac.getFunctionLT(x,  c2));
		
		CQIE q = fac.getCQIE(null, body);
		
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		assertFalse(evaluator.evaluateExpressions(q));
	}
	
	/**
	 * Test transitivity of LT
	 */
	public void test5() {
		// evaluate: x1 < x2, ..., x(N-1) < xN, xN < x1
		List<Function> body = new ArrayList<>();
		CQIE q;
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		
		int N = 10;
		Variable[] vars = new Variable[N];
		
		for(int i=0; i<N; i++)
			vars[i] = fac.getVariable("x" + i);
		
		for(int i=0; i<N-1; i++)
			body.add(fac.getFunctionLT(vars[i], vars[i+1]));
		
		q = fac.getCQIE(null, body);
		assertFalse(evaluator.evaluateExpressions(q));
		
		body.add(fac.getFunctionGT(vars[0], vars[N-1]));
		
		q = fac.getCQIE(null, body);
		assertTrue(evaluator.evaluateExpressions(q));
	}
	public void test6() {
		// evaluate: x1 =< x2, ..., x(N-1) =< xN, xN =< x1, xN != x1 
		List<Function> body = new ArrayList<>();
		CQIE q;
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		
		int N = 10;
		Variable[] vars = new Variable[N+1];
		
		for(int i = 0; i <= N; i++)
			vars[i] = fac.getVariable("x" + i);
		
		for(int i = 0; i < N; i++)
			if (i % 2 == 0)
				body.add(fac.getFunctionLTE(vars[i], vars[i+1]));
			else
				body.add(fac.getFunctionGTE(vars[i+1], vars[i]));
		
		body.add(fac.getFunctionLTE(vars[N], vars[0]));
		
		q = fac.getCQIE(null, body);
		assertFalse(evaluator.evaluateExpressions(q));
		
		body.add(fac.getFunctionNEQ(vars[0], vars[N]));
		
		q = fac.getCQIE(null, body);
		assertTrue(evaluator.evaluateExpressions(q));
	}

	public void test7() {
		// evaluate random disjoint dis-equalities
		List<Function> body = new ArrayList<>();
		CQIE q;
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		
		int N = 10;
		
		for(int i = 0; i < N; i++)
			if (i % 2 == 0)
				body.add(fac.getFunctionLTE(
							fac.getVariable("x" + i),
							fac.getConstantLiteral(Double.toString(Math.random() * N), COL_TYPE.FLOAT)
						));
			else
				body.add(fac.getFunctionGTE(
						fac.getVariable("x" + i),
						fac.getConstantLiteral(Double.toString(Math.random() * N), COL_TYPE.FLOAT)
					));
		
		q = fac.getCQIE(null, body);
		assertFalse(evaluator.evaluateExpressions(q));
	}
	
	/*
	public void test1() {
		System.out.println("[INEQ] test on satisf!");
		//DatalogProgram unfolding = null;
		List<Function> body = new ArrayList<>();
		Constant t0, t1;
		
		Variable x = fac.getVariable("x");
		t0 = fac.getConstantLiteral("1", COL_TYPE.INTEGER);
		t1 = fac.getConstantLiteral("0", COL_TYPE.INTEGER);
		
		body.add(fac.getFunctionEQ(x, t0));
		body.add(fac.getFunctionEQ(x, t1));
		
		CQIE q = fac.getCQIE(null, body);
		
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		if (evaluator.evaluateExpressions(q)) {
			System.out.println("Result: unsatisfiable");
		} else {
			System.out.println("Result: satisfiable");
		}
		
	}
	
	*//*
	public void testUnfoldingWithMultipleSuccessfulResolutions() {

		// query rule
		DatalogProgram queryProgram = fac.getDatalogProgram();
		Function a = fac.getFunction(fac.getClassPredicate("A"), fac.getVariable("x"));
		Function R = fac.getFunction(fac.getObjectPropertyPredicate("R"), fac.getVariable("x"), fac.getVariable("y"));
		Function lj = fac.getSPARQLLeftJoin(a, R);
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

		DatalogUnfolder unfolder = new DatalogUnfolder(p.getRules());
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
		Function lj = fac.getSPARQLLeftJoin(a, R);
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

		DatalogUnfolder unfolder = new DatalogUnfolder(p.getRules());
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
		Function lj = fac.getSPARQLLeftJoin(a, R);
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

		DatalogUnfolder unfolder = new DatalogUnfolder(p.getRules());
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
			Function lj = fac.getSPARQLLeftJoin(a, R);
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

			DatalogUnfolder unfolder = new DatalogUnfolder(p.getRules());
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
		Function lj = fac.getSPARQLLeftJoin(a, R);
		head = fac.getFunction(fac.getPredicate("q", 2), fac.getVariable("x"), fac.getVariable("y"));
		CQIE rule1 = fac.getCQIE(head, lj);
		query.appendRule(rule1);

		DatalogUnfolder unfolder = new DatalogUnfolder(p.getRules());
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
	*/
}
