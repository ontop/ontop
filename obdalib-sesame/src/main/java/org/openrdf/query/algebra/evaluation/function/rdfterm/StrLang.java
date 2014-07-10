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
package org.openrdf.query.algebra.evaluation.function.rdfterm;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} STRLANG, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-strlang">SPARQL Query
 * Language for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class StrLang implements Function {

	public String getURI() {
		return "STRLANG";
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 2) {
			throw new ValueExprEvaluationException("STRLANG requires 2 arguments, got " + args.length);
		}

		Value lexicalValue = args[0];
		Value languageValue = args[1];

		if (QueryEvaluationUtil.isSimpleLiteral(lexicalValue)) {
			Literal lit = (Literal)lexicalValue;

			if (languageValue instanceof Literal) {
				return valueFactory.createLiteral(lit.getLabel(), ((Literal)languageValue).getLabel());
			}
			else {
				throw new ValueExprEvaluationException("illegal value for operand: " + languageValue);
			}
		}
		else {
			throw new ValueExprEvaluationException("illegal value for operand: " + lexicalValue);
		}

	}

}
