package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.functionsymbol.OperationPredicate;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedSelectQueryRuntimeException;
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
import org.apache.commons.rdf.api.IRI;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;


/**
 * Created by Roman Kontchakov on 10/11/2016.
 *
 */

public class ExpressionParser {

    private final QuotedIDFactory idfac;
    private final ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;

    public ExpressionParser(QuotedIDFactory idfac, ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes,
                            TermFactory termFactory, TypeFactory typeFactory) {
        this.idfac = idfac;
        this.attributes = attributes;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
    }

    public ImmutableTerm parseTerm(Expression expression) {
        TermVisitor visitor = new TermVisitor(attributes);
        return visitor.getTerm(expression);
    }

    public ImmutableList<ImmutableExpression> parseBooleanExpression(Expression expression) {
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

        BooleanExpressionVisitor(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes) {
            termVisitor = new TermVisitor(attributes);
        }

        private ImmutableList<ImmutableExpression> translate(Expression expression) {
            expression.accept(this);
            return this.result;
        }

        // cancel double negation
        private ImmutableExpression negation(ImmutableExpression arg) {
            return (arg.getFunctionSymbol() == ExpressionOperation.NOT)
                    ? (ImmutableExpression)arg.getTerm(0)
                    : termFactory.getImmutableExpression(ExpressionOperation.NOT, arg);
        }

        private java.util.function.Function<ImmutableExpression, ImmutableList<ImmutableExpression>> notOperation(boolean isNot) {
            return isNot
                    ? arg -> ImmutableList.of(negation(arg))
                    : arg -> ImmutableList.of(arg);
        }

        private void process(BinaryExpression expression, BiFunction<ImmutableTerm, ImmutableTerm, ImmutableExpression> op) {
            ImmutableTerm leftTerm = termVisitor.getTerm(expression.getLeftExpression());
            ImmutableTerm rightTerm = termVisitor.getTerm(expression.getRightExpression());
            ImmutableExpression f = op.apply(leftTerm, rightTerm);
            result = notOperation(expression.isNot()).apply(f);
        }

        private void processOJ(OldOracleJoinBinaryExpression expression, BiFunction<ImmutableTerm, ImmutableTerm, ImmutableExpression> op) {
            if (expression.getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR)
                throw new UnsupportedSelectQueryRuntimeException("Oracle PRIOR is not supported", expression);

            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                throw new UnsupportedSelectQueryRuntimeException("Old Oracle OUTER JOIN syntax is not supported", expression);

            process(expression, op);
        }

        private ImmutableExpression getOR(ImmutableList<ImmutableExpression> list) {
            return list.reverse().stream()
                    .reduce(null, (a, b) -> (a == null) ? b : termFactory.getImmutableExpression(ExpressionOperation.OR, b, a));
        }

        private ImmutableExpression getAND(ImmutableList<ImmutableExpression> list) {
            return list.reverse().stream()
                    .reduce(null, (a, b) -> (a == null) ? b : termFactory.getImmutableExpression(ExpressionOperation.AND, b, a));
        }

        // ------------------------------------------------------------
        //        BOOLEAN OPERATIONS
        // ------------------------------------------------------------

        @Override
        public void visit(AndExpression expression) {
            ImmutableList<ImmutableExpression> left = translate(expression.getLeftExpression());
            ImmutableList<ImmutableExpression> right = translate(expression.getRightExpression());
            ImmutableList<ImmutableExpression> and = Stream.of(left, right).flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toList());

            result = (expression.isNot())
                    ? ImmutableList.of(negation(getAND(and)))
                    : and;
        }

        @Override
        public void visit(OrExpression expression) {
            ImmutableExpression left = getAND(translate(expression.getLeftExpression()));
            ImmutableExpression right = getAND(translate(expression.getRightExpression()));
            result = (expression.isNot())
                    ? ImmutableList.of(negation(left), negation(right))
                    : ImmutableList.of(termFactory.getImmutableExpression(ExpressionOperation.OR, left, right));
        }

        @Override
        public void visit(Parenthesis expression) {
            ImmutableList<ImmutableExpression> arg = translate(expression.getExpression());
            result = (expression.isNot()) ? ImmutableList.of(negation(getAND(arg))) : arg;
        }


        // ------------------------------------------------------------
        //        RELATIONAL OPERATIONS
        // ------------------------------------------------------------

        @Override
        public void visit(IsNullExpression expression) {
            ImmutableTerm term = termVisitor.getTerm(expression.getLeftExpression());
            result = notOperation(expression.isNot()).apply(termFactory.getImmutableExpression(ExpressionOperation.IS_NULL, term));
        }

        @Override
        public void visit(EqualsTo expression) {
            processOJ(expression, (t1, t2) -> termFactory.getImmutableExpression(ExpressionOperation.EQ, t1, t2));
        }

        @Override
        public void visit(GreaterThan expression) {
            processOJ(expression, (t1, t2) -> termFactory.getImmutableExpression(ExpressionOperation.GT, t1, t2));
        }

        @Override
        public void visit(GreaterThanEquals expression) {
            processOJ(expression, (t1, t2) -> termFactory.getImmutableExpression(ExpressionOperation.GTE, t1, t2));
        }

        @Override
        public void visit(MinorThan expression) {
            processOJ(expression, (t1, t2) -> termFactory.getImmutableExpression(ExpressionOperation.LT, t1, t2));
        }

        @Override
        public void visit(MinorThanEquals expression) {
            processOJ(expression, (t1, t2) -> termFactory.getImmutableExpression(ExpressionOperation.LTE, t1, t2));
        }

        @Override
        public void visit(NotEqualsTo expression) {
            processOJ(expression, (t1, t2) -> termFactory.getImmutableExpression(ExpressionOperation.NEQ, t1, t2));
        }


        // ------------------------------------------------------------
        //        STRING RELATIONAL OPERATIONS
        // ------------------------------------------------------------

        @Override
        public void visit(LikeExpression expression) {
            process(expression, (t1, t2) -> termFactory.getImmutableExpression(ExpressionOperation.SQL_LIKE, t1, t2));
        }

        @Override
        public void visit(RegExpMySQLOperator expression) {
            ImmutableTerm flags;
            switch (expression.getOperatorType()) {
                case MATCH_CASESENSITIVE:
                    flags = termFactory.getConstantLiteral("");
                    break;
                case MATCH_CASEINSENSITIVE:
                    flags = termFactory.getConstantLiteral("i");
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            process(expression, (t1, t2) ->  termFactory.getImmutableExpression(ExpressionOperation.REGEX, t1, t2, flags));
        }

        // POSIX Regular Expressions
        // e.g., https://www.postgresql.org/docs/9.6/static/functions-matching.html#FUNCTIONS-POSIX-REGEXP

        @Override
        public void visit(RegExpMatchOperator expression) {
            ImmutableTerm flags;
            java.util.function.UnaryOperator<ImmutableExpression> not;
            switch (expression.getOperatorType()) {
                case MATCH_CASESENSITIVE:
                    flags = termFactory.getConstantLiteral("");
                    not = UnaryOperator.identity();
                    break;
                case MATCH_CASEINSENSITIVE:
                    flags = termFactory.getConstantLiteral("i");
                    not = UnaryOperator.identity();
                    break;
                case NOT_MATCH_CASESENSITIVE:
                    flags = termFactory.getConstantLiteral("");
                    not = arg -> termFactory.getImmutableExpression(ExpressionOperation.NOT, arg);
                    break;
                case NOT_MATCH_CASEINSENSITIVE:
                    flags = termFactory.getConstantLiteral("i");
                    not = arg -> termFactory.getImmutableExpression(ExpressionOperation.NOT, arg);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            process(expression, (t1, t2) ->
                    not.apply(termFactory.getImmutableExpression(ExpressionOperation.REGEX, t1, t2, flags)));
        }





        @Override
        public void visit(Between expression) {
            ImmutableTerm t1 = termVisitor.getTerm(expression.getLeftExpression());
            ImmutableTerm t2 = termVisitor.getTerm(expression.getBetweenExpressionStart());

            ImmutableTerm t3 = termVisitor.getTerm(expression.getLeftExpression());
            ImmutableTerm t4 = termVisitor.getTerm(expression.getBetweenExpressionEnd());

            if (expression.isNot()) {
                ImmutableExpression atom1 = termFactory.getImmutableExpression(ExpressionOperation.LT, t1, t2);
                ImmutableExpression atom2 = termFactory.getImmutableExpression(ExpressionOperation.GT, t3, t4);

                result = ImmutableList.of(termFactory.getImmutableExpression(ExpressionOperation.OR, atom1, atom2));
            }
            else {
                ImmutableExpression atom1 = termFactory.getImmutableExpression(ExpressionOperation.GTE, t1, t2);
                ImmutableExpression atom2 = termFactory.getImmutableExpression(ExpressionOperation.LTE, t3, t4);

                result = ImmutableList.of(atom1, atom2);
            }
        }


        @Override
        public void visit(InExpression expression) {

            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                throw new UnsupportedSelectQueryRuntimeException("Oracle OUTER JOIN syntax is not supported", expression);

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
                            return termFactory.getImmutableExpression(ExpressionOperation.EQ, t1, t2);
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
            ImmutableList<ImmutableExpression> equalities =
                    ImmutableList.<ImmutableExpression>builder().addAll(stream.iterator()).build();

            ImmutableExpression atom;
            switch (equalities.size()) {
                case 0:
                    throw new InvalidSelectQueryRuntimeException("IN must contain at least one expression", expression);
                case 1:
                    atom = equalities.get(0);
                    break;
                default:
                    atom = getOR(equalities);
            }
            result = notOperation(expression.isNot()).apply(atom);
        }

        /*
        private ImmutableList<Function> getEqLists(List<Expression> leftList, List<Expression> rightList) {
            ImmutableList.Builder<Function> builder = ImmutableList.builder();
            for (int i = 0; i < leftList.size(); i++) {
                Term t1 = getTerm(leftList.get(i));
                Term t2 = getTerm(rightList.get(i));
                builder.add(FACTORY.getFunctionEQ(t1, t2));
            }
            return builder.build();
        }
        */






        @Override
        public void visit(Column expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(net.sf.jsqlparser.expression.Function expression) {
            // do not use ImmutableCollectors.toList because this cannot be done concurrently
            ImmutableList<ImmutableTerm> terms = (expression.getParameters() != null)
                    ? ImmutableList.<ImmutableTerm>builder()
                    .addAll(expression.getParameters().getExpressions().stream()
                            .map(t -> termVisitor.getTerm(t)).iterator())
                    .build()
                    : ImmutableList.of();

            BiFunction<ImmutableList<ImmutableTerm>, net.sf.jsqlparser.expression.Function, ImmutableExpression> function
                    = BOOLEAN_FUNCTIONS.get(expression.getName().toUpperCase());

            if (function == null)
                throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);

            result = ImmutableList.of(function.apply(terms, expression));
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
        //        ARITHMETIC, STRING OPERATIONS AND FUNCTIONS (NOT ALLOWED)
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
        public void visit(Modulo expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(SignedExpression expression) {
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
        public void visit(UserVariable expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);
        }

        @Override
        public void visit(NumericBind expression) {
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
        public void visit(NotExpression expression) {
            result = ImmutableList.of(termFactory.getImmutableExpression(ExpressionOperation.NOT, termVisitor.getTerm(expression.getExpression())));
//            throw new UnsupportedSelectQueryRuntimeException("Not a Boolean expression", expression);

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
        public void visit(BitwiseAnd expression) {
            throw new UnsupportedSelectQueryRuntimeException("Bitwise AND is not supported", expression);
        }

        @Override
        public void visit(BitwiseOr expression) {
            throw new UnsupportedSelectQueryRuntimeException("Bitwise OR is not supported", expression);
        }

        @Override
        public void visit(BitwiseXor expression) {
            throw new UnsupportedSelectQueryRuntimeException("Bitwise XOR is not supported", expression);
        }

        @Override
        public void visit(AnalyticExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Analytic expressions is not supported", expression);
        }

        @Override
        public void visit(WithinGroupExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("WithinGroup expressions is not supported", expression);
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
        public void visit(JdbcParameter expression) {
            throw new InvalidSelectQueryRuntimeException("JDBC parameters are not allowed", expression);
        }

        @Override
        public void visit(JdbcNamedParameter expression) {
            throw new InvalidSelectQueryRuntimeException("JDBC named parameters are not allowed", expression);
        }
    }


    /**
     * This visitor class converts the SQL Expression to a Term
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
    private class TermVisitor implements ExpressionVisitor {

        private final ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;

        // CAREFUL: this variable gets reset in each visit method implementation
        // concurrent evaluation is not possible
        private ImmutableTerm result;

        TermVisitor(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes) {
            this.attributes = attributes;
        }

        ImmutableTerm getTerm(Expression expression) {
            expression.accept(this);
            return this.result;
        }



        @Override
        public void visit(net.sf.jsqlparser.expression.Function expression) {
            // do not use ImmutableCollectors.toList because this cannot be done concurrently
            ImmutableList<ImmutableTerm> terms = (expression.getParameters() != null)
                    ? ImmutableList.<ImmutableTerm>builder()
                    .addAll(expression.getParameters().getExpressions().stream()
                            .map(t -> getTerm(t)).iterator())
                    .build()
                    : ImmutableList.of();

            BiFunction<ImmutableList<ImmutableTerm>, net.sf.jsqlparser.expression.Function, ImmutableFunctionalTerm> function
                    = FUNCTIONS.get(expression.getName().toUpperCase());

            if (function == null)
                throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);

            result = function.apply(terms, expression);
        }


        // ------------------------------------------------------------
        //        CONSTANT EXPRESSIONS
        // ------------------------------------------------------------

        @Override
        public void visit(NullValue expression) {
            throw new UnsupportedSelectQueryRuntimeException("NULL is not supported", expression);
        }

        @Override
        public void visit(DoubleValue expression) {
            process(expression.toString(), XSD.DOUBLE);
        }

        @Override
        public void visit(LongValue expression) {
            process(expression.getStringValue(), XSD.LONG);
        }

        @Override
        public void visit(HexValue expression) {
            throw new UnsupportedSelectQueryRuntimeException("HEX is not supported", expression);
        }

        @Override
        public void visit(StringValue expression) {
            process(expression.getValue(), XSD.STRING);
        }

        @Override
        public void visit(DateValue expression) {
            process(expression.getValue().toString(), XSD.DATE);
        }

        @Override
        public void visit(TimeValue expression) {
            process(expression.getValue().toString(), XSD.TIME);
        }

        @Override
        public void visit(TimestampValue expression) {
            process(expression.getValue().toString(), XSD.DATETIME);
        }

        @Override
        public void visit(IntervalExpression expression) {
            // example: INTERVAL '4 5:12' DAY TO MINUTE
            throw new UnsupportedSelectQueryRuntimeException("Temporal INTERVALs are not supported yet", expression);
        }

        private void process(String value, IRI datatype) {
            result = termFactory.getConstantLiteral(value, typeFactory.getDatatype(datatype));
        }



        // ------------------------------------------------------------
        //        BINARY OPERATIONS
        // ------------------------------------------------------------

        @Override
        public void visit(Addition expression) {
            process(expression, (t1, t2) -> termFactory.getImmutableFunctionalTerm(ExpressionOperation.ADD, t1, t2));
        }

        @Override
        public void visit(Subtraction expression) {
            process(expression, (t1, t2) -> termFactory.getImmutableFunctionalTerm(ExpressionOperation.SUBTRACT, t1, t2));
        }

        @Override
        public void visit(Multiplication expression) {
            process(expression, (t1, t2) -> termFactory.getImmutableFunctionalTerm(ExpressionOperation.MULTIPLY, t1, t2));
        }

        @Override
        public void visit(Division expression) {
            process(expression, (t1, t2) -> termFactory.getImmutableFunctionalTerm(ExpressionOperation.DIVIDE, t1, t2));
        }

        @Override
        public void visit(Modulo expression) {
            // TODO: introduce operation and implement
            throw new UnsupportedSelectQueryRuntimeException("MODULO is not supported yet", expression);
        }

        @Override
        public void visit(Concat expression) {
            process(expression, (t1, t2) -> termFactory.getImmutableFunctionalTerm(ExpressionOperation.CONCAT, t1, t2));
        }

        private void process(BinaryExpression expression, BinaryOperator<ImmutableTerm> op) {
            if (expression.isNot())
                throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);

            ImmutableTerm leftTerm = getTerm(expression.getLeftExpression());
            ImmutableTerm rightTerm = getTerm(expression.getRightExpression());
            result = op.apply(leftTerm, rightTerm);
        }




        /*
                UNARY OPERATIONS
         */

        @Override
        public void visit(IsNullExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);
        }

        @Override
        public void visit(Parenthesis expression) {
            if (expression.isNot())
                throw new UnsupportedSelectQueryRuntimeException("Not a term", expression);

            result = getTerm(expression.getExpression());
        }

        @Override
        public void visit(SignedExpression expression) {

            ImmutableTerm arg = getTerm(expression.getExpression());
            switch (expression.getSign()) {
                case '-' :
                    result = termFactory.getImmutableFunctionalTerm(ExpressionOperation.MINUS, arg);
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
                    result = termFactory.getBooleanConstant(true);
                else if (column.equals(idfac.createAttributeID("false")))
                    result = termFactory.getBooleanConstant(false);
                else
                    throw new UnsupportedSelectQueryRuntimeException("Unable to find attribute name ", expression);
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

        @Override //SELECT @col FROM table1
        public void visit(UserVariable expression) {
            throw new UnsupportedSelectQueryRuntimeException("User variables are not supported yet", expression);
        }

        @Override //SELECT a FROM b WHERE c = :1
        public void visit(NumericBind expression) {
            throw new UnsupportedSelectQueryRuntimeException("NumericBind is not supported yet", expression);
        }

        @Override //KEEP (DENSE_RANK FIRST ORDER BY col1)
        public void visit(KeepExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Keep Expression is not supported yet", expression);

        }

        @Override
        public void visit(MySQLGroupConcat expression) {
            throw new UnsupportedSelectQueryRuntimeException("MySQLGroupConcat is not supported yet", expression);
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


        @Override
        public void visit(CastExpression expression) {
            ImmutableTerm term = getTerm(expression.getLeftExpression());
            ColDataType type = expression.getType();
            String datatype = type.getDataType();
            // TODO: proper datatype conversion is required at this stage
            // result = FACTORY.getFunctionCast(term, datatype);
            throw new UnsupportedSelectQueryRuntimeException("CAST is not supported yet", expression);
        }



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
        public void visit(BitwiseAnd expression) {
            throw new UnsupportedSelectQueryRuntimeException("Bitwise AND is not supported", expression);
        }

        @Override
        public void visit(BitwiseOr expression) {
            throw new UnsupportedSelectQueryRuntimeException("Bitwise OR is not supported", expression);
        }

        @Override
        public void visit(BitwiseXor expression) {
            throw new UnsupportedSelectQueryRuntimeException("Bitwise XOR is not supported", expression);
        }

        @Override
        public void visit(AnalyticExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("Analytic expressions is not supported", expression);
        }

        @Override //SELECT LISTAGG(col1, '##') WITHIN GROUP (ORDER BY col1) FROM table1
        public void visit(WithinGroupExpression expression) {
            throw new UnsupportedSelectQueryRuntimeException("WithinGroup expressions is not supported", expression);
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
            throw new UnsupportedSelectQueryRuntimeException("JSON operators are not supported", expression);
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
    // supported SQL functions
    // (WARNING: not all combinations of the parameters are supported)
    // ---------------------------------------------------------------

    private final ImmutableMap<String, BiFunction<ImmutableList<ImmutableTerm>, net.sf.jsqlparser.expression.Function, ImmutableFunctionalTerm>>
            FUNCTIONS = ImmutableMap.<String, BiFunction<ImmutableList<ImmutableTerm>, net.sf.jsqlparser.expression.Function, ImmutableFunctionalTerm>>builder()
            .put("REGEXP_REPLACE", this::get_REGEXP_REPLACE)
            .put("REPLACE", this::get_REPLACE)
            .put("CONCAT", this::get_CONCAT)
            .put("SUBSTR", this::get_SUBSTR)
            .put("SUBSTRING", this::get_SUBSTR)
            .put("LCASE", this::get_LCASE)
            .put("LOWER", this::get_LCASE)
            .put("UCASE", this::get_UCASE)
            .put("UPPER", this::get_UCASE)
            .put("LENGTH", this::get_STRLEN)
            .put("RAND", this::get_RAND)
            .build();

    private final ImmutableMap<String, BiFunction<ImmutableList<ImmutableTerm>, net.sf.jsqlparser.expression.Function, ImmutableExpression>>
            BOOLEAN_FUNCTIONS = ImmutableMap.<String, BiFunction<ImmutableList<ImmutableTerm>, net.sf.jsqlparser.expression.Function, ImmutableExpression>>builder()
            .put("REGEXP_LIKE", this::get_REGEXP_LIKE)
            .build();

    private ImmutableExpression get_REGEXP_LIKE(ImmutableList<ImmutableTerm> terms, net.sf.jsqlparser.expression.Function expression) {
        // Oracle only:
        // a source string, a regex pattern (POSIX regular expression), an optional flags
        switch (terms.size()) {
            case 2:
                return termFactory.getImmutableExpression(
                        ExpressionOperation.REGEX, terms.get(0), terms.get(1), termFactory.getConstantLiteral(""));
            case 3:
                // check the flag?
                return termFactory.getImmutableExpression(
                        ExpressionOperation.REGEX, terms.get(0), terms.get(1), terms.get(2));
        }
        throw new InvalidSelectQueryRuntimeException("Wrong number of arguments for SQL function", expression);
    }

    private ImmutableFunctionalTerm get_REGEXP_REPLACE(ImmutableList<ImmutableTerm> terms, net.sf.jsqlparser.expression.Function expression) {
        ImmutableTerm flags;
        switch (terms.size()) {
            case 3:
                // either Oracle or PostgreSQL, without flags
                flags = termFactory.getConstantLiteral(""); // the 4th argument is flags
                break;
            case 4:
                if (((ValueConstant)terms.get(3)).getType().equals(typeFactory.getDatatype(XSD.STRING))) {
                    // PostgreSQL
                    flags =  terms.get(3);
                    // check that flags is either ig or g
                }
                else
                    throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);
                break;
            case 6:
                // Oracle
                if (!terms.get(3).equals(termFactory.getConstantLiteral("1", typeFactory.getDatatype(XSD.LONG)))
                        || !terms.get(4).equals(termFactory.getConstantLiteral("0", typeFactory.getDatatype(XSD.LONG))))
                    throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);

                // check that the flags is a combination of imx
                flags = terms.get(5);
                break;
            default:
                throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);
        }

        return termFactory.getImmutableFunctionalTerm(
                ExpressionOperation.REPLACE, terms.get(0), terms.get(1), terms.get(2), flags);
    }

    private ImmutableFunctionalTerm get_REPLACE(ImmutableList<ImmutableTerm> terms, net.sf.jsqlparser.expression.Function expression) {
        ImmutableTerm flags = termFactory.getConstantLiteral("");
        switch (terms.size()) {
            case 2:
                return termFactory.getImmutableFunctionalTerm(
                        ExpressionOperation.REPLACE, terms.get(0), terms.get(1),
                        termFactory.getConstantLiteral(""), flags);
            case 3:
                return termFactory.getImmutableFunctionalTerm(
                        ExpressionOperation.REPLACE, terms.get(0), terms.get(1), terms.get(2), flags);

        }
        throw new InvalidSelectQueryRuntimeException("Wrong number of arguments in SQL function", expression);
    }

    private ImmutableFunctionalTerm get_CONCAT(ImmutableList<ImmutableTerm> terms, net.sf.jsqlparser.expression.Function expression) {
        return (ImmutableFunctionalTerm)
                terms.stream()  // left recursion to match || in JSQLParser
                        .reduce(null, (a, b) -> (a == null)
                                ? b
                                : termFactory.getImmutableFunctionalTerm(ExpressionOperation.CONCAT, a, b));
    }

    private ImmutableFunctionalTerm get_SUBSTR(ImmutableList<ImmutableTerm> terms, net.sf.jsqlparser.expression.Function expression) {
        switch (terms.size()) {
            case 2:
                return termFactory.getImmutableFunctionalTerm(ExpressionOperation.SUBSTR2, terms.get(0), terms.get(1));
            case 3:
                return termFactory.getImmutableFunctionalTerm(ExpressionOperation.SUBSTR3, terms.get(0), terms.get(1), terms.get(2));
        }
        // DB2 has 4
        throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);
    }

    private ImmutableFunctionalTerm get_RAND(ImmutableList<ImmutableTerm> terms, net.sf.jsqlparser.expression.Function expression) {
        switch (terms.size()) {
            case 0:
                return termFactory.getImmutableFunctionalTerm(ExpressionOperation.RAND);
        }
        throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);
    }

    private ImmutableFunctionalTerm get_LCASE(ImmutableList<ImmutableTerm> terms, net.sf.jsqlparser.expression.Function expression) {
        if (terms.size() == 1)
            return termFactory.getImmutableFunctionalTerm(ExpressionOperation.LCASE, terms.get(0));
        // DB2 has 3
        throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);
    }

    private ImmutableFunctionalTerm get_UCASE(ImmutableList<ImmutableTerm> terms, net.sf.jsqlparser.expression.Function expression) {
        if (terms.size() == 1)
            return termFactory.getImmutableFunctionalTerm(ExpressionOperation.UCASE, terms.get(0));
        // DB2 has 3
        throw new UnsupportedSelectQueryRuntimeException("Unsupported SQL function", expression);
    }

    private ImmutableFunctionalTerm get_STRLEN(ImmutableList<ImmutableTerm> terms, net.sf.jsqlparser.expression.Function expression) {
        if (terms.size() == 1)
            return termFactory.getImmutableFunctionalTerm(ExpressionOperation.STRLEN, terms.get(0));

        throw new InvalidSelectQueryRuntimeException("Wrong number of arguments in SQL function", expression);
    }

}
