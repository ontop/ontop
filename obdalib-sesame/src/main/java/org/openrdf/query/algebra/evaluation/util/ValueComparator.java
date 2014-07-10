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
package org.openrdf.query.algebra.evaluation.util;

import java.util.Comparator;

import info.aduna.lang.ObjectUtil;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * A comparator that compares values according the SPARQL value ordering as
 * specified in <A
 * href="http://www.w3.org/TR/rdf-sparql-query/#modOrderBy">SPARQL Query
 * Language for RDF</a>.
 * 
 * @author james
 * @author Arjohn Kampman
 */
public class ValueComparator implements Comparator<Value> {

	public int compare(Value o1, Value o2) {
		// check equality
		if (ObjectUtil.nullEquals(o1, o2)) {
			return 0;
		}

		// 1. (Lowest) no value assigned to the variable
		if (o1 == null) {
			return -1;
		}
		if (o2 == null) {
			return 1;
		}

		// 2. Blank nodes
		boolean b1 = o1 instanceof BNode;
		boolean b2 = o2 instanceof BNode;
		if (b1 && b2) {
			return compareBNodes((BNode)o1, (BNode)o2);
		}
		if (b1) {
			return -1;
		}
		if (b2) {
			return 1;
		}

		// 3. IRIs
		boolean u1 = o1 instanceof URI;
		boolean u2 = o2 instanceof URI;
		if (u1 && u2) {
			return compareURIs((URI)o1, (URI)o2);
		}
		if (u1) {
			return -1;
		}
		if (u2) {
			return 1;
		}

		// 4. RDF literals
		return compareLiterals((Literal)o1, (Literal)o2);
	}

	private int compareBNodes(BNode leftBNode, BNode rightBNode) {
		return leftBNode.getID().compareTo(rightBNode.getID());
	}

	private int compareURIs(URI leftURI, URI rightURI) {
		return leftURI.toString().compareTo(rightURI.toString());
	}

	private int compareLiterals(Literal leftLit, Literal rightLit) {
		// Additional constraint for ORDER BY: "A plain literal is lower
		// than an RDF literal with type xsd:string of the same lexical
		// form."
		if (!(QueryEvaluationUtil.isPlainLiteral(leftLit) || QueryEvaluationUtil.isPlainLiteral(rightLit))) {
			try {
				boolean isSmaller = QueryEvaluationUtil.compareLiterals(leftLit, rightLit, CompareOp.LT);

				if (isSmaller) {
					return -1;
				}
				else {
					return 1;
				}
			}
			catch (ValueExprEvaluationException e) {
				// literals cannot be compared using the '<' operator, continue
				// below
			}
		}

		int result = 0;

		// Sort by datatype first, plain literals come before datatyped literals
		URI leftDatatype = leftLit.getDatatype();
		URI rightDatatype = rightLit.getDatatype();

		if (leftDatatype != null) {
			if (rightDatatype != null) {
				// Both literals have datatypes
				result = compareDatatypes(leftDatatype, rightDatatype);
			}
			else {
				result = 1;
			}
		}
		else if (rightDatatype != null) {
			result = -1;
		}

		if (result == 0) {
			// datatypes are equal or both literals are untyped; sort by language
			// tags, simple literals come before literals with language tags
			String leftLanguage = leftLit.getLanguage();
			String rightLanguage = rightLit.getLanguage();

			if (leftLanguage != null) {
				if (rightLanguage != null) {
					result = leftLanguage.compareTo(rightLanguage);
				}
				else {
					result = 1;
				}
			}
			else if (rightLanguage != null) {
				result = -1;
			}
		}

		if (result == 0) {
			// Literals are equal as fas as their datatypes and language tags are
			// concerned, compare their labels
			result = leftLit.getLabel().compareTo(rightLit.getLabel());
		}

		return result;
	}

	/**
	 * Compares two literal datatypes and indicates if one should be ordered
	 * after the other. This algorithm ensures that compatible ordered datatypes
	 * (numeric and date/time) are grouped together so that
	 * {@link QueryEvaluationUtil#compareLiterals(Literal, Literal, CompareOp)}
	 * is used in consecutive ordering steps.
	 */
	private int compareDatatypes(URI leftDatatype, URI rightDatatype) {
		if (XMLDatatypeUtil.isNumericDatatype(leftDatatype)) {
			if (XMLDatatypeUtil.isNumericDatatype(rightDatatype)) {
				// both are numeric datatypes
				return compareURIs(leftDatatype, rightDatatype);
			}
			else {
				return -1;
			}
		}
		else if (XMLDatatypeUtil.isNumericDatatype(rightDatatype)) {
			return 1;
		}
		else if (XMLDatatypeUtil.isCalendarDatatype(leftDatatype)) {
			if (XMLDatatypeUtil.isCalendarDatatype(rightDatatype)) {
				// both are calendar datatypes
				return compareURIs(leftDatatype, rightDatatype);
			}
			else {
				return -1;
			}
		}
		else if (XMLDatatypeUtil.isCalendarDatatype(rightDatatype)) {
			return 1;
		}
		else {
			// incompatible or unordered datatypes
			return compareURIs(leftDatatype, rightDatatype);
		}
	}
}