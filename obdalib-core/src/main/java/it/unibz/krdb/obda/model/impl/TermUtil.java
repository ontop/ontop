/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;

/**
 * A utility class to handle atom terms.
 */
public class TermUtil {

	public static String toString(Term term) {
		if (term instanceof Variable) {
			Variable variable = (Variable) term;
			return variable.getName();
		} 
		else if (term instanceof ValueConstant) {
			ValueConstant constant = (ValueConstant) term;
			StringBuilder bf = new StringBuilder();
			bf.append("\"");
			bf.append(constant.getValue());
			bf.append("\"");
			
			
		    final COL_TYPE datatype = constant.getType();
			if (datatype == COL_TYPE.LITERAL_LANG) {
				bf.append("@");
				bf.append(constant.getLanguage());
			} else if (datatype == COL_TYPE.LITERAL) { 
				// NO-OP
		    } else {
				bf.append("^^");
				bf.append(datatype);
			}
			return bf.toString();
		}
		else if (term instanceof URIConstant) {
			URIConstant constant = (URIConstant) term;
			return constant.getValue();
		} 
		else if (term instanceof Function) {
			Function function = (Function) term;
			Predicate functionSymbol = function.getFunctionSymbol();
			
			StringBuilder args = new StringBuilder();
			args.append(functionSymbol.toString());
			args.append("(");
			boolean separator = false;
			for (Term innerTerm : function.getTerms()) {
				if (separator) {
					args.append(", ");
				}
				args.append(toString(innerTerm));
				separator = true;
			}
			args.append(")");
			return args.toString();
		}
		else if (term instanceof BNode) {
			BNode bnode = (BNode) term;
			return bnode.getName();
		}
		return term.toString(); // for other unknown term
	}
}
