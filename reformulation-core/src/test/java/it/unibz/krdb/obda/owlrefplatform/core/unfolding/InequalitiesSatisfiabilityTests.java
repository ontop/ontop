package it.unibz.krdb.obda.owlrefplatform.core.unfolding;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import org.junit.Test;


import java.util.ArrayList;
import java.util.List;

//import org.junit.Test;

import junit.framework.TestCase;

public class InequalitiesSatisfiabilityTests extends TestCase {
	OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	public void test1() {
		// evaluate: x < 1, y > 3, x > y
		List<Function> body = new ArrayList<>();
		Constant t1, t3;
		
		Variable x = fac.getVariable("x");
		Variable y = fac.getVariable("y");
		t1 = fac.getConstantLiteral("1", COL_TYPE.INTEGER);
		t3 = fac.getConstantLiteral("3", COL_TYPE.INTEGER);
		
		body.add(fac.getFunctionLT(x, t1));
		body.add(fac.getFunctionGT(y, t3));
		body.add(fac.getFunctionLT(y, x));
		
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
		// evaluate: x = y, x != y
		List<Function> body = new ArrayList<>();
		
		Variable x = fac.getVariable("x");
		Variable y = fac.getVariable("y");
		
		body.add( fac.getFunctionEQ(x, y));
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
	 * Evaluate query: x{1} < x{2}, ..., x{N-1} < x{N}, x{N} < x{1}
	 */
	@Test
	public void test5() {
		List<Function> body = new ArrayList<>();
		CQIE q;
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		
		final int N = 10;
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
	
}


