package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.UnsupportedSelectQueryException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Test;

import java.util.List;

import static it.unibz.inf.ontop.utils.SQLMappingTestingTools.*;
import static org.junit.Assert.*;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 * -
 */
public class SelectQueryParserTest {

    private DatabaseRelationDefinition TABLE_P, TABLE_Q, TABLE_R;

    private static final String A1 = "A1";
    private static final String A2 = "A2";
    private static final String A3 = "A3";
    private static final String B1 = "B1";
    private static final String B2 = "B2";
    private static final String C1 = "C1";
    private static final String C2 = "C2";
    private static final String C3 = "C3";
    private static final String D1 = "D1";

    private RAExpression parse(String sql) throws JSQLParserException, InvalidSelectQueryException, UnsupportedSelectQueryException {
        SelectQueryParser parser = createParser();
        return parser.parse(JSqlParserTools.parse(sql));
    }

    @Test
    public void inner_join_on_same_table_test() throws Exception {
        RAExpression re = parse("SELECT p1.A, p2.B FROM P p1 INNER JOIN  P p2 on p1.A = p2.A ");
        System.out.println(re);

//        assertEquals(2, parse.getHead().getTerms().size());
//        assertEquals(4, parse.getReferencedVariables().size());

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_P, A2, B2)), re.getDataAtoms());
    }


    @Test(expected = InvalidSelectQueryException.class)
    public void inner_join_on_inner_join_ambiguity_test() throws Exception {
        // common column name "A" appears more than once in left table
        parse("SELECT A, C FROM P INNER JOIN  Q on P.A =  Q.A NATURAL JOIN  R ");
    }


    @Test(expected = InvalidSelectQueryException.class)
    public void inner_join_on_inner_join_ambiguity2_test() throws Exception {
        // column reference "a" is ambiguous
        RAExpression re = parse("SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN  R on Q.C =  R.C;");
    }

    @Test(expected = InvalidSelectQueryException.class)
    public void inner_join_on_inner_join_test() throws Exception {
        // common column name "A" appears more than once in left table
        parse("SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN  R on Q.C =  R.C;");
    }

    @Test
    public void subjoin_test() throws Exception {
        RAExpression re = parse("SELECT S.A, S.C FROM R JOIN (P NATURAL JOIN Q) AS S ON R.A = S.A");
        System.out.println(re);

        assertEquals(ImmutableList.of(eqOf(A2, A3), eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_R, A1, B1, C1, D1),
                dataAtomOf(TABLE_P, A2, B2), dataAtomOf(TABLE_Q, A3, C3)), re.getDataAtoms());
    }

    // -----------------------------------------------------
    // NEW TESTS

    @Test
    public void simple_join_test() throws Exception {
        RAExpression re = parse("SELECT * FROM P, Q;");

        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void natural_join_test() throws Exception {
        RAExpression re = parse("SELECT A FROM P NATURAL JOIN  Q;");
        System.out.println(re);

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void cross_join_test() throws Exception {
        RAExpression re = parse("SELECT * FROM P CROSS JOIN  Q;");

        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void join_on_test() throws Exception {
        RAExpression re = parse("SELECT * FROM P JOIN  Q ON P.A = Q.A;");

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void inner_join_on_test() throws Exception {
        RAExpression re = parse("SELECT * FROM P INNER JOIN  Q ON P.A = Q.A;");

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void join_using_test() throws Exception {
        RAExpression re = parse("SELECT * FROM P JOIN  Q USING(A);");

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_no_from_test() throws Exception {
        parse("SELECT 1");
    }

    // TODO: extend
    @Test//(expected = InvalidSelectQueryException.class)
    public void select_one_complex_expression_test() throws Exception {
        parse("SELECT 1 FROM Q");
    }


    @Test(expected = InvalidSelectQueryException.class)
    public void select_one_complex_expression_test2() throws Exception {
        parse("SELECT R FROM Q");
    }

    @Test
    public void inner_join_using_test() throws Exception {
        RAExpression re = parse("SELECT * FROM P INNER JOIN  Q USING(A);");

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    //end region



    @Test
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
            try {
                parse(query);
                System.out.println(query + " - Wrong!");
                fail();
            }
            catch (UnsupportedSelectQueryException | InvalidSelectQueryException | JSQLParserException ex) {
                System.out.println(query + " - OK");
            }
        });
    }

    @Test
    public void invalid_joins_test() {
        ImmutableList.of(
                "SELECT * FROM P JOIN  Q;",
                "SELECT * FROM P INNER JOIN  Q;",
                "SELECT * FROM P NATURAL JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P CROSS JOIN  Q ON P.A = Q.A;",
                "SELECT * FROM P NATURAL JOIN  Q USING(A);",
                "SELECT * FROM P CROSS JOIN  Q USING(A);"
        ).forEach(query -> {
            Exception e = null;
            try {
                parse(query);
                System.out.println(query + " - Wrong!");
            }
            catch (InvalidSelectQueryException ex) {
                System.out.println(query + " - OK");
                e = ex;
            }
            catch (Exception ex) {

            }
            assertNotNull(e);
        });
    }

    @Test
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
                "SELECT * FROM P LEFT OUTER JOIN  Q ON P.A = Q.A;"
        ).forEach(query -> {
            Exception e = null;
            try {
                parse(query);
                System.out.println(query + " - Wrong!");
            }
            catch (UnsupportedSelectQueryException ex) {
                System.out.println(query + " - OK");
                e = ex;
            }
            catch (InvalidSelectQueryException e1) {
                e1.printStackTrace();
            }
            catch (JSQLParserException e2) {
                e2.printStackTrace();
            }
            assertNotNull(e);
        });
    }

    @Test
    public void join_using_2_test() throws Exception {
        RAExpression re = parse("SELECT A, B FROM P INNER JOIN R USING (A,B)");
        System.out.println(re);

        assertEquals(2, re.getDataAtoms().size());
        // TODO: add data atoms asserts

        assertEquals(ImmutableList.of(eqOf(A1, A2), eqOf(B1, B2)), re.getFilterAtoms());
    }

    @Test
    public void select_join_2_test() throws Exception {
        // common column name "A" appears more than once in left table
        RAExpression re = parse("SELECT a.A, b.B FROM P AS a JOIN R AS b  ON (a.A = b.B);");

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
    public void sub_select_one_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM P) AS S;";
        RAExpression re = parse(query);
        System.out.print(re);

        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1)), re.getDataAtoms());
    }

    @Test
    public void sub_select_two_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM (SELECT * FROM P) AS T) AS S;";
        RAExpression re = parse(query);
        System.out.print(re);

        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1)), re.getDataAtoms());
    }

    @Test
    public void sub_select_one_simple_join_internal_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM P, Q) AS S;";
        RAExpression re = parse(query);
        System.out.print(re);

        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }


    @Test
    public void sub_select_one_simple_join_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM P) AS S, Q;";
        RAExpression re = parse(query);
        System.out.print(re);

        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }


    // END SUB SELECT TESTS

    private ImmutableExpression eqOf(String var1, String var2) {
        return TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable(var1), TERM_FACTORY.getVariable(var2));
    }

    private ExtensionalDataNode dataAtomOf(RelationDefinition table, String var1, String var2) {
        return IQ_FACTORY.createExtensionalDataNode(table,
                ImmutableMap.of(0, TERM_FACTORY.getVariable(var1), 1, TERM_FACTORY.getVariable(var2)));
    }

    private ExtensionalDataNode dataAtomOf(RelationDefinition table, String var1, String var2, String var3, String var4) {
        return IQ_FACTORY.createExtensionalDataNode(table,
                ImmutableMap.of(0, TERM_FACTORY.getVariable(var1), 1, TERM_FACTORY.getVariable(var2), 2, TERM_FACTORY.getVariable(var3), 3, TERM_FACTORY.getVariable(var4)));
    }

    private static void assertMatches(ImmutableList<ExtensionalDataNode> list0, List<ExtensionalDataNode> list) {
        assertEquals(list0.size(), list.size());
        list0.forEach(a -> assertTrue(list.stream()
                .anyMatch(b -> b.isSyntacticallyEquivalentTo(a))));
    }

    private SelectQueryParser createParser() {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        TABLE_P = builder.createDatabaseRelation("P",
            "A", integerDBType, false,
            "B", integerDBType, false);

        TABLE_Q = builder.createDatabaseRelation("Q",
            "A", integerDBType, false,
            "C", integerDBType, false);

        TABLE_R = builder.createDatabaseRelation("R",
            "A", integerDBType, false,
            "B", integerDBType, false,
            "C", integerDBType, false,
            "D", integerDBType, false);

        MetadataLookup metadataLookup = builder.build();
        return new SelectQueryParser(metadataLookup, CORE_SINGLETONS);
    }
}
