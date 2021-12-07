package it.unibz.inf.ontop.spec.sqlparser;

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
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidQueryException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Roman Kontchakov on 09/01/2017.
 */

public class ApproximateSelectQueryAttributeExtractor {

    private final QuotedIDFactory idfac;

    private static final Pattern AS = Pattern.compile("\\sAS\\s", Pattern.CASE_INSENSITIVE);
    private static final Pattern BRACKETS = Pattern.compile("\\([^()]*\\)");
    private static final Pattern COL_SEP = Pattern.compile(",");
    private static final Pattern SELECT = Pattern.compile("\\A[\\s]*SELECT\\s+(DISTINCT\\s)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern FROM = Pattern.compile("\\sFROM\\s", Pattern.CASE_INSENSITIVE);

    public ApproximateSelectQueryAttributeExtractor(QuotedIDFactory idfac) {
        this.idfac = idfac;
    }

    public ImmutableList<QuotedID> getAttributes(String sql) throws InvalidQueryException {

        // COULD NOT PARSE - do a rough approximation

        Matcher startMatcher = SELECT.matcher(sql);
        if (!startMatcher.find())
            throw new InvalidQueryException("Error parsing SQL query: Couldn't find SELECT clause", sql);
        int start = startMatcher.end();

        Matcher endMatcher = FROM.matcher(sql);
        if (!endMatcher.find())
            throw new InvalidQueryException("Error parsing SQL query: Couldn't find FROM clause", sql);
        int end = endMatcher.start();

        String projection = sql.substring(start, end);

        // remove all brackets
        for (Matcher matcher = BRACKETS.matcher(projection); matcher.find(); matcher = BRACKETS.matcher(projection))
            projection = matcher.replaceAll("");

        final ImmutableList.Builder<QuotedID> attributes = ImmutableList.builder();
        for (String column : COL_SEP.split(projection)) {
            String[] components = AS.split(column);
            // components = { column } if there is no AS
            String columnName = components[components.length - 1].trim();

            // ROMAN (25 Jan 2017): do not understand the purpose
            // split on spaces that are not inside single quotes
            // if (columnName.contains(" "))
            //    columnName = columnName.split("\\s+(?![^'\"]*')")[1].trim();

            // get only the column name (but not the qualifier table name)
            // eg: table.column -> column
            columnName = columnName.substring(columnName.lastIndexOf(".") + 1);

            QuotedID attribute = idfac.createAttributeID(columnName);
            attributes.add(attribute);
        }

        return attributes.build();
    }
}
