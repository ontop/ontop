/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 * 
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.domain;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

@Deprecated
public abstract class Query implements Cloneable {

	/***************************************************************************
	 * This is the string which initialized this query. It might be null if the
	 * query has no arbitrary string asociated to it.
	 */
	protected String	inputquery	= null;
	
	protected APIController apic = null;

	/**	 * *
	 * 
	 * @deprecated Dont use this constructor, no concept of input query will be kept in the future
	 * @param inputquery
	 * @param apic
	 * @throws QueryParseException
	 */
	public Query(String inputquery, APIController apic) throws QueryParseException {
		this.apic = apic;
		if (inputquery == null)
			throw new IllegalArgumentException("received null");
		this.inputquery = inputquery;
		updateObjectToInputQuery(apic);
	}
	
	public Query() {
		//TODO remove this input query completly
		this.inputquery = "";
	}

	/***
	 * @deprecated there wont be mix between sintax and object anymore
	 * 
	 * @return
	 */
	public String getInputQuString() {
		if (inputquery == null) {
			return "";
		}
		return inputquery;
	}

	/***
	 * @deprecated there wont be mix between sintax and object anymore
	 * 
	 * @return
	 */
	public void setInputQuery(String inputquery, APIController apic) throws QueryParseException {
		this.inputquery = inputquery;
		updateObjectToInputQuery(apic);
	}

	/***
	 * @deprecated there wont be mix between sintax and object anymore
	 * 
	 * @return
	 */
	public boolean isInputQueryValid(APIController apic) {
		if (inputquery == null)
			return false;
		boolean valid = false;
		Query query;
		try {
			query = parse(inputquery,apic);
		} catch (QueryParseException e) {
			return false;
		}
		if (query != null) {
			valid = true;
		}
		return valid;
	}

	/***************************************************************************
	 * Returns the String representation of this Query structure. Returns the
	 * empty string "" if the internal representation cant be translated into a
	 * valid string in the corresponding query language.
	 */
	public abstract String toString();

	/***************************************************************************
	 * Makes a deep copy of this query, creating clones of each of its fields.
	 * No references to existing objects are kept.
	 */
	public abstract Query clone();

	/***
	 * Updates the internal representation of this query to the representation
	 * that corresponds to the input query. If the inputquery is invalid it will
	 * asure that the object is left in a state in which toString() returns
	 * null.
	 * 
	 * @deprecated about to be refactored
	 */
	protected abstract void updateObjectToInputQuery(APIController apic) throws QueryParseException;

	/***
	 * parses the given query and returns a new Query object which corresponds
	 * to the given query string. If the string was invalid it returns "".
	 * 
	 * @deprecated about to be refactored
	 * 
	 * @param query
	 * @return
	 */
	public abstract Query parse(String query, APIController apic) throws QueryParseException;
}
