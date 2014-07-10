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
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} STRBEFORE, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-substr">SPARQL Query Language
 * for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class StrBefore implements Function {

	public String getURI() {
		return FN.SUBSTRING_BEFORE.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 2) {
			throw new ValueExprEvaluationException("Incorrect number of arguments for STRBEFORE: " + args.length);
		}

		Value leftArg = args[0];
		Value rightArg = args[1];

		if (leftArg instanceof Literal && rightArg instanceof Literal) {
			Literal leftLit = (Literal)leftArg;
			Literal rightLit = (Literal)rightArg;

			
			if (QueryEvaluationUtil.compatibleArguments(leftLit, rightLit))
			{
				String leftLanguage = leftLit.getLanguage();
				URI leftDt = leftLit.getDatatype();

				String lexicalValue = leftLit.getLabel();
				String substring = rightLit.getLabel();
				
				int index = lexicalValue.indexOf(substring);

				String substringBefore = "";
				if (index > -1) {
					substringBefore = lexicalValue.substring(0, index);
				}
				else {
					// no match, return empty string with no language or datatype
					leftLanguage = null;
					leftDt = null;
				}
				
				if (leftLanguage != null) {
					return valueFactory.createLiteral(substringBefore, leftLanguage);
				}
				else if (leftDt != null) {
					return valueFactory.createLiteral(substringBefore, leftDt);
				}
				else {
					return valueFactory.createLiteral(substringBefore);
				}
			}
			else {
				throw new ValueExprEvaluationException("incompatible operands for STRBEFORE: " + leftArg + ", " + rightArg);
			}
		}
		else {
			throw new ValueExprEvaluationException("incompatible operands for STRBEFORE: " + leftArg + ", " + rightArg);
		}
	}
}
