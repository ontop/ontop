package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.datalog.DatalogProgram;
import it.unibz.inf.ontop.datalog.DatalogProgramSyntaxFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import org.junit.Test;

import static it.unibz.inf.ontop.datalog.DatalogProgramSyntaxFactory.func;
import static org.junit.Assert.assertEquals;

public class DatalogProgramSyntaxFactoryTest {

	@Test
	public void test01() {

		String r1 = "ans1(URI2(\"http://www.example.org/test#{}\",t1_4),http://www.w3.org/2000/01/rdf-schema#Literal(t2_2),"
				+ "http://www.w3.org/2000/01/rdf-schema#Literal(t5_3),http://www.w3.org/2000/01/rdf-schema#Literal(t6_4)) "
				+ ":- LeftJoin(ans4(pf1),ans5(namef2,nick1f3,nick2f4,p5),EQ(pf1,p5))";

		String r2 = "ans4(URI2(\"http://www.example.org/test#{}\",t1_1)) "
				+ ":- people(t1_1,t2_1,t3_1,t4_1,t5_1,t6_1), IS_NOT_NULL(t1_1)";

		Predicate ans1 = DatalogProgramSyntaxFactory.predicate("ans1", 4);

		Predicate ans4 = DatalogProgramSyntaxFactory.predicate("ans4", 1);
		Predicate ans5 = DatalogProgramSyntaxFactory.predicate("ans5", 4);
		Predicate people = DatalogProgramSyntaxFactory.predicate("people", 6);

		//@formatter:off
		DatalogProgram program = DatalogProgramSyntaxFactory.program(
				// r1
				DatalogProgramSyntaxFactory.rule(
				    DatalogProgramSyntaxFactory.func(ans1, DatalogProgramSyntaxFactory.uri(DatalogProgramSyntaxFactory.constant("http://www.example.org/test#{}"), DatalogProgramSyntaxFactory.var("t1_4")), DatalogProgramSyntaxFactory.rdfsLiteral(DatalogProgramSyntaxFactory.var("t2_2")),
				    		DatalogProgramSyntaxFactory.rdfsLiteral(DatalogProgramSyntaxFactory.var("t5_3")), DatalogProgramSyntaxFactory.rdfsLiteral(DatalogProgramSyntaxFactory.var("t6_4"))),
					DatalogProgramSyntaxFactory.leftJoin(
						func(ans4, DatalogProgramSyntaxFactory.var("pf1")),
						DatalogProgramSyntaxFactory.func(ans5, DatalogProgramSyntaxFactory.var("namef2"), DatalogProgramSyntaxFactory.var("nick1f3"), DatalogProgramSyntaxFactory.var("nick2f4"), DatalogProgramSyntaxFactory.var("p5")),
						DatalogProgramSyntaxFactory.eq(DatalogProgramSyntaxFactory.var("pf1"), DatalogProgramSyntaxFactory.var("p5")))
				),
				// r2
				DatalogProgramSyntaxFactory.rule(
				    func(ans4, DatalogProgramSyntaxFactory.uri(DatalogProgramSyntaxFactory.constant("http://www.example.org/test#{}"), DatalogProgramSyntaxFactory.var("t1_1"))), //
					DatalogProgramSyntaxFactory.func(people, DatalogProgramSyntaxFactory.var("t1_1"), DatalogProgramSyntaxFactory.var("t2_1"), DatalogProgramSyntaxFactory.var("t3_1"), DatalogProgramSyntaxFactory.var("t4_1"), DatalogProgramSyntaxFactory.var("t5_1"), DatalogProgramSyntaxFactory.var("t6_1")), //
					DatalogProgramSyntaxFactory.isNotNull(DatalogProgramSyntaxFactory.var("t1_1"))
				)
		);
		//@formatter:on

		System.out.println(program);

		assertEquals(r1, program.getRules().get(0).toString());
		assertEquals(r2, program.getRules().get(1).toString());

	}
}
