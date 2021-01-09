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
 * Database identifier used for schema names, table names and aliases
 * <p>
 * An identifier can be in quotation marks (to preserve the case etc.).
 * Quotation, however, is ignored when identifiers are compared
 *
 * @author Roman Kontchakov
 *
 */


public interface QuotedID {

	/**
	 * returns the identifier (stripped of quotation marks)
	 *
	 * @return identifier without quotation marks (for comparison etc.)
	 */

	@Nonnull
	String getName();

	/**
	 * returns SQL rendering of the identifier, in quotes, if necessary
	 *
	 * @return identifier possibly in quotes
	 */

	@Nonnull
	String getSQLRendering();
}
