package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.spec.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryRuntimeException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.*;


/**
 * Created by Roman Kontchakov on 10/11/2016.
 *
 */

public class ExpressionParser {

    protected final QuotedIDFactory idfac;
    protected final TermFactory termFactory;
    protected final DBTypeFactory dbTypeFactory;
    protected final DBFunctionSymbolFactory dbFunctionSymbolFactory;

    public ExpressionParser(QuotedIDFactory idfac, CoreSingletons coreSingletons) {
        this.idfac = idfac;
        this.termFactory = coreSingletons.getTermFactory();
        this.dbTypeFactory = coreSingletons.getTypeFactory().getDBTypeFactory();
        this.dbFunctionSymbolFactory = coreSingletons.getDBFunctionsymbolFactory();
    }

    public ImmutableTerm parseTerm(Expression expression, RAExpressionAttributes attributes) {
        TermVisitor visitor = new TermVisitor(attributes);
        return visitor.getTerm(expression);
    }

    public ImmutableList<ImmutableExpression> parseBooleanExpression(Expression expression, RAExpressionAttributes attributes) {
        TermVisitor visitor = new TermVisitor(attributes);
        return visitor.getExpression(expression).flattenAND().collect(ImmutableCollectors.toList());
    }






    // ---------------------------------------------------------------
    // supported and officially unsupported SQL functions
    // (WARNING: not all combinations of the parameters are supported)
    // ---------------------------------------------------------------

    private final ImmutableMap<String, BiFunction<Function, TermVisitor, ImmutableFunctionalTerm>>
            FUNCTIONS = ImmutableMap.<String, BiFunction<Function, TermVisitor, ImmutableFunctionalTerm>>builder()
            .put("RAND", this::getRAND) // to make it deterministic
            .put("CONVERT", this::getCONVERT)
            // Aggregate functions
            .put("COUNT", this::getCount)
            .put("MIN", this::getMin)
            .put("MAX", this::getMax)
            .put("SUM", this::getSum)
            .put("AVG", this::getAvg)
            .put("STDDEV", this::getStddev)
            .put("STDDEV_POP", this::getStddevPop)
            .put("STDDEV_SAMP", this::getStddevSamp)
            .put("VARIANCE", this::getVariance)
            .put("VAR_POP", this::getVarPop)
            .put("VAR_SAMP", this::getVarSamp)
            // Array functions (PostgreSQL) change cardinality
            .put("UNNEST", this::reject)
            .put("JSON_EACH", this::reject)
            .put("JSON_EACH_TEXT", this::reject)
            .put("JSON_POPULATE_RECORDSET", this::reject)
            .put("JSON_ARRAY_ELEMENTS", this::reject)
            .build();

    protected ImmutableFunctionalTerm getGenericDBFunction(Function expression, TermVisitor termVisitor) {
        if (expression.isDistinct())
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function: DISTINCT", expression);

        if (expression.isUnique())
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function: UNIQUE", expression);

        if (expression.isAllColumns())
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function: ALL", expression);

        if (expression.getOrderByElements() != null)
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function: ORDER BY expression", expression);

        if (expression.getKeep() != null)
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function: KEEP expression", expression);

        if (expression.getAttribute() != null || expression.getAttributeName() != null)
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function: attribute", expression);

        if (expression.isEscaped())
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function: escaped", expression);

        ImmutableList<ImmutableTerm> terms;
        if (expression.getParameters() != null) {
            terms = expression.getParameters().getExpressions().stream()
                    .map(termVisitor::getTerm).collect(ImmutableCollectors.toList());
        }
        else if (expression.getNamedParameters() != null) {
            // TODO: handle parameter names as in SUBSTRING(X FROM 1 FOR 2):
            //           "" for X, "FROM" for 1 and "FOR" for 2
            terms = expression.getNamedParameters().getExpressions().stream()
                    .map(termVisitor::getTerm)
                    .collect(ImmutableCollectors.toList());
        }
        else
            terms = ImmutableList.of();

        DBFunctionSymbol functionSymbol = dbFunctionSymbolFactory.getRegularDBFunctionSymbol(expression.getName(), terms.size());
        return termFactory.getImmutableFunctionalTerm(functionSymbol, terms);
    }

    private ImmutableFunctionalTerm getCONVERT(Function expression, TermVisitor termVisitor) {
        if (expression.getParameters() == null)
            throw new InvalidSelectQueryRuntimeException("Invalid CONVERT", expression);
        List<Expression> parameters = expression.getParameters().getExpressions();
        if (parameters.size() != 2)
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);

        ImmutableTerm term = termVisitor.getTerm(parameters.get(1));
        String datatype = parameters.get(0).toString();
        return termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBTermType(datatype), term);
    }

    private ImmutableFunctionalTerm getRAND(Function expression, TermVisitor termVisitor) {
        if (expression.getParameters() != null)
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);

        return termFactory.getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRand(UUID.randomUUID()));
    }

    protected ImmutableFunctionalTerm getCount(Function function, TermVisitor termVisitor) {
        return reject(function, termVisitor);
    }

    protected ImmutableFunctionalTerm getSum(Function function, TermVisitor termVisitor) {
        return reject(function, termVisitor);
    }

    protected ImmutableFunctionalTerm getAvg(Function function, TermVisitor termVisitor) {
        return reject(function, termVisitor);
    }

    protected ImmutableFunctionalTerm getMin(Function function, TermVisitor termVisitor) {
        return reject(function, termVisitor);
    }

    protected ImmutableFunctionalTerm getMax(Function function, TermVisitor termVisitor) {
        return reject(function, termVisitor);
    }

    protected ImmutableFunctionalTerm getStddev(Function function, TermVisitor termVisitor) {
        return reject(function, termVisitor);
    }

    protected ImmutableFunctionalTerm getStddevPop(Function function, TermVisitor termVisitor) {
        return reject(function, termVisitor);
    }

    protected ImmutableFunctionalTerm getStddevSamp(Function function, TermVisitor termVisitor) {
        return reject(function, termVisitor);
    }

    protected ImmutableFunctionalTerm getVariance(Function function, TermVisitor termVisitor) {
        return reject(function, termVisitor);
    }

    protected ImmutableFunctionalTerm getVarPop(Function function, TermVisitor termVisitor) {
        return reject(function, termVisitor);
    }

    protected ImmutableFunctionalTerm getVarSamp(Function function, TermVisitor termVisitor) {
        return reject(function, termVisitor);
    }

    protected ImmutableFunctionalTerm reject(Function expression, TermVisitor termVisitor) {
        throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);
    }



    /**
     * This visitor class converts the SQL Expression to a Term
     *
     * Exceptions
     *      - UnsupportedOperationException:
     *                  an internal error (due to the unexpected bahaviour of JSQLParser)
     *      - InvalidSelectQueryRuntimeException:
     *                  the input is not a valid mapping query
     *      - UnsupportedSelectQueryRuntimeException:
     *                  the input cannot be converted into a CQ and needs to be wrapped
     *
     */
    protected class TermVisitor implements ExpressionVisitor {

        private final RAExpressionAttributes attributes;

        // CAREFUL: this variable gets reset in each visit method implementation
        // concurrent evaluation is not possible
        private ImmutableTerm result;

        TermVisitor(RAExpressionAttributes attributes) {
            this.attributes = attributes;
        }

        ImmutableTerm getTerm(Expression expression) {
            expression.accept(this);
            return this.result;
        }


        @Override
        public void visit(Function expression) {
            BiFunction<Function, TermVisitor, ImmutableFunctionalTerm> function
                    = FUNCTIONS.getOrDefault(expression.getName().toUpperCase(),
                                    ExpressionParser.this::getGenericDBFunction);

            result = function.apply(expression, this);
        }


        // ------------------------------------------------------------
        //        CONSTANT EXPRESSIONS
        // ------------------------------------------------------------

        @Override
        public void visit(NullValue expression) {
            result = termFactory.getNullConstant();
        }

        @Override
        public void visit(DoubleValue expression) {
            result = termFactory.getDBConstant(expression.toString(), dbTypeFactory.getDBDoubleType());
        }

        @Override
        public void visit(LongValue expression) {
            result = termFactory.getDBConstant(expression.getStringValue(), dbTypeFactory.getDBLargeIntegerType());
        }

        @Override
        public void visit(HexValue expression) {
            String str = expression.getValue();
            long value;
            if (str.startsWith("0x"))
                value = Long.parseLong(str.substring(2), 16);
            else if (str.toUpperCase().startsWith("X'")) // MySQL syntax
                value = Long.parseLong(str.substring(2, str.length() - 1), 16);
            else
                throw new UnsupportedOperationException("Invalid HEX" + str);

            result = termFactory.getDBConstant(value + "", dbTypeFactory.getDBLargeIntegerType());
        }

        @Override
        public void visit(StringValue expression) {
            result = termFactory.getDBConstant(expression.getNotExcapedValue(), dbTypeFactory.getDBStringType());
        }

        @Override
        public void visit(DateValue expression) {
            result = termFactory.getDBConstant(expression.getValue().toString(), dbTypeFactory.getDBDateType());
        }

        @Override
        public void visit(TimeValue expression) {
            result = termFactory.getDBConstant(expression.getValue().toString(), dbTypeFactory.getDBTimeType());
        }

        @Override
        public void visit(TimestampValue expression) {
            result = termFactory.getDBConstant(expression.getValue().toString(), dbTypeFactory.getDBDateTimestampType());
        }

        @Override
        public void visit(IntervalExpression expression) {
            // example: INTERVAL '4 5:12' DAY TO MINUTE
            throw new UnsupportedSelectQueryRuntimeException("Temporal INTERVALs are not supported", expression);
        }

        @Override
        public void visit(DateTimeLiteralExpression expression) {
            String val = expression.getValue();
            switch (expression.getType()) {
                case DATE:
                    result = termFactory.getDBConstant(stripOffQuotes(val), dbTypeFactory.getDBDateType());
                    break;
                case TIME:
                    result = termFactory.getDBConstant(stripOffQuotes(val), dbTypeFactory.getDBTimeType());
                    break;
                case TIMESTAMP:
                    result = termFactory.getDBConstant(stripOffQuotes(val), dbTypeFactory.getDBDateTimestampType());
                    break;
                default:
                    throw new UnsupportedOperationException(expression + " is not valid");
            }
        }

        private String stripOffQuotes(String s) {
            if (s.charAt(0) != '\'' || s.charAt(s.length() - 1) != '\'')
                throw new UnsupportedOperationException(s + " is not a valid date-time expression");

            return s.substring(1, s.length() - 1);
        }

        @Override
        public void visit(TimeKeyExpression expression) {
            String str = expression.getStringValue().toUpperCase(); // TODO: double-check
            DBFunctionSymbol functionSymbol;
            switch (str) {
                case "CURRENT_TIMESTAMP":
                case "CURRENT_TIMESTAMP()":
                    functionSymbol = dbFunctionSymbolFactory.getCurrentDateTimeSymbol("TIMESTAMP");
                    break;
                case "CURRENT_TIME":
                case "CURRENT_TIME()":
                    functionSymbol = dbFunctionSymbolFactory.getCurrentDateTimeSymbol("TIME");
                    break;
                case "CURRENT_DATE":
                case "CURRENT_DATE()":
                    functionSymbol = dbFunctionSymbolFactory.getCurrentDateTimeSymbol("DATE");
                    break;
                default:
                    throw new UnsupportedSelectQueryRuntimeException("TimeKeyExpression is not supported", expression);
            }
            result = termFactory.getImmutableFunctionalTerm(functionSymbol);
        }

        @Override //  expression (AT TIME ZONE tz)*
        public void visit(TimezoneExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("TimezoneExpression is not supported yet", expression);
        }

        // ------------------------------------------------------------
        //        BINARY OPERATIONS (ARITHMETIC + STRING CONCATENATION)
        // ------------------------------------------------------------

        @Override
        public void visit(Addition expression) {
            process(expression, getArithmeticOperation(expression));
        }

        @Override
        public void visit(Subtraction expression) {
            process(expression, getArithmeticOperation(expression));
        }

        @Override
        public void visit(Multiplication expression) {
            process(expression, getArithmeticOperation(expression));
        }

        @Override
        public void visit(Division expression) {
            process(expression, getArithmeticOperation(expression));
        }

        @Override
        public void visit(IntegerDivision expression) {
            process(expression, getArithmeticOperation(expression));
        }

        @Override
        public void visit(Modulo expression) {
            process(expression, getArithmeticOperation(expression));
        }

        @Override
        public void visit(Concat expression) {
            process(expression, dbFunctionSymbolFactory.getDBConcatOperator(2));
        }

        private void process(BinaryExpression expression, DBFunctionSymbol function) {
            ImmutableTerm leftTerm = getTerm(expression.getLeftExpression());
            ImmutableTerm rightTerm = getTerm(expression.getRightExpression());
            result = termFactory.getImmutableFunctionalTerm(function, leftTerm, rightTerm);
        }

        private DBMathBinaryOperator getArithmeticOperation(BinaryExpression expression) {
            return dbFunctionSymbolFactory.getUntypedDBMathBinaryOperator(expression.getStringExpression());
        }

        // ------------------------------------------------------------
        //        BITWISE BINARY OPERATIONS (NONE SUPPORTED)
        // ------------------------------------------------------------

        @Override
        public void visit(BitwiseAnd expression) { // expression1 & expression2
            throw new UnsupportedSelectQueryRuntimeException("Bitwise AND is not supported", expression);
        }

        @Override
        public void visit(BitwiseOr expression) { // expression1 | expression2
            throw new UnsupportedSelectQueryRuntimeException("Bitwise OR is not supported", expression);
        }

        @Override
        public void visit(BitwiseXor expression) { // expression1 ^ expression2
            throw new UnsupportedSelectQueryRuntimeException("Bitwise XOR is not supported", expression);
        }

        @Override
        public void visit(BitwiseRightShift expression) { // expression1 >> expression2
            throw new UnsupportedSelectQueryRuntimeException("BITWISE RIGHT SHIFT is not supported", expression);
        }

        @Override
        public void visit(BitwiseLeftShift expression) { // expression1 << expression2
            throw new UnsupportedSelectQueryRuntimeException("BITWISE LEFT SHIFT is not supported", expression);
        }


        // ------------------------------------------------------------
        //        UNARY OPERATIONS
        // ------------------------------------------------------------

        @Override
        public void visit(Parenthesis expression) {
            result = getTerm(expression.getExpression());
        }


        @Override
        public void visit(SignedExpression expression) {
            ImmutableTerm arg = getTerm(expression.getExpression());
            switch (expression.getSign()) {
                case '-' :
                    result = termFactory.getImmutableFunctionalTerm(
                            dbFunctionSymbolFactory.getUntypedDBMathBinaryOperator("*"),
                            termFactory.getDBConstant("-1", dbTypeFactory.getDBLargeIntegerType()),
                            arg);
                    break;
                case '+':
                    result = arg;
                    break;
                default:
                    throw new UnsupportedOperationException(expression + " is not valid");
            }
        }

        @Override
        public void visit(ExtractExpression expression) { // EXTRACT(MONTH/YEAR/etc. FROM order_date)
            DBFunctionSymbol extractFunctionSymbol = dbFunctionSymbolFactory.getExtractFunctionSymbol(expression.getName());
            ImmutableTerm arg = getTerm(expression.getExpression());
            result = termFactory.getImmutableFunctionalTerm(extractFunctionSymbol, arg);
        }


        @Override
        public void visit(Column expression) {
            QuotedID column = idfac.createAttributeID(expression.getColumnName());
            Table table = expression.getTable();
            RelationID relation = (table != null) && (table.getName() != null)
                    ? JSqlParserTools.getRelationId(idfac, table)
                    : null;
            QualifiedAttributeID qa = new QualifiedAttributeID(relation, column);
            ImmutableTerm var = attributes.get(qa);

            if (var == null) {
                // can be
                //    - a CONSTANT or
                //    - a PSEUDO-COLUMN like ROWID, ROWNUM or
                //    - a FUNCTION without arguments like USER

                if (column.equals(idfac.createAttributeID("true")))
                    result = termFactory.getDBBooleanConstant(true);
                else if (column.equals(idfac.createAttributeID("false")))
                    result = termFactory.getDBBooleanConstant(false);
                else
                    throw new InvalidSelectQueryRuntimeException("Unable to find attribute "
                            + expression
                            + " (available attributes are " + attributes.asMap().keySet() + ")", expression);
            }
            else {
                // if it is an attribute name (qualified or not)
                result = var;
            }
        }


        @Override // *
        public void visit(AllColumns expression) {
            throw new UnsupportedSelectQueryRuntimeException("* is not supported in this context", expression);
        }

        @Override // T.*
        public void visit(AllTableColumns expression) {
            throw new UnsupportedSelectQueryRuntimeException(expression.getTable() + ".* is not supported in this context", expression);
        }

        @Override // ALL
        public void visit(AllValue expression) {
            throw new UnsupportedSelectQueryRuntimeException("ALL is not supported in this context", expression);
        }

        /**
         * See for instance https://wiki.postgresql.org/wiki/Is_distinct_from
         */
        @Override
        public void visit(IsDistinctExpression expression) {
            process(expression, expression.isNot(),
                    (t1, t2) -> {
                        ImmutableExpression isNotNullT1 = termFactory.getDBIsNotNull(t1);
                        ImmutableExpression isNotNullT2 = termFactory.getDBIsNotNull(t2);
                        ImmutableExpression isNullT1 = termFactory.getDBIsNull(t1);
                        ImmutableExpression isNullT2 = termFactory.getDBIsNull(t2);
                        ImmutableExpression trueExpression = termFactory.getIsTrue(termFactory.getDBBooleanConstant(true));
                        ImmutableExpression falseExpression = termFactory.getIsTrue(termFactory.getDBBooleanConstant(false));

                        return termFactory.getDBBooleanCase(
                                ImmutableMap.of(
                                                termFactory.getConjunction(isNotNullT1, isNotNullT2),
                                                termFactory.getDBNot(termFactory.getNotYetTypedEquality(t1, t2)),
                                                termFactory.getConjunction(isNotNullT1, isNullT2),
                                                trueExpression,
                                                // Added for optimization purposes (if one argument is null while the other is nullable)
                                                termFactory.getConjunction(isNullT1, isNotNullT2),
                                                trueExpression,
                                                termFactory.getConjunction(isNullT1, isNullT2),
                                                falseExpression)
                                        .entrySet().stream(),
                                // Will never be reached
                                trueExpression,
                                false
                        );
                    });
        }

        @Override
        public void visit(GeometryDistance expression) {
            throw new UnsupportedSelectQueryRuntimeException("Geometry distance is not supported in this context", expression);
        }


        // ------------------------------------------
        // RELATIONAL OPERATIONS
        // -----------------------------------------

        @Override
        public void visit(EqualsTo expression) { // expression1 = expression2 (+Oracle Join)
            processOJ(expression, (t1, t2) -> termFactory.getNotYetTypedEquality(t1, t2));
        }

        @Override
        public void visit(GreaterThan expression) { // expression1 > expression2 (+Oracle Join)
            processOJ(expression, (t1, t2) -> termFactory.getDBDefaultInequality(GT, t1, t2));
        }

        @Override
        public void visit(GreaterThanEquals expression) { // expression1 >= expression2 (+Oracle Join)
            processOJ(expression, (t1, t2) -> termFactory.getDBDefaultInequality(GTE, t1, t2));
        }

        @Override
        public void visit(MinorThan expression) { // expression1 < expression2 (+Oracle Join)
            processOJ(expression, (t1, t2) -> termFactory.getDBDefaultInequality(LT, t1, t2));
        }

        @Override
        public void visit(MinorThanEquals expression) { // expression1 <= expression2 (+Oracle Join)
            processOJ(expression, (t1, t2) -> termFactory.getDBDefaultInequality(LTE, t1, t2));
        }

        @Override
        public void visit(NotEqualsTo expression) { // expression1 <> expression2 (+Oracle Join)
            processOJ(expression, (t1, t2) -> termFactory.getDBNot(termFactory.getNotYetTypedEquality(t1, t2)));
        }

        private void processOJ(OldOracleJoinBinaryExpression expression, BiFunction<ImmutableTerm, ImmutableTerm, ImmutableExpression> op) {
            if (expression.getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR)
                throw new UnsupportedSelectQueryRuntimeException("Oracle PRIOR is not supported", expression);

            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                throw new UnsupportedSelectQueryRuntimeException("Old Oracle OUTER JOIN syntax is not supported", expression);

            process(expression, op);
        }

        private void process(BinaryExpression expression, BiFunction<ImmutableTerm, ImmutableTerm, ImmutableExpression> op) {
            ImmutableTerm leftTerm = getTerm(expression.getLeftExpression());
            ImmutableTerm rightTerm = getTerm(expression.getRightExpression());
            result = op.apply(leftTerm, rightTerm);
        }

        private void process(BinaryExpression expression, boolean not, BiFunction<ImmutableTerm, ImmutableTerm, ImmutableExpression> op) {
            ImmutableTerm leftTerm = getTerm(expression.getLeftExpression());
            ImmutableTerm rightTerm = getTerm(expression.getRightExpression());
            result = notOperation(not).apply(op.apply(leftTerm, rightTerm));
        }


        // ------------------------------------------------------------
        //        STRING RELATIONAL OPERATIONS
        // ------------------------------------------------------------

        @Override
        // expression1 [NOT] LIKE|ILIKE expression2 [ESCAPE escape]
        public void visit(LikeExpression expression) {
            // TODO: handle isCaseInsensitive() and getEscape()
            process(expression, expression.isNot(), (t1, t2) ->
                    termFactory.getImmutableExpression(dbFunctionSymbolFactory.getDBLike(), t1, t2));
        }

        @Override
        // expression1 [NOT] RLIKE|REGEXP [BINARY] expression2
        public void visit(RegExpMySQLOperator expression) {
            // TODO: isUseRLike
            DBConstant flags;
            switch (expression.getOperatorType()) {
                case MATCH_CASESENSITIVE:
                    flags = termFactory.getDBStringConstant("");
                    break;
                case MATCH_CASEINSENSITIVE:
                    flags = termFactory.getDBStringConstant("i");
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            process(expression, expression.isNot(), (t1, t2) -> flags.getValue().isEmpty()
                    ? termFactory.getDBRegexpMatches(ImmutableList.of(t1, t2))
                    : termFactory.getDBRegexpMatches(ImmutableList.of(t1, t2, flags)));
        }

        // POSIX Regular Expressions
        // e.g., https://www.postgresql.org/docs/9.6/static/functions-matching.html#FUNCTIONS-POSIX-REGEXP

        @Override
        public void visit(RegExpMatchOperator expression) { // expression [!]~[*] expression2
            DBConstant flags;
            boolean not;
            switch (expression.getOperatorType()) {
                case MATCH_CASESENSITIVE:
                    flags = termFactory.getDBStringConstant("");
                    not = false;
                    break;
                case MATCH_CASEINSENSITIVE:
                    flags = termFactory.getDBStringConstant("i");
                    not = false;
                    break;
                case NOT_MATCH_CASESENSITIVE:
                    flags = termFactory.getDBStringConstant("");
                    not = true;
                    break;
                case NOT_MATCH_CASEINSENSITIVE:
                    flags = termFactory.getDBStringConstant("i");
                    not = true;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            process(expression, not, (t1, t2) -> flags.getValue().isEmpty()
                    ? termFactory.getDBRegexpMatches(ImmutableList.of(t1, t2))
                    : termFactory.getDBRegexpMatches(ImmutableList.of(t1, t2, flags)));
        }

        @Override
        // expression1 [NOT] SIMILAR TO expression2 [ESCAPE escape]
        public void visit(SimilarToExpression expression) {
            if (expression.getEscape() != null)
                throw new UnsupportedSelectQueryRuntimeException("SIMILAR TO with escape is not not supported", expression);

            process(expression, expression.isNot(), (t1, t2) ->
                    termFactory.getImmutableExpression(dbFunctionSymbolFactory.getDBSimilarTo(), t1, t2));
        }

        @Override
        // MATCH (columns) AGAINST (value [modifiers])
        public void visit(FullTextSearch fullTextSearch) {
            throw new UnsupportedSelectQueryRuntimeException("FullTextSearch is not supported", fullTextSearch);
        }






        @Override //KEEP (DENSE_RANK FIRST|LAST [ORDER BY columns])
        public void visit(KeepExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("KEEP expression is not supported", expression);

        }

        @Override // GROUP_CONCAT([DISTINCT] expressions [ORDER BY columns] [SEPARATOR s])
        public void visit(MySQLGroupConcat expression) {
            throw new UnsupportedSelectQueryRuntimeException("MySQL GROUP_CONCAT is not supported", expression);
        }

        @Override
        public void visit(ValueListExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("ValueList is not supported", expression);
        }

        @Override
        public void visit(RowConstructor expression) {
            throw new UnsupportedSelectQueryRuntimeException("RowConstructor is not supported", expression);
        }

        @Override
        public void visit(RowGetExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("RowGetExpression is not supported", expression);
        }

        @Override
        public void visit(OracleHint expression) {
            throw new UnsupportedSelectQueryRuntimeException("OracleHint is not supported", expression);
        }



        // -----------------------------------
        // BOOLEAN EXPRESSIONS
        // -----------------------------------

        // cancel double negation
        private ImmutableExpression negation(ImmutableExpression arg) {
            return (arg.getFunctionSymbol() instanceof DBNotFunctionSymbol)
                    ? (ImmutableExpression)arg.getTerm(0)
                    : termFactory.getDBNot(arg);
        }

        private java.util.function.Function<ImmutableExpression, ImmutableExpression> notOperation(boolean isNot) {
            return isNot ? this::negation : java.util.function.Function.identity();
        }

        @Override
        public void visit(IsNullExpression expression) { // expression IS [NOT] NULL
            ImmutableTerm term = getTerm(expression.getLeftExpression());
            result = notOperation(expression.isNot()).apply(termFactory.getDBIsNull(term));
        }

        private ImmutableExpression getExpression(Expression expression) {
            ImmutableTerm term = getTerm(expression);
            if (term instanceof ImmutableExpression)
                return (ImmutableExpression)term;
            if (term instanceof NonFunctionalTerm)
                return termFactory.getIsTrue((NonFunctionalTerm) term);
            throw new UnsupportedSelectQueryRuntimeException(
                    "Non-boolean functional terms are not supported as conditions", expression);
        }

        @Override
        public void visit(AndExpression expression) { // expression1 AND expression2
            ImmutableExpression left = getExpression(expression.getLeftExpression());
            ImmutableExpression right = getExpression(expression.getRightExpression());
            result = termFactory.getConjunction(left, right);
        }

        @Override
        public void visit(OrExpression expression) { // expression1 OR expression2
            ImmutableExpression left = getExpression(expression.getLeftExpression());
            ImmutableExpression right = getExpression(expression.getRightExpression());
            result = termFactory.getDisjunction(left, right);
        }

        @Override
        public void visit(XorExpression expression) { // expression1 XOR expression2
            throw new UnsupportedSelectQueryRuntimeException("XorExpression is not supported", expression);
        }

        @Override
        public void visit(NotExpression expression) { // NOT/! expression
            result = negation(getExpression(expression.getExpression()));
        }

        @Override
        public void visit(IsBooleanExpression expression) { // expression IS [NOT] TRUE|FALSE
            result = notOperation(expression.isNot() == expression.isTrue())
                    .apply(getExpression(expression.getLeftExpression()));
        }



        // ------------------------------------------------------------
        //        OTHER RELATIONAL OPERATIONS
        // ------------------------------------------------------------

        @Override
        // expression [NOT] BETWEEN expression1 AND expression2
        public void visit(Between expression) {
            ImmutableTerm t = getTerm(expression.getLeftExpression());
            ImmutableTerm t1 = getTerm(expression.getBetweenExpressionStart());
            ImmutableTerm t2 = getTerm(expression.getBetweenExpressionEnd());

            if (expression.isNot()) {
                ImmutableExpression e1 = termFactory.getDBDefaultInequality(LT, t, t1);
                ImmutableExpression e2 = termFactory.getDBDefaultInequality(GT, t, t2);
                result = termFactory.getDisjunction(e1, e2);
            }
            else {
                ImmutableExpression e1 = termFactory.getDBDefaultInequality(GTE, t, t1);
                ImmutableExpression e2 = termFactory.getDBDefaultInequality(LTE, t, t2);
                result = termFactory.getConjunction(e1, e2);
            }
        }

        private ImmutableList<ImmutableTerm> getExpressionsList(Expression expression) {
            if (expression instanceof RowConstructor) {
                RowConstructor leftRowConstructor = (RowConstructor) expression;
                return leftRowConstructor.getExprList().getExpressions().stream()
                        .map(TermVisitor.this::getTerm)
                        .collect(ImmutableCollectors.toList());
            }
            else
                return ImmutableList.of(getTerm(expression));
        }

        @Override
        //  Expression [(+)] [NOT] MultiExpressionList | ItemsList | Expression
        public void visit(InExpression expression) {

            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                throw new UnsupportedSelectQueryRuntimeException("Oracle OUTER JOIN syntax is not supported", expression);

            // JSQLParser 4.2 supports only NO_ORACLE_PRIOR
            //if (expression.getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR)
            //    throw new UnsupportedSelectQueryRuntimeException("Oracle PRIOR syntax is not supported", expression);

            ImmutableList<ImmutableExpression> equalities;

            ItemsList rightItemsList = expression.getRightItemsList();
            if (rightItemsList != null) {
                if (!(rightItemsList instanceof ExpressionList))
                    throw new UnsupportedSelectQueryRuntimeException("Expression on the right in IN is not an ExpressionList", expression);

                ExpressionList rightItemsExpressionList = (ExpressionList) rightItemsList;

                ImmutableList<ImmutableTerm> leftList = getExpressionsList(expression.getLeftExpression());

                equalities = rightItemsExpressionList.getExpressions().stream()
                        .map(this::getExpressionsList)
                        .map(r -> {
                            if (leftList.size() != r.size())
                                throw new InvalidSelectQueryRuntimeException("Mismatch in the length of the lists", expression);

                            return termFactory.getConjunction(IntStream.range(0, leftList.size())
                                            .mapToObj(i -> termFactory.getNotYetTypedEquality(leftList.get(i), r.get(i))))
                                    .get();
                        }).collect(ImmutableCollectors.toList());
            }
            else {
                Expression rightExpression = expression.getRightExpression();
                if (rightExpression == null)
                    throw new InvalidSelectQueryRuntimeException("Both RightExpression and RightItemsList are missing", expression);

                throw new UnsupportedSelectQueryRuntimeException("Expression on the right in IN is not supported", expression);
            }

            if (equalities.isEmpty())
                throw new InvalidSelectQueryRuntimeException("IN must contain at least one expression", expression);

            result = notOperation(expression.isNot()).apply(termFactory.getDisjunction(equalities));
        }




        @Override
        // Syntax:
        //      * CASE
        //      * WHEN condition THEN expression
        //      * [WHEN condition THEN expression]...
        //      * [ELSE expression]
        //      * END
        // or
        //      * CASE expression
        //      * WHEN condition THEN expression
        //      * [WHEN condition THEN expression]...
        //      * [ELSE expression]
        //      * END

        public void visit(CaseExpression expression) {
            java.util.function.Function<WhenClause, ImmutableExpression> whenTranslation;
            if (expression.getSwitchExpression() != null) {
                ImmutableTerm switchTerm = getTerm(expression.getSwitchExpression());
                whenTranslation = w -> termFactory.getNotYetTypedEquality(
                        switchTerm, getTerm(w.getWhenExpression()));
            }
            else {
                whenTranslation = w -> getExpression(w.getWhenExpression());
            }
            ImmutableList<Map.Entry<ImmutableExpression, ImmutableTerm>> whenPairs = expression.getWhenClauses().stream()
                    .map(w -> Maps.immutableEntry(
                            whenTranslation.apply(w),
                            getTerm(w.getThenExpression())))
                    .collect(ImmutableCollectors.toList());

            ImmutableTerm defaultTerm = Optional.ofNullable(expression.getElseExpression())
                    .map(this::getTerm)
                    .orElse(termFactory.getNullConstant());

            result = termFactory.getDBCase(whenPairs.stream(), defaultTerm, false);
        }

        @Override
        public void visit(WhenClause expression) { // handled in CaseExpression
            throw new UnsupportedOperationException("Unexpected WHEN: " + expression);
        }


        @Override
        public void visit(CastExpression expression) { // CAST expression AS type
            if (expression.getRowConstructor() != null)
                throw new UnsupportedOperationException("RowConstructor is not supported in " + expression);

            ImmutableTerm term = getTerm(expression.getLeftExpression());
            ColDataType type = expression.getType();
            String datatype = type.getDataType();
            result = termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBTermType(datatype), term);
        }

        @Override  // MS SQL: TRY_CAST ( expression AS data_type [ ( length ) ] )
                   // PostreSQL: expression::type
        public void visit(TryCastExpression expression) {
            throw new UnsupportedOperationException("TRY_CAST is not supported " + expression);
        }


        // ------------------------------------------------------------
        //        SUBQUERIES
        // ------------------------------------------------------------

        @Override
        public void visit(SubSelect expression) {
            throw new UnsupportedSelectQueryRuntimeException("SubSelect is not supported yet", expression);
        }

        @Override
        // TODO: this probably could be supported
        public void visit(ExistsExpression expression) { // [NOT] EXISTS expression
            throw new UnsupportedSelectQueryRuntimeException("EXISTS is not supported yet", expression);
        }

        @Override
        public void visit(AnyComparisonExpression expression) { // ANY|SOME|ALL sub-select
            throw new UnsupportedSelectQueryRuntimeException(expression.getAnyType() + " is not supported yet", expression);
        }




        @Override
        public void visit(AnalyticExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Analytic expressions is not supported", expression);
        }

        // OracleHierarchicalExpression can only occur in the form of a clause after WHERE
        @Override
        public void visit(OracleHierarchicalExpression expression) {
            throw new UnsupportedOperationException("Unexpected Oracle START WITH ... CONNECT BY");
        }

        @Override
        public void visit(Matches expression) { // expression1 @@ expression2
            throw new UnsupportedSelectQueryRuntimeException("Oracle @@ not supported", expression);
            // would be processOJ
        }

        @Override
        public void visit(JsonExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("JSON expressions are not supported", expression);
        }
        @Override // JSON_ARRAYAGG | JSON_OBJECTAGG
        public void visit(JsonAggregateFunction expression) {
            throw new UnsupportedSelectQueryRuntimeException("JsonAggregateFunction is not supported yet", expression);
        }

        @Override // JSON_OBJECT | JSON_ARRAY
        public void visit(JsonFunction expression) {
            throw new UnsupportedSelectQueryRuntimeException("JsonFunction is not supported yet", expression);
        }


        @Override //  expression'[' index-expression ']' or expression'[' index-expression1 : index-expression2 ']'
        public void visit(ArrayExpression expression) {
            ImmutableTerm arrayTerm = getTerm(expression.getObjExpression());
            ImmutableTerm indexTerm = getTerm(expression.getIndexExpression());

            result = termFactory.getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBArrayAccess(), arrayTerm, indexTerm);
        }

        @Override // ARRAY[]
        public void visit(ArrayConstructor expression) {
            throw new UnsupportedSelectQueryRuntimeException("ArrayConstructor is not supported yet", expression);
        }

        @Override // variable = expression
        public void visit(VariableAssignment expression) {
            throw new UnsupportedSelectQueryRuntimeException("VariableAssignment is not supported yet", expression);
        }

        @Override // xmlserialize(xmlagg(xmltext(expression) ORDER BY list) AS datatype)
        public void visit(XMLSerializeExpr expression) {
            throw new UnsupportedSelectQueryRuntimeException("XMLSerializeExpr is not supported yet", expression);
        }


        @Override // CONNECT_BY_ROOT
        public void visit(ConnectByRootOperator expression) {
            throw new UnsupportedSelectQueryRuntimeException("CONNECT_BY_ROOT is not supported yet", expression);
        }

        @Override // name => expression
        public void visit(OracleNamedFunctionParameter expression) {
            throw new UnsupportedSelectQueryRuntimeException("OracleNamedFunctionParameter is not supported yet", expression);
        }

        @Override
        public void visit(NextValExpression expression) { // NEXTVAL FOR
            throw new UnsupportedSelectQueryRuntimeException("NextVal is not supported yet", expression);
        }

        @Override
        public void visit(CollateExpression expression) { // COLLATE
            throw new UnsupportedSelectQueryRuntimeException("Collate is not supported yet", expression);
        }

        @Override
        public void visit(JsonOperator expression) { // expression1 @> expression2
            throw new UnsupportedSelectQueryRuntimeException("JSON operators are not supported", expression);
        }

        @Override //SELECT @col FROM table1
        public void visit(UserVariable expression) {
            throw new InvalidSelectQueryRuntimeException("User variables are not allowed", expression);
        }

        @Override //SELECT a FROM b WHERE c = :1
        public void visit(NumericBind expression) {
            throw new InvalidSelectQueryRuntimeException("Numeric Binds are not allowed", expression);
        }

        @Override
        public void visit(JdbcParameter expression) { // ?[parameter]
            throw new InvalidSelectQueryRuntimeException("JDBC parameters are not allowed", expression);
        }

        @Override
        public void visit(JdbcNamedParameter expression) { // :parameter
            throw new InvalidSelectQueryRuntimeException("JDBC named parameters are not allowed", expression);
        }
    }
}
