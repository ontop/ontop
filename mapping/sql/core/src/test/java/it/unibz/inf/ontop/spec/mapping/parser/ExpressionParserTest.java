package it.unibz.inf.ontop.spec.mapping.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.mapping.parser.impl.ExpressionParser;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedSelectQueryRuntimeException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.utils.SQLMappingTestingTools.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Roman Kontchakov on 02/12/2016.
 */
public class ExpressionParserTest {

    private DBMetadata METADATA;
    private QuotedIDFactory IDFAC;

    @Before
    public void beforeEachTest() {
        METADATA = EMPTY_METADATA;
        IDFAC = METADATA.getQuotedIDFactory();
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void null_Test() throws JSQLParserException {
        String sql = "SELECT NULL AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test
    public void double_Test() throws JSQLParserException {
        String sql = "SELECT 1.0 AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getConstantLiteral("1.0", XSD.DOUBLE), translation);
    }

    @Test
    public void long_Test() throws JSQLParserException {
        String sql = "SELECT 1 AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getConstantLiteral("1", XSD.LONG), translation);
    }

    @Test
    public void string_Test() throws JSQLParserException {
        String sql = "SELECT \'1\' AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getConstantLiteral("1", XSD.STRING), translation);
    }

    @Test
    public void date_Test() throws JSQLParserException {
        //  ODBC escape sequence syntax
        String sql = "SELECT {d '2016-12-02'} AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getConstantLiteral("2016-12-02", XSD.DATE), translation);
    }

    @Test
    public void time_Test() throws JSQLParserException {
        //  ODBC escape sequence syntax
        String sql = "SELECT {t '15:57:02'} AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getConstantLiteral("15:57:02", XSD.TIME), translation);
    }

    @Test
    public void timestamp_Test() throws JSQLParserException {
        //  ODBC escape sequence syntax
        String sql = "SELECT {ts '2016-12-02 15:57:02.03'} AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getConstantLiteral("2016-12-02 15:57:02.03", XSD.DATETIMESTAMP), translation);
    }

    @Test
    public void addition_Test() throws JSQLParserException {
        String sql = "SELECT X + 1 AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.ADD,
                v,
                TERM_FACTORY.getConstantLiteral("1", XSD.LONG)), translation);
    }

    @Test
    public void subtraction_Test() throws JSQLParserException {
        String sql = "SELECT X - 1 AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.SUBTRACT,
                v,
                TERM_FACTORY.getConstantLiteral("1", XSD.LONG)), translation);
    }

    @Test
    public void multiplication_Test() throws JSQLParserException {
        String sql = "SELECT X * 2 AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.MULTIPLY,
                v,
                TERM_FACTORY.getConstantLiteral("2", XSD.LONG)), translation);
    }

    @Test
    public void division_Test() throws JSQLParserException {
        String sql = "SELECT X / 2 AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.DIVIDE,
                v,
                TERM_FACTORY.getConstantLiteral("2", XSD.LONG)), translation);
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void modulo_Test() throws JSQLParserException {
        String sql = "SELECT X % 2 AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

//        assertEquals(FACTORY.getFunction(
//                ExpressionOperation.DIVIDE,
//                v,
//                FACTORY.getConstantLiteral("2", XSD.LONG)), translation);
    }

    @Test
    public void concat_2_Test() throws JSQLParserException {
        String sql = "SELECT X || 'B' AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.CONCAT,
                v,
                TERM_FACTORY.getConstantLiteral("B", XSD.STRING)), translation);
    }

    @Test
    public void concat_3_Test() throws JSQLParserException {
        String sql = "SELECT 'A' || X || 'B' FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));


        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.CONCAT,
                TERM_FACTORY.getImmutableFunctionalTerm(
                        ExpressionOperation.CONCAT,
                        TERM_FACTORY.getConstantLiteral("A", XSD.STRING),
                        v),
                TERM_FACTORY.getConstantLiteral("B", XSD.STRING)), translation);
    }

    @Test
    public void function_CONCAT_Test() throws JSQLParserException {
        String sql = "SELECT CONCAT('A', X, 'B') FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.CONCAT,
                TERM_FACTORY.getImmutableFunctionalTerm(
                        ExpressionOperation.CONCAT,
                        TERM_FACTORY.getConstantLiteral("A", XSD.STRING),
                        v),
                TERM_FACTORY.getConstantLiteral("B", XSD.STRING)), translation);
    }

    // Boolean expressions are not allowed in the SELECT clause
    // so, the tests below depend on the WHERE clause

    @Test
    public void equalsTo_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X = 'B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.EQ,
                v,
                TERM_FACTORY.getConstantLiteral("B", XSD.STRING)), translation.get(0));
    }

    @Test
    public void notEqualsTo_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X <> 'B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.NEQ,
                v,
                TERM_FACTORY.getConstantLiteral("B", XSD.STRING)), translation.get(0));
    }

    @Test
    public void notEqualsTo_Bang_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X != 'B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.NEQ,
                v,
                TERM_FACTORY.getConstantLiteral("B", XSD.STRING)), translation.get(0));
    }

    @Test
    public void greaterThan_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X > 3";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.GT,
                v,
                TERM_FACTORY.getConstantLiteral("3", XSD.LONG)), translation.get(0));
    }

    @Test
    public void greaterThanEquals_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X >= 3";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.GTE,
                v,
                TERM_FACTORY.getConstantLiteral("3", XSD.LONG)), translation.get(0));
    }

    @Test
    public void minorThan_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X < 3";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.LT,
                v,
                TERM_FACTORY.getConstantLiteral("3", XSD.LONG)), translation.get(0));
    }

    @Test
    public void minorThanEquals_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X <= 3";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.LTE,
                v,
                TERM_FACTORY.getConstantLiteral("3", XSD.LONG)), translation.get(0));
    }

    @Test
    public void not_EqualsTo_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X = 'B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT, TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.EQ,
                v,
                TERM_FACTORY.getConstantLiteral("B", XSD.STRING))), translation.get(0));
    }

    @Test
    public void not_NotEqualsTo_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X <> 'B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT, TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.NEQ,
                v,
                TERM_FACTORY.getConstantLiteral("B", XSD.STRING))), translation.get(0));
    }

    @Test
    public void not_NotEqualsTo_Bang_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X != 'B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT, TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.NEQ,
                v,
                TERM_FACTORY.getConstantLiteral("B", XSD.STRING))), translation.get(0));
    }

    @Test
    public void not_GreaterThan_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X > 3";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT, TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.GT,
                v,
                TERM_FACTORY.getConstantLiteral("3", XSD.LONG))), translation.get(0));
    }

    @Test
    public void not_GreaterThanEquals_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X >= 3";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT, TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.GTE,
                v,
                TERM_FACTORY.getConstantLiteral("3", XSD.LONG))), translation.get(0));
    }

    @Test
    public void not_MinorThan_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X < 3";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT, TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.LT,
                v,
                TERM_FACTORY.getConstantLiteral("3", XSD.LONG))), translation.get(0));
    }

    @Test
    public void not_MinorThanEquals_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X <= 3";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT, TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.LTE,
                v,
                TERM_FACTORY.getConstantLiteral("3", XSD.LONG))), translation.get(0));
    }

    @Test
    public void in_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X IN (1, 3)";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.OR,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.EQ,
                        v,
                        TERM_FACTORY.getConstantLiteral("1", XSD.LONG)),
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.EQ,
                        v,
                        TERM_FACTORY.getConstantLiteral("3", XSD.LONG))), translation.get(0));
    }

    @Test
    public void not_In_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X NOT IN (1, 3)";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.OR,
                        TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.EQ,
                                v,
                                TERM_FACTORY.getConstantLiteral("1", XSD.LONG)),
                        TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.EQ,
                                v,
                                TERM_FACTORY.getConstantLiteral("3", XSD.LONG)))), translation.get(0));
    }

    @Ignore
    @Test
    public void in_Multi_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE (X, Y) IN ((1, 3), (2,4))";

        Variable v1 = TERM_FACTORY.getVariable("x0");
        Variable v2 = TERM_FACTORY.getVariable("y0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v1,
                new QualifiedAttributeID(null, IDFAC.createAttributeID("Y")), v2), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.OR,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.AND,
                        TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.EQ,
                                v1,
                                TERM_FACTORY.getConstantLiteral("1", XSD.LONG)),
                        TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.EQ,
                                v2,
                                TERM_FACTORY.getConstantLiteral("3", XSD.LONG))),
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.EQ,
                        v1,
                        TERM_FACTORY.getConstantLiteral("2", XSD.LONG)),
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.EQ,
                        v2,
                        TERM_FACTORY.getConstantLiteral("4", XSD.LONG))), translation.get(0));
    }


    @Test
    public void isNull_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X IS NULL";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.IS_NULL, v), translation.get(0));
    }

    @Test
    public void isNotNull_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X IS NOT NULL";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.IS_NULL, v)), translation.get(0));
    }

    @Test
    public void between_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X BETWEEN 1 AND 3";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(ImmutableList.of(// FACTORY.getFunction(ExpressionOperation.AND,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.GTE,
                        v,
                        TERM_FACTORY.getConstantLiteral("1", XSD.LONG)),
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.LTE,
                        v,
                        TERM_FACTORY.getConstantLiteral("3", XSD.LONG))), translation);
    }

    @Test
    public void not_Between_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X NOT BETWEEN 1 AND 3";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(//FACTORY.getFunction(ExpressionOperation.NOT,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.OR,
                        TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.LT,
                                v,
                                TERM_FACTORY.getConstantLiteral("1", XSD.LONG)),
                        TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.GT,
                                v,
                                TERM_FACTORY.getConstantLiteral("3", XSD.LONG))), translation.get(0));
    }

    @Test
    public void like_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X LIKE '_A%'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.SQL_LIKE,
                v,
                TERM_FACTORY.getConstantLiteral("_A%", XSD.STRING)), translation.get(0));
    }

    @Test
    public void not_Like_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X NOT LIKE '_A%'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.SQL_LIKE,
                        v,
                        TERM_FACTORY.getConstantLiteral("_A%", XSD.STRING))), translation.get(0));
    }

    @Test
    public void regexp_Match_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X ~ 'A.*B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX,
                v,
                TERM_FACTORY.getConstantLiteral("A.*B", XSD.STRING),
                TERM_FACTORY.getConstantLiteral("", XSD.STRING)), translation.get(0));
    }

    @Test
    public void regexp_NotMatch_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X !~ 'A.*B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX,
                        v,
                        TERM_FACTORY.getConstantLiteral("A.*B", XSD.STRING),
                        TERM_FACTORY.getConstantLiteral("", XSD.STRING))), translation.get(0));
    }

    @Test
    public void regexp_MatchIgnoreCase_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X ~* 'A.*B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX,
                v,
                TERM_FACTORY.getConstantLiteral("A.*B", XSD.STRING),
                TERM_FACTORY.getConstantLiteral("i", XSD.STRING)), translation.get(0));
    }

    @Test
    public void regexp_NotMatchIgnoreCase_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X !~* 'A.*B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX,
                        v,
                        TERM_FACTORY.getConstantLiteral("A.*B", XSD.STRING),
                        TERM_FACTORY.getConstantLiteral("i", XSD.STRING))), translation.get(0));
    }

    @Test
    public void not_Regexp_Match_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X ~ 'A.*B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX,
                        v,
                        TERM_FACTORY.getConstantLiteral("A.*B", XSD.STRING),
                        TERM_FACTORY.getConstantLiteral("", XSD.STRING))), translation.get(0));
    }

    @Test
    public void not_Regexp_NotMatch_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X !~ 'A.*B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX,
                v,
                TERM_FACTORY.getConstantLiteral("A.*B", XSD.STRING),
                TERM_FACTORY.getConstantLiteral("", XSD.STRING)), translation.get(0));
    }

    @Test
    public void not_Regexp_MatchIgnoreCase_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X ~* 'A.*B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX,
                        v,
                        TERM_FACTORY.getConstantLiteral("A.*B", XSD.STRING),
                        TERM_FACTORY.getConstantLiteral("i", XSD.STRING))), translation.get(0));
    }

    @Test
    public void not_Regexp_NotMatchIgnoreCase_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X !~* 'A.*B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX,
                v,
                TERM_FACTORY.getConstantLiteral("A.*B", XSD.STRING),
                TERM_FACTORY.getConstantLiteral("i", XSD.STRING)), translation.get(0));
    }


    @Test
    public void regexp_MySQL_Match_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X REGEXP BINARY 'A.*B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX,
                v,
                TERM_FACTORY.getConstantLiteral("A.*B", XSD.STRING),
                TERM_FACTORY.getConstantLiteral("", XSD.STRING)), translation.get(0));
    }

    @Test
    public void regexp_MySQL_MatchIgnoreCase_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X REGEXP 'A.*B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX,
                v,
                TERM_FACTORY.getConstantLiteral("A.*B", XSD.STRING),
                TERM_FACTORY.getConstantLiteral("i", XSD.STRING)), translation.get(0));
    }

    @Test
    public void not_Regexp_MySQL_Match_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X REGEXP BINARY 'A.*B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX,
                        v,
                        TERM_FACTORY.getConstantLiteral("A.*B", XSD.STRING),
                        TERM_FACTORY.getConstantLiteral("", XSD.STRING))), translation.get(0));
    }

    @Test
    public void not_Regexp_MySQL_MatchIgnoreCase_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X REGEXP 'A.*B'";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX,
                        v,
                        TERM_FACTORY.getConstantLiteral("A.*B", XSD.STRING),
                        TERM_FACTORY.getConstantLiteral("i", XSD.STRING))), translation.get(0));
    }

    @Test
    public void and_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X >= 1 AND X <= 3";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(ImmutableList.of( //FACTORY.getFunction(ExpressionOperation.AND,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.GTE,
                        v,
                        TERM_FACTORY.getConstantLiteral("1", XSD.LONG)),
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.LTE,
                        v,
                        TERM_FACTORY.getConstantLiteral("3", XSD.LONG))), translation);
    }

    @Test
    public void or_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X < 1 OR X > 3";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.OR,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.LT,
                        v,
                        TERM_FACTORY.getConstantLiteral("1", XSD.LONG)),
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.GT,
                        v,
                        TERM_FACTORY.getConstantLiteral("3", XSD.LONG))), translation.get(0));
    }

    @Test
    public void parenthesis_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE (X >= 1)";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.GTE,
                v,
                TERM_FACTORY.getConstantLiteral("1", XSD.LONG)), translation.get(0));
    }

    @Test
    public void not_Parenthesis_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT (X >= 1)";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.NOT,
                TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.GTE,
                        v,
                        TERM_FACTORY.getConstantLiteral("1", XSD.LONG))), translation.get(0));
    }

    @Test
    public void sign_Plus_Test() throws JSQLParserException {
        String sql = "SELECT +X  AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(v, translation);
    }

    @Test
    public void sign_Minus_Test() throws JSQLParserException {
        String sql = "SELECT -X  AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.MINUS, v), translation);
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void case_When_Test() throws JSQLParserException {
        String sql = "SELECT CASE A WHEN 1 THEN 3 ELSE 4 END FROM DUMMY;";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void subSelect_Test() throws JSQLParserException {
        String sql = "SELECT (SELECT A FROM Q WHERE A = P.B) AS C FROM P;";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void exists_Test() throws JSQLParserException {
        String sql = "SELECT * FROM P WHERE EXISTS (SELECT * FROM Q WHERE A = P.B);";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v), TERM_FACTORY, TYPE_FACTORY);

        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void not_Exists_Test() throws JSQLParserException {
        String sql = "SELECT * FROM P WHERE NOT EXISTS (SELECT * FROM Q WHERE A = P.B);";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v), TERM_FACTORY, TYPE_FACTORY);

        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void allComparison_Test() throws JSQLParserException {
        String sql = "SELECT * FROM P WHERE A > ALL (SELECT C FROM Q WHERE A = P.B);";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v), TERM_FACTORY, TYPE_FACTORY);

        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void anyComparison_Test() throws JSQLParserException {
        String sql = "SELECT * FROM P WHERE A > ANY (SELECT C FROM Q WHERE A = P.B);";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v), TERM_FACTORY, TYPE_FACTORY);

        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void bitwiseAnd_Test() throws JSQLParserException {
        String sql = "SELECT X & Y AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");
        Variable u = TERM_FACTORY.getVariable("y0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v,
                new QualifiedAttributeID(null, IDFAC.createAttributeID("Y")), u), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void bitwiseOr_Test() throws JSQLParserException {
        String sql = "SELECT X | Y AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");
        Variable u = TERM_FACTORY.getVariable("y0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v,
                new QualifiedAttributeID(null, IDFAC.createAttributeID("Y")), u), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void bitwiseXor_Test() throws JSQLParserException {
        String sql = "SELECT X ^ Y AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");
        Variable u = TERM_FACTORY.getVariable("y0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v,
                new QualifiedAttributeID(null, IDFAC.createAttributeID("Y")), u), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void extract_Test() throws JSQLParserException {
        String sql = "SELECT EXTRACT(MONTH FROM CURRENT_DATE) AS C FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void interval_Test() throws JSQLParserException {
        String sql = "SELECT INTERVAL '31' DAY FROM DUMMY;";

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void analyticExpression_Test() throws JSQLParserException {
        String sql = "SELECT LAG(A) OVER () FROM P;";

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void jsonExpression_Test() throws JSQLParserException {
        String sql = "SELECT A->'B' FROM DUMMY;";

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test(expected = InvalidSelectQueryRuntimeException.class)
    public void jdbcParameter_Test() throws JSQLParserException {
        String sql = "SELECT A FROM P WHERE B = ?;";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("B")), v), TERM_FACTORY, TYPE_FACTORY);

        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));
    }

    @Test(expected = InvalidSelectQueryRuntimeException.class)
    public void jdbcNamedParameter_Test() throws JSQLParserException {
        String sql = "SELECT A FROM P WHERE B = :name;";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("B")), v), TERM_FACTORY, TYPE_FACTORY);

        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void oracle_OuterJoin_Test() throws JSQLParserException {
        String sql = "SELECT * FROM P, Q WHERE P.A = Q.A(+)";

        Variable v = TERM_FACTORY.getVariable("x0");
        Variable u = TERM_FACTORY.getVariable("y0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(IDFAC.createRelationID(null,"P"), IDFAC.createAttributeID("A")), v,
                new QualifiedAttributeID(IDFAC.createRelationID(null, "Q"), IDFAC.createAttributeID("A")), u), TERM_FACTORY, TYPE_FACTORY);

        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));
    }

    @Test
    public void true_column_Test() throws JSQLParserException {
        String sql = "SELECT * FROM P WHERE A = true";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v), TERM_FACTORY, TYPE_FACTORY);

        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.EQ,
                v,
                TERM_FACTORY.getBooleanConstant(true)), translation.get(0));
    }

    @Test
    public void false_column_Test() throws JSQLParserException {
        String sql = "SELECT * FROM P WHERE A = false";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v), TERM_FACTORY, TYPE_FACTORY);

        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                ExpressionOperation.EQ,
                v,
                TERM_FACTORY.getBooleanConstant(false)), translation.get(0));
    }

    @Test
    public void function_REGEXP_LIKE_Test() throws JSQLParserException {
        String sql = "SELECT X FROM DUMMY WHERE REGEXP_LIKE(X, '^Ste(v|ph)en$')";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX, v,
                TERM_FACTORY.getConstantLiteral("^Ste(v|ph)en$"),
                TERM_FACTORY.getConstantLiteral("")), translation.get(0));
    }

    @Test
    public void function_REGEXP_LIKE_3_Test() throws JSQLParserException {
        String sql = "SELECT X FROM DUMMY WHERE REGEXP_LIKE(X, '^Ste(v|ph)en$', 'i')";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REGEX, v,
                TERM_FACTORY.getConstantLiteral("^Ste(v|ph)en$"),
                TERM_FACTORY.getConstantLiteral("i")), translation.get(0));
    }

    @Test(expected = InvalidSelectQueryRuntimeException.class)
    public void function_REGEXP_LIKE_4_Test() throws JSQLParserException {
        String sql = "SELECT X FROM DUMMY WHERE REGEXP_LIKE(X, '^Ste(v|ph)en$', 'i', '')";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(getWhereExpression(sql));
    }

    @Test
    public void function_REGEXP_REPLACE_Test() throws JSQLParserException {
        String sql = "SELECT REGEXP_REPLACE(X, '^Ste(v|ph)en$', '')  AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REPLACE, v,
                TERM_FACTORY.getConstantLiteral("^Ste(v|ph)en$"),
                TERM_FACTORY.getConstantLiteral(""),
                TERM_FACTORY.getConstantLiteral("")), translation);
    }

    @Test
    public void function_REGEXP_REPLACE_4_Test() throws JSQLParserException {
        String sql = "SELECT REGEXP_REPLACE(X, '^Ste(v|ph)en$', '', 'i')  AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REPLACE, v,
                TERM_FACTORY.getConstantLiteral("^Ste(v|ph)en$"),
                TERM_FACTORY.getConstantLiteral(""),
                TERM_FACTORY.getConstantLiteral("i")), translation);
    }

    @Test
    public void function_REGEXP_REPLACE_6_Test() throws JSQLParserException {
        String sql = "SELECT REGEXP_REPLACE(X, '^Ste(v|ph)en$', '', 1, 0, 'i')  AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REPLACE, v,
                TERM_FACTORY.getConstantLiteral("^Ste(v|ph)en$"),
                TERM_FACTORY.getConstantLiteral(""),
                TERM_FACTORY.getConstantLiteral("i")), translation);
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void function_REGEXP_REPLACE_6a_Test() throws JSQLParserException {
        String sql = "SELECT REGEXP_REPLACE(X, '^Ste(v|ph)en$', '', 2, 0, 'i')  AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void function_REGEXP_REPLACE_7_Test() throws JSQLParserException {
        String sql = "SELECT REGEXP_REPLACE(X, '^Ste(v|ph)en$', '', 1, 0, 'i', '')  AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test
    public void function_REPLACE_Test() throws JSQLParserException {
        String sql = "SELECT REPLACE(X,'J') AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REPLACE, v,
                TERM_FACTORY.getConstantLiteral("J"),
                TERM_FACTORY.getConstantLiteral(""),
                TERM_FACTORY.getConstantLiteral("")), translation);
    }

    @Test
    public void function_REPLACE_3_Test() throws JSQLParserException {
        String sql = "SELECT REPLACE(X, 'J', 'BL')  AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.REPLACE, v,
                TERM_FACTORY.getConstantLiteral("J"),
                TERM_FACTORY.getConstantLiteral("BL"),
                TERM_FACTORY.getConstantLiteral("")), translation);
    }

    @Test(expected = InvalidSelectQueryRuntimeException.class)
    public void function_REPLACE_4_Test() throws JSQLParserException {
        String sql = "SELECT REPLACE(X, 'J', 'BL', 'i')  AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test
    public void function_SUBSTR_2_Test() throws JSQLParserException {
        String sql = "SELECT SUBSTR(X, 1) AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.SUBSTR2, v,
                TERM_FACTORY.getConstantLiteral("1", XSD.LONG)), translation);
    }

    @Test
    public void function_SUBSTR_3_Test() throws JSQLParserException {
        String sql = "SELECT SUBSTR(X, 1, 2) AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.SUBSTR3, v,
                TERM_FACTORY.getConstantLiteral("1", XSD.LONG),
                TERM_FACTORY.getConstantLiteral("2", XSD.LONG)), translation);
    }

    @Test (expected = JSQLParserException.class)
    public void function_SQL_SUBSTRING_Test() throws JSQLParserException {
        // SQL:99 SUBSTRING cannot be supported because of JSQLParser
        String sql = "SELECT SUBSTRING(X FROM 1 FOR 2) AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test
    public void function_SUBSTRING_2_Test() throws JSQLParserException {
        String sql = "SELECT SUBSTRING(X, 1) AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.SUBSTR2, v,
                TERM_FACTORY.getConstantLiteral("1", XSD.LONG)), translation);
    }

    @Test
    public void function_SUBSTRING_3_Test() throws JSQLParserException {
        String sql = "SELECT SUBSTRING(X, 1, 2) AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.SUBSTR3, v,
                TERM_FACTORY.getConstantLiteral("1", XSD.LONG),
                TERM_FACTORY.getConstantLiteral("2", XSD.LONG)), translation);
    }

    @Test
    public void function_LCASE_Test() throws JSQLParserException {
        String sql = "SELECT LCASE(X) AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.LCASE, v), translation);
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void function_LCASE_2_Test() throws JSQLParserException {
        String sql = "SELECT LCASE(X, 'A') AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test
    public void function_LOWER_Test() throws JSQLParserException {
        String sql = "SELECT LOWER(X) AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.LCASE, v), translation);
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void function_LOWER_2_Test() throws JSQLParserException {
        String sql = "SELECT LOWER(X, 'A') AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test
    public void function_UCASE_Test() throws JSQLParserException {
        String sql = "SELECT UCASE(X) AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.UCASE, v), translation);
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void function_UCASE_2_Test() throws JSQLParserException {
        String sql = "SELECT UCASE(X, 'A') AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test
    public void function_UPPER_Test() throws JSQLParserException {
        String sql = "SELECT UPPER(X) AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.UCASE, v), translation);
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void function_UPPER_2_Test() throws JSQLParserException {
        String sql = "SELECT UPPER(X, 'A') AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test
    public void function_LENGTH_Test() throws JSQLParserException {
        String sql = "SELECT LENGTH(X) AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));

        System.out.println(translation);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(ExpressionOperation.STRLEN, v), translation);
    }

    @Test(expected = InvalidSelectQueryRuntimeException.class)
    public void function_LENGTH_2_Test() throws JSQLParserException {
        String sql = "SELECT LENGTH(X, 'A') AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void function_LEN_Test() throws JSQLParserException {
        String sql = "SELECT LEN(X) AS A FROM DUMMY";

        Variable v = TERM_FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v), TERM_FACTORY, TYPE_FACTORY);
        ImmutableTerm translation = parser.parseTerm(getExpression(sql));
    }




    private net.sf.jsqlparser.expression.Expression getExpression(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        SelectItem si = ((PlainSelect) ((Select) statement).getSelectBody()).getSelectItems().get(0);
        return ((SelectExpressionItem) si).getExpression();
    }

    private net.sf.jsqlparser.expression.Expression getWhereExpression(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        return ((PlainSelect) ((Select) statement).getSelectBody()).getWhere();
    }
}
