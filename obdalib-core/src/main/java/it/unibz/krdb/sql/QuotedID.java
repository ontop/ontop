package it.unibz.krdb.sql;

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

/**
 * Database identifier used for schema names, table names and aliases
 * <p>
 * An identifier can be in quotation marks (to preserve the case etc.).
 * Quotation, however, is ignored when identifiers are compared
 * 
 * @author Roman Kontchakov
 *
 */


public class QuotedID {
	
	private final String id;
	private final String quoteString;

	public static final String NO_QUOTATION = "";
	public static final String QUOTATION = "\"";
	
	/**
	 * 
	 * @param name can be null
	 * @param quotes cannot be null (the empty string stands for no quotation, as in getIdentifierQuoteString)
	 */
	QuotedID(String id, String quoteString) {
		this.id = id;
		this.quoteString = quoteString;
	}
	
	public static QuotedID createFromDatabaseRecord(String s) {
		return new QuotedID(s, QUOTATION);
	}
	
	/**
	 * returns the identifier (stripped of quotation marks)
	 * 
	 * @return identifier without quotation marks (for comparison etc.)
	 */
	
	public String getName() {
		return id;
	}
	
	/**
	 * returns SQL rendering of the identifier, in quotes, if necessary
	 * 
	 * @return identifier possibly in quotes
	 */
	
	public String getSQLRendering() {
		if (id == null)
			return null;
		
		return quoteString + id + quoteString;
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
		
		if (obj instanceof QuotedID)  {
			QuotedID other = (QuotedID)obj;
			// very careful, name can be null
			if (this.id == other.id)
				return true;
			
			return (this.id != null) && this.id.equals(other.id);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (id != null)
			return id.hashCode();
		return 0;
	}

}
