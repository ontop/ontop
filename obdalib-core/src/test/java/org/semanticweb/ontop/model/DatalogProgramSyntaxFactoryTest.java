package org.semanticweb.ontop.model;

import org.junit.Test;

import static org.semanticweb.ontop.model.DatalogProgramSyntaxFactory.*;

import static org.junit.Assert.assertEquals;

public class DatalogProgramSyntaxFactoryTest {

	@Test
	public void test01() {

		String r1 = "ans1(URI(\"http://www.example.org/test#{}\",t1_4),http://www.w3.org/2000/01/rdf-schema#Literal(t2_2),"
				+ "http://www.w3.org/2000/01/rdf-schema#Literal(t5_3),http://www.w3.org/2000/01/rdf-schema#Literal(t6_4)) "
				+ ":- LeftJoin(ans4(pf1),ans5(namef2,nick1f3,nick2f4,p5),EQ(pf1,p5))";

		String r2 = "ans4(URI(\"http://www.example.org/test#{}\",t1_1)) "
				+ ":- people(t1_1,t2_1,t3_1,t4_1,t5_1,t6_1), IS_NOT_NULL(t1_1)";

		Predicate ans1 = predicate("ans1", 4);

		Predicate ans4 = predicate("ans4", 1);
		Predicate ans5 = predicate("ans5", 4);
		Predicate people = predicate("people", 6);

		//@formatter:off
		DatalogProgram program = program(
				// r1
				rule(
				    func(ans1, uri(constant("http://www.example.org/test#{}"), var("t1_4")), rdfsLiteral(var("t2_2")),
				    		rdfsLiteral(var("t5_3")), rdfsLiteral(var("t6_4"))),
					leftJoin(
						func(ans4, var("pf1")),
						func(ans5, var("namef2"), var("nick1f3"), var("nick2f4"), var("p5")),
						eq(var("pf1"), var("p5")))
				),
				// r2
				rule(
				    func(ans4, uri(constant("http://www.example.org/test#{}"), var("t1_1"))), //
					func(people, var("t1_1"), var("t2_1"), var("t3_1"), var("t4_1"), var("t5_1"), var("t6_1")), //
					isNotNull(var("t1_1"))
				)
		);
		//@formatter:on

		System.out.println(program);

		assertEquals(r1, program.getRules().get(0).toString());
		assertEquals(r2, program.getRules().get(1).toString());

	}
}
