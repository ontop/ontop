package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;
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
			return String.format("$%s", variable.getName());
		} 
		else if (term instanceof ValueConstant) {
			ValueConstant constant = (ValueConstant) term;
			return String.format("\"%s\"", constant.getValue());
		}
		else if (term instanceof URIConstant) {
			URIConstant constant = (URIConstant) term;
			return String.format("<%s>", constant.getURI());
		} 
		else if (term instanceof Function) {
			Function function = (Function) term;
			Predicate functionSymbol = function.getFunctionSymbol();
			
			StringBuffer args = new StringBuffer();
			boolean separator = false;
			for (Term innerTerm : function.getTerms()) {
				if (separator) {
					args.append(", ");
				}
				args.append(toString(innerTerm));
				separator = true;
			}
			return String.format("%s(%s)", functionSymbol.toString(), args.toString());
		}
		return term.toString(); // for other unknown term
	}
}
