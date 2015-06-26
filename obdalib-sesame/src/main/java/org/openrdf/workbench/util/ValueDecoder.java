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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.Iterations;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.exceptions.BadRequestException;

/**
 * Decodes strings into values for {@link WorkbenchRequst}.
 */
class ValueDecoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValueDecoder.class);

	private final ValueFactory factory;

	private final Repository repository;

	/**
	 * Creates an instance of ValueDecoder.
	 * 
	 * @param repository
	 *        to get namespaces from
	 * @param factory
	 *        to generate values
	 */
	protected ValueDecoder(Repository repository, ValueFactory factory) {
		this.repository = repository;
		this.factory = factory;
	}

	/**
	 * Decode the given string into a {@link org.openrdf.model.Value}.
	 * 
	 * @param string
	 *        representation of an RDF value
	 * @return the parsed value, or null if the string is null, empty, only
	 *         whitespace, or {@link java.lang.String#equals(Object)} "null".
	 * @throws BadRequestException
	 *         if a problem occurs during parsing
	 */
	protected Value decodeValue(String string)
		throws BadRequestException
	{
		Value result = null;
		try {
			if (string != null) {
				String value = string.trim();
				if (!value.isEmpty() && !"null".equals(value)) {
					if (value.startsWith("_:")) {
						String label = value.substring("_:".length());
						result = factory.createBNode(label);
					}
					else {
						if (value.charAt(0) == '<' && value.endsWith(">")) {
							result = factory.createURI(value.substring(1, value.length() - 1));
						}
						else {
							if (value.charAt(0) == '"') {
								result = parseLiteral(value);
							}
							else {
								result = parseURI(value);
							}
						}
					}
				}
			}
		}
		catch (Exception exc) {
			LOGGER.warn(exc.toString(), exc);
			throw new BadRequestException("Malformed value: " + string, exc);
		}
		return result;
	}

	private Value parseURI(String value)
		throws RepositoryException, BadRequestException
	{
		String prefix = value.substring(0, value.indexOf(':'));
		String localPart = value.substring(prefix.length() + 1);
		String namespace = getNamespace(prefix);
		if (namespace == null) {
			throw new BadRequestException("Undefined prefix: " + value);
		}
		return factory.createURI(namespace, localPart);
	}

	private Value parseLiteral(String value)
		throws BadRequestException
	{
		String label = value.substring(1, value.lastIndexOf('"'));
		Value result;
		if (value.length() == (label.length() + 2)) {
			result = factory.createLiteral(label);
		}
		else {
			String rest = value.substring(label.length() + 2);
			if (rest.startsWith("^^")) {
				Value datatype = decodeValue(rest.substring(2));
				if (datatype instanceof URI) {
					result = factory.createLiteral(label, (URI)datatype);
				}
				else {
					throw new BadRequestException("Malformed datatype: " + value);
				}
			}
			else if (rest.charAt(0) == '@') {
				result = factory.createLiteral(label, rest.substring(1));
			}
			else {
				throw new BadRequestException("Malformed language tag or datatype: " + value);
			}
		}
		return result;
	}

	private String getNamespace(String prefix)
		throws RepositoryException
	{
		RepositoryConnection con = repository.getConnection();
		try {
			return con.getNamespace(prefix);
		}
		finally {
			con.close();
		}
	}

}
