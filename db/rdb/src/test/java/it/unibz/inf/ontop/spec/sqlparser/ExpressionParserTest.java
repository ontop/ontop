package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.spec.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryRuntimeException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.ReturningReferenceType;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.XmlTypeModifier;
import net.sf.jsqlparser.statement.select.FunctionAllColumns;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.TableFunction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.*;
import static it.unibz.inf.ontop.spec.sqlparser.SQLTestingTools.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpressionParserTest {

    private static final DBBooleanFunctionSymbol NOT = DB_FS_FACTORY.getDBNot();

    private static final QuotedIDFactory IDFAC;
    private static final DBTypeFactory DB_TYPE_FACTORY;
    private static final DBTermType dbLongType;

    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        IDFAC = builder.getQuotedIDFactory();
        DB_TYPE_FACTORY = builder.getDBTypeFactory();
        dbLongType = DB_TYPE_FACTORY.getDBLargeIntegerType();
    }

    @Test
    public void null_test() throws Exception {
        ImmutableTerm translation = parseTerm("NULL", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getNullConstant(), translation);
    }

    @Test
    public void boolean_literal_test() throws Exception {
        assertEquals(TERM_FACTORY.getDBBooleanConstant(true), parseTerm("TRUE", ImmutableMap.of()));
        assertEquals(TERM_FACTORY.getDBBooleanConstant(false), parseTerm("FALSE", ImmutableMap.of()));
    }

    @Test
    public void trim_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                        DB_FS_FACTORY.getRegularDBFunctionSymbol("TRIM", 1), v),
                parseTerm("TRIM(X)", ImmutableMap.of(IDFAC.createAttributeID("X"), v)));
        assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("TRIM(LEADING 'a' FROM X)", ImmutableMap.of(IDFAC.createAttributeID("X"), v)));
    }

    @Test
    public void try_cast_test() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("TRY_CAST(1 AS INTEGER)", ImmutableMap.of()));
        assertEquals("Cast keyword is not supported", ex.getMessage());
    }

    @Test
    public void row_in_length_mismatch_test() throws Exception {
        var ex = assertThrows(InvalidSelectQueryRuntimeException.class, () ->
                parseBooleanExpression("(1, 2) IN ((1, 2, 3))", ImmutableMap.of()));
        assertEquals("Mismatch in the length of the lists", ex.getMessage());
    }

    @Test
    public void new_expression_is_rejected_test() throws Exception {
        assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("1 IS UNKNOWN", ImmutableMap.of()));
    }

    @Test
    public void in_subquery_is_rejected_test() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseBooleanExpression("1 IN (SELECT 1 FROM P)", ImmutableMap.of()));
        assertEquals("Expression on the right in IN is not supported", ex.getMessage());
    }

    @Test
    public void double_test() throws Exception {
        ImmutableTerm translation = parseTerm("1.0", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getDBConstant("1.0", DB_TYPE_FACTORY.getDBDoubleType()), translation);
    }

    @Test
    public void hex_test() throws Exception {
        ImmutableTerm translation = parseTerm("0xFF", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getDBConstant("255", dbLongType), translation);
    }

    @Test
    public void hex_quote_test() throws Exception {
        ImmutableTerm translation = parseTerm("x'FF'", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getDBConstant("255", dbLongType), translation);
    }

    @Test
    public void hex_quote2_test() throws Exception {
        ImmutableTerm translation = parseTerm("X'FF'", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getDBConstant("255", dbLongType), translation);
    }

    @Test
    public void long_test() throws Exception {
        ImmutableTerm translation = parseTerm("1", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getDBConstant("1", dbLongType), translation);
    }

    @Test
    public void string_test() throws Exception {
        ImmutableTerm translation = parseTerm("'1'", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getDBStringConstant("1"), translation);
    }

    @Test
    public void date_test() throws Exception {
        //  ODBC escape sequence syntax
        ImmutableTerm translation = parseTerm("{d '2016-12-02'}", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getDBConstant("2016-12-02", DB_TYPE_FACTORY.getDBDateType()), translation);
    }

    @Test
    public void time_test() throws Exception {
        //  ODBC escape sequence syntax
        ImmutableTerm translation = parseTerm("{t '15:57:02'}", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getDBConstant("15:57:02", DB_TYPE_FACTORY.getDBTimeType()), translation);
    }

    @Test
    public void timestamp_test() throws Exception {
        //  ODBC escape sequence syntax
        ImmutableTerm translation = parseTerm("{ts '2016-12-02 15:57:02.03'}", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getDBConstant("2016-12-02 15:57:02.03",
                DB_TYPE_FACTORY.getDBDateTimestampType()), translation);
    }


    @Test
    public void date_literal_test() throws Exception {
        ImmutableTerm translation = parseTerm("DATE '1998-03-07'", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getDBConstant("1998-03-07", DB_TYPE_FACTORY.getDBDateType()), translation);
    }

    @Test
    public void time_literal_test() throws Exception {
        ImmutableTerm translation = parseTerm("TIME '15:57:02'", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getDBConstant("15:57:02", DB_TYPE_FACTORY.getDBTimeType()), translation);
    }

    @Test
    public void timestamp_literal_test() throws Exception {
        ImmutableTerm translation = parseTerm("TIMESTAMP '2016-12-02 15:57:02.03'", ImmutableMap.of());
        assertEquals(TERM_FACTORY.getDBConstant("2016-12-02 15:57:02.03", DB_TYPE_FACTORY.getDBDateTimestampType()), translation);
    }

    @Test
    public void addition_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("X + 1", ImmutableMap.of(IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                DB_FS_FACTORY.getUntypedDBMathBinaryOperator("+"),
                v,
                TERM_FACTORY.getDBConstant("1", dbLongType)), translation);
    }

    @Test
    public void subtraction_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("X - 1", ImmutableMap.of(IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                DB_FS_FACTORY.getUntypedDBMathBinaryOperator("-"),
                v,
                TERM_FACTORY.getDBConstant("1", dbLongType)), translation);
    }

    @Test
    public void multiplication_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("X * 2", ImmutableMap.of(IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                DB_FS_FACTORY.getUntypedDBMathBinaryOperator("*"),
                v,
                TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test
    public void division_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("X / 2", ImmutableMap.of(IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                DB_FS_FACTORY.getUntypedDBMathBinaryOperator("/"),
                v,
                TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test
    public void modulo_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("X % 2", ImmutableMap.of(IDFAC.createAttributeID("X"), v)));

        assertEquals("The untyped math operator % is not supported", ex.getMessage());
    //    assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
    //            DB_FS_FACTORY.getUntypedDBMathBinaryOperator("%"),
    //            v,
    //            TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test
    public void integer_div_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("X DIV 2", ImmutableMap.of(IDFAC.createAttributeID("X"), v)));

        assertEquals("The untyped math operator DIV is not supported", ex.getMessage());
//        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
//                DB_FS_FACTORY.getUntypedDBMathBinaryOperator("DIV"),
//                v,
//                TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test
    public void concat_2_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");

        ImmutableTerm translation = parseTerm("X || 'B'", ImmutableMap.of(IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                DB_FS_FACTORY.getNullRejectingDBConcat(2),
                v,
                TERM_FACTORY.getDBStringConstant("B")), translation);
    }

    @Test
    public void concat_3_Test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("'A' || X || 'B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        DBFunctionSymbol concat = DB_FS_FACTORY.getNullRejectingDBConcat(2);

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                concat,
                TERM_FACTORY.getImmutableFunctionalTerm(
                        concat,
                        TERM_FACTORY.getDBStringConstant("A"),
                        v),
                TERM_FACTORY.getDBStringConstant("B")), translation);
    }

    @Test
    public void function_CONCAT_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("CONCAT('A', X, 'B')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                DB_FS_FACTORY.getRegularDBFunctionSymbol("CONCAT", 3),
                TERM_FACTORY.getDBStringConstant("A"),
                v,
                TERM_FACTORY.getDBStringConstant("B")), translation);
    }

    // Boolean expressions are not allowed in the SELECT clause
    // so, the tests below depend on the WHERE clause

    @Test
    public void equalsTo_Test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X = 'B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getNotYetTypedEquality(
                v,
                TERM_FACTORY.getDBStringConstant("B")), translation);
    }

    @Test
    public void not_equalsTo_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X <> 'B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getDBNot(TERM_FACTORY.getNotYetTypedEquality(
                v,
                TERM_FACTORY.getDBStringConstant("B"))), translation);
    }

    @Test
    public void not_equalsTo_bang_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X != 'B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getDBNot(TERM_FACTORY.getNotYetTypedEquality(
                v,
                TERM_FACTORY.getDBStringConstant("B"))), translation);
    }

    @Test
    public void equalsTo_constant_with_quoteTest() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X = 'Jane''s dogs'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getNotYetTypedEquality(
                v,
                TERM_FACTORY.getDBStringConstant("Jane's dogs")), translation);
    }

    @Test
    public void equalsTo_constant_with_quote2Test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X = 'Jane''''s dogs'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getNotYetTypedEquality(
                v,
                TERM_FACTORY.getDBStringConstant("Jane''s dogs")), translation);
    }

    @Test
    public void greater_than_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X > 3", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(
                DB_FS_FACTORY.getDBDefaultInequality(GT),
                v,
                TERM_FACTORY.getDBConstant("3", dbLongType)), translation);
    }

    @Test
    public void greater_than_or_equals_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X >= 3", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(
                DB_FS_FACTORY.getDBDefaultInequality(GTE),
                v,
                TERM_FACTORY.getDBConstant("3", dbLongType)), translation);
    }

    @Test
    public void minor_than_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X < 3", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(
                DB_FS_FACTORY.getDBDefaultInequality(LT),
                v,
                TERM_FACTORY.getDBConstant("3", dbLongType)), translation);
    }

    @Test
    public void minor_than_equals_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X <= 3", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(
                DB_FS_FACTORY.getDBDefaultInequality(LTE),
                v,
                TERM_FACTORY.getDBConstant("3", dbLongType)), translation);
    }

    @Test
    public void not_equals_to_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X = 'B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT, TERM_FACTORY.getNotYetTypedEquality(
                v,
                TERM_FACTORY.getDBStringConstant("B"))), translation);
    }

    @Test
    public void not_not_equals_to_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X <> 'B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getNotYetTypedEquality(
                v,
                TERM_FACTORY.getDBStringConstant("B")), translation);
    }

    @Test
    public void not_not_equals_to_bang_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X != 'B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getNotYetTypedEquality(
                v,
                TERM_FACTORY.getDBStringConstant("B")), translation);
    }

    @Test
    public void not_greater_than_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X > 3", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT, TERM_FACTORY.getImmutableExpression(
                DB_FS_FACTORY.getDBDefaultInequality(GT),
                v,
                TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }

    @Test
    public void not_greater_than_equals_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X >= 3", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT, TERM_FACTORY.getImmutableExpression(
                DB_FS_FACTORY.getDBDefaultInequality(GTE),
                v,
                TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }

    @Test
    public void not_minor_than_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X < 3", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT, TERM_FACTORY.getImmutableExpression(
                DB_FS_FACTORY.getDBDefaultInequality(LT),
                v,
                TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }

    @Test
    public void not_minor_than_equals_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X <= 3", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT, TERM_FACTORY.getImmutableExpression(
                DB_FS_FACTORY.getDBDefaultInequality(LTE),
                v,
                TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }

    @Test
    public void is_distinct_from_test1() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("1 IS DISTINCT FROM 1", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getDBNot(
                TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getDBConstant("1", dbLongType),
                        TERM_FACTORY.getDBConstant("1", dbLongType))), translation.simplify());
    }

    @Test
    public void is_distinct_from_test2() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("1 IS DISTINCT FROM NULL", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getDBBooleanConstant(true), translation.simplify());
    }

    @Test
    public void is_distinct_from_test3() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NULL IS DISTINCT FROM 2", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getDBBooleanConstant(true), translation.simplify());
    }

    @Test
    public void is_distinct_from_test4() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NULL IS DISTINCT FROM NULL", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getDBBooleanConstant(false), translation.simplify());
    }

    @Test
    public void is_not_distinct_from_test1() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("1 IS NOT DISTINCT FROM 1", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(
                TERM_FACTORY.getNotYetTypedEquality(
                        TERM_FACTORY.getDBConstant("1", dbLongType),
                        TERM_FACTORY.getDBConstant("1", dbLongType)),
                translation.simplify());
    }

    @Disabled("DBFunctionSymbolFactory in H2 does not support this")
    @Test
    public void array_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("X[0]", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                        DB_FS_FACTORY.getDBArrayAccess(), v,
                        TERM_FACTORY.getDBConstant("1", dbLongType)),
                translation);
    }

    @Test
    public void array_interval_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("X[2:4]", ImmutableMap.of(
                        IDFAC.createAttributeID("X"), v)));

        assertEquals("Array intervals are not supported", ex.getMessage());
    }

    @Test
    public void in_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X IN (1, 3)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBOr(2),
                TERM_FACTORY.getNotYetTypedEquality(
                        v,
                        TERM_FACTORY.getDBConstant("1", dbLongType)),
                TERM_FACTORY.getNotYetTypedEquality(
                        v,
                        TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }

    @Test
    public void in_test_2() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X IN (1)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getNotYetTypedEquality(
                        v,
                        TERM_FACTORY.getDBConstant("1", dbLongType)), translation);
    }

    @Test
    public void not_in_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X NOT IN (1, 3)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT,
                TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBOr(2),
                        TERM_FACTORY.getNotYetTypedEquality(
                                v,
                                TERM_FACTORY.getDBConstant("1", dbLongType)),
                        TERM_FACTORY.getNotYetTypedEquality(
                                v,
                                TERM_FACTORY.getDBConstant("3", dbLongType)))), translation);
    }

    @Test
    public void in_multi_test() throws Exception {
        Variable v1 = TERM_FACTORY.getVariable("x0");
        Variable v2 = TERM_FACTORY.getVariable("y0");

        ImmutableExpression translation = parseBooleanExpression("(X, Y) IN ((1, 3), (2,4))", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v1,
                IDFAC.createAttributeID("Y"), v2));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBOr(2),
                TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getNotYetTypedEquality(
                                v1,
                                TERM_FACTORY.getDBConstant("1", dbLongType)),
                        TERM_FACTORY.getNotYetTypedEquality(
                                v2,
                                TERM_FACTORY.getDBConstant("3", dbLongType))),
                TERM_FACTORY.getConjunction(
                        TERM_FACTORY.getNotYetTypedEquality(
                                v1,
                                TERM_FACTORY.getDBConstant("2", dbLongType)),
                        TERM_FACTORY.getNotYetTypedEquality(
                                v2,
                                TERM_FACTORY.getDBConstant("4", dbLongType)))),
                translation);
    }


    @Test
    public void in_multi_test_2() throws Exception {
        Variable v1 = TERM_FACTORY.getVariable("x0");
        Variable v2 = TERM_FACTORY.getVariable("y0");

        ImmutableExpression translation = parseBooleanExpression("(X, Y) IN ((1, 3))", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v1,
                IDFAC.createAttributeID("Y"), v2));

        assertEquals(TERM_FACTORY.getConjunction(
                                TERM_FACTORY.getNotYetTypedEquality(
                                        v1,
                                        TERM_FACTORY.getDBConstant("1", dbLongType)),
                                TERM_FACTORY.getNotYetTypedEquality(
                                        v2,
                                        TERM_FACTORY.getDBConstant("3", dbLongType))),
                translation);
    }

    @Test
    public void is_null_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X IS NULL", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBIsNull(), v), translation);
    }

    @Test
    public void is_not_null_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X IS NOT NULL", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT,
                TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBIsNull(), v)), translation);
    }

    @Test
    public void between_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X BETWEEN 1 AND 3", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getConjunction(
                TERM_FACTORY.getImmutableExpression(
                        DB_FS_FACTORY.getDBDefaultInequality(GTE),
                        v,
                        TERM_FACTORY.getDBConstant("1", dbLongType)),
                TERM_FACTORY.getImmutableExpression(
                        DB_FS_FACTORY.getDBDefaultInequality(LTE),
                        v,
                        TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }

    @Test
    public void not_between_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X NOT BETWEEN 1 AND 3", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBOr(2),
                        TERM_FACTORY.getImmutableExpression(
                                DB_FS_FACTORY.getDBDefaultInequality(LT),
                                v,
                                TERM_FACTORY.getDBConstant("1", dbLongType)),
                        TERM_FACTORY.getImmutableExpression(
                                DB_FS_FACTORY.getDBDefaultInequality(GT),
                                v,
                                TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }

    @Test
    public void like_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X LIKE '_A%'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBLike(),
                v,
                TERM_FACTORY.getDBStringConstant("_A%")), translation);
    }

    @Test
    public void not_like_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X NOT LIKE '_A%'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT,
                TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBLike(),
                        v,
                        TERM_FACTORY.getDBStringConstant("_A%"))), translation);
    }

    @Test
    public void regexp_match_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X ~ 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches2(),
                v,
                TERM_FACTORY.getDBStringConstant("A.*B")), translation);
    }

    @Test
    public void regexp_not_match_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X !~ 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT,
                TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches2(),
                        v,
                        TERM_FACTORY.getDBStringConstant("A.*B"))), translation);
    }

    @Test
    public void regexp_match_ignore_case_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X ~* 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches3(),
                v,
                TERM_FACTORY.getDBStringConstant("A.*B"),
                TERM_FACTORY.getDBStringConstant("i")), translation);
    }

    @Test
    public void regexp_not_match_ignore_case_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X !~* 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT,
                TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches3(),
                        v,
                        TERM_FACTORY.getDBStringConstant("A.*B"),
                        TERM_FACTORY.getDBStringConstant("i"))), translation);
    }

    @Test
    public void not_regexp_match_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X ~ 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT,
                TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches2(),
                        v,
                        TERM_FACTORY.getDBStringConstant("A.*B"))), translation);
    }

    @Test
    public void not_regexp_not_match_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X !~ 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches2(),
                v,
                TERM_FACTORY.getDBStringConstant("A.*B")), translation);
    }

    @Test
    public void not_regexp_match_ignore_case_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X ~* 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT,
                TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches3(),
                        v,
                        TERM_FACTORY.getDBStringConstant("A.*B"),
                        TERM_FACTORY.getDBStringConstant("i"))), translation);
    }

    @Test
    public void not_regexp_not_match_ignore_case_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X !~* 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches3(),
                v,
                TERM_FACTORY.getDBStringConstant("A.*B"),
                TERM_FACTORY.getDBStringConstant("i")), translation);
    }


    @Test
    public void regexp_MySQL_match_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X REGEXP BINARY 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches2(),
                v,
                TERM_FACTORY.getDBStringConstant("A.*B")), translation);
    }

    @Test
    public void regexp_MySQL_match_ignore_case_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X REGEXP 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches3(),
                v,
                TERM_FACTORY.getDBStringConstant("A.*B"),
                TERM_FACTORY.getDBStringConstant("i")), translation);
    }

    @Test
    public void not_regexp_MySQL_match_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X REGEXP BINARY 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT,
                TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches2(),
                        v,
                        TERM_FACTORY.getDBStringConstant("A.*B"))), translation);
    }

    @Test
    public void not_regexp_MySQL_match_infix_NOT_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression(" X NOT REGEXP BINARY 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT,
                TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches2(),
                        v,
                        TERM_FACTORY.getDBStringConstant("A.*B"))), translation);
    }

    @Test
    public void not_regexp_MySQL_match_ignore_case_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X REGEXP 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT,
                TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches3(),
                        v,
                        TERM_FACTORY.getDBStringConstant("A.*B"),
                        TERM_FACTORY.getDBStringConstant("i"))), translation);
    }

    @Test
    public void not_regexp_MySQL_match_ignore_case_infix_NOT_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression(" X NOT REGEXP 'A.*B'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT,
                TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches3(),
                        v,
                        TERM_FACTORY.getDBStringConstant("A.*B"),
                        TERM_FACTORY.getDBStringConstant("i"))), translation);
    }

    @Test
    public void and_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X >= 1 AND X <= 3", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getConjunction(
                TERM_FACTORY.getImmutableExpression(
                        DB_FS_FACTORY.getDBDefaultInequality(GTE),
                        v,
                        TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                TERM_FACTORY.getImmutableExpression(
                        DB_FS_FACTORY.getDBDefaultInequality(LTE),
                        v,
                        TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }

    @Test
    public void not_and_test_brackets() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT (X >= 1 AND X <= 3)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getDBNot(
                        TERM_FACTORY.getConjunction(
                                TERM_FACTORY.getImmutableExpression(
                                        DB_FS_FACTORY.getDBDefaultInequality(GTE),
                                        v,
                                        TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                                TERM_FACTORY.getImmutableExpression(
                                        DB_FS_FACTORY.getDBDefaultInequality(LTE),
                                        v,
                                        TERM_FACTORY.getDBConstant("3", dbLongType)))), translation);
    }

    @Test
    public void not_not_and_test_brackets() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT (NOT (X >= 1 AND X <= 3))", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getConjunction(
                TERM_FACTORY.getImmutableExpression(
                        DB_FS_FACTORY.getDBDefaultInequality(GTE),
                        v,
                        TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                TERM_FACTORY.getImmutableExpression(
                        DB_FS_FACTORY.getDBDefaultInequality(LTE),
                        v,
                        TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }


    @Test
    public void or_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X < 1 OR X > 3", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBOr(2),
                TERM_FACTORY.getImmutableExpression(
                        DB_FS_FACTORY.getDBDefaultInequality(LT),
                        v,
                        TERM_FACTORY.getDBConstant("1", dbLongType)),
                TERM_FACTORY.getImmutableExpression(
                        DB_FS_FACTORY.getDBDefaultInequality(GT),
                        v,
                        TERM_FACTORY.getDBConstant("3", dbLongType))), translation);
    }

    @Test
    public void parenthesis_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("(X >= 1)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(
                DB_FS_FACTORY.getDBDefaultInequality(GTE),
                v,
                TERM_FACTORY.getDBConstant("1", dbLongType)), translation);
    }

    @Test
    public void not_parenthesis_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT (X >= 1)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(NOT,
                TERM_FACTORY.getImmutableExpression(
                        DB_FS_FACTORY.getDBDefaultInequality(GTE),
                        v,
                        TERM_FACTORY.getDBConstant("1", dbLongType))), translation);
    }

    @Test
    public void ontop_contains_role_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("ontop_contains_role('admin')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getOntopContainsRole(), TERM_FACTORY.getDBStringConstant("admin")), translation);
    }

    @Test
    public void ontop_contains_group_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("ontop_contains_group('admin')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getOntopContainsGroup(), TERM_FACTORY.getDBStringConstant("admin")), translation);
    }

    @Test
    public void ontop_contains_role_or_group_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("ontop_contains_role_or_group('admin')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getOntopContainsRoleOrGroup(), TERM_FACTORY.getDBStringConstant("admin")), translation);
    }

    @Test
    public void ontop_user_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("ontop_user() = 'roger'", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getNotYetTypedEquality(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getOntopUser()), TERM_FACTORY.getDBStringConstant("roger")), translation);
    }

    @Test
    public void unary_plus_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("+X ", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(v, translation);
    }

    @Test
    public void unary_minus_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("-X ", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(
                DB_FS_FACTORY.getUntypedDBMathBinaryOperator(SPARQL.MULTIPLY),
                TERM_FACTORY.getDBConstant("-1", DB_TYPE_FACTORY.getDBLargeIntegerType()),
                v), translation);
    }

    @Test
    public void case_when_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("CASE A WHEN 1 THEN 3 ELSE 4 END", ImmutableMap.of(
                IDFAC.createAttributeID("A"), v));

        assertEquals(TERM_FACTORY.getIfThenElse(
                TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType()),
                TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                translation);
    }

    @Test
    public void case_when_test_3() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("CASE A WHEN 1 THEN 3 WHEN 2 THEN 4 ELSE 5 END", ImmutableMap.of(
                IDFAC.createAttributeID("A"), v));

        assertEquals(TERM_FACTORY.getDBCase(
                Stream.of(Maps.immutableEntry(TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                Maps.immutableEntry(TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("2", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType()))),
                TERM_FACTORY.getDBConstant("5", DB_TYPE_FACTORY.getDBLargeIntegerType()), false),
                translation);
    }

    @Test
    public void case_when_test_3b() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("CASE WHEN A = 1 THEN 3 WHEN A = 2 THEN 4 ELSE 5 END", ImmutableMap.of(
                IDFAC.createAttributeID("A"), v));

        assertEquals(TERM_FACTORY.getDBCase(
                Stream.of(Maps.immutableEntry(TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        Maps.immutableEntry(TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("2", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                                TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType()))),
                TERM_FACTORY.getDBConstant("5", DB_TYPE_FACTORY.getDBLargeIntegerType()), false),
                translation);
    }

    @Test
    public void case_when_test_4() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("CASE A WHEN 1 THEN 3 WHEN 2 THEN 4 END", ImmutableMap.of(
                IDFAC.createAttributeID("A"), v));

        assertEquals(TERM_FACTORY.getDBCase(
                Stream.of(Maps.immutableEntry(TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        Maps.immutableEntry(TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("2", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                                TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType()))),
                TERM_FACTORY.getNullConstant(), false),
                translation);
    }

    @Test
    public void case_when_test_4_null() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("CASE A WHEN 1 THEN 3 WHEN 2 THEN 4 ELSE NULL END", ImmutableMap.of(
                IDFAC.createAttributeID("A"), v));

        assertEquals(TERM_FACTORY.getDBCase(
                Stream.of(Maps.immutableEntry(TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        Maps.immutableEntry(TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("2", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                                TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType()))),
                TERM_FACTORY.getNullConstant(), false),
                translation);
    }

    @Test
    public void case_when_test_4b() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("CASE WHEN A = 1 THEN 3 WHEN A = 2 THEN 4 END", ImmutableMap.of(
                IDFAC.createAttributeID("A"), v));

        assertEquals(TERM_FACTORY.getDBCase(
                Stream.of(Maps.immutableEntry(TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        Maps.immutableEntry(TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("2", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                                TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType()))),
                TERM_FACTORY.getNullConstant(), false),
                translation);
    }

    @Test
    public void case_when_test_4b_null() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("CASE WHEN A = 1 THEN 3 WHEN A = 2 THEN 4 ELSE NULL END", ImmutableMap.of(
                IDFAC.createAttributeID("A"), v));

        assertEquals(TERM_FACTORY.getDBCase(
                Stream.of(Maps.immutableEntry(TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("1", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        TERM_FACTORY.getDBConstant("3", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                        Maps.immutableEntry(TERM_FACTORY.getNotYetTypedEquality(v, TERM_FACTORY.getDBConstant("2", DB_TYPE_FACTORY.getDBLargeIntegerType())),
                                TERM_FACTORY.getDBConstant("4", DB_TYPE_FACTORY.getDBLargeIntegerType()))),
                TERM_FACTORY.getNullConstant(), false),
                translation);
    }

    @Test
    public void boolean_column_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("X", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getIsTrue(), v), translation);
    }

    @Test
    public void not_boolean_column_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("NOT X", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(
                DB_FS_FACTORY.getDBNot(),
                TERM_FACTORY.getImmutableExpression(
                        DB_FS_FACTORY.getIsTrue(),
                        v)),
                translation);
    }


    @Test
    public void subSelect_Test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("(SELECT A FROM Q WHERE A = P.B)", ImmutableMap.of(
                    IDFAC.createAttributeID("X"), v)));

        assertEquals("SubSelect is not supported yet", ex.getMessage());
    }

    @Test
    public void exists_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseBooleanExpression("EXISTS (SELECT * FROM Q WHERE A = P.B)", ImmutableMap.of(
                        IDFAC.createAttributeID("A"), v)));

        assertEquals("EXISTS is not supported yet", ex.getMessage());
    }

    @Test
    public void not_exists_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseBooleanExpression("NOT EXISTS (SELECT * FROM Q WHERE A = P.B)", ImmutableMap.of(
                        IDFAC.createAttributeID("A"), v)));

        assertEquals("EXISTS is not supported yet", ex.getMessage());
    }

    @Test
    public void all_comparison_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseBooleanExpression("A > ALL (SELECT C FROM Q WHERE A = P.B)", ImmutableMap.of(
                        IDFAC.createAttributeID("A"), v)));

        assertEquals("ALL is not supported yet", ex.getMessage());
    }

    @Test
    public void any_comparison_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseBooleanExpression("A > ANY (SELECT C FROM Q WHERE A = P.B)", ImmutableMap.of(
                        IDFAC.createAttributeID("A"), v)));

        assertEquals("ANY is not supported yet", ex.getMessage());
    }

    @Test
    public void bitwise_and_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        Variable u = TERM_FACTORY.getVariable("y0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("X & Y", ImmutableMap.of(
                    IDFAC.createAttributeID("X"), v,
                    IDFAC.createAttributeID("Y"), u)));

        assertEquals("Bitwise AND is not supported", ex.getMessage());
    }

    @Test
    public void bitwise_or_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        Variable u = TERM_FACTORY.getVariable("y0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("X | Y", ImmutableMap.of(
                        IDFAC.createAttributeID("X"), v,
                        IDFAC.createAttributeID("Y"), u)));

        assertEquals("Bitwise OR is not supported", ex.getMessage());
    }

    @Test
    public void bitwise_xor_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        Variable u = TERM_FACTORY.getVariable("y0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("X ^ Y", ImmutableMap.of(
                        IDFAC.createAttributeID("X"), v,
                        IDFAC.createAttributeID("Y"), u)));

        assertEquals("Bitwise XOR is not supported", ex.getMessage());
    }

    @Test
    public void extract_variable_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("EXTRACT(MONTH FROM X)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getExtractFunctionSymbol("MONTH"),
                v), translation);
    }

    @Test
    public void extract_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("EXTRACT(MONTH FROM CURRENT_DATE)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getExtractFunctionSymbol("MONTH"),
                TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getCurrentDateTimeSymbol("DATE"))), translation);
    }

    @Test
    public void extract_current_date_brackets_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("EXTRACT(MONTH FROM CURRENT_DATE())", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getExtractFunctionSymbol("MONTH"),
                TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getCurrentDateTimeSymbol("DATE"))), translation);
    }

    @Test
    public void extract_from_literal_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("EXTRACT(YEAR FROM DATE '1998-03-07')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getExtractFunctionSymbol("YEAR"),
                TERM_FACTORY.getDBConstant("1998-03-07", DB_TYPE_FACTORY.getDBDateType())),
                translation);
    }

    @Test
    public void interval_test() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("INTERVAL '31' DAY", ImmutableMap.of()));

        assertEquals("Temporal INTERVALs are not supported", ex.getMessage());
    }


    @Test
    public void sum_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("SUM(X)", ImmutableMap.of(
                        IDFAC.createAttributeID("X"), v)));

        assertEquals("Unsupported SQL function", ex.getMessage());
    }

    @Test
    public void avg_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("AVG(X)", ImmutableMap.of(
                        IDFAC.createAttributeID("X"), v)));

        assertEquals("Unsupported SQL function", ex.getMessage());
    }

    @Test
    public void min_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("MIN(X)", ImmutableMap.of(
                        IDFAC.createAttributeID("X"), v)));

        assertEquals("Unsupported SQL function", ex.getMessage());
    }

    @Test
    public void max_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("MAX(X)", ImmutableMap.of(
                        IDFAC.createAttributeID("X"), v)));

        assertEquals("Unsupported SQL function", ex.getMessage());
    }

    @Test
    public void count_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("COUNT(X)", ImmutableMap.of(
                        IDFAC.createAttributeID("X"), v)));

        assertEquals("Unsupported SQL function", ex.getMessage());
    }

    @Test
    public void count_star_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("COUNT(*)", ImmutableMap.of(
                        IDFAC.createAttributeID("X"), v)));

        assertEquals("Unsupported SQL function", ex.getMessage());
    }



    @Test
    public void analytic_expression_test() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("LAG(A) OVER ()", ImmutableMap.of()));

        assertEquals("Analytic expressions is not supported", ex.getMessage());
    }

    @Test
    public void json_expression_test() throws Exception {
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseTerm("A->'B'", ImmutableMap.of()));

        assertEquals("JSON expressions are not supported", ex.getMessage());
    }

    @Test
    public void jdbc_parameter_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(InvalidSelectQueryRuntimeException.class, () ->
                parseBooleanExpression("B = ?", ImmutableMap.of(
                        IDFAC.createAttributeID("B"), v)));

        assertEquals("JDBC parameters are not allowed", ex.getMessage());
    }

    @Test
    public void jdbc_named_parameter_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        var ex = assertThrows(InvalidSelectQueryRuntimeException.class, () ->
                parseBooleanExpression("B = :name", ImmutableMap.of(
                        IDFAC.createAttributeID("B"), v)));

        assertEquals("JDBC named parameters are not allowed", ex.getMessage());
    }

    @Test
    public void oracle_outer_join_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        Variable u = TERM_FACTORY.getVariable("y0");
        var ex = assertThrows(UnsupportedSelectQueryRuntimeException.class, () ->
                parseBooleanExpression("A = B(+)", ImmutableMap.of(
                        IDFAC.createAttributeID("A"), v,
                        IDFAC.createAttributeID("B"), u)));

        assertEquals("Old Oracle OUTER JOIN syntax is not supported", ex.getMessage());
    }

    @Test
    public void true_column_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("A = true", ImmutableMap.of(
                IDFAC.createAttributeID("A"), v));

        assertEquals(TERM_FACTORY.getNotYetTypedEquality(
                v,
                TERM_FACTORY.getDBBooleanConstant(true)), translation);
    }

    @Test
    public void false_column_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("A = false", ImmutableMap.of(
                IDFAC.createAttributeID("A"), v));

        assertEquals(TERM_FACTORY.getNotYetTypedEquality(
                v,
                TERM_FACTORY.getDBBooleanConstant(false)), translation);
    }

    @Test
    public void function_REGEXP_LIKE_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("REGEXP_LIKE(X, '^Ste(v|ph)en$')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches2(), v,
                TERM_FACTORY.getDBStringConstant("^Ste(v|ph)en$")), translation);
    }

    @Test
    public void function_REGEXP_LIKE_3_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("REGEXP_LIKE(X, '^Ste(v|ph)en$', 'i')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableExpression(DB_FS_FACTORY.getDBRegexpMatches3(), v,
                TERM_FACTORY.getDBStringConstant("^Ste(v|ph)en$"),
                TERM_FACTORY.getDBStringConstant("i")), translation);
    }

    @Disabled("Not recognized ??? - not a boolean function")
    @Test
    public void function_REGEXP_LIKE_4_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableExpression translation = parseBooleanExpression("REGEXP_LIKE(X, '^Ste(v|ph)en$', 'i', '')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getDBRegexpReplace4(), v,
                TERM_FACTORY.getDBStringConstant("^Ste(v|ph)en$"),
                TERM_FACTORY.getDBStringConstant("i"),
                TERM_FACTORY.getDBStringConstant("")), translation);
    }

    @Test
    public void function_REGEXP_REPLACE_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("REGEXP_REPLACE(X, '^Ste(v|ph)en$', '')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getDBRegexpReplace3(), v,
                TERM_FACTORY.getDBStringConstant("^Ste(v|ph)en$"),
                TERM_FACTORY.getDBStringConstant("")), translation);
    }

    @Test
    public void function_REGEXP_REPLACE_4_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("REGEXP_REPLACE(X, '^Ste(v|ph)en$', '', 'i')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getDBRegexpReplace4(), v,
                TERM_FACTORY.getDBStringConstant("^Ste(v|ph)en$"),
                TERM_FACTORY.getDBStringConstant(""),
                TERM_FACTORY.getDBStringConstant("i")), translation);
    }

    @Test
    public void function_REGEXP_REPLACE_6_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("REGEXP_REPLACE(X, '^Ste(v|ph)en$', '', 1, 0, 'i')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getRegularDBFunctionSymbol("REGEXP_REPLACE", 6), v,
                TERM_FACTORY.getDBStringConstant("^Ste(v|ph)en$"),
                TERM_FACTORY.getDBStringConstant(""),
                TERM_FACTORY.getDBIntegerConstant(1),
                TERM_FACTORY.getDBIntegerConstant(0),
                TERM_FACTORY.getDBStringConstant("i")), translation);
    }

    @Test
    public void function_REGEXP_REPLACE_6a_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("REGEXP_REPLACE(X, '^Ste(v|ph)en$', '', 2, 0, 'i')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getRegularDBFunctionSymbol("REGEXP_REPLACE", 6), v,
                TERM_FACTORY.getDBStringConstant("^Ste(v|ph)en$"),
                TERM_FACTORY.getDBStringConstant(""),
                TERM_FACTORY.getDBIntegerConstant(2),
                TERM_FACTORY.getDBIntegerConstant(0),
                TERM_FACTORY.getDBStringConstant("i")), translation);
    }

    @Test
    public void function_REGEXP_REPLACE_7_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("REGEXP_REPLACE(X, '^Ste(v|ph)en$', '', 1, 0, 'i', '')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getRegularDBFunctionSymbol("REGEXP_REPLACE", 7), v,
                TERM_FACTORY.getDBStringConstant("^Ste(v|ph)en$"),
                TERM_FACTORY.getDBStringConstant(""),
                TERM_FACTORY.getDBIntegerConstant(1),
                TERM_FACTORY.getDBIntegerConstant(0),
                TERM_FACTORY.getDBStringConstant("i"),
                TERM_FACTORY.getDBStringConstant("")), translation);
    }

    @Test
    public void function_REPLACE_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("REPLACE(X,'J')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getRegularDBFunctionSymbol("REPLACE", 2), v,
                TERM_FACTORY.getDBStringConstant("J")), translation);
    }

    @Test
    public void function_REPLACE_3_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("REPLACE(X, 'J', 'BL')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getRegularDBFunctionSymbol("REPLACE", 3), v,
                TERM_FACTORY.getDBStringConstant("J"),
                TERM_FACTORY.getDBStringConstant("BL")), translation);
    }

    @Test
    public void function_REPLACE_4_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("REPLACE(X, 'J', 'BL', 'i')", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getRegularDBFunctionSymbol("REPLACE", 4), v,
                TERM_FACTORY.getDBStringConstant("J"),
                TERM_FACTORY.getDBStringConstant("BL"),
                TERM_FACTORY.getDBStringConstant("i")), translation);
    }

    @Test
    public void function_SUBSTR_2_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SUBSTR(X, 1)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getRegularDBFunctionSymbol("SUBSTR", 2), v,
                TERM_FACTORY.getDBConstant("1", dbLongType)), translation);
    }

    @Test
    public void function_SUBSTR_3_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SUBSTR(X, 1, 2)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getRegularDBFunctionSymbol("SUBSTR", 3), v,
                TERM_FACTORY.getDBConstant("1", dbLongType),
                TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test
    public void function_SQL_SUBSTRING2_test() throws Exception {
        // SQL:99: SUBSTRING <left paren> <character value expression> FROM <start position> [ FOR <string length> ] <right paren>
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SUBSTRING(X FROM 1)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));
        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getDBSubString2(), v,
                TERM_FACTORY.getDBConstant("1", dbLongType)), translation);
    }

    @Test
    public void function_SQL_SUBSTRING3_test() throws Exception {
        // SQL:99: SUBSTRING <left paren> <character value expression> FROM <start position> [ FOR <string length> ] <right paren>
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SUBSTRING(X FROM 1 FOR 2)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));
        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getDBSubString3(), v,
                TERM_FACTORY.getDBConstant("1", dbLongType),
                TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test
    public void function_SUBSTRING_2_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SUBSTRING(X, 1)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getDBSubString2(), v,
                TERM_FACTORY.getDBConstant("1", dbLongType)), translation);
    }

    @Test
    public void function_SUBSTRING_3_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("SUBSTRING(X, 1, 2)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getDBSubString3(), v,
                TERM_FACTORY.getDBConstant("1", dbLongType),
                TERM_FACTORY.getDBConstant("2", dbLongType)), translation);
    }

    @Test
    public void function_LCASE_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("LCASE(X)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getDBLower(), v), translation);
    }

    @Test
    public void function_LOWER_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("LOWER(X)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getDBLower(), v), translation);
    }

    @Test
    public void function_UCASE_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("UCASE(X)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getDBUpper(), v), translation);
    }

    @Test
    public void function_UPPER_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("UPPER(X)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(DB_FS_FACTORY.getDBUpper(), v), translation);
    }

    @Test
    public void function_LENGTH_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("LENGTH(X)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        DBFunctionSymbol lengthFunctionSymbol = DB_FS_FACTORY.getRegularDBFunctionSymbol("LENGTH", 1);
        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(lengthFunctionSymbol, v), translation);
    }

    @Test
    public void function_LEN_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("LEN(X)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        DBFunctionSymbol lenFunctionSymbol = DB_FS_FACTORY.getRegularDBFunctionSymbol("LEN", 1);
        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(lenFunctionSymbol, v), translation);
    }

    @Test
    public void function_CONVERT_test() throws Exception {
        Variable v = TERM_FACTORY.getVariable("x0");
        ImmutableTerm translation = parseTerm("CONVERT(VARCHAR, X)", ImmutableMap.of(
                IDFAC.createAttributeID("X"), v));

        DBFunctionSymbol castFunctionSymbol = DB_FS_FACTORY.getDBCastFunctionSymbol(DB_TYPE_FACTORY.getDBTermType("VARCHAR"));
        assertEquals(TERM_FACTORY.getImmutableFunctionalTerm(castFunctionSymbol, v), translation);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "F(1 IGNORE NULLS)", "F(1 RESPECT NULLS)", "F(1) IGNORE NULLS",
            "F(1 HAVING MAX 2)", "F(1 LIMIT 2)", "F(1)(2)",
            "F(1 ON OVERFLOW TRUNCATE)", "F(1 SEPARATOR ',')", "APPROXIMATE F(1)",
            "RAND() IGNORE NULLS", "POSITION('a' IN 'abc')",
            "OVERLAY('abc' PLACING 'x' FROM 1)",
            "'a' ILIKE 'A'", "'a' LIKE BINARY 'A'", "'a' LIKE 'a' ESCAPE '_'",
            "'a' REGEXP 'a' ESCAPE '_'", "'a' SIMILAR TO 'a' ESCAPE '_'",
            "1 GLOBAL IN (1)", "1 GLOBAL NOT IN (1)",
            "1 BETWEEN SYMMETRIC 2 AND 0",
            "TRIM(LEADING 'a' FROM 'aaa')", "TRIM(TRAILING 'a' FROM 'aaa')",
            "TRIM(BOTH 'a' FROM 'aaa')", "TRIM('a' FROM 'aaa')",
            "SAFE_CAST(1 AS INTEGER)", "CAST(1 AS INTEGER FORMAT '9')",
            "TRY_CONVERT(INTEGER, 1)", "CONVERT(1 USING utf8)",
            "CAST('abc' AS CHAR(1))", "CAST(1 AS DECIMAL(4, 2))",
            "CAST(1 AS INTEGER UNSIGNED)", "CAST(1 AS INTEGER[])", "CONVERT(VARCHAR(50), 1)",
            "TIMESTAMPTZ '2000-01-01 00:00:00+01'", "DATETIME '2000-01-01 00:00:00'",
            "N'abc'", "E'abc'", "B'101'", "R'abc'", "_utf8'abc'",
            "~1", "1 DIV 2", "1 NOTNULL", "CAST(1 AS ROW(x INTEGER, y INTEGER))",
            "0xFFFFFFFFFFFFFFFF", "X'FFFFFFFFFFFFFFFF'", "CAST(1 AS CHAR CHARACTER SET utf8)"
    })
    public void ignored_expression_syntax_is_rejected(String sql) {
        assertThrows(UnsupportedSelectQueryRuntimeException.class, () -> parseTerm(sql, ImmutableMap.of()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1, 2", "1 AS x", "DISTINCT 1", "TOP 1 1",
            "1 INTO X", "1 FROM P UNION SELECT 2 --"})
    public void expression_string_must_not_discard_select_syntax(String sql) {
        assertThrows(UnsupportedSelectQueryException.class, () -> parseTerm(sql, ImmutableMap.of()));
    }

    @ParameterizedTest
    @MethodSource("functionModifiers")
    public void function_modifiers_are_checked_before_specialized_dispatch(Consumer<Function> modifier) {
        for (String name : List.of("F", "RAND", "CONVERT", "COUNT")) {
            Function function = new Function();
            function.setName(name);
            modifier.accept(function);
            assertUnsupported(function);
        }
    }

    private static Stream<Consumer<Function>> functionModifiers() {
        return Stream.of(
                f -> f.setDistinct(true), f -> f.setUnique(true), f -> f.setAllColumns(true),
                f -> f.setOrderByElements(List.of()), f -> f.setKeep(new KeepExpression()),
                f -> f.setAttribute(new Column("x")), f -> f.setEscaped(true),
                f -> f.setHavingClause("MAX", new LongValue(1)),
                f -> f.setNullHandling(Function.NullHandling.IGNORE_NULLS),
                f -> f.setNullHandling(Function.NullHandling.RESPECT_NULLS),
                f -> f.setIgnoreNullsOutside(true), f -> f.setLimit(new Limit()),
                f -> f.setChainedParameters(new ExpressionList<>(new LongValue(1))),
                f -> f.setOnOverflowTruncate("TRUNCATE"), f -> f.setExtraKeyword("BOTH"),
                f -> f.setKeywordArguments(List.of()));
    }

    @Test
    public void inherited_expression_visitors_must_not_discard_subclass_fields() {
        assertUnsupported(new RawFunction("F", "1 AS INTEGER"));
        assertUnsupported(new JsonTableFunction());
        assertUnsupported(new XmlTableFunction());
        assertUnsupported(new TableFunction("F", new LongValue(1)));
        assertUnsupported(new Addition() {});
        assertUnsupported(new Column("x") {});
    }

    @Test
    public void unknown_expression_dispatch_must_not_return_a_child_term() {
        class UnknownExpression extends net.sf.jsqlparser.parser.ASTNodeAccessImpl implements Expression {
            @Override
            public <T, S> T accept(ExpressionVisitor<T> visitor, S context) {
                return new LongValue(1).accept(visitor, context);
            }
        }
        assertUnsupported(new UnknownExpression());
    }

    @Test
    public void new_expression_nodes_are_rejected_even_when_nested() {
        List<Expression> expressions = List.of(
                new DateUnitExpression("DAY"), new KeyExpression(new LongValue(1)),
                new PostgresNamedFunctionParameter("x", new LongValue(1)),
                new DoubleAnd(), new Contains(), new ContainedBy(),
                new MemberOfExpression(new LongValue(1), new LongValue(2)),
                new FunctionAllColumns(new Function()), new Intersects(),
                new RangeExpression(new LongValue(1), new LongValue(2)),
                new TernaryExpression(), new TSQLLeftJoin(), new TSQLRightJoin(),
                new HighExpression(), new LowExpression(), new Plus(new LongValue(1), new LongValue(2)),
                new PriorTo(new LongValue(1), new LongValue(2)),
                new Inverse(), new CosineSimilarity());
        for (Expression expression : expressions) {
            assertUnsupported(expression);
            Function function = new Function();
            function.setName("F");
            function.setParameters(new ExpressionList<>(new LongValue(1), expression));
            assertUnsupported(function);
        }
    }

    @Test
    public void named_function_parameters_must_have_supported_names_and_shape() {
        NamedExpressionList<Expression> named = new NamedExpressionList<>();
        named.add(new StringValue("abc"));
        named.setNames(List.of("", "FROM"));
        Function function = new Function();
        function.setName("SUBSTRING");
        function.setNamedParameters(named);
        assertUnsupported(function);
        function = new Function();
        function.setName("RAND");
        function.setNamedParameters(new NamedExpressionList<>());
        assertUnsupported(function);
        function.setName("F");
        function.setParameters(new ExpressionList<>(new LongValue(1)));
        assertUnsupported(function);
    }

    @Test
    public void in_expression_lists_must_not_discard_row_or_named_metadata() {
        RowConstructor<Expression> row = new RowConstructor<>();
        row.setName("ROW");
        row.add(new LongValue(1));
        NamedExpressionList<Expression> named = new NamedExpressionList<>();
        named.add(new LongValue(1));
        named.setNames(List.of("FROM"));
        for (ExpressionList<?> list : List.of(row, named, new PartitionByClause())) {
            InExpression right = new InExpression();
            right.setLeftExpression(new LongValue(1));
            right.setRightExpression(list);
            assertUnsupported(right);
            InExpression left = new InExpression();
            left.setLeftExpression(list);
            left.setRightExpression(new ParenthesedExpressionList<>(new LongValue(1)));
            assertUnsupported(left);
        }
    }

    @Test
    public void oracle_prior_is_checked_on_all_supported_relational_nodes() {
        List<Expression> expressions = List.of(new LikeExpression(), new SimilarToExpression(),
                new Between(), new InExpression(), new IsNullExpression(),
                new IsBooleanExpression(), new IsDistinctExpression());
        for (Expression expression : expressions) {
            ((SupportsOldOracleJoinSyntax) expression).setOraclePriorPosition(
                    SupportsOldOracleJoinSyntax.ORACLE_PRIOR_START);
            assertUnsupported(expression);
        }
    }

    @Test
    public void column_and_array_access_modifiers_are_rejected() {
        Column oracle = new Column("x");
        oracle.setOldOracleJoinSyntax(SupportsOldOracleJoinSyntax.ORACLE_JOIN_LEFT);
        assertUnsupported(oracle);
        Column returning = new Column("x");
        returning.setReturningQualifier("OLD");
        assertUnsupported(returning);
        returning.setReturningQualifier(null);
        returning.setReturningReferenceType(ReturningReferenceType.values()[0]);
        assertUnsupported(returning);
        Column delimiter = new Column("x");
        delimiter.setTableDelimiter(":");
        assertUnsupported(delimiter);
        Table qualifier = new Table("P");
        qualifier.setTimeTravel("FOR SYSTEM_TIME AS OF '2020-01-01'");
        assertUnsupported(new Column(qualifier, "x"));
        ArrayExpression slice = new ArrayExpression();
        slice.setObjExpression(new LongValue(1));
        slice.setIndexExpression(new LongValue(1));
        slice.setStartIndexExpression(new LongValue(2));
        assertUnsupported(slice);
        slice.setStartIndexExpression(null);
        slice.setStopIndexExpression(new LongValue(3));
        assertUnsupported(slice);
        for (boolean keyword : List.of(false, true)) {
            Column column = new Column("TRUE");
            ArrayConstructor array = new ArrayConstructor();
            array.setExpressions(new ExpressionList<>(new LongValue(1)));
            array.setArrayKeyword(keyword);
            if (!keyword)
                array.setDataType(new ColDataType("INTEGER"));
            column.setArrayConstructor(array);
            assertUnsupported(column);
        }
    }

    @ParameterizedTest
    @MethodSource("castTypeModifiers")
    public void casts_and_conversions_must_not_discard_type_modifiers(Consumer<ColDataType> modifier) {
        ColDataType type = new ColDataType("INTEGER");
        modifier.accept(type);
        CastExpression cast = new CastExpression();
        cast.setColDataType(type);
        cast.setLeftExpression(new LongValue(1));
        assertUnsupported(cast);
        assertUnsupported(new TranscodingFunction(type, new LongValue(1), null));
    }

    private static Stream<Consumer<ColDataType>> castTypeModifiers() {
        return Stream.of(
                t -> t.setArgumentsStringList(List.of("2")),
                t -> t.setArrayData(List.of(2)),
                t -> t.setCharacterSet("utf8"),
                t -> t.setSignedness(ColDataType.Signedness.UNSIGNED),
                t -> t.setZerofill(true),
                t -> t.setPrecision(4),
                t -> t.setScale(2),
                t -> t.setTypeModifiers(List.of(ColDataType.TypeModifier.UNSIGNED)),
                t -> t.setNationalCharacterType(ColDataType.NationalCharacterType.CHAR),
                t -> t.setIntervalQualifier(new IntervalQualifier("DAY", null, "SECOND", null)),
                t -> t.setXmlTypeModifier(new XmlTypeModifier(XmlTypeModifier.Kind.CONTENT, List.of("schema_collection"))));
    }

    @Test
    public void supported_expression_forms_are_preserved() throws Exception {
        assertEquals(parseTerm("1", ImmutableMap.of()), parseTerm("((1))", ImmutableMap.of()));
        assertEquals(parseTerm("CAST(1 AS INTEGER)", ImmutableMap.of()), parseTerm("1::INTEGER", ImmutableMap.of()));
        assertEquals(parseBooleanExpression("1 BETWEEN 0 AND 2", ImmutableMap.of()),
                parseBooleanExpression("1 BETWEEN ASYMMETRIC 0 AND 2", ImmutableMap.of()));
        assertEquals(TERM_FACTORY.getDBStringConstant("it's"), parseTerm("'it''s'", ImmutableMap.of()));
        assertEquals(TERM_FACTORY.getDBStringConstant("it''s"), parseTerm("$$it''s$$", ImmutableMap.of()));
        assertEquals(parseTerm("0xFF", ImmutableMap.of()), parseTerm("0XFF", ImmutableMap.of()));
    }

    private void assertUnsupported(Expression expression) {
        ExpressionParser parser = new ExpressionParser(IDFAC, CORE_SINGLETONS);
        assertThrows(UnsupportedSelectQueryRuntimeException.class,
                () -> parser.parseTerm(expression, RAExpressionAttributes.ofUnqualifiedAttributesMap(ImmutableMap.of())));
    }

    private ImmutableTerm parseTerm(String sql, ImmutableMap<QuotedID, ImmutableTerm> map) throws UnsupportedSelectQueryException, InvalidQueryException {
        ExpressionParser parser = new ExpressionParser(IDFAC, CORE_SINGLETONS);
        return parser.parseTerm(sql, RAExpressionAttributes.ofUnqualifiedAttributesMap(map));
    }

    private ImmutableExpression parseBooleanExpression(String sql, ImmutableMap<QuotedID, ImmutableTerm> map) throws UnsupportedSelectQueryException, InvalidQueryException {
        ExpressionParser parser = new ExpressionParser(IDFAC, CORE_SINGLETONS);
        return parser.parseBooleanExpression(sql, RAExpressionAttributes.ofUnqualifiedAttributesMap(map));
    }
}
