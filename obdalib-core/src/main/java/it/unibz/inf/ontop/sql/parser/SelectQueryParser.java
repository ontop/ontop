package it.unibz.inf.ontop.sql.parser;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.impl.*;
import it.unibz.inf.ontop.parser.*;
import it.unibz.inf.ontop.sql.*;
import it.unibz.inf.ontop.sql.parser.exceptions.IllegalJoinException;
import it.unibz.inf.ontop.sql.parser.exceptions.InvalidSelectQueryException;
import it.unibz.inf.ontop.sql.parser.exceptions.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 */
public class SelectQueryParser {
    private final DBMetadata metadata;
    private final QuotedIDFactory idfac;

    private int relationIndex = 0;

    public SelectQueryParser(DBMetadata metadata) {
        this.metadata = metadata;
        this.idfac = metadata.getQuotedIDFactory();
    }

    public RelationalExpression parse(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select))
                throw new InvalidSelectQueryException("The query is not a SELECT statement", statement);

            RelationalExpression re = select(((Select) statement).getSelectBody());
            return re;
        }
        catch (JSQLParserException e) {
            throw new UnsupportedSelectQueryException("Cannot parse SQL: " + sql, e);
        }
    }

    private RelationalExpression select(SelectBody selectBody) {

        if (!(selectBody instanceof PlainSelect))
            throw new UnsupportedSelectQueryException("Complex SELECT statements are not supported", selectBody);

        PlainSelect plainSelect = (PlainSelect) selectBody;

        if (plainSelect.getDistinct() != null)
            throw new UnsupportedSelectQueryException("DISTINCT is not supported", selectBody);

        if (plainSelect.getGroupByColumnReferences() != null || plainSelect.getHaving() != null)
            throw new UnsupportedSelectQueryException("GROUP BY / HAVING are not supported", selectBody);

        if (plainSelect.getLimit() != null || plainSelect.getTop() != null)
            throw new UnsupportedSelectQueryException("LIMIT / TOP are not supported", selectBody);

        if (plainSelect.getOrderByElements() != null)
            throw new UnsupportedSelectQueryException("ORDER BY is not supported", selectBody);

        if (plainSelect.getOracleHierarchical() != null || plainSelect.isOracleSiblings())
            throw new UnsupportedSelectQueryException("Oracle START WITH ... CONNECT BY / ORDER SIBLINGS BY are not supported", selectBody);

        if (plainSelect.getIntoTables() != null)
            throw new InvalidSelectQueryException("SELECT INTO is not allowed in mappings", selectBody);

        if (plainSelect.getFromItem() == null)
            throw new UnsupportedSelectQueryException("SELECT without FROM is not supported", selectBody);

        RelationalExpression current = getRelationalExpression(plainSelect.getFromItem());
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins())
                try {
                    current = join(current, join);
                }
                catch (IllegalJoinException e) {
                    throw new InvalidSelectQueryException(e.toString(), plainSelect);
                }
        }

        ImmutableList<Function> filterAtoms = (plainSelect.getWhere() == null)
                ? current.getFilterAtoms()
                : ImmutableList.<Function>builder()
                    .addAll(current.getFilterAtoms())
                    .addAll(new BooleanExpressionParser(idfac, plainSelect.getWhere()).apply(current.getAttributes()))
                    .build();

        ImmutableList.Builder<Function> assignmentsBuilder = ImmutableList.builder();
        ImmutableMap.Builder<QualifiedAttributeID, Variable> attributesBuilder = ImmutableMap.builder();
        SelectItemProcessor sip = new SelectItemProcessor(current.getAttributes());

        plainSelect.getSelectItems().forEach(si -> {
            ImmutableMap<QualifiedAttributeID, Variable> attrs = sip.getAttributes(si);

            // attributesBuilder.build() below checks that the keys in attrs do not intersect
            attributesBuilder.putAll(attrs);

            if (sip.assignment != null)
                assignmentsBuilder.add(sip.assignment);
        });

        ImmutableMap<QualifiedAttributeID, Variable> attributes;
        try {
            attributes = attributesBuilder.build();
        }
        catch (IllegalArgumentException e) {
            SelectItemProcessor sip2 = new SelectItemProcessor(current.getAttributes());
            Map<QualifiedAttributeID, Integer> duplicates = new HashMap<>();
            plainSelect.getSelectItems().forEach(si -> {
                ImmutableMap<QualifiedAttributeID, Variable> attrs = sip2.getAttributes(si);
                for (Map.Entry<QualifiedAttributeID, Variable> a : attrs.entrySet())
                    duplicates.put(a.getKey(), duplicates.getOrDefault(a.getKey(), 0) + 1);
            });
            throw new InvalidSelectQueryException("Duplicate column names " + Joiner.on(", ").join(
                    duplicates.entrySet().stream()
                            .filter(d -> d.getValue() > 1)
                            .map(d -> d.getKey())
                            .collect(ImmutableCollectors.toList())) + " in the SELECT clause: ", selectBody);
        }

        return new RelationalExpression(current.getDataAtoms(),
                ImmutableList.<Function>builder().addAll(filterAtoms).addAll(assignmentsBuilder.build()).build(),
                attributes, null);
    }

    private RelationalExpression join(RelationalExpression left, Join join) throws IllegalJoinException {

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
            // construct the predicate using the table name
            DatabaseRelationDefinition relation = metadata.getDatabaseRelation(id);
            if (relation == null)
                throw new InvalidSelectQueryException("Table " + id + " not found in metadata", tableName);
            relationIndex++;

            RelationID alias = (tableName.getAlias() != null)
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
            // create an atom for a particular table
            Function atom = Relation2DatalogPredicate.getAtom(relation, terms);

            // DEFAULT SCHEMA
            // TODO: to be improved
            if ((tableName.getAlias() == null) &&
                    relation.getID().getSchemaName() != null &&
                    metadata.getDatabaseRelation(relation.getID().getSchemalessID()).equals(relation))
                result = RelationalExpression.create(ImmutableList.of(atom), ImmutableList.of(), attributes.build(), alias, relation.getID().getSchemalessID());
            else
                result = RelationalExpression.create(ImmutableList.of(atom), ImmutableList.of(), attributes.build(), alias);
        }


        @Override
        public void visit(SubSelect subSelect) {
            if (subSelect.getAlias() == null || subSelect.getAlias().getName() == null)
                throw new InvalidSelectQueryException("SUB-SELECT must have an alias", subSelect);

            RelationalExpression current = select(subSelect.getSelectBody());

            RelationID aliasId = idfac.createRelationID(null, subSelect.getAlias().getName());
            result = RelationalExpression.alias(current, aliasId);
        }

        @Override
        public void visit(SubJoin subjoin) {
            if (subjoin.getAlias() == null || subjoin.getAlias().getName() == null)
                throw new InvalidSelectQueryException("SUB-JOIN must have an alias", subjoin);

            RelationalExpression left = getRelationalExpression(subjoin.getLeft());
            RelationalExpression join;
            try {
                join = join(left, subjoin.getJoin());
            }
            catch (IllegalJoinException e) {
                throw new InvalidSelectQueryException(e.toString(), subjoin);
            }

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

    private class SelectItemProcessor implements SelectItemVisitor {
        final ImmutableMap<QualifiedAttributeID, Variable> attributes;

        ImmutableMap<QualifiedAttributeID, Variable> map;
        Function assignment;

        SelectItemProcessor(ImmutableMap<QualifiedAttributeID, Variable> attributes) {
            this.attributes = attributes;
        }

        ImmutableMap<QualifiedAttributeID, Variable> getAttributes(SelectItem si) {
            assignment = null;
            si.accept(this);
            return map;
        }

        @Override
        public void visit(AllColumns allColumns) {
            map = attributes.entrySet().stream()
                    .filter(e -> e.getKey().getRelation() == null)
                    .collect(ImmutableCollectors.toMap());
        }

        @Override
        public void visit(AllTableColumns allTableColumns) {
            Table table = allTableColumns.getTable();
            RelationID id = idfac.createRelationID(table.getSchemaName(), table.getName());

            map = attributes.entrySet().stream()
                    .filter(e -> e.getKey().getRelation() != null && e.getKey().getRelation().equals(id))
                    .collect(ImmutableCollectors.toMap(
                            e -> new QualifiedAttributeID(null, e.getKey().getAttribute()),
                            Map.Entry::getValue));
        }

        @Override
        public void visit(SelectExpressionItem selectExpressionItem) {
            Expression expr = selectExpressionItem.getExpression();
            if (expr instanceof Column) {
                Column column = (Column) expr;
                QuotedID id = idfac.createAttributeID(column.getColumnName());
                Table table = column.getTable();
                QualifiedAttributeID attr = (table == null || table.getName() == null)
                        ? new QualifiedAttributeID(null, id)
                        : new QualifiedAttributeID(idfac.createRelationID(table.getSchemaName(), table.getName()), id);

                //System.out.println("" + attr + " in " + attributes);
                Variable var = attributes.get(attr);
                if (var != null) {
                    Alias columnAlias = selectExpressionItem.getAlias();
                    QuotedID name = (columnAlias == null || columnAlias.getName() == null)
                            ? id
                            : idfac.createAttributeID(columnAlias.getName());

                    map = ImmutableMap.of(new QualifiedAttributeID(null, name), var);
                }
                else
                    throw new InvalidSelectQueryException("Column not found", selectExpressionItem);
            }
            else {
                //throw new UnsupportedSelectQueryException("Complex expressions in SELECT", selectExpressionItem);
                Alias columnAlias = selectExpressionItem.getAlias();
                if (columnAlias == null || columnAlias.getName() == null)
                    throw new InvalidSelectQueryException("Complex expression in SELECT must have an alias", selectExpressionItem);

                OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

                QuotedID name = idfac.createAttributeID(columnAlias.getName());
                Variable var = fac.getVariable(columnAlias.getName() + relationIndex);
                map = ImmutableMap.of(new QualifiedAttributeID(null, name), var);

                Term term = new ExpressionParser(idfac, expr).apply(attributes);
                assignment = fac.getFunctionEQ(var, term);
            }

        }
    }
}
