package it.unibz.inf.ontop.sql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.parser.SelectQueryParser;
import it.unibz.inf.ontop.sql.parser.exceptions.UnsupportedSelectQueryException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 * -
 */
public class SelectQueryParserTest {

    private OBDADataFactory FACTORY;

    @Before
    public void beforeEachTest() {
        FACTORY = OBDADataFactoryImpl.getInstance();
    }

    @Test
    public void simpleJoin_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);
        CQIE parse = parser.parse("SELECT A, B FROM P, Q"); //todo: fix this when the column reference A is ambiguous should throw an exception!!
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable b1 = FACTORY.getVariable("B1");

        Function atomP = FACTORY.getFunction(
                FACTORY.getPredicate("P", new Predicate.COL_TYPE[]{null, null}),
                ImmutableList.of(a1, b1));

        assertTrue(parse.getBody().contains(atomP));

        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive
        Variable c2 = FACTORY.getVariable("C2");

        Function atomQ = FACTORY.getFunction(
                FACTORY.getPredicate("Q", new Predicate.COL_TYPE[]{null, null}),
                ImmutableList.of(a2, c2));

        assertTrue(parse.getBody().contains(atomQ));
    }


    @Test // TODO: does relationAliasesConsistent check is wrong?
    public void innerJoinOn_Same_Table_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);
        CQIE parse = parser.parse("SELECT p1.A, p2.B FROM P p1 INNER JOIN  P p2 on p1.A = p2.A ");
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive

        Function atomQ = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, a2));

        assertTrue(parse.getBody().contains(atomQ));
    }


    @Test
    public void innerJoinOn_EQ_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);
        CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A = Q.A ");
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive

        Function atomQ = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, a2));

        assertTrue(parse.getBody().contains(atomQ));
    }


    @Test
    public void innerJoinOn_GTE_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);
        CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A >= Q.A ");
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive

        Function atomQ = FACTORY.getFunction(ExpressionOperation.GTE, ImmutableList.of(a1, a2));

        assertTrue(parse.getBody().contains(atomQ));
    }

    @Test
    public void innerJoinOn_GT_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A >Q.A");
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive

        Function atomQ = FACTORY.getFunction(ExpressionOperation.GT, ImmutableList.of(a1, a2));

        assertTrue(parse.getBody().contains(atomQ));
    }

    @Test
    public void innerJoinOn_LT_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A < Q.A");
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive

        Function atomQ = FACTORY.getFunction(ExpressionOperation.LT, ImmutableList.of(a1, a2));

        assertTrue(parse.getBody().contains(atomQ));
    }

    @Test
    public void innerJoinOn_LTE_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A <= Q.A");
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive

        Function atomQ = FACTORY.getFunction(ExpressionOperation.LTE, ImmutableList.of(a1, a2));

        assertTrue(parse.getBody().contains(atomQ));
    }

    @Test
    public void innerJoinOn_NEQ_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A <> Q.A");
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive

        Function atomQ = FACTORY.getFunction(ExpressionOperation.NEQ, ImmutableList.of(a1, a2));

        assertTrue(parse.getBody().contains(atomQ));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void innerJoinOn_naturalJoin_ambiguity_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A =  Q.A NATURAL JOIN  R ");
    }


    //TODO: In this case column reference "a" is ambiguous. Does this should be check during the "selection" operation handler?
    @Test(expected = UnsupportedOperationException.class)
    public void innerJoinOn_naturalJoin_ambiguity2_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // column reference "a" is ambiguous
        String sql = "SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN  R on Q.C =  R.C;";
        CQIE parse = parser.parse(sql);
        System.out.println("\n" + sql + "\n" + "column reference \"a\" is ambiguous --- this is wrongly parsed:\n" + parse);
    }

    @Test
    public void innerJoinOn_naturalJoin_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        CQIE parse = parser.parse("SELECT P.A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN  R on Q.C =  R.C;");
        System.out.println(parse);
    }

    @Test
    public void innerJoinOn_AND_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A <> Q.A AND P.A = 2 AND Q.A = 3");
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        assertEquals(5, parse.getBody().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive
        ValueConstant v2 = FACTORY.getConstantLiteral("2", Predicate.COL_TYPE.LONG);
        ValueConstant v3 = FACTORY.getConstantLiteral("3", Predicate.COL_TYPE.LONG);

        Function atomQ = FACTORY.getFunction(ExpressionOperation.NEQ, ImmutableList.of(a1, a2));
        assertEquals(atomQ, parse.getBody().get(2));

        Function atomQ2 = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, v2));
        assertEquals(atomQ2, parse.getBody().get(3));

        Function atomQ3 = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a2, v3));
        assertEquals(atomQ3, parse.getBody().get(4));
    }

    @Test
    public void innerJoinOn_EQ_CONSTANT_INT_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A = 1");
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        Variable a1 = FACTORY.getVariable("A1");
        ValueConstant a2 = FACTORY.getConstantLiteral("1", Predicate.COL_TYPE.LONG); // variable are key sensitive

        Function atomQ = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, a2));

        assertTrue(parse.getBody().contains(atomQ));
    }

    @Test
    public void natural_join_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A, C FROM P NATURAL JOIN  Q");
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2");

        Function atomQ = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, a2));

        assertTrue(parse.getBody().contains(atomQ));
    }

    @Test
    public void subjoin_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A, C FROM R JOIN (P NATURAL JOIN Q) AS S ON R.A = S.A");
        System.out.println(parse);
    }

    @Test
    public void join_using_inner_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN Q USING (A)");
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2");

        Function atomQ = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, a2));

        assertTrue(parse.getBody().contains(atomQ));
    }

    @Test
    public void join_using_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN Q USING (A)");
        System.out.println(parse);

        assertTrue(parse.getHead().getTerms().size() == 0);
        assertTrue(parse.getReferencedVariables().size() == 4);

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2");


        Function atomQ = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, a2));

        assertTrue(parse.getBody().contains(atomQ));
    }


    @Test //todo: this should works
    public void join_using_2_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT Q.A, R.B FROM Q INNER JOIN R USING (A,B)");
        System.out.println(parse);

        assertTrue(parse.getHead().getTerms().size() == 0);
        assertTrue(parse.getReferencedVariables().size() == 4);

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2");

        Function atomQ_A = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, a2));

        Variable b1 = FACTORY.getVariable("B1");
        Variable b2 = FACTORY.getVariable("B2");

        Function atomQ_B = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(b1, b2));

        assertTrue(parse.getBody().contains(atomQ_A));
        assertTrue(parse.getBody().contains(atomQ_B));
    }


    @Test
    public void concat_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A FROM P where A = CONCAT('A', A, 'B')");
        System.out.println(parse);
        assertEquals(0, parse.getHead().getTerms().size());

        Variable a1 = FACTORY.getVariable("A1");
        ValueConstant v1 = FACTORY.getConstantLiteral("A", Predicate.COL_TYPE.STRING); // variable are key sensitive
        ValueConstant v2 = FACTORY.getConstantLiteral("B", Predicate.COL_TYPE.STRING);

        Function atom = FACTORY.getFunction(ExpressionOperation.EQ, a1,
                FACTORY.getFunction(ExpressionOperation.CONCAT, ImmutableList.of(v1,
                        FACTORY.getFunction(ExpressionOperation.CONCAT, a1, v2))
                ));

        System.out.println(atom);
        assertTrue(parse.getBody().contains(atom));
    }


    @Test
    public void in_condition_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A, B FROM P where P.A IN ( 1, 2, 3 )");
        System.out.println(parse);
        assertEquals(0, parse.getHead().getTerms().size());

        Variable a1 = FACTORY.getVariable("A1");
        ValueConstant v1 = FACTORY.getConstantLiteral("1", Predicate.COL_TYPE.LONG); // variable are key sensitive
        ValueConstant v2 = FACTORY.getConstantLiteral("2", Predicate.COL_TYPE.LONG);
        ValueConstant v3 = FACTORY.getConstantLiteral("3", Predicate.COL_TYPE.LONG);

        Function atom = FACTORY.getFunction(ExpressionOperation.OR, ImmutableList.of(
                FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, v1)),
                FACTORY.getFunction(ExpressionOperation.OR, ImmutableList.of(
                        FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, v2)),
                        FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, v3))))
        ));

        assertTrue(parse.getBody().contains(atom));
    }

    @Test
    public void in_condition1_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT A, B FROM P where P.A IN ( 1 )");
        System.out.println(parse);
        assertEquals(0, parse.getHead().getTerms().size());

        Variable a1 = FACTORY.getVariable("A1");
        ValueConstant v1 = FACTORY.getConstantLiteral("1", Predicate.COL_TYPE.LONG); // variable are key sensitive

        Function atom = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, v1));

        assertTrue(parse.getBody().contains(atom));
    }


    @Test(expected = UnsupportedSelectQueryException.class)
    public void left_outer_Join_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P LEFT  OUTER JOIN  Q on P.A =  Q.A ");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void right_outer_Join_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P RIGHT  OUTER JOIN  Q on P.A =  Q.A ");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void right_Join_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P RIGHT JOIN  Q on P.A =  Q.A ");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void left_Join_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P LEFT JOIN  Q on P.A =  Q.A ");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void full_outer_Join_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        CQIE parse = parser.parse("SELECT A, C FROM P FULL OUTER JOIN  P on P.A =  Q.A ");
    }


    @Test()
    public void full_natural_Join_3_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        CQIE parse = parser.parse("SELECT * FROM P NATURAL JOIN Q NATURAL JOIN R");

        assertFalse(parse.getBody().isEmpty());
    }


    @Test()
    public void full_natural_Join_REDUCE_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        CQIE parse = parser.parse("SELECT * FROM P AS dp INNER REDUCE JOIN Q AS fis ON dp.A = fis.B");
        assertNull(parse); // ******* This query cannot be parsed because MS-SQL REDUCE operator
    }


    @Test()
    public void full_natural_Join_REDISTRIBUTE_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        CQIE parse = parser.parse("SELECT * FROM P AS dp INNER REDISTRIBUTE JOIN Q AS fis ON dp.A = fis.B");
        assertNull(parse); // ******* This query cannot be parsed because MS-SQL REDISTRIBUTE operator
    }

    @Test()
    public void full_natural_Join_REPLICATE_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);

        CQIE parse = parser.parse("SELECT * FROM P AS dp INNER REPLICATE JOIN Q AS fis ON dp.A = fis.B");
        assertNull(parse); // ******* This query cannot be parsed because MS-SQL REDISTRIBUTE operator
    }


    @Test()
    public void full_natural_Join_Plus_Oracle_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        CQIE parse = parser.parse("SELECT A, B FROM P  WHERE P.A(+) = P.B;");
        assertFalse(parse.getBody().isEmpty()); // The (+) operator does not produce an outer join if you specify one table in the outer query and the other table in an inner query.
    }


    @Test()
    public void full_natural_Join_Plus_2_Oracle_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
//        Users familiar with the traditional Oracle Database outer joins syntax will recognize the same query in this form:
        CQIE parse = parser.parse("SELECT A, B FROM P  WHERE P.A(+) = P.A;");
        assertFalse( parse.getBody().isEmpty());
    }


    @Test(expected = UnsupportedOperationException.class)
    public void using_should_not_work_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // ERROR: common column name "a" appears more than once in left table
        CQIE parse = parser.parse("SELECT * FROM Q INNER JOIN P ON P.A=Q.A  INNER JOIN  R USING (A) ;");
        assertNull(parse);
    }



    @Test()
    public void joins_allowed_to_parse() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        ImmutableList.of(
                "SELECT * FROM P, Q;",
                "SELECT * FROM P NATURAL JOIN  Q;",
                "SELECT * FROM P CROSS JOIN  Q;",
                "SELECT * FROM P JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P INNER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P JOIN  Q USING(A);",
                "SELECT * FROM P INNER JOIN  Q USING(A);")
                .forEach(query -> {
                    final CQIE parse = parser.parse("SELECT A, B FROM P  WHERE P.A(+) = P.B;");
                    assertFalse(parse.getBody().isEmpty());
                    System.out.println(query + " - OK!");
                });

    }


    @Test()
    public void joins_invalid_to_parse_JSQL_error() {
        // During this tests the parser rises ParseException
        // todo: this should be categorised as invalid mapping which cannot be supported

        ImmutableList.of(
                "SELECT * FROM P OUTER JOIN  Q;",
                "SELECT * FROM P NATURAL OUTER JOIN  Q;",
                "SELECT * FROM P CROSS OUTER JOIN  Q;",
                "SELECT * FROM P RIGHT INNER JOIN  Q;",
                "SELECT * FROM P NATURAL INNER JOIN  Q;",
                "SELECT * FROM P FULL INNER JOIN  Q;",
                "SELECT * FROM P LEFT INNER JOIN  Q;",
                "SELECT * FROM P CROSS INNER JOIN  Q;",
                "SELECT * FROM P OUTER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P NATURAL OUTER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P CROSS OUTER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P RIGHT INNER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P NATURAL INNER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P FULL INNER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P LEFT INNER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P CROSS INNER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P OUTER JOIN  Q USING(A);",
                "SELECT * FROM P NATURAL OUTER JOIN  Q USING(A);",
                "SELECT * FROM P CROSS OUTER JOIN  USING(A);",
                "SELECT * FROM P RIGHT INNER JOIN  Q USING(A);",
                "SELECT * FROM P NATURAL INNER JOIN  Q USING(A);",
                "SELECT * FROM P FULL INNER JOIN  Q USING(A);",
                "SELECT * FROM P LEFT INNER JOIN  Q USING(A);",
                "SELECT * FROM P CROSS INNER JOIN  Q USING(A);"
        ).forEach(query -> {
            final SelectQueryParser parser = new SelectQueryParser(createMetadata());
            final CQIE parse = parser.parse(query);
            assertTrue( parser.isParseException());
            assertNull(parse);

       });
    }

    @Test()
    public void joins_invalid_to_parse() {
        final SelectQueryParser parser = new SelectQueryParser(createMetadata());
        ImmutableList.of(
                "SELECT * FROM P JOIN  Q;",
                "SELECT * FROM P RIGHT OUTER JOIN  Q;",
                "SELECT * FROM P RIGHT JOIN  Q;",
                "SELECT * FROM P FULL JOIN  Q;",
                "SELECT * FROM P LEFT JOIN  Q;",
                "SELECT * FROM P FULL OUTER JOIN  Q;",
                "SELECT * FROM P LEFT OUTER JOIN  Q;",
                "SELECT * FROM P INNER JOIN  Q;",
                "SELECT * FROM P NATURAL JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P CROSS JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P NATURAL JOIN  Q USING(A);",
                "SELECT * FROM P CROSS JOIN  Q USING(A);"
                )
                .forEach(this::assertUnsupported);
    }

    @Test()
    public void joins_not_supported_to_parse() {
        final SelectQueryParser parser = new SelectQueryParser(createMetadata());
        ImmutableList.of(
                "SELECT * FROM P RIGHT JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P FULL JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P LEFT JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P RIGHT OUTER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P FULL OUTER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P LEFT OUTER JOIN  Q ON P.A = Q.A;")
                .forEach(this::assertUnsupported);
    }

    private void  assertUnsupported(String query ){
        final SelectQueryParser parser = new SelectQueryParser(createMetadata());
        Exception e = null;
        CQIE parse = null;
        try {
            parse = parser.parse(query);
            System.out.println(query + " - Wrong!");
        } catch (UnsupportedSelectQueryException ex) { // todo: specialise exception
            System.out.println(query + " - OK");
            e = ex;
        }
        assertNotNull(e);
        assertNull(parse);

    }

    @Test
    public void select__Join_2_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        CQIE parse = parser.parse("SELECT a.A, b.B FROM P AS a JOIN R AS b  ON (a.A = b.B);");

        assertNotNull(parse);
    }

    @Test
    public void parser_combination_query_test() {
        int i = 0;
        parseQueryTest("SELECT * FROM P, Q;", i);
        for (String c : new String[]{"", " ON P.A = Q.A", " USING (A)"}) {
            for (String b : new String[]{"", " OUTER", " INNER"}) {
                for (String a : new String[]{"", " RIGHT", " NATURAL", " FULL", " LEFT", " CROSS"}) {
                    i++;
                    parseQueryTest("SELECT * FROM P" + a + b + " JOIN Q" + c + ";", i);
                }
            }
        }
    }

    private void parseQueryTest(String query, int i) {
        try {
            System.out.print("" + i + ": " + query);
            Statement statement = CCJSqlParserUtil.parse(query);
            System.out.println(" OK");
        } catch (Exception e) {
            System.out.println(" " + e.getClass().getCanonicalName() + "  occurred");
        }
    }


    private DBMetadata createMetadata() {
        DBMetadata metadata = DBMetadataExtractor.createDummyMetadata();
        QuotedIDFactory idfac = metadata.getQuotedIDFactory();

        RelationID table1 = idfac.createRelationID(null, "P");
        QuotedID attx0 = idfac.createAttributeID("A");
        QuotedID atty0 = idfac.createAttributeID("B");

        DatabaseRelationDefinition relation1 = metadata.createDatabaseRelation(table1);
        relation1.addAttribute(attx0, 0, "INT", false);
        relation1.addAttribute(atty0, 0, "INT", false);

        RelationID table2 = idfac.createRelationID(null, "Q");
        QuotedID attx1 = idfac.createAttributeID("A");
        QuotedID atty1 = idfac.createAttributeID("C");

        DatabaseRelationDefinition relation2 = metadata.createDatabaseRelation(table2);
        relation2.addAttribute(attx1, 0, "INT", false);
        relation2.addAttribute(atty1, 0, "INT", false);

        RelationID table3 = idfac.createRelationID(null, "R");
        QuotedID attx3 = idfac.createAttributeID("A");
        QuotedID atty3 = idfac.createAttributeID("B");
        QuotedID attu3 = idfac.createAttributeID("C");
        QuotedID attv3 = idfac.createAttributeID("D");

        DatabaseRelationDefinition relation3 = metadata.createDatabaseRelation(table3);
        relation3.addAttribute(attx3, 0, "INT", false);
        relation3.addAttribute(atty3, 0, "INT", false);
        relation3.addAttribute(attu3, 0, "INT", false);
        relation3.addAttribute(attv3, 0, "INT", false);

        return metadata;
    }


}
