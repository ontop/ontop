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
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.List;
import java.util.function.BinaryOperator;

/**
 * Created by Roman Kontchakov on 10/11/2016.
 */

public class ExpressionParser {

    private final QuotedIDFactory idfac;
    private final ImmutableMap<QualifiedAttributeID, Variable> attributes;

    public ExpressionParser(ImmutableMap<QualifiedAttributeID, Variable> attributes, QuotedIDFactory idfac) {
        this.attributes = attributes;
        this.idfac = idfac;
    }

    public Function convert(Expression expression) {
        ExpressionVisitorImpl visitor = new ExpressionVisitorImpl();
        expression.accept(visitor);
        return (Function)visitor.result;
    }

    // TODO: this class  should be reviewed

    private static final OBDADataFactory FACTORY = OBDADataFactoryImpl.getInstance();
    private static final ImmutableMap<String, OperationPredicate> operations =
            new ImmutableMap.Builder<String, OperationPredicate>()
                    .put("=", ExpressionOperation.EQ)
                    .put(">=", ExpressionOperation.GTE)
                    .put(">", ExpressionOperation.GT)
                    .put("<=", ExpressionOperation.LTE)
                    .put("<", ExpressionOperation.LT)
                    .put("<>", ExpressionOperation.NEQ)
                    .put("!=", ExpressionOperation.NEQ)
                    .put("+", ExpressionOperation.ADD)
                    .put("-", ExpressionOperation.SUBTRACT)
                    .put("*", ExpressionOperation.MULTIPLY)
                    .put("/", ExpressionOperation.DIVIDE)
                    .build();


    /**
     * This visitor class converts the SQL Expression to a Function
     *
     */
    private class ExpressionVisitorImpl implements ExpressionVisitor {

        private Term result; // CAREFUL: this variable gets reset in each visit method implementation

        // TODO: use this method instead of the one below
        // see example in Addition
        private void visitBinaryExpression(BinaryExpression expression, BinaryOperator<Term> op) {
            Expression left = expression.getLeftExpression();
            left.accept(this);
            Term leftTerm = result;

            Expression right = expression.getRightExpression();
            right.accept(this);
            Term rightTerm = result;

            result = op.apply(leftTerm, rightTerm);
        }

        // TODO: to be eliminated
        private Term visitEx(Expression expression) {
            expression.accept(this);
            return this.result;
        }

        // TODO: get rid of this method - see above
        private void visitBinaryExpression(BinaryExpression expression){
            Expression left = expression.getLeftExpression();
            Expression right = expression.getRightExpression();

            Term t1 = visitEx(left);
            if (t1 == null) // TODO: why only the first argument is checked? and why check and not throw exceptions?
                throw new RuntimeException("Unable to find column name for variable: " +left);

            Term t2 = visitEx(right);

            Function compositeTerm;

            //get boolean operation
            String op = expression.getStringExpression();
            Predicate p = operations.get(op);
            if (p != null) {
                compositeTerm = FACTORY.getFunction(p, t1, t2);
            }
            else {
                switch (op) {
                    case "AND":
                        compositeTerm = FACTORY.getFunctionAND(t1, t2);
                        break;
                    case "OR":
                        compositeTerm = FACTORY.getFunctionOR(t1, t2);
                        break;
                    case "LIKE":
                        compositeTerm = FACTORY.getSQLFunctionLike(t1, t2);
                        break;
                    case "~":
                        compositeTerm = FACTORY.getFunction(ExpressionOperation.REGEX, t1, t2, FACTORY.getConstantLiteral(""));
                        break;
                    case "~*":
                        compositeTerm = FACTORY.getFunction(ExpressionOperation.REGEX, t1, t2, FACTORY.getConstantLiteral("i")); // i flag for case insensitivity
                        break;
                    case "!~":
                        compositeTerm = FACTORY.getFunctionNOT(
                                FACTORY.getFunction(ExpressionOperation.REGEX, t1, t2, FACTORY.getConstantLiteral("")));
                        break;
                    case "!~*":
                        compositeTerm = FACTORY.getFunctionNOT(
                                FACTORY.getFunction(ExpressionOperation.REGEX,t1, t2, FACTORY.getConstantLiteral("i")));
                        break;
                    case "REGEXP":
                        compositeTerm = FACTORY.getFunction(ExpressionOperation.REGEX, t1, t2, FACTORY.getConstantLiteral("i"));
                        break;
                    case "REGEXP BINARY":
                        compositeTerm = FACTORY.getFunction(ExpressionOperation.REGEX, t1, t2, FACTORY.getConstantLiteral(""));
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown operator: " + op);
                }
            }
            result = compositeTerm;
        }

        @Override
        public void visit(NullValue nullValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(net.sf.jsqlparser.expression.Function func) {
            if (func.getName().toLowerCase().equals("regexp_like")) {

                List<Expression> expressions = func.getParameters().getExpressions();
                if (expressions.size() == 2 || expressions.size() == 3) {
                    // first parameter is a source_string, generally a column
                    Expression first = expressions.get(0);
                    Term t1 = visitEx(first);
                    if (t1 == null)
                        throw new RuntimeException("Unable to find column name for variable: "
                                + first);

                    // second parameter is a pattern, so generally a regex string
                    Expression second = expressions.get(1);
                    Term t2 = visitEx(second);

                    /*
                     * Term t3 is optional for match_parameter in regexp_like
			         */
                    Term t3;
                    if (expressions.size() == 3){
                        Expression third = expressions.get(2);
                        t3 = visitEx(third);
                    }
                    else {
                        t3 = FACTORY.getConstantLiteral("");
                    }
                    result = FACTORY.getFunction(ExpressionOperation.REGEX, t1, t2, t3);
                }
                else
                    throw new UnsupportedOperationException("Wrong number of arguments (found " + expressions.size() + ", only 2 or 3 supported) to sql function Regex");
            }
            else if (func.getName().toLowerCase().endsWith("replace")) {

                List<Expression> expressions = func.getParameters().getExpressions();
                if (expressions.size() == 2 || expressions.size() == 3) {

                    Term t1; // first parameter is a function expression
                    Expression first = expressions.get(0);
                    t1 = visitEx(first);

                    if (t1 == null)
                        throw new RuntimeException("Unable to find source expression: "
                                + first);

                    // second parameter is a string
                    Expression second = expressions.get(1);
                    Term out_string = visitEx(second);

                    /*
                     * Term t3 is optional: no string means delete occurrences of second param
			         */
                    Term in_string;
                    if (expressions.size() == 3) {
                        Expression third = expressions.get(2);
                        in_string = visitEx(third);
                    }
                    else {
                        in_string = FACTORY.getConstantLiteral("");
                    }
                    result = FACTORY.getFunction(ExpressionOperation.REPLACE, t1, out_string, in_string,
                            FACTORY.getConstantLiteral("")); // the 4th argument is flags
                }
                else
                    throw new UnsupportedOperationException("Wrong number of arguments (found " + expressions.size() + ", only 2 or 3 supported) to sql function REPLACE");
            }
            else if (func.getName().toLowerCase().endsWith("concat")){

                List<Expression> expressions = func.getParameters().getExpressions();

                int nParameters = expressions.size();
                Function topConcat = null;

                for (int i= 0; i<nParameters; i+=2) {

                    if (topConcat == null){

                        Expression first = expressions.get(i);
                        Term first_string = visitEx(first);

                        Expression second = expressions.get(i+1);
                        Term second_string = visitEx(second);

                        topConcat = FACTORY.getFunction(ExpressionOperation.CONCAT, first_string, second_string);
                    }
                    else {

                        Expression second = expressions.get(i);
                        Term second_string = visitEx(second);

                        topConcat = FACTORY.getFunction(ExpressionOperation.CONCAT, topConcat, second_string);
                    }

                }

                result = topConcat;
            }
            else {
                throw new UnsupportedOperationException("Unsupported expression " + func);
            }
        }

        /*
                CONSTANT EXPRESSIONS
         */

        @Override
        public void visit(DoubleValue expression) {
            result = FACTORY.getConstantLiteral(expression.toString(), Predicate.COL_TYPE.DOUBLE);
        }

        @Override
        public void visit(LongValue expression) {
            result = FACTORY.getConstantLiteral(expression.getStringValue(), Predicate.COL_TYPE.LONG);
        }

        @Override
        public void visit(StringValue expression) {
            result = FACTORY.getConstantLiteral(expression.getValue(), Predicate.COL_TYPE.STRING);
        }

        @Override
        public void visit(DateValue expression) {
            result = FACTORY.getConstantLiteral(expression.getValue().toString(), Predicate.COL_TYPE.DATE);
        }

        @Override
        public void visit(TimeValue expression) {
            result = FACTORY.getConstantLiteral(expression.getValue().toString(), Predicate.COL_TYPE.TIME);
        }

        @Override
        public void visit(TimestampValue expression) {
            result = FACTORY.getConstantLiteral(expression.getValue().toString(), Predicate.COL_TYPE.DATETIME);
        }

        /*
            BINARY OPERATIONS
        */

        @Override
        public void visit(Addition addition) {
            visitBinaryExpression(addition,
                    (t1, t2) -> FACTORY.getFunction(ExpressionOperation.ADD, t1, t2));
        }

        @Override
        public void visit(Subtraction subtraction) {
            visitBinaryExpression(subtraction);
        }

        @Override
        public void visit(Multiplication multiplication) {
            visitBinaryExpression(multiplication);
        }

        @Override
        public void visit(Division division) {
            visitBinaryExpression(division);
        }

        @Override
        public void visit(Modulo modulo) {
            throw new UnsupportedOperationException();
        }


        @Override
        // TODO: use new visitBinaryExpression
        public void visit(Concat concat) {
            Expression left = concat.getLeftExpression();
            Expression right = concat.getRightExpression();
            Term l = visitEx(left);
            Term r = visitEx(right);
            result = FACTORY.getFunction(ExpressionOperation.CONCAT, l, r);
        }



        @Override
        public void visit(EqualsTo expression) {
            visitBinaryExpression(expression);
        }

        @Override
        public void visit(GreaterThan expression) {
            visitBinaryExpression(expression);
        }

        @Override
        public void visit(GreaterThanEquals expression) {
            visitBinaryExpression(expression);
        }

        @Override
        public void visit(MinorThan minorThan) {
            visitBinaryExpression(minorThan);
        }

        @Override
        public void visit(MinorThanEquals minorThanEquals) {
            visitBinaryExpression(minorThanEquals);
        }

        @Override
        public void visit(NotEqualsTo notEqualsTo) {
            visitBinaryExpression(notEqualsTo);
        }



        @Override
        public void visit(RegExpMySQLOperator regExpMySQLOperator) {
            visitBinaryExpression(regExpMySQLOperator);
        }

        @Override
        public void visit(LikeExpression likeExpression) {
            visitBinaryExpression(likeExpression);
        }

        @Override
        public void visit(RegExpMatchOperator expression) {
            visitBinaryExpression(expression);
        }



        @Override
        public void visit(AndExpression andExpression) {
            visitBinaryExpression(andExpression);
        }

        @Override
        public void visit(OrExpression orExpression) {
            visitBinaryExpression(orExpression);
        }



        @Override
        public void visit(Between expression) {
            final Expression leftExpression = expression.getLeftExpression();
            Expression e1 = expression.getBetweenExpressionStart();
            Expression e2 = expression.getBetweenExpressionEnd();

            // TODO: replace with "native" FACTORY.get.. calls
            final GreaterThanEquals gte = new GreaterThanEquals();
            gte.setLeftExpression(leftExpression);
            gte.setRightExpression(e1);

            final MinorThanEquals mte = new MinorThanEquals();
            mte.setLeftExpression(leftExpression);
            mte.setRightExpression(e2);

            final AndExpression e = new AndExpression(gte, mte);
            result = visitEx(e);
        }


        @Override
        public void visit(InExpression expression) {

            // TODO: replace with "native" FACTORY.get.. calls

            Expression left = expression.getLeftExpression();
            ExpressionList rightItemsList = (ExpressionList) expression.getRightItemsList();
            if (rightItemsList == null)
                throw new UnsupportedOperationException();

            final ImmutableList.Builder<EqualsTo> builderEqualsToList = new ImmutableList.Builder<>();
            rightItemsList.getExpressions().forEach( item -> {
                final EqualsTo eq = new EqualsTo();
                eq.setLeftExpression(left);
                eq.setRightExpression(item);
                builderEqualsToList.add(eq);
            });
            ImmutableList<EqualsTo> equalsToList = builderEqualsToList.build();
            int size = equalsToList.size();
            if (size > 1) {
                OrExpression or = new OrExpression(equalsToList.get(size - 1), equalsToList.get(size - 2));

                for (int i = size - 3; i >= 0; i--)
                    or = new OrExpression(equalsToList.get(i), or);

                result = visitEx(or);
            } else {
                result = visitEx(equalsToList.get(0));
            }
        }

        /*
                UNARY OPERATIONS
         */

        @Override
        public void visit(IsNullExpression expression) {
            // TODO: why not process as the usual subexpression?
            Column column = (Column)expression.getLeftExpression();
            Term var = getVariable(column);
            // TODO: not runtime exception
            if (var == null) {
                throw new RuntimeException(
                        "Unable to find column name for variable: " + column);
            }

            if (!expression.isNot()) {
                result = FACTORY.getFunctionIsNull(var);
            } else {
                result = FACTORY.getFunctionIsNotNull(var);
            }
        }

        @Override
        public void visit(Parenthesis expression) {
            Expression inside = expression.getExpression();

            //Consider the case of NOT(...)
            if (expression.isNot())
                result = FACTORY.getFunctionNOT(visitEx(inside));
            else
                result = visitEx(inside);
        }

        // TODO: not sure should not be supported
        @Override
        public void visit(SignedExpression signedExpression) {
            throw new UnsupportedOperationException();
        }



        @Override
        public void visit(Column expression) {

            Term term = getVariable(expression);

            if (term != null) {
                /*
                 * If the termName is not null, create a variable
                 */
                result = term;
            }
            else {
                // TODO: careful here
                // Constructs constant
                // if the columns contains a boolean value
                String columnName = expression.getColumnName();
                // check whether it is an SQL boolean value
                String lowerCase = columnName.toLowerCase();
                if (lowerCase.equals("true")) {
                    result = FACTORY.getBooleanConstant(true);
                }
                else if (lowerCase.equals("false")) {
                    result = FACTORY.getBooleanConstant(false);
                }
                else
                    throw new RuntimeException( "Unable to find column name for variable: "
                            + columnName);
            }

        }


        private Term getVariable(Column expression) {
            QuotedID column = idfac.createAttributeID(expression.getColumnName());
            RelationID relation = null;
            if (expression.getTable().getName() != null)
                relation = idfac.createRelationID(expression.getTable().getSchemaName(), expression.getTable().getName());

            QualifiedAttributeID qa = new QualifiedAttributeID(relation, column);

            return attributes.get(qa);
        }

        @Override
        // TODO: this should be supported
        public void visit(CaseExpression caseExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        // TODO: this should be supported
        public void visit(WhenClause whenClause) {
            throw new UnsupportedOperationException();
        }


        @Override
        public void visit(CastExpression expression) {
            // TODO
            Expression column = expression.getLeftExpression();
            String columnName = column.toString();
            //    String variableName = attributes.lookup(columnName);
            //    if (variableName == null) {
            //        throw new RuntimeException(
            //                "Unable to find column name for variable: " + columnName);
            //    }
            //    Term var = FACTORY.getVariable(variableName);

            //     ColDataType datatype = expression.getType();



            //    Term var2 = null;

            //first value is a column, second value is a datatype. It can  also have the size

            //    result = FACTORY.getFunctionCast(var, var2);

        }



        @Override
        public void visit(SubSelect subSelect) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(ExistsExpression existsExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(AllComparisonExpression allComparisonExpression) { throw new UnsupportedOperationException(); }

        @Override
        public void visit(AnyComparisonExpression anyComparisonExpression) { throw new UnsupportedOperationException(); }

        @Override
        public void visit(Matches matches) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(BitwiseAnd bitwiseAnd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(BitwiseOr bitwiseOr) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(BitwiseXor bitwiseXor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(AnalyticExpression expression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(ExtractExpression expression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(IntervalExpression expression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(OracleHierarchicalExpression expression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(JsonExpression jsonExpr) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(JdbcParameter jdbcParameter) {
            // TODO:  exception
            // do nothing
        }

        @Override
        public void visit(JdbcNamedParameter jdbcNamedParameter) {
            // TODO:  exception
            // do nothing
        }

    }

}
