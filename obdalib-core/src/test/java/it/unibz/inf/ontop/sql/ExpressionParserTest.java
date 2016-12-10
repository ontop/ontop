package it.unibz.inf.ontop.sql;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.parser.ExpressionParser;
import it.unibz.inf.ontop.sql.parser.exceptions.InvalidSelectQueryException;
import it.unibz.inf.ontop.sql.parser.exceptions.UnsupportedSelectQueryException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.model.Predicate.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Roman Kontchakov on 02/12/2016.
 */
public class ExpressionParserTest {

    private OBDADataFactory FACTORY;
    private DBMetadata METADATA;
    private QuotedIDFactory IDFAC;

    @Before
    public void beforeEachTest() {
        FACTORY = OBDADataFactoryImpl.getInstance();
        METADATA = DBMetadataExtractor.createDummyMetadata();
        IDFAC = METADATA.getQuotedIDFactory();
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void null_Test() throws JSQLParserException {
        String sql = "SELECT NULL AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of());
    }

    @Test
    public void double_Test() throws JSQLParserException {
        String sql = "SELECT 1.0 AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of());

        System.out.println(translation);

        assertEquals(FACTORY.getConstantLiteral("1.0", COL_TYPE.DOUBLE), translation);
    }

    @Test
    public void long_Test() throws JSQLParserException {
        String sql = "SELECT 1 AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of());

        System.out.println(translation);

        assertEquals(FACTORY.getConstantLiteral("1", COL_TYPE.LONG), translation);
    }

    @Test
    public void string_Test() throws JSQLParserException {
        String sql = "SELECT \'1\' AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of());

        System.out.println(translation);

        assertEquals(FACTORY.getConstantLiteral("1", COL_TYPE.STRING), translation);
    }

    @Test
    public void date_Test() throws JSQLParserException {
        //  ODBC escape sequence syntax
        String sql = "SELECT {d '2016-12-02'} AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of());

        System.out.println(translation);

        assertEquals(FACTORY.getConstantLiteral("2016-12-02", COL_TYPE.DATE), translation);
    }

    @Test
    public void time_Test() throws JSQLParserException {
        //  ODBC escape sequence syntax
        String sql = "SELECT {t '15:57:02'} AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of());

        System.out.println(translation);

        assertEquals(FACTORY.getConstantLiteral("15:57:02", COL_TYPE.TIME), translation);
    }

    @Test
    public void timestamp_Test() throws JSQLParserException {
        //  ODBC escape sequence syntax
        String sql = "SELECT {ts '2016-12-02 15:57:02.03'} AS A FROM DUMMY";

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of());

        System.out.println(translation);

        assertEquals(FACTORY.getConstantLiteral("2016-12-02 15:57:02.03", COL_TYPE.DATETIME_STAMP), translation);
    }

    @Test
    public void addition_Test() throws JSQLParserException {
        String sql = "SELECT X + 1 AS A FROM DUMMY";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(
                ExpressionOperation.ADD,
                v,
                FACTORY.getConstantLiteral("1", COL_TYPE.LONG)), translation);
    }

    @Test
    public void subtraction_Test() throws JSQLParserException {
        String sql = "SELECT X - 1 AS A FROM DUMMY";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(
                ExpressionOperation.SUBTRACT,
                v,
                FACTORY.getConstantLiteral("1", COL_TYPE.LONG)), translation);
    }

    @Test
    public void multiplication_Test() throws JSQLParserException {
        String sql = "SELECT X * 2 AS A FROM DUMMY";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(
                ExpressionOperation.MULTIPLY,
                v,
                FACTORY.getConstantLiteral("2", COL_TYPE.LONG)), translation);
    }

    @Test
    public void division_Test() throws JSQLParserException {
        String sql = "SELECT X / 2 AS A FROM DUMMY";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(
                ExpressionOperation.DIVIDE,
                v,
                FACTORY.getConstantLiteral("2", COL_TYPE.LONG)), translation);
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void modulo_Test() throws JSQLParserException {
        String sql = "SELECT X % 2 AS A FROM DUMMY";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

//        assertEquals(FACTORY.getFunction(
//                ExpressionOperation.DIVIDE,
//                v,
//                FACTORY.getConstantLiteral("2", COL_TYPE.LONG)), translation);
    }

    @Test
    public void concat_Test() throws JSQLParserException {
        String sql = "SELECT X || 'B' AS A FROM DUMMY";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(
                ExpressionOperation.CONCAT,
                v,
                FACTORY.getConstantLiteral("B", COL_TYPE.STRING)), translation);
    }

    @Test
    public void concat_2_Test() throws JSQLParserException {
        String sql = "SELECT CONCAT('A', X, 'B') FROM DUMMY";
        Variable v = FACTORY.getVariable("x0");
        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));


        assertEquals(FACTORY.getFunction(
                ExpressionOperation.CONCAT, FACTORY.getConstantLiteral("A", COL_TYPE.STRING),
                FACTORY.getFunction(
                        ExpressionOperation.CONCAT,
                        v,
                        FACTORY.getConstantLiteral("B", COL_TYPE.STRING))), translation);

    }

    // Boolean expressions are not allowed in the SELECT clause
    // so, the tests below depend on the WHERE clause

    @Test
    public void equalsTo_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X = 'B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(
                ExpressionOperation.EQ,
                v,
                FACTORY.getConstantLiteral("B", COL_TYPE.STRING)), translation);
    }

    @Test
    public void notEqualsTo_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X <> 'B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(
                ExpressionOperation.NEQ,
                v,
                FACTORY.getConstantLiteral("B", COL_TYPE.STRING)), translation);
    }

    @Test
    public void notEqualsTo_Bang_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X != 'B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(
                ExpressionOperation.NEQ,
                v,
                FACTORY.getConstantLiteral("B", COL_TYPE.STRING)), translation);
    }

    @Test
    public void greaterThan_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X > 3";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(
                ExpressionOperation.GT,
                v,
                FACTORY.getConstantLiteral("3", COL_TYPE.LONG)), translation);
    }

    @Test
    public void greaterThanEquals_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X >= 3";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(
                ExpressionOperation.GTE,
                v,
                FACTORY.getConstantLiteral("3", COL_TYPE.LONG)), translation);
    }

    @Test
    public void minorThan_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X < 3";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(
                ExpressionOperation.LT,
                v,
                FACTORY.getConstantLiteral("3", COL_TYPE.LONG)), translation);
    }

    @Test
    public void minorThanEquals_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X <= 3";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(
                ExpressionOperation.LTE,
                v,
                FACTORY.getConstantLiteral("3", COL_TYPE.LONG)), translation);
    }

    @Test
    public void not_EqualsTo_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X = 'B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT, FACTORY.getFunction(
                ExpressionOperation.EQ,
                v,
                FACTORY.getConstantLiteral("B", COL_TYPE.STRING))), translation);
    }

    @Test
    public void not_NotEqualsTo_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X <> 'B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT, FACTORY.getFunction(
                ExpressionOperation.NEQ,
                v,
                FACTORY.getConstantLiteral("B", COL_TYPE.STRING))), translation);
    }

    @Test
    public void not_NotEqualsTo_Bang_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X != 'B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT, FACTORY.getFunction(
                ExpressionOperation.NEQ,
                v,
                FACTORY.getConstantLiteral("B", COL_TYPE.STRING))), translation);
    }

    @Test
    public void not_GreaterThan_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X > 3";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT, FACTORY.getFunction(
                ExpressionOperation.GT,
                v,
                FACTORY.getConstantLiteral("3", COL_TYPE.LONG))), translation);
    }

    @Test
    public void not_GreaterThanEquals_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X >= 3";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT, FACTORY.getFunction(
                ExpressionOperation.GTE,
                v,
                FACTORY.getConstantLiteral("3", COL_TYPE.LONG))), translation);
    }

    @Test
    public void not_MinorThan_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X < 3";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT, FACTORY.getFunction(
                ExpressionOperation.LT,
                v,
                FACTORY.getConstantLiteral("3", COL_TYPE.LONG))), translation);
    }

    @Test
    public void not_MinorThanEquals_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X <= 3";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT, FACTORY.getFunction(
                ExpressionOperation.LTE,
                v,
                FACTORY.getConstantLiteral("3", COL_TYPE.LONG))), translation);
    }

    @Test
    public void in_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X IN (1, 3)";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.OR,
                FACTORY.getFunction(ExpressionOperation.EQ,
                        v,
                        FACTORY.getConstantLiteral("1", COL_TYPE.LONG)),
                FACTORY.getFunction(ExpressionOperation.EQ,
                        v,
                        FACTORY.getConstantLiteral("3", COL_TYPE.LONG))), translation);
    }

    @Test
    public void not_In_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X NOT IN (1, 3)";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT,
                FACTORY.getFunction(ExpressionOperation.OR,
                        FACTORY.getFunction(ExpressionOperation.EQ,
                                v,
                                FACTORY.getConstantLiteral("1", COL_TYPE.LONG)),
                        FACTORY.getFunction(ExpressionOperation.EQ,
                                v,
                                FACTORY.getConstantLiteral("3", COL_TYPE.LONG)))), translation);
    }

    @Ignore
    @Test
    public void in_Multi_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE (X, Y) IN ((1, 3), (2,4))";

        Variable v1 = FACTORY.getVariable("x0");
        Variable v2 = FACTORY.getVariable("y0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v1,
                new QualifiedAttributeID(null, IDFAC.createAttributeID("Y")), v2));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.OR,
                FACTORY.getFunction(ExpressionOperation.AND,
                        FACTORY.getFunction(ExpressionOperation.EQ,
                                v1,
                                FACTORY.getConstantLiteral("1", COL_TYPE.LONG)),
                        FACTORY.getFunction(ExpressionOperation.EQ,
                                v2,
                                FACTORY.getConstantLiteral("3", COL_TYPE.LONG))),
                FACTORY.getFunction(ExpressionOperation.EQ,
                        v1,
                        FACTORY.getConstantLiteral("2", COL_TYPE.LONG)),
                FACTORY.getFunction(ExpressionOperation.EQ,
                        v2,
                        FACTORY.getConstantLiteral("4", COL_TYPE.LONG))), translation);
    }


    @Test
    public void isNull_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X IS NULL";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.IS_NULL, v), translation);
    }

    @Test
    public void isNotNull_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X IS NOT NULL";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT,
                FACTORY.getFunction(ExpressionOperation.IS_NULL, v)), translation);
    }

    @Test
    public void between_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X BETWEEN 1 AND 3";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.AND,
                FACTORY.getFunction(ExpressionOperation.GTE,
                        v,
                        FACTORY.getConstantLiteral("1", COL_TYPE.LONG)),
                FACTORY.getFunction(ExpressionOperation.LTE,
                        v,
                        FACTORY.getConstantLiteral("3", COL_TYPE.LONG))), translation);
    }

    @Test
    public void not_Between_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X NOT BETWEEN 1 AND 3";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT,
                FACTORY.getFunction(ExpressionOperation.AND,
                        FACTORY.getFunction(ExpressionOperation.GTE,
                                v,
                                FACTORY.getConstantLiteral("1", COL_TYPE.LONG)),
                        FACTORY.getFunction(ExpressionOperation.LTE,
                                v,
                                FACTORY.getConstantLiteral("3", COL_TYPE.LONG)))), translation);
    }

    @Test
    public void like_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X LIKE '_A%'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.SQL_LIKE,
                v,
                FACTORY.getConstantLiteral("_A%", COL_TYPE.STRING)), translation);
    }

    @Test
    public void not_Like_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X NOT LIKE '_A%'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT,
                FACTORY.getFunction(ExpressionOperation.SQL_LIKE,
                        v,
                        FACTORY.getConstantLiteral("_A%", COL_TYPE.STRING))), translation);
    }

    @Test
    public void regexp_Match_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X ~ 'A.*B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.REGEX,
                v,
                FACTORY.getConstantLiteral("A.*B", COL_TYPE.STRING),
                FACTORY.getConstantLiteral("", COL_TYPE.STRING)), translation);
    }

    @Test
    public void regexp_NotMatch_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X !~ 'A.*B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT,
                FACTORY.getFunction(ExpressionOperation.REGEX,
                        v,
                        FACTORY.getConstantLiteral("A.*B", COL_TYPE.STRING),
                        FACTORY.getConstantLiteral("", COL_TYPE.STRING))), translation);
    }

    @Test
    public void regexp_MatchIgnoreCase_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X ~* 'A.*B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.REGEX,
                v,
                FACTORY.getConstantLiteral("A.*B", COL_TYPE.STRING),
                FACTORY.getConstantLiteral("i", COL_TYPE.STRING)), translation);
    }

    @Test
    public void regexp_NotMatchIgnoreCase_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X !~* 'A.*B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT,
                FACTORY.getFunction(ExpressionOperation.REGEX,
                        v,
                        FACTORY.getConstantLiteral("A.*B", COL_TYPE.STRING),
                        FACTORY.getConstantLiteral("i", COL_TYPE.STRING))), translation);
    }

    @Test
    public void not_Regexp_Match_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X ~ 'A.*B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT,
                FACTORY.getFunction(ExpressionOperation.REGEX,
                        v,
                        FACTORY.getConstantLiteral("A.*B", COL_TYPE.STRING),
                        FACTORY.getConstantLiteral("", COL_TYPE.STRING))), translation);
    }

    @Test
    public void not_Regexp_NotMatch_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X !~ 'A.*B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.REGEX,
                v,
                FACTORY.getConstantLiteral("A.*B", COL_TYPE.STRING),
                FACTORY.getConstantLiteral("", COL_TYPE.STRING)), translation);
    }

    @Test
    public void not_Regexp_MatchIgnoreCase_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X ~* 'A.*B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT,
                FACTORY.getFunction(ExpressionOperation.REGEX,
                        v,
                        FACTORY.getConstantLiteral("A.*B", COL_TYPE.STRING),
                        FACTORY.getConstantLiteral("i", COL_TYPE.STRING))), translation);
    }

    @Test
    public void not_Regexp_NotMatchIgnoreCase_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X !~* 'A.*B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.REGEX,
                v,
                FACTORY.getConstantLiteral("A.*B", COL_TYPE.STRING),
                FACTORY.getConstantLiteral("i", COL_TYPE.STRING)), translation);
    }


    @Test
    public void regexp_MySQL_Match_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X REGEXP BINARY 'A.*B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.REGEX,
                v,
                FACTORY.getConstantLiteral("A.*B", COL_TYPE.STRING),
                FACTORY.getConstantLiteral("", COL_TYPE.STRING)), translation);
    }

    @Test
    public void regexp_MySQL_MatchIgnoreCase_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X REGEXP 'A.*B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.REGEX,
                v,
                FACTORY.getConstantLiteral("A.*B", COL_TYPE.STRING),
                FACTORY.getConstantLiteral("i", COL_TYPE.STRING)), translation);
    }

    @Test
    public void not_Regexp_MySQL_Match_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X REGEXP BINARY 'A.*B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT,
                FACTORY.getFunction(ExpressionOperation.REGEX,
                        v,
                        FACTORY.getConstantLiteral("A.*B", COL_TYPE.STRING),
                        FACTORY.getConstantLiteral("", COL_TYPE.STRING))), translation);
    }

    @Test
    public void not_Regexp_MySQL_MatchIgnoreCase_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT X REGEXP 'A.*B'";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT,
                FACTORY.getFunction(ExpressionOperation.REGEX,
                        v,
                        FACTORY.getConstantLiteral("A.*B", COL_TYPE.STRING),
                        FACTORY.getConstantLiteral("i", COL_TYPE.STRING))), translation);
    }

    @Test
    public void and_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X >= 1 AND X <= 3";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.AND,
                FACTORY.getFunction(ExpressionOperation.GTE,
                        v,
                        FACTORY.getConstantLiteral("1", COL_TYPE.LONG)),
                FACTORY.getFunction(ExpressionOperation.LTE,
                        v,
                        FACTORY.getConstantLiteral("3", COL_TYPE.LONG))), translation);
    }

    @Test
    public void or_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE X < 1 OR X > 3";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.OR,
                FACTORY.getFunction(ExpressionOperation.LT,
                        v,
                        FACTORY.getConstantLiteral("1", COL_TYPE.LONG)),
                FACTORY.getFunction(ExpressionOperation.GT,
                        v,
                        FACTORY.getConstantLiteral("3", COL_TYPE.LONG))), translation);
    }

    @Test
    public void parenthesis_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE (X >= 1)";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.GTE,
                v,
                FACTORY.getConstantLiteral("1", COL_TYPE.LONG)), translation);
    }

    @Test
    public void not_Parenthesis_Test() throws JSQLParserException {
        String sql = "SELECT X AS A FROM DUMMY WHERE NOT (X >= 1)";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.NOT,
                FACTORY.getFunction(ExpressionOperation.GTE,
                        v,
                        FACTORY.getConstantLiteral("1", COL_TYPE.LONG))), translation);
    }

    @Test
    public void sign_Plus_Test() throws JSQLParserException {
        String sql = "SELECT +X  AS A FROM DUMMY";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(v, translation);
    }

    @Test
    public void sign_Minus_Test() throws JSQLParserException {
        String sql = "SELECT -X  AS A FROM DUMMY";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        System.out.println(translation);

        assertEquals(FACTORY.getFunction(ExpressionOperation.MINUS, v), translation);
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void case_When_Test() throws JSQLParserException {
        String sql = "SELECT CASE A WHEN 1 THEN 3 ELSE 4 END FROM DUMMY;";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));

        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void subSelect_Test() throws JSQLParserException {
        String sql = "SELECT (SELECT A FROM Q WHERE A = P.B) AS C FROM P;";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));

        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void exists_Test() throws JSQLParserException {
        String sql = "SELECT * FROM P WHERE EXISTS (SELECT * FROM Q WHERE A = P.B);";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));

        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void not_Exists_Test() throws JSQLParserException {
        String sql = "SELECT * FROM P WHERE NOT EXISTS (SELECT * FROM Q WHERE A = P.B);";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));

        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void allComparison_Test() throws JSQLParserException {
        String sql = "SELECT * FROM P WHERE A > ALL (SELECT C FROM Q WHERE A = P.B);";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));

        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void anyComparison_Test() throws JSQLParserException {
        String sql = "SELECT * FROM P WHERE A > ANY (SELECT C FROM Q WHERE A = P.B);";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));

        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void bitwiseAnd_Test() throws JSQLParserException {
        String sql = "SELECT X & Y AS A FROM DUMMY";

        Variable v = FACTORY.getVariable("x0");
        Variable u = FACTORY.getVariable("y0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v,
                new QualifiedAttributeID(null, IDFAC.createAttributeID("Y")), u));
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void bitwiseOr_Test() throws JSQLParserException {
        String sql = "SELECT X | Y AS A FROM DUMMY";

        Variable v = FACTORY.getVariable("x0");
        Variable u = FACTORY.getVariable("y0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v,
                new QualifiedAttributeID(null, IDFAC.createAttributeID("Y")), u));
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void bitwiseXor_Test() throws JSQLParserException {
        String sql = "SELECT X ^ Y AS A FROM DUMMY";

        Variable v = FACTORY.getVariable("x0");
        Variable u = FACTORY.getVariable("y0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v,
                new QualifiedAttributeID(null, IDFAC.createAttributeID("Y")), u));
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void extract_Test() throws JSQLParserException {
        String sql = "SELECT EXTRACT(MONTH FROM CURRENT_DATE) AS C FROM DUMMY";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void interval_Test() throws JSQLParserException {
        String sql = "SELECT INTERVAL '31' DAY FROM DUMMY;";

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of());
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void analyticExpression_Test() throws JSQLParserException {
        String sql = "SELECT LAG(A) OVER () FROM P;";

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of());
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void jsonExpression_Test() throws JSQLParserException {
        String sql = "SELECT A->'B' FROM DUMMY;";

        ExpressionParser parser = new ExpressionParser(IDFAC, getExpression(sql));
        Term translation = parser.apply(ImmutableMap.of());
    }

    @Test(expected = InvalidSelectQueryException.class)
    public void jdbcParameter_Test() throws JSQLParserException {
        String sql = "SELECT A FROM P WHERE B = ?;";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));

        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("B")), v));
    }

    @Test(expected = InvalidSelectQueryException.class)
    public void jdbcNamedParameter_Test() throws JSQLParserException {
        String sql = "SELECT A FROM P WHERE B = :name;";

        Variable v = FACTORY.getVariable("x0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));

        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("B")), v));
    }

    @Test(expected = UnsupportedSelectQueryException.class)
    public void oracle_OuterJoin_Test() throws JSQLParserException {
        String sql = "SELECT * FROM P, Q WHERE P.A = Q.A(+)";

        Variable v = FACTORY.getVariable("x0");
        Variable u = FACTORY.getVariable("y0");

        ExpressionParser parser = new ExpressionParser(IDFAC, getWhereExpression(sql));

        Term translation = parser.apply(ImmutableMap.of(
                new QualifiedAttributeID(IDFAC.createRelationID(null,"P"), IDFAC.createAttributeID("A")), v,
                new QualifiedAttributeID(IDFAC.createRelationID(null, "Q"), IDFAC.createAttributeID("A")), u));
    }


    private Expression getExpression(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        SelectItem si = ((PlainSelect) ((Select) statement).getSelectBody()).getSelectItems().get(0);
        return ((SelectExpressionItem) si).getExpression();
    }

    private Expression getWhereExpression(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        return ((PlainSelect) ((Select) statement).getSelectBody()).getWhere();
    }
}
