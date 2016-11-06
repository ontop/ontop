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
                            ImmutableList<Function> on =  getAtomsFromExpression(current, join.getOnExpression());
                            current = RelationalExpression.joinOn(current, on);
                        }else if ( join.getUsingColumns() != null ){
                             current = RelationalExpression.joinUsing(current, right, join.getUsingColumns());
                        }
                    }
                }
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



    private ImmutableList<Function> getAtomsFromExpression(RelationalExpression current, Expression onExpressionItem) {



        return new ExpressionItemProcessor(current, onExpressionItem).getAtoms();
    }


    private  class  ExpressionItemProcessor implements ExpressionVisitor{
        private Logger logger = LoggerFactory.getLogger(getClass());
        ImmutableMap.Builder attributesBuilder = ImmutableMap.<QualifiedAttributeID, Variable>builder();

        private ImmutableList.Builder<Function> atomsListBuilder = new ImmutableList.Builder<>();
        private ImmutableList.Builder<Column> columnsListBuilder = new ImmutableList.Builder<>();

        private ImmutableList.Builder<Term> valuesListBuilder = new ImmutableList.Builder<>();
        private final RelationalExpression current;

        final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();


        ExpressionItemProcessor(RelationalExpression current, Expression onExpressionItem) {
            this.current = current;
            onExpressionItem.accept(this);
        }

        public ImmutableList<Function> getAtoms(){
            return atomsListBuilder.build();
        }

        public ImmutableList<Column> getColumns(){
            return  columnsListBuilder.build();
        }

        public ImmutableList<Term> getValues() {
            return valuesListBuilder.build();
        }


        @Override
        public void visit(NullValue nullValue) {
            logger.debug("Visit NullValue");

        }

        @Override
        public void visit(net.sf.jsqlparser.expression.Function function) {
            logger.debug("Visit Function");
        }

        @Override
        public void visit(SignedExpression signedExpression) {
            logger.debug("Visit signedExpression");
        }

        @Override
        public void visit(JdbcParameter jdbcParameter) {
            logger.debug("Visit jdbcParameter");
        }

        @Override
        public void visit(JdbcNamedParameter jdbcNamedParameter) {
            logger.debug("Visit jdbcNamedParameter");
        }

        @Override
        public void visit(DoubleValue doubleValue) {
            logger.debug("Visit doubleValue");
            valuesListBuilder.add( fac.getConstantLiteral(doubleValue.toString()));
        }

        @Override
        public void visit(LongValue longValue) {
            logger.debug("Visit longValue");
            valuesListBuilder.add( fac.getConstantLiteral(longValue.toString()));
        }

        @Override
        public void visit(DateValue dateValue) {
            logger.debug("Visit dateValue");
            valuesListBuilder.add( fac.getConstantLiteral(dateValue.toString()));
        }

        @Override
        public void visit(TimeValue timeValue) {
            logger.debug("Visit timeValue");
            valuesListBuilder.add( fac.getConstantLiteral(timeValue.toString()));
        }

        @Override
        public void visit(TimestampValue timestampValue) {
            logger.debug("Visit timestampValue");
            valuesListBuilder.add( fac.getConstantLiteral(timestampValue.toString()));
        }

        @Override
        public void visit(Parenthesis parenthesis) {
            logger.debug("Visit parenthesis");
            throw new UnsupportedSelectQuery("Unsupported parenthesis ", parenthesis);
        }

        @Override
        public void visit(StringValue stringValue) {
            logger.debug("Visit stringValue");
            valuesListBuilder.add( fac.getConstantLiteral(stringValue.getValue()));

        }

        @Override
        public void visit(Addition addition) {
            logger.debug("Visit addition");
            throw new UnsupportedSelectQuery("Unsupported addition operations ", addition);
        }

        @Override
        public void visit(Division division) {
            logger.debug("Visit division");
            throw new UnsupportedSelectQuery("Unsupported division operations ", division);
        }

        @Override
        public void visit(Multiplication multiplication) {
            logger.debug("Visit multiplication");
            throw new UnsupportedSelectQuery("Unsupported division operations ", multiplication);
        }

        @Override
        public void visit(Subtraction subtraction) {
            logger.debug("Visit subtraction");
            throw new UnsupportedSelectQuery("Unsupported division operations ", subtraction);
        }

        @Override
        public void visit(AndExpression andExpression) {
            logger.debug("Visit andExpression");
            // TODO: this should be supported

        }

        @Override
        public void visit(OrExpression orExpression) {
            logger.debug("Visit orExpression");
            // TODO: this should be supported


        }

        @Override
        public void visit(Between between) {
            logger.debug("Visit Between");
             // TODO: this has to be implemented
        }

        @Override
        public void visit(EqualsTo equalsTo) {
            logger.debug("Visit EqualsTo");
            Term[] terms = getTermsJoinColumn(equalsTo);
            atomsListBuilder.add(fac.getFunction(ExpressionOperation.EQ, terms[0], terms[1]));
        }

        private Term[] getTermsJoinColumn(BinaryExpression  expression){
            ExpressionItemProcessor columnItemVisitor = new ExpressionItemProcessor(current, expression.getLeftExpression());

            Term firstTerm;
            if  ( !(columnItemVisitor.getColumns().isEmpty() || columnItemVisitor.getColumns().size() > 1 ) ){
                final QualifiedAttributeID leftAttribute = getAttributeFromColumn(columnItemVisitor.getColumns().get(0));
                firstTerm = current.getAttributes().get(leftAttribute).clone();
            }else if ( ! (columnItemVisitor.getValues().isEmpty() || columnItemVisitor.getValues().size()>1 ) ) {
                firstTerm = columnItemVisitor.getValues().get(0);
            }else
                throw new UnsupportedSelectQuery("unsupported operation", expression.getLeftExpression());

            Term secondTerm;
            columnItemVisitor = new ExpressionItemProcessor(current, expression.getRightExpression());
            if  ( !( columnItemVisitor.getColumns().isEmpty() || columnItemVisitor.getColumns().size() > 1 ) ) {
                final QualifiedAttributeID rightAttribute = getAttributeFromColumn(columnItemVisitor.getColumns().get(0));
                secondTerm = current.getAttributes().get(rightAttribute).clone();
            }else if ( !(columnItemVisitor.getValues().isEmpty() || columnItemVisitor.getValues().size()>1 ) ){
                secondTerm = columnItemVisitor.getValues().get(0);
            }else
                throw new UnsupportedSelectQuery("unsupported operation", expression.getRightExpression());

            return new Term[]{ firstTerm, secondTerm};
        }


        private QualifiedAttributeID getAttributeFromColumn(Column column){
            final QuotedID attributeID = metadata.getQuotedIDFactory().createAttributeID(column.getColumnName());
            final RelationID relationID = metadata.getQuotedIDFactory().createRelationID(null, column.getTable().getName());
            return new QualifiedAttributeID(relationID, attributeID );
        }

        @Override
        public void visit(GreaterThan greaterThan) {
            logger.debug("Visit GreaterThan");
            Term[] terms = getTermsJoinColumn(greaterThan);
            atomsListBuilder.add(fac.getFunction(ExpressionOperation.GT, terms[0], terms[1]));
        }

        @Override
        public void visit(GreaterThanEquals greaterThanEquals) {
            logger.debug("Visit GreaterThanEquals");
            Term[] terms = getTermsJoinColumn(greaterThanEquals);
            atomsListBuilder.add(fac.getFunction(ExpressionOperation.GTE, terms[0], terms[1]));
        }

        @Override
        public void visit(InExpression inExpression) {
            logger.debug("Visit InExpression");
        }

        @Override
        public void visit(IsNullExpression isNullExpression) {
            logger.debug("Visit IsNullExpression");
        }

        @Override
        public void visit(LikeExpression likeExpression) {
            logger.debug("Visit LikeExpression");
            // TODO: Does like is present?
        }

        @Override
        public void visit(MinorThan minorThan) {
            logger.debug("Visit MinorThan");
            Term[] terms = getTermsJoinColumn(minorThan);
            atomsListBuilder.add(fac.getFunction(ExpressionOperation.LT, terms[0], terms[1]));
        }

        @Override
        public void visit(MinorThanEquals minorThanEquals) {
            logger.debug("Visit MinorThanEquals");
            Term[] terms = getTermsJoinColumn(minorThanEquals);
            atomsListBuilder.add(fac.getFunction(ExpressionOperation.LTE, terms[0], terms[1]));
        }

        @Override
        public void visit(NotEqualsTo notEqualsTo) {
            logger.debug("Visit NotEqualsTo");
            Term[] terms = getTermsJoinColumn(notEqualsTo);
            atomsListBuilder.add(fac.getFunction(ExpressionOperation.NEQ, terms[0], terms[1]));
        }

        @Override
        public void visit(Column tableColumn) {
            logger.debug("Visit Column ", tableColumn);
            columnsListBuilder.add(tableColumn);
        }

        @Override
        public void visit(SubSelect subSelect) {
            throw new UnsupportedSelectQuery("SubSelect are not supported", subSelect);
        }

        @Override
        public void visit(CaseExpression caseExpression) {
            logger.debug("Visit CaseExpression");
        }

        @Override
        public void visit(WhenClause whenClause) {
            logger.debug("Visit WhenClause");
        }

        @Override
        public void visit(ExistsExpression existsExpression) {
            logger.debug("Visit ExistsExpression");
        }

        @Override
        public void visit(AllComparisonExpression allComparisonExpression) {
            logger.debug("Visit AllComparisonExpression");
        }

        @Override
        public void visit(AnyComparisonExpression anyComparisonExpression) {
            logger.debug("Visit AnyComparisonExpression");
        }

        @Override
        public void visit(Concat concat) {
            logger.debug("Visit Concat");
        }

        @Override
        public void visit(Matches matches) {
            logger.debug("Visit Matches");
        }

        @Override
        public void visit(BitwiseAnd bitwiseAnd) {
            logger.debug("Visit BitwiseAnd");
        }

        @Override
        public void visit(BitwiseOr bitwiseOr) {
            logger.debug("Visit BitwiseOr");
        }

        @Override
        public void visit(BitwiseXor bitwiseXor) {
            logger.debug("Visit BitwiseXor");
        }

        @Override
        public void visit(CastExpression cast) {
            logger.debug("Visit CastExpression");
        }

        @Override
        public void visit(Modulo modulo) {
            logger.debug("Visit Modulo");
        }

        @Override
        public void visit(AnalyticExpression aexpr) {
            logger.debug("Visit AnalyticExpression");
        }

        @Override
        public void visit(ExtractExpression eexpr) {
            logger.debug("Visit ExtractExpression");
        }

        @Override
        public void visit(IntervalExpression iexpr) {
            logger.debug("Visit IntervalExpression");
        }

        @Override
        public void visit(OracleHierarchicalExpression oexpr) {
            logger.debug("Visit OracleHierarchicalExpression");
        }

        @Override
        public void visit(RegExpMatchOperator rexpr) {
            logger.debug("Visit RegExpMatchOperator");
        }

        @Override
        public void visit(JsonExpression jsonExpr) {
            throw new UnsupportedSelectQuery("JsonExpression are not supported", jsonExpr);
        }

        @Override
        public void visit(RegExpMySQLOperator regExpMySQLOperator) {
            logger.debug("Visit RegExpMySQLOperator");
        }
    }





}
