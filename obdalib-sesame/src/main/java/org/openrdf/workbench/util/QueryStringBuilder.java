/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.workbench.util;

import java.util.regex.Pattern;

/**
 * Helper class for substituting in variables to query templates for the purpose
 * of saving and retrieving user queries to a repository local to the workbench.
 * 
 * @author Dale Visser
 */
public class QueryStringBuilder {

	private final StringBuilder builder;

	private static final Pattern VAR_PATTERN = Pattern.compile("\\$<\\w+>");

	/**
	 * Creates a new builder from the given template.
	 * 
	 * @param template
	 */
	public QueryStringBuilder(final String template) {
		if (null == template || template.isEmpty()) {
			throw new IllegalArgumentException("Template is null or length is zero.");
		}
		if (!VAR_PATTERN.matcher(template).find()) {
			throw new IllegalArgumentException("Template did not contain variables.");
		}
		this.builder = new StringBuilder(template);
	}

	/**
	 * Returns the internal string being constructed.
	 */
	@Override
	public String toString() {
		return this.builder.toString();
	}

	/**
	 * Replace the repository variable with the current repository URL.
	 * 
	 * @param paramText
	 *        the $<...> formatted parameter name
	 * @param uri
	 *        any object who's toString() returns a valid URI
	 */
	protected void replaceURI(final String paramText, final Object uri) {
		replace(paramText, QueryStringBuilder.uriQuote(uri.toString()));
	}

	/**
	 * Replace instances of the old text with a copy of the new text.
	 * 
	 * @param paramText
	 *        parameter in the form "$<paramName>"
	 * @param newText
	 *        the new text
	 */
	protected void replace(final String paramText, final String newText) {
		int loc = builder.indexOf(paramText);
		while (loc >= 0) {
			builder.replace(loc, loc + paramText.length(), newText);
			loc = builder.indexOf(paramText);
		}
	}

	protected void replaceQuote(final String paramText, final String newText) {
		this.replace(paramText, quote(newText));
	}

	/**
	 * Place double quotes around the given string.
	 * 
	 * @param value
	 *        the string to add quotes to
	 * @return a copy of the given strings quoted with double quotes
	 */
	private static String quote(final String value) {
		return quote(value, "\"", "\"");
	}

	/**
	 * Place double quotes around the given string and append an XSD data type.
	 * 
	 * @param value
	 *        the value to quote
	 * @param type
	 *        the XSD data type name
	 * @return a copy of the given string quoted with XSD data type appended
	 */
	protected static String xsdQuote(final String value, final String type) {
		return quote(value, "\"", "\"^^xsd:" + type);
	}

	/**
	 * Place angle brackets around a URI or URL.
	 * 
	 * @param uri
	 *        an object whose toString() returns a URI or URL
	 * @return a string quoting the given URI with angle brackets
	 */
	private static String uriQuote(final Object uri) {
		return quote(uri.toString(), "<", ">");
	}

	protected static String quote(final String value, final String left, final String right) {
		return left + value + right;
	}
}