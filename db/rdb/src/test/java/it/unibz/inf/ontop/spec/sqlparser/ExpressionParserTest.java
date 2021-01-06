package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.spec.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryRuntimeException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Roman Kontchakov on 02/12/2016.
 */
public class ExpressionParserTest {

    private static final DBBooleanFunctionSymbol NOT = SQLTestingTools.DB_FS_FACTORY.getDBNot();

    private static final QuotedIDFactory IDFAC;
    private static final DBTypeFactory DB_TYPE_FACTORY;
    private static final DBTermType dbLongType;

    static {
        OfflineMetadataProviderBuilder builder = SQLTestingTools.createMetadataProviderBuilder();
        IDFAC = builder.getQuotedIDFactory();
        DB_TYPE_FACTORY = builder.getDBTypeFactory();
        dbLongType = DB_TYPE_FACTORY.getDBLargeIntegerType();
    }

    @Test
    public void null_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT NULL AS A FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getNullConstant(), translation);
    }

    @Test
    public void double_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT 1.0 AS A FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBConstant("1.0", DB_TYPE_FACTORY.getDBDoubleType()), translation);
    }

    @Test
    public void hex_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT 0xFF AS A FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBConstant("255", dbLongType), translation);
    }

    @Test
    public void hex_quote_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT x'FF' AS A FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBConstant("255", dbLongType), translation);
    }

    @Test
    public void hex_quote2_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT X'FF' AS A FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBConstant("255", dbLongType), translation);
    }

    @Test
    public void long_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT 1 AS A FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType), translation);
    }

    @Test
    public void string_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT '1' AS A FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBStringConstant("1"), translation);
    }

    @Test
    public void date_test() throws JSQLParserException {
        //  ODBC escape sequence syntax
        ImmutableTerm translation = parseTerm("SELECT {d '2016-12-02'} AS A FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBConstant("2016-12-02", DB_TYPE_FACTORY.getDBDateType()), translation);
    }

    @Test
    public void time_test() throws JSQLParserException {
        //  ODBC escape sequence syntax
        ImmutableTerm translation = parseTerm("SELECT {t '15:57:02'} AS A FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBConstant("15:57:02", DB_TYPE_FACTORY.getDBTimeType()), translation);
    }

    @Test
    public void timestamp_test() throws JSQLParserException {
        //  ODBC escape sequence syntax
        ImmutableTerm translation = parseTerm("SELECT {ts '2016-12-02 15:57:02.03'} AS A FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBConstant("2016-12-02 15:57:02.03",
                DB_TYPE_FACTORY.getDBDateTimestampType()), translation);
    }


    @Test
    public void date_literal_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT DATE '1998-03-07' FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBConstant("1998-03-07", DB_TYPE_FACTORY.getDBDateType()), translation);
    }

    @Test
    public void time_literal_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT TIME '15:57:02' FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBConstant("15:57:02", DB_TYPE_FACTORY.getDBTimeType()), translation);
    }

    @Test
    public void timestamp_literal_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT TIMESTAMP '2016-12-02 15:57:02.03' FROM DUMMY", ImmutableMap.of());
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBConstant("2016-12-02 15:57:02.03", DB_TYPE_FACTORY.getDBDateTimestampType()), translation);
    }

    @Test
    public void addition_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT X + 1 AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(
                SQLTestingTools.DB_FS_FACTORY.getUntypedDBMathBinaryOperator("+"),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType)), translation);
    }

    @Test
    public void subtraction_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT X - 1 AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(
                SQLTestingTools.DB_FS_FACTORY.getUntypedDBMathBinaryOperator("-"),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType)), translation);
    }

    @Test
    public void multiplication_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT X * 2 AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(
                SQLTestingTools.DB_FS_FACTORY.getUntypedDBMathBinaryOperator("*"),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test
    public void division_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT X / 2 AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(
                SQLTestingTools.DB_FS_FACTORY.getUntypedDBMathBinaryOperator("/"),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void modulo_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT X % 2 AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(
                SQLTestingTools.DB_FS_FACTORY.getUntypedDBMathBinaryOperator("%"),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void integer_div_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT X DIV 2 AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(
                SQLTestingTools.DB_FS_FACTORY.getUntypedDBMathBinaryOperator("DIV"),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test
    public void concat_2_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");

        ImmutableTerm translation = parseTerm("SELECT X || 'B' AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(
                SQLTestingTools.DB_FS_FACTORY.getNullRejectingDBConcat(2),
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("B")), translation);
    }

    @Test
    public void concat_3_Test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT 'A' || X || 'B' FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        DBFunctionSymbol concat = SQLTestingTools.DB_FS_FACTORY.getNullRejectingDBConcat(2);

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(
                concat,
                SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(
                        concat,
                        SQLTestingTools.TERM_FACTORY.getDBStringConstant("A"),
                        v),
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("B")), translation);
    }

    @Test
    public void function_CONCAT_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT CONCAT('A', X, 'B') FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(
                SQLTestingTools.DB_FS_FACTORY.getRegularDBFunctionSymbol("CONCAT", 3),
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("A"),
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("B")), translation);
    }

    // Boolean expressions are not allowed in the SELECT clause
    // so, the tests below depend on the WHERE clause

    @Test
    public void equalsTo_Test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X = 'B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("B")), translation.get(0));
    }

    @Test
    public void not_equalsTo_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X <> 'B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBNot(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("B"))), translation.get(0));
    }

    @Test
    public void not_equalsTo_bang_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X != 'B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBNot(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("B"))), translation.get(0));
    }

    @Test
    public void greater_than_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X > 3", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(GT),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType)), translation.get(0));
    }

    @Test
    public void greater_than_or_equals_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X >= 3", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(GTE),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType)), translation.get(0));
    }

    @Test
    public void minor_than_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X < 3", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(LT),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType)), translation.get(0));
    }

    @Test
    public void minor_than_equals_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X <= 3", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(LTE),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType)), translation.get(0));
    }

    @Test
    public void not_equals_to_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X = 'B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT, SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("B"))), translation.get(0));
    }

    @Test
    public void not_not_equals_to_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X <> 'B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("B")), translation.get(0));
    }

    @Test
    public void not_not_equals_to_bang_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X != 'B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("B")), translation.get(0));
    }

    @Test
    public void not_greater_than_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X > 3", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT, SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(GT),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType))), translation.get(0));
    }

    @Test
    public void not_greater_than_equals_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X >= 3", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT, SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(GTE),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType))), translation.get(0));
    }

    @Test
    public void not_minor_than_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X < 3", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT, SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(LT),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType))), translation.get(0));
    }

    @Test
    public void not_minor_than_equals_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X <= 3", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT, SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(LTE),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType))), translation.get(0));
    }

    @Test
    public void in_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X IN (1, 3)", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBOr(2),
                SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType)),
                SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType))), translation.get(0));
    }

    @Test
    public void not_in_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X NOT IN (1, 3)", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT,
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBOr(2),
                        SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                                v,
                                SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType)),
                        SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                                v,
                                SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType)))), translation.get(0));
    }

    @Test(expected = JSQLParserException.class)
    public void in_multi_test() throws JSQLParserException {
        Variable v1 = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        Variable v2 = SQLTestingTools.TERM_FACTORY.getVariable("y0");

        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE (X, Y) IN ((1, 3), (2,4))", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v1,
                new QualifiedAttributeID(null, IDFAC.createAttributeID("Y")), v2));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBOr(2),
                SQLTestingTools.TERM_FACTORY.getConjunction(
                        SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                                v1,
                                SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType)),
                        SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                                v2,
                                SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType))),
                SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                        v1,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("2", dbLongType)),
                SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                        v2,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("4", dbLongType))), translation.get(0));
    }


    @Test
    public void is_null_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X IS NULL", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBIsNull(), v), translation.get(0));
    }

    @Test
    public void is_not_null_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X IS NOT NULL", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT,
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBIsNull(), v)), translation.get(0));
    }

    @Test
    public void between_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X BETWEEN 1 AND 3", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        assertEquals(ImmutableList.of(
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                        SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(GTE),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType)),
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                        SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(LTE),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }

    @Test
    public void not_between_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X NOT BETWEEN 1 AND 3", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBOr(2),
                        SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                                SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(LT),
                                v,
                                SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType)),
                        SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                                SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(GT),
                                v,
                                SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType))), translation.get(0));
    }

    @Test
    public void like_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X LIKE '_A%'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBLike(),
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("_A%")), translation.get(0));
    }

    @Test
    public void not_like_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X NOT LIKE '_A%'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT,
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBLike(),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBStringConstant("_A%"))), translation.get(0));
    }

    @Test
    public void regexp_match_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X ~ 'A.*B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches2(),
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("A.*B")), translation.get(0));
    }

    @Test
    public void regexp_not_match_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X !~ 'A.*B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT,
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches2(),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBStringConstant("A.*B"))), translation.get(0));
    }

    @Test
    public void regexp_match_ignore_case_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X ~* 'A.*B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches3(),
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("A.*B"),
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("i")), translation.get(0));
    }

    @Test
    public void regexp_not_match_ignore_case_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X !~* 'A.*B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT,
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches3(),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBStringConstant("A.*B"),
                        SQLTestingTools.TERM_FACTORY.getDBStringConstant("i"))), translation.get(0));
    }

    @Test
    public void not_regexp_match_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X ~ 'A.*B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT,
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches2(),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBStringConstant("A.*B"))), translation.get(0));
    }

    @Test
    public void not_regexp_not_match_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X !~ 'A.*B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches2(),
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("A.*B")), translation.get(0));
    }

    @Test
    public void not_regexp_match_ignore_case_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X ~* 'A.*B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT,
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches3(),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBStringConstant("A.*B"),
                        SQLTestingTools.TERM_FACTORY.getDBStringConstant("i"))), translation.get(0));
    }

    @Test
    public void not_regexp_not_match_ignore_case_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X !~* 'A.*B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches3(),
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("A.*B"),
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("i")), translation.get(0));
    }


    @Test
    public void regexp_MySQL_match_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X REGEXP BINARY 'A.*B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches2(),
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("A.*B")), translation.get(0));
    }

    @Test
    public void regexp_MySQL_match_ignore_case_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X REGEXP 'A.*B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches3(),
                v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("A.*B"),
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("i")), translation.get(0));
    }

    @Test
    public void not_regexp_MySQL_match_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X REGEXP BINARY 'A.*B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT,
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches2(),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBStringConstant("A.*B"))), translation.get(0));
    }

    @Test
    public void not_regexp_MySQL_match_ignore_case_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT X REGEXP 'A.*B'", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT,
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches3(),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBStringConstant("A.*B"),
                        SQLTestingTools.TERM_FACTORY.getDBStringConstant("i"))), translation.get(0));
    }

    @Test
    public void and_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X >= 1 AND X <= 3", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        assertEquals(ImmutableList.of( //FACTORY.getImmutableExpression(AND,
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                        SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(GTE),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                        SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(LTE),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }

    @Test
    public void not_and_test_brackets() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT (X >= 1 AND X <= 3)", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        assertEquals(ImmutableList.of(SQLTestingTools.TERM_FACTORY.getDBNot(SQLTestingTools.TERM_FACTORY.getConjunction(
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                        SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(GTE),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                        SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(LTE),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType))))), translation);
    }

    @Test
    public void not_not_and_test_brackets() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT (NOT (X >= 1 AND X <= 3))", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        assertEquals(ImmutableList.of(
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                        SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(GTE),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                        SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(LTE),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }


    @Test
    public void or_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE X < 1 OR X > 3", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBOr(2),
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                        SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(LT),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType)),
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                        SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(GT),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("3", dbLongType))), translation.get(0));
    }

    @Test
    public void parenthesis_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE (X >= 1)", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(GTE),
                v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType)), translation.get(0));
    }

    @Test
    public void not_parenthesis_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X AS A FROM DUMMY WHERE NOT (X >= 1)", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(NOT,
                SQLTestingTools.TERM_FACTORY.getImmutableExpression(
                        SQLTestingTools.DB_FS_FACTORY.getDBDefaultInequality(GTE),
                        v,
                        SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType))), translation.get(0));
    }

    @Test
    public void unary_plus_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT +X  AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        assertEquals(v, translation);
    }

    @Test
    public void unary_minus_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT -X  AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(
                SQLTestingTools.DB_FS_FACTORY.getUntypedDBMathBinaryOperator(SPARQL.NUMERIC_MULTIPLY),
                SQLTestingTools.TERM_FACTORY.getDBConstant("-1", DB_TYPE_FACTORY.getDBLargeIntegerType()),
                v), translation);
    }

    @Test
    public void case_when_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT CASE A WHEN 1 THEN 3 ELSE 4 END FROM DUMMY;", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getIfThenElse(
                SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                SQLTestingTools.TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType()),
                SQLTestingTools.TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                translation);
    }

    @Test
    public void case_when_test_3() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT CASE A WHEN 1 THEN 3 WHEN 2 THEN 4 ELSE 5 END FROM DUMMY;", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBCase(
                Stream.of(Maps.immutableEntry(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                SQLTestingTools.TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                Maps.immutableEntry(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("2", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        SQLTestingTools.TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType()))),
                SQLTestingTools.TERM_FACTORY.getDBConstant("5", DB_TYPE_FACTORY.getDBLargeIntegerType()), false),
                translation);
    }

    @Test
    public void case_when_test_3b() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT CASE WHEN A = 1 THEN 3 WHEN A = 2 THEN 4 ELSE 5 END FROM DUMMY;", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBCase(
                Stream.of(Maps.immutableEntry(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        SQLTestingTools.TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        Maps.immutableEntry(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("2", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                                SQLTestingTools.TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType()))),
                SQLTestingTools.TERM_FACTORY.getDBConstant("5", DB_TYPE_FACTORY.getDBLargeIntegerType()), false),
                translation);
    }

    @Test
    public void case_when_test_4() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT CASE A WHEN 1 THEN 3 WHEN 2 THEN 4 END FROM DUMMY;", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBCase(
                Stream.of(Maps.immutableEntry(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        SQLTestingTools.TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        Maps.immutableEntry(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("2", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                                SQLTestingTools.TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType()))),
                SQLTestingTools.TERM_FACTORY.getNullConstant(), false),
                translation);
    }

    @Test
    public void case_when_test_4_null() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT CASE A WHEN 1 THEN 3 WHEN 2 THEN 4 ELSE NULL END FROM DUMMY;", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBCase(
                Stream.of(Maps.immutableEntry(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        SQLTestingTools.TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        Maps.immutableEntry(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("2", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                                SQLTestingTools.TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType()))),
                SQLTestingTools.TERM_FACTORY.getNullConstant(), false),
                translation);
    }

    @Test
    public void case_when_test_4b() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT CASE WHEN A = 1 THEN 3 WHEN A = 2 THEN 4 END FROM DUMMY;", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBCase(
                Stream.of(Maps.immutableEntry(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        SQLTestingTools.TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        Maps.immutableEntry(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("2", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                                SQLTestingTools.TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType()))),
                SQLTestingTools.TERM_FACTORY.getNullConstant(), false),
                translation);
    }

    @Test
    public void case_when_test_4b_null() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT CASE WHEN A = 1 THEN 3 WHEN A = 2 THEN 4 ELSE NULL END FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getDBCase(
                Stream.of(Maps.immutableEntry(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        SQLTestingTools.TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        Maps.immutableEntry(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(v, SQLTestingTools.TERM_FACTORY.getDBConstant("2", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                                SQLTestingTools.TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType()))),
                SQLTestingTools.TERM_FACTORY.getNullConstant(), false),
                translation);
    }


    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void subSelect_Test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT (SELECT A FROM Q WHERE A = P.B) AS C FROM P", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void exists_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT * FROM P WHERE EXISTS (SELECT * FROM Q WHERE A = P.B);", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void not_exists_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT * FROM P WHERE NOT EXISTS (SELECT * FROM Q WHERE A = P.B);", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void all_comparison_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT * FROM P WHERE A > ALL (SELECT C FROM Q WHERE A = P.B);", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void any_comparison_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT * FROM P WHERE A > ANY (SELECT C FROM Q WHERE A = P.B);", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void bitwise_and_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        Variable u = SQLTestingTools.TERM_FACTORY.getVariable("y0");
        ImmutableTerm translation = parseTerm("SELECT X & Y AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v,
                new QualifiedAttributeID(null, IDFAC.createAttributeID("Y")), u));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void bitwise_or_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        Variable u = SQLTestingTools.TERM_FACTORY.getVariable("y0");
        ImmutableTerm translation = parseTerm("SELECT X | Y AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v,
                new QualifiedAttributeID(null, IDFAC.createAttributeID("Y")), u));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void bitwise_xor_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        Variable u = SQLTestingTools.TERM_FACTORY.getVariable("y0");
        ImmutableTerm translation = parseTerm("SELECT X ^ Y AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v,
                new QualifiedAttributeID(null, IDFAC.createAttributeID("Y")), u));
    }

    @Test
    public void extract_variable_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT EXTRACT(MONTH FROM X) AS C FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getExtractFunctionSymbol("MONTH"),
                v), translation);
    }

    @Test
    public void extract_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT EXTRACT(MONTH FROM CURRENT_DATE) AS C FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getExtractFunctionSymbol("MONTH"),
                SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getCurrentDateTimeSymbol("DATE"))), translation);
    }

    @Test
    public void extract_current_date_brackets_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT EXTRACT(MONTH FROM CURRENT_DATE()) AS C FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getExtractFunctionSymbol("MONTH"),
                SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getCurrentDateTimeSymbol("DATE"))), translation);
    }

    @Test
    public void extract_from_literal_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT EXTRACT(YEAR FROM DATE '1998-03-07') FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getExtractFunctionSymbol("YEAR"),
                SQLTestingTools.TERM_FACTORY.getDBConstant("1998-03-07", DB_TYPE_FACTORY.getDBDateType())),
                translation);
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void interval_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT INTERVAL '31' DAY FROM DUMMY", ImmutableMap.of());
    }


    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void sum_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT SUM(X) AS C FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void avg_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT AVG(X) AS C FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void min_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT MIN(X) AS C FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void max_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT MAX(X) AS C FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void count_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT COUNT(X) AS C FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void count_star_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT COUNT(*) AS C FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
    }



    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void analytic_expression_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT LAG(A) OVER () FROM P", ImmutableMap.of());
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void json_expression_test() throws JSQLParserException {
        ImmutableTerm translation = parseTerm("SELECT A->'B' FROM DUMMY", ImmutableMap.of());
    }

    @Test(expected = InvalidSelectQueryRuntimeException.class)
    public void jdbc_parameter_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT A FROM P WHERE B = ?", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("B")), v));
    }

    @Test(expected = InvalidSelectQueryRuntimeException.class)
    public void jdbc_named_parameter_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT A FROM P WHERE B = :name", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("B")), v));
    }

    @Test(expected = UnsupportedSelectQueryRuntimeException.class)
    public void oracle_outer_join_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        Variable u = SQLTestingTools.TERM_FACTORY.getVariable("y0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT * FROM P, Q WHERE P.A = Q.A(+)", ImmutableMap.of(
                new QualifiedAttributeID(IDFAC.createRelationID("P"), IDFAC.createAttributeID("A")), v,
                new QualifiedAttributeID(IDFAC.createRelationID( "Q"), IDFAC.createAttributeID("A")), u));
    }

    @Test
    public void true_column_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT * FROM P WHERE A = true", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                v,
                SQLTestingTools.TERM_FACTORY.getDBBooleanConstant(true)), translation.get(0));
    }

    @Test
    public void false_column_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT * FROM P WHERE A = false", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("A")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getNotYetTypedEquality(
                v,
                SQLTestingTools.TERM_FACTORY.getDBBooleanConstant(false)), translation.get(0));
    }

    @Test
    public void function_REGEXP_LIKE_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X FROM DUMMY WHERE REGEXP_LIKE(X, '^Ste(v|ph)en$')", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches2(), v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("^Ste(v|ph)en$")), translation.get(0));
    }

    @Test
    public void function_REGEXP_LIKE_3_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X FROM DUMMY WHERE REGEXP_LIKE(X, '^Ste(v|ph)en$', 'i')", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableExpression(SQLTestingTools.DB_FS_FACTORY.getDBRegexpMatches3(), v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("^Ste(v|ph)en$"),
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("i")), translation.get(0));
    }

    /**
     * Not recognized ??? - not a boolean function?
     */
    @Ignore
    @Test
    public void function_REGEXP_LIKE_4_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableList<ImmutableExpression> translation = parseBooleanExpression("SELECT X FROM DUMMY WHERE REGEXP_LIKE(X, '^Ste(v|ph)en$', 'i', '')", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
    }

    @Test
    public void function_REGEXP_REPLACE_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT REGEXP_REPLACE(X, '^Ste(v|ph)en$', '')  AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getDBRegexpReplace3(), v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("^Ste(v|ph)en$"),
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("")), translation);
    }

    @Test
    public void function_REGEXP_REPLACE_4_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT REGEXP_REPLACE(X, '^Ste(v|ph)en$', '', 'i')  AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getDBRegexpReplace4(), v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("^Ste(v|ph)en$"),
                SQLTestingTools.TERM_FACTORY.getDBStringConstant(""),
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("i")), translation);
    }

    /**
     * Not recognized ????
     */
    @Test
    public void function_REGEXP_REPLACE_6_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT REGEXP_REPLACE(X, '^Ste(v|ph)en$', '', 1, 0, 'i')  AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
    }

    /**
     * Not recognized ????
     */
    @Test
    public void function_REGEXP_REPLACE_6a_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT REGEXP_REPLACE(X, '^Ste(v|ph)en$', '', 2, 0, 'i')  AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
    }

    /**
     * Not recognized ????
     */
    @Test
    public void function_REGEXP_REPLACE_7_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT REGEXP_REPLACE(X, '^Ste(v|ph)en$', '', 1, 0, 'i', '')  AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
    }

    @Test
    public void function_REPLACE_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT REPLACE(X,'J') AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getRegularDBFunctionSymbol("REPLACE", 2), v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("J")), translation);
    }

    @Test
    public void function_REPLACE_3_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT REPLACE(X, 'J', 'BL') AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getRegularDBFunctionSymbol("REPLACE", 3), v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("J"),
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("BL")), translation);
    }

    @Test
    public void function_REPLACE_4_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT REPLACE(X, 'J', 'BL', 'i') AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getRegularDBFunctionSymbol("REPLACE", 4), v,
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("J"),
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("BL"),
                SQLTestingTools.TERM_FACTORY.getDBStringConstant("i")), translation);
    }

    @Test
    public void function_SUBSTR_2_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT SUBSTR(X, 1) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getRegularDBFunctionSymbol("SUBSTR", 2), v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType)), translation);
    }

    @Test
    public void function_SUBSTR_3_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT SUBSTR(X, 1, 2) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getRegularDBFunctionSymbol("SUBSTR", 3), v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType),
                SQLTestingTools.TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test
    public void function_SQL_SUBSTRING2_test() throws JSQLParserException {
        // SQL:99: SUBSTRING <left paren> <character value expression> FROM <start position> [ FOR <string length> ] <right paren>
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT SUBSTRING(X FROM 1) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getDBSubString2(), v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType)), translation);
    }

    @Test
    public void function_SQL_SUBSTRING3_test() throws JSQLParserException {
        // SQL:99: SUBSTRING <left paren> <character value expression> FROM <start position> [ FOR <string length> ] <right paren>
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT SUBSTRING(X FROM 1 FOR 2) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getDBSubString3(), v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType),
                SQLTestingTools.TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test
    public void function_SUBSTRING_2_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT SUBSTRING(X, 1) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getDBSubString2(), v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType)), translation);
    }

    @Test
    public void function_SUBSTRING_3_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT SUBSTRING(X, 1, 2) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getDBSubString3(), v,
                SQLTestingTools.TERM_FACTORY.getDBConstant("1", dbLongType),
                SQLTestingTools.TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test
    public void function_LCASE_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT LCASE(X) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getDBLower(), v), translation);
    }

    @Test
    public void function_LOWER_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT LOWER(X) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getDBLower(), v), translation);
    }

    @Test
    public void function_UCASE_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT UCASE(X) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getDBUpper(), v), translation);
    }

    @Test
    public void function_UPPER_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT UPPER(X) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(SQLTestingTools.DB_FS_FACTORY.getDBUpper(), v), translation);
    }

    @Test
    public void function_LENGTH_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT LENGTH(X) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        DBFunctionSymbol lengthFunctionSymbol = SQLTestingTools.DB_FS_FACTORY.getRegularDBFunctionSymbol("LENGTH", 1);
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(lengthFunctionSymbol, v), translation);
    }

    @Test
    public void function_LEN_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT LEN(X) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        DBFunctionSymbol lenFunctionSymbol = SQLTestingTools.DB_FS_FACTORY.getRegularDBFunctionSymbol("LEN", 1);
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(lenFunctionSymbol, v), translation);
    }

    @Test
    public void function_CONVERT_test() throws JSQLParserException {
        Variable v = SQLTestingTools.TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SELECT CONVERT(VARCHAR(50), X) AS A FROM DUMMY", ImmutableMap.of(
                new QualifiedAttributeID(null, IDFAC.createAttributeID("X")), v));

        DBFunctionSymbol castFunctionSymbol = SQLTestingTools.DB_FS_FACTORY.getDBCastFunctionSymbol(DB_TYPE_FACTORY.getDBTermType("VARCHAR(50)"));
        Assert.assertEquals(SQLTestingTools.TERM_FACTORY.getImmutableFunctionalTerm(castFunctionSymbol, v), translation);
    }


    private ImmutableTerm parseTerm(String sql, ImmutableMap<QualifiedAttributeID, ImmutableTerm> map) throws JSQLParserException {
        ExpressionParser parser = new ExpressionParser(IDFAC, SQLTestingTools.CORE_SINGLETONS);
        Statement statement = CCJSqlParserUtil.parse(sql);
        SelectItem si = ((PlainSelect) ((Select) statement).getSelectBody()).getSelectItems().get(0);
        net.sf.jsqlparser.expression.Expression exp = ((SelectExpressionItem) si).getExpression();
        ImmutableTerm translation = parser.parseTerm(exp, new RAExpressionAttributes(map, null));
        System.out.println(translation);
        return translation;
    }

    private ImmutableList<ImmutableExpression> parseBooleanExpression(String sql, ImmutableMap<QualifiedAttributeID, ImmutableTerm> map) throws JSQLParserException {
        ExpressionParser parser = new ExpressionParser(IDFAC, SQLTestingTools.CORE_SINGLETONS);
        Statement statement = CCJSqlParserUtil.parse(sql);
        net.sf.jsqlparser.expression.Expression exp = ((PlainSelect) ((Select) statement).getSelectBody()).getWhere();
        ImmutableList<ImmutableExpression> translation = parser.parseBooleanExpression(exp, new RAExpressionAttributes(map, null));
        System.out.println(translation);
        return translation;
    }
}
