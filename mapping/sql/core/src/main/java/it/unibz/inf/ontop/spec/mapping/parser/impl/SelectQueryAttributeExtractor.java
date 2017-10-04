package it.unibz.inf.ontop.spec.mapping.parser.impl;

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
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.regex.Pattern;


/**
 * Created by Roman Kontchakov on 09/01/2017.
 */
public class SelectQueryAttributeExtractor {

    private final QuotedIDFactory idfac;

    private static final Pattern AS = Pattern.compile("\\sAS\\s", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern SQL_FUNCTION = Pattern.compile("\\w+\\s*\\(.*\\)\\s*as", Pattern.CASE_INSENSITIVE);

    private final SelectQueryAttributeExtractor2 sqae;

    public SelectQueryAttributeExtractor(DBMetadata metadata) {
        this.idfac = metadata.getQuotedIDFactory();
        sqae = new SelectQueryAttributeExtractor2(metadata);
    }

    public ImmutableList<QuotedID> extract(String sql) throws InvalidSelectQueryException {

        try {
            ImmutableMap<QualifiedAttributeID, Variable> attrs = sqae.parse(sql).getAttributes();

            return attrs.keySet().stream()
                    .filter(id -> id.getRelation() == null)
                    .map(id -> id.getAttribute())
                    .collect(ImmutableCollectors.toList());
        }
        catch (Exception e) {
            final ImmutableList.Builder<QuotedID> attributes = ImmutableList.builder();

            // COULD NOT PARSE - do a rough approximation

            int start = sql.toLowerCase().indexOf("select") + "select".length();
            // might be a good idea to surround FROM with whitespaces
            int end = sql.toLowerCase().indexOf("from");
            if (end == -1)
                throw new InvalidSelectQueryException("Error parsing SQL query: Couldn't find FROM clause", sql);

            String projection = sql.substring(start, end).trim();

            // remove all brackets
            while (projection.matches("\\([^\\(]*\\)"))
                projection = projection.replaceAll("\\([^\\(]*\\)", "");
            
            // fabad (3 Oct 2017): Remove functions from the projection
            // in order to avoid parser errors because of commas inside
            // the function parameters.
            // For example:
            // to_char(ALMAES001.INIT_DATE,'YYYY-MM-DD') AS INIT_DATE => INIT_DATE.
            // It is mandatory to rename the result of the function with "AS" clause.
            // In other case the parser error occurs.
            projection = SQL_FUNCTION.matcher(projection).replaceAll("");

            for (String col : projection.split(",")) {
                // TODO: AS should be treated as optional
                String[] components = AS.split(col);
                String columnName = components[components.length - 1].trim();

                // ROMAN (25 Jan 2017): do not understand the purpose
                // split on spaces that are not inside single quotes
                // if (columnName.contains(" "))
                //    columnName = columnName.split("\\s+(?![^'\"]*')")[1].trim();

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

