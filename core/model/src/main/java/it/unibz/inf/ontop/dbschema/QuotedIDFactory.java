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


import javax.annotation.Nonnull;

/**
 * Factory for creating attribute and relation identifier from strings.
 * It defines the rules of transforming unquoted and quoted identifiers.
 * 
 * @author Roman Kontchakov
 *
 */

public interface QuotedIDFactory {

	/**
	 * Creates a new attribute ID from a string.
	 * @param attributeId possibly quoted attribute ID (SQL rendering)
	 * @return attribute ID
	 */
	QuotedID createAttributeID(@Nonnull String attributeId);


	RelationID createRelationID(@Nonnull String tableId);

	/**
	 * Creates a new relation ID from the component strings.
	 * @param components list of the possibly quoted components of relation ID,
	 *                      from the catalog to the table name
	 * @return relation ID
	 */
	RelationID createRelationID(String... components);


	/**
	 * @return quotation string used in the SQL rendering
	 */
	String getIDQuotationString();
}
