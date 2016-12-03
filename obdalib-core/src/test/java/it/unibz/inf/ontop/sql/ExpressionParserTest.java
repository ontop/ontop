package it.unibz.inf.ontop.sql;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.parser.ExpressionParser;
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
    public void beforeEachTest(){
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

    private Expression getExpression(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        SelectItem si = ((PlainSelect)((Select)statement).getSelectBody()).getSelectItems().get(0);
        return ((SelectExpressionItem)si).getExpression();
    }

    private Expression getWhereExpression(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        return ((PlainSelect)((Select)statement).getSelectBody()).getWhere();
    }
}
