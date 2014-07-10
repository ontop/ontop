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
package org.openrdf.query.algebra.evaluation.function.string;

import java.io.UnsupportedEncodingException;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} ENCODE_FOR_URI, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-encode">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 * @author Arjohn Kampman
 */
public class EncodeForUri implements Function {

	public String getURI() {
		return FN.ENCODE_FOR_URI.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("ENCODE_FOR_URI requires exactly 1 argument, got "
					+ args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];

			if (QueryEvaluationUtil.isStringLiteral(literal)) {
				String lexValue = literal.getLabel();

				return valueFactory.createLiteral(encodeUri(lexValue));
			}
			else {
				throw new ValueExprEvaluationException("Invalid argument for ENCODE_FOR_URI: " + literal);
			}
		}
		else {
			throw new ValueExprEvaluationException("Invalid argument for ENCODE_FOR_URI: " + args[0]);
		}
	}

	private String encodeUri(String uri) {
				
		StringBuilder buf = new StringBuilder(uri.length() * 2);

		int uriLen = uri.length();
		for (int i = 0; i < uriLen; i++) {
			char c = uri.charAt(i);

			if (isUnreserved(c)) {
				buf.append(c);
			}
			else {
				// use UTF-8 hex encoding for character.
				try {
					byte[] utf8 = Character.toString(c).getBytes("UTF-8");
					
					for (byte b: utf8) {
						// Escape character
						buf.append('%');
						
						char cb = (char)(b & 0xFF);
						
						String hexVal = Integer.toHexString(cb).toUpperCase();

						// Ensure use of two characters
						if (hexVal.length() == 1) {
							buf.append('0');
						}

						buf.append(hexVal);
					}
						
				}
				catch (UnsupportedEncodingException e) {
					// UTF-8 is always supported
					throw new RuntimeException(e);
				}
			}
		}

		return buf.toString();
	}

	private final boolean isUnreserved(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '-' || c == '.'
				|| c == '_' || c == '~';
	}
}
