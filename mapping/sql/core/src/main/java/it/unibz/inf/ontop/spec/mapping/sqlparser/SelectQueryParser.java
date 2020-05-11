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

import java.util.Optional;
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

        if (plainSelect.getOracleHint() != null)
            throw new UnsupportedSelectQueryRuntimeException("Oracle hints are not supported", plainSelect);

        if (plainSelect.getSkip() != null || plainSelect.getFirst() != null)
            throw new UnsupportedSelectQueryRuntimeException("SKIP / FIRST are not supported", plainSelect);

        if (plainSelect.getDistinct() != null)
            throw new UnsupportedSelectQueryRuntimeException("DISTINCT is not supported", plainSelect);

        if (plainSelect.getTop() != null)
            throw new UnsupportedSelectQueryRuntimeException("TOP is not supported", plainSelect);

        if (plainSelect.getMySqlSqlNoCache())
            throw new UnsupportedSelectQueryRuntimeException("MySQL SQL_NO_CACHE is not supported", plainSelect);

        if (plainSelect.getMySqlSqlCalcFoundRows())
            throw new UnsupportedSelectQueryRuntimeException("MySQL SQL_CALC_FOUND_ROWS is not supported", plainSelect);

        if (plainSelect.getKsqlWindow() != null)
            throw new UnsupportedSelectQueryRuntimeException("WINDOW is not supported", plainSelect);

        if (plainSelect.getOracleHierarchical() != null || plainSelect.isOracleSiblings())
            throw new UnsupportedSelectQueryRuntimeException("Oracle START WITH ... CONNECT BY / ORDER SIBLINGS BY are not supported", plainSelect);

        if (plainSelect.getGroupBy() != null || plainSelect.getHaving() != null)
            throw new UnsupportedSelectQueryRuntimeException("GROUP BY / HAVING are not supported", plainSelect);

        if (plainSelect.getOrderByElements() != null)
            throw new UnsupportedSelectQueryRuntimeException("ORDER BY is not supported", plainSelect);

        if (plainSelect.getLimit() != null || plainSelect.getOffset() != null || plainSelect.getFetch() != null)
            throw new UnsupportedSelectQueryRuntimeException("LIMIT / OFFSET / FETCH are not supported", plainSelect);

        if (plainSelect.isForUpdate())
            throw new UnsupportedSelectQueryRuntimeException("FOR UPDATE is not supported", plainSelect);

        if (plainSelect.getOptimizeFor() != null)
            throw new UnsupportedSelectQueryRuntimeException("OPTIMIZE FOR is not supported", plainSelect);

        if (plainSelect.getForXmlPath() != null)
            throw new UnsupportedSelectQueryRuntimeException("FOR XML PATH is not supported", plainSelect);

        RAExpression rae;
        try {
            rae = (plainSelect.getFromItem() != null)
                ? translateJoins(plainSelect.getFromItem(), plainSelect.getJoins())
                : new RAExpression(ImmutableList.of(), ImmutableList.of(),
                    RAExpressionAttributes.create(ImmutableMap.of()), termFactory);
        }
        catch (IllegalJoinException e) {
            throw new InvalidSelectQueryRuntimeException(e.toString(), plainSelect);
        }

        ImmutableList<ImmutableExpression> filterAtoms = Stream.concat(
                rae.getFilterAtoms().stream(),
                plainSelect.getWhere() == null
                    ? Stream.of()
                    : expressionParser.parseBooleanExpression(
                        plainSelect.getWhere(), rae.getAttributes()).stream())
                .collect(ImmutableCollectors.toList());

        SelectItemParser sip = new SelectItemParser(rae.getAttributes(), expressionParser::parseTerm, idfac);
        RAExpressionAttributes attributes =
                sip.parseSelectItems(plainSelect.getSelectItems());

        return new RAExpression(rae.getDataAtoms(), filterAtoms, attributes, termFactory);
    }


    @Override
    protected RAExpression join(RAExpression left, Join join) throws IllegalJoinException {

        if (join.isSimple() && join.isOuter())
            throw new UnsupportedSelectQueryRuntimeException("Simple OUTER JOINs are not supported", join);

        if (join.isApply())
            throw new UnsupportedSelectQueryRuntimeException("APPLY is not supported", join);

        if (join.isStraight())
            throw new UnsupportedSelectQueryRuntimeException("STRAIGHT_JOIN is not supported", join);

        if (join.isWindowJoin())
            throw new UnsupportedSelectQueryRuntimeException("WITHIN WINDOW is not supported", join);

        if ((join.isFull() || join.isRight() || join.isLeft() || join.isOuter())
            && (join.getUsingColumns() != null || join.getOnExpression() != null))
            throw new UnsupportedSelectQueryRuntimeException("LEFT/RIGHT/FULL OUTER JOINs are not supported", join);

        return super.join(left, join);
    }





    @Override
    public RAExpression create(DatabaseRelationDefinition relation) {
        return create(relation, createRAExpressionAttributes(relation));
    }

    public RAExpression create(RelationDefinition relation, RAExpressionAttributes attributes) {
        ImmutableMap<Integer, Variable> terms = relation.getAttributes().stream()
                .collect(ImmutableCollectors.toMap(a -> a.getIndex() - 1,
                        a -> (Variable) attributes.get(a.getID())));

        ExtensionalDataNode atom =  iqFactory.createExtensionalDataNode(relation, terms);
        return new RAExpression(ImmutableList.of(atom), ImmutableList.of(), attributes, termFactory);
    }
}
