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
			StringBuilder sb = new StringBuilder();
			
			String value = constant.getValue();
			switch (constant.getType()) {
				case STRING:
				case DATETIME: sb.append(quoted(value)); break;
				case INTEGER:
				case DECIMAL:
				case DOUBLE:
				case BOOLEAN: sb.append(value); break;
				case LITERAL:
				case LITERAL_LANG:
					String lang = constant.getLanguage();
					if (lang != null && !lang.isEmpty()) {
						value += "@" + lang;
					}
					sb.append(quoted(value)); break;
				default:
					sb.append(value);
			}
			return sb.toString();
		}
		else if (term instanceof URIConstant) {
			URIConstant constant = (URIConstant) term;
			return "<" + constant.getValue() + ">";
		} 
		else if (term instanceof Function) {
			Function function = (Function) term;
			Predicate functionSymbol = function.getFunctionSymbol();
			
			StringBuilder sb = new StringBuilder();
			sb.append(functionSymbol.toString());
			sb.append("(");
			boolean separator = false;
			for (Term innerTerm : function.getTerms()) {
				if (separator) {
					sb.append(",");
				}
				sb.append(toString(innerTerm));
				separator = true;
			}
			sb.append(")");
			return sb.toString();
		}
		else if (term instanceof BNode) {
			BNode bnode = (BNode) term;			
			return bnode.getName();
		}
		return term.toString(); // for other unknown term
	}

	private static String quoted(String value) {
		return "\"" + value + "\"";
	}
}
