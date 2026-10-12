package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DatabaseTableDefinition;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.spec.sqlparser.exception.QueryParseException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryRuntimeException;

import java.util.List;
import java.util.function.Consumer;

import static it.unibz.inf.ontop.spec.sqlparser.SQLTestingTools.*;
import static org.junit.jupiter.api.Assertions.*;

public class SelectQueryParserTest {

    private NamedRelationDefinition TABLE_P, TABLE_Q, TABLE_R, TABLE_SP, TABLE_SQ;
    private DBTermType integerDBType;
    private QuotedIDFactory idfac;

    private static final String A1 = "A1";
    private static final String A2 = "A2";
    private static final String A3 = "A3";
    private static final String B1 = "B1";
    private static final String B2 = "B2";
    private static final String B3 = "B3";
    private static final String C1 = "C1";
    private static final String C2 = "C2";
    private static final String C3 = "C3";
    private static final String D1 = "D1";
    private static final String D2 = "D2";
    private static final String D3 = "D3";

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * EXCEPT (A) FROM P",
            "SELECT t.* EXCEPT (A) FROM P t",
            "SELECT * EXCLUDE (A) FROM P",
            "SELECT t.* EXCLUDE (A) FROM P t",
            "SELECT * REPLACE (A + 1 AS A) FROM P",
            "SELECT t.* REPLACE (A + 1 AS A) FROM P t",
            "SELECT * EXCEPT (A) REPLACE (B + 1 AS B) FROM P",
            "SELECT t.* EXCEPT (A) REPLACE (B + 1 AS B) FROM P t",
            "SELECT * AS x FROM P",
            "SELECT A AS x(y) FROM P",
            "SELECT AS STRUCT A FROM P",
            "SELECT AS VALUE A FROM P",
            "SELECT STRAIGHT_JOIN A FROM P",
            "SELECT A FROM P FINAL",
            "SELECT A FROM ONLY P",
            "SELECT A FROM P PREWHERE A = 1",
            "SELECT A FROM P QUALIFY A = 1",
            "SELECT A FROM P WINDOW w AS (PARTITION BY A)",
            "SELECT A FROM P WHERE A = 1 EMIT CHANGES",
            "SELECT A FROM P EMIT FINAL",
            "SELECT A FROM P SETTINGS x = 1",
            "SELECT A FROM P LIMIT 1 BY A",
            "SELECT A FROM P FOR UPDATE",
            "SELECT A FROM P FOR SHARE",
            "SELECT A FROM P FOR UPDATE OF P NOWAIT",
            "SELECT A FROM P FOR UPDATE SKIP LOCKED",
            "SELECT A FROM P FOR UPDATE WAIT 5",
            "SELECT A FROM P OPTION (RECOMPILE)",
            "SELECT A FROM P PROCEDURE ANALYSE (1, 2)",
            "SELECT A FROM P PREFERRING HIGH A",
            "SELECT A FROM P TABLESAMPLE SYSTEM (10)",
            "SELECT A FROM P FOR SYSTEM_TIME AS OF '2020-01-01'",
            "SELECT A FROM P t FOR SYSTEM_TIME AS OF '2020-01-01'",
            "SELECT A FROM P WITH (NOLOCK)",
            "SELECT A FROM @P",
            "SELECT P.A FROM P GLOBAL JOIN Q ON P.A = Q.A",
            "SELECT P.A FROM P p ANY INNER JOIN Q ON p.A = Q.A",
            "SELECT P.A FROM P ALL JOIN Q ON P.A = Q.A",
            "SELECT P.A FROM P ASOF JOIN Q ON P.A = Q.A",
            "SELECT P.A FROM P INNER HASH JOIN Q ON P.A = Q.A",
            "SELECT P.A FROM P JOIN FETCH Q ON P.A = Q.A",
            "SELECT P.A FROM P ARRAY JOIN Q",
            "SELECT A FROM P LATERAL VIEW explode(B) t AS x",
            "SELECT s.A FROM (SELECT A FROM P) s LIMIT 1",
            "SELECT P.A FROM P NATURAL LEFT JOIN Q",
            "SELECT P.A FROM P NATURAL RIGHT JOIN Q",
            "SELECT P.A FROM P NATURAL FULL JOIN Q",
            "SELECT s.A FROM (SELECT A FROM P) s TABLESAMPLE SYSTEM (10)",
            "SELECT s.A FROM (P JOIN Q ON P.A = Q.A) s TABLESAMPLE SYSTEM (10)",
            "SELECT COUNT(*) FROM P",
            "SELECT A LIKE B COLLATE utf8 FROM P",
            "SELECT A BETWEEN SYMMETRIC 1 AND 2 FROM P",
            "SELECT CAST(A AS CHAR CHARACTER SET utf8) FROM P",
            "SELECT A + 1 AS x FROM P WHERE A GLOBAL IN (1, 2)",
            "SELECT LOWER(A IGNORE NULLS) AS x FROM P"
    })
    public void rejectsUnsupportedSyntax(String sql) {
        assertThrows(UnsupportedSelectQueryException.class, () -> parse(sql), sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT A FROM P INTO OUTFILE 'result.txt'",
            "SELECT A FROM P INTO DUMPFILE 'result.txt'",
            "SELECT A FROM P INTO TEMP result WITH NO LOG"
    })
    public void rejectsAdditionalSelectIntoForms(String sql) {
        assertThrows(InvalidQueryException.class, () -> parse(sql), sql);
    }

    @Test
    public void rejectsFromItemSubclasses() throws Exception {
        parse("SELECT A FROM P");
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        builder.createDatabaseRelation("P", "A", integerDBType, false);
        SelectQueryParser parser = new SelectQueryParser(builder.build(), CORE_SINGLETONS);
        PlainSelect plain = JSqlParserTools.parse("SELECT A FROM P", false).getPlainSelect();
        plain.setFromItem(new Table("P") { });
        assertThrows(UnsupportedSelectQueryRuntimeException.class, () -> parser.translateSelect(plain, null));
        ParenthesedSelect subselect = new ParenthesedSelect() { };
        subselect.setSelect(JSqlParserTools.parse("SELECT A FROM P", false));
        subselect.setAlias(new Alias("s"));
        plain.setFromItem(subselect);
        assertThrows(UnsupportedSelectQueryRuntimeException.class, () -> parser.translateSelect(plain, null));
    }

    @Test
    public void rejectsWildcardAndSelectItemSubclasses() throws Exception {
        RAExpression ra = parse("SELECT A FROM P");
        SelectItemParser parser = new SelectItemParser(ra.getAttributes(),
                new ExpressionParser(idfac, CORE_SINGLETONS)::parseTerm, idfac);
        assertThrows(UnsupportedSelectQueryRuntimeException.class,
                () -> parser.parseSelectItems(List.of(SelectItem.from(new AllColumns() { }))));
        assertThrows(UnsupportedSelectQueryRuntimeException.class,
                () -> parser.parseSelectItems(List.of(SelectItem.from(new AllTableColumns(new Table("P")) { }))));
        assertThrows(UnsupportedSelectQueryRuntimeException.class,
                () -> parser.parseSelectItems(List.of(new SelectItem<>(new LongValue(1), new Alias("x")) { })));
        AllTableColumns returning = new AllTableColumns(new Table("P"));
        returning.setReturningQualifier("OLD");
        assertThrows(UnsupportedSelectQueryRuntimeException.class,
                () -> parser.parseSelectItems(List.of(SelectItem.from(returning))));
        returning.setReturningQualifier(null);
        returning.setReturningReferenceType(net.sf.jsqlparser.statement.ReturningReferenceType.values()[0]);
        assertThrows(UnsupportedSelectQueryRuntimeException.class,
                () -> parser.parseSelectItems(List.of(SelectItem.from(returning))));
        Table qualifier = new Table("P");
        qualifier.setTimeTravel("FOR SYSTEM_TIME AS OF '2020-01-01'");
        assertThrows(UnsupportedSelectQueryRuntimeException.class,
                () -> parser.parseSelectItems(List.of(SelectItem.from(new AllTableColumns(qualifier)))));
    }

    @Test
    public void rejectsPropertiesEvenWhenNotRendered() throws Exception {
        parse("SELECT A FROM P");
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        builder.createDatabaseRelation("P", "A", integerDBType, false);
        SelectQueryParser parser = new SelectQueryParser(builder.build(), CORE_SINGLETONS);
        List<Consumer<PlainSelect>> modifiers = List.of(
                s -> s.setSettings(List.of()),
                s -> s.setInterpolate(List.of()),
                s -> s.setForUpdateTables(List.of()),
                s -> s.setNoWait(true),
                s -> s.setSkipLocked(true),
                s -> s.setForUpdateBeforeOrderBy(true));
        for (Consumer<PlainSelect> modifier : modifiers) {
            PlainSelect select = JSqlParserTools.parse("SELECT A FROM P", false).getPlainSelect();
            modifier.accept(select);
            assertThrows(UnsupportedSelectQueryRuntimeException.class, () -> parser.translateSelect(select, null));
        }
        RAExpression ra = parse("SELECT A FROM P");
        SelectItemParser sip = new SelectItemParser(ra.getAttributes(),
                new ExpressionParser(idfac, CORE_SINGLETONS)::parseTerm, idfac);
        for (AllColumns wildcard : List.of(new AllColumns(), new AllTableColumns(new Table("P")))) {
            wildcard.setExceptColumns(new net.sf.jsqlparser.expression.operators.relational.ExpressionList<>());
            assertThrows(UnsupportedSelectQueryRuntimeException.class,
                    () -> sip.parseSelectItems(List.of(SelectItem.from(wildcard))));
            wildcard.setExceptColumns(null);
            wildcard.setReplaceExpressions(List.of());
            assertThrows(UnsupportedSelectQueryRuntimeException.class,
                    () -> sip.parseSelectItems(List.of(SelectItem.from(wildcard))));
            wildcard.setReplaceExpressions(null);
            wildcard.setExceptKeyword("EXCLUDE");
            assertThrows(UnsupportedSelectQueryRuntimeException.class,
                    () -> sip.parseSelectItems(List.of(SelectItem.from(wildcard))));
        }
        PlainSelect outer = JSqlParserTools.parse("SELECT s.A FROM (SELECT A FROM P) s", false).getPlainSelect();
        ParenthesedSelect wrapper = (ParenthesedSelect) outer.getFromItem();
        wrapper.setLimit(new Limit().withRowCount(new LongValue(1)));
        assertThrows(UnsupportedSelectQueryRuntimeException.class, () -> parser.translateSelect(outer, null));
        wrapper.setLimit(null);
        wrapper.setOrderByElements(List.of(new OrderByElement()));
        assertThrows(UnsupportedSelectQueryRuntimeException.class, () -> parser.translateSelect(outer, null));
    }

    @Test
    public void rejectsJoinModifiersAndUsingColumnProperties() throws Exception {
        parse("SELECT A FROM P");
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        builder.createDatabaseRelation("P", "A", integerDBType, false);
        builder.createDatabaseRelation("Q", "A", integerDBType, false);
        SelectQueryParser parser = new SelectQueryParser(builder.build(), CORE_SINGLETONS);
        List<Consumer<Join>> modifiers = List.of(
                j -> j.setAny(true), j -> j.setAll(true), j -> j.setAsOf(true),
                j -> j.setGlobal(true), j -> j.setArray(true), j -> j.setFetch(true),
                j -> j.setJoinHint(new JoinHint("HASH")));
        for (Consumer<Join> modifier : modifiers) {
            PlainSelect select = JSqlParserTools.parse("SELECT P.A FROM P JOIN Q USING (A)", false).getPlainSelect();
            modifier.accept(select.getJoins().get(0));
            assertThrows(UnsupportedSelectQueryRuntimeException.class, () -> parser.translateSelect(select, null));
        }
        PlainSelect select = JSqlParserTools.parse("SELECT P.A FROM P JOIN Q USING (A)", false).getPlainSelect();
        select.getJoins().get(0).getUsingColumns().get(0).setReturningQualifier("OLD");
        assertThrows(UnsupportedSelectQueryRuntimeException.class, () -> parser.translateSelect(select, null));
    }

    private RAExpression parse(String sql) throws QueryParseException, InvalidQueryException, UnsupportedSelectQueryException {

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

        return parser.parse(sql);
    }

    @Test
    public void inner_join_on_same_table_test() throws Exception {
        RAExpression re = parse("SELECT p1.A, p2.B FROM P p1 INNER JOIN P p2 on p1.A = p2.A ");

        assertEquals(join(eqOf(A1, A2), dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_P, A2, B2)), re.getIQTree());
    }

    private IQTree join(ImmutableExpression exp, IQTree tree1, IQTree tree2) {
        return IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(exp),
                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(),
                        ImmutableList.of(tree1, tree2)));
    }


    @Test
    public void inner_join_on_inner_join_ambiguity_test() throws Exception {
        // common column name "A" appears more than once in left table
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT A, C FROM P INNER JOIN Q on P.A = Q.A NATURAL JOIN R"));

        assertEquals("Attribute A is ambiguous with attributes", ex.getMessage().substring(0, ex.getMessage().indexOf(": {")));
    }


    @Test
    public void inner_join_on_inner_join_ambiguity2_test() throws Exception {
        // column reference "a" is ambiguous
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN R on Q.C = R.C"));

        assertEquals("Unable to find attribute A (available attributes are [P.A, Q.A, R.A, P.B, R.B, Q.C, R.C, D, R.D]) (from A)", ex.getMessage());
    }

    @Test
    public void inner_join_on_inner_join_test() throws Exception {
        RAExpression re = parse("SELECT P.A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN R on Q.C = R.C");

        assertEquals(join(eqOf(C2, C3),
                        join(eqOf(A1, A2), dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)),
                dataAtomOf(TABLE_R, A3, B3, C3, D3)),
                re.getIQTree());
    }

    @Test
    public void inner_join_on_inner_join_test2() throws Exception {
        RAExpression re = parse("SELECT Q.A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN R on Q.C = R.C");

        assertEquals(join(eqOf(C2, C3),
                        join(eqOf(A1, A2), dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)),
                        dataAtomOf(TABLE_R, A3, B3, C3, D3)),
                re.getIQTree());
    }

    @Test
    public void subjoin_test() throws Exception {
        RAExpression re = parse("SELECT S.A, S.C FROM R JOIN (P NATURAL JOIN Q) AS S ON R.A = S.A");

        assertEquals(join(eqOf(A1, A2), dataAtomOf(TABLE_R, A1, B1, C1, D1),
                        join(eqOf(A2, A3), dataAtomOf(TABLE_P, A2, B2), dataAtomOf(TABLE_Q, A3, C3))),
                re.getIQTree());
    }

    @Test
    public void select_one_no_from() throws Exception {
        RAExpression re = parse("SELECT 1");

        assertEquals(RAExpressionAttributes.ofUnqualifiedAttributesMap(ImmutableMap.of()), re.getAttributes());
        assertEquals(IQ_FACTORY.createTrueNode(), re.getIQTree());
    }

    @Test
    public void select_one_no_from_alias() throws Exception {
        RAExpression re = parse("SELECT 1 AS A");

        assertEquals(RAExpressionAttributes.ofUnqualifiedAttributesMap(ImmutableMap.of(idfac.createAttributeID("A"), SQLTestingTools.TERM_FACTORY.getDBConstant("1", integerDBType))), re.getAttributes());
        assertEquals(IQ_FACTORY.createTrueNode(), re.getIQTree());
    }

    @Test
    public void select_one_from() throws Exception {
        RAExpression re = parse("SELECT 1 FROM Q");

        assertEquals(RAExpressionAttributes.ofUnqualifiedAttributesMap(ImmutableMap.of()), re.getAttributes());
        assertEquals(dataAtomOf(TABLE_Q, A1, C1), re.getIQTree());
    }

    @Test
    public void select_one_from_alias() throws Exception {
        RAExpression re = parse("SELECT 1 AS A FROM Q");

        assertEquals(RAExpressionAttributes.ofUnqualifiedAttributesMap(ImmutableMap.of(idfac.createAttributeID("A"), SQLTestingTools.TERM_FACTORY.getDBConstant("1", integerDBType))), re.getAttributes());
        assertEquals(dataAtomOf(TABLE_Q, A1, C1), re.getIQTree());
    }

    @Test
    public void select_missing_column_test2() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT R FROM Q"));

        assertEquals("Unable to find attribute R (available attributes are [A, Q.A, C, Q.C]) (from R)", ex.getMessage());
    }

    @Test
    public void select_natural_join_schema() throws Exception {
        RAExpression re = parse("SELECT A FROM S.PP NATURAL JOIN S.QQ");

        assertEquals(join(eqOf(A1, A2), dataAtomOf(TABLE_SP, A1, B1), dataAtomOf(TABLE_SQ, A2, C2)), re.getIQTree());
    }

    @Test
    public void select_apply() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT A FROM P APPLY Q"));

        assertEquals("Encountered: <S_IDENTIFIER> / \"Q\", at line 1, column 23, in lexical state DEFAULT.",
                ex.getOriginalMessage().split("\\R", 2)[0]);
    }

    @Test
    public void select_cross_apply() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT A FROM P CROSS APPLY Q"));

        assertEquals("APPLY is not supported CROSS APPLY Q", ex.getMessage());
    }

    @Test // is valid in MS SQL Server
    public void select_outer_apply() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT A FROM P OUTER APPLY Q"));

        assertEquals("APPLY is not supported OUTER APPLY Q", ex.getMessage());
    }

    @Test
    public void select_straight() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT A FROM P STRAIGHT_JOIN Q"));

        assertEquals("STRAIGHT_JOIN is not supported STRAIGHT_JOIN Q", ex.getMessage());
    }

    @Test
    public void select_straight_on() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT A FROM P STRAIGHT_JOIN Q ON (P.A = Q.A)"));

        assertEquals("STRAIGHT_JOIN is not supported STRAIGHT_JOIN Q ON (P.A = Q.A)", ex.getMessage());    }

    @Test
    public void select_straight_using() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT A FROM P STRAIGHT_JOIN Q USING (A)"));

        assertEquals("STRAIGHT_JOIN is not supported STRAIGHT_JOIN Q USING (A)", ex.getMessage());
    }

    @Test
    public void select_within() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT A FROM P INNER JOIN Q WITHIN (1 HOURS) ON P.A = Q.A"));

        assertEquals("WITHIN WINDOW is not supported INNER JOIN Q WITHIN (1 HOURS) ON P.A = Q.A", ex.getMessage());
    }

    @Test
    public void select_oracle_hint() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT /*+ value  */ A FROM P"));

        assertEquals("Oracle hints are not supported SELECT /*+ value */ A FROM P", ex.getMessage());
    }

    @Test
    public void select_skip() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT SKIP 1 A FROM P"));

        assertEquals("SKIP / FIRST are not supported SELECT SKIP 1 A FROM P", ex.getMessage());
    }

    @Test
    public void select_first() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT FIRST 10 A FROM P"));

        assertEquals("SKIP / FIRST are not supported SELECT FIRST 10 A FROM P", ex.getMessage());
    }

    @Test
    public void select_top() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT TOP 10 A FROM P"));

        assertEquals("TOP is not supported SELECT TOP 10 A FROM P", ex.getMessage());
    }

    @Test
    public void select_sql_no_cache() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT SQL_NO_CACHE A FROM P"));

        assertEquals("MySQL SQL_NO_CACHE/SQL_CACHE is not supported SELECT SQL_NO_CACHE A FROM P", ex.getMessage());
    }

    @Test
    public void select_sql_cal_found_rows() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT SQL_CALC_FOUND_ROWS A FROM P"));

        assertEquals("MySQL SQL_CALC_FOUND_ROWS is not supported SELECT SQL_CALC_FOUND_ROWS A FROM P", ex.getMessage());
    }

    // JSQLParser apparently allows more weird combinations like this
    @Test
    public void select_left_simple() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P LEFT, Q"));

        assertEquals("Invalid simple join (from Q)", ex.getMessage());
    }

    @Test
    public void select_simple_on() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P, Q ON P.A = Q.A"));

        assertEquals("Invalid simple join (from Q ON P.A = Q.A)", ex.getMessage());
    }

    @Test
    public void select_simple_using() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P, Q USING (A)"));

        assertEquals("Invalid simple join (from Q USING (A))", ex.getMessage());
    }

    @Test
    public void select_left_semi_join_on() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT * FROM P LEFT SEMI JOIN Q ON P.A = Q.A"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported LEFT SEMI JOIN Q ON P.A = Q.A", ex.getMessage());
    }

    @Test
    public void select_left_semi_join_using() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P LEFT SEMI JOIN Q USING (A)"));

        assertEquals("Invalid SEMI JOIN (from LEFT SEMI JOIN Q USING (A))", ex.getMessage());
    }

    @Test
    public void select_column_alias() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT * FROM P AS PP(AA, BB)"));

        assertEquals("Alias columns are not supported  AS PP(AA, BB)", ex.getMessage());
    }

    @Test
    public void select_from_values() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT * FROM (VALUES(1,2,3)) QQ(A,B,C)"));

        assertEquals("ValuesLists are not supported (VALUES (1, 2, 3)) QQ(A, B, C)", ex.getMessage());
    }


    // -----------------------------------------------------
    // NEW TESTS

    @Test
    public void select_simple_join() throws Exception {
        RAExpression re = parse("SELECT * FROM P, Q");

        assertEquals(IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2))), re.getIQTree());
    }

    @Test
    public void select_simple_outer_join() throws Exception {
        // special case in JSQLParser - no clue what it may even mean
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT * FROM P, OUTER Q"));

        assertEquals("Simple OUTER JOINs are not supported OUTER Q", ex.getMessage());
    }

    // ----------------------------------------------------------
    // valid combinations - basic tests

    @Test
    public void select_natural_join() throws Exception {
        RAExpression re = parse("SELECT A FROM P NATURAL JOIN Q");

        assertEquals(join(eqOf(A1, A2), dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getIQTree());
    }

    @Test
    public void select_cross_join() throws Exception {
        RAExpression re = parse("SELECT * FROM P CROSS JOIN Q");

        assertEquals(IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2))), re.getIQTree());
    }

    @Test
    public void select_join_on() throws Exception {
        RAExpression re = parse("SELECT * FROM P JOIN Q ON P.A = Q.A");

        assertEquals(join(eqOf(A1, A2), dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getIQTree());
    }

    @Test
    public void select_inner_join_on() throws Exception {
        RAExpression re = parse("SELECT * FROM P INNER JOIN Q ON P.A = Q.A");

        assertEquals(join(eqOf(A1, A2), dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getIQTree());
    }

    @Test
    public void select_join_using() throws Exception {
        RAExpression re = parse("SELECT * FROM P JOIN Q USING(A)");

        assertEquals(join(eqOf(A1, A2), dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getIQTree());
    }

    @Test
    public void select_inner_join_using() throws Exception {
        RAExpression re = parse("SELECT * FROM P INNER JOIN Q USING(A)");

        assertEquals(join(eqOf(A1, A2), dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2)), re.getIQTree());
    }


    @Test
    public void select_outer_join_using() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT * FROM P OUTER JOIN Q USING(A)"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported OUTER JOIN Q USING (A)", ex.getMessage());
    }

    @Test
    public void select_outer_join_on() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse("SELECT * FROM P OUTER JOIN Q ON P.A = Q.A"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported OUTER JOIN Q ON P.A = Q.A", ex.getMessage());
    }

    @Test
    public void select_outer_join() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P OUTER JOIN Q"));

        assertEquals("[INNER|OUTER] JOIN requires either ON or USING (from OUTER JOIN Q)", ex.getMessage());
    }


    // -----------------------------------------------
    // invalid combinations for JSQLParser

    @Test
    public void select_natural_outer_join() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P NATURAL OUTER JOIN Q"));

        assertEquals("Invalid JOIN modifiers: NATURAL OUTER", ex.getOriginalMessage());
    }

    @Test
    public void select_cross_outer_join() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P CROSS OUTER JOIN Q"));

        assertEquals("Encountered: <K_CROSS> / \"CROSS\", at line 1, column 17, in lexical state DEFAULT.",
                ex.getOriginalMessage().split("\\R", 2)[0]);
    }

    @Test
    public void select_natural_inner_join() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P NATURAL INNER JOIN Q"));

        assertEquals("Invalid JOIN modifiers: NATURAL INNER", ex.getOriginalMessage());
    }

    @Test
    public void select_cross_inner_join() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P CROSS INNER JOIN Q"));

        assertEquals("Encountered: <K_CROSS> / \"CROSS\", at line 1, column 17, in lexical state DEFAULT.",
                ex.getOriginalMessage().split("\\R", 2)[0]);
    }

    @Test
    public void select_right_inner_join() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P RIGHT INNER JOIN Q"));

        assertEquals("Invalid JOIN modifiers: RIGHT INNER", ex.getOriginalMessage());
    }

    @Test
    public void select_full_inner_join() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P FULL INNER JOIN Q"));

        assertEquals("Encountered: <K_FULL> / \"FULL\", at line 1, column 17, in lexical state DEFAULT.",
                ex.getOriginalMessage().split("\\R", 2)[0]);
    }

    @Test
    public void select_left_inner_join() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P LEFT INNER JOIN Q"));

        assertEquals("Invalid JOIN modifiers: LEFT INNER", ex.getOriginalMessage());
    }

    @Test
    public void select_natural_outer_join_on() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P NATURAL OUTER JOIN Q ON P.A = Q.A"));

        assertEquals("Invalid JOIN modifiers: NATURAL OUTER", ex.getOriginalMessage());
    }

    @Test
    public void select_cross_outer_join_on() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P CROSS OUTER JOIN Q ON P.A = Q.A"));

        assertEquals("Encountered: <K_CROSS> / \"CROSS\", at line 1, column 17, in lexical state DEFAULT.",
                ex.getOriginalMessage().split("\\R", 2)[0]);
    }

    @Test
    public void select_natural_inner_join_on() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P NATURAL INNER JOIN Q ON P.A = Q.A"));

        assertEquals("Invalid JOIN modifiers: NATURAL INNER", ex.getOriginalMessage());
    }

    @Test
    public void select_cross_inner_join_on() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P CROSS INNER JOIN Q ON P.A = Q.A"));

        assertEquals("Encountered: <K_CROSS> / \"CROSS\", at line 1, column 17, in lexical state DEFAULT.",
                ex.getOriginalMessage().split("\\R", 2)[0]);
    }

    @Test
    public void select_right_inner_join_on() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P RIGHT INNER JOIN Q ON P.A = Q.A"));

        assertEquals("Invalid JOIN modifiers: RIGHT INNER", ex.getOriginalMessage());
    }

    @Test
    public void select_full_inner_join_on() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P FULL INNER JOIN Q ON P.A = Q.A"));

        assertEquals("Encountered: <K_FULL> / \"FULL\", at line 1, column 17, in lexical state DEFAULT.",
                ex.getOriginalMessage().split("\\R", 2)[0]);
    }

    @Test
    public void select_left_inner_join_on() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P LEFT INNER JOIN Q ON P.A = Q.A"));

        assertEquals("Invalid JOIN modifiers: LEFT INNER", ex.getOriginalMessage());
    }

    @Test
    public void select_natural_outer_join_using() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P NATURAL OUTER JOIN Q USING(A)"));

        assertEquals("Invalid JOIN modifiers: NATURAL OUTER", ex.getOriginalMessage());
    }

    @Test
    public void select_cross_outer_join_using() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P CROSS OUTER JOIN Q USING(A)"));

        assertEquals("Encountered: <K_CROSS> / \"CROSS\", at line 1, column 17, in lexical state DEFAULT.",
                ex.getOriginalMessage().split("\\R", 2)[0]);
    }

    @Test
    public void select_natural_inner_join_using() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P NATURAL INNER JOIN Q USING(A)"));

        assertEquals("Invalid JOIN modifiers: NATURAL INNER", ex.getOriginalMessage());
    }

    @Test
    public void select_cross_inner_join_using() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P CROSS INNER JOIN Q USING(A)"));

        assertEquals("Encountered: <K_CROSS> / \"CROSS\", at line 1, column 17, in lexical state DEFAULT.",
                ex.getOriginalMessage().split("\\R", 2)[0]);
    }

    @Test
    public void select_right_inner_join_using() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P RIGHT INNER JOIN Q USING(A)"));

        assertEquals("Invalid JOIN modifiers: RIGHT INNER", ex.getOriginalMessage());
    }

    @Test
    public void select_full_inner_join_using() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P FULL INNER JOIN Q USING(A)"));

        assertEquals("Encountered: <K_FULL> / \"FULL\", at line 1, column 17, in lexical state DEFAULT.",
                ex.getOriginalMessage().split("\\R", 2)[0]);
    }

    @Test
    public void select_left_inner_join_using() throws Exception {
        var ex = assertThrows(QueryParseException.class, () ->
                parse("SELECT * FROM P LEFT INNER JOIN Q USING(A)"));

        assertEquals("Invalid JOIN modifiers: LEFT INNER", ex.getOriginalMessage());
    }


    // -------------------------------------------------------
    // invalid combinations of join modifiers (see SQL standard)

    @Test
    public void select_join() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P JOIN Q"));

        assertEquals("[INNER|OUTER] JOIN requires either ON or USING (from JOIN Q)", ex.getMessage());
    }

    @Test
    public void select_right_join() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P RIGHT JOIN Q"));

        assertEquals("[INNER|OUTER] JOIN requires either ON or USING (from RIGHT JOIN Q)", ex.getMessage());
    }

    @Test
    public void select_full_join() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P FULL JOIN Q"));

        assertEquals("[INNER|OUTER] JOIN requires either ON or USING (from FULL JOIN Q)", ex.getMessage());
    }

    @Test
    public void select_left_join() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P LEFT JOIN Q"));

        assertEquals("[INNER|OUTER] JOIN requires either ON or USING (from LEFT JOIN Q)", ex.getMessage());
    }

    @Test
    public void select_right_outer_join() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P RIGHT OUTER JOIN Q"));

        assertEquals("[INNER|OUTER] JOIN requires either ON or USING (from RIGHT OUTER JOIN Q)", ex.getMessage());
    }

    @Test
    public void select_full_outer_join() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P FULL OUTER JOIN Q"));

        assertEquals("[INNER|OUTER] JOIN requires either ON or USING (from FULL OUTER JOIN Q)", ex.getMessage());
    }

    @Test
    public void select_left_outer_join() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P LEFT OUTER JOIN Q"));

        assertEquals("[INNER|OUTER] JOIN requires either ON or USING (from LEFT OUTER JOIN Q)", ex.getMessage());
    }

    @Test
    public void select_inner_join() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P INNER JOIN Q"));

        assertEquals("[INNER|OUTER] JOIN requires either ON or USING (from INNER JOIN Q)", ex.getMessage());
    }

    @Test
    public void select_natural_join_on() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P NATURAL JOIN Q ON P.A = Q.A"));

        assertEquals("NATURAL JOIN cannot have USING/ON conditions (from NATURAL JOIN Q ON P.A = Q.A)", ex.getMessage());
    }

    @Test
    public void select_cross_join_on() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P CROSS JOIN Q ON P.A = Q.A"));

        assertEquals("CROSS JOIN cannot have USING/ON conditions (from CROSS JOIN Q ON P.A = Q.A)", ex.getMessage());
    }

    @Test
    public void select_natural_join_using() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P NATURAL JOIN Q USING(A)"));

        assertEquals("NATURAL JOIN cannot have USING/ON conditions (from NATURAL JOIN Q USING (A))", ex.getMessage());
    }

    @Test
    public void select_cross_join_using() throws Exception {
        var ex = assertThrows(InvalidQueryException.class, () ->
                parse("SELECT * FROM P CROSS JOIN Q USING(A)"));

        assertEquals("CROSS JOIN cannot have USING/ON conditions (from CROSS JOIN Q USING (A))", ex.getMessage());
    }


    // ---------------------------------------------------
    // Unsupported - non-CQ in the mapping

    @Test
    public void select_right_join_on() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse( "SELECT * FROM P RIGHT JOIN Q ON P.A = Q.A"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported RIGHT JOIN Q ON P.A = Q.A", ex.getMessage());
    }

    @Test
    public void select_full_join_on() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse( "SELECT * FROM P FULL JOIN Q ON P.A = Q.A"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported FULL JOIN Q ON P.A = Q.A", ex.getMessage());
    }

    @Test
    public void select_left_join_on() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse( "SELECT * FROM P LEFT JOIN Q ON P.A = Q.A"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported LEFT JOIN Q ON P.A = Q.A", ex.getMessage());
    }

    @Test
    public void select_right_outer_join_on() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse( "SELECT * FROM P RIGHT OUTER JOIN Q ON P.A = Q.A"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported RIGHT OUTER JOIN Q ON P.A = Q.A", ex.getMessage());
    }

    @Test
    public void select_full_outer_join_on() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse( "SELECT * FROM P FULL OUTER JOIN Q ON P.A = Q.A"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported FULL OUTER JOIN Q ON P.A = Q.A", ex.getMessage());
    }

    @Test
    public void select_left_outer_join_on() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryException.class, () ->
                parse( "SELECT * FROM P LEFT OUTER JOIN Q ON P.A = Q.A"));

        assertEquals("LEFT/RIGHT/FULL OUTER JOINs are not supported LEFT OUTER JOIN Q ON P.A = Q.A", ex.getMessage());
    }

    // -------------------------------------------------
    // other features

    @Test
    public void join_using_2_test() throws Exception {
        RAExpression re = parse("SELECT A, B FROM P INNER JOIN R USING (A,B)");

        assertEquals(join(TERM_FACTORY.getConjunction(eqOf(A1, A2), eqOf(B1, B2)), dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_R, A2, B2, C2, D2)), re.getIQTree());
    }

    @Test
    public void select_join_2_test() throws Exception {
        RAExpression re = parse("SELECT a.A, b.B FROM P AS a JOIN R AS b ON (a.A = b.B)");

        assertEquals(join(eqOf(A1, B2), dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_R, A2, B2, C2, D2)), re.getIQTree());
    }

    @Test
    public void sub_select_one_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM P) AS S";
        RAExpression re = parse(query);

        assertEquals(dataAtomOf(TABLE_P, A1, B1), re.getIQTree());
    }

    @Test
    public void sub_select_two_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM (SELECT * FROM P) AS T) AS S";
        RAExpression re = parse(query);

        assertEquals(dataAtomOf(TABLE_P, A1, B1), re.getIQTree());
    }

    @Test
    public void sub_select_one_simple_join_internal_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM P, Q) AS S";
        RAExpression re = parse(query);

        assertEquals(IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2))), re.getIQTree());
    }


    @Test
    public void sub_select_one_simple_join_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM P) AS S, Q";
        RAExpression re = parse(query);

        assertEquals(IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataAtomOf(TABLE_P, A1, B1), dataAtomOf(TABLE_Q, A2, C2))), re.getIQTree());
    }



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
}
