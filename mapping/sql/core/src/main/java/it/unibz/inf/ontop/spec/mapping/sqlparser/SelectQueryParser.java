package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 */
public class SelectQueryParser {
    private final MetadataLookup metadata;
    private final QuotedIDFactory idfac;
    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final ExpressionParser expressionParser;

    private int relationIndex = 0;

    public SelectQueryParser(MetadataLookup metadata, CoreSingletons coreSingletons) {
        this.metadata = metadata;
        this.idfac = metadata.getQuotedIDFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.expressionParser = new ExpressionParser(idfac, coreSingletons);
    }

    public RAExpression parse(String sql) throws InvalidSelectQueryException, UnsupportedSelectQueryException {
        try {
            return select(JSqlParserTools.parse(sql));
        }
        catch (InvalidSelectQueryRuntimeException e) {
            throw new InvalidSelectQueryException(e.getMessage(), e.getObject());
        }
        catch (UnsupportedSelectQueryRuntimeException e) {
            throw new UnsupportedSelectQueryException(e.getMessage(), e.getObject());
        }
    }


    private RAExpression select(SelectBody selectBody) {
        return plainSelect(JSqlParserTools.getPlainSelect(selectBody));
    }

    private RAExpression plainSelect(PlainSelect plainSelect) {

        if (plainSelect.getDistinct() != null)
            throw new UnsupportedSelectQueryRuntimeException("DISTINCT is not supported", plainSelect);

        if (plainSelect.getGroupBy() != null || plainSelect.getHaving() != null)
            throw new UnsupportedSelectQueryRuntimeException("GROUP BY / HAVING are not supported", plainSelect);

        if (plainSelect.getLimit() != null || plainSelect.getTop() != null || plainSelect.getOffset()!= null)
            throw new UnsupportedSelectQueryRuntimeException("LIMIT / OFFSET / TOP are not supported", plainSelect);

        if (plainSelect.getOrderByElements() != null)
            throw new UnsupportedSelectQueryRuntimeException("ORDER BY is not supported", plainSelect);

        if (plainSelect.getOracleHierarchical() != null || plainSelect.isOracleSiblings())
            throw new UnsupportedSelectQueryRuntimeException("Oracle START WITH ... CONNECT BY / ORDER SIBLINGS BY are not supported", plainSelect);

        if (plainSelect.getFromItem() == null)
            throw new UnsupportedSelectQueryRuntimeException("SELECT without FROM is not supported", plainSelect);

        RAExpression current = getRelationalExpression(plainSelect.getFromItem());
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins())
                try {
                    current = join(current, join);
                }
                catch (IllegalJoinException e) {
                    throw new InvalidSelectQueryRuntimeException(e.toString(), plainSelect);
                }
        }

        ImmutableList<ImmutableExpression> filterAtoms = (plainSelect.getWhere() == null)
                ? current.getFilterAtoms()
                : ImmutableList.<ImmutableExpression>builder()
                .addAll(current.getFilterAtoms())
                .addAll(expressionParser.parseBooleanExpression(
                            plainSelect.getWhere(), current.getAttributes()))
                .build();

        ImmutableMap.Builder<QualifiedAttributeID, ImmutableTerm> attributesBuilder = ImmutableMap.builder();
        SelectItemProcessor sip = new SelectItemProcessor(current.getAttributes());

        plainSelect.getSelectItems().forEach(si -> {
            ImmutableMap<QualifiedAttributeID, ImmutableTerm> attrs = sip.getAttributes(si);

            // attributesBuilder.build() below checks that the keys in attrs do not intersect
            attributesBuilder.putAll(attrs);
        });

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;
        try {
            attributes = attributesBuilder.build();
        }
        catch (IllegalArgumentException e) {
            SelectItemProcessor sip2 = new SelectItemProcessor(current.getAttributes());
            Map<QualifiedAttributeID, Integer> duplicates = new HashMap<>();
            plainSelect.getSelectItems().forEach(si -> {
                ImmutableMap<QualifiedAttributeID, ImmutableTerm> attrs = sip2.getAttributes(si);
                for (Map.Entry<QualifiedAttributeID, ImmutableTerm> a : attrs.entrySet())
                    duplicates.put(a.getKey(), duplicates.getOrDefault(a.getKey(), 0) + 1);
            });
            throw new InvalidSelectQueryRuntimeException(duplicates.entrySet().stream()
                    .filter(d -> d.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .map(QualifiedAttributeID::getSQLRendering)
                    .collect(Collectors.joining(", ", "Duplicate column names ", " in the SELECT clause: ")), plainSelect);
        }

        return new RAExpression(current.getDataAtoms(),
                ImmutableList.<ImmutableExpression>builder().addAll(filterAtoms).build(),
                new RAExpressionAttributes(attributes));
    }

    private RAExpression join(RAExpression left, Join join) throws IllegalJoinException {

        if (join.isFull() || join.isRight() || join.isLeft() || join.isOuter())
            throw new UnsupportedSelectQueryRuntimeException("LEFT/RIGHT/FULL OUTER JOINs are not supported", join);

        RAExpression right = getRelationalExpression(join.getRightItem());
        if (join.isSimple()) {
            return RAExpression.crossJoin(left, right);
        }
        else if (join.isCross()) {
            if (join.getOnExpression() != null || join.getUsingColumns() != null)
                throw new InvalidSelectQueryRuntimeException("CROSS JOIN cannot have USING/ON conditions", join);

            if (join.isInner())
                throw new InvalidSelectQueryRuntimeException("CROSS INNER JOIN is not allowed", join);

            return RAExpression.crossJoin(left, right);
        }
        else if (join.isNatural()) {
            if (join.getOnExpression() != null || join.getUsingColumns() != null)
                throw new InvalidSelectQueryRuntimeException("NATURAL JOIN cannot have USING/ON conditions", join);

            if (join.isInner())
                throw new InvalidSelectQueryRuntimeException("NATURAL INNER JOIN is not allowed", join);

            return RAExpression.naturalJoin(left, right, termFactory);
        }
        else {
            if (join.getOnExpression() != null) {
                if (join.getUsingColumns() !=null)
                    throw new InvalidSelectQueryRuntimeException("JOIN cannot have both USING and ON", join);

                return RAExpression.joinOn(left, right,
                        (attributes -> expressionParser.parseBooleanExpression(
                                join.getOnExpression(), attributes)));
            }
            else if (join.getUsingColumns() != null) {
                return RAExpression.joinUsing(left, right,
                        join.getUsingColumns().stream()
                                .map(p -> idfac.createAttributeID(p.getColumnName()))
                                .collect(ImmutableCollectors.toSet()), termFactory);
            }
            else
                throw new InvalidSelectQueryRuntimeException("[INNER] JOIN requires either ON or USING", join);
        }
    }


    private RAExpression getRelationalExpression(FromItem fromItem) {
        return new FromItemProcessor(fromItem).result;
    }

    public RAExpression createAtom(RelationDefinition relation, ImmutableSet<RelationID> relationIDs) {
        ImmutableMap<Integer, Variable> terms = relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(a -> a.getIndex() - 1,
                        a -> termFactory.getVariable(a.getID().getName() + relationIndex)));

        ExtensionalDataNode atom = iqFactory.createExtensionalDataNode(relation, terms);

        ImmutableMap<QuotedID, ImmutableTerm> attributes = relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(
                        Attribute::getID,
                        a -> terms.get(a.getIndex() - 1)));

        RAExpressionAttributes attrs = RAExpressionAttributes.create(attributes, relationIDs);

        return new RAExpression(ImmutableList.of(atom), ImmutableList.of(), attrs);
    }


    private class FromItemProcessor implements FromItemVisitor {

        private RAExpression result = null;

        FromItemProcessor(FromItem fromItem) {
            fromItem.accept(this);
        }

        @Override
        public void visit(Table tableName) {

            RelationID id = idfac.createRelationID(tableName.getSchemaName(), tableName.getName());
            // construct the predicate using the table name
            try {
                DatabaseRelationDefinition relation = metadata.getRelation(id);
                relationIndex++;

                ImmutableSet<RelationID> relationIDs = (tableName.getAlias() == null)
                        ? relation.getAllIDs()
                        : ImmutableSet.of(idfac.createRelationID(null, tableName.getAlias().getName()));

                result = createAtom(relation, relationIDs);
            }
            catch (MetadataExtractionException e) {
                throw new InvalidSelectQueryRuntimeException(e.getMessage(), id);
            }
        }


        @Override
        public void visit(SubSelect subSelect) {
            if (subSelect.getAlias() == null || subSelect.getAlias().getName() == null)
                throw new InvalidSelectQueryRuntimeException("SUB-SELECT must have an alias", subSelect);

            RAExpression current = select(subSelect.getSelectBody());

            RelationID aliasId = idfac.createRelationID(null, subSelect.getAlias().getName());
            result = RAExpression.alias(current, aliasId);
        }

        @Override
        public void visit(SubJoin subjoin) {
            if (subjoin.getAlias() == null || subjoin.getAlias().getName() == null)
                throw new InvalidSelectQueryRuntimeException("SUB-JOIN must have an alias", subjoin);

            RAExpression join = getRelationalExpression(subjoin.getLeft());
            try {
                for (Join j : subjoin.getJoinList())
                    join = join(join, j);
            }
            catch (IllegalJoinException e) {
                throw new InvalidSelectQueryRuntimeException(e.toString(), subjoin);
            }

            RelationID aliasId = idfac.createRelationID(null, subjoin.getAlias().getName());
            result = RAExpression.alias(join, aliasId);
        }

        @Override
        public void visit(LateralSubSelect lateralSubSelect) {
            throw new UnsupportedSelectQueryRuntimeException("LateralSubSelects are not supported", lateralSubSelect);
        }

        @Override
        public void visit(ValuesList valuesList) {
            throw new UnsupportedSelectQueryRuntimeException("ValuesLists are not supported", valuesList);
        }

        @Override
        public void visit(TableFunction tableFunction) {
            throw new UnsupportedSelectQueryRuntimeException("TableFunction are not supported", tableFunction);
        }

        @Override
        public void visit(ParenthesisFromItem parenthesisFromItem) {
            throw new UnsupportedSelectQueryRuntimeException("ParenthesisFromItem are not supported", parenthesisFromItem);
        }
    }

    private class SelectItemProcessor implements SelectItemVisitor {
        final ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> map;

        SelectItemProcessor(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes) {
            this.attributes = attributes;
        }

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> getAttributes(SelectItem si) {
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

                ImmutableTerm var = attributes.get(attr);
                if (var != null) {
                    Alias columnAlias = selectExpressionItem.getAlias();
                    QuotedID name = (columnAlias == null || columnAlias.getName() == null)
                            ? id
                            : idfac.createAttributeID(columnAlias.getName());

                    map = ImmutableMap.of(new QualifiedAttributeID(null, name), var);
                }
                else
                    throw new InvalidSelectQueryRuntimeException("Column not found", selectExpressionItem);
            }
            else {
                Alias columnAlias = selectExpressionItem.getAlias();
                if (columnAlias == null || columnAlias.getName() == null)
                    throw new InvalidSelectQueryRuntimeException("Complex expression in SELECT must have an alias", selectExpressionItem);

                QuotedID name = idfac.createAttributeID(columnAlias.getName());
                //Variable var = termFactory.getVariable(name.getName() + relationIndex);

                ImmutableTerm term =  expressionParser.parseTerm(expr, attributes);
                map = ImmutableMap.of(new QualifiedAttributeID(null, name), term);
            }
        }
    }
}
