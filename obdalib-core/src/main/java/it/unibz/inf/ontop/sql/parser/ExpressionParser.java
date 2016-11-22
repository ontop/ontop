package it.unibz.inf.ontop.sql.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.QualifiedAttributeID;
import it.unibz.inf.ontop.sql.QuotedID;
import it.unibz.inf.ontop.sql.QuotedIDFactory;
import it.unibz.inf.ontop.sql.RelationID;
import it.unibz.inf.ontop.sql.parser.exceptions.InvalidSelectQueryException;
import it.unibz.inf.ontop.sql.parser.exceptions.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;


/**
 * Created by Roman Kontchakov on 10/11/2016.
 *
 */

public class ExpressionParser implements java.util.function.Function<ImmutableMap<QualifiedAttributeID, Variable>, ImmutableList<Function>> {

    private final QuotedIDFactory idfac;
    private final Expression root;

    private static final OBDADataFactory FACTORY = OBDADataFactoryImpl.getInstance();

    public   ExpressionParser(QuotedIDFactory idfac, Expression expression) {
        this.idfac = idfac;
        this.root = expression;
    }

    public static ExpressionParser empty() {
        return new ExpressionParser (null, null) {
            @Override
            public ImmutableList<Function> apply(ImmutableMap<QualifiedAttributeID, Variable> attributes) {
                return ImmutableList.of();
            }
        };
    }

    @Override
    public ImmutableList<Function> apply(ImmutableMap<QualifiedAttributeID, Variable> attributes) {

        ExpressionVisitorImpl visitor = new ExpressionVisitorImpl(attributes);
        Expression current = root;

        if (current instanceof AndExpression) {
            ImmutableList.Builder<Function> builder = ImmutableList.builder();
            do {
                AndExpression and = (AndExpression) current;
                // for a sequence of AND operations, JSQLParser makes the right argument simple
                builder.add(visitor.getFunction(and.getRightExpression()));
                // and the left argument complex (nested AND)
                current = and.getLeftExpression();
            } while (current instanceof AndExpression);

            builder.add(visitor.getFunction(current));

            // restore the original order
            return builder.build().reverse();
        }
        return ImmutableList.of(visitor.getFunction(current));
    }



    // TODO: this class is being reviewed


    /**
     * This visitor class converts the SQL Expression to a Function
     *
     * Exceptions
     *      - UnsupportedOperationException: an internal error (due to the unexpected bahaviour of JSQLparser)
     *      - InvalidSelectQueryException: the input is not a valid mapping query
     *      - UnsupportedSelectQueryException: the input cannot be converted into a CQ and needs to be wrapped
     *
     */
    private class ExpressionVisitorImpl implements ExpressionVisitor {

        private final ImmutableMap<QualifiedAttributeID, Variable> attributes;

        private Term result; // CAREFUL: this variable gets reset in each visit method implementation

        ExpressionVisitorImpl(ImmutableMap<QualifiedAttributeID, Variable> attributes) {
            this.attributes = attributes;
        }

        // TODO: double-check this
        private Function getFunction(Expression expression) {
            Term t = getTerm(expression);
            if (t instanceof Function)
                return (Function)t;

            throw new UnsupportedSelectQueryException("Unexpected conversion to Boolean", expression);
        }

        private Term getTerm(Expression expression) {
            expression.accept(this);
            return this.result;
        }

        private java.util.function.UnaryOperator<Term> notOperation(boolean isNot) {
            return isNot ? term -> FACTORY.getFunctionNOT(term) : UnaryOperator.identity();
        }

        private void process(BinaryExpression expression, BinaryOperator<Term> op) {
            Term leftTerm = getTerm(expression.getLeftExpression());
            Term rightTerm = getTerm(expression.getRightExpression());
            Term expTerm = op.apply(leftTerm, rightTerm);
            result = notOperation(expression.isNot()).apply(expTerm);
        }

        // CAREFUL: the first argument is NOT the composite term, but rather its argument
        private void process(Expression arg, UnaryOperator<Term> op) {
            Term term = getTerm(arg);
            Term expTerm = op.apply(term);
            result = expTerm;
        }

        private void process(String value, Predicate.COL_TYPE datatype) {
            result = FACTORY.getConstantLiteral(value, datatype);
        }



        @Override
        public void visit(net.sf.jsqlparser.expression.Function func) {
            String functionName = func.getName().toLowerCase();
            List<Expression> args = func.getParameters().getExpressions();
            int arity = args.size();

            if (functionName.equals("regexp_like")) {
                if (arity != 2 && arity != 3)
                    throw new InvalidSelectQueryException("Wrong number of arguments for SQL function REGEX_LIKE", func);

                Term t1 = getTerm(args.get(0));  // a source string
                Term t2 = getTerm(args.get(1)); // a regex pattern

                // the third parameter is optional for match_parameter in regexp_like
                Term t3 = (arity == 3) ? getTerm(args.get(2)) : FACTORY.getConstantLiteral("");

                result = FACTORY.getFunction(ExpressionOperation.REGEX, t1, t2, t3);
            }
            else if (functionName.endsWith("replace")) {
                if (arity != 2 && arity != 3)
                    throw new InvalidSelectQueryException("Wrong number of arguments in SQL function REPLACE", func);

                Term t1 = getTerm(args.get(0));
                Term t2 = getTerm(args.get(1)); // second parameter is a string

                // Term t3 is optional: no string means delete occurrences of second param
                Term t3 =  (arity == 3) ? getTerm(args.get(2)) : FACTORY.getConstantLiteral("");

                result = FACTORY.getFunction(ExpressionOperation.REPLACE, t1, t2, t3,
                            FACTORY.getConstantLiteral("")); // the 4th argument is flags
            }
            else if (functionName.endsWith("concat")) {
                ImmutableList<Term> terms = args.stream()
                        .map(t -> getTerm(t))
                        .collect(ImmutableCollectors.toList());

                // need to reverse the list to obtain the required order
                // (note that b and a are swapped)
                Term term =  terms.reverse().stream()
                        .reduce(null, (a, b) -> (a == null)
                                ? b
                                : FACTORY.getFunction(ExpressionOperation.CONCAT, b, a));

                result = term;
            }
            else
                throw new UnsupportedSelectQueryException("Unsupported function ", func);
        }

        /*
                CONSTANT EXPRESSIONS
         */

        @Override
        public void visit(NullValue expression) {
            throw new UnsupportedSelectQueryException("NULL is not supported", expression);
        }

        @Override
        public void visit(DoubleValue expression) {
            process(expression.toString(), Predicate.COL_TYPE.DOUBLE);
        }

        @Override
        public void visit(LongValue expression) {
            process(expression.getStringValue(), Predicate.COL_TYPE.LONG);
        }

        @Override
        public void visit(StringValue expression) {
            process(expression.getValue(), Predicate.COL_TYPE.STRING);
        }

        @Override
        public void visit(DateValue expression) {
            process(expression.getValue().toString(), Predicate.COL_TYPE.DATE);
        }

        @Override
        public void visit(TimeValue expression) {
            process(expression.getValue().toString(), Predicate.COL_TYPE.TIME);
        }

        @Override
        public void visit(TimestampValue expression) {
            process(expression.getValue().toString(), Predicate.COL_TYPE.DATETIME);
        }

        @Override
        public void visit(IntervalExpression expression) {
            // example: INTERVAL '4 5:12' DAY TO MINUTE
            throw new UnsupportedSelectQueryException("Temporal INTERVALs are not supported yet", expression);
        }

        /*
            BINARY OPERATIONS
        */

        @Override
        public void visit(Addition expression) {
            process(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.ADD, t1, t2));
        }

        @Override
        public void visit(Subtraction expression) {
            process(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.SUBTRACT, t1, t2));
        }

        @Override
        public void visit(Multiplication expression) {
            process(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.MULTIPLY, t1, t2));
        }

        @Override
        public void visit(Division expression) {
            process(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.DIVIDE, t1, t2));
        }

        @Override
        public void visit(Modulo expression) {
            // TODO: introduce operation and implement
            throw new UnsupportedSelectQueryException("MODULO is not supported yet", expression);
        }

        @Override
        public void visit(Concat expression) {
            process(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.CONCAT, t1, t2));
        }



        @Override
        public void visit(EqualsTo expression) {
            process(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.EQ, t1, t2));
        }

        @Override
        public void visit(GreaterThan expression) {
            process(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.GT, t1, t2));
        }

        @Override
        public void visit(GreaterThanEquals expression) {
            process(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.GTE, t1, t2));
        }

        @Override
        public void visit(MinorThan expression) {
            process(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.LT, t1, t2));
        }

        @Override
        public void visit(MinorThanEquals expression) {
            process(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.LTE, t1, t2));
        }

        @Override
        public void visit(NotEqualsTo expression) {
            process(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.NEQ, t1, t2));
        }


        @Override
        public void visit(LikeExpression expression) {
            process(expression, (t1, t2) -> FACTORY.getSQLFunctionLike(t1, t2));
        }

        @Override
        public void visit(RegExpMySQLOperator expression) {
            Term flags;
            switch (expression.getOperatorType()) {
                case MATCH_CASESENSITIVE:
                    flags = FACTORY.getConstantLiteral("");
                    break;
                case MATCH_CASEINSENSITIVE:
                    flags = FACTORY.getConstantLiteral("i");
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            process(expression, (t1, t2) ->  FACTORY.getFunction(ExpressionOperation.REGEX, t1, t2, flags));
        }

        @Override
        public void visit(RegExpMatchOperator expression) {
            Term flags;
            boolean not;
            switch (expression.getOperatorType()) {
                case MATCH_CASESENSITIVE:
                    flags = FACTORY.getConstantLiteral("");
                    not = false;
                    break;
                case MATCH_CASEINSENSITIVE:
                    flags = FACTORY.getConstantLiteral("i");
                    not = false;
                    break;
                case NOT_MATCH_CASESENSITIVE:
                    flags = FACTORY.getConstantLiteral("");
                    not = true;
                    break;
                case NOT_MATCH_CASEINSENSITIVE:
                    flags = FACTORY.getConstantLiteral("i");
                    not = true;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            process(expression, (t1, t2) ->
                    notOperation(not).apply(FACTORY.getFunction(ExpressionOperation.REGEX, t1, t2, flags)));
        }


        @Override
        public void visit(AndExpression expression) {
            process(expression, (t1, t2) -> FACTORY.getFunctionAND(t1, t2));
        }

        @Override
        public void visit(OrExpression expression) {
            process(expression, (t1, t2) -> FACTORY.getFunctionOR(t1, t2));
        }



        @Override
        public void visit(Between expression) {
            Term t1 = getTerm(expression.getLeftExpression());
            Term t2 = getTerm(expression.getBetweenExpressionStart());
            Function atom1 = FACTORY.getFunction(ExpressionOperation.GTE, t1, t2);

            Term t3 = getTerm(expression.getLeftExpression());
            Term t4 = getTerm(expression.getBetweenExpressionEnd());
            Function atom2 = FACTORY.getFunction(ExpressionOperation.LTE, t3, t4);

            result = notOperation(expression.isNot()).apply(FACTORY.getFunctionAND(atom1, atom2));
        }


        @Override
        public void visit(InExpression expression) {

            ItemsList right = expression.getRightItemsList();
            // right can be SubSelect, ExpressionList and MultiExpressionList
            if (!(right instanceof ExpressionList))
                throw new UnsupportedSelectQueryException("IN is supported only with ExpressionList on the right-hand side", expression);

            Expression left = expression.getLeftExpression();
            if (left == null || expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                throw new UnsupportedSelectQueryException("IN is supported only with Expression on the left-hand side (and no Oracle OUTER JOIN syntax)", expression);

            ImmutableList<Function> equalities = ImmutableList.<Function>builder()
                    .addAll(((ExpressionList)right).getExpressions().stream()
                        .map(item -> {
                            Term t1 = getTerm(expression.getLeftExpression());
                            Term t2 = getTerm(item);
                            return FACTORY.getFunctionEQ(t1, t2);
                        }).iterator())
                    .build();

            Function atom;
            switch (equalities.size()) {
                case 0:
                    throw new InvalidSelectQueryException("IN must contain at least one expression", expression);
                case 1:
                    atom = equalities.get(0);
                    break;
                default:
                    atom = equalities.reverse().stream()
                            .reduce(null, (a, b) -> (a == null) ? b : FACTORY.getFunctionOR(b, a));
            }

            result = notOperation(expression.isNot()).apply(atom);
        }

        /*
                UNARY OPERATIONS
         */

        @Override
        public void visit(IsNullExpression expression) {
            process(expression.getLeftExpression(),
                    t -> notOperation(expression.isNot()).apply(FACTORY.getFunctionIsNull(t)));
        }

        @Override
        public void visit(Parenthesis expression) {
            process(expression.getExpression(), notOperation(expression.isNot()));
        }

        @Override
        public void visit(SignedExpression expression) {
            UnaryOperator<Term> op;
            switch (expression.getSign()) {
                case '-' :
                    op = t -> FACTORY.getFunction(ExpressionOperation.MINUS, t);
                    break;
                case '+':
                    op = UnaryOperator.identity();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            process(expression.getExpression(), op);
        }

        @Override
        public void visit(ExtractExpression expression) {
            // Example: EXTRACT(month FROM order_date)
            throw new UnsupportedSelectQueryException("EXTRACT is not supported yet", expression);
        }




        @Override
        public void visit(Column expression) {
            QuotedID column = idfac.createAttributeID(expression.getColumnName());
            Table table = expression.getTable();
            RelationID relation = (table != null) && (table.getName() != null)
                ? idfac.createRelationID(table.getSchemaName(), table.getName())
                : null;
            QualifiedAttributeID qa = new QualifiedAttributeID(relation, column);
            Variable var = attributes.get(qa);

            if (var != null) {
                 // if it is an attribute name (qualified or not)
                result = var;
            }
            else {
                // TODO: careful here - could be a built-in function with no arguments
                String columnName = expression.getColumnName();
                // check whether it is an SQL boolean value
                String lowerCase = columnName.toLowerCase();
                if (lowerCase.equals("true"))
                    result = FACTORY.getBooleanConstant(true);
                else if (lowerCase.equals("false"))
                    result = FACTORY.getBooleanConstant(false);
                else
                    throw new UnsupportedSelectQueryException("Unable to find column name for variable: ", expression);
            }

        }



        @Override
        // TODO: this should be supported
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
            throw new UnsupportedSelectQueryException("CASE is not supported yet", expression);
        }

        @Override
        public void visit(WhenClause expression) {
            throw new UnsupportedSelectQueryException("CASE is not supported yet", expression);
        }


        @Override
        public void visit(CastExpression expression) {
            Term term = getTerm(expression.getLeftExpression());
            ColDataType type = expression.getType();
            String datatype = type.getDataType();
            // TODO: proper datatype conversion is required at this stage
            // result = FACTORY.getFunctionCast(term, datatype);
        }



        @Override
        public void visit(SubSelect expression) {
            throw new UnsupportedSelectQueryException("SubSelect is not supported yet", expression);
        }

        @Override
        // TODO: this probably could be supported
        public void visit(ExistsExpression expression) {
            throw new UnsupportedSelectQueryException("EXISTS is not supported yet", expression);
        }

        @Override
        public void visit(AllComparisonExpression expression) {
            throw new UnsupportedSelectQueryException("ALL is not supported yet", expression);
        }

        @Override
        public void visit(AnyComparisonExpression expression) {
            throw new UnsupportedSelectQueryException("ANY is not supported yet", expression);
        }




        @Override
        public void visit(BitwiseAnd expression) {
            throw new UnsupportedSelectQueryException("Bitwise AND is not supported", expression);
        }

        @Override
        public void visit(BitwiseOr expression) {
            throw new UnsupportedSelectQueryException("Bitwise OR is not supported", expression);
        }

        @Override
        public void visit(BitwiseXor expression) {
            throw new UnsupportedSelectQueryException("Bitwise XOR is not supported", expression);
        }

        @Override
        public void visit(AnalyticExpression expression) {
            throw new UnsupportedSelectQueryException("Analytic expressions is not supported", expression);
        }


        @Override
        public void visit(OracleHierarchicalExpression expression) {
            throw new UnsupportedSelectQueryException("Oracle hierarchical expressions is not supported", expression);
        }

        @Override
        public void visit(Matches expression) {
            throw new UnsupportedSelectQueryException("Oracle join syntax not supported", expression);
        }



        @Override
        public void visit(JsonExpression expression) {
            throw new InvalidSelectQueryException("JSON expressions are not allowed", expression);
        }

        @Override
        public void visit(JdbcParameter expression) {
            throw new InvalidSelectQueryException("JDBC parameters are not allowed", expression);
        }

        @Override
        public void visit(JdbcNamedParameter expression) {
            throw new InvalidSelectQueryException("JDBC named parameters are not allowed", expression);
        }
    }
}
