package it.unibz.inf.ontop.sql.parser;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.sql.*;
import it.unibz.inf.ontop.sql.parser.exceptions.InvalidSelectQueryException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import javax.management.relation.Relation;


/**
 * Created by Roman Kontchakov on 09/01/2017.
 */
public class SelectQueryAttributeExtractor {

    private final QuotedIDFactory idfac;
    private final DBMetadata metadata;

    private static final  String[] aliasSplitters = new String[]{" as ", " AS "};

    private final SelectQueryAttributeExtractor2 sqae;

    public SelectQueryAttributeExtractor(DBMetadata metadata) {
        this.metadata = metadata;
        this.idfac = metadata.getQuotedIDFactory();
        sqae = new SelectQueryAttributeExtractor2(metadata);
    }

    public ImmutableList<QuotedID> extract(String sql) {

        try {
            ImmutableMap<QualifiedAttributeID, Variable> attrs = sqae.parse(sql).getAttributes();

            return attrs.keySet().stream()
                    .filter(id -> id.getRelation() == null)
                    .map(id -> id.getAttribute())
                    .collect(ImmutableCollectors.toList());
        }
/*
        final ImmutableList.Builder<QuotedID> attributes = ImmutableList.builder();

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select))
                throw new InvalidSelectQueryException("The inserted query is not a SELECT statement", statement);

            Select select = (Select) statement;
            select.getSelectBody().accept(new SelectVisitor() {

                @Override
                public void visit(PlainSelect plainSelect) {
                    for (SelectItem item : plainSelect.getSelectItems())
                        item.accept(new SelectItemVisitor() {

                            @Override
                            public void visit(AllColumns allColumns) {
                                // do not add columns in the case of SELECT *
                            }

                            @Override
                            public void visit(AllTableColumns allTableColumns) {
                                // assumes that there are no aliases and the table = relation in the database
                                RelationID id = idfac.createRelationID(allTableColumns.getTable().getSchemaName(), allTableColumns.getTable().getName());
                                DatabaseRelationDefinition relation = metadata.getDatabaseRelation(id);
                                if (relation != null) {
                                    for (Attribute attribute : relation.getAttributes())
                                        attributes.add(attribute.getID());
                                }
                            }

                            @Override
                            public void visit(SelectExpressionItem selectExpressionItem) {
                                String attributeName;
                                if (selectExpressionItem.getAlias() != null) {
                                    attributeName = selectExpressionItem.getAlias().getName();
                                }
                                else {
                                    Expression exp = selectExpressionItem.getExpression();
                                    if (!(exp instanceof Column))
                                        throw new InvalidSelectQueryException("All complex expressions must have aliases", selectExpressionItem);

                                    attributeName = ((Column) exp).getColumnName();
                                }
                                QuotedID attribute = idfac.createAttributeID(attributeName);
                                attributes.add(attribute);
                            }
                        });
                }

                @Override
                public void visit(SetOperationList setOpList) {
                    // for INTERSECT, EXCEPT, MINUS, UNION
                    // process only the first argument
                    // (the other argument must be compatible, with the same list of attributes)
                    setOpList.getPlainSelects().get(0).accept(this);
                }

                @Override
                public void visit(WithItem withItem) {
                    // ignore WITH clauses
                }
            });
        }
        catch (JSQLParserException e) {
*/
        catch (Exception e) {
            final ImmutableList.Builder<QuotedID> attributes = ImmutableList.builder();

            // COULD NOT PARSE - do a rough approximation

            int start = "select".length();
            // might be a good idea to surround FROM with whitespaces
            int end = sql.toLowerCase().indexOf("from");
            if (end == -1)
                throw new InvalidSelectQueryException("Error parsing SQL query: Couldn't find FROM clause", sql);

            String projection = sql.substring(start, end).trim();

            // split on commas that are not inside parenthesis
            String[] columns = projection.split(",+(?!.*\\))");

            for (String col : columns) {
                String columnName = col.trim();

                // take the alias name if present
                for (String aliasSplitter : aliasSplitters)
                    if (columnName.contains(aliasSplitter)) { // has an alias
                        columnName = columnName.split(aliasSplitter)[1].trim();
                        break;
                    }

                // split on spaces that are not inside single quotes
                if (columnName.contains(" "))
                    columnName = columnName.split("\\s+(?![^'\"]*')")[1].trim();

                // get only the column name (but not the qualifier table name)
                // eg: table.column -> column
                if (columnName.contains("."))
                    columnName = columnName.substring(columnName.lastIndexOf(".") + 1);

                QuotedID attribute = idfac.createAttributeID(columnName);
                attributes.add(attribute);
            }

            return attributes.build();
        }
    }

}
