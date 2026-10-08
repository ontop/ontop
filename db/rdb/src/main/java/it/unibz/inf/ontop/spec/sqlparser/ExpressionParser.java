package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.spec.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.sqlparser.exception.QueryParseException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
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
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.piped.FromQuery;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.*;


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

    public ImmutableExpression parseBooleanExpression(Expression expression, RAExpressionAttributes attributes) {
        TermVisitor visitor = new TermVisitor(attributes);
        return visitor.getExpression(expression);
    }


    public ImmutableTerm parseTerm(String expression, RAExpressionAttributes attributes) throws InvalidQueryException, UnsupportedSelectQueryException {
        return parseTerm(parseJSqlExpression(expression), attributes);
    }

    public ImmutableExpression parseBooleanExpression(String expression, RAExpressionAttributes attributes) throws InvalidQueryException, UnsupportedSelectQueryException {
        return parseBooleanExpression(parseJSqlExpression(expression), attributes);
    }


    private Expression parseJSqlExpression(String expression) throws InvalidQueryException, UnsupportedSelectQueryException {
        try {
            String sqlQuery = "SELECT \n" + expression + "\n FROM fakeTable";
            Select statement = JSqlParserTools.parse(sqlQuery, !idfac.supportsSquareBracketQuotation());
            return statement.getPlainSelect().getSelectItems().get(0).getExpression();
        }
        catch (QueryParseException | InvalidSelectQueryRuntimeException e) {
            throw new InvalidQueryException(e.getMessage());
        }
        catch (UnsupportedSelectQueryRuntimeException e) {
            throw new UnsupportedSelectQueryException(e.getMessage(), expression);
        }
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

        if (expression.getAttribute() != null)
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function: attribute", expression);

        if (expression.isEscaped())
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function: escaped", expression);

        ImmutableList<ImmutableTerm> terms;
        if (expression.getParameters() != null) {
            terms = expression.getParameters().stream()
                    .map(termVisitor::getTerm).collect(ImmutableCollectors.toList());
        }
        else if (expression.getNamedParameters() != null) {
            // TODO: handle parameter names as in SUBSTRING(X FROM 1 FOR 2):
            //           "" for X, "FROM" for 1 and "FOR" for 2
            terms = expression.getNamedParameters().stream()
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
        ExpressionList<?> parameters = expression.getParameters();
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
    protected class TermVisitor implements ExpressionVisitor<Void> {

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
        public <S> Void visit(Function expression, S context) {
            BiFunction<Function, TermVisitor, ImmutableFunctionalTerm> function
                    = FUNCTIONS.getOrDefault(expression.getName().toUpperCase(),
                                    ExpressionParser.this::getGenericDBFunction);

            result = function.apply(expression, this);
            return null;
        }


        // ------------------------------------------------------------
        //        CONSTANT EXPRESSIONS
        // ------------------------------------------------------------

        @Override
        public <S> Void visit(NullValue expression, S context) {
            result = termFactory.getNullConstant();
            return null;
        }

        @Override
        public <S> Void visit(DoubleValue expression, S context) {
            result = termFactory.getDBConstant(expression.toString(), dbTypeFactory.getDBDoubleType());
            return null;
        }

        @Override
        public <S> Void visit(LongValue expression, S context) {
            result = termFactory.getDBConstant(expression.getStringValue(), dbTypeFactory.getDBLargeIntegerType());
            return null;
        }

        @Override
        public <S> Void visit(HexValue expression, S context) {
            String str = expression.getValue();
            long value;
            if (str.startsWith("0x"))
                value = Long.parseLong(str.substring(2), 16);
            else if (str.toUpperCase().startsWith("X'")) // MySQL syntax
                value = Long.parseLong(str.substring(2, str.length() - 1), 16);
            else
                throw new UnsupportedOperationException("Invalid HEX" + str);

            result = termFactory.getDBConstant(String.valueOf(value), dbTypeFactory.getDBLargeIntegerType());
            return null;
        }

        @Override
        public <S> Void visit(StringValue expression, S context) {
            result = termFactory.getDBConstant(expression.getNotExcapedValue(), dbTypeFactory.getDBStringType());
            return null;
        }

        @Override
        public <S> Void visit(DateValue expression, S context) {
            result = termFactory.getDBConstant(expression.getValue().toString(), dbTypeFactory.getDBDateType());
            return null;
        }

        @Override
        public <S> Void visit(TimeValue expression, S context) {
            result = termFactory.getDBConstant(expression.getValue().toString(), dbTypeFactory.getDBTimeType());
            return null;
        }

        @Override
        public <S> Void visit(TimestampValue expression, S context) {
            result = termFactory.getDBConstant(expression.getValue().toString(), dbTypeFactory.getDBDateTimestampType());
            return null;
        }

        @Override
        public <S> Void visit(IntervalExpression expression, S context) {
            // example: INTERVAL '4 5:12' DAY TO MINUTE
            throw new UnsupportedSelectQueryRuntimeException("Temporal INTERVALs are not supported", expression);
        }

        @Override
        public <S> Void visit(DateTimeLiteralExpression expression, S context) {
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
            return null;
        }

        private String stripOffQuotes(String s) {
            if (s.charAt(0) != '\'' || s.charAt(s.length() - 1) != '\'')
                throw new UnsupportedOperationException(s + " is not a valid date-time expression");

            return s.substring(1, s.length() - 1);
        }

        @Override
        public <S> Void visit(TimeKeyExpression expression, S context) {
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
            return null;
        }

        @Override //  expression (AT TIME ZONE tz)*
        public <S> Void visit(TimezoneExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("TimezoneExpression is not supported yet", expression);
        }

        // ------------------------------------------------------------
        //        BINARY OPERATIONS (ARITHMETIC + STRING CONCATENATION)
        // ------------------------------------------------------------

        @Override
        public <S> Void visit(Addition expression, S context) {
            processArithmeticOperation(expression);
            return null;
        }

        @Override
        public <S> Void visit(Subtraction expression, S context) {
            processArithmeticOperation(expression);
            return null;
        }

        @Override
        public <S> Void visit(Multiplication expression, S context) {
            processArithmeticOperation(expression);
            return null;
        }

        @Override
        public <S> Void visit(Division expression, S context) {
            processArithmeticOperation(expression);
            return null;
        }

        @Override
        public <S> Void visit(IntegerDivision expression, S context) {
            processArithmeticOperation(expression);
            return null;
        }

        @Override
        public <S> Void visit(Modulo expression, S context) {
            processArithmeticOperation(expression);
            return null;
        }

        @Override
        public <S> Void visit(Concat expression, S context) {
            process(expression, dbFunctionSymbolFactory.getDBConcatOperator(2));
            return null;
        }

        private void process(BinaryExpression expression, DBFunctionSymbol function) {
            ImmutableTerm leftTerm = getTerm(expression.getLeftExpression());
            ImmutableTerm rightTerm = getTerm(expression.getRightExpression());
            result = termFactory.getImmutableFunctionalTerm(function, leftTerm, rightTerm);
        }

        private void processArithmeticOperation(BinaryExpression expression) {
            DBMathBinaryOperator operator = dbFunctionSymbolFactory.getUntypedDBMathBinaryOperator(expression.getStringExpression());
            process(expression, operator);
        }

        // ------------------------------------------------------------
        //        BITWISE BINARY OPERATIONS (NONE SUPPORTED)
        // ------------------------------------------------------------

        @Override
        public <S> Void visit(BitwiseAnd expression, S context) { // expression1 & expression2
            throw new UnsupportedSelectQueryRuntimeException("Bitwise AND is not supported", expression);
        }

        @Override
        public <S> Void visit(BitwiseOr expression, S context) { // expression1 | expression2
            throw new UnsupportedSelectQueryRuntimeException("Bitwise OR is not supported", expression);
        }

        @Override
        public <S> Void visit(BitwiseXor expression, S context) { // expression1 ^ expression2
            throw new UnsupportedSelectQueryRuntimeException("Bitwise XOR is not supported", expression);
        }

        @Override
        public <S> Void visit(BitwiseRightShift expression, S context) { // expression1 >> expression2
            throw new UnsupportedSelectQueryRuntimeException("BITWISE RIGHT SHIFT is not supported", expression);
        }

        @Override
        public <S> Void visit(BitwiseLeftShift expression, S context) { // expression1 << expression2
            throw new UnsupportedSelectQueryRuntimeException("BITWISE LEFT SHIFT is not supported", expression);
        }


        // ------------------------------------------------------------
        //        UNARY OPERATIONS
        // ------------------------------------------------------------

        @Override
        public <S> Void visit(ExpressionList<? extends Expression> expression, S context) {
            if (!(expression instanceof ParenthesedExpressionList) || expression.size() != 1)
                throw new UnsupportedSelectQueryRuntimeException("ValueList is not supported", expression);
            result = getTerm(expression.get(0));
            return null;
        }


        @Override
        public <S> Void visit(SignedExpression expression, S context) {
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
            return null;
        }

        @Override
        public <S> Void visit(ExtractExpression expression, S context) { // EXTRACT(MONTH/YEAR/etc. FROM order_date)
            DBFunctionSymbol extractFunctionSymbol = dbFunctionSymbolFactory.getExtractFunctionSymbol(expression.getName());
            ImmutableTerm arg = getTerm(expression.getExpression());
            result = termFactory.getImmutableFunctionalTerm(extractFunctionSymbol, arg);
            return null;
        }


        @Override
        public <S> Void visit(Column expression, S context) {
            QuotedID column = idfac.createAttributeID(expression.getColumnName());
            Table table = expression.getTable();
            RelationID relation = (table != null) && (table.getName() != null)
                    ? JSqlParserTools.getRelationId(idfac, expression)
                    : null;
            QualifiedAttributeID qa = new QualifiedAttributeID(relation, column);
            ImmutableTerm var = attributes.get(qa);

            if (var == null) {
                // can be
                //    - a CONSTANT or
                //    - a PSEUDO-COLUMN like ROWID, ROWNUM or
                //    - a FUNCTION without arguments like USER

                if (relation == null && column.equals(idfac.createAttributeID("true")))
                    result = termFactory.getDBBooleanConstant(true);
                else if (relation == null && column.equals(idfac.createAttributeID("false")))
                    result = termFactory.getDBBooleanConstant(false);
                else
                    throw new InvalidSelectQueryRuntimeException("Unable to find attribute "
                            + expression
                            + " (available attributes are " + attributes.getAttributes() + ")", expression);
            }
            else {
                // if it is an attribute name (qualified or not)
                result = var;
            }
            ArrayConstructor array = expression.getArrayConstructor();
            if (array != null) {
                for (Expression index : array.getExpressions()) {
                    if (index instanceof JsonExpression)
                        throw new UnsupportedSelectQueryRuntimeException("Array intervals are not supported", expression);
                    ImmutableTerm arrayTerm = result;
                    result = termFactory.getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBArrayAccess(),
                            arrayTerm, getTerm(index));
                }
            }
            return null;
        }


        @Override // *
        public <S> Void visit(AllColumns expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("* is not supported in this context", expression);
        }

        @Override // T.*
        public <S> Void visit(AllTableColumns expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException(expression.getTable() + ".* is not supported in this context", expression);
        }

        @Override // ALL
        public <S> Void visit(AllValue expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("ALL is not supported in this context", expression);
        }

        /**
         * See for instance https://wiki.postgresql.org/wiki/Is_distinct_from
         */
        @Override
        public <S> Void visit(IsDistinctExpression expression, S context) {
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
            return null;
        }

        @Override
        public <S> Void visit(GeometryDistance expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Geometry distance is not supported in this context", expression);
        }


        // ------------------------------------------
        // RELATIONAL OPERATIONS
        // -----------------------------------------

        @Override
        public <S> Void visit(EqualsTo expression, S context) { // expression1 = expression2 (+Oracle Join)
            processOJ(expression, (t1, t2) -> termFactory.getNotYetTypedEquality(t1, t2));
            return null;
        }

        @Override
        public <S> Void visit(GreaterThan expression, S context) { // expression1 > expression2 (+Oracle Join)
            processOJ(expression, (t1, t2) -> termFactory.getDBDefaultInequality(GT, t1, t2));
            return null;
        }

        @Override
        public <S> Void visit(GreaterThanEquals expression, S context) { // expression1 >= expression2 (+Oracle Join)
            processOJ(expression, (t1, t2) -> termFactory.getDBDefaultInequality(GTE, t1, t2));
            return null;
        }

        @Override
        public <S> Void visit(MinorThan expression, S context) { // expression1 < expression2 (+Oracle Join)
            processOJ(expression, (t1, t2) -> termFactory.getDBDefaultInequality(LT, t1, t2));
            return null;
        }

        @Override
        public <S> Void visit(MinorThanEquals expression, S context) { // expression1 <= expression2 (+Oracle Join)
            processOJ(expression, (t1, t2) -> termFactory.getDBDefaultInequality(LTE, t1, t2));
            return null;
        }

        @Override
        public <S> Void visit(NotEqualsTo expression, S context) { // expression1 <> expression2 (+Oracle Join)
            processOJ(expression, (t1, t2) -> termFactory.getDBNot(termFactory.getNotYetTypedEquality(t1, t2)));
            return null;
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
        public <S> Void visit(LikeExpression expression, S context) {
            switch (expression.getLikeKeyWord()) {
                case RLIKE:
                case REGEXP:
                    process(expression, expression.isNot(),
                            getDBRegexpMatchesFunction(expression.isUseBinary() ? "" : "i"));
                    return null;
                case SIMILAR_TO:
                    if (expression.getEscape() != null)
                        throw new UnsupportedSelectQueryRuntimeException("SIMILAR TO with escape is not not supported", expression);
                    process(expression, expression.isNot(), (t1, t2) ->
                            termFactory.getImmutableExpression(dbFunctionSymbolFactory.getDBSimilarTo(), t1, t2));
                    return null;
                case LIKE:
                case ILIKE:
                    break;
                default:
                    throw new UnsupportedSelectQueryRuntimeException("String matching operator is not supported", expression);
            }
            // TODO: handle isCaseInsensitive() and getEscape()
            process(expression, expression.isNot(), (t1, t2) ->
                    termFactory.getImmutableExpression(dbFunctionSymbolFactory.getDBLike(), t1, t2));
            return null;
        }

        @Override
        // POSIX Regular Expressions
        // e.g., https://www.postgresql.org/docs/9.6/static/functions-matching.html#FUNCTIONS-POSIX-REGEXP
        public <S> Void visit(RegExpMatchOperator expression, S context) { // expression [!]~[*] expression2
            switch (expression.getOperatorType()) {
                case MATCH_CASESENSITIVE:
                    process(expression, false, getDBRegexpMatchesFunction(""));
                    break;
                case MATCH_CASEINSENSITIVE:
                    process(expression, false, getDBRegexpMatchesFunction("i"));
                    break;
                case NOT_MATCH_CASESENSITIVE:
                    process(expression, true, getDBRegexpMatchesFunction(""));
                    break;
                case NOT_MATCH_CASEINSENSITIVE:
                    process(expression, true, getDBRegexpMatchesFunction("i"));
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            return null;
        }

        private BiFunction<ImmutableTerm, ImmutableTerm, ImmutableExpression> getDBRegexpMatchesFunction(String flags) {
            return flags.isEmpty()
                    ? (t1, t2) -> termFactory.getDBRegexpMatches(ImmutableList.of(t1, t2))
                    : (t1, t2) -> termFactory.getDBRegexpMatches(ImmutableList.of(t1, t2, termFactory.getDBStringConstant(flags)));
        }

        @Override
        // expression1 [NOT] SIMILAR TO expression2 [ESCAPE escape]
        public <S> Void visit(SimilarToExpression expression, S context) {
            if (expression.getEscape() != null)
                throw new UnsupportedSelectQueryRuntimeException("SIMILAR TO with escape is not not supported", expression);

            process(expression, expression.isNot(), (t1, t2) ->
                    termFactory.getImmutableExpression(dbFunctionSymbolFactory.getDBSimilarTo(), t1, t2));
            return null;
        }

        @Override
        // MATCH (columns) AGAINST (value [modifiers])
        public <S> Void visit(FullTextSearch fullTextSearch, S context) {
            throw new UnsupportedSelectQueryRuntimeException("FullTextSearch is not supported", fullTextSearch);
        }






        @Override //KEEP (DENSE_RANK FIRST|LAST [ORDER BY columns])
        public <S> Void visit(KeepExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("KEEP expression is not supported", expression);

        }

        @Override // GROUP_CONCAT([DISTINCT] expressions [ORDER BY columns] [SEPARATOR s])
        public <S> Void visit(MySQLGroupConcat expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("MySQL GROUP_CONCAT is not supported", expression);
        }

        @Override
        public <S> Void visit(RowConstructor<? extends Expression> expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("RowConstructor is not supported", expression);
        }

        @Override
        public <S> Void visit(RowGetExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("RowGetExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(OracleHint expression, S context) {
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
        public <S> Void visit(IsNullExpression expression, S context) { // expression IS [NOT] NULL
            ImmutableTerm term = getTerm(expression.getLeftExpression());
            result = notOperation(expression.isNot()).apply(termFactory.getDBIsNull(term));
            return null;
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
        public <S> Void visit(AndExpression expression, S context) { // expression1 AND expression2
            ImmutableExpression left = getExpression(expression.getLeftExpression());
            ImmutableExpression right = getExpression(expression.getRightExpression());
            result = termFactory.getConjunction(left, right);
            return null;
        }

        @Override
        public <S> Void visit(OrExpression expression, S context) { // expression1 OR expression2
            ImmutableExpression left = getExpression(expression.getLeftExpression());
            ImmutableExpression right = getExpression(expression.getRightExpression());
            result = termFactory.getDisjunction(left, right);
            return null;
        }

        @Override
        public <S> Void visit(XorExpression expression, S context) { // expression1 XOR expression2
            throw new UnsupportedSelectQueryRuntimeException("XorExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(NotExpression expression, S context) { // NOT/! expression
            result = negation(getExpression(expression.getExpression()));
            return null;
        }

        @Override
        public <S> Void visit(IsBooleanExpression expression, S context) { // expression IS [NOT] TRUE|FALSE
            result = notOperation(expression.isNot() == expression.isTrue())
                    .apply(getExpression(expression.getLeftExpression()));
            return null;
        }



        // ------------------------------------------------------------
        //        OTHER RELATIONAL OPERATIONS
        // ------------------------------------------------------------

        @Override
        // expression [NOT] BETWEEN expression1 AND expression2
        public <S> Void visit(Between expression, S context) {
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
            return null;
        }

        private ImmutableList<ImmutableTerm> getExpressionsList(Expression expression) {
            if (expression instanceof ExpressionList) {
                ExpressionList<?> expressions = (ExpressionList<?>) expression;
                return expressions.stream()
                        .map(TermVisitor.this::getTerm)
                        .collect(ImmutableCollectors.toList());
            }
            else
                return ImmutableList.of(getTerm(expression));
        }

        @Override
        // Expression [(+)] [NOT] IN (expressions)
        public <S> Void visit(InExpression expression, S context) {

            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                throw new UnsupportedSelectQueryRuntimeException("Oracle OUTER JOIN syntax is not supported", expression);

            // JSQLParser 4.2 supports only NO_ORACLE_PRIOR
            //if (expression.getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR)
            //    throw new UnsupportedSelectQueryRuntimeException("Oracle PRIOR syntax is not supported", expression);

            ImmutableList<ImmutableExpression> equalities;

            Expression rightExpression = expression.getRightExpression();
            if (rightExpression instanceof ExpressionList) {
                ExpressionList<?> rightItemsExpressionList = (ExpressionList<?>) rightExpression;

                ImmutableList<ImmutableTerm> leftList = getExpressionsList(expression.getLeftExpression());

                equalities = rightItemsExpressionList.stream()
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
                if (rightExpression == null)
                    throw new InvalidSelectQueryRuntimeException("Both RightExpression and RightItemsList are missing", expression);

                throw new UnsupportedSelectQueryRuntimeException("Expression on the right in IN is not supported", expression);
            }

            if (equalities.isEmpty())
                throw new InvalidSelectQueryRuntimeException("IN must contain at least one expression", expression);

            result = notOperation(expression.isNot()).apply(termFactory.getDisjunction(equalities));
            return null;
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

        public <S> Void visit(CaseExpression expression, S context) {
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
            return null;
        }

        @Override
        public <S> Void visit(WhenClause expression, S context) { // handled in CaseExpression
            throw new UnsupportedOperationException("Unexpected WHEN: " + expression);
        }


        @Override
        public <S> Void visit(CastExpression expression, S context) { // CAST expression AS type
            if ("TRY_CAST".equalsIgnoreCase(expression.keyword))
                throw new UnsupportedOperationException("TRY_CAST is not supported " + expression);

            if (expression.getColumnDefinitions() != null && !expression.getColumnDefinitions().isEmpty())
                throw new UnsupportedOperationException("RowConstructor is not supported in " + expression);

            ImmutableTerm term = getTerm(expression.getLeftExpression());
            ColDataType type = expression.getColDataType();
            String datatype = type.getDataType().replaceFirst("\\s*\\([^)]*\\)$", "").trim();
            result = termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBTermType(datatype), term);
            return null;
        }


        // ------------------------------------------------------------
        //        SUBQUERIES
        // ------------------------------------------------------------

        @Override
        public <S> Void visit(ParenthesedSelect expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("SubSelect is not supported yet", expression);
        }

        @Override
        // TODO: this probably could be supported
        public <S> Void visit(ExistsExpression expression, S context) { // [NOT] EXISTS expression
            throw new UnsupportedSelectQueryRuntimeException("EXISTS is not supported yet", expression);
        }

        @Override
        public <S> Void visit(AnyComparisonExpression expression, S context) { // ANY|SOME|ALL sub-select
            throw new UnsupportedSelectQueryRuntimeException(expression.getAnyType() + " is not supported yet", expression);
        }




        @Override
        public <S> Void visit(AnalyticExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Analytic expressions is not supported", expression);
        }

        // OracleHierarchicalExpression can only occur in the form of a clause after WHERE
        @Override
        public <S> Void visit(OracleHierarchicalExpression expression, S context) {
            throw new UnsupportedOperationException("Unexpected Oracle START WITH ... CONNECT BY");
        }

        @Override
        public <S> Void visit(Matches expression, S context) { // expression1 @@ expression2
            throw new UnsupportedSelectQueryRuntimeException("Oracle @@ not supported", expression);
            // would be processOJ
        }

        @Override
        public <S> Void visit(JsonExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("JSON expressions are not supported", expression);
        }
        @Override // JSON_ARRAYAGG | JSON_OBJECTAGG
        public <S> Void visit(JsonAggregateFunction expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("JsonAggregateFunction is not supported yet", expression);
        }

        @Override // JSON_OBJECT | JSON_ARRAY
        public <S> Void visit(JsonFunction expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("JsonFunction is not supported yet", expression);
        }


        @Override //  expression'[' index-expression ']' or expression'[' index-expression1 : index-expression2 ']'
        public <S> Void visit(ArrayExpression expression, S context) {
            if (expression.getIndexExpression() == null)
                throw new UnsupportedSelectQueryRuntimeException("Array intervals are not supported", expression);

            ImmutableTerm arrayTerm = getTerm(expression.getObjExpression());
            ImmutableTerm indexTerm = getTerm(expression.getIndexExpression());

            result = termFactory.getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBArrayAccess(), arrayTerm, indexTerm);
            return null;
        }

        @Override // ARRAY[]
        public <S> Void visit(ArrayConstructor expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("ArrayConstructor is not supported yet", expression);
        }

        @Override // variable = expression
        public <S> Void visit(VariableAssignment expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("VariableAssignment is not supported yet", expression);
        }

        @Override // xmlserialize(xmlagg(xmltext(expression) ORDER BY list) AS datatype)
        public <S> Void visit(XMLSerializeExpr expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("XMLSerializeExpr is not supported yet", expression);
        }


        @Override // CONNECT_BY_ROOT
        public <S> Void visit(ConnectByRootOperator expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("CONNECT_BY_ROOT is not supported yet", expression);
        }

        @Override // name => expression
        public <S> Void visit(OracleNamedFunctionParameter expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("OracleNamedFunctionParameter is not supported yet", expression);
        }

        @Override
        public <S> Void visit(NextValExpression expression, S context) { // NEXTVAL FOR
            throw new UnsupportedSelectQueryRuntimeException("NextVal is not supported yet", expression);
        }

        @Override
        public <S> Void visit(CollateExpression expression, S context) { // COLLATE
            throw new UnsupportedSelectQueryRuntimeException("Collate is not supported yet", expression);
        }

        @Override
        public <S> Void visit(JsonOperator expression, S context) { // expression1 @> expression2
            throw new UnsupportedSelectQueryRuntimeException("JSON operators are not supported", expression);
        }

        @Override //SELECT @col FROM table1
        public <S> Void visit(UserVariable expression, S context) {
            throw new InvalidSelectQueryRuntimeException("User variables are not allowed", expression);
        }

        @Override //SELECT a FROM b WHERE c = :1
        public <S> Void visit(NumericBind expression, S context) {
            throw new InvalidSelectQueryRuntimeException("Numeric Binds are not allowed", expression);
        }

        @Override
        public <S> Void visit(JdbcParameter expression, S context) { // ?[parameter]
            throw new InvalidSelectQueryRuntimeException("JDBC parameters are not allowed", expression);
        }

        @Override
        public <S> Void visit(JdbcNamedParameter expression, S context) { // :parameter
            throw new InvalidSelectQueryRuntimeException("JDBC named parameters are not allowed", expression);
        }

        @Override
        public <S> Void visit(BooleanValue expression, S context) {
            result = termFactory.getDBBooleanConstant(expression.getValue());
            return null;
        }

        @Override
        public <S> Void visit(OverlapsCondition expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("OverlapsCondition is not supported", expression);
        }

        @Override
        public <S> Void visit(IncludesExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("IncludesExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(ExcludesExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("ExcludesExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(IsUnknownExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("IsUnknownExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(DoubleAnd expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("DoubleAnd is not supported", expression);
        }

        @Override
        public <S> Void visit(Contains expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Contains is not supported", expression);
        }

        @Override
        public <S> Void visit(ContainedBy expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("ContainedBy is not supported", expression);
        }

        @Override
        public <S> Void visit(MemberOfExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("MemberOfExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(ConnectByPriorOperator expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("ConnectByPriorOperator is not supported", expression);
        }

        @Override
        public <S> Void visit(FunctionAllColumns expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("FunctionAllColumns is not supported", expression);
        }

        @Override
        public <S> Void visit(Intersects expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Intersects is not supported", expression);
        }

        @Override
        public <S> Void visit(Select expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("SubSelect is not supported yet", expression);
        }

        @Override
        public <S> Void visit(TranscodingFunction expression, S context) {
            if (expression.isTranscodeStyle() || expression.getTranscodingName() != null)
                throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);
            result = termFactory.getDBCastFunctionalTerm(
                    dbTypeFactory.getDBTermType(expression.getColDataType().toString().replace(" (", "(")),
                    getTerm(expression.getExpression()));
            return null;
        }

        @Override
        public <S> Void visit(TrimFunction expression, S context) {
            Function function = new Function();
            function.setName("TRIM");
            function.setParameters(expression.getFromExpression() == null
                    ? new ExpressionList<>(expression.getExpression())
                    : new ExpressionList<>(expression.getExpression(), expression.getFromExpression()));
            result = getGenericDBFunction(function, this);
            return null;
        }

        @Override
        public <S> Void visit(RangeExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("RangeExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(TernaryExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("TernaryExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(TSQLLeftJoin expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("TSQLLeftJoin is not supported", expression);
        }

        @Override
        public <S> Void visit(TSQLRightJoin expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("TSQLRightJoin is not supported", expression);
        }

        @Override
        public <S> Void visit(StructType expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("StructType is not supported", expression);
        }

        @Override
        public <S> Void visit(LambdaExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("LambdaExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(HighExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("HighExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(LowExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("LowExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(Plus expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Plus is not supported", expression);
        }

        @Override
        public <S> Void visit(PriorTo expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("PriorTo is not supported", expression);
        }

        @Override
        public <S> Void visit(Inverse expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Inverse is not supported", expression);
        }

        @Override
        public <S> Void visit(CosineSimilarity expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("CosineSimilarity is not supported", expression);
        }

        @Override
        public <S> Void visit(FromQuery expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("FromQuery is not supported", expression);
        }

        @Override
        public <S> Void visit(DateUnitExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("DateUnitExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(KeyExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("KeyExpression is not supported", expression);
        }

        @Override
        public <S> Void visit(PostgresNamedFunctionParameter expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("PostgresNamedFunctionParameter is not supported", expression);
        }

    }
}
