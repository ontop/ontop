package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.UnsupportedSelectQueryRuntimeException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;
import java.util.function.Function;

public abstract class FromItemParser<T> {

    protected final ExpressionParser expressionParser;
    protected final QuotedIDFactory idfac;

    private final MetadataLookup metadata;

    protected abstract T crossJoin(T left, T right) throws IllegalJoinException;
    protected abstract T naturalJoin(T left, T right) throws IllegalJoinException;
    protected abstract T joinOn(T left, T right, Function<ImmutableMap<QualifiedAttributeID, ImmutableTerm>, ImmutableList<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException;
    protected abstract T joinUsing(T left, T right, ImmutableSet<QuotedID> using) throws IllegalJoinException;

    protected abstract T create(RelationDefinition relation, ImmutableSet<RelationID> relationIds);
    protected abstract T alias(T t, RelationID relationId);

    protected abstract T translateSelectBody(SelectBody selectBody);

    protected FromItemParser(ExpressionParser expressionParser, QuotedIDFactory idfac, MetadataLookup metadata) {
        this.expressionParser = expressionParser;
        this.idfac = idfac;
        this.metadata = metadata;
    }

    private T translateFromItem(FromItem fromItem) {
        return new FromItemProcessor().translate(fromItem);
    }

    protected T join(T left, Join join) throws IllegalJoinException {

        T right = translateFromItem(join.getRightItem());
        if (join.isSimple()) {
            return crossJoin(left, right);
        }
        else if (join.isCross()) {
            if (join.getOnExpression() != null || join.getUsingColumns() != null)
                throw new InvalidSelectQueryRuntimeException("CROSS JOIN cannot have USING/ON conditions", join);

            if (join.isInner())
                throw new InvalidSelectQueryRuntimeException("CROSS INNER JOIN is not allowed", join);

            return crossJoin(left, right);
        }
        else if (join.isNatural()) {
            if (join.getOnExpression() != null || join.getUsingColumns() != null)
                throw new InvalidSelectQueryRuntimeException("NATURAL JOIN cannot have USING/ON conditions", join);

            if (join.isInner())
                throw new InvalidSelectQueryRuntimeException("NATURAL INNER JOIN is not allowed", join);

            return naturalJoin(left, right);
        }
        else {
            if (join.getOnExpression() != null) {
                if (join.getUsingColumns() !=null)
                    throw new InvalidSelectQueryRuntimeException("JOIN cannot have both USING and ON", join);

                return joinOn(left, right,
                        (attributes -> expressionParser.parseBooleanExpression(
                                join.getOnExpression(), attributes)));
            }
            else if (join.getUsingColumns() != null) {
                if (join.getUsingColumns().stream().anyMatch(p -> p.getTable() != null))
                    throw new InvalidSelectQueryRuntimeException("JOIN USING columns cannot be qualified", join);

                return joinUsing(left, right,
                        join.getUsingColumns().stream()
                                .map(p -> idfac.createAttributeID(p.getColumnName()))
                                .collect(ImmutableCollectors.toSet()));
            }
            else
                throw new InvalidSelectQueryRuntimeException("[INNER] JOIN requires either ON or USING", join);
        }
    }

    protected T translateJoins(FromItem left, List<Join> joins) throws IllegalJoinException {
        T current = translateFromItem(left);
        if (joins != null)
            for (Join join : joins)
                    current = join(current, join);

        return current;
    }

    protected class FromItemProcessor implements FromItemVisitor {

        private T result = null;

        T translate(FromItem fromItem) {
            fromItem.accept(this);
            return result;
        }

        @Override
        public void visit(Table tableName) {

            RelationID id = idfac.createRelationID(tableName.getSchemaName(), tableName.getName());
            // construct the predicate using the table name
            try {
                DatabaseRelationDefinition relation = metadata.getRelation(id);

                ImmutableSet<RelationID> relationIDs = (tableName.getAlias() == null)
                        ? relation.getAllIDs()
                        : ImmutableSet.of(idfac.createRelationID(null, tableName.getAlias().getName()));

                result = create(relation, relationIDs);
            }
            catch (MetadataExtractionException e) {
                throw new InvalidSelectQueryRuntimeException(e.getMessage(), id);
            }
        }


        @Override
        public void visit(SubSelect subSelect) {
            if (subSelect.getAlias() == null || subSelect.getAlias().getName() == null)
                throw new InvalidSelectQueryRuntimeException("SUB-SELECT must have an alias", subSelect);

            T current = translateSelectBody(subSelect.getSelectBody());

            RelationID aliasId = idfac.createRelationID(null, subSelect.getAlias().getName());
            result = alias(current, aliasId);
        }

        @Override
        public void visit(SubJoin subjoin) {
            if (subjoin.getAlias() == null || subjoin.getAlias().getName() == null)
                throw new InvalidSelectQueryRuntimeException("SUB-JOIN must have an alias", subjoin);

            try {
                T join = translateJoins(subjoin.getLeft(), subjoin.getJoinList());
                RelationID aliasId = idfac.createRelationID(null, subjoin.getAlias().getName());
                result = alias(join, aliasId);
            }
            catch (IllegalJoinException e) {
                throw new InvalidSelectQueryRuntimeException(e.toString(), subjoin);
            }
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

}
