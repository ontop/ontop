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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * A {@link Function} that tries to cast its argument to an
 * <tt>xsd:dateTime</tt>.
 * 
 * @author Arjohn Kampman
 */
public class DateTimeCast implements Function {

	public String getURI() {
		return XMLSchema.DATETIME.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("xsd:dateTime cast requires exactly 1 argument, got "
					+ args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];
			URI datatype = literal.getDatatype();

			if (QueryEvaluationUtil.isStringLiteral(literal)) {
				String dateTimeValue = XMLDatatypeUtil.collapseWhiteSpace(literal.getLabel());
				if (XMLDatatypeUtil.isValidDateTime(dateTimeValue)) {
					return valueFactory.createLiteral(dateTimeValue, XMLSchema.DATETIME);
				}
			}
			else if (datatype != null) {
				if (datatype.equals(XMLSchema.DATETIME)) {
					return literal;
				}
				if (datatype.equals(XMLSchema.DATE)) {
					// If ST is xs:date, then let SYR be eg:convertYearToString(
					// fn:year-from-date( SV )), let SMO be eg:convertTo2CharString(
					// fn:month-from-date( SV )), let SDA be eg:convertTo2CharString(
					// fn:day-from-date( SV )) and let STZ be eg:convertTZtoString(
					// fn:timezone-from-date( SV )); TV is xs:dateTime( fn:concat(
					// SYR , '-', SMO , '-', SDA , 'T00:00:00 ', STZ ) ).
					try {
						XMLGregorianCalendar calValue = literal.calendarValue();

						int year = calValue.getYear();
						int month = calValue.getMonth();
						int day = calValue.getDay();
						int timezoneOffset = calValue.getTimezone();

						if (DatatypeConstants.FIELD_UNDEFINED != year && DatatypeConstants.FIELD_UNDEFINED != month
								&& DatatypeConstants.FIELD_UNDEFINED != day)
						{
							StringBuilder dtBuilder = new StringBuilder();
							dtBuilder.append(year);
							dtBuilder.append("-");
							if (month < 10) {
								dtBuilder.append("0");
							}
							dtBuilder.append(month);
							dtBuilder.append("-");
							if (day < 10) {
								dtBuilder.append("0");
							}
							dtBuilder.append(day);
							dtBuilder.append("T00:00:00");
							if (DatatypeConstants.FIELD_UNDEFINED != timezoneOffset) {
								int minutes = Math.abs(timezoneOffset);
								int hours = minutes / 60;
								minutes = minutes - (hours * 60);
								if (timezoneOffset > 0) {
									dtBuilder.append("+");
								}
								else {
									dtBuilder.append("-");
								}
								if (hours < 10) {
									dtBuilder.append("0");
								}
								dtBuilder.append(hours);
								dtBuilder.append(":");
								if (minutes < 10) {
									dtBuilder.append("0");
								}
								dtBuilder.append(minutes);
							}
							
							return valueFactory.createLiteral(dtBuilder.toString(), XMLSchema.DATETIME);
						}
						else {
							throw new ValueExprEvaluationException("not a valid date value: " + literal);
						}
					}
					catch (IllegalArgumentException e) {
						throw new ValueExprEvaluationException("not a valid calendar value: " + literal);
					}
				}
			}
		}

		throw new ValueExprEvaluationException("Invalid argument for xsd:dateTime cast: " + args[0]);
	}
}
