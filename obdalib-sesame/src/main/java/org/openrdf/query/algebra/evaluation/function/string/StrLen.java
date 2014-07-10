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

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} STRLEN, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-strlen">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class StrLen implements Function {

	public String getURI() {
		return FN.STRING_LENGTH.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("STRLEN requires 1 argument, got " + args.length);
		}

		Value argValue = args[0];
		if (argValue instanceof Literal) {
			Literal literal = (Literal)argValue;

			// strlen function accepts only string literals 
			if (QueryEvaluationUtil.isStringLiteral(literal)) {

				// TODO we jump through some hoops here to get an xsd:integer
				// literal. Shouldn't createLiteral(int) return an xsd:integer
				// rather than an xsd:int?
				Integer length = literal.getLabel().length();
				return valueFactory.createLiteral(length.toString(), XMLSchema.INTEGER);
			}
			else {
				throw new ValueExprEvaluationException("unexpected input value for strlen function: " + argValue);
			}
		}
		else {
			throw new ValueExprEvaluationException("unexpected input value for strlen function: " + argValue);
		}
	}

}
