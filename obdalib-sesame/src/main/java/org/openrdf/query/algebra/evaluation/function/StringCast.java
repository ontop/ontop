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
package org.openrdf.query.algebra.evaluation.function;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * A {@link Function} that tries to cast its argument to an <tt>xsd:string</tt>.
 * 
 * @author Arjohn Kampman
 */
public class StringCast implements Function {

	public String getURI() {
		return XMLSchema.STRING.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("xsd:string cast requires exactly 1 argument, got " + args.length);
		}

		Value value = args[0];
		if (value instanceof URI) {
			return valueFactory.createLiteral(value.toString(), XMLSchema.STRING);
		}
		else if (value instanceof Literal) {
			Literal literal = (Literal)value;
			URI datatype = literal.getDatatype();

			if (QueryEvaluationUtil.isSimpleLiteral(literal)) {
				return valueFactory.createLiteral(literal.getLabel(), XMLSchema.STRING);
			}
			else if (datatype != null) {
				if (datatype.equals(XMLSchema.STRING)) {
					return literal;
				}
				else if (XMLDatatypeUtil.isNumericDatatype(datatype) || datatype.equals(XMLSchema.BOOLEAN)
						|| datatype.equals(XMLSchema.DATETIME))
				{
					// FIXME Slightly simplified wrt the spec, we just always use the canonical value of the
					// source literal as the target lexical value. This is not 100% compliant with handling of 
					// some date-related datatypes. 
					// 
					// See http://www.w3.org/TR/xpath-functions/#casting-from-primitive-to-primitive
					if (XMLDatatypeUtil.isValidValue(literal.getLabel(), datatype)) {
						String normalizedValue = XMLDatatypeUtil.normalize(literal.getLabel(), datatype);
						return valueFactory.createLiteral(normalizedValue, XMLSchema.STRING);
					}
					else {
						return valueFactory.createLiteral(literal.getLabel(), XMLSchema.STRING);
					}
				}
				else {
					// for unknown datatypes, just use the lexical value.
					return valueFactory.createLiteral(literal.getLabel(), XMLSchema.STRING);
				}
			}
		}

		throw new ValueExprEvaluationException("Invalid argument for xsd:string cast: " + value);
	}
}
