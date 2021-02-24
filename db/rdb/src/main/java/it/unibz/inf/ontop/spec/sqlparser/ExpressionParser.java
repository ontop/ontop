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
import it.unibz.inf.ontop.spec.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryRuntimeException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.*;


/**
 * Created by Roman Kontchakov on 10/11/2016.
 *
 */

public class ExpressionParser {

    private final QuotedIDFactory idfac;
    private final TermFactory termFactory;
    private final DBTypeFactory dbTypeFactory;
    private final DBFunctionSymbolFactory dbFunctionSymbolFactory;

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
            .put("COUNT", this::reject)
            .put("MIN", this::reject)
            .put("MAX", this::reject)
            .put("SUM", this::reject)
            .put("AVG", this::reject)
            // Array functions (PostgreSQL) change cardinality
            .put("UNNEST", this::reject)
            .put("JSON_EACH", this::reject)
            .put("JSON_EACH_TEXT", this::reject)
            .put("JSON_OBJECT_KEYS", this::reject)
            .put("JSON_POPULATE_RECORDSET", this::reject)
            .put("JSON_ARRAY_ELEMENTS", this::reject)
            .build();

    private ImmutableFunctionalTerm getGenericDBFunction(Function expression, TermVisitor termVisitor) {
        if (expression.isDistinct() || expression.isAllColumns())
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);

        if (expression.isEscaped())
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);

        if (expression.getAttribute() != null || expression.getAttributeName() != null)
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);

        if (expression.getKeep() != null)
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);

        ImmutableList<ImmutableTerm> terms;
        if (expression.getParameters() != null) {
            terms = expression.getParameters().getExpressions().stream()
                    .map(termVisitor::getTerm).collect(ImmutableCollectors.toList());
        }
        else if (expression.getNamedParameters() != null) {
            // TODO: handle parameter names as in SUBSTRING(X FROM 1 FOR 2):
            //           "" for X, "FROM" for 1 and "FOR" for 2
            terms = expression.getNamedParameters().getExpressions().stream()
                    .map(termVisitor::getTerm).collect(ImmutableCollectors.toList());
        }
        else
            terms = ImmutableList.of();

        DBFunctionSymbol functionSymbol = dbFunctionSymbolFactory.getRegularDBFunctionSymbol(expression.getName(),
                terms.size());
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
        if (expression.getParameters() == null)
            return termFactory.getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRand(UUID.randomUUID()));

        throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);
    }

    private ImmutableFunctionalTerm reject(Function expression, TermVisitor termVisitor) {
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
    private class TermVisitor implements ExpressionVisitor {

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
            result = termFactory.getDBConstant(expression.getValue(), dbTypeFactory.getDBStringType());
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
                    // strip off ''
                    result = termFactory.getDBConstant(val.substring(1, val.length() - 1), dbTypeFactory.getDBDateType());
                    break;
                case TIME:
                    result = termFactory.getDBConstant(val.substring(1, val.length() - 1), dbTypeFactory.getDBTimeType());
                    break;
                case TIMESTAMP:
                    result = termFactory.getDBConstant(val.substring(1, val.length() - 1), dbTypeFactory.getDBDateTimestampType());
                    break;
                default:
                    throw new UnsupportedOperationException(expression + " is not valid");
            }
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
                    throw new UnsupportedOperationException();
            }
        }

        @Override
        public void visit(ExtractExpression expression) { // EXTRACT(MONTH/YEAR/etc. FROM order_date)
            DBFunctionSymbol extractFunctionSymbol = dbFunctionSymbolFactory.getExtractFunctionSymbol(expression.getName());
            ImmutableTerm arg = getTerm(expression.getExpression());
            result = termFactory.getImmutableFunctionalTerm(extractFunctionSymbol, arg);
        }

        @Override
        public void visit(TimeKeyExpression expression) {
            String str = expression.getStringValue().toUpperCase();
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
            processOJ(expression, (t1, t2) -> termFactory.getDBNot(
                    termFactory.getNotYetTypedEquality(t1, t2)));
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


        // ------------------------------------------------------------
        //        STRING RELATIONAL OPERATIONS
        // ------------------------------------------------------------

        @Override
        // expression1 [NOT] LIKE|ILIKE expression2 [ESCAPE escape]
        public void visit(LikeExpression expression) {
            // TODO: handle isCaseInsensitive() and getEscape()
            if (expression.isNot())
                process(expression, (t1, t2) -> termFactory.getDBNot(
                        termFactory.getImmutableExpression(dbFunctionSymbolFactory.getDBLike(), t1, t2)));
            else
                process(expression, (t1, t2) -> termFactory.getImmutableExpression(dbFunctionSymbolFactory.getDBLike(), t1, t2));
        }

        @Override
        // expression1 RLIKE|REGEXP [BINARY] expression2
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
            process(expression, (t1, t2) -> flags.getValue().isEmpty()
                    ? termFactory.getDBRegexpMatches(ImmutableList.of(t1, t2))
                    : termFactory.getDBRegexpMatches(ImmutableList.of(t1, t2, flags)));
        }

        // POSIX Regular Expressions
        // e.g., https://www.postgresql.org/docs/9.6/static/functions-matching.html#FUNCTIONS-POSIX-REGEXP

        @Override
        public void visit(RegExpMatchOperator expression) { // expression [!]~[*] expression2
            DBConstant flags;
            java.util.function.UnaryOperator<ImmutableExpression> not;
            switch (expression.getOperatorType()) {
                case MATCH_CASESENSITIVE:
                    flags = termFactory.getDBStringConstant("");
                    not = UnaryOperator.identity();
                    break;
                case MATCH_CASEINSENSITIVE:
                    flags = termFactory.getDBStringConstant("i");
                    not = UnaryOperator.identity();
                    break;
                case NOT_MATCH_CASESENSITIVE:
                    flags = termFactory.getDBStringConstant("");
                    not = termFactory::getDBNot;
                    break;
                case NOT_MATCH_CASEINSENSITIVE:
                    flags = termFactory.getDBStringConstant("i");
                    not = termFactory::getDBNot;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            process(expression, (t1, t2) ->
                    not.apply(
                            flags.getValue().isEmpty()
                                    ? termFactory.getDBRegexpMatches(ImmutableList.of(t1, t2))
                                    : termFactory.getDBRegexpMatches(ImmutableList.of(t1, t2, flags))));
        }

        @Override
        // expression1 [NOT] SIMILAR TO expression2 [ESCAPE escape]
        public void visit(SimilarToExpression expression) {
            if (expression.getEscape() != null)
                throw new UnsupportedSelectQueryRuntimeException("SIMILAR TO with escape is not not supported", expression);

            if (expression.isNot())
                process(expression, (t1, t2) -> termFactory.getDBNot(
                            termFactory.getImmutableExpression(dbFunctionSymbolFactory.getDBSimilarTo(), t1, t2)));
            else
                process(expression, (t1, t2) -> termFactory.getImmutableExpression(
                        dbFunctionSymbolFactory.getDBSimilarTo(), t1, t2));
        }

        @Override
        // MATCH (columns) AGAINST (value [modifiers])
        public void visit(FullTextSearch fullTextSearch) {
            throw new UnsupportedSelectQueryRuntimeException("FullTextSearch is not supported", fullTextSearch);
        }






        @Override //KEEP (DENSE_RANK FIRST ORDER BY col1)
        public void visit(KeepExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("KeepExpression is not supported", expression);

        }

        @Override
        public void visit(MySQLGroupConcat expression) {
            throw new UnsupportedSelectQueryRuntimeException("MySQLGroupConcat is not supported", expression);
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
            return (ImmutableExpression)term;
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


        @Override
        //  item-list | expression [NOT] IN item-list
        public void visit(InExpression expression) {

            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                throw new UnsupportedSelectQueryRuntimeException("Oracle OUTER JOIN syntax is not supported", expression);

            if (expression.getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR)
                throw new UnsupportedSelectQueryRuntimeException("Oracle PRIOR syntax is not supported", expression);

            Stream<ImmutableExpression> stream;
            Expression left = expression.getLeftExpression();
            if (left != null) {
                ItemsList right = expression.getRightItemsList();
                // right can be SubSelect, ExpressionList and MultiExpressionList
                if (right instanceof SubSelect)
                    throw new UnsupportedSelectQueryRuntimeException("SubSelect in IN is not supported", expression);

                if (right instanceof MultiExpressionList)
                    throw new InvalidSelectQueryRuntimeException("MultiExpressionList is not allowed with a single expression on the left in IN", expression);

                ImmutableTerm leftTerm = getTerm(expression.getLeftExpression());
                stream = ((ExpressionList)right).getExpressions().stream()
                        .map(item -> termFactory.getNotYetTypedEquality(leftTerm, getTerm(item)));
            }
            else {
                ItemsList list = expression.getLeftItemsList();
                if (!(list instanceof ExpressionList))
                    throw new InvalidSelectQueryRuntimeException("Only ExpressionList is allowed on the left of IN", expression);

                ItemsList right = expression.getRightItemsList();
                // right can be SubSelect, ExpressionList and MultiExpressionList
                if (right instanceof SubSelect)
                    throw new UnsupportedSelectQueryRuntimeException("SubSelect in IN is not supported", expression);

                if (right instanceof ExpressionList)
                    throw new InvalidSelectQueryRuntimeException("ExpressionList is not allowed with an ExpressionList on the left in IN", expression);

                // TODO: check
                /* MultiExpressionList is not supported by JSQLParser

                List<Expression> leftList = ((ExpressionList)list).getExpressions();

                stream = ((MultiExpressionList)right).getExprList().stream().map(el -> {
                    List<Expression> rightList  = el.getExpressions();
                    if (leftList.size() != rightList.size())
                        throw new InvalidSelectQueryException("Mismatch in the length of the lists", expression);

                    return getEqLists(leftList, rightList).reverse().stream()
                            .reduce(null, (a, b) -> (a == null) ? b : FACTORY.getFunctionAND(b, a));
                }); */
                throw new InvalidSelectQueryRuntimeException("not possible in the current JSQLParser", expression);
            }

            // do not use ImmutableCollectors.toList because this cannot be done concurrently
            ImmutableList<ImmutableExpression> equalities = stream.collect(ImmutableCollectors.toList());
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
            ImmutableTerm term = getTerm(expression.getLeftExpression());
            ColDataType type = expression.getType();
            String datatype = type.getDataType();
            result = termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBTermType(datatype), term);
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
        public void visit(AllComparisonExpression expression) {  // ALL sub-select
            throw new UnsupportedSelectQueryRuntimeException("ALL is not supported yet", expression);
        }

        @Override
        public void visit(AnyComparisonExpression expression) { // ANY|SOME sub-select
            throw new UnsupportedSelectQueryRuntimeException("ANY is not supported yet", expression);
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

        @Override
        public void visit(ArrayExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Array is not supported yet", expression);
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
