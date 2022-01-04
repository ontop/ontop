package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DatabaseTableDefinition;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.Test;

import java.util.List;

import static it.unibz.inf.ontop.spec.sqlparser.SQLTestingTools.*;
import static org.junit.Assert.*;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 * -
 */
public class SelectQueryParserTest {

    private NamedRelationDefinition TABLE_P, TABLE_Q, TABLE_R, TABLE_SP, TABLE_SQ;
    private DBTermType integerDBType;
    private QuotedIDFactory idfac;

    private static final String A1 = "A1";
    private static final String A2 = "A2";
    private static final String A3 = "A3";
    private static final String B1 = "B1";
    private static final String B2 = "B2";
    private static final String C1 = "C1";
    private static final String C2 = "C2";
    private static final String C3 = "C3";
    private static final String D1 = "D1";
    private static final String D2 = "D2";

    private RAExpression parse(String sql) throws JSQLParserException, InvalidQueryException, UnsupportedSelectQueryException {

        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

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

        idfac = builder.getQuotedIDFactory();
        TABLE_SP = builder.createDatabaseRelation(
                ImmutableList.of(idfac.createRelationID("PP"),
                        idfac.createRelationID("S", "PP")),
                DatabaseTableDefinition.attributeListBuilder()
        .addAttribute(idfac.createAttributeID("A"), integerDBType, false)
        .addAttribute(idfac.createAttributeID("B"), integerDBType, false));

        TABLE_SQ = builder.createDatabaseRelation(
                ImmutableList.of(idfac.createRelationID("QQ"),
                        idfac.createRelationID("S", "QQ")),
                DatabaseTableDefinition.attributeListBuilder()
                        .addAttribute(idfac.createAttributeID("A"), integerDBType, false)
                        .addAttribute(idfac.createAttributeID("C"), integerDBType, false));

        MetadataLookup metadataLookup = builder.build();
        SelectQueryParser parser = new SelectQueryParser(metadataLookup, CORE_SINGLETONS);

        return parser.parse(JSqlParserTools.parse(sql));
    }


    @Test
    public void inner_join_on_same_table_test() throws Exception {
        RAExpression re = parse("SELECT p1.A, p2.B FROM P p1 INNER JOIN P p2 on p1.A = p2.A ");
        System.out.println(re);

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_P, A2, B2)), re.getDataAtoms());
    }


    @Test(expected = InvalidQueryException.class)
    public void inner_join_on_inner_join_ambiguity_test() throws Exception {
        // common column name "A" appears more than once in left table
        parse("SELECT A, C FROM P INNER JOIN Q on P.A =  Q.A NATURAL JOIN R");
    }


    @Test(expected = InvalidQueryException.class)
    public void inner_join_on_inner_join_ambiguity2_test() throws Exception {
        // column reference "a" is ambiguous
        RAExpression re = parse("SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN R on Q.C =  R.C");
    }

    @Test(expected = InvalidQueryException.class)
    public void inner_join_on_inner_join_test() throws Exception {
        // common column name "A" appears more than once in left table
        parse("SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN R on Q.C =  R.C");
    }

    @Test
    public void subjoin_test() throws Exception {
        RAExpression re = parse("SELECT S.A, S.C FROM R JOIN (P NATURAL JOIN Q) AS S ON R.A = S.A");
        System.out.println(re);

        assertEquals(ImmutableList.of(eqOf(A2, A3), eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_R, A1, B1, C1, D1),
                dataAtomOf(TABLE_P, A2, B2), dataAtomOf(TABLE_Q, A3, C3)), re.getDataAtoms());
    }

    @Test
    public void select_one_no_from() throws Exception {
        RAExpression re = parse("SELECT 1");

        assertEquals(ImmutableMap.of(), re.getAttributes().asMap());
        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(), re.getDataAtoms());
    }

    @Test
    public void select_one_no_from_alias() throws Exception {
        RAExpression re = parse("SELECT 1 AS A");

        assertEquals(ImmutableMap.of(new QualifiedAttributeID(null, idfac.createAttributeID("A")), TERM_FACTORY.getDBConstant("1", integerDBType)), re.getAttributes().asMap());
        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(), re.getDataAtoms());
    }

    @Test
    public void select_one_from() throws Exception {
        RAExpression re = parse("SELECT 1 FROM Q");
        assertEquals(ImmutableMap.of(), re.getAttributes().asMap());
        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_Q, A1, C1)), re.getDataAtoms());
    }

    @Test
    public void select_one_from_alias() throws Exception {
        RAExpression re = parse("SELECT 1 AS A FROM Q");

        assertEquals(ImmutableMap.of(new QualifiedAttributeID(null, idfac.createAttributeID("A")), TERM_FACTORY.getDBConstant("1", integerDBType)), re.getAttributes().asMap());
        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_Q, A1, C1)), re.getDataAtoms());
    }

    @Test(expected = InvalidQueryException.class)
    public void select_missing_column_test2() throws Exception {
        parse("SELECT R FROM Q");
    }

    @Test
    public void select_natural_join_schema() throws Exception {
        RAExpression re = parse("SELECT A FROM S.PP NATURAL JOIN S.QQ");
        System.out.println(re);

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_SP, A1, B1), dataAtomOf(TABLE_SQ, A2, C2)), re.getDataAtoms());
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_apply() throws Exception {
        RAExpression re = parse("SELECT A FROM P APPLY Q");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_cross_apply() throws Exception {
        RAExpression re = parse("SELECT A FROM P CROSS APPLY Q");
    }

    @Test(expected = JSQLParserException.class) // is valid in MS SQL Server
    public void select_outer_apply() throws Exception {
        RAExpression re = parse("SELECT A FROM P OUTER APPLY Q");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_straight() throws Exception {
        RAExpression re = parse("SELECT A FROM P STRAIGHT_JOIN Q");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_straight_on() throws Exception {
        RAExpression re = parse("SELECT A FROM P STRAIGHT_JOIN Q ON (P.A = Q.A)");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_straight_using() throws Exception {
        RAExpression re = parse("SELECT A FROM P STRAIGHT_JOIN Q USING (A)");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_within() throws Exception {
        RAExpression re = parse("SELECT A FROM P INNER JOIN Q WITHIN (1 HOURS) ON P.A = Q.A");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_oracle_hint() throws Exception {
        RAExpression re = parse("SELECT /*+ value  */ A FROM P");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_skip() throws Exception {
        RAExpression re = parse("SELECT SKIP 1 A FROM P");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_first() throws Exception {
        RAExpression re = parse("SELECT FIRST 10 A FROM P");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_top() throws Exception {
        RAExpression re = parse("SELECT TOP 10 A FROM P");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_sql_no_cache() throws Exception {
        RAExpression re = parse("SELECT SQL_NO_CACHE A FROM P");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_sql_cal_found_rows() throws Exception {
        RAExpression re = parse("SELECT SQL_CALC_FOUND_ROWS A FROM P");
    }

    // JSQLParser apparently allows more weird combinations like this
    @Test(expected = InvalidQueryException.class)
    public void select_left_simple() throws Exception {
        RAExpression re = parse("SELECT * FROM P LEFT, Q");
        System.out.println(re);
    }

    @Test(expected = InvalidQueryException.class)
    public void select_simple_on() throws Exception {
        RAExpression re = parse("SELECT * FROM P, Q ON P.A = Q.A");
        System.out.println(re);
    }

    @Test(expected = InvalidQueryException.class)
    public void select_simple_using() throws Exception {
        RAExpression re = parse("SELECT * FROM P, Q USING (A)");
        System.out.println(re);
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_left_semi_join_on() throws Exception {
        RAExpression re = parse("SELECT * FROM P LEFT SEMI JOIN Q ON P.A = Q.A");
        System.out.println(re);
    }

    @Test(expected = InvalidQueryException.class)
    public void select_left_semi_join_using() throws Exception {
        RAExpression re = parse("SELECT * FROM P LEFT SEMI JOIN Q USING (A)");
        System.out.println(re);
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_column_alias() throws Exception {
        RAExpression re = parse("SELECT * FROM P AS PP(AA, BB)");
        System.out.println(re);
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_from_values() throws Exception {
        RAExpression re = parse("SELECT * FROM (VALUES(1,2,3)) QQ(A,B,C)");
        System.out.println(re);
    }


    // -----------------------------------------------------
    // NEW TESTS

    @Test
    public void select_simple_join() throws Exception {
        RAExpression re = parse("SELECT * FROM P, Q");

        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_simple_outer_join() throws Exception {
        // special case in JSQLParser - no clue what it may even mean
        RAExpression re = parse("SELECT * FROM P, OUTER Q");
    }

    // ----------------------------------------------------------
    // valid combinations - basic tests

    @Test
    public void select_natural_join() throws Exception {
        RAExpression re = parse("SELECT A FROM P NATURAL JOIN Q");
        System.out.println(re);

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void select_cross_join() throws Exception {
        RAExpression re = parse("SELECT * FROM P CROSS JOIN Q");
        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void select_join_on() throws Exception {
        RAExpression re = parse("SELECT * FROM P JOIN Q ON P.A = Q.A");

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void select_inner_join_on() throws Exception {
        RAExpression re = parse("SELECT * FROM P INNER JOIN Q ON P.A = Q.A");

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void select_join_using() throws Exception {
        RAExpression re = parse("SELECT * FROM P JOIN Q USING(A)");

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void select_inner_join_using() throws Exception {
        RAExpression re = parse("SELECT * FROM P INNER JOIN Q USING(A)");

        assertEquals(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }

    // -----------------------------------------------
    // invalid combinations for JSQLParser

    @Test(expected = JSQLParserException.class)
    public void select_outer_join() throws Exception {
        parse("SELECT * FROM P OUTER JOIN Q");
    }

    @Test(expected = JSQLParserException.class)
    public void select_natural_outer_join() throws Exception {
        parse("SELECT * FROM P NATURAL OUTER JOIN Q");
    }

    @Test(expected = JSQLParserException.class)
    public void select_cross_outer_join() throws Exception {
        parse("SELECT * FROM P CROSS OUTER JOIN Q");
    }

    @Test(expected = JSQLParserException.class)
    public void select_natural_inner_join() throws Exception {
        parse("SELECT * FROM P NATURAL INNER JOIN Q");
    }

    @Test(expected = JSQLParserException.class)
    public void select_cross_inner_join() throws Exception {
        parse("SELECT * FROM P CROSS INNER JOIN Q");
    }

    @Test(expected = JSQLParserException.class)
    public void select_right_inner_join() throws Exception {
        parse("SELECT * FROM P RIGHT INNER JOIN Q");
    }

    @Test(expected = JSQLParserException.class)
    public void select_full_inner_join() throws Exception {
        parse("SELECT * FROM P FULL INNER JOIN Q");
    }

    @Test(expected = JSQLParserException.class)
    public void select_left_inner_join() throws Exception {
        parse("SELECT * FROM P LEFT INNER JOIN Q");
    }

    @Test(expected = JSQLParserException.class)
    public void select_outer_join_on() throws Exception {
        parse("SELECT * FROM P OUTER JOIN Q ON P.A = Q.A");
    }

    @Test(expected = JSQLParserException.class)
    public void select_natural_outer_join_on() throws Exception {
        parse("SELECT * FROM P NATURAL OUTER JOIN Q ON P.A = Q.A");
    }

    @Test(expected = JSQLParserException.class)
    public void select_cross_outer_join_on() throws Exception {
        parse("SELECT * FROM P CROSS OUTER JOIN Q ON P.A = Q.A");
    }

    @Test(expected = JSQLParserException.class)
    public void select_natural_inner_join_on() throws Exception {
        parse("SELECT * FROM P NATURAL INNER JOIN Q ON P.A = Q.A");
    }

    @Test(expected = JSQLParserException.class)
    public void select_cross_inner_join_on() throws Exception {
        parse("SELECT * FROM P CROSS INNER JOIN Q ON P.A = Q.A");
    }

    @Test(expected = JSQLParserException.class)
    public void select_right_inner_join_on() throws Exception {
        parse("SELECT * FROM P RIGHT INNER JOIN Q ON P.A = Q.A");
    }

    @Test(expected = JSQLParserException.class)
    public void select_full_inner_join_on() throws Exception {
        parse("SELECT * FROM P FULL INNER JOIN Q ON P.A = Q.A");
    }

    @Test(expected = JSQLParserException.class)
    public void select_left_inner_join_on() throws Exception {
        parse("SELECT * FROM P LEFT INNER JOIN Q ON P.A = Q.A");
    }

    @Test(expected = JSQLParserException.class)
    public void select_outer_join_using() throws Exception {
        parse("SELECT * FROM P OUTER JOIN Q USING(A)");
    }

    @Test(expected = JSQLParserException.class)
    public void select_natural_outer_join_using() throws Exception {
        parse("SELECT * FROM P NATURAL OUTER JOIN Q USING(A)");
    }

    @Test(expected = JSQLParserException.class)
    public void select_cross_outer_join_using() throws Exception {
        parse("SELECT * FROM P CROSS OUTER JOIN Q USING(A)");
    }

    @Test(expected = JSQLParserException.class)
    public void select_natural_inner_join_using() throws Exception {
        parse("SELECT * FROM P NATURAL INNER JOIN Q USING(A)");
    }

    @Test(expected = JSQLParserException.class)
    public void select_cross_inner_join_using() throws Exception {
        parse("SELECT * FROM P CROSS INNER JOIN Q USING(A)");
    }

    @Test(expected = JSQLParserException.class)
    public void select_right_inner_join_using() throws Exception {
        parse("SELECT * FROM P RIGHT INNER JOIN Q USING(A)");
    }

    @Test(expected = JSQLParserException.class)
    public void select_full_inner_join_using() throws Exception {
        parse("SELECT * FROM P FULL INNER JOIN Q USING(A)");
    }

    @Test(expected = JSQLParserException.class)
    public void select_left_inner_join_using() throws Exception {
        parse("SELECT * FROM P LEFT INNER JOIN Q USING(A)");
    }


    // -------------------------------------------------------
    // invalid combinations of join modifiers (see SQL standard)

    @Test(expected = InvalidQueryException.class)
    public void select_join() throws Exception {
        parse("SELECT * FROM P JOIN Q"); // requires on or using
    }

    @Test(expected = InvalidQueryException.class)
    public void select_right_join() throws Exception {
        parse("SELECT * FROM P RIGHT JOIN Q");
    }

    @Test(expected = InvalidQueryException.class)
    public void select_full_join() throws Exception {
        parse("SELECT * FROM P FULL JOIN Q");
    }

    @Test(expected = InvalidQueryException.class)
    public void select_left_join() throws Exception {
        parse("SELECT * FROM P LEFT JOIN Q");
    }

    @Test(expected = InvalidQueryException.class)
    public void select_right_outer_join() throws Exception {
        parse("SELECT * FROM P RIGHT OUTER JOIN Q");
    }

    @Test(expected = InvalidQueryException.class)
    public void select_full_outer_join() throws Exception {
        parse("SELECT * FROM P FULL OUTER JOIN Q");
    }

    @Test(expected = InvalidQueryException.class)
    public void select_left_outer_join() throws Exception {
        parse("SELECT * FROM P LEFT OUTER JOIN Q");
    }

    @Test(expected = InvalidQueryException.class)
    public void select_inner_join() throws Exception {
        parse("SELECT * FROM P INNER JOIN Q"); // requires on or using
    }

    @Test(expected = InvalidQueryException.class)
    public void select_natural_join_on() throws Exception {
        parse("SELECT * FROM P NATURAL JOIN Q ON P.A = Q.A");
    }

    @Test(expected = InvalidQueryException.class)
    public void select_cross_join_on() throws Exception {
        parse("SELECT * FROM P CROSS JOIN Q ON P.A = Q.A");
    }

    @Test(expected = InvalidQueryException.class)
    public void select_natural_join_using() throws Exception {
        parse("SELECT * FROM P NATURAL JOIN Q USING(A)");
    }

    @Test(expected = InvalidQueryException.class)
    public void select_cross_join_using() throws Exception {
        parse("SELECT * FROM P CROSS JOIN Q USING(A)");
    }


    // ---------------------------------------------------
    // Unsupported - non-CQ in the mapping

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_right_join_on() throws Exception {
        parse( "SELECT * FROM P RIGHT JOIN Q ON P.A = Q.A");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_full_join_on() throws Exception {
        parse( "SELECT * FROM P FULL JOIN Q ON P.A = Q.A");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_left_join_on() throws Exception {
        parse( "SELECT * FROM P LEFT JOIN Q ON P.A = Q.A");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_right_outer_join_on() throws Exception {
        parse( "SELECT * FROM P RIGHT OUTER JOIN Q ON P.A = Q.A");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_full_outer_join_on() throws Exception {
        parse( "SELECT * FROM P FULL OUTER JOIN Q ON P.A = Q.A");
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_left_outer_join_on() throws Exception {
        parse( "SELECT * FROM P LEFT OUTER JOIN Q ON P.A = Q.A");
    }

    // -------------------------------------------------
    // other features

    @Test
    public void join_using_2_test() throws Exception {
        RAExpression re = parse("SELECT A, B FROM P INNER JOIN R USING (A,B)");
        System.out.println(re);
        assertEquals(ImmutableList.of(eqOf(A1, A2), eqOf(B1, B2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_R, A2, B2, C2, D2)), re.getDataAtoms());
    }

    @Test
    public void select_join_2_test() throws Exception {
        RAExpression re = parse("SELECT a.A, b.B FROM P AS a JOIN R AS b ON (a.A = b.B)");

        assertEquals(ImmutableList.of(eqOf(A1, B2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_R, A2, B2, C2, D2)), re.getDataAtoms());
    }



    // SUB SELECT TESTS
    @Test
    public void sub_select_one_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM P) AS S";
        RAExpression re = parse(query);
        System.out.print(re);

        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1)), re.getDataAtoms());
    }

    @Test
    public void sub_select_two_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM (SELECT * FROM P) AS T) AS S";
        RAExpression re = parse(query);
        System.out.print(re);

        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1)), re.getDataAtoms());
    }

    @Test
    public void sub_select_one_simple_join_internal_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM P, Q) AS S";
        RAExpression re = parse(query);
        System.out.print(re);

        assertEquals(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getDataAtoms());
    }


    @Test
    public void sub_select_one_simple_join_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM P) AS S, Q";
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
                .anyMatch(b -> b.equals(a))));
    }
}
