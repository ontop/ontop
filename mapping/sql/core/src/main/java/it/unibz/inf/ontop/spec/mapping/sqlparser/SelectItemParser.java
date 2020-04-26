package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.Map;
import java.util.function.BiFunction;

public class SelectItemParser {

    private final QuotedIDFactory idfac;
    private final ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes;
    private final BiFunction<Expression, ImmutableMap<QualifiedAttributeID, ImmutableTerm>, ImmutableTerm> expressionParser;

    SelectItemParser(ImmutableMap<QualifiedAttributeID, ImmutableTerm> attributes, BiFunction<Expression, ImmutableMap<QualifiedAttributeID, ImmutableTerm>, ImmutableTerm> expressionParser, QuotedIDFactory idfac) {
        this.idfac = idfac;
        this.attributes = attributes;
        this.expressionParser = expressionParser;
    }

    public ImmutableMap<QualifiedAttributeID, ImmutableTerm> getAttributes(SelectItem si) {
        SelectItemProcessor sip = new SelectItemProcessor();
        si.accept(sip);
        return sip.map;
    }

    private class SelectItemProcessor implements SelectItemVisitor {

        ImmutableMap<QualifiedAttributeID, ImmutableTerm> map;

        @Override
        public void visit(AllColumns allColumns) {
            map = attributes.entrySet().stream()
                    .filter(e -> e.getKey().getRelation() == null)
                    .collect(ImmutableCollectors.toMap());
        }

        @Override
        public void visit(AllTableColumns allTableColumns) {
            Table table = allTableColumns.getTable();
            RelationID id = idfac.createRelationID(table.getSchemaName(), table.getName());

            map = attributes.entrySet().stream()
                    .filter(e -> e.getKey().getRelation() != null && e.getKey().getRelation().equals(id))
                    .collect(ImmutableCollectors.toMap(
                            e -> new QualifiedAttributeID(null, e.getKey().getAttribute()),
                            Map.Entry::getValue));
        }

        @Override
        public void visit(SelectExpressionItem selectExpressionItem) {
            Expression expr = selectExpressionItem.getExpression();
            if (expr instanceof Column) {
                Column column = (Column) expr;
                QuotedID id = idfac.createAttributeID(column.getColumnName());
                ImmutableTerm var = expressionParser.apply(expr, attributes);
                Alias columnAlias = selectExpressionItem.getAlias();
                QuotedID name = (columnAlias == null || columnAlias.getName() == null)
                        ? id
                        : idfac.createAttributeID(columnAlias.getName());

                map = ImmutableMap.of(new QualifiedAttributeID(null, name), var);
            }
            else {
                Alias columnAlias = selectExpressionItem.getAlias();
                if (columnAlias != null && columnAlias.getName() != null) {
                    QuotedID name = idfac.createAttributeID(columnAlias.getName());
                    ImmutableTerm term = expressionParser.apply(expr, attributes);
                    map = ImmutableMap.of(new QualifiedAttributeID(null, name), term);
                }
                else {
                    // ignore complex expressions without an alias
                    // see https://www.w3.org/TR/r2rml/#r2rml-views
                    map = ImmutableMap.of();
                }
            }
        }
    }
}
