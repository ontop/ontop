package it.unibz.inf.ontop.sql.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.impl.*;
import it.unibz.inf.ontop.parser.*;
import it.unibz.inf.ontop.sql.*;
import it.unibz.inf.ontop.sql.parser.exceptions.InvalidSelectQuery;
import it.unibz.inf.ontop.sql.parser.exceptions.UnsupportedSelectQuery;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 */
public class SelectQueryParser {
    public static final String QUERY_NOT_SUPPORTED = "Query not yet supported";

    private static Logger log = LoggerFactory.getLogger(SQLQueryDeepParser.class);
    private final DBMetadata metadata;
    private int relationIndex = 0;

    public SelectQueryParser(DBMetadata metadata) {
        this.metadata = metadata;
    }


    public CQIE parse(String sql) {

        boolean errors = false;
        CQIE parsedSql = null;

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select))
                throw new InvalidSelectQuery("The inserted query is not a SELECT statement", statement);

            Select select = (Select) statement;

            SelectBody selectBody = select.getSelectBody();
            if (!(selectBody instanceof PlainSelect))
                throw new UnsupportedSelectQuery("Complex SELECT statements are not supported", selectBody);

            PlainSelect plainSelect = (PlainSelect)selectBody;

            RelationalExpression current = getRelationalExpression(plainSelect.getFromItem());
            if (plainSelect.getJoins() != null) {
                for (Join join : plainSelect.getJoins()) {
                    RelationalExpression right = getRelationalExpression(join.getRightItem());
                    if (join.isCross() || join.isSimple())
                        current = RelationalExpression.crossJoin(current, right);
                    else if  ( join.isNatural() ){
                        current = RelationalExpression.naturalJoin(current, right);
                    }else if( join.isInner() ) {
                        if (join.getOnExpression() != null) {
                            current = RelationalExpression.crossJoin( current, right);
                            Function on =  getAtomsFromExpression(current, join.getOnExpression());
                            current = RelationalExpression.addAtom(current, on);
                        }else if ( join.getUsingColumns() != null ){
                             current = RelationalExpression.joinUsing(current, right, join.getUsingColumns());
                        }
                    }
                }
            }

            if (plainSelect.getWhere() != null ) {
                final Function atomsFromExpression = getAtomsFromExpression(current, plainSelect.getWhere());
                current = RelationalExpression.addAtom(current, atomsFromExpression);
            }


            final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
            // TODO: proper handling of the head predicate
            parsedSql = fac.getCQIE(
                    fac.getFunction(fac.getPredicate("Q", new Predicate.COL_TYPE[] {})),
                    current.getAtoms());
        }
        catch (JSQLParserException e) {
            if (e.getCause() instanceof ParseException)
                log.warn("Parse exception, check no SQL reserved keywords have been used "+ e.getCause().getMessage());
            errors = true;
        }

        if (parsedSql == null || errors) {
            log.warn("The following query couldn't be parsed. " +
                    "This means Quest will need to use nested subqueries (views) to use this mappings. " +
                    "This is not good for SQL performance, specially in MySQL. " +
                    "Try to simplify your query to allow Quest to parse it. " +
                    "If you think this query is already simple and should be parsed by Quest, " +
                    "please contact the authors. \nQuery: '{}'", sql);

            ParserViewDefinition viewDef = createViewDefinition(sql);
            // TODO: proper handling
            parsedSql = null;
        }
        return parsedSql;
    }

    private ParserViewDefinition createViewDefinition(String sql) {

        QuotedIDFactory idfac = metadata.getQuotedIDFactory();

        // TODO: TRY TO GET COLUMN NAMES USING JSQLParser
        boolean supported = false;

        ParserViewDefinition viewDefinition = metadata.createParserView(sql);

        if (supported) {
            List<Column> columns = null;
            for (Column column : columns) {
                QuotedID columnId = idfac.createAttributeID(column.getColumnName());
                RelationID relationId;
                Table table = column.getTable();
                if (table == null) // this column is an alias
                    relationId = viewDefinition.getID();
                else
                    relationId = idfac.createRelationID(table.getSchemaName(), table.getName());

                viewDefinition.addAttribute(new QualifiedAttributeID(relationId, columnId));
            }
        }
        else {
            int start = "select".length();
            int end = sql.toLowerCase().indexOf("from");
            if (end == -1)
                throw new RuntimeException("Error parsing SQL query: Couldn't find FROM clause");


            String projection = sql.substring(start, end).trim();

            //split where comma is present but not inside parenthesis
            String[] columns = projection.split(",+(?!.*\\))");
//            String[] columns = projection.split(",+(?![^\\(]*\\))");


            for (String col : columns) {
                String columnName = col.trim();

    			/*
    			 * Take the alias name if the column name has it.
    			 */
                final String[] aliasSplitters = new String[] { " as ",  " AS " };

                for (String aliasSplitter : aliasSplitters) {
                    if (columnName.contains(aliasSplitter)) { // has an alias
                        columnName = columnName.split(aliasSplitter)[1].trim();
                        break;
                    }
                }
                ////split where space is present but not inside single quotes
                if (columnName.contains(" "))
                    columnName = columnName.split("\\s+(?![^'\"]*')")[1].trim();

                // Get only the short name if the column name uses qualified name.
                // Example: table.column -> column
                if (columnName.contains(".")) {
                    columnName = columnName.substring(columnName.lastIndexOf(".") + 1); // get only the name
                }
                // TODO (ROMAN 20 Oct 2015): extract schema and table name as well

                QuotedID columnId = idfac.createAttributeID(columnName);

                viewDefinition.addAttribute(new QualifiedAttributeID(null, columnId));
            }
        }
        return viewDefinition;
    }

    private RelationalExpression getRelationalExpression(FromItem fromItem) {
        return new FromItemProcessor(fromItem).result;
    }


    private class FromItemProcessor implements FromItemVisitor {

        private RelationalExpression result = null;

        public FromItemProcessor(FromItem fromItem) {
            fromItem.accept(this);
        }

        @Override
        public void visit(Table tableName) {
            final QuotedIDFactory idfac = metadata.getQuotedIDFactory();
            RelationID id = idfac.createRelationID(tableName.getSchemaName(), tableName.getName());
            // Construct the predicate using the table name
            DatabaseRelationDefinition relation = metadata.getDatabaseRelation(id);
            if (relation == null)
                throw new InvalidSelectQuery("Table " + id + " not found in metadata", tableName);
            relationIndex++;

            final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
            List<Term> terms = new ArrayList<>(relation.getAttributes().size());
            ImmutableMap.Builder attributesBuilder = ImmutableMap.<QualifiedAttributeID, Variable>builder();
            ImmutableMap.Builder occurrencesBuilder = ImmutableMap.<QuotedID, ImmutableSet<RelationID>>builder();
            for (Attribute attribute : relation.getAttributes()) {
                Variable var = fac.getVariable(attribute.getID().getName() + relationIndex);
                terms.add(var);

                attributesBuilder.put(attribute.getQualifiedID(), var);
                attributesBuilder.put(new QualifiedAttributeID(null, attribute.getID()), var);

                occurrencesBuilder.put(attribute.getID(), ImmutableSet.of(relation.getID()));
            }
            // Create an atom for a particular table
            Function atom = Relation2DatalogPredicate.getAtom(relation, terms);

            result = new RelationalExpression(ImmutableList.of(atom),
                    attributesBuilder.build(), occurrencesBuilder.build());
        }



        @Override
        public void visit(SubSelect subSelect) {
            // TODO: implementation
        }

        @Override
        public void visit(SubJoin subjoin) {
            throw new UnsupportedSelectQuery("Subjoins are not supported", subjoin);
        }

        @Override
        public void visit(LateralSubSelect lateralSubSelect) {
            throw new UnsupportedSelectQuery("LateralSubSelects are not supported", lateralSubSelect);
        }

        @Override
        public void visit(ValuesList valuesList) {
            throw new UnsupportedSelectQuery("ValuesLists are not supported", valuesList);
        }
    }

    private Function getAtomsFromExpression(RelationalExpression current, Expression expression) {
        return new ExpressionOperator(current.getAttributes(), metadata.getQuotedIDFactory()).convert(expression);
    }



    /**
     * This visitor class converts the SQL Expression to a Function
     *
     */
    public static class ExpressionOperator implements ExpressionVisitor {

        // TODO: this class  should be review

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

        private final QuotedIDFactory idfac;
        private final ImmutableMap<QualifiedAttributeID, Variable> attributes;
        private Term result;

        public ExpressionOperator(ImmutableMap<QualifiedAttributeID, Variable> attributes, QuotedIDFactory idfac) {
            this.attributes = attributes;
            this.idfac = idfac;
        }

        public Function convert(Expression expression){
            expression.accept(this);
            return (Function)result;
        }

        /**
         * Visits the expression and gets the result
         */
        private Term visitEx(Expression expression) {
            expression.accept(this);
            return this.result;
        }

        private void visitBinaryExpression(BinaryExpression expression){
            Expression left = expression.getLeftExpression();
            Expression right = expression.getRightExpression();

            Term t1 = visitEx(left);
            if (t1 == null)
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

        @Override
        public void visit(SignedExpression signedExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(JdbcParameter jdbcParameter) {
            // do nothing
        }

        @Override
        public void visit(JdbcNamedParameter jdbcNamedParameter) {
            // do nothing
        }

        @Override
        public void visit(DoubleValue expression) {
            String termRightName = expression.toString();
            result = FACTORY.getConstantLiteral(termRightName, Predicate.COL_TYPE.DOUBLE);
        }

        @Override
        public void visit(LongValue expression) {
            String termRightName = expression.getStringValue();
            result = FACTORY.getConstantLiteral(termRightName, Predicate.COL_TYPE.LONG);
        }

        @Override
        public void visit(DateValue expression) {
            String termRightName = expression.getValue().toString();
            result = FACTORY.getConstantLiteral(termRightName, Predicate.COL_TYPE.DATE);
        }

        @Override
        public void visit(TimeValue expression) {
            String termRightName = expression.getValue().toString();
            result = FACTORY.getConstantLiteral(termRightName, Predicate.COL_TYPE.TIME);
        }

        @Override
        public void visit(TimestampValue expression) {
            String termRightName = expression.getValue().toString();
            result = FACTORY.getConstantLiteral(termRightName, Predicate.COL_TYPE.DATETIME);
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

        @Override
        public void visit(StringValue expression) {
            String termRightName = expression.getValue();
            result = FACTORY.getConstantLiteral(termRightName, Predicate.COL_TYPE.STRING);
        }

        @Override
        public void visit(Addition addition) {
            visitBinaryExpression(addition);
        }

        @Override
        public void visit(Division division) {
            visitBinaryExpression(division);
        }

        @Override
        public void visit(Multiplication multiplication) {
            visitBinaryExpression(multiplication);
        }

        @Override
        public void visit(Subtraction subtraction) {
            visitBinaryExpression(subtraction);
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
        public void visit(InExpression expression) {
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

        @Override
        public void visit(IsNullExpression expression) {
            Column column = (Column)expression.getLeftExpression();
            Term var = getVariable(column);
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
        public void visit(LikeExpression likeExpression) {
            visitBinaryExpression(likeExpression);
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
        public void visit(Column expression) {

            Term term = getVariable(expression);

            if (term != null) {
                /*
                 * If the termName is not null, create a variable
                 */
                result = term;
            }
            else {
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
        public void visit(SubSelect subSelect) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(CaseExpression caseExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(WhenClause whenClause) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(ExistsExpression existsExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(AllComparisonExpression allComparisonExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(AnyComparisonExpression anyComparisonExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(Concat concat) {
            Expression left = concat.getLeftExpression();
            Expression right = concat.getRightExpression();
            Term l = visitEx(left);
            Term r = visitEx(right);
            result = FACTORY.getFunction(ExpressionOperation.CONCAT, l, r);
        }

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
        public void visit(Modulo modulo) {
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
        public void visit(RegExpMatchOperator expression) {
            visitBinaryExpression(expression);
        }

        @Override
        public void visit(JsonExpression jsonExpr) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(RegExpMySQLOperator regExpMySQLOperator) {
            visitBinaryExpression(regExpMySQLOperator);
        }
    }

}
