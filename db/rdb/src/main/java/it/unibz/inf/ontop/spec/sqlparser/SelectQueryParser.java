package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.sqlparser.exception.*;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 */
public class SelectQueryParser extends FromItemParser<RAExpression> {
    private final QuotedIDFactory idfac;
    private final ExpressionParser expressionParser;

    public SelectQueryParser(MetadataLookup metadata, CoreSingletons coreSingletons) {
        super(new ExpressionParser(metadata.getQuotedIDFactory(), coreSingletons), metadata.getQuotedIDFactory(), metadata, coreSingletons.getTermFactory(), new RAExpressionOperations(coreSingletons.getTermFactory(), coreSingletons.getIQFactory()));
        this.idfac = metadata.getQuotedIDFactory();
        this.expressionParser = new ExpressionParser(idfac, coreSingletons);
    }

    public RAExpression parse(SelectBody selectBody) throws InvalidQueryException, UnsupportedSelectQueryException {
        try {
            return translateSelectBody(selectBody);
        }
        catch (InvalidSelectQueryRuntimeException e) {
            throw new InvalidQueryException(e.getMessage(), e.getObject());
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
            RAExpression base = translateJoins(plainSelect.getFromItem(), plainSelect.getJoins());

            ImmutableList<ImmutableExpression> filter = plainSelect.getWhere() == null
                    ? ImmutableList.of()
                    : expressionParser.parseBooleanExpression(plainSelect.getWhere(), base.getAttributes());

            rae = operations.filter(base, filter);
        }
        catch (IllegalJoinException e) {
            throw new InvalidSelectQueryRuntimeException(e.toString(), plainSelect);
        }

        SelectItemParser sip = new SelectItemParser(rae.getAttributes(), expressionParser::parseTerm, idfac);
        RAExpressionAttributes attributes =
                sip.parseSelectItems(plainSelect.getSelectItems());

        return new RAExpression(rae.getDataAtoms(), rae.getFilterAtoms(), attributes);
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

        if ((join.isFull() || join.isRight() || (join.isLeft() && !join.isSemi()) || join.isOuter())
            && (join.getUsingColumns() != null || join.getOnExpression() != null))
            throw new UnsupportedSelectQueryRuntimeException("LEFT/RIGHT/FULL OUTER JOINs are not supported", join);

        if (join.isLeft() && join.isSemi() && join.getOnExpression() != null)
            throw new UnsupportedSelectQueryRuntimeException("LEFT/RIGHT/FULL OUTER JOINs are not supported", join);

        return super.join(left, join);
    }

    @Override
    protected void validateFromItem(Table table) {
        if (table.getIndexHint() != null)
            throw new UnsupportedSelectQueryRuntimeException("MySQL index hints are not supported", table);
    }

    @Override
    public RAExpression create(NamedRelationDefinition relation) {
        return operations.create(relation, createAttributeVariables(relation));
    }

    public RAExpression translateParserView(RelationDefinition view) {
        return ((RAExpressionOperations)operations).createWithoutName(view, createAttributeVariables(view));
    }
}
