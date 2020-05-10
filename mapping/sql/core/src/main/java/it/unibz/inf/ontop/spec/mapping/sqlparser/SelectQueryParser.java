package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.statement.select.*;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 */
public class SelectQueryParser extends FromItemParser<RAExpression> {
    private final QuotedIDFactory idfac;
    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;
    private final ExpressionParser expressionParser;

    public SelectQueryParser(MetadataLookup metadata, CoreSingletons coreSingletons) {
        super(new ExpressionParser(metadata.getQuotedIDFactory(), coreSingletons), metadata.getQuotedIDFactory(), metadata, coreSingletons.getTermFactory());
        this.idfac = metadata.getQuotedIDFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.expressionParser = new ExpressionParser(idfac, coreSingletons);
    }

    public RAExpression parse(SelectBody selectBody) throws InvalidSelectQueryException, UnsupportedSelectQueryException {
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
    protected RAExpression translateSelectBody(SelectBody selectBody) {
        PlainSelect plainSelect = JSqlParserTools.getPlainSelect(selectBody);

        if (plainSelect.getDistinct() != null)
            throw new UnsupportedSelectQueryRuntimeException("DISTINCT is not supported", plainSelect);

        if (plainSelect.getGroupBy() != null || plainSelect.getHaving() != null)
            throw new UnsupportedSelectQueryRuntimeException("GROUP BY / HAVING are not supported", plainSelect);

        if (plainSelect.getLimit() != null || plainSelect.getTop() != null || plainSelect.getOffset() != null)
            throw new UnsupportedSelectQueryRuntimeException("LIMIT / OFFSET / TOP are not supported", plainSelect);

        if (plainSelect.getOrderByElements() != null)
            throw new UnsupportedSelectQueryRuntimeException("ORDER BY is not supported", plainSelect);

        if (plainSelect.getOracleHierarchical() != null || plainSelect.isOracleSiblings())
            throw new UnsupportedSelectQueryRuntimeException("Oracle START WITH ... CONNECT BY / ORDER SIBLINGS BY are not supported", plainSelect);

        if (plainSelect.getFromItem() == null)
            throw new UnsupportedSelectQueryRuntimeException("SELECT without FROM is not supported", plainSelect);

        RAExpression current;
        try {
            current = translateJoins(plainSelect.getFromItem(), plainSelect.getJoins());
        }
        catch (IllegalJoinException e) {
            throw new InvalidSelectQueryRuntimeException(e.toString(), plainSelect);
        }

        ImmutableList<ImmutableExpression> filterAtoms = Stream.concat(
                current.getFilterAtoms().stream(),
                plainSelect.getWhere() == null
                    ? Stream.of()
                    : expressionParser.parseBooleanExpression(
                        plainSelect.getWhere(), current.getAttributes()).stream())
                .collect(ImmutableCollectors.toList());

        SelectItemParser sip = new SelectItemParser(current.getAttributes(), expressionParser::parseTerm, idfac);
        RAExpressionAttributes attributes =
                JSqlParserTools.parseSelectItems(sip, plainSelect.getSelectItems());

        return new RAExpression(current.getDataAtoms(), filterAtoms, attributes, termFactory);
    }


    @Override
    protected RAExpression join(RAExpression left, Join join) throws IllegalJoinException {

        if (join.isSimple() && join.isOuter())
            throw new UnsupportedSelectQueryRuntimeException("Simple OUTER JOINs are not supported", join);

        if ((join.isFull() || join.isRight() || join.isLeft() || join.isOuter())
            && (join.getUsingColumns() != null || join.getOnExpression() != null))
            throw new UnsupportedSelectQueryRuntimeException("LEFT/RIGHT/FULL OUTER JOINs are not supported", join);

        return super.join(left, join);
    }





    @Override
    public RAExpression create(RelationDefinition relation, ImmutableSet<RelationID> relationIDs) {
        RAExpressionAttributes attributes = createRAExpressionAttributes(relation, relationIDs);

        ImmutableMap<Integer, Variable> terms = relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(a -> a.getIndex() - 1,
                        a -> (Variable) attributes.getAttributes().get(new QualifiedAttributeID(null, a.getID()))));

        ExtensionalDataNode atom = iqFactory.createExtensionalDataNode(relation, terms);

        return new RAExpression(ImmutableList.of(atom), ImmutableList.of(), attributes, termFactory);
    }
}
