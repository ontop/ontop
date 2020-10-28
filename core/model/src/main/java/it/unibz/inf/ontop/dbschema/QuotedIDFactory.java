package it.unibz.inf.ontop.dbschema;

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


import javax.annotation.Nullable;

/**
 * Factory for creating attribute and relation identifier from strings.
 * It defines the rules of transforming unquoted and quoted identifiers.
 * 
 * @author Roman Kontchakov
 *
 */

public interface QuotedIDFactory {

	/**
	 * 
	 * @param s possibly quoted (SQL rendering)
	 * @return
	 */
	
	QuotedID createAttributeID(String s);
	
	
	/**
	 * 
	 * @param schema null or a possibly quoted string (SQL rendering)
	 * @param table a possibly quoted string (SQL rendering)
	 * @return
	 */
	
	RelationID createRelationID(@Nullable String schema, String table);
	
	
	String getIDQuotationString();
}
