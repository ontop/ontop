package it.unibz.inf.ontop.sql.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.impl.*;
import it.unibz.inf.ontop.parser.*;
import it.unibz.inf.ontop.sql.*;
import it.unibz.inf.ontop.sql.parser.exceptions.InvalidSelectQueryException;
import it.unibz.inf.ontop.sql.parser.exceptions.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.JSQLParserException;
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
    private static Logger log = LoggerFactory.getLogger(SQLQueryDeepParser.class);

    private final DBMetadata metadata;
    private final QuotedIDFactory idfac;

    private int relationIndex = 0;
    private boolean parseException;

    public SelectQueryParser(DBMetadata metadata) {
        this.metadata = metadata;
        this.idfac = metadata.getQuotedIDFactory();
    }

    public CQIE parse(String sql) {
        parseException = false;
        CQIE parsedSql = null;

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select))
                throw new InvalidSelectQueryException("The inserted query is not a SELECT statement", statement);

            Select select = (Select) statement;

            SelectBody selectBody = select.getSelectBody();
            if (!(selectBody instanceof PlainSelect))
                throw new UnsupportedSelectQueryException("Complex SELECT statements are not supported", selectBody);

            PlainSelect plainSelect = (PlainSelect) selectBody;

            RelationalExpression current = getRelationalExpression(plainSelect.getFromItem());
            if (plainSelect.getJoins() != null) {
                for (Join join : plainSelect.getJoins())
                    current = join(current, join);
            }

            if (plainSelect.getWhere() != null)
                current = RelationalExpression.where(current,
                        new BooleanExpressionParser(idfac, plainSelect.getWhere()));

            final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
            // TODO: proper handling of the head predicate
            parsedSql = fac.getCQIE(
                    fac.getFunction(fac.getPredicate("Q", new Predicate.COL_TYPE[]{})),
                    current.getAtoms());
        } catch (JSQLParserException e) {
            if (e.getCause() instanceof ParseException)
                log.warn("Parse exception, check no SQL reserved keywords have been used " + e.getCause().getMessage());
            parseException = true;
        }


        if (parsedSql == null || parseException) {
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

    public boolean isParseException() {
        return parseException;
    }

    private RelationalExpression join(RelationalExpression left, Join join) {

        if (join.isFull() || join.isRight() || join.isLeft() || join.isOuter())
            throw new UnsupportedSelectQueryException("LEFT/RIGHT/FULL OUTER JOINs are not supported", join);

        RelationalExpression right = getRelationalExpression(join.getRightItem());
        if (join.isSimple()) {
            return RelationalExpression.crossJoin(left, right);
        }
        else if (join.isCross()) {
            if (join.getOnExpression() != null || join.getUsingColumns() != null)
                throw new InvalidSelectQueryException("CROSS JOIN cannot have USING/ON conditions", join);

            if (join.isInner())
                throw new InvalidSelectQueryException("CROSS INNER JOIN is not allowed", join);

            return RelationalExpression.crossJoin(left, right);
        }
        else if (join.isNatural()) {
            if (join.getOnExpression() != null || join.getUsingColumns() != null)
                throw new InvalidSelectQueryException("NATURAL JOIN cannot have USING/ON conditions", join);

            if (join.isInner())
                throw new InvalidSelectQueryException("NATURAL INNER JOIN is not allowed", join);

            return RelationalExpression.naturalJoin(left, right);
        }
        else {
            if (join.getOnExpression() != null) {
                if (join.getUsingColumns() !=null)
                    throw new InvalidSelectQueryException("JOIN cannot have both USING and ON", join);

                return RelationalExpression.joinOn(left, right,
                        new BooleanExpressionParser(idfac, join.getOnExpression()));
            }
            else if (join.getUsingColumns() != null) {
                return RelationalExpression.joinUsing(left, right,
                        join.getUsingColumns().stream()
                                .map(p -> idfac.createAttributeID(p.getColumnName()))
                                .collect(ImmutableCollectors.toSet()));
            }
            else
                throw new InvalidSelectQueryException("[INNER] JOIN requires either ON or USING", join);
        }
    }


    private ParserViewDefinition createViewDefinition(String sql) {

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
        } else {
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
                final String[] aliasSplitters = new String[]{" as ", " AS "};

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

            RelationID id = idfac.createRelationID(tableName.getSchemaName(), tableName.getName());
            // Construct the predicate using the table name
            DatabaseRelationDefinition relation = metadata.getDatabaseRelation(id);
            if (relation == null)
                throw new InvalidSelectQueryException("Table " + id + " not found in metadata", tableName);
            relationIndex++;

            RelationID aliasId = (tableName.getAlias() != null)
                    ? idfac.createRelationID(null, tableName.getAlias().getName())
                    : relation.getID();

            OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
            List<Term> terms = new ArrayList<>(relation.getAttributes().size());
            ImmutableMap.Builder attributes = ImmutableMap.<QuotedID, Variable>builder();
            // the order in the loop is important
            relation.getAttributes().forEach(attribute -> {
                QuotedID attributeId = attribute.getID();
                Variable var = fac.getVariable(attributeId.getName() + relationIndex);
                terms.add(var);
                attributes.put(attributeId, var);
            });
            // Create an atom for a particular table
            Function atom = Relation2DatalogPredicate.getAtom(relation, terms);

            result = RelationalExpression.create(ImmutableList.of(atom), attributes.build(), aliasId);
        }


        @Override
        public void visit(SubSelect subSelect) {
            // TODO: implementation

            // use RelationalExpression.alias at the end
        }

        @Override
        public void visit(SubJoin subjoin) {
            RelationalExpression left = getRelationalExpression(subjoin.getLeft());
            RelationalExpression join = join(left, subjoin.getJoin());

            if (subjoin.getAlias() == null || subjoin.getAlias().getName() == null)
                throw new InvalidSelectQueryException("SUBJOIN must have an alias", subjoin);

            RelationID aliasId = idfac.createRelationID(null, subjoin.getAlias().getName());

            result = RelationalExpression.alias(join, aliasId);
        }

        @Override
        public void visit(LateralSubSelect lateralSubSelect) {
            throw new UnsupportedSelectQueryException("LateralSubSelects are not supported", lateralSubSelect);
        }

        @Override
        public void visit(ValuesList valuesList) {
            throw new UnsupportedSelectQueryException("ValuesLists are not supported", valuesList);
        }
    }


}
