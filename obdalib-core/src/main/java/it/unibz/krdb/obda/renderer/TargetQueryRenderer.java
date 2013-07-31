/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.renderer;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A utility class to render a Target Query object into its representational
 * string.
 */
public class TargetQueryRenderer {

	/**
	 * Transforms the given <code>OBDAQuery</code> into a string. The method requires
	 * a prefix manager to shorten full IRI name.
	 */
	public static String encode(OBDAQuery input, PrefixManager prefixManager) {
		if (!(input instanceof CQIE)) {
			return "";
		}
		TurtleWriter turtleWriter = new TurtleWriter();
		List<Function> body = ((CQIE) input).getBody();
		for (Function atom : body) {
			String subject, predicate, object = "";
			String originalString = atom.getFunctionSymbol().toString();
			if (isUnary(atom)) {
				Term subjectTerm = atom.getTerm(0);
				subject = getDisplayName(subjectTerm, prefixManager);
				predicate = "a";
				object = getAbbreviatedName(originalString, prefixManager, false);
				if (originalString.equals(object)) {
					object = "<" + object + ">";
				}
			} else {
				Term subjectTerm = atom.getTerm(0);
				subject = getDisplayName(subjectTerm, prefixManager);
				predicate = getAbbreviatedName(originalString, prefixManager, false);
				if (originalString.equals(predicate)) {
					predicate = "<" + predicate + ">";
				}
				Term objectTerm = atom.getTerm(1);
				object = getDisplayName(objectTerm, prefixManager);
			}
			turtleWriter.put(subject, predicate, object);
		}
		return turtleWriter.print();
	}

	/**
	 * Checks if the atom is unary or not.
	 */
	private static boolean isUnary(Function atom) {
		return atom.getArity() == 1 ? true : false;
	}

	/**
	 * Prints the short form of the predicate (by omitting the complete URI and
	 * replacing it by a prefix name).
	 * 
	 * Note that by default this method will consider a set of predefined
	 * prefixes, i.e., rdf:, rdfs:, owl:, xsd: and quest: To support this
	 * prefixes the method will temporally add the prefixes if they dont exist
	 * already, taken care to remove them if they didn't exist.
	 * 
	 * The implementation requires at the moment, the implementation requires
	 * cloning the existing prefix manager, and hence this is highly inefficient
	 * method. *
	 */
	private static String getAbbreviatedName(String uri, PrefixManager pm, boolean insideQuotes) {
		// Cloning the existing manager
		PrefixManager prefManClone = new SimplePrefixManager();
		Map<String,String> currentMap = pm.getPrefixMap();
		for (String prefix: currentMap.keySet()) {
			prefManClone.addPrefix(prefix, pm.getURIDefinition(prefix));
		}
		boolean containsXSDPrefix = prefManClone.contains(OBDAVocabulary.PREFIX_XSD);
		boolean containsRDFPrefix = prefManClone.contains(OBDAVocabulary.PREFIX_RDF);
		boolean containsRDFSPrefix = prefManClone.contains(OBDAVocabulary.PREFIX_RDFS);
		boolean containsOWLPrefix = prefManClone.contains(OBDAVocabulary.PREFIX_OWL);
		boolean containsQUESTPrefix = prefManClone.contains(OBDAVocabulary.PREFIX_QUEST);

		if (!containsXSDPrefix) {
			prefManClone.addPrefix(OBDAVocabulary.PREFIX_XSD, OBDAVocabulary.NS_XSD);
		}
		if (!containsRDFPrefix) {
			prefManClone.addPrefix(OBDAVocabulary.PREFIX_RDF, OBDAVocabulary.NS_RDF);
		}
		if (!containsRDFSPrefix) {
			prefManClone.addPrefix(OBDAVocabulary.PREFIX_RDFS, OBDAVocabulary.NS_RDFS);
		}
		if (!containsOWLPrefix) {
			prefManClone.addPrefix(OBDAVocabulary.PREFIX_OWL, OBDAVocabulary.NS_OWL);
		}
		if (!containsQUESTPrefix) {
			prefManClone.addPrefix(OBDAVocabulary.PREFIX_QUEST, OBDAVocabulary.NS_QUEST);
		}
		return prefManClone.getShortForm(uri, insideQuotes);
	}

	/**
	 * Prints the text representation of different terms.
	 */
	private static String getDisplayName(Term term, PrefixManager prefixManager) {
		StringBuilder sb = new StringBuilder();
		if (term instanceof FunctionalTermImpl) {
			FunctionalTermImpl function = (FunctionalTermImpl) term;
			Predicate functionSymbol = function.getFunctionSymbol();
			String fname = getAbbreviatedName(functionSymbol.toString(), prefixManager, false);
			if (functionSymbol instanceof DataTypePredicate) {
				// if the function symbol is a data type predicate
				if (isLiteralDataType(functionSymbol)) {
					// if it is rdfs:Literal
					int arity = function.getArity();
					if (arity == 1) {
						// without the language tag
						Term var = function.getTerms().get(0);
						sb.append(getDisplayName(var, prefixManager));
						sb.append("^^rdfs:Literal");
					} else if (arity == 2) {
						// with the language tag
						Term var = function.getTerms().get(0);
						Term lang = function.getTerms().get(1);
						sb.append(getDisplayName(var, prefixManager));
						sb.append("@");
						if (lang instanceof ValueConstant) {
							// Don't pass this to getDisplayName() because 
							// language constant is not written as @"lang-tag"
							sb.append(((ValueConstant) lang).getValue());
						} else {
							sb.append(getDisplayName(lang, prefixManager));
						}
					}
				} else { // for the other data types
					Term var = function.getTerms().get(0);
					sb.append(getDisplayName(var, prefixManager));
					sb.append("^^");
					sb.append(fname);
				}
			} else if (functionSymbol instanceof URITemplatePredicate) {
				String template = ((ValueConstant) function.getTerms().get(0)).getValue();
				
				// Utilize the String.format() method so we replaced placeholders '{}' with '%s'
				String templateFormat = template.replace("{}", "%s");
				List<String> varNames = new ArrayList<String>();
				for (Term innerTerm : function.getTerms()) {
					if (innerTerm instanceof Variable) {
						varNames.add(getDisplayName(innerTerm, prefixManager));
					}
				}
				String originalUri = String.format(templateFormat, varNames.toArray());
				String shortenUri = getAbbreviatedName(originalUri, prefixManager, false); // shorten the URI if possible
				if (!shortenUri.equals(originalUri)) {
					sb.append(shortenUri);
				} else {
					// If the URI can't be shorten then use the full URI within brackets
					sb.append("<");
					sb.append(originalUri);
					sb.append(">");
				}				
			} else { // for any ordinary function symbol
				sb.append(fname);
				sb.append("(");
				boolean separator = false;
				for (Term innerTerm : function.getTerms()) {
					if (separator) {
						sb.append(", ");
					}
					sb.append(getDisplayName(innerTerm, prefixManager));
					separator = true;
				}
				sb.append(")");
			}
		} else if (term instanceof Variable) {
			sb.append("{");
			sb.append(((Variable) term).getName());
			sb.append("}");
		} else if (term instanceof URIConstant) {
			String originalUri = term.toString();
			String shortenUri = getAbbreviatedName(originalUri, prefixManager, false); // shorten the URI if possible
			if (!shortenUri.equals(originalUri)) {
				sb.append(shortenUri);
			} else {
				// If the URI can't be shorten then use the full URI within brackets
				sb.append("<");
				sb.append(originalUri);
				sb.append(">");
			}
		} else if (term instanceof ValueConstant) {
			sb.append("\"");
			sb.append(((ValueConstant) term).getValue());
			sb.append("\"");
		}
		return sb.toString();
	}

	private static boolean isLiteralDataType(Predicate predicate) {
		return predicate.equals(OBDAVocabulary.RDFS_LITERAL) || predicate.equals(OBDAVocabulary.RDFS_LITERAL_LANG);
	}

	private TargetQueryRenderer() {
		// Prevent initialization
	}
}
