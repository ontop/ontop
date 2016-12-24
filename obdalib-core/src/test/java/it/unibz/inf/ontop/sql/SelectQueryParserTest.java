package it.unibz.inf.ontop.sql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.parser.RelationalExpression;
import it.unibz.inf.ontop.sql.parser.SelectQueryParser;
import it.unibz.inf.ontop.sql.parser.exceptions.InvalidSelectQueryException;
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
    public void inner_join_on_same_table_test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);
        RelationalExpression re = parser.parse("SELECT p1.A, p2.B FROM P p1 INNER JOIN  P p2 on p1.A = p2.A ");
        System.out.println(re);

//        assertEquals(2, parse.getHead().getTerms().size());
//        assertEquals(4, parse.getReferencedVariables().size());

        Function atom_EQ = FACTORY.getFunction(ExpressionOperation.EQ,
                ImmutableList.of(FACTORY.getVariable("A1"), FACTORY.getVariable("A2")));

        Function atom_P_1 = FACTORY.getFunction(
                FACTORY.getPredicate("P", new Predicate.COL_TYPE[]{null, null}),
                ImmutableList.of(FACTORY.getVariable("A1"), FACTORY.getVariable("B1")));

        Function atom_P_2 = FACTORY.getFunction(
                FACTORY.getPredicate("P", new Predicate.COL_TYPE[]{null, null}),
                ImmutableList.of(FACTORY.getVariable("A2"), FACTORY.getVariable("B2")));

        assertEquals(1, re.getFilterAtoms().size());
        assertTrue(re.getFilterAtoms().contains(atom_EQ));

        assertEquals(2, re.getDataAtoms().size());
        assertTrue(re.getDataAtoms().contains(atom_P_1));
        assertTrue(re.getDataAtoms().contains(atom_P_2));
    }


    @Test(expected = InvalidSelectQueryException.class)
    public void inner_join_on_inner_join_ambiguity_test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A =  Q.A NATURAL JOIN  R ");
    }


    @Test(expected = InvalidSelectQueryException.class)
    public void inner_join_on_inner_join_ambiguity2_test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // column reference "a" is ambiguous
        String sql = "SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN  R on Q.C =  R.C;";
        RelationalExpression re = parser.parse(sql);
        System.out.println("\n" + sql + "\n" + "column reference \"a\" is ambiguous --- this is wrongly parsed:\n" + re);
    }

    @Test(expected = InvalidSelectQueryException.class)
    public void inner_join_on_inner_join_test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN  R on Q.C =  R.C;");
    }

    @Test
    public void subjoin_test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        RelationalExpression re = parser.parse("SELECT S.A, S.C FROM R JOIN (P NATURAL JOIN Q) AS S ON R.A = S.A");
        System.out.println(re);

        Function atom_R = FACTORY.getFunction(
                FACTORY.getPredicate("R", new Predicate.COL_TYPE[]{null, null, null, null}),
                ImmutableList.of(FACTORY.getVariable("A1"), FACTORY.getVariable("B1"),
                        FACTORY.getVariable("C1"), FACTORY.getVariable("D1")));

        Function atom_P = FACTORY.getFunction(
                FACTORY.getPredicate("P", new Predicate.COL_TYPE[]{null, null}),
                ImmutableList.of(FACTORY.getVariable("A2"), FACTORY.getVariable("B2")));

        Function atom_Q = FACTORY.getFunction(
                FACTORY.getPredicate("Q", new Predicate.COL_TYPE[]{null, null}),
                ImmutableList.of(FACTORY.getVariable("A3"), FACTORY.getVariable("C3")));

        Function atom_EQ1 = FACTORY.getFunction(ExpressionOperation.EQ,
                ImmutableList.of(FACTORY.getVariable("A1"), FACTORY.getVariable("A2")));

        Function atom_EQ2 = FACTORY.getFunction(ExpressionOperation.EQ,
                ImmutableList.of(FACTORY.getVariable("A2"),  FACTORY.getVariable("A3")));

        assertEquals(2, re.getFilterAtoms().size());
        assertTrue(re.getFilterAtoms().contains(atom_EQ1));
        assertTrue(re.getFilterAtoms().contains(atom_EQ2));

        assertEquals(3, re.getDataAtoms().size());
        assertTrue(re.getDataAtoms().contains(atom_R));
        assertTrue(re.getDataAtoms().contains(atom_P));
        assertTrue(re.getDataAtoms().contains(atom_Q));
    }

    // -----------------------------------------------------
    // NEW TESTS

    @Test()
    public void simple_join_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT * FROM P, Q;");
        assert_join_common(re);
        assertEquals(0, re.getFilterAtoms().size());
    }

    @Test()
    public void natural_join_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT A FROM P NATURAL JOIN  Q;");
        System.out.println(re);
        assert_join_common(re);

        assert_contains_EQ_atom(re);
    }

    @Test()
    public void cross_join_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT * FROM P CROSS JOIN  Q;");
        assert_join_common(re);
        assertEquals(0, re.getFilterAtoms().size());
    }

    @Test()
    public void join_on_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT * FROM P JOIN  Q ON P.A = Q.A;");
        assert_join_common(re);
        assert_contains_EQ_atom(re);
    }

    @Test()
    public void inner_join_on_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT * FROM P INNER JOIN  Q ON P.A = Q.A;");
        assert_join_common(re);
        assert_contains_EQ_atom(re);
    }

    @Test()
    public void join_using_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT * FROM P JOIN  Q USING(A);");
        assert_join_common(re);
        assert_contains_EQ_atom(re);
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_no_from_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        parser.parse("SELECT 1");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_one_complex_expression_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        parser.parse("SELECT 1 FROM Q");
    }


    @Test(expected = InvalidSelectQueryException.class)
    public void select_one_complex_expression_test2() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        parser.parse("SELECT R FROM Q");
    }

    @Test()
    public void inner_join_using_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT * FROM P INNER JOIN  Q USING(A);");
        assert_join_common(re);
        assert_contains_EQ_atom(re);
    }

    private void assert_join_common(RelationalExpression re) {

        assertEquals(2, re.getDataAtoms().size());

        Function atomP = FACTORY.getFunction(
                FACTORY.getPredicate("P", new Predicate.COL_TYPE[]{null, null}),
                ImmutableList.of(FACTORY.getVariable("A1"), FACTORY.getVariable("B1")));

        assertTrue(re.getDataAtoms().contains(atomP));

        Function atomQ = FACTORY.getFunction(
                FACTORY.getPredicate("Q", new Predicate.COL_TYPE[]{null, null}),
                ImmutableList.of(FACTORY.getVariable("A2"), FACTORY.getVariable("C2")));

        assertTrue(re.getDataAtoms().contains(atomQ));

        //assertEquals(0, parse.getHead().getTerms().size());
        // assertEquals(4, parse.getReferencedVariables().size());
    }

    private void assert_contains_EQ_atom(RelationalExpression re) {

        assertEquals(1, re.getFilterAtoms().size());

        Function atomEQ = FACTORY.getFunction(ExpressionOperation.EQ,
                ImmutableList.of(FACTORY.getVariable("A1"), FACTORY.getVariable("A2")));
        assertTrue(re.getFilterAtoms().contains(atomEQ));
    }
    //end region



    @Test()
    public void parse_exception_test() {
        // During this tests the parser throws a ParseException
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
            SelectQueryParser parser = new SelectQueryParser(createMetadata());
            Exception e = null;
            try {
                parser.parse(query);
                System.out.println(query + " - Wrong!");
            }
            catch (UnsupportedSelectQueryException ex) {
                System.out.println(query + " - OK");
                e = ex;
            }
            assertNotNull(e);
        });
    }

    @Test()
    public void invalid_joins_test() {
        ImmutableList.of(
                "SELECT * FROM P JOIN  Q;",
                "SELECT * FROM P INNER JOIN  Q;",
                "SELECT * FROM P NATURAL JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P CROSS JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P NATURAL JOIN  Q USING(A);",
                "SELECT * FROM P CROSS JOIN  Q USING(A);"
        )
                .forEach(query -> {
                    SelectQueryParser parser = new SelectQueryParser(createMetadata());
                    Exception e = null;
                    try {
                        parser.parse(query);
                        System.out.println(query + " - Wrong!");
                    }
                    catch (InvalidSelectQueryException ex) {
                        System.out.println(query + " - OK");
                        e = ex;
                    }
                    assertNotNull(e);
                });
    }

    @Test()
    public void unsupported_joins_test() {
        ImmutableList.of(
                "SELECT * FROM P RIGHT OUTER JOIN  Q;",
                "SELECT * FROM P RIGHT JOIN  Q;",
                "SELECT * FROM P FULL JOIN  Q;",
                "SELECT * FROM P LEFT JOIN  Q;",
                "SELECT * FROM P FULL OUTER JOIN  Q;",
                "SELECT * FROM P LEFT OUTER JOIN  Q;",
                "SELECT * FROM P RIGHT JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P FULL JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P LEFT JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P RIGHT OUTER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P FULL OUTER JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P LEFT OUTER JOIN  Q ON P.A = Q.A;")
                .forEach(query -> {
                    SelectQueryParser parser = new SelectQueryParser(createMetadata());
                    Exception e = null;
                    try {
                        parser.parse(query);
                        System.out.println(query + " - Wrong!");
                    }
                    catch (UnsupportedSelectQueryException ex) {
                        System.out.println(query + " - OK");
                        e = ex;
                    }
                    assertNotNull(e);
                });
    }

    @Test
    public void join_using_2_test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);

        RelationalExpression re = parser.parse("SELECT A, B FROM P INNER JOIN R USING (A,B)");
        System.out.println(re);

        //assertEquals(2, parse.getHead().getTerms().size());
        //assertEquals(6, parse.getReferencedVariables().size());

        assertEquals(2, re.getDataAtoms().size());
        // TODO: add data atoms asserts

        Function atomQ_A = FACTORY.getFunction(ExpressionOperation.EQ,
                ImmutableList.of(FACTORY.getVariable("A1"), FACTORY.getVariable("A2")));

        Function atomQ_B = FACTORY.getFunction(ExpressionOperation.EQ,
                ImmutableList.of(FACTORY.getVariable("B1"), FACTORY.getVariable("B2")));

        assertEquals(2, re.getFilterAtoms().size());
        assertTrue(re.getFilterAtoms().contains(atomQ_A));
        assertTrue(re.getFilterAtoms().contains(atomQ_B));
    }

    @Test
    public void select_join_2_test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        RelationalExpression re = parser.parse("SELECT a.A, b.B FROM P AS a JOIN R AS b  ON (a.A = b.B);");

        // TODO: add proper asserts
        //assertNotNull(parse);
    }


    @Test
    public void parser_combination_query_test() {
        int i = 0;
        try {
            String query = "SELECT * FROM P, Q;";
            System.out.print("" + i + ": " + query);
            Statement statement = CCJSqlParserUtil.parse(query);
            System.out.println(" OK");
        }
        catch (Exception e) {
            System.out.println(" " + e.getClass().getCanonicalName() + "  occurred");
        }

        for (String c : new String[]{"", " ON P.A = Q.A", " USING (A)"}) {
            for (String b : new String[]{"", " OUTER", " INNER"}) {
                for (String a : new String[]{"", " RIGHT", " NATURAL", " FULL", " LEFT", " CROSS"}) {
                    i++;
                    try {
                        String query = "SELECT * FROM P" + a + b + " JOIN Q" + c + ";";
                        System.out.print("" + i + ": " + query);
                        Statement statement = CCJSqlParserUtil.parse(query);
                        System.out.println(" OK");
                    }
                    catch (Exception e) {
                        System.out.println(" " + e.getClass().getCanonicalName() + "  occurred");
                    }
                }
            }
        }
    }


    // SUB SELECT TESTS
    @Test
    public void sub_select_one_test(){
        String  query = "SELECT * FROM (SELECT * FROM P ) AS S;";
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse(query);
        System.out.print(re);

        assertEquals(0, re.getFilterAtoms().size());

        Function atomP = FACTORY.getFunction(
                FACTORY.getPredicate("P", new Predicate.COL_TYPE[]{null, null}),
                ImmutableList.of(FACTORY.getVariable("A1"), FACTORY.getVariable("B1")));

        assertEquals(1, re.getDataAtoms().size());
        assertTrue(re.getDataAtoms().contains(atomP));
    }

    @Test
    public void sub_select_two_test(){
        String  query = "SELECT * FROM (SELECT * FROM (SELECT * FROM P ) AS T ) AS S;";
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse(query);
        System.out.print(re);

        assertEquals(0, re.getFilterAtoms().size());

        Function atomP = FACTORY.getFunction(
                FACTORY.getPredicate("P", new Predicate.COL_TYPE[]{null, null}),
                ImmutableList.of(FACTORY.getVariable("A1"), FACTORY.getVariable("B1")));

        assertEquals(1, re.getDataAtoms().size());
        assertTrue(re.getDataAtoms().contains(atomP));
    }

    @Test
    public void sub_select_one_simple_join_internal_test(){
        String  query = "SELECT * FROM (SELECT * FROM P, Q) AS S;";
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse(query);
        System.out.print(re);

        assert_simple_join(re);
    }


    @Test
    public void sub_select_one_simple_join_test(){
        String  query = "SELECT * FROM (SELECT * FROM P) AS S, Q ;";
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse(query);
        System.out.print(re);

        assert_simple_join(re);
    }

    private void assert_simple_join(RelationalExpression re){

        Function atomP = FACTORY.getFunction(
                FACTORY.getPredicate("P", new Predicate.COL_TYPE[]{null, null}),
                ImmutableList.of(FACTORY.getVariable("A1"), FACTORY.getVariable("B1")));

        Function atomQ = FACTORY.getFunction(
                FACTORY.getPredicate("Q", new Predicate.COL_TYPE[]{null, null}),
                ImmutableList.of(FACTORY.getVariable("A2"), FACTORY.getVariable("C2")));

        assertEquals(2, re.getDataAtoms().size());
        assertTrue(re.getDataAtoms().contains(atomP));
        assertTrue(re.getDataAtoms().contains(atomQ));
    }

    // END SUB SELECT TESTS

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
