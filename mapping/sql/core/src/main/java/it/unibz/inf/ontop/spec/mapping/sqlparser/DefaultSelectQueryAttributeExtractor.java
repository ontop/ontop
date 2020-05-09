package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Roman Kontchakov on 25/01/2017.
 */

public class DefaultSelectQueryAttributeExtractor {
    private final MetadataLookup metadata;
    private final QuotedIDFactory idfac;
    private final CoreSingletons coreSingletons;

    private int relationIndex = 0;
    private final TermFactory termFactory;

    public DefaultSelectQueryAttributeExtractor(MetadataLookup metadata, CoreSingletons coreSingletons) {
        this.metadata = metadata;
        this.idfac = metadata.getQuotedIDFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.coreSingletons = coreSingletons;
    }

    public RAExpressionAttributes getRAExpressionAttributes(SelectBody selectBody) throws InvalidSelectQueryException, UnsupportedSelectQueryException {
        try {
            PlainSelect plainSelect = JSqlParserTools.getPlainSelect(selectBody);
            return select(plainSelect);
        }
        catch (InvalidSelectQueryRuntimeException e) {
            throw new InvalidSelectQueryException(e.getMessage(), e.getObject());
        }
        catch (UnsupportedSelectQueryRuntimeException e) {
            throw new UnsupportedSelectQueryException(e.getMessage(), e.getObject());
        }
    }

    private ImmutableMap<QualifiedAttributeID, ImmutableTerm> getQueryBodyAttributes(PlainSelect plainSelect)  {

        if (plainSelect.getFromItem() == null)
            return ImmutableMap.of();

        RAExpressionAttributes current = getRelationalExpression(plainSelect.getFromItem());
        if (plainSelect.getJoins() != null)
            for (Join join : plainSelect.getJoins())
                try {
                    current = join(current, join);
                }
                catch (IllegalJoinException e) {
                    throw new InvalidSelectQueryRuntimeException(e.toString(), join);
                }
        return current.getAttributes();
    }



    private RAExpressionAttributes select(PlainSelect plainSelect) {

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> currentAttributes = getQueryBodyAttributes(plainSelect);

        ExpressionParser ep = new ExpressionParser(idfac, coreSingletons);
        SelectItemParser sip = new SelectItemParser(currentAttributes,
                (e, a) -> (e instanceof Column)
                        ? ep.parseTerm(e, a)
                        : createVariable(idfac.createAttributeID("something")), idfac);

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;
        try {
            attributes = plainSelect.getSelectItems().stream()
                    .map(si -> sip.getAttributes(si).entrySet())
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toMap());
        }
        catch (IllegalArgumentException e) {
            Map<QualifiedAttributeID, Integer> duplicates = new HashMap<>();
            plainSelect.getSelectItems().stream()
                    .map(si -> sip.getAttributes(si).entrySet())
                    .flatMap(Collection::stream)
                    .forEach(a -> duplicates.put(a.getKey(), duplicates.getOrDefault(a.getKey(), 0) + 1));

            throw new InvalidSelectQueryRuntimeException(duplicates.entrySet().stream()
                    .filter(d -> d.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .map(QualifiedAttributeID::getSQLRendering)
                    .collect(Collectors.joining(", ",
                            "Duplicate column names ",
                            " in the SELECT clause: ")),  plainSelect);
        }

        return new RAExpressionAttributes(attributes);
    }

    private RAExpressionAttributes join(RAExpressionAttributes left, Join join) throws IllegalJoinException {

        // TODO: check
        //if (join.isFull() || join.isRight() || join.isLeft() || join.isOuter())
        //    throw new UnsupportedSelectQueryRuntimeException("LEFT/RIGHT/FULL OUTER JOINs are not supported", join);

        RAExpressionAttributes right = getRelationalExpression(join.getRightItem());
        if (join.isSimple()) {
            return RAExpressionAttributes.crossJoin(left, right);
        }
        else if (join.isCross()) {
            if (join.getOnExpression() != null || join.getUsingColumns() != null)
                throw new InvalidSelectQueryRuntimeException("CROSS JOIN cannot have USING/ON conditions", join);

            if (join.isInner())
                throw new InvalidSelectQueryRuntimeException("CROSS INNER JOIN is not allowed", join);

            return RAExpressionAttributes.crossJoin(left, right);
        }
        else if (join.isNatural()) {
            if (join.getOnExpression() != null || join.getUsingColumns() != null)
                throw new InvalidSelectQueryRuntimeException("NATURAL JOIN cannot have USING/ON conditions", join);

            if (join.isInner())
                throw new InvalidSelectQueryRuntimeException("NATURAL INNER JOIN is not allowed", join);

            return RAExpressionAttributes.joinUsing(left, right, RAExpressionAttributes.getShared(left, right));
        }
        else {
            if (join.getOnExpression() != null) {
                if (join.getUsingColumns() !=null)
                    throw new InvalidSelectQueryRuntimeException("JOIN cannot have both USING and ON", join);

                return RAExpressionAttributes.crossJoin(left, right);
            }
            else if (join.getUsingColumns() != null) {
                return RAExpressionAttributes.joinUsing(left, right,
                        join.getUsingColumns().stream()
                                .map(c -> idfac.createAttributeID(c.getColumnName()))
                                .collect(ImmutableCollectors.toSet()));
            }
            else
                throw new InvalidSelectQueryRuntimeException("[INNER] JOIN requires either ON or USING", join);
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
            DatabaseRelationDefinition relation;
            try {
                relation = metadata.getRelation(id);
            }
            catch (MetadataExtractionException e) {
                throw  new InvalidSelectQueryRuntimeException(e.getMessage(), id);
            }
            relationIndex++;

            ImmutableMap<QuotedID, ImmutableTerm> attributes = relation.getAttributes().stream()
                    .collect(ImmutableCollectors.toMap(Attribute::getID,
                            attribute -> createVariable(attribute.getID())));

            ImmutableSet<RelationID> relationIDs = (tableName.getAlias() == null)
                    ? relation.getAllIDs()
                    : ImmutableSet.of(idfac.createRelationID(null, tableName.getAlias().getName()));

            result = RAExpressionAttributes.create(attributes, relationIDs);
        }


        @Override
        public void visit(SubSelect subSelect) {
            if (subSelect.getAlias() == null || subSelect.getAlias().getName() == null)
                throw new InvalidSelectQueryRuntimeException("SUB-SELECT must have an alias", subSelect);
            relationIndex++;

            SelectBody selectBody = subSelect.getSelectBody();
            if (!(selectBody instanceof PlainSelect))
                throw new UnsupportedSelectQueryRuntimeException("Complex SELECT statements are not supported", selectBody);

            RAExpressionAttributes current = select((PlainSelect) selectBody);

            RelationID aliasId = idfac.createRelationID(null, subSelect.getAlias().getName());
            result = RAExpressionAttributes.create(current.getUnqualifiedAttributes(), ImmutableSet.of(aliasId));
        }

        @Override
        public void visit(SubJoin subjoin) {
            if (subjoin.getAlias() == null || subjoin.getAlias().getName() == null)
                throw new InvalidSelectQueryRuntimeException("SUB-JOIN must have an alias", subjoin);

            RAExpressionAttributes join = getRelationalExpression(subjoin.getLeft());
            try {
                for (Join j : subjoin.getJoinList())
                    join = join(join, j);
            }
            catch (IllegalJoinException e) {
                throw new InvalidSelectQueryRuntimeException(e.toString(), subjoin);
            }

            RelationID aliasId = idfac.createRelationID(null, subjoin.getAlias().getName());
            result = RAExpressionAttributes.create(join.getUnqualifiedAttributes(), ImmutableSet.of(aliasId));
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


    private Variable createVariable(QuotedID id) {
        return termFactory.getVariable(id.getName() + relationIndex);
    }
}
