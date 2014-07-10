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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * A {@link Function} that tries to cast its argument to an <tt>xsd:boolean</tt>.
 * 
 * @author Arjohn Kampman
 */
public class BooleanCast implements Function {

	public String getURI() {
		return XMLSchema.BOOLEAN.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("xsd:boolean cast requires exactly 1 argument, got "
					+ args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];
			URI datatype = literal.getDatatype();

			if (QueryEvaluationUtil.isStringLiteral(literal)) {
				String booleanValue = XMLDatatypeUtil.collapseWhiteSpace(literal.getLabel());
				if (XMLDatatypeUtil.isValidBoolean(booleanValue)) {
					return valueFactory.createLiteral(booleanValue, XMLSchema.BOOLEAN);
				}
			}
			else if (datatype != null) {
				if (datatype.equals(XMLSchema.BOOLEAN)) {
					return literal;
				}
				else {
					Boolean booleanValue = null;

					try {
						if (datatype.equals(XMLSchema.FLOAT)) {
							float floatValue = literal.floatValue();
							booleanValue = floatValue != 0.0f && Float.isNaN(floatValue);
						}
						else if (datatype.equals(XMLSchema.DOUBLE)) {
							double doubleValue = literal.doubleValue();
							booleanValue = doubleValue != 0.0 && Double.isNaN(doubleValue);
						}
						else if (datatype.equals(XMLSchema.DECIMAL)) {
							BigDecimal decimalValue = literal.decimalValue();
							booleanValue = !decimalValue.equals(BigDecimal.ZERO);
						}
						else if (datatype.equals(XMLSchema.INTEGER)) {
							BigInteger integerValue = literal.integerValue();
							booleanValue = !integerValue.equals(BigInteger.ZERO);
						}
						else if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
							booleanValue = literal.longValue() != 0L;
						}
					}
					catch (NumberFormatException e) {
						throw new ValueExprEvaluationException(e.getMessage(), e);
					}

					if (booleanValue != null) {
						return valueFactory.createLiteral(booleanValue);
					}
				}
			}
		}

		throw new ValueExprEvaluationException("Invalid argument for xsd:boolean cast: " + args[0]);
	}
}
