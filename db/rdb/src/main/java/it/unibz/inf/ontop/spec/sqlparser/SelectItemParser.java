package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.Maps;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryRuntimeException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.DateUnitExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectItemParser {

    private final QuotedIDFactory idfac;
    private final RAExpressionAttributes attributes;
    private final BiFunction<Expression, RAExpressionAttributes, ImmutableTerm> expressionParser;

    SelectItemParser(RAExpressionAttributes attributes, BiFunction<Expression, RAExpressionAttributes, ImmutableTerm> expressionParser, QuotedIDFactory idfac) {
        this.idfac = idfac;
        this.attributes = attributes;
        this.expressionParser = expressionParser;
    }

    public RAExpressionAttributes parseSelectItems(List<SelectItem<?>> selectItems) {
        try {
            return RAExpressionAttributes.of(selectItems.stream()
                    .flatMap(si -> new SelectItemProcessor().getAttributes(si)));
        }
        catch (RAExpressionAttributes.DuplicateAttrbuteEntriesException e) {
            throw new InvalidSelectQueryRuntimeException(e.getDuplicates().stream()
                    .map(QuotedID::getSQLRendering)
                    .collect(Collectors.joining(", ",
                            "Duplicate column names ",
                            " in the SELECT clause: ")),  selectItems);
        }
    }


    private class SelectItemProcessor {
        Stream<Map.Entry<QuotedID, ImmutableTerm>> stream;

        private Stream<Map.Entry<QuotedID, ImmutableTerm>> getAttributes(SelectItem<?> si) {
            if (si.getClass() != SelectItem.class)
                throw new UnsupportedSelectQueryRuntimeException("Unsupported SELECT item", si);
            Alias alias = si.getAlias();
            if (alias != null && (alias.getClass() != Alias.class || alias.getAliasColumns() != null))
                throw new UnsupportedSelectQueryRuntimeException("Alias columns are not supported", alias);
            Expression expression = si.getExpression();
            if (expression instanceof AllColumns) {
                AllColumns wildcard = (AllColumns) expression;
                if (expression.getClass() != AllColumns.class && expression.getClass() != AllTableColumns.class)
                    throw new UnsupportedSelectQueryRuntimeException("Unsupported wildcard", expression);
                // AllTableColumns inherits EXCEPT/REPLACE from AllColumns.
                if (wildcard.getExceptColumns() != null || wildcard.getReplaceExpressions() != null
                        || wildcard.getExceptKeyword() != null)
                    throw new UnsupportedSelectQueryRuntimeException("Wildcard EXCEPT/REPLACE are not supported", expression);
                if (alias != null)
                    throw new UnsupportedSelectQueryRuntimeException("Wildcard aliases are not supported", si);
            }
            if (expression instanceof AllTableColumns)
                visit((AllTableColumns) expression);
            else if (expression instanceof AllColumns)
                visit((AllColumns) expression);
            else
                visit(si);
            return stream;
        }

        public void visit(AllColumns allColumns) {
            stream =  attributes.getUnqualifiedAttributesMap().entrySet().stream();
        }

        public void visit(AllTableColumns allTableColumns) {
            if (allTableColumns.getReturningReferenceType() != null || allTableColumns.getReturningQualifier() != null)
                throw new UnsupportedSelectQueryRuntimeException("Returning wildcards are not supported", allTableColumns);
            Table table = allTableColumns.getTable();
            if (table == null)
                throw new UnsupportedSelectQueryRuntimeException("Missing wildcard table qualifier", allTableColumns);
            JSqlParserTools.validateTableQualifier(table);
            RelationID id = JSqlParserTools.getRelationId(idfac, table);

            stream = attributes.getRelationAttributesMap(id).entrySet().stream();
        }

        public void visit(SelectItem<?> selectExpressionItem) {
            Alias columnAlias = selectExpressionItem.getAlias();
            Expression expr = selectExpressionItem.getExpression();
            Optional<String> alias;
            if (columnAlias != null && columnAlias.getName() != null) {
                alias = Optional.of(columnAlias.getName());
            }
            else if (expr instanceof Column) {
                alias = Optional.of(((Column) expr).getColumnName());
            }
            else if (expr instanceof DateUnitExpression) {
                alias = Optional.of(expr.toString());
            }
            else {
                // ignore complex expressions without an alias
                // see https://www.w3.org/TR/r2rml/#r2rml-views
                alias = Optional.empty();
            }
            // Unnamed expressions do not contribute attributes, but still need feature validation.
            ImmutableTerm term = expressionParser.apply(expr, attributes);
            stream = alias.stream()
                    .map(a -> Maps.immutableEntry(idfac.createAttributeID(a), term));
        }
    }
}
