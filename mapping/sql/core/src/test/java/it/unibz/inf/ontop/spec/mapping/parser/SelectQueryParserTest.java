package it.unibz.inf.ontop.spec.mapping.parser;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.spec.mapping.parser.impl.RAExpression;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryParser;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedSelectQueryException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Test;

import java.util.List;

import static it.unibz.inf.ontop.utils.SQLMappingTestingTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 * -
 */
public class SelectQueryParserTest {

    private static final String P = "P";
    private static final String Q = "Q";
    private static final String R = "R";

    private static final String A1 = "A1";
    private static final String A2 = "A2";
    private static final String A3 = "A3";
    private static final String B1 = "B1";
    private static final String B2 = "B2";
    private static final String C1 = "C1";
    private static final String C2 = "C2";
    private static final String C3 = "C3";
    private static final String D1 = "D1";
    private static final TermType ROOT_TERM_TYPE = TYPE_FACTORY.getAbstractAtomicTermType();


    @Test
    public void inner_join_on_same_table_test() throws Exception {
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse("SELECT p1.A, p2.B FROM P p1 INNER JOIN  P p2 on p1.A = p2.A ");
        System.out.println(re);

//        assertEquals(2, parse.getHead().getTerms().size());
//        assertEquals(4, parse.getReferencedVariables().size());

        assertMatches(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, P, A1, B1), dataAtomOf(m, P, A2, B2)), re.getDataAtoms());
    }


    @Test(expected = InvalidSelectQueryException.class)
    public void inner_join_on_inner_join_ambiguity_test() throws Exception {
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A =  Q.A NATURAL JOIN  R ");
    }


    @Test(expected = InvalidSelectQueryException.class)
    public void inner_join_on_inner_join_ambiguity2_test() throws Exception {
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        // column reference "a" is ambiguous
        String sql = "SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN  R on Q.C =  R.C;";
        RAExpression re = parser.parse(sql);
        System.out.println("\n" + sql + "\n" + "column reference \"a\" is ambiguous --- this is wrongly parsed:\n" + re);
    }

    @Test(expected = InvalidSelectQueryException.class)
    public void inner_join_on_inner_join_test() throws Exception {
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN  R on Q.C =  R.C;");
    }

    @Test
    public void subjoin_test() throws Exception {
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse("SELECT S.A, S.C FROM R JOIN (P NATURAL JOIN Q) AS S ON R.A = S.A");
        System.out.println(re);

        assertMatches(ImmutableList.of(eqOf(A1, A2), eqOf(A2, A3)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, R, A1, B1, C1, D1),
                dataAtomOf(m, P, A2, B2), dataAtomOf(m, Q, A3, C3)), re.getDataAtoms());
    }

    // -----------------------------------------------------
    // NEW TESTS

    @Test
    public void simple_join_test() throws Exception {
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse("SELECT * FROM P, Q;");

        assertMatches(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, P, A1, B1), dataAtomOf(m, Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void natural_join_test() throws Exception {
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse("SELECT A FROM P NATURAL JOIN  Q;");
        System.out.println(re);

        assertMatches(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, P, A1, B1), dataAtomOf(m, Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void cross_join_test() throws Exception {
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse("SELECT * FROM P CROSS JOIN  Q;");

        assertMatches(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, P, A1, B1), dataAtomOf(m, Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void join_on_test() throws Exception {
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse("SELECT * FROM P JOIN  Q ON P.A = Q.A;");

        assertMatches(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, P, A1, B1), dataAtomOf(m, Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void inner_join_on_test() throws Exception {
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse("SELECT * FROM P INNER JOIN  Q ON P.A = Q.A;");

        assertMatches(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, P, A1, B1), dataAtomOf(m, Q, A2, C2)), re.getDataAtoms());
    }

    @Test
    public void join_using_test() throws Exception {
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse("SELECT * FROM P JOIN  Q USING(A);");

        assertMatches(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, P, A1, B1), dataAtomOf(m, Q, A2, C2)), re.getDataAtoms());
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void select_no_from_test() throws Exception {
        SelectQueryParser parser = new SelectQueryParser(createMetadata(), CORE_SINGLETONS);
        parser.parse("SELECT 1");
    }

    @Test(expected = InvalidSelectQueryException.class)
    public void select_one_complex_expression_test() throws Exception {
        SelectQueryParser parser = new SelectQueryParser(createMetadata(), CORE_SINGLETONS);
        parser.parse("SELECT 1 FROM Q");
    }


    @Test(expected = InvalidSelectQueryException.class)
    public void select_one_complex_expression_test2() throws Exception {
        SelectQueryParser parser = new SelectQueryParser(createMetadata(), CORE_SINGLETONS);
        parser.parse("SELECT R FROM Q");
    }

    @Test
    public void inner_join_using_test() throws Exception {
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse("SELECT * FROM P INNER JOIN  Q USING(A);");

        assertMatches(ImmutableList.of(eqOf(A1, A2)), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, P, A1, B1), dataAtomOf(m, Q, A2, C2)), re.getDataAtoms());
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
            SelectQueryParser parser = new SelectQueryParser(createMetadata(), CORE_SINGLETONS);
            Exception e = null;
            try {
                parser.parse(query);
                System.out.println(query + " - Wrong!");
            }
            catch (UnsupportedSelectQueryException ex) {
                System.out.println(query + " - OK");
                e = ex;
            } catch (InvalidSelectQueryException e1) {
                e1.printStackTrace();
            }
            assertNotNull(e);
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
            SelectQueryParser parser = new SelectQueryParser(createMetadata(), CORE_SINGLETONS);
            Exception e = null;
            try {
                parser.parse(query);
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
            SelectQueryParser parser = new SelectQueryParser(createMetadata(), CORE_SINGLETONS);
            Exception e = null;
            try {
                parser.parse(query);
                System.out.println(query + " - Wrong!");
            }
            catch (UnsupportedSelectQueryException ex) {
                System.out.println(query + " - OK");
                e = ex;
            }
            catch (InvalidSelectQueryException e1) {
                e1.printStackTrace();
            }
            assertNotNull(e);
        });
    }

    @Test
    public void join_using_2_test() throws Exception {
        SelectQueryParser parser = new SelectQueryParser(createMetadata(), CORE_SINGLETONS);

        RAExpression re = parser.parse("SELECT A, B FROM P INNER JOIN R USING (A,B)");
        System.out.println(re);

        //assertEquals(2, parse.getHead().getTerms().size());
        //assertEquals(6, parse.getReferencedVariables().size());

        assertEquals(2, re.getDataAtoms().size());
        // TODO: add data atoms asserts

        assertMatches(ImmutableList.of(eqOf(A1, A2), eqOf(B1, B2)), re.getFilterAtoms());
    }

    @Test
    public void select_join_2_test() throws Exception {
        SelectQueryParser parser = new SelectQueryParser(createMetadata(), CORE_SINGLETONS);
        // common column name "A" appears more than once in left table
        RAExpression re = parser.parse("SELECT a.A, b.B FROM P AS a JOIN R AS b  ON (a.A = b.B);");

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
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse(query);
        System.out.print(re);

        assertMatches(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, P, A1, B1)), re.getDataAtoms());
    }

    @Test
    public void sub_select_two_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM (SELECT * FROM P) AS T) AS S;";
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse(query);
        System.out.print(re);

        assertMatches(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, P, A1, B1)), re.getDataAtoms());
    }

    @Test
    public void sub_select_one_simple_join_internal_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM P, Q) AS S;";
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse(query);
        System.out.print(re);

        assertMatches(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, P, A1, B1), dataAtomOf(m, Q, A2, C2)), re.getDataAtoms());
    }


    @Test
    public void sub_select_one_simple_join_test() throws Exception {
        String  query = "SELECT * FROM (SELECT * FROM P) AS S, Q ;";
        DBMetadata m = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(m, CORE_SINGLETONS);
        RAExpression re = parser.parse(query);
        System.out.print(re);

        assertMatches(ImmutableList.of(), re.getFilterAtoms());
        assertMatches(ImmutableList.of(dataAtomOf(m, P, A1, B1), dataAtomOf(m, Q, A2, C2)), re.getDataAtoms());
    }


    // END SUB SELECT TESTS

    private ImmutableExpression eqOf(String var1, String var2) {
        return TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getVariable(var1), TERM_FACTORY.getVariable(var2));
    }

    private DataAtom<RelationPredicate> dataAtomOf(DBMetadata m, String predicateName, String var1, String var2) {
        return ATOM_FACTORY.getDataAtom(new FakeRelationPredicate(m.getDatabaseRelation(m.getQuotedIDFactory().createRelationID(null, predicateName))),
                ImmutableList.of(TERM_FACTORY.getVariable(var1), TERM_FACTORY.getVariable(var2)));
    }

    private DataAtom<RelationPredicate> dataAtomOf(DBMetadata m, String predicateName, String var1, String var2, String var3, String var4) {
        return ATOM_FACTORY.getDataAtom(new FakeRelationPredicate(m.getDatabaseRelation(m.getQuotedIDFactory().createRelationID(null, predicateName))),
                ImmutableList.of(TERM_FACTORY.getVariable(var1), TERM_FACTORY.getVariable(var2), TERM_FACTORY.getVariable(var3), TERM_FACTORY.getVariable(var4)));
    }

    private <T> void assertMatches(ImmutableList<T> list0, List<T> list) {
        assertEquals(list0.size(), list.size());
        list0.forEach(a -> { assertTrue(list.contains(a)); });
    }


    private DBMetadata createMetadata() {
        RDBMetadata metadata = createDummyMetadata();
        QuotedIDFactory idfac = metadata.getQuotedIDFactory();

        DBTermType integerType = TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType();

        DatabaseRelationDefinition relation1 =
                metadata.createDatabaseRelation(idfac.createRelationID(null, P));
        relation1.addAttribute(idfac.createAttributeID("A"), integerType.getName(), integerType, false);
        relation1.addAttribute(idfac.createAttributeID("B"), integerType.getName(), integerType, false);

        DatabaseRelationDefinition relation2 =
                metadata.createDatabaseRelation(idfac.createRelationID(null, Q));
        relation2.addAttribute(idfac.createAttributeID("A"), integerType.getName(), integerType, false);
        relation2.addAttribute(idfac.createAttributeID("C"), integerType.getName(), integerType, false);

        DatabaseRelationDefinition relation3 =
                metadata.createDatabaseRelation(idfac.createRelationID(null, R));
        relation3.addAttribute(idfac.createAttributeID("A"), integerType.getName(), integerType, false);
        relation3.addAttribute(idfac.createAttributeID("B"), integerType.getName(), integerType, false);
        relation3.addAttribute(idfac.createAttributeID("C"), integerType.getName(), integerType, false);
        relation3.addAttribute(idfac.createAttributeID("D"), integerType.getName(), integerType, false);

        return metadata;
    }
}
