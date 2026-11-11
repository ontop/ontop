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

    // Inherited visitor dispatch must not silently translate additional subclass state.
    private static final Set<Class<?>> TRANSLATED_EXPRESSION_TYPES = Set.of(
            Function.class, NullValue.class, DoubleValue.class, LongValue.class, HexValue.class,
            StringValue.class, DateValue.class, TimeValue.class, TimestampValue.class,
            DateTimeLiteralExpression.class, TimeKeyExpression.class, Addition.class,
            Subtraction.class, Multiplication.class, Division.class, IntegerDivision.class,
            Modulo.class, Concat.class, ExpressionList.class, ParenthesedExpressionList.class,
            Parenthesis.class, SignedExpression.class, ExtractExpression.class, Column.class,
            IsDistinctExpression.class, EqualsTo.class, GreaterThan.class, GreaterThanEquals.class,
            MinorThan.class, MinorThanEquals.class, NotEqualsTo.class, LikeExpression.class,
            RegExpMatchOperator.class, SimilarToExpression.class, IsNullExpression.class,
            AndExpression.class, OrExpression.class, NotExpression.class, IsBooleanExpression.class,
            Between.class, InExpression.class, CaseExpression.class, WhenClause.class,
            CastExpression.class, ArrayExpression.class, BooleanValue.class,
            TranscodingFunction.class, TrimFunction.class);

    private static final Set<Class<?>> REJECTED_EXPRESSION_TYPES = Set.of(
            IntervalExpression.class, TimezoneExpression.class, BitwiseAnd.class, BitwiseOr.class,
            BitwiseXor.class, BitwiseRightShift.class, BitwiseLeftShift.class, AllColumns.class,
            AllTableColumns.class, AllValue.class, GeometryDistance.class, FullTextSearch.class,
            KeepExpression.class, MySQLGroupConcat.class, RowConstructor.class, RowGetExpression.class,
            OracleHint.class, XorExpression.class, Select.class, ExistsExpression.class,
            AnyComparisonExpression.class, AnalyticExpression.class, OracleHierarchicalExpression.class,
            Matches.class, JsonExpression.class, JsonAggregateFunction.class, JsonFunction.class,
            ArrayConstructor.class, VariableAssignment.class, XMLSerializeExpr.class,
            ConnectByRootOperator.class, OracleNamedFunctionParameter.class, NextValExpression.class,
            CollateExpression.class, JsonOperator.class, UserVariable.class, NumericBind.class,
            JdbcParameter.class, JdbcNamedParameter.class, OverlapsCondition.class,
            IncludesExpression.class, ExcludesExpression.class, IsUnknownExpression.class,
            DoubleAnd.class, Contains.class, ContainedBy.class, MemberOfExpression.class,
            ConnectByPriorOperator.class, FunctionAllColumns.class, Intersects.class,
            RangeExpression.class, TernaryExpression.class, TSQLLeftJoin.class, TSQLRightJoin.class,
            StructType.class, LambdaExpression.class, HighExpression.class, LowExpression.class,
            Plus.class, PriorTo.class, Inverse.class, CosineSimilarity.class,
            DateUnitExpression.class, KeyExpression.class, PostgresNamedFunctionParameter.class);

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
            if (!(statement instanceof PlainSelect)
                    || statement.getPlainSelect().getSelectItems().size() != 1)
                throw new UnsupportedSelectQueryRuntimeException("Expected a single SQL expression", statement);
            Expression parsedExpression = statement.getPlainSelect().getSelectItems().get(0).getExpression();
            if (!statement.toString().equals("SELECT " + parsedExpression + " FROM fakeTable"))
                throw new UnsupportedSelectQueryRuntimeException("Unexpected SQL expression modifiers", statement);
            return parsedExpression;
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

    private void checkFunctionModifiers(Function expression) {
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

        if (expression.getHavingClause() != null || expression.getNullHandling() != null
                || expression.isIgnoreNullsOutside() || expression.getLimit() != null
                || expression.getChainedParameters() != null || expression.getOnOverflowTruncate() != null
                || expression.getExtraKeyword() != null || expression.getKeywordArguments() != null)
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function modifiers", expression);

        if (expression.getParameters() != null && expression.getNamedParameters() != null)
            throw new UnsupportedSelectQueryRuntimeException("Conflicting SQL function parameters", expression);
        if (expression.getMultipartName() != null && expression.getMultipartName().size() > 1
                && "APPROXIMATE".equalsIgnoreCase(expression.getMultipartName().get(0)))
            throw new UnsupportedSelectQueryRuntimeException("APPROXIMATE functions are not supported", expression);
        if (expression.getParameters() != null)
            checkExpressionList(expression.getParameters());
    }

    protected ImmutableFunctionalTerm getGenericDBFunction(Function expression, TermVisitor termVisitor) {
        checkFunctionModifiers(expression);
        ImmutableList<ImmutableTerm> terms;
        if (expression.getParameters() != null) {
            terms = expression.getParameters().stream()
                    .map(termVisitor::getTerm).collect(ImmutableCollectors.toList());
        }
        else if (expression.getNamedParameters() != null) {
            List<String> names = expression.getNamedParameters().getNames();
            if (names == null || names.size() != expression.getNamedParameters().size()
                    || !"SUBSTRING".equalsIgnoreCase(expression.getName())
                    || !(List.of("", "FROM").equals(names) || List.of("", "FROM", "FOR").equals(names)))
                throw new UnsupportedSelectQueryRuntimeException("Unsupported named SQL function parameters", expression);
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
        Expression datatypeExpression = parameters.get(0);
        if (!(datatypeExpression instanceof Column))
            throw new UnsupportedSelectQueryRuntimeException("Unsupported CONVERT type", expression);
        Column datatypeColumn = (Column) datatypeExpression;
        if (datatypeColumn.getTable() != null
                || datatypeColumn.getArrayConstructor() != null
                || datatypeColumn.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN
                || datatypeColumn.getReturningReferenceType() != null
                || datatypeColumn.getReturningQualifier() != null)
            throw new UnsupportedSelectQueryRuntimeException("Unsupported CONVERT type", expression);
        String datatype = datatypeColumn.getColumnName();
        return termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBTermType(datatype), term);
    }

    private ImmutableFunctionalTerm getRAND(Function expression, TermVisitor termVisitor) {
        if (expression.getParameters() != null || expression.getNamedParameters() != null)
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

    private static void checkExpressionList(ExpressionList<?> expression) {
        Class<?> type = expression.getClass();
        if (type != ExpressionList.class && type != ParenthesedExpressionList.class && type != Parenthesis.class)
            throw new UnsupportedSelectQueryRuntimeException("Unsupported expression list", expression);
    }

    private static String getCastTypeName(ColDataType type, Expression expression) {
        // The DB type factory normalizes away precision/length, so passing toString() is not sufficient.
        if (type.getCharacterSet() != null || type.getIntervalQualifier() != null
                || !type.getArrayData().isEmpty() || type.getSignedness() != null || type.isZerofill()
                || type.getTypeModifiers() != null && !type.getTypeModifiers().isEmpty()
                || type.getNationalCharacterType() != null || type.getXmlTypeModifier() != null
                || type.getArgumentsStringList() != null && !type.getArgumentsStringList().isEmpty()
                || type.getPrecision() != null || type.getScale() != null
                || type.getDataType().contains("("))
            throw new UnsupportedSelectQueryRuntimeException("Cast type modifiers are not supported", expression);
        return type.getDataType().trim();
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private static boolean isSubclassOfAny(Class<?> expressionClass, Set<Class<?>> classes) {
        return classes.stream().anyMatch(c -> c.isAssignableFrom(expressionClass));
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
    protected class TermVisitor implements ExpressionVisitor<ImmutableTerm> {

        private final RAExpressionAttributes attributes;

        TermVisitor(RAExpressionAttributes attributes) {
            this.attributes = attributes;
        }

        ImmutableTerm getTerm(Expression expression) {
            Class<?> expressionClass = expression.getClass();
            if (!TRANSLATED_EXPRESSION_TYPES.contains(expressionClass)
                    && (isSubclassOfAny(expressionClass, TRANSLATED_EXPRESSION_TYPES)
                    || !isSubclassOfAny(expressionClass, REJECTED_EXPRESSION_TYPES)))
                throw new UnsupportedSelectQueryRuntimeException("Unsupported expression subclass", expression);
            if (expression instanceof SupportsOldOracleJoinSyntax
                    && !(expression instanceof OldOracleJoinBinaryExpression)) {
                SupportsOldOracleJoinSyntax oracle = (SupportsOldOracleJoinSyntax) expression;
                if (oracle.getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR)
                    throw new UnsupportedSelectQueryRuntimeException("Oracle PRIOR is not supported", expression);
                if (oracle.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                    throw new UnsupportedSelectQueryRuntimeException("Oracle OUTER JOIN syntax is not supported", expression);
            }
            ImmutableTerm translatedTerm = expression.accept(this, null);
            if (translatedTerm == null)
                throw new UnsupportedSelectQueryRuntimeException("Expression is not supported", expression);
            return translatedTerm;
        }


        @Override
        public <S> ImmutableTerm visit(Function expression, S context) {
            checkFunctionModifiers(expression);
            BiFunction<Function, TermVisitor, ImmutableFunctionalTerm> function
                    = FUNCTIONS.getOrDefault(expression.getName().toUpperCase(Locale.ROOT),
                                    ExpressionParser.this::getGenericDBFunction);

            return function.apply(expression, this);
        }


        // ------------------------------------------------------------
        //        CONSTANT EXPRESSIONS
        // ------------------------------------------------------------

        @Override
        public <S> ImmutableTerm visit(NullValue expression, S context) {
            return termFactory.getNullConstant();
        }

        @Override
        public <S> ImmutableTerm visit(DoubleValue expression, S context) {
            return termFactory.getDBConstant(expression.toString(), dbTypeFactory.getDBDoubleType());
        }

        @Override
        public <S> ImmutableTerm visit(LongValue expression, S context) {
            return termFactory.getDBConstant(expression.getStringValue(), dbTypeFactory.getDBLargeIntegerType());
        }

        @Override
        public <S> ImmutableTerm visit(HexValue expression, S context) {
            String str = expression.getValue();
            long value;
            try {
                if (startsWithIgnoreCase(str, "0x"))
                    value = Long.parseLong(str.substring(2), 16);
                else if (startsWithIgnoreCase(str, "X'")) // MySQL syntax
                    value = Long.parseLong(str.substring(2, str.length() - 1), 16);
                else
                    throw new UnsupportedSelectQueryRuntimeException("Unsupported HEX literal", expression);
            }
            catch (NumberFormatException e) {
                throw new UnsupportedSelectQueryRuntimeException("HEX literal is out of range", expression);
            }

            return termFactory.getDBConstant(String.valueOf(value), dbTypeFactory.getDBLargeIntegerType());
        }

        @Override
        public <S> ImmutableTerm visit(StringValue expression, S context) {
            if (expression.getPrefix() != null)
                throw new UnsupportedSelectQueryRuntimeException("Prefixed string literals are not supported", expression);
            return termFactory.getDBConstant(expression.getNotExcapedValue(), dbTypeFactory.getDBStringType());
        }

        @Override
        public <S> ImmutableTerm visit(DateValue expression, S context) {
            return termFactory.getDBConstant(expression.getValue().toString(), dbTypeFactory.getDBDateType());
        }

        @Override
        public <S> ImmutableTerm visit(TimeValue expression, S context) {
            return termFactory.getDBConstant(expression.getValue().toString(), dbTypeFactory.getDBTimeType());
        }

        @Override
        public <S> ImmutableTerm visit(TimestampValue expression, S context) {
            return termFactory.getDBConstant(expression.getValue().toString(), dbTypeFactory.getDBDateTimestampType());
        }

        @Override
        public <S> ImmutableTerm visit(IntervalExpression expression, S context) {
            // example: INTERVAL '4 5:12' DAY TO MINUTE
            throw new UnsupportedSelectQueryRuntimeException("Temporal INTERVALs are not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(DateTimeLiteralExpression expression, S context) {
            String val = expression.getValue();
            switch (expression.getType()) {
                case DATE:
                    return termFactory.getDBConstant(stripOffQuotes(val), dbTypeFactory.getDBDateType());
                case TIME:
                    return termFactory.getDBConstant(stripOffQuotes(val), dbTypeFactory.getDBTimeType());
                case TIMESTAMP:
                    return termFactory.getDBConstant(stripOffQuotes(val), dbTypeFactory.getDBDateTimestampType());
                default:
                    throw new UnsupportedSelectQueryRuntimeException("Date-time literal type is not supported", expression);
            }
        }

        private String stripOffQuotes(String s) {
            if (s.charAt(0) != '\'' || s.charAt(s.length() - 1) != '\'')
                throw new UnsupportedOperationException(s + " is not a valid date-time expression");

            return s.substring(1, s.length() - 1);
        }

        @Override
        public <S> ImmutableTerm visit(TimeKeyExpression expression, S context) {
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
            return termFactory.getImmutableFunctionalTerm(functionSymbol);
        }

        @Override //  expression (AT TIME ZONE tz)*
        public <S> ImmutableTerm visit(TimezoneExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("TimezoneExpression is not supported yet", expression);
        }

        // ------------------------------------------------------------
        //        BINARY OPERATIONS (ARITHMETIC + STRING CONCATENATION)
        // ------------------------------------------------------------

        @Override
        public <S> ImmutableTerm visit(Addition expression, S context) {
            return processArithmeticOperation(expression);
        }

        @Override
        public <S> ImmutableTerm visit(Subtraction expression, S context) {
            return processArithmeticOperation(expression);
        }

        @Override
        public <S> ImmutableTerm visit(Multiplication expression, S context) {
            return processArithmeticOperation(expression);
        }

        @Override
        public <S> ImmutableTerm visit(Division expression, S context) {
            return processArithmeticOperation(expression);
        }

        @Override
        public <S> ImmutableTerm visit(IntegerDivision expression, S context) {
            return processArithmeticOperation(expression);
        }

        @Override
        public <S> ImmutableTerm visit(Modulo expression, S context) {
            return processArithmeticOperation(expression);
        }

        @Override
        public <S> ImmutableTerm visit(Concat expression, S context) {
            return process(expression, dbFunctionSymbolFactory.getDBConcatOperator(2));
        }

        private ImmutableTerm process(BinaryExpression expression, DBFunctionSymbol function) {
            ImmutableTerm leftTerm = getTerm(expression.getLeftExpression());
            ImmutableTerm rightTerm = getTerm(expression.getRightExpression());
            return termFactory.getImmutableFunctionalTerm(function, leftTerm, rightTerm);
        }

        private ImmutableTerm processArithmeticOperation(BinaryExpression expression) {
            DBMathBinaryOperator operator;
            try {
                operator = dbFunctionSymbolFactory.getUntypedDBMathBinaryOperator(expression.getStringExpression());
            }
            catch (UnsupportedOperationException e) {
                throw new UnsupportedSelectQueryRuntimeException(e.getMessage(), expression);
            }
            return process(expression, operator);
        }

        // ------------------------------------------------------------
        //        BITWISE BINARY OPERATIONS (NONE SUPPORTED)
        // ------------------------------------------------------------

        @Override
        public <S> ImmutableTerm visit(BitwiseAnd expression, S context) { // expression1 & expression2
            throw new UnsupportedSelectQueryRuntimeException("Bitwise AND is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(BitwiseOr expression, S context) { // expression1 | expression2
            throw new UnsupportedSelectQueryRuntimeException("Bitwise OR is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(BitwiseXor expression, S context) { // expression1 ^ expression2
            throw new UnsupportedSelectQueryRuntimeException("Bitwise XOR is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(BitwiseRightShift expression, S context) { // expression1 >> expression2
            throw new UnsupportedSelectQueryRuntimeException("BITWISE RIGHT SHIFT is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(BitwiseLeftShift expression, S context) { // expression1 << expression2
            throw new UnsupportedSelectQueryRuntimeException("BITWISE LEFT SHIFT is not supported", expression);
        }


        // ------------------------------------------------------------
        //        UNARY OPERATIONS
        // ------------------------------------------------------------

        @Override
        public <S> ImmutableTerm visit(ExpressionList<? extends Expression> expression, S context) {
            if (!(expression instanceof ParenthesedExpressionList) || expression.size() != 1)
                throw new UnsupportedSelectQueryRuntimeException("ValueList is not supported", expression);
            return getTerm(expression.get(0));
        }


        @Override
        public <S> ImmutableTerm visit(SignedExpression expression, S context) {
            ImmutableTerm arg = getTerm(expression.getExpression());
            switch (expression.getSign()) {
                case '-' :
                    return termFactory.getImmutableFunctionalTerm(
                            dbFunctionSymbolFactory.getUntypedDBMathBinaryOperator("*"),
                            termFactory.getDBConstant("-1", dbTypeFactory.getDBLargeIntegerType()),
                            arg);
                case '+':
                    return arg;
                default:
                    throw new UnsupportedSelectQueryRuntimeException("Unary operator is not supported", expression);
            }
        }

        @Override
        public <S> ImmutableTerm visit(ExtractExpression expression, S context) { // EXTRACT(MONTH/YEAR/etc. FROM order_date)
            DBFunctionSymbol extractFunctionSymbol = dbFunctionSymbolFactory.getExtractFunctionSymbol(expression.getName());
            ImmutableTerm arg = getTerm(expression.getExpression());
            return termFactory.getImmutableFunctionalTerm(extractFunctionSymbol, arg);
        }


        @Override
        public <S> ImmutableTerm visit(Column expression, S context) {
            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN
                    || expression.getReturningReferenceType() != null || expression.getReturningQualifier() != null
                    || !".".equals(expression.getTableDelimiter()))
                throw new UnsupportedSelectQueryRuntimeException("Column modifiers are not supported", expression);
            QuotedID column = idfac.createAttributeID(expression.getColumnName());
            Table table = expression.getTable();
            if (table != null)
                JSqlParserTools.validateTableQualifier(table);
            RelationID relation = (table != null) && (table.getName() != null)
                    ? JSqlParserTools.getRelationId(idfac, expression)
                    : null;
            QualifiedAttributeID qa = new QualifiedAttributeID(relation, column);
            ImmutableTerm var = attributes.get(qa);

            ImmutableTerm term;
            if (var == null) {
                // can be
                //    - a CONSTANT or
                //    - a PSEUDO-COLUMN like ROWID, ROWNUM or
                //    - a FUNCTION without arguments like USER

                if (relation == null && column.equals(idfac.createAttributeID("true")))
                    term = termFactory.getDBBooleanConstant(true);
                else if (relation == null && column.equals(idfac.createAttributeID("false")))
                    term = termFactory.getDBBooleanConstant(false);
                else
                    throw new InvalidSelectQueryRuntimeException("Unable to find attribute "
                            + expression
                            + " (available attributes are " + attributes.getAttributes() + ")", expression);
            }
            else {
                // if it is an attribute name (qualified or not)
                term = var;
            }
            ArrayConstructor array = expression.getArrayConstructor();
            if (array != null) {
                if (array.isArrayKeyword() || array.getDataType() != null)
                    throw new UnsupportedSelectQueryRuntimeException("Array access modifiers are not supported", expression);
                checkExpressionList(array.getExpressions());
                for (Expression index : array.getExpressions()) {
                    if (index instanceof JsonExpression)
                        throw new UnsupportedSelectQueryRuntimeException("Array intervals are not supported", expression);
                    term = termFactory.getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBArrayAccess(),
                            term, getTerm(index));
                }
            }
            return term;
        }


        @Override // *
        public <S> ImmutableTerm visit(AllColumns expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("* is not supported in this context", expression);
        }

        @Override // T.*
        public <S> ImmutableTerm visit(AllTableColumns expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException(expression.getTable() + ".* is not supported in this context", expression);
        }

        @Override // ALL
        public <S> ImmutableTerm visit(AllValue expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("ALL is not supported in this context", expression);
        }

        /**
         * See for instance https://wiki.postgresql.org/wiki/Is_distinct_from
         */
        @Override
        public <S> ImmutableTerm visit(IsDistinctExpression expression, S context) {
            return process(expression, expression.isNot(),
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
        public <S> ImmutableTerm visit(GeometryDistance expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Geometry distance is not supported in this context", expression);
        }


        // ------------------------------------------
        // RELATIONAL OPERATIONS
        // -----------------------------------------

        @Override
        public <S> ImmutableTerm visit(EqualsTo expression, S context) { // expression1 = expression2 (+Oracle Join)
            return processOJ(expression, (t1, t2) -> termFactory.getNotYetTypedEquality(t1, t2));
        }

        @Override
        public <S> ImmutableTerm visit(GreaterThan expression, S context) { // expression1 > expression2 (+Oracle Join)
            return processOJ(expression, (t1, t2) -> termFactory.getDBDefaultInequality(GT, t1, t2));
        }

        @Override
        public <S> ImmutableTerm visit(GreaterThanEquals expression, S context) { // expression1 >= expression2 (+Oracle Join)
            return processOJ(expression, (t1, t2) -> termFactory.getDBDefaultInequality(GTE, t1, t2));
        }

        @Override
        public <S> ImmutableTerm visit(MinorThan expression, S context) { // expression1 < expression2 (+Oracle Join)
            return processOJ(expression, (t1, t2) -> termFactory.getDBDefaultInequality(LT, t1, t2));
        }

        @Override
        public <S> ImmutableTerm visit(MinorThanEquals expression, S context) { // expression1 <= expression2 (+Oracle Join)
            return processOJ(expression, (t1, t2) -> termFactory.getDBDefaultInequality(LTE, t1, t2));
        }

        @Override
        public <S> ImmutableTerm visit(NotEqualsTo expression, S context) { // expression1 <> expression2 (+Oracle Join)
            return processOJ(expression, (t1, t2) -> termFactory.getDBNot(termFactory.getNotYetTypedEquality(t1, t2)));
        }

        private ImmutableTerm processOJ(OldOracleJoinBinaryExpression expression, BiFunction<ImmutableTerm, ImmutableTerm, ImmutableExpression> op) {
            if (expression.getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR)
                throw new UnsupportedSelectQueryRuntimeException("Oracle PRIOR is not supported", expression);

            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                throw new UnsupportedSelectQueryRuntimeException("Old Oracle OUTER JOIN syntax is not supported", expression);

            return process(expression, op);
        }

        private ImmutableTerm process(BinaryExpression expression, BiFunction<ImmutableTerm, ImmutableTerm, ImmutableExpression> op) {
            ImmutableTerm leftTerm = getTerm(expression.getLeftExpression());
            ImmutableTerm rightTerm = getTerm(expression.getRightExpression());
            return op.apply(leftTerm, rightTerm);
        }

        private ImmutableTerm process(BinaryExpression expression, boolean not, BiFunction<ImmutableTerm, ImmutableTerm, ImmutableExpression> op) {
            ImmutableTerm leftTerm = getTerm(expression.getLeftExpression());
            ImmutableTerm rightTerm = getTerm(expression.getRightExpression());
            return notOperation(not).apply(op.apply(leftTerm, rightTerm));
        }


        // ------------------------------------------------------------
        //        STRING RELATIONAL OPERATIONS
        // ------------------------------------------------------------

        @Override
        // expression1 [NOT] LIKE|ILIKE expression2 [ESCAPE escape]
        public <S> ImmutableTerm visit(LikeExpression expression, S context) {
            if (expression.getEscape() != null)
                throw new UnsupportedSelectQueryRuntimeException("String matching with ESCAPE is not supported", expression);
            if (expression.isCaseInsensitive()
                    || expression.isUseBinary() && expression.getLikeKeyWord() != LikeExpression.KeyWord.RLIKE
                    && expression.getLikeKeyWord() != LikeExpression.KeyWord.REGEXP)
                throw new UnsupportedSelectQueryRuntimeException("String matching modifiers are not supported", expression);
            switch (expression.getLikeKeyWord()) {
                case RLIKE:
                case REGEXP:
                    return process(expression, expression.isNot(),
                            getDBRegexpMatchesFunction(expression.isUseBinary() ? "" : "i"));
                case SIMILAR_TO:
                    if (expression.getEscape() != null)
                        throw new UnsupportedSelectQueryRuntimeException("SIMILAR TO with escape is not not supported", expression);
                    return process(expression, expression.isNot(), (t1, t2) ->
                            termFactory.getImmutableExpression(dbFunctionSymbolFactory.getDBSimilarTo(), t1, t2));
                case LIKE:
                case ILIKE:
                    break;
                default:
                    throw new UnsupportedSelectQueryRuntimeException("String matching operator is not supported", expression);
            }
            return process(expression, expression.isNot(), (t1, t2) ->
                    termFactory.getImmutableExpression(dbFunctionSymbolFactory.getDBLike(), t1, t2));
        }

        @Override
        // POSIX Regular Expressions
        // e.g., https://www.postgresql.org/docs/9.6/static/functions-matching.html#FUNCTIONS-POSIX-REGEXP
        public <S> ImmutableTerm visit(RegExpMatchOperator expression, S context) { // expression [!]~[*] expression2
            switch (expression.getOperatorType()) {
                case MATCH_CASESENSITIVE:
                    return process(expression, false, getDBRegexpMatchesFunction(""));
                case MATCH_CASEINSENSITIVE:
                    return process(expression, false, getDBRegexpMatchesFunction("i"));
                case NOT_MATCH_CASESENSITIVE:
                    return process(expression, true, getDBRegexpMatchesFunction(""));
                case NOT_MATCH_CASEINSENSITIVE:
                    return process(expression, true, getDBRegexpMatchesFunction("i"));
                default:
                    throw new UnsupportedOperationException();
            }
        }

        private BiFunction<ImmutableTerm, ImmutableTerm, ImmutableExpression> getDBRegexpMatchesFunction(String flags) {
            return flags.isEmpty()
                    ? (t1, t2) -> termFactory.getDBRegexpMatches(ImmutableList.of(t1, t2))
                    : (t1, t2) -> termFactory.getDBRegexpMatches(ImmutableList.of(t1, t2, termFactory.getDBStringConstant(flags)));
        }

        @Override
        // expression1 [NOT] SIMILAR TO expression2 [ESCAPE escape]
        public <S> ImmutableTerm visit(SimilarToExpression expression, S context) {
            if (expression.getEscape() != null)
                throw new UnsupportedSelectQueryRuntimeException("SIMILAR TO with escape is not not supported", expression);

            return process(expression, expression.isNot(), (t1, t2) ->
                    termFactory.getImmutableExpression(dbFunctionSymbolFactory.getDBSimilarTo(), t1, t2));
        }

        @Override
        // MATCH (columns) AGAINST (value [modifiers])
        public <S> ImmutableTerm visit(FullTextSearch fullTextSearch, S context) {
            throw new UnsupportedSelectQueryRuntimeException("FullTextSearch is not supported", fullTextSearch);
        }






        @Override //KEEP (DENSE_RANK FIRST|LAST [ORDER BY columns])
        public <S> ImmutableTerm visit(KeepExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("KEEP expression is not supported", expression);

        }

        @Override // GROUP_CONCAT([DISTINCT] expressions [ORDER BY columns] [SEPARATOR s])
        public <S> ImmutableTerm visit(MySQLGroupConcat expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("MySQL GROUP_CONCAT is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(RowConstructor<? extends Expression> expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("RowConstructor is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(RowGetExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("RowGetExpression is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(OracleHint expression, S context) {
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
        public <S> ImmutableTerm visit(IsNullExpression expression, S context) { // expression IS [NOT] NULL
            if (expression.isUseNotNull())
                throw new UnsupportedSelectQueryRuntimeException("NOTNULL is not supported", expression);
            ImmutableTerm term = getTerm(expression.getLeftExpression());
            return notOperation(expression.isNot()).apply(termFactory.getDBIsNull(term));
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
        public <S> ImmutableTerm visit(AndExpression expression, S context) { // expression1 AND expression2
            ImmutableExpression left = getExpression(expression.getLeftExpression());
            ImmutableExpression right = getExpression(expression.getRightExpression());
            return termFactory.getConjunction(left, right);
        }

        @Override
        public <S> ImmutableTerm visit(OrExpression expression, S context) { // expression1 OR expression2
            ImmutableExpression left = getExpression(expression.getLeftExpression());
            ImmutableExpression right = getExpression(expression.getRightExpression());
            return termFactory.getDisjunction(left, right);
        }

        @Override
        public <S> ImmutableTerm visit(XorExpression expression, S context) { // expression1 XOR expression2
            throw new UnsupportedSelectQueryRuntimeException("XorExpression is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(NotExpression expression, S context) { // NOT/! expression
            return negation(getExpression(expression.getExpression()));
        }

        @Override
        public <S> ImmutableTerm visit(IsBooleanExpression expression, S context) { // expression IS [NOT] TRUE|FALSE
            return notOperation(expression.isNot() == expression.isTrue())
                    .apply(getExpression(expression.getLeftExpression()));
        }



        // ------------------------------------------------------------
        //        OTHER RELATIONAL OPERATIONS
        // ------------------------------------------------------------

        @Override
        // expression [NOT] BETWEEN expression1 AND expression2
        public <S> ImmutableTerm visit(Between expression, S context) {
            if (expression.isUsingSymmetric())
                throw new UnsupportedSelectQueryRuntimeException("BETWEEN SYMMETRIC is not supported", expression);
            ImmutableTerm t = getTerm(expression.getLeftExpression());
            ImmutableTerm t1 = getTerm(expression.getBetweenExpressionStart());
            ImmutableTerm t2 = getTerm(expression.getBetweenExpressionEnd());

            if (expression.isNot()) {
                ImmutableExpression e1 = termFactory.getDBDefaultInequality(LT, t, t1);
                ImmutableExpression e2 = termFactory.getDBDefaultInequality(GT, t, t2);
                return termFactory.getDisjunction(e1, e2);
            }
            else {
                ImmutableExpression e1 = termFactory.getDBDefaultInequality(GTE, t, t1);
                ImmutableExpression e2 = termFactory.getDBDefaultInequality(LTE, t, t2);
                return termFactory.getConjunction(e1, e2);
            }
        }

        private ImmutableList<ImmutableTerm> getExpressionsList(Expression expression) {
            if (expression instanceof ExpressionList) {
                ExpressionList<?> expressions = (ExpressionList<?>) expression;
                checkExpressionList(expressions);
                return expressions.stream()
                        .map(TermVisitor.this::getTerm)
                        .collect(ImmutableCollectors.toList());
            }
            else
                return ImmutableList.of(getTerm(expression));
        }

        @Override
        // Expression [(+)] [NOT] IN (expressions)
        public <S> ImmutableTerm visit(InExpression expression, S context) {
            if (expression.isGlobal())
                throw new UnsupportedSelectQueryRuntimeException("GLOBAL IN is not supported", expression);

            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                throw new UnsupportedSelectQueryRuntimeException("Oracle OUTER JOIN syntax is not supported", expression);

            // JSQLParser 4.2 supports only NO_ORACLE_PRIOR
            //if (expression.getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR)
            //    throw new UnsupportedSelectQueryRuntimeException("Oracle PRIOR syntax is not supported", expression);

            ImmutableList<ImmutableExpression> equalities;

            Expression rightExpression = expression.getRightExpression();
            if (rightExpression instanceof ExpressionList) {
                ExpressionList<?> rightItemsExpressionList = (ExpressionList<?>) rightExpression;
                checkExpressionList(rightItemsExpressionList);

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

            return notOperation(expression.isNot()).apply(termFactory.getDisjunction(equalities));
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

        public <S> ImmutableTerm visit(CaseExpression expression, S context) {
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

            return termFactory.getDBCase(whenPairs.stream(), defaultTerm, false);
        }

        @Override
        public <S> ImmutableTerm visit(WhenClause expression, S context) { // handled in CaseExpression
            throw new UnsupportedSelectQueryRuntimeException("Unexpected WHEN", expression);
        }

        @Override
        public <S> ImmutableTerm visit(CastExpression expression, S context) { // CAST expression AS type
            if (expression.keyword != null && !expression.keyword.isEmpty()
                    && !"CAST".equalsIgnoreCase(expression.keyword))
                throw new UnsupportedSelectQueryRuntimeException("Cast keyword is not supported", expression);

            if (expression.getFormat() != null)
                throw new UnsupportedSelectQueryRuntimeException("CAST FORMAT is not supported", expression);

            if (expression.getColumnDefinitions() != null && !expression.getColumnDefinitions().isEmpty())
                throw new UnsupportedSelectQueryRuntimeException("RowConstructor is not supported in CAST", expression);

            ImmutableTerm term = getTerm(expression.getLeftExpression());
            ColDataType type = expression.getColDataType();
            String datatype = getCastTypeName(type, expression);
            return termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBTermType(datatype), term);
        }


        // ------------------------------------------------------------
        //        SUBQUERIES
        // ------------------------------------------------------------

        @Override
        public <S> ImmutableTerm visit(ParenthesedSelect expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("SubSelect is not supported yet", expression);
        }

        @Override
        // TODO: this probably could be supported
        public <S> ImmutableTerm visit(ExistsExpression expression, S context) { // [NOT] EXISTS expression
            throw new UnsupportedSelectQueryRuntimeException("EXISTS is not supported yet", expression);
        }

        @Override
        public <S> ImmutableTerm visit(AnyComparisonExpression expression, S context) { // ANY|SOME|ALL sub-select
            throw new UnsupportedSelectQueryRuntimeException(expression.getAnyType() + " is not supported yet", expression);
        }




        @Override
        public <S> ImmutableTerm visit(AnalyticExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Analytic expressions is not supported", expression);
        }

        // OracleHierarchicalExpression can only occur in the form of a clause after WHERE
        @Override
        public <S> ImmutableTerm visit(OracleHierarchicalExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Oracle START WITH ... CONNECT BY is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(Matches expression, S context) { // expression1 @@ expression2
            throw new UnsupportedSelectQueryRuntimeException("Oracle @@ not supported", expression);
            // would be processOJ
        }

        @Override
        public <S> ImmutableTerm visit(JsonExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("JSON expressions are not supported", expression);
        }
        @Override // JSON_ARRAYAGG | JSON_OBJECTAGG
        public <S> ImmutableTerm visit(JsonAggregateFunction expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("JsonAggregateFunction is not supported yet", expression);
        }

        @Override // JSON_OBJECT | JSON_ARRAY
        public <S> ImmutableTerm visit(JsonFunction expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("JsonFunction is not supported yet", expression);
        }


        @Override //  expression'[' index-expression ']' or expression'[' index-expression1 : index-expression2 ']'
        public <S> ImmutableTerm visit(ArrayExpression expression, S context) {
            if (expression.getIndexExpression() == null || expression.getStartIndexExpression() != null
                    || expression.getStopIndexExpression() != null)
                throw new UnsupportedSelectQueryRuntimeException("Array intervals are not supported", expression);

            ImmutableTerm arrayTerm = getTerm(expression.getObjExpression());
            ImmutableTerm indexTerm = getTerm(expression.getIndexExpression());

            return termFactory.getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBArrayAccess(), arrayTerm, indexTerm);
        }

        @Override // ARRAY[]
        public <S> ImmutableTerm visit(ArrayConstructor expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("ArrayConstructor is not supported yet", expression);
        }

        @Override // variable = expression
        public <S> ImmutableTerm visit(VariableAssignment expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("VariableAssignment is not supported yet", expression);
        }

        @Override // xmlserialize(xmlagg(xmltext(expression) ORDER BY list) AS datatype)
        public <S> ImmutableTerm visit(XMLSerializeExpr expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("XMLSerializeExpr is not supported yet", expression);
        }


        @Override // CONNECT_BY_ROOT
        public <S> ImmutableTerm visit(ConnectByRootOperator expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("CONNECT_BY_ROOT is not supported yet", expression);
        }

        @Override // name => expression
        public <S> ImmutableTerm visit(OracleNamedFunctionParameter expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("OracleNamedFunctionParameter is not supported yet", expression);
        }

        @Override
        public <S> ImmutableTerm visit(NextValExpression expression, S context) { // NEXTVAL FOR
            throw new UnsupportedSelectQueryRuntimeException("NextVal is not supported yet", expression);
        }

        @Override
        public <S> ImmutableTerm visit(CollateExpression expression, S context) { // COLLATE
            throw new UnsupportedSelectQueryRuntimeException("Collate is not supported yet", expression);
        }

        @Override
        public <S> ImmutableTerm visit(JsonOperator expression, S context) { // expression1 @> expression2
            throw new UnsupportedSelectQueryRuntimeException("JSON operators are not supported", expression);
        }

        @Override //SELECT @col FROM table1
        public <S> ImmutableTerm visit(UserVariable expression, S context) {
            throw new InvalidSelectQueryRuntimeException("User variables are not allowed", expression);
        }

        @Override //SELECT a FROM b WHERE c = :1
        public <S> ImmutableTerm visit(NumericBind expression, S context) {
            throw new InvalidSelectQueryRuntimeException("Numeric Binds are not allowed", expression);
        }

        @Override
        public <S> ImmutableTerm visit(JdbcParameter expression, S context) { // ?[parameter]
            throw new InvalidSelectQueryRuntimeException("JDBC parameters are not allowed", expression);
        }

        @Override
        public <S> ImmutableTerm visit(JdbcNamedParameter expression, S context) { // :parameter
            throw new InvalidSelectQueryRuntimeException("JDBC named parameters are not allowed", expression);
        }

        @Override
        public <S> ImmutableTerm visit(BooleanValue expression, S context) {
            return termFactory.getDBBooleanConstant(expression.getValue());
        }

        @Override
        public <S> ImmutableTerm visit(OverlapsCondition expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("OverlapsCondition is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(IncludesExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("IncludesExpression is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(ExcludesExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("ExcludesExpression is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(IsUnknownExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("IsUnknownExpression is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(DoubleAnd expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("DoubleAnd is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(Contains expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Contains is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(ContainedBy expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("ContainedBy is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(MemberOfExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("MemberOfExpression is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(ConnectByPriorOperator expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("ConnectByPriorOperator is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(FunctionAllColumns expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("FunctionAllColumns is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(Intersects expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Intersects is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(Select expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("SubSelect is not supported yet", expression);
        }

        @Override
        public <S> ImmutableTerm visit(TranscodingFunction expression, S context) {
            if (!"CONVERT".equalsIgnoreCase(expression.getKeyword())
                    || expression.isTranscodeStyle() || expression.getTranscodingName() != null)
                throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);
            return termFactory.getDBCastFunctionalTerm(
                    dbTypeFactory.getDBTermType(getCastTypeName(expression.getColDataType(), expression)),
                    getTerm(expression.getExpression()));
        }

        @Override
        public <S> ImmutableTerm visit(TrimFunction expression, S context) {
            if (expression.getTrimSpecification() != null || expression.isUsingFromKeyword())
                throw new UnsupportedSelectQueryRuntimeException("TRIM modifiers are not supported", expression);
            Function function = new Function();
            function.setName("TRIM");
            function.setParameters(expression.getFromExpression() == null
                    ? new ExpressionList<>(expression.getExpression())
                    : new ExpressionList<>(expression.getExpression(), expression.getFromExpression()));
            return getGenericDBFunction(function, this);
        }

        @Override
        public <S> ImmutableTerm visit(RangeExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("RangeExpression is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(TernaryExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("TernaryExpression is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(TSQLLeftJoin expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("TSQLLeftJoin is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(TSQLRightJoin expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("TSQLRightJoin is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(StructType expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("StructType is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(LambdaExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("LambdaExpression is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(HighExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("HighExpression is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(LowExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("LowExpression is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(Plus expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Plus is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(PriorTo expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("PriorTo is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(Inverse expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("Inverse is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(CosineSimilarity expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("CosineSimilarity is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(FromQuery expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("FromQuery is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(DateUnitExpression expression, S context) {
            // JSqlParser 5.4 parses unquoted columns named YEAR, MONTH, etc. as date units.
            try {
                return getTerm(new Column(expression.toString()));
            }
            catch (InvalidSelectQueryRuntimeException e) {
                throw new UnsupportedSelectQueryRuntimeException(
                        "DateUnitExpression is not supported", expression);
            }
        }

        @Override
        public <S> ImmutableTerm visit(KeyExpression expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("KeyExpression is not supported", expression);
        }

        @Override
        public <S> ImmutableTerm visit(PostgresNamedFunctionParameter expression, S context) {
            throw new UnsupportedSelectQueryRuntimeException("PostgresNamedFunctionParameter is not supported", expression);
        }

    }
}
