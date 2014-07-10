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
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} CONTAINS, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-contains">SPARQL Query
 * Language for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class Contains implements Function {

	public String getURI() {
		return FN.CONTAINS.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 2) {
			throw new ValueExprEvaluationException("CONTAINS requires 2 arguments, got " + args.length);
		}
		Value leftVal = args[0];
		Value rightVal = args[1];

		if (leftVal instanceof Literal && rightVal instanceof Literal) {
			Literal leftLit = (Literal)leftVal;
			Literal rightLit = (Literal)rightVal;

			if (leftLit.getLanguage() != null) {
				if (rightLit.getLanguage() == null || rightLit.getLanguage().equals(leftLit.getLanguage())) {

					String leftLexVal = leftLit.getLabel();
					String rightLexVal = rightLit.getLabel();

					return BooleanLiteralImpl.valueOf(leftLexVal.contains(rightLexVal));
				}
				else {
					throw new ValueExprEvaluationException("incompatible operands for CONTAINS function");
				}
			}
			else if (QueryEvaluationUtil.isStringLiteral(leftLit)) {
				if (QueryEvaluationUtil.isStringLiteral(rightLit)) {
					String leftLexVal = leftLit.getLabel();
					String rightLexVal = rightLit.getLabel();

					return BooleanLiteralImpl.valueOf(leftLexVal.contains(rightLexVal));
				}
				else {
					throw new ValueExprEvaluationException("incompatible operands for CONTAINS function");
				}
			}
			else {
				throw new ValueExprEvaluationException("incompatible operands for CONTAINS function");
			}
		}
		else {
			throw new ValueExprEvaluationException("CONTAINS function expects literal operands");
		}

	}

}
