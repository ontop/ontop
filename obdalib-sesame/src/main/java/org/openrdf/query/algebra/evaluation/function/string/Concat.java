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

/**
 * The SPARQL built-in {@link Function} CONCAT, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-concat">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class Concat implements Function {

	public String getURI() {
		return FN.CONCAT.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length == 0) {
			throw new ValueExprEvaluationException("CONCAT requires at least 1 argument, got " + args.length);
		}

		StringBuilder concatBuilder = new StringBuilder();
		String languageTag = null;

		boolean useLanguageTag = true;
		boolean useDatatype = true;

		for (Value arg : args) {
			if (arg instanceof Literal) {
				Literal lit = (Literal)arg;

				// verify that every literal argument has the same language tag. If
				// not, the operator result should not use a language tag.
				if (useLanguageTag && lit.getLanguage() != null) {
					if (languageTag == null) {
						languageTag = lit.getLanguage();
					}
					else if (!languageTag.equals(lit.getLanguage())) {
						languageTag = null;
						useLanguageTag = false;
					}
				}
				else {
					useLanguageTag = false;
				}

				// check datatype: concat only expects plain, language-tagged or
				// string-typed literals. If all arguments are of type xsd:string,
				// the result also should be,
				// otherwise the result will not have a datatype.
				if (lit.getDatatype() == null) {
					useDatatype = false;
				}
				else if (!lit.getDatatype().equals(XMLSchema.STRING)) {
					throw new ValueExprEvaluationException("unexpected data type for concat operand: " + arg);
				}

				concatBuilder.append(lit.getLabel());
			}
			else {
				throw new ValueExprEvaluationException("unexpected argument type for concat operator: " + arg);
			}
		}

		Literal result = null;

		if (useDatatype) {
			result = valueFactory.createLiteral(concatBuilder.toString(), XMLSchema.STRING);
		}
		else if (useLanguageTag) {
			result = valueFactory.createLiteral(concatBuilder.toString(), languageTag);
		}
		else {
			result = valueFactory.createLiteral(concatBuilder.toString());
		}

		return result;

	}

}
