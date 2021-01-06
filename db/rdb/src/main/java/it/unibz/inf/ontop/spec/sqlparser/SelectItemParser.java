package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectItemParser {

    private final QuotedIDFactory idfac;
    private final RAExpressionAttributes attributes;
    private final Function<Expression, ImmutableTerm> expressionParser;

    SelectItemParser(RAExpressionAttributes attributes, BiFunction<Expression, RAExpressionAttributes, ImmutableTerm> expressionParser, QuotedIDFactory idfac) {
        this.idfac = idfac;
        this.attributes = attributes;
        this.expressionParser = e -> expressionParser.apply(e, attributes);
    }

    public RAExpressionAttributes parseSelectItems(List<SelectItem> selectItems) {

        try {
            ImmutableMap<QuotedID, ImmutableTerm> map = selectItems.stream()
                    .flatMap(si -> new SelectItemProcessor().getAttributes(si))
                    .collect(ImmutableCollectors.toMap());

            return new RAExpressionAttributesOperations().create(map);
        }
        catch (IllegalArgumentException e) {
            Map<QuotedID, Integer> duplicates = new HashMap<>();
            selectItems.stream()
                    .flatMap(si -> new SelectItemProcessor().getAttributes(si))
                    .forEach(a -> duplicates.put(a.getKey(), duplicates.getOrDefault(a.getKey(), 0) + 1));

            throw new InvalidSelectQueryRuntimeException(duplicates.entrySet().stream()
                    .filter(d -> d.getValue() > 1)
                    .map(Map.Entry::getKey)
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
            stream = attributes.asMap().entrySet().stream()
                    .filter(e -> e.getKey().getRelation() == null)
                    .map(e -> Maps.immutableEntry(e.getKey().getAttribute(), e.getValue()));
        }

        @Override
        public void visit(AllTableColumns allTableColumns) {
            Table table = allTableColumns.getTable();
            RelationID id = JSqlParserTools.getRelationId(idfac, table);

            stream = attributes.asMap().entrySet().stream()
                    .filter(e -> e.getKey().getRelation() != null && e.getKey().getRelation().equals(id))
                    .map(e -> Maps.immutableEntry(e.getKey().getAttribute(), e.getValue()));
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
            stream = alias.map(a -> Stream.of(Maps.immutableEntry(
                        idfac.createAttributeID(a), expressionParser.apply(expr))))
                    .orElse(Stream.of());
        }
    }
}
