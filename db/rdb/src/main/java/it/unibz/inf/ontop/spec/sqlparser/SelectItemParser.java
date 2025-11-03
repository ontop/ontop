package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.Maps;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.sqlparser.exception.InvalidSelectQueryRuntimeException;
import net.sf.jsqlparser.expression.Alias;
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

    public RAExpressionAttributes parseSelectItems(List<SelectItem> selectItems) {
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


    private class SelectItemProcessor implements SelectItemVisitor {
        Stream<Map.Entry<QuotedID, ImmutableTerm>> stream;

        private Stream<Map.Entry<QuotedID, ImmutableTerm>> getAttributes(SelectItem si) {
            si.accept(this);
            return stream;
        }

        @Override
        public void visit(AllColumns allColumns) {
            stream =  attributes.getUnqualifiedAttributesMap().entrySet().stream();
        }

        @Override
        public void visit(AllTableColumns allTableColumns) {
            Table table = allTableColumns.getTable();
            RelationID id = JSqlParserTools.getRelationId(idfac, table);

            stream = attributes.getRelationAttributesMap(id).entrySet().stream();
        }

        @Override
        public void visit(SelectExpressionItem selectExpressionItem) {
            Alias columnAlias = selectExpressionItem.getAlias();
            Expression expr = selectExpressionItem.getExpression();
            Optional<String> alias;
            if (columnAlias != null && columnAlias.getName() != null) {
                alias = Optional.of(columnAlias.getName());
            }
            else if (expr instanceof Column) {
                alias = Optional.of(((Column) expr).getColumnName());
            }
            else {
                // ignore complex expressions without an alias
                // see https://www.w3.org/TR/r2rml/#r2rml-views
                alias = Optional.empty();
            }
            stream = alias.stream()
                    .map(a -> Maps.immutableEntry(idfac.createAttributeID(a), expressionParser.apply(expr, attributes)));
        }
    }
}
