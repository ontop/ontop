package it.unibz.inf.ontop.sql.parser;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.*;
import it.unibz.inf.ontop.sql.parser.exceptions.IllegalJoinException;
import it.unibz.inf.ontop.sql.parser.exceptions.InvalidSelectQueryException;
import it.unibz.inf.ontop.sql.parser.exceptions.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roman Kontchakov on 25/01/2017.
 */
public class SelectQueryAttributeExtractor2 {
    private final DBMetadata metadata;
    private final QuotedIDFactory idfac;

    private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

    private int relationIndex = 0;

    public SelectQueryAttributeExtractor2(DBMetadata metadata) {
        this.metadata = metadata;
        this.idfac = metadata.getQuotedIDFactory();
    }

    public RAExpressionAttributes parse(String sql) {
        PlainSelect plainSelect = getParsedSql(sql);
        return select(plainSelect);
    }

    public PlainSelect getParsedSql(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select))
                throw new InvalidSelectQueryException("The query is not a SELECT statement", statement);

            SelectBody selectBody = ((Select) statement).getSelectBody();
            if (!(selectBody instanceof PlainSelect))
                throw new UnsupportedSelectQueryException("Complex SELECT statements are not supported", selectBody);

            PlainSelect plainSelect = (PlainSelect) selectBody;

            if (plainSelect.getIntoTables() != null)
                throw new InvalidSelectQueryException("SELECT INTO is not allowed in mappings", plainSelect);

            return plainSelect;
        }
        catch (JSQLParserException e) {
            throw new UnsupportedSelectQueryException("Cannot parse SQL: " + sql, e);
        }

    }

    public ImmutableMap<QualifiedAttributeID, Variable> getQueryBodyAttributes(PlainSelect plainSelect) {

        if (plainSelect.getFromItem() == null)
            throw new UnsupportedSelectQueryException("SELECT without FROM is not supported", plainSelect);

        RAExpressionAttributes current = getRelationalExpression(plainSelect.getFromItem());
        if (plainSelect.getJoins() != null)
            for (Join join : plainSelect.getJoins())
                try {
                    current = join(current, join);
                }
                catch (IllegalJoinException e) {
                    throw new InvalidSelectQueryException(e.toString(), join);
                }

        return current.getAttributes();
    }

    private RAExpressionAttributes select(PlainSelect plainSelect) {

        ImmutableMap<QualifiedAttributeID, Variable> currentAttributes = getQueryBodyAttributes(plainSelect);

        ImmutableMap<QualifiedAttributeID, Variable> attributes;
        try {
            attributes = plainSelect.getSelectItems().stream()
                        .map(si -> new SelectItemProcessor(currentAttributes).getAttributes(si).entrySet())
                        .flatMap(Collection::stream)
                        .collect(ImmutableCollectors.toMap());
        }
        catch (IllegalArgumentException e) {
            SelectItemProcessor sip = new SelectItemProcessor(currentAttributes);
            Map<QualifiedAttributeID, Integer> duplicates = new HashMap<>();
            plainSelect.getSelectItems().forEach(si -> {
                ImmutableMap<QualifiedAttributeID, Variable> attrs = sip.getAttributes(si);
                for (Map.Entry<QualifiedAttributeID, Variable> a : attrs.entrySet())
                    duplicates.put(a.getKey(), duplicates.getOrDefault(a.getKey(), 0) + 1);
            });
            throw new InvalidSelectQueryException("Duplicate column names " + Joiner.on(", ").join(
                    duplicates.entrySet().stream()
                            .filter(d -> d.getValue() > 1)
                            .map(d -> d.getKey())
                            .collect(ImmutableCollectors.toList())) + " in the SELECT clause: ", plainSelect);
        }

        return new RAExpressionAttributes(attributes, null);
    }

    private RAExpressionAttributes join(RAExpressionAttributes left, Join join) throws IllegalJoinException {

        if (join.isFull() || join.isRight() || join.isLeft() || join.isOuter())
            throw new UnsupportedSelectQueryException("LEFT/RIGHT/FULL OUTER JOINs are not supported", join);

        RAExpressionAttributes right = getRelationalExpression(join.getRightItem());
        if (join.isSimple()) {
            return RAExpressionAttributes.crossJoin(left, right);
        }
        else if (join.isCross()) {
            if (join.getOnExpression() != null || join.getUsingColumns() != null)
                throw new InvalidSelectQueryException("CROSS JOIN cannot have USING/ON conditions", join);

            if (join.isInner())
                throw new InvalidSelectQueryException("CROSS INNER JOIN is not allowed", join);

            return RAExpressionAttributes.crossJoin(left, right);
        }
        else if (join.isNatural()) {
            if (join.getOnExpression() != null || join.getUsingColumns() != null)
                throw new InvalidSelectQueryException("NATURAL JOIN cannot have USING/ON conditions", join);

            if (join.isInner())
                throw new InvalidSelectQueryException("NATURAL INNER JOIN is not allowed", join);

            return RAExpressionAttributes.joinUsing(left, right, RAExpressionAttributes.getShared(left, right));
        }
        else {
            if (join.getOnExpression() != null) {
                if (join.getUsingColumns() !=null)
                    throw new InvalidSelectQueryException("JOIN cannot have both USING and ON", join);

                return RAExpressionAttributes.crossJoin(left, right);
            }
            else if (join.getUsingColumns() != null) {
                return RAExpressionAttributes.joinUsing(left, right,
                        join.getUsingColumns().stream()
                                .map(c -> idfac.createAttributeID(c.getColumnName()))
                                .collect(ImmutableCollectors.toSet()));
            }
            else
                throw new InvalidSelectQueryException("[INNER] JOIN requires either ON or USING", join);
        }
    }


    private RAExpressionAttributes getRelationalExpression(FromItem fromItem) {
        return new FromItemProcessor(fromItem).result;
    }


    private class FromItemProcessor implements FromItemVisitor {

        private RAExpressionAttributes result = null;

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

            ImmutableMap<QuotedID, Variable> attributes = relation.getAttributes().stream()
                    .collect(ImmutableCollectors.toMap(Attribute::getID,
                            attribute -> createVariable(attribute.getID())));

            // DEFAULT SCHEMA
            // TODO: to be improved
            if ((tableName.getAlias() == null) &&
                    relation.getID().getSchemaName() != null &&
                    metadata.getDatabaseRelation(relation.getID().getSchemalessID()).equals(relation))
                result = RAExpressionAttributes.create(attributes, alias, relation.getID().getSchemalessID());
            else
                result = RAExpressionAttributes.create(attributes, alias);
        }


        @Override
        public void visit(SubSelect subSelect) {
            if (subSelect.getAlias() == null || subSelect.getAlias().getName() == null)
                throw new InvalidSelectQueryException("SUB-SELECT must have an alias", subSelect);
            relationIndex++;


            SelectBody selectBody = subSelect.getSelectBody();
            if (!(selectBody instanceof PlainSelect))
                throw new UnsupportedSelectQueryException("Complex SELECT statements are not supported", selectBody);

            RAExpressionAttributes current = select((PlainSelect) selectBody);

            RelationID aliasId = idfac.createRelationID(null, subSelect.getAlias().getName());
            result = RAExpressionAttributes.alias(current, aliasId);
        }

        @Override
        public void visit(SubJoin subjoin) {
            if (subjoin.getAlias() == null || subjoin.getAlias().getName() == null)
                throw new InvalidSelectQueryException("SUB-JOIN must have an alias", subjoin);

            RAExpressionAttributes left = getRelationalExpression(subjoin.getLeft());
            RAExpressionAttributes join;
            try {
                join = join(left, subjoin.getJoin());
            }
            catch (IllegalJoinException e) {
                throw new InvalidSelectQueryException(e.toString(), subjoin);
            }

            RelationID aliasId = idfac.createRelationID(null, subjoin.getAlias().getName());
            result = RAExpressionAttributes.alias(join, aliasId);
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

        SelectItemProcessor(ImmutableMap<QualifiedAttributeID, Variable> attributes) {
            this.attributes = attributes;
        }

        ImmutableMap<QualifiedAttributeID, Variable> getAttributes(SelectItem si) {
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

                QuotedID name = idfac.createAttributeID(columnAlias.getName());
                map = ImmutableMap.of(new QualifiedAttributeID(null, name), createVariable(name));
            }
        }
    }

    private Variable createVariable(QuotedID id) {
        return fac.getVariable(id.getName() + relationIndex);
    }
}
