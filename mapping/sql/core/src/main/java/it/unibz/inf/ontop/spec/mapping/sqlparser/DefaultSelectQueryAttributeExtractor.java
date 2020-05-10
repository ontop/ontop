package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

import java.util.function.Function;

/**
 * Created by Roman Kontchakov on 25/01/2017.
 */

public class DefaultSelectQueryAttributeExtractor extends FromItemParser<RAExpressionAttributes> {
    private final QuotedIDFactory idfac;
    private final TermFactory termFactory;
    private final CoreSingletons coreSingletons;

    private int relationIndex = 0;

    public DefaultSelectQueryAttributeExtractor(MetadataLookup metadata, CoreSingletons coreSingletons) {
        super(new ExpressionParser(metadata.getQuotedIDFactory(), coreSingletons), metadata.getQuotedIDFactory(), metadata);
        this.idfac = metadata.getQuotedIDFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.coreSingletons = coreSingletons;
    }

    public RAExpressionAttributes getRAExpressionAttributes(SelectBody selectBody) throws InvalidSelectQueryException, UnsupportedSelectQueryException {
        try {
            return translateSelectBody(selectBody);
        }
        catch (InvalidSelectQueryRuntimeException e) {
            throw new InvalidSelectQueryException(e.getMessage(), e.getObject());
        }
        catch (UnsupportedSelectQueryRuntimeException e) {
            throw new UnsupportedSelectQueryException(e.getMessage(), e.getObject());
        }
    }


    @Override
    protected RAExpressionAttributes translateSelectBody(SelectBody selectBody) {
        PlainSelect plainSelect = JSqlParserTools.getPlainSelect(selectBody);

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributeMap;
        try {
            if (plainSelect.getFromItem() != null) {
                RAExpressionAttributes current = translateJoins(plainSelect.getFromItem(), plainSelect.getJoins());
                attributeMap = current.getAttributes();
            }
            else
                attributeMap = ImmutableMap.of();
        }
        catch (IllegalJoinException e) {
            throw new InvalidSelectQueryRuntimeException(e.toString(), plainSelect);
        }

        ExpressionParser ep = new ExpressionParser(idfac, coreSingletons);
        SelectItemParser sip = new SelectItemParser(attributeMap,
                (e, a) -> (e instanceof Column)
                        ? ep.parseTerm(e, a)
                        : termFactory.getVariable("something"), idfac);

        return JSqlParserTools.parseSelectItems(sip, plainSelect.getSelectItems());
    }

    @Override
    protected RAExpressionAttributes crossJoin(RAExpressionAttributes left, RAExpressionAttributes right) throws IllegalJoinException {
        return RAExpressionAttributes.crossJoin(left, right);
    }

    @Override
    protected ImmutableSet<QuotedID> getShared(RAExpressionAttributes left, RAExpressionAttributes right)  {
        return RAExpressionAttributes.getShared(left, right);
    }

    @Override
    protected RAExpressionAttributes joinOn(RAExpressionAttributes left, RAExpressionAttributes right, Function<ImmutableMap<QualifiedAttributeID, ImmutableTerm>, ImmutableList<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException {
        return RAExpressionAttributes.crossJoin(left, right);
    }

    @Override
    protected RAExpressionAttributes joinUsing(RAExpressionAttributes left, RAExpressionAttributes right, ImmutableSet<QuotedID> using) throws IllegalJoinException {
        return RAExpressionAttributes.joinUsing(left, right, using);
    }

    @Override
    protected RAExpressionAttributes create(RelationDefinition relation, ImmutableSet<RelationID> relationIds) {

        relationIndex++;
        ImmutableMap<QuotedID, ImmutableTerm> attributes = relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(Attribute::getID,
                        attribute -> termFactory.getVariable(attribute.getID().getName() + relationIndex)));

        return RAExpressionAttributes.create(attributes, relationIds);
    }

    @Override
    protected RAExpressionAttributes alias(RAExpressionAttributes rae, RelationID aliasId) {
        return RAExpressionAttributes.create(rae.getUnqualifiedAttributes(), ImmutableSet.of(aliasId));
    }

}
