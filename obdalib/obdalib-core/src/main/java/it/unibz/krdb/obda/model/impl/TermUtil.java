package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;

/**
 * A utility class to handle atom terms.
 */
public class TermUtil {

	public static String toString(NewLiteral term) {
		if (term instanceof Variable) {
			Variable variable = (Variable) term;
			return String.format("%s", variable.getName());
		} 
		else if (term instanceof ValueConstant) {
			ValueConstant constant = (ValueConstant) term;
			StringBuffer bf = new StringBuffer();
			bf.append(String.format("\"%s\"", constant.getValue()));
			
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
			return String.format("%s", constant.getValue());
		} 
		else if (term instanceof Function) {
			Function function = (Function) term;
			Predicate functionSymbol = function.getFunctionSymbol();
			
			StringBuffer args = new StringBuffer();
			boolean separator = false;
			for (NewLiteral innerTerm : function.getTerms()) {
				if (separator) {
					args.append(", ");
				}
				args.append(toString(innerTerm));
				separator = true;
			}
			return String.format("%s(%s)", functionSymbol.toString(), args.toString());
		}
		else if (term instanceof BNode) {
			BNode bnode = (BNode) term;
			return bnode.getName();
		}
		return term.toString(); // for other unknown term
	}
}
