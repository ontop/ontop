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

import java.util.List;

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
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT p1.A, p2.B FROM P p1 INNER JOIN  P p2 on p1.A = p2.A ");
        System.out.println(re);

//        assertEquals(2, parse.getHead().getTerms().size());
//        assertEquals(4, parse.getReferencedVariables().size());

        assertMatches(createEQ("A1", "A2"), re.getFilterAtoms());

        assertMatches(createDataAtom("P", "A1", "B1"),
                createDataAtom("P", "A2", "B2"), re.getDataAtoms());
    }


    @Test(expected = InvalidSelectQueryException.class)
    public void inner_join_on_inner_join_ambiguity_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A =  Q.A NATURAL JOIN  R ");
    }


    @Test(expected = InvalidSelectQueryException.class)
    public void inner_join_on_inner_join_ambiguity2_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        // column reference "a" is ambiguous
        String sql = "SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN  R on Q.C =  R.C;";
        RelationalExpression re = parser.parse(sql);
        System.out.println("\n" + sql + "\n" + "column reference \"a\" is ambiguous --- this is wrongly parsed:\n" + re);
    }

    @Test(expected = InvalidSelectQueryException.class)
    public void inner_join_on_inner_join_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN  R on Q.C =  R.C;");
    }

    @Test
    public void subjoin_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT S.A, S.C FROM R JOIN (P NATURAL JOIN Q) AS S ON R.A = S.A");
        System.out.println(re);

        assertMatches(createEQ("A1", "A2"), createEQ("A2","A3"), re.getFilterAtoms());

        assertMatches(createDataAtom("R", "A1", "B1", "C1", "D1"),
                createDataAtom("P", "A2", "B2"),
                createDataAtom("Q","A3", "C3"),
                re.getDataAtoms());
    }

    // -----------------------------------------------------
    // NEW TESTS

    @Test()
    public void simple_join_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT * FROM P, Q;");

        assertMatches(createDataAtom("P", "A1", "B1"),
                createDataAtom("Q","A2", "C2"), re.getDataAtoms());

        assertEquals(0, re.getFilterAtoms().size());
    }

    @Test()
    public void natural_join_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT A FROM P NATURAL JOIN  Q;");
        System.out.println(re);

        assertMatches(createDataAtom("P", "A1", "B1"),
                createDataAtom("Q","A2", "C2"), re.getDataAtoms());

        assertMatches(createEQ("A1", "A2"), re.getFilterAtoms());
    }

    @Test()
    public void cross_join_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT * FROM P CROSS JOIN  Q;");

        assertMatches(createDataAtom("P", "A1", "B1"),
                createDataAtom("Q","A2", "C2"), re.getDataAtoms());

        assertEquals(0, re.getFilterAtoms().size());
    }

    @Test()
    public void join_on_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT * FROM P JOIN  Q ON P.A = Q.A;");

        assertMatches(createDataAtom("P", "A1", "B1"),
                createDataAtom("Q","A2", "C2"), re.getDataAtoms());

        assertMatches(createEQ("A1", "A2"), re.getFilterAtoms());
    }

    @Test()
    public void inner_join_on_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT * FROM P INNER JOIN  Q ON P.A = Q.A;");

        assertMatches(createDataAtom("P", "A1", "B1"),
                createDataAtom("Q","A2", "C2"), re.getDataAtoms());

        assertMatches(createEQ("A1", "A2"), re.getFilterAtoms());
    }

    @Test()
    public void join_using_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse("SELECT * FROM P JOIN  Q USING(A);");

        assertMatches(createDataAtom("P", "A1", "B1"),
                createDataAtom("Q","A2", "C2"), re.getDataAtoms());

        assertMatches(createEQ("A1", "A2"), re.getFilterAtoms());
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

        assertMatches(createDataAtom("P", "A1", "B1"),
                createDataAtom("Q","A2", "C2"), re.getDataAtoms());

        assertMatches(createEQ("A1", "A2"), re.getFilterAtoms());
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
        SelectQueryParser parser = new SelectQueryParser(createMetadata());

        RelationalExpression re = parser.parse("SELECT A, B FROM P INNER JOIN R USING (A,B)");
        System.out.println(re);

        //assertEquals(2, parse.getHead().getTerms().size());
        //assertEquals(6, parse.getReferencedVariables().size());

        assertEquals(2, re.getDataAtoms().size());
        // TODO: add data atoms asserts

        assertMatches(createEQ("A1", "A2"), createEQ("B1", "B2"), re.getFilterAtoms());
    }

    @Test
    public void select_join_2_test() {
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
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

        assertMatches(createDataAtom("P", "A1", "B1"), re.getDataAtoms());
    }

    @Test
    public void sub_select_two_test(){
        String  query = "SELECT * FROM (SELECT * FROM (SELECT * FROM P ) AS T ) AS S;";
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse(query);
        System.out.print(re);

        assertEquals(0, re.getFilterAtoms().size());

        assertMatches(createDataAtom("P","A1", "B1"), re.getDataAtoms());
    }

    @Test
    public void sub_select_one_simple_join_internal_test(){
        String  query = "SELECT * FROM (SELECT * FROM P, Q) AS S;";
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse(query);
        System.out.print(re);

        assertMatches(createDataAtom("P","A1","B1"),
                createDataAtom("Q","A2", "C2"), re.getDataAtoms());

        assertEquals(0, re.getFilterAtoms().size());
    }


    @Test
    public void sub_select_one_simple_join_test(){
        String  query = "SELECT * FROM (SELECT * FROM P) AS S, Q ;";
        SelectQueryParser parser = new SelectQueryParser(createMetadata());
        RelationalExpression re = parser.parse(query);
        System.out.print(re);

        assertMatches(createDataAtom("P","A1","B1"),
                createDataAtom("Q","A2", "C2"), re.getDataAtoms());

        assertEquals(0, re.getFilterAtoms().size());
    }


    // END SUB SELECT TESTS

    private Function createEQ(String var1, String var2) {
        return FACTORY.getFunction(ExpressionOperation.EQ,
                ImmutableList.of(FACTORY.getVariable(var1), FACTORY.getVariable(var2)));
    }

    private Function createDataAtom(String predicate, String var1, String var2) {
        return FACTORY.getFunction(FACTORY.getPredicate(predicate, new Predicate.COL_TYPE[]{ null, null }),
                ImmutableList.of(FACTORY.getVariable(var1), FACTORY.getVariable(var2)));
    }

    private Function createDataAtom(String predicate, String var1, String var2, String var3, String var4) {
        return FACTORY.getFunction(FACTORY.getPredicate(predicate, new Predicate.COL_TYPE[]{ null, null, null, null }),
                ImmutableList.of(FACTORY.getVariable(var1), FACTORY.getVariable(var2), FACTORY.getVariable(var3), FACTORY.getVariable(var4)));
    }

    private void assertMatches(Function f, List<Function> list) {
        assertEquals(1, list.size());
        assertTrue(list.contains(f));
    }

    private void assertMatches(Function f1, Function f2, List<Function> list) {
        assertEquals(2, list.size());
        assertTrue(list.contains(f1));
        assertTrue(list.contains(f2));
    }

    private void assertMatches(Function f1, Function f2, Function f3, List<Function> list) {
        assertEquals(3, list.size());
        assertTrue(list.contains(f1));
        assertTrue(list.contains(f2));
        assertTrue(list.contains(f3));
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
