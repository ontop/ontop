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
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} STRDT, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-strdt">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class StrDt implements Function {

	public String getURI() {
		return "STRDT";
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 2) {
			throw new ValueExprEvaluationException("STRDT requires 2 arguments, got "
					+ args.length);
		}

		Value lexicalValue = args[0];
		Value datatypeValue = args[1];
		
		if (QueryEvaluationUtil.isSimpleLiteral(lexicalValue)) {
			Literal lit = (Literal)lexicalValue;
			if (datatypeValue instanceof URI) {
				return valueFactory.createLiteral(lit.getLabel(), (URI)datatypeValue);
			}
			else {
				throw new ValueExprEvaluationException("illegal value for operand: " + datatypeValue);
			}
		}
		else {
			throw new ValueExprEvaluationException("illegal value for operand: " + lexicalValue);
		}
	}

}
