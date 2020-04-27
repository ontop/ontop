package it.unibz.inf.ontop.dbschema.impl;


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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.unibz.inf.ontop.dbschema.QuotedID;

import java.io.IOException;

import static it.unibz.inf.ontop.dbschema.impl.SQLStandardQuotedIDFactory.NO_QUOTATION;

/**
 * Database identifier used for schema names, table names and aliases
 * <p>
 * An identifier can be in quotation marks (to preserve the case etc.).
 * Quotation, however, is ignored when identifiers are compared
 *
 * @author Roman Kontchakov
 *
 */


public class QuotedIDImpl implements QuotedID {

    private final String id;
    private final String quoteString;
    private final boolean caseSensitive;
    private final int hashCode;

    public static final QuotedID EMPTY_ID = new QuotedIDImpl(null, NO_QUOTATION);

    /**
     * (used only in QuotedIDFactory implementations)
     *
     * @param id can be null
     * @param quoteString cannot be null (the empty string stands for no quotation, as in getIdentifierQuoteString)
     */
    QuotedIDImpl(String id, String quoteString) {
        this(id, quoteString, true);
    }

    QuotedIDImpl(String id, String quoteString, boolean caseSensitive) {
        this.id = id;
        this.quoteString = quoteString;
        this.caseSensitive = caseSensitive;
        // increases collisions but makes it possible to have case-insensitive ids (for MySQL)
        this.hashCode = (id != null) ? id.toLowerCase().hashCode() : 0;
    }

    /**
     * returns the identifier (stripped of quotation marks)
     *
     * @return identifier without quotation marks (for comparison etc.)
     */

    @Override
    public String getName() {
        return id;
    }

    /**
     * returns SQL rendering of the identifier, in quotes, if necessary
     *
     * @return identifier possibly in quotes
     */

    @Override
    public String getSQLRendering() {
        return (id != null) ?  quoteString + id + quoteString : null;
    }

    @Override
    public String toString() {
        return getSQLRendering();
    }

    /**
     * compares two identifiers ignoring quotation
     */

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof QuotedIDImpl)  {
            QuotedIDImpl other = (QuotedIDImpl)obj;
            // very careful, id can be null
            // (proper comparison with .equals is below)
            //noinspection StringEquality
            if (this.id == other.id)
                return true;

            if  ((this.id != null) && (other.id != null)) {
                if (this.id.equals(other.id))
                    return true;
                if (!this.caseSensitive || !other.caseSensitive)
                    return this.id.toLowerCase().equals(other.id.toLowerCase());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public static class QuotedIDSerializer extends JsonSerializer<QuotedID> {
        @Override
        public void serialize(QuotedID value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getSQLRendering());
        }
    }
}
