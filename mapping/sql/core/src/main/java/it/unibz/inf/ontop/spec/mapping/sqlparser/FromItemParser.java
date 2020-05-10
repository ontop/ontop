package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.IllegalJoinException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.UnsupportedSelectQueryRuntimeException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;

public abstract class FromItemParser<T extends RAEntity<T>> {

    protected final ExpressionParser expressionParser;
    protected final QuotedIDFactory idfac;
    protected final TermFactory termFactory;

    private final MetadataLookup metadata;

    private int relationIndex = 0;

    protected abstract T create(RelationDefinition relation, ImmutableSet<RelationID> relationIds);

    protected abstract T translateSelectBody(SelectBody selectBody);

    protected FromItemParser(ExpressionParser expressionParser, QuotedIDFactory idfac, MetadataLookup metadata, TermFactory termFactory) {
        this.expressionParser = expressionParser;
        this.idfac = idfac;
        this.metadata = metadata;
        this.termFactory = termFactory;
    }

    /**
     * main method for analysing FROM clauses
     *
     * @param left
     * @param joins
     * @return
     * @throws IllegalJoinException
     */
    protected T translateJoins(FromItem left, List<Join> joins) throws IllegalJoinException {
        T current = translateFromItem(left);
        if (joins != null)
            for (Join join : joins) // no reduce - exception handling
                current = join(current, join);

        return current;
    }

    private T translateFromItem(FromItem fromItem) {
        return new FromItemProcessor().translate(fromItem);
    }

    /**
     * can be overridden to add additional checks
     *
     * @param left
     * @param join
     * @return
     * @throws IllegalJoinException
     */
    protected T join(T left, Join join) throws IllegalJoinException {

        T right = translateFromItem(join.getRightItem());
        if (join.isSimple()) {
            return left.crossJoin(right);
        }
        else if (join.isCross()) {
            if (join.getOnExpression() != null || join.getUsingColumns() != null)
                throw new InvalidSelectQueryRuntimeException("CROSS JOIN cannot have USING/ON conditions", join);

            if (join.isInner())
                throw new InvalidSelectQueryRuntimeException("CROSS INNER JOIN is not allowed", join);

            return left.crossJoin(right);
        }
        else if (join.isNatural()) {
            if (join.getOnExpression() != null || join.getUsingColumns() != null)
                throw new InvalidSelectQueryRuntimeException("NATURAL JOIN cannot have USING/ON conditions", join);

            if (join.isInner())
                throw new InvalidSelectQueryRuntimeException("NATURAL INNER JOIN is not allowed", join);

            return left.joinUsing(right, left.getSharedAttributeNames(right));
        }
        else {
            if (join.getOnExpression() != null) {
                if (join.getUsingColumns() !=null)
                    throw new InvalidSelectQueryRuntimeException("JOIN cannot have both USING and ON", join);

                return left.joinOn(right,
                        (attributes -> expressionParser.parseBooleanExpression(
                                join.getOnExpression(), attributes)));
            }
            else if (join.getUsingColumns() != null) {
                if (join.getUsingColumns().stream().anyMatch(p -> p.getTable() != null))
                    throw new InvalidSelectQueryRuntimeException("JOIN USING columns cannot be qualified", join);

                return left.joinUsing(right,
                        join.getUsingColumns().stream()
                                .map(p -> idfac.createAttributeID(p.getColumnName()))
                                .collect(ImmutableCollectors.toSet()));
            }
            else
                throw new InvalidSelectQueryRuntimeException("[INNER|OUTER] JOIN requires either ON or USING", join);
        }
    }

    protected RAExpressionAttributes createRAExpressionAttributes(RelationDefinition relation, ImmutableSet<RelationID> relationIds) {

        relationIndex++;
        ImmutableMap<QuotedID, ImmutableTerm> attributes = relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(Attribute::getID,
                        attribute -> termFactory.getVariable(attribute.getID().getName() + relationIndex)));

        return RAExpressionAttributes.create(attributes, relationIds);
    }


    private class FromItemProcessor implements FromItemVisitor {

        private T result = null;

        T translate(FromItem fromItem) {
            fromItem.accept(this);
            return result;
        }

        @Override
        public void visit(Table tableName) {

            RelationID id = idfac.createRelationID(tableName.getSchemaName(), tableName.getName());
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
            result = current.withAlias(aliasId);
        }

        @Override
        public void visit(SubJoin subjoin) {
            if (subjoin.getAlias() == null || subjoin.getAlias().getName() == null)
                throw new InvalidSelectQueryRuntimeException("SUB-JOIN must have an alias", subjoin);

            try {
                T join = translateJoins(subjoin.getLeft(), subjoin.getJoinList());
                RelationID aliasId = idfac.createRelationID(null, subjoin.getAlias().getName());
                result = join.withAlias(aliasId);
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
