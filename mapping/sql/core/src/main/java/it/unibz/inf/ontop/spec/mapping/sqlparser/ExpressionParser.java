package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBNotFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.UnsupportedSelectQueryRuntimeException;
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
import java.util.function.BinaryOperator;
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
        BooleanExpressionVisitor parser = new BooleanExpressionVisitor(attributes);
        return parser.translate(expression);
    }





    /**
     * This visitor class converts the given SQL Boolean Expression to a Function
     *
     * Exceptions
     *      - UnsupportedOperationException:
     *                  an internal error (due to the unexpected bahaviour of JSQLparser)
     *      - InvalidSelectQueryRuntimeException:
     *                  the input is not a valid mapping query
     *      - UnsupportedSelectQueryRuntimeException:
     *                  the input cannot be converted into a CQ and needs to be wrapped
     *
     */
    private class BooleanExpressionVisitor implements ExpressionVisitor {

        private final TermVisitor termVisitor;

        // CAREFUL: this variable gets reset in each visit method implementation
        // concurrent evaluation is not possible
        private ImmutableList<ImmutableExpression> result;

        BooleanExpressionVisitor(RAExpressionAttributes attributes) {
            termVisitor = new TermVisitor(attributes);
        }

        private ImmutableList<ImmutableExpression> translate(Expression expression) {
            expression.accept(this);
            return this.result;
        }

        // cancel double negation
        private ImmutableExpression negation(ImmutableExpression arg) {
            return (arg.getFunctionSymbol() instanceof DBNotFunctionSymbol)
                    ? (ImmutableExpression)arg.getTerm(0)
                    : termFactory.getDBNot(arg);
        }

        private java.util.function.Function<ImmutableExpression, ImmutableList<ImmutableExpression>> notOperation(boolean isNot) {
            return isNot
                    ? arg -> ImmutableList.of(negation(arg))
                    : arg -> ImmutableList.of(arg);
        }

        private void process(BinaryExpression expression, BiFunction<ImmutableTerm, ImmutableTerm, ImmutableExpression> op) {
            ImmutableTerm leftTerm = termVisitor.getTerm(expression.getLeftExpression());
            ImmutableTerm rightTerm = termVisitor.getTerm(expression.getRightExpression());
            result = ImmutableList.of(op.apply(leftTerm, rightTerm));
        }

        private void processOJ(OldOracleJoinBinaryExpression expression, BiFunction<ImmutableTerm, ImmutableTerm, ImmutableExpression> op) {
            if (expression.getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR)
                throw new UnsupportedSelectQueryRuntimeException("Oracle PRIOR is not supported", expression);

            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                throw new UnsupportedSelectQueryRuntimeException("Old Oracle OUTER JOIN syntax is not supported", expression);

            process(expression, op);
        }

        // ------------------------------------------------------------
        //        BOOLEAN OPERATIONS
        // ------------------------------------------------------------

        @Override
        public void visit(AndExpression expression) { // expression1 AND expression2
            ImmutableList<ImmutableExpression> left = translate(expression.getLeftExpression());
            ImmutableList<ImmutableExpression> right = translate(expression.getRightExpression());
            result = Stream.of(left, right).flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toList());
        }

        @Override
        public void visit(OrExpression expression) { // expression1 OR expression2
            ImmutableExpression left = termFactory.getConjunction(translate(expression.getLeftExpression()));
            ImmutableExpression right = termFactory.getConjunction(translate(expression.getRightExpression()));
            result = ImmutableList.of(termFactory.getDisjunction(left, right));
        }

        @Override
        public void visit(Parenthesis expression) { // (expression)
            result = translate(expression.getExpression());
        }

        @Override
        public void visit(NotExpression expression) { // NOT/! expression
            ImmutableList<ImmutableExpression> subExp = translate(expression.getExpression());
            result = ImmutableList.of(negation(termFactory.getConjunction(subExp)));
        }

        @Override
        public void visit(IsBooleanExpression expression) { // expression IS [NOT] TRUE|FALSE
            ImmutableList<ImmutableExpression> subExp = translate(expression.getLeftExpression());
            result = notOperation(expression.isNot() == expression.isTrue())
                    .apply(termFactory.getConjunction(subExp));
        }


        // ------------------------------------------------------------
        //        RELATIONAL OPERATIONS
        // ------------------------------------------------------------

        @Override
        public void visit(IsNullExpression expression) { // expression IS [NOT] NULL
            ImmutableTerm term = termVisitor.getTerm(expression.getLeftExpression());
            result = notOperation(expression.isNot()).apply(termFactory.getDBIsNull(term));
        }

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
            throw new UnsupportedSelectQueryRuntimeException("Not yet supported", expression);
        }

        @Override
        // MATCH (columns) AGAINST (value [modifiers])
        public void visit(FullTextSearch fullTextSearch) {
            throw new UnsupportedSelectQueryRuntimeException("fullTextSearch is not supported", fullTextSearch);
        }




        // ------------------------------------------------------------
        //        OTHER RELATIONAL OPERATIONS
        // ------------------------------------------------------------

        @Override
        // expression [NOT] BETWEEN expression1 AND expression2
        public void visit(Between expression) {
            ImmutableTerm t = termVisitor.getTerm(expression.getLeftExpression());
            ImmutableTerm t1 = termVisitor.getTerm(expression.getBetweenExpressionStart());
            ImmutableTerm t2 = termVisitor.getTerm(expression.getBetweenExpressionEnd());

            if (expression.isNot()) {
                ImmutableExpression e1 = termFactory.getDBDefaultInequality(LT, t, t1);
                ImmutableExpression e2 = termFactory.getDBDefaultInequality(GT, t, t2);
                result = ImmutableList.of(termFactory.getDisjunction(e1, e2));
            }
            else {
                ImmutableExpression e1 = termFactory.getDBDefaultInequality(GTE, t, t1);
                ImmutableExpression e2 = termFactory.getDBDefaultInequality(LTE, t, t2);
                result = ImmutableList.of(e1, e2);
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

                // cannot be done in parallel
                stream = ((ExpressionList)right).getExpressions().stream()
                        .map(item -> {
                            ImmutableTerm t1 = termVisitor.getTerm(expression.getLeftExpression());
                            ImmutableTerm t2 = termVisitor.getTerm(item);
                            return termFactory.getNotYetTypedEquality(t1, t2);
                        });
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



        // ------------------------------------------------------------
        //        COLUMNS AND FUNCTIONS
        // ------------------------------------------------------------

        @Override
        public void visit(Column expression) {
            // TODO: support boolean "columns"
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(Function expression) {
            ImmutableList<ImmutableTerm> terms = (expression.getParameters() != null)
                    ? expression.getParameters().getExpressions().stream()
                            .map(termVisitor::getTerm)
                            .collect(ImmutableCollectors.toList())
                    : ImmutableList.of();

            DBBooleanFunctionSymbol functionSymbol = dbFunctionSymbolFactory.getRegularDBBooleanFunctionSymbol(
                    expression.getName(),
                    terms.size());

            result = ImmutableList.of(termFactory.getImmutableExpression(functionSymbol, terms));
        }


        // ------------------------------------------------------------
        //        CONSTANT EXPRESSIONS (NOT ALLOWED)
        // ------------------------------------------------------------

        @Override
        public void visit(NullValue expression) {
            throw new UnsupportedSelectQueryRuntimeException("NULL is not supported", expression);
        }

        @Override
        public void visit(DoubleValue expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(LongValue expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(HexValue expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(StringValue expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(DateValue expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(TimeValue expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(TimestampValue expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(IntervalExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        // ------------------------------------------------------------
        //        ARITHMETIC AND STRING OPERATIONS (NOT ALLOWED)
        // ------------------------------------------------------------

        @Override
        public void visit(Addition expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(Subtraction expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(Multiplication expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(Division expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(IntegerDivision expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(Modulo expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(SignedExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(BitwiseRightShift expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(BitwiseLeftShift expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(BitwiseAnd expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(BitwiseOr expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(BitwiseXor expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }


        @Override
        public void visit(Concat expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(ExtractExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(CastExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }


        @Override
        public void visit(KeepExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(MySQLGroupConcat expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(ValueListExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(RowConstructor expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(OracleHint expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(TimeKeyExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(DateTimeLiteralExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(NextValExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(CollateExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(ArrayExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
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
            throw new UnsupportedSelectQueryRuntimeException("CASE is not supported yet", expression);
            // expression.getSwitchExpression();
            // expression.getWhenClauses();
            // expression.getElseExpression();
        }

        @Override
        public void visit(WhenClause expression) {
            throw new UnsupportedSelectQueryRuntimeException("CASE/WHEN is not supported yet", expression);
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
        public void visit(ExistsExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("EXISTS is not supported yet", expression);
        }

        @Override
        public void visit(AllComparisonExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("ALL is not supported yet", expression);
        }

        @Override
        public void visit(AnyComparisonExpression expression) {
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
        public void visit(Matches expression) {
            throw new UnsupportedSelectQueryRuntimeException("Oracle @@ not supported", expression);
            // would be processOJ
        }

        @Override
        public void visit(JsonExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("JSON expressions are not supported", expression);
        }

        @Override
        public void visit(JsonOperator expression) {
            throw new UnsupportedSelectQueryRuntimeException("JsonOperator expressions is not supported", expression);
        }

        @Override
        public void visit(UserVariable expression) { // @variable
            throw new InvalidSelectQueryRuntimeException("User variables are not allowed", expression);
        }

        @Override
        public void visit(NumericBind expression) { // :bind
            throw new InvalidSelectQueryRuntimeException("Numeric binds are not allowed", expression);
        }

        @Override
        public void visit(JdbcParameter expression) {
            throw new InvalidSelectQueryRuntimeException("JDBC parameters are not allowed", expression);
        }

        @Override
        public void visit(JdbcNamedParameter expression) {
            throw new InvalidSelectQueryRuntimeException("JDBC named parameters are not allowed", expression);
        }
    }


    // ---------------------------------------------------------------
    // supported and officially unsupported SQL functions
    // (WARNING: not all combinations of the parameters are supported)
    // ---------------------------------------------------------------

    private final ImmutableMap<String, BiFunction< net.sf.jsqlparser.expression.Function, TermVisitor, ImmutableFunctionalTerm>>
            FUNCTIONS = ImmutableMap.<String, BiFunction<net.sf.jsqlparser.expression.Function, TermVisitor, ImmutableFunctionalTerm>>builder()
            .put("RAND", this::getRAND)
            .put("CONVERT", this::getCONVERT)
            // due to COUNT(*) TODO: support it
            .put("COUNT", this::reject)
            // Array functions changing the cardinality: not yet supported
            //    - From PostgreSQL
            .put("UNNEST", this::reject)
            .put("JSON_EACH", this::reject)
            .put("JSON_EACH_TEXT", this::reject)
            .put("JSON_OBJECT_KEYS", this::reject)
            .put("JSON_POPULATE_RECORDSET", this::reject)
            .put("JSON_ARRAY_ELEMENTS", this::reject)
            .build();

    private ImmutableFunctionalTerm getGenericDBFunction(net.sf.jsqlparser.expression.Function expression, TermVisitor termVisitor) {
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

    private ImmutableFunctionalTerm getCONVERT(net.sf.jsqlparser.expression.Function expression, TermVisitor termVisitor) {
        if (expression.getParameters() == null)
            throw new InvalidSelectQueryRuntimeException("Invalid CONVERT", expression);
        List<Expression> parameters = expression.getParameters().getExpressions();
        if (parameters.size() != 2)
            throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);

        ImmutableTerm term = termVisitor.getTerm(parameters.get(1));
        String datatype = parameters.get(0).toString();
        return termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBTermType(datatype), term);
    }

    private ImmutableFunctionalTerm getRAND(net.sf.jsqlparser.expression.Function expression, TermVisitor termVisitor) {
        if (expression.getParameters() == null)
            return termFactory.getImmutableFunctionalTerm(dbFunctionSymbolFactory.getDBRand(UUID.randomUUID()));

        throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);
    }

    private ImmutableFunctionalTerm reject(net.sf.jsqlparser.expression.Function expression, TermVisitor termVisitor) {
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
        public void visit(net.sf.jsqlparser.expression.Function expression) {
            BiFunction<net.sf.jsqlparser.expression.Function, TermVisitor, ImmutableFunctionalTerm> function
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
            process(expression.toString(), dbTypeFactory.getDBDoubleType());
        }

        @Override
        public void visit(LongValue expression) {
            process(expression.getStringValue(), dbTypeFactory.getDBLargeIntegerType());
        }

        @Override
        public void visit(HexValue expression) {
            String value = expression.getValue();
            if (value.startsWith("0x"))
                process(Long.parseLong(value.substring(2), 16) + "", dbTypeFactory.getDBLargeIntegerType());
            else if (value.toUpperCase().startsWith("X'"))
                process(Long.parseLong(value.substring(2, value.length() - 1), 16) + "", dbTypeFactory.getDBLargeIntegerType());
            else
                throw new UnsupportedOperationException("Invalid HEX" + value);
        }

        @Override
        public void visit(StringValue expression) {
            process(expression.getValue(), dbTypeFactory.getDBStringType());
        }

        @Override
        public void visit(DateValue expression) {
            process(expression.getValue().toString(), dbTypeFactory.getDBDateType());
        }

        @Override
        public void visit(TimeValue expression) {
            process(expression.getValue().toString(), dbTypeFactory.getDBTimeType());
        }

        @Override
        public void visit(TimestampValue expression) {
            process(expression.getValue().toString(), dbTypeFactory.getDBDateTimestampType());
        }

        @Override
        public void visit(IntervalExpression expression) {
            // example: INTERVAL '4 5:12' DAY TO MINUTE
            throw new UnsupportedSelectQueryRuntimeException("Temporal INTERVALs are not supported yet", expression);
        }

        private void process(String value, DBTermType termType) {
            result = termFactory.getDBConstant(value, termType);
        }



        // ------------------------------------------------------------
        //        BINARY OPERATIONS
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
            process(expression, (t1, t2) -> termFactory.getImmutableFunctionalTerm(
                    dbFunctionSymbolFactory.getDBConcatOperator(2), t1, t2));
        }

        private void process(BinaryExpression expression, BinaryOperator<ImmutableTerm> op) {
            ImmutableTerm leftTerm = getTerm(expression.getLeftExpression());
            ImmutableTerm rightTerm = getTerm(expression.getRightExpression());
            result = op.apply(leftTerm, rightTerm);
        }

        private BinaryOperator<ImmutableTerm> getArithmeticOperation(BinaryExpression expression) {
            return (t1, t2) -> termFactory.getImmutableFunctionalTerm(
                    dbFunctionSymbolFactory.getUntypedDBMathBinaryOperator(expression.getStringExpression()), t1, t2);
        }

        /*
                UNARY OPERATIONS
         */

        @Override
        public void visit(IsNullExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(IsBooleanExpression isBooleanExpression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", isBooleanExpression);
        }

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
                            dbFunctionSymbolFactory.getUntypedDBMathBinaryOperator(SPARQL.NUMERIC_MULTIPLY),
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
        public void visit(ExtractExpression expression) {
            // Example: EXTRACT(month FROM order_date)
            throw new UnsupportedSelectQueryRuntimeException("EXTRACT is not supported yet", expression);
        }




        @Override
        public void visit(Column expression) {
            QuotedID column = idfac.createAttributeID(expression.getColumnName());
            Table table = expression.getTable();
            RelationID relation = (table != null) && (table.getName() != null)
                    ? idfac.createRelationID(table.getSchemaName(), table.getName())
                    : null;
            QualifiedAttributeID qa = new QualifiedAttributeID(relation, column);
            ImmutableTerm var = attributes.get(qa);

            if (var == null) {
                // can be
                //    - a CONSTANT or
                //    - a PSEUDO-COLUMN like ROWID, ROWNUM or
                //    - a FUNCTION without arguments like USER, CURRENT_DATE

                if (column.equals(idfac.createAttributeID("true")))
                    result = termFactory.getDBBooleanConstant(true);
                else if (column.equals(idfac.createAttributeID("false")))
                    result = termFactory.getDBBooleanConstant(false);
                else
                    throw new InvalidSelectQueryRuntimeException("Unable to find attribute name ", expression);
            }
            else {
                // if it is an attribute name (qualified or not)
                result = var;
            }
        }






        @Override
        public void visit(EqualsTo expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(GreaterThan expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(GreaterThanEquals expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(MinorThan expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(MinorThanEquals expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(NotEqualsTo expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(LikeExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(RegExpMySQLOperator expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override //KEEP (DENSE_RANK FIRST ORDER BY col1)
        public void visit(KeepExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("KeepExpression is not supported yet", expression);

        }

        @Override
        public void visit(MySQLGroupConcat expression) {
            throw new UnsupportedSelectQueryRuntimeException("MySQLGroupConcat is not supported yet", expression);
        }

        @Override
        public void visit(ValueListExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("ValueList is not supported yet", expression);
        }

        @Override
        public void visit(RowConstructor expression) {
            throw new UnsupportedSelectQueryRuntimeException("RowConstructor is not supported yet", expression);
        }

        @Override
        public void visit(OracleHint expression) {
            throw new UnsupportedSelectQueryRuntimeException("OracleHint is not supported yet", expression);
        }

        @Override
        public void visit(TimeKeyExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("TimeKeyExpression is not supported yet", expression);
        }

        @Override
        public void visit(DateTimeLiteralExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("DateTimeLiteralExpression is not supported yet", expression);
        }

        @Override
        public void visit(NotExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(SimilarToExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(RegExpMatchOperator expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(AndExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(OrExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(Between expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(InExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(FullTextSearch expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
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
                BooleanExpressionVisitor bev = new BooleanExpressionVisitor(attributes);
                whenTranslation = w -> termFactory.getConjunction(
                        bev.translate(w.getWhenExpression()));
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
            throw new UnsupportedSelectQueryRuntimeException("BITWISE RIGHT SHIFT is not supported yet", expression);
        }

        @Override
        public void visit(BitwiseLeftShift expression) { // expression1 << expression2
            throw new UnsupportedSelectQueryRuntimeException("BITWISE LEFT SHIFT is not supported yet", expression);
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
