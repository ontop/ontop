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
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;


/**
 * Created by Roman Kontchakov on 10/11/2016.
 *
 */

public class ExpressionParser implements java.util.function.Function<ImmutableMap<QualifiedAttributeID, Variable>, Term> {

    private final QuotedIDFactory idfac;
    private final Expression root;

    private static final OBDADataFactory FACTORY = OBDADataFactoryImpl.getInstance();

    public   ExpressionParser(QuotedIDFactory idfac, Expression expression) {
        this.idfac = idfac;
        this.root = expression;
    }

    @Override
    public Term apply(ImmutableMap<QualifiedAttributeID, Variable> attributes) {
        ExpressionVisitorImpl visitor = new ExpressionVisitorImpl(attributes);
        return visitor.getTerm(root);
    }




    // ---------------------------------------------------------------
    // supported SQL functions
    // (WARNING: not all combinations of the parameters are supported)
    // ---------------------------------------------------------------

    private static final ImmutableMap<String, BiFunction<ImmutableList<Term>, net.sf.jsqlparser.expression.Function, Function>>
            FUNCTIONS = ImmutableMap.<String, BiFunction<ImmutableList<Term>, net.sf.jsqlparser.expression.Function, Function>>builder()
                .put("REGEXP_LIKE", ExpressionParser::get_REGEXP_LIKE)
                .put("REGEXP_REPLACE", ExpressionParser::get_REGEXP_REPLACE)
                .put("REPLACE", ExpressionParser::get_REPLACE)
                .put("CONCAT", ExpressionParser::get_CONCAT)
                .put("SUBSTR", ExpressionParser::get_SUBSTR)
                .put("SUBSTRING", ExpressionParser::get_SUBSTR)
                .put("LCASE", ExpressionParser::get_LCASE)
                .put("LOWER", ExpressionParser::get_LCASE)
                .put("UCASE", ExpressionParser::get_UCASE)
                .put("UPPER", ExpressionParser::get_UCASE)
                .put("LENGTH", ExpressionParser::get_STRLEN)
                .build();

    private static Function get_REGEXP_LIKE(ImmutableList<Term> terms, net.sf.jsqlparser.expression.Function expression) {
        // Oracle only:
        // a source string, a regex pattern (POSIX regular expression), an optional flags
        switch (terms.size()) {
            case 2:
                return FACTORY.getFunction(
                        ExpressionOperation.REGEX, terms.get(0), terms.get(1), FACTORY.getConstantLiteral(""));
            case 3:
                // check the flag?
                return FACTORY.getFunction(
                        ExpressionOperation.REGEX, terms.get(0), terms.get(1), terms.get(2));
        }
        throw new InvalidSelectQueryException("Wrong number of arguments for SQL function", expression);
    }

    private static Function get_REGEXP_REPLACE(ImmutableList<Term> terms, net.sf.jsqlparser.expression.Function expression) {
        Term flags;
        switch (terms.size()) {
            case 3:
                // either Oracle or PostgreSQL, without flags
                flags = FACTORY.getConstantLiteral(""); // the 4th argument is flags
                break;
            case 4:
                if (((ValueConstant)terms.get(3)).getType() == Predicate.COL_TYPE.STRING) {
                    // PostgreSQL
                    flags =  terms.get(3);
                    // check that flags is either ig or g
                }
                else
                    throw new UnsupportedSelectQueryException("Unsupported SQL function", expression);
                break;
            case 6:
                // Oracle
                if (!terms.get(3).equals(FACTORY.getConstantLiteral("1", Predicate.COL_TYPE.LONG))
                        || !terms.get(4).equals(FACTORY.getConstantLiteral("0", Predicate.COL_TYPE.LONG)))
                    throw new UnsupportedSelectQueryException("Unsupported SQL function", expression);

                // check that the flags is a combination of imx
                flags = terms.get(5);
                break;
            default:
                throw new UnsupportedSelectQueryException("Unsupported SQL function", expression);
        }

        return FACTORY.getFunction(
                ExpressionOperation.REPLACE, terms.get(0), terms.get(1), terms.get(2), flags);
    }

    private static Function get_REPLACE(ImmutableList<Term> terms, net.sf.jsqlparser.expression.Function expression) {
        Term flags = FACTORY.getConstantLiteral("");
        switch (terms.size()) {
            case 2:
                return FACTORY.getFunction(
                        ExpressionOperation.REPLACE, terms.get(0), terms.get(1),
                        FACTORY.getConstantLiteral(""), flags);
            case 3:
                return FACTORY.getFunction(
                        ExpressionOperation.REPLACE, terms.get(0), terms.get(1), terms.get(2), flags);

        }
        throw new InvalidSelectQueryException("Wrong number of arguments in SQL function", expression);
    }

    private static Function get_CONCAT(ImmutableList<Term> terms, net.sf.jsqlparser.expression.Function expression) {
        return (Function)
                terms.stream()  // left recursion to match || in JSQLParser
                        .reduce(null, (a, b) -> (a == null)
                                ? b
                                : FACTORY.getFunction(ExpressionOperation.CONCAT, a, b));
    }

    private static Function get_SUBSTR(ImmutableList<Term> terms, net.sf.jsqlparser.expression.Function expression) {
        switch (terms.size()) {
            case 2:
                return FACTORY.getFunction(ExpressionOperation.SUBSTR2, terms.get(0), terms.get(1));
            case 3:
                return FACTORY.getFunction(ExpressionOperation.SUBSTR3, terms.get(0), terms.get(1), terms.get(2));
        }
        // DB2 has 4
        throw new UnsupportedSelectQueryException("Unsupported SQL function", expression);
    }

    private static Function get_LCASE(ImmutableList<Term> terms, net.sf.jsqlparser.expression.Function expression) {
        if (terms.size() == 1)
            return FACTORY.getFunction(ExpressionOperation.LCASE, terms.get(0));
        // DB2 has 3
        throw new UnsupportedSelectQueryException("Unsupported SQL function", expression);
    }

    private static Function get_UCASE(ImmutableList<Term> terms, net.sf.jsqlparser.expression.Function expression) {
        if (terms.size() == 1)
            return FACTORY.getFunction(ExpressionOperation.UCASE, terms.get(0));
        // DB2 has 3
        throw new UnsupportedSelectQueryException("Unsupported SQL function", expression);
    }

    private static Function get_STRLEN(ImmutableList<Term> terms, net.sf.jsqlparser.expression.Function expression) {
        if (terms.size() == 1)
            return FACTORY.getFunction(ExpressionOperation.STRLEN, terms.get(0));

        throw new InvalidSelectQueryException("Wrong number of arguments in SQL function", expression);
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

        // CAREFUL: this variable gets reset in each visit method implementation
        // concurrent evaluation is not possible
        private Term result;

        ExpressionVisitorImpl(ImmutableMap<QualifiedAttributeID, Variable> attributes) {
            this.attributes = attributes;
        }

        private Term getTerm(Expression expression) {
            expression.accept(this);
            return this.result;
        }

        private java.util.function.UnaryOperator<Term> notOperation(boolean isNot) {
            return isNot
                    // cancel double negation
                    ? term -> ((term instanceof Function) &&
                            ((Function)term).getFunctionSymbol() == ExpressionOperation.NOT)
                            ? ((Function)term).getTerm(0)
                            : FACTORY.getFunctionNOT(term)
                    : UnaryOperator.identity();
        }

        private void process(BinaryExpression expression, BinaryOperator<Term> op) {
            Term leftTerm = getTerm(expression.getLeftExpression());
            Term rightTerm = getTerm(expression.getRightExpression());
            Term expTerm = op.apply(leftTerm, rightTerm);
            result = notOperation(expression.isNot()).apply(expTerm);
        }

        private void processOJ(OldOracleJoinBinaryExpression expression, BinaryOperator<Term> op) {
            if (expression.getOraclePriorPosition() != SupportsOldOracleJoinSyntax.NO_ORACLE_PRIOR)
                throw new UnsupportedSelectQueryException("Oracle PRIOR is not supported", expression);

            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                throw new UnsupportedSelectQueryException("Old Oracle OUTER JOIN syntax is not supported", expression);

            process(expression, op);
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
        public void visit(net.sf.jsqlparser.expression.Function expression) {
            // do not use ImmutableCollectors.toList because this cannot be done concurrently
            ImmutableList<Term> terms = (expression.getParameters() != null)
                    ? ImmutableList.<Term>builder()
                        .addAll(expression.getParameters().getExpressions().stream()
                            .map(t -> getTerm(t)).iterator())
                        .build()
                    : ImmutableList.of();

            BiFunction<ImmutableList<Term>, net.sf.jsqlparser.expression.Function, Function> function
                    = FUNCTIONS.get(expression.getName().toUpperCase());

            if (function == null)
                throw new UnsupportedSelectQueryException("Unsupported SQL function", expression);

            result = function.apply(terms, expression);
        }


        // ------------------------------------------------------------
        //        CONSTANT EXPRESSIONS
        // ------------------------------------------------------------

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

        // ------------------------------------------------------------
        //        BINARY OPERATIONS
        // ------------------------------------------------------------

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
            processOJ(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.EQ, t1, t2));
        }

        @Override
        public void visit(GreaterThan expression) {
            processOJ(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.GT, t1, t2));
        }

        @Override
        public void visit(GreaterThanEquals expression) {
            processOJ(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.GTE, t1, t2));
        }

        @Override
        public void visit(MinorThan expression) {
            processOJ(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.LT, t1, t2));
        }

        @Override
        public void visit(MinorThanEquals expression) {
            processOJ(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.LTE, t1, t2));
        }

        @Override
        public void visit(NotEqualsTo expression) {
            processOJ(expression, (t1, t2) -> FACTORY.getFunction(ExpressionOperation.NEQ, t1, t2));
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

        // POSIX Regular Expressions
        // e.g., https://www.postgresql.org/docs/9.6/static/functions-matching.html#FUNCTIONS-POSIX-REGEXP

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

            if (expression.getOldOracleJoinSyntax() != SupportsOldOracleJoinSyntax.NO_ORACLE_JOIN)
                throw new UnsupportedSelectQueryException("Oracle OUTER JOIN syntax is not supported", expression);

            Stream<Function> stream;
            Expression left = expression.getLeftExpression();
            if (left != null) {
                ItemsList right = expression.getRightItemsList();
                // right can be SubSelect, ExpressionList and MultiExpressionList
                if (right instanceof SubSelect)
                    throw new UnsupportedSelectQueryException("SubSelect in IN is not supported", expression);
                if (right instanceof MultiExpressionList)
                    throw new InvalidSelectQueryException("MultiExpressionList is not allowed with a single expression on the left in IN", expression);

                stream = ((ExpressionList)right).getExpressions().stream()
                                .map(item -> {
                                    Term t1 = getTerm(expression.getLeftExpression());
                                    Term t2 = getTerm(item);
                                    return FACTORY.getFunctionEQ(t1, t2);
                                });
            }
            else {
                ItemsList list = expression.getLeftItemsList();
                if (!(list instanceof ExpressionList))
                    throw new InvalidSelectQueryException("Only ExpressionList is allowed on the left of IN", expression);

                ItemsList right = expression.getRightItemsList();
                // right can be SubSelect, ExpressionList and MultiExpressionList
                if (right instanceof SubSelect)
                    throw new UnsupportedSelectQueryException("SubSelect in IN is not supported", expression);
                if (right instanceof ExpressionList)
                    throw new InvalidSelectQueryException("ExpressionList is not allowed with an ExpressionList on the left in IN", expression);

                /* MultiExpressionList is not supported by JSQLParser

                List<Expression> leftList = ((ExpressionList)list).getExpressions();

                stream = ((MultiExpressionList)right).getExprList().stream().map(el -> {
                    List<Expression> rightList  = el.getExpressions();
                    if (leftList.size() != rightList.size())
                        throw new InvalidSelectQueryException("Mismatch in the length of the lists", expression);

                    return getEqLists(leftList, rightList).reverse().stream()
                            .reduce(null, (a, b) -> (a == null) ? b : FACTORY.getFunctionAND(b, a));
                }); */
                throw new InvalidSelectQueryException("not possible in the current JSQLParser", expression);
            }

            // do not use ImmutableCollectors.toList because this cannot be done concurrently
            ImmutableList<Function> equalities =
                    ImmutableList.<Function>builder().addAll(stream.iterator()).build();

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
            switch (expression.getSign()) {
                case '-' :
                    process(expression.getExpression(),  t -> FACTORY.getFunction(ExpressionOperation.MINUS, t));
                    break;
                case '+':
                    process(expression.getExpression(), UnaryOperator.identity());
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
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

            if (var == null) {
                // can be
                //    - a CONSTANT or
                //    - a PSEUDO-COLUMN like ROWID, ROWNUM or
                //    - a FUNCTION without arguments like USER, CURRENT_DATE

                if (column.equals(idfac.createAttributeID("true")))
                    result = FACTORY.getBooleanConstant(true);
                else if (column.equals(idfac.createAttributeID("false")))
                    result = FACTORY.getBooleanConstant(false);
                else
                    throw new UnsupportedSelectQueryException("Unable to find attribute name ", expression);
            }
            else {
                // if it is an attribute name (qualified or not)
                result = var;
            }
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
            throw new UnsupportedSelectQueryException("CASE is not supported yet", expression);
            // expression.getSwitchExpression();
            // expression.getWhenClauses();
            // expression.getElseExpression();
        }

        @Override
        public void visit(WhenClause expression) {
            throw new UnsupportedSelectQueryException("CASE/WHEN is not supported yet", expression);
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

        // OracleHierarchicalExpression can only occur in the form of a clause after WHERE
        @Override
        public void visit(OracleHierarchicalExpression expression) {
            throw new UnsupportedOperationException("Unexpected Oracle START WITH ... CONNECT BY");
        }

        @Override
        public void visit(Matches expression) {
            throw new UnsupportedSelectQueryException("Oracle @@ not supported", expression);
            // would be processOJ
        }

        @Override
        public void visit(JsonExpression expression) {
            throw new UnsupportedSelectQueryException("JSON expressions are not supported", expression);
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
