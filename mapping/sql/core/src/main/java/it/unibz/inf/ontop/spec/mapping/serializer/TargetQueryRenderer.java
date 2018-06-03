package it.unibz.inf.ontop.spec.mapping.serializer;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to render a Target Query object into its representational
 * string.
 */
public class TargetQueryRenderer {

	/**
	 * Transforms the given <code>OBDAQuery</code> into a string. The method requires
	 * a prefix manager to shorten full IRI name.
	 */
	public static String encode(ImmutableList<TargetAtom> body, PrefixManager prefixManager) {

		TurtleWriter turtleWriter = new TurtleWriter();
		for (TargetAtom atom : body) {
			String subject, predicate, object = "";
			String originalString = atom.getProjectionAtom().getPredicate().toString();
			if (isUnary(atom.getProjectionAtom())) {
				ImmutableTerm subjectTerm = atom.getSubstitutedTerm(0);
				subject = getDisplayName(subjectTerm, prefixManager);
				predicate = "a";
				object = getAbbreviatedName(originalString, prefixManager, false);
				if (originalString.equals(object)) {
					object = "<" + object + ">";
				}
			}
			else if (originalString.equals("triple")) {
                ImmutableTerm subjectTerm = atom.getSubstitutedTerm(0);
                subject = getDisplayName(subjectTerm, prefixManager);
                ImmutableTerm predicateTerm = atom.getSubstitutedTerm(1);
                predicate = getDisplayName(predicateTerm, prefixManager);
                ImmutableTerm objectTerm = atom.getSubstitutedTerm(2);
                object = getDisplayName(objectTerm, prefixManager);
			}
			else {
                ImmutableTerm subjectTerm = atom.getSubstitutedTerm(0);
				subject = getDisplayName(subjectTerm, prefixManager);
				predicate = getAbbreviatedName(originalString, prefixManager, false);
				if (originalString.equals(predicate)) {
					predicate = "<" + predicate + ">";
				}
                ImmutableTerm objectTerm = atom.getSubstitutedTerm(1);
				object = getDisplayName(objectTerm, prefixManager);
			}
			turtleWriter.put(subject, predicate, object);
		}
		return turtleWriter.print();
	}

    /**
     * Checks if the atom is unary or not.
     */
    private static boolean isUnary(DataAtom atom) {
        return atom.getArity() == 1;
    }

    /**
     * Prints the short form of the predicate (by omitting the complete URI and
     * replacing it by a prefix name).
     */
    private static String getAbbreviatedName(String uri, PrefixManager pm, boolean insideQuotes) {
        return pm.getShortForm(uri, insideQuotes);
    }

    private static String appendTerms(ImmutableTerm term) {
        if (term instanceof Constant) {
            String st = ((Constant) term).getValue();
            if (st.contains("{")) {
                st = st.replace("{", "\\{");
                st = st.replace("}", "\\}");
            }
            return st;
        } else {
            return "{" + ((Variable) term).getName() + "}";
        }
    }

    //Appends nested concats
    public static void getNestedConcats(StringBuilder stb, ImmutableTerm term1, ImmutableTerm term2) {
        if (term1 instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm f = (ImmutableFunctionalTerm) term1;
            getNestedConcats(stb, f.getTerms().get(0), f.getTerms().get(1));
        } else {
            stb.append(appendTerms(term1));
        }
        if (term2 instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm f = (ImmutableFunctionalTerm) term2;
            getNestedConcats(stb, f.getTerms().get(0), f.getTerms().get(1));
        } else {
            stb.append(appendTerms(term2));
        }
    }

    /**
     * Prints the text representation of different terms.
     */
    private static String getDisplayName(ImmutableTerm term, PrefixManager prefixManager) {
        if (term instanceof ImmutableFunctionalTerm)
            return displayFunction((ImmutableFunctionalTerm) term, prefixManager);
        if (term instanceof Variable)
            return displayVariable((Variable)term);
        if (term instanceof IRIConstant)
            return displayURIConstant((Constant)term, prefixManager);
        if (term instanceof ValueConstant)
            return displayValueConstant((Constant)term);
        if (term instanceof BNode)
            return displayBnode((BNode)term);
        throw new UnexpectedTermException(term);
    }

    private static String displayBnode(Term term) {
        return ((BNode) term).getName();
    }

    private static String displayValueConstant(Term term) {
        return "\"" + ((ValueConstant) term).getValue() + "\"";
    }

    private static String displayURIConstant(Term term, PrefixManager prefixManager) {
        StringBuilder sb = new StringBuilder();
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
        return sb.toString();
    }

    private static String displayVariable(Variable term) {
        return "{" + ((Variable) term).getName() + "}";
    }

    private static String displayFunction(ImmutableFunctionalTerm function, PrefixManager prefixManager) {
        Predicate functionSymbol = function.getFunctionSymbol();
        String fname = getAbbreviatedName(functionSymbol.toString(), prefixManager, false);
        if (functionSymbol instanceof DatatypePredicate)
            return displayDatatypeFunction(function, functionSymbol, fname, prefixManager);
        if (functionSymbol instanceof RDFTermType) {
            ImmutableTerm lexicalTerm = function.getTerm(0);
            if ((lexicalTerm instanceof ImmutableFunctionalTerm)
                    && (((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol() instanceof IRIStringTemplateFunctionSymbol))
                return displayURITemplate((ImmutableFunctionalTerm)lexicalTerm, prefixManager);
        }
        // TODO: remove?
        if (functionSymbol == ExpressionOperation.CONCAT)
            return displayConcat(function);
        if (functionSymbol instanceof BNodePredicate)
            return displayFunctionalBnode(function);
        return displayOrdinaryFunction(function, fname, prefixManager);
    }

    private static String displayFunctionalBnode(ImmutableFunctionalTerm function) {
        StringBuilder sb = new StringBuilder("_:");
        sb.append("{"+function.getTerm(0)+"}");
        function.getTerms().stream()
                .skip(1)
                .forEach(t -> sb.append("_{"+t.toString()+"}"));
        return sb.toString();
    }

    private static String displayOrdinaryFunction(ImmutableFunctionalTerm function, String fname, PrefixManager prefixManager) {
        StringBuilder sb = new StringBuilder();
        sb.append(fname);
        sb.append("(");
        boolean separator = false;
        for (ImmutableTerm innerTerm : function.getTerms()) {
            if (separator) {
                sb.append(", ");
            }
            sb.append(getDisplayName(innerTerm, prefixManager));
            separator = true;
        }
        sb.append(")");
        return sb.toString();
    }

    private static String displayDatatypeFunction(ImmutableFunctionalTerm function, Predicate functionSymbol, String fname, PrefixManager prefixManager) {
        StringBuilder sb = new StringBuilder();
        // Language tag case
        if (functionSymbol.getName().equals(RDF.LANGSTRING.getIRIString())) {
            // with the language tag
            ImmutableTerm var = function.getTerms().get(0);
            ImmutableTerm lang = function.getTerms().get(1);
            sb.append(getDisplayName(var, prefixManager));
            sb.append("@");
            if (lang instanceof ValueConstant) {
                // Don't pass this to getDisplayName() because
                // language constant is not written as @"lang-tag"
                sb.append(((ValueConstant) lang).getValue());
            } else {
                sb.append(getDisplayName(lang, prefixManager));
            }
        } else { // for the other data types
            ImmutableTerm var = function.getTerms().get(0);
            sb.append(getDisplayName(var, prefixManager));
            sb.append("^^");
            sb.append(fname);
        }
        return sb.toString();
    }


    private static String displayURITemplate(ImmutableFunctionalTerm function, PrefixManager prefixManager) {
        StringBuilder sb = new StringBuilder();
        ImmutableTerm lexicalTerm = function.getTerms().get(0);

        if (lexicalTerm instanceof Variable) {
            sb.append("<{");
            sb.append(((Variable) lexicalTerm).getName());
            sb.append("}>");
        }
        else if (lexicalTerm instanceof ValueConstant) {
            return "<" + ((ValueConstant) lexicalTerm).getValue() + ">";
        }
        else if ((lexicalTerm instanceof ImmutableFunctionalTerm)
                && ((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol() instanceof IRIStringTemplateFunctionSymbol) {

            ImmutableFunctionalTerm lexicalFunctionalTerm = (ImmutableFunctionalTerm) lexicalTerm;
            String template = ((IRIStringTemplateFunctionSymbol) lexicalFunctionalTerm.getFunctionSymbol()).getIRITemplate();

            // Utilize the String.format() method so we replaced placeholders '{}' with '%s'
            String templateFormat = template.replace("{}", "%s");
            List<String> varNames = new ArrayList<>();
            for (ImmutableTerm innerTerm : lexicalFunctionalTerm.getTerms()) {
                if (innerTerm instanceof Variable) {
                    varNames.add(getDisplayName(innerTerm, prefixManager));
                }
            }
            String originalUri = String.format(templateFormat, varNames.toArray());
            if (originalUri.equals(RDF.TYPE.getIRIString())) {
                sb.append("a");
            } else {
                String shortenUri = getAbbreviatedName(originalUri, prefixManager, false); // shorten the URI if possible
                if (!shortenUri.equals(originalUri)) {
                    sb.append(shortenUri);
                } else {
                    // If the URI can't be shorten then use the full URI within brackets
                    sb.append("<");
                    sb.append(originalUri);
                    sb.append(">");
                }
            }
        }
        return sb.toString();
    }

    private static String displayConcat(ImmutableFunctionalTerm function) {
        StringBuilder sb = new StringBuilder();
        ImmutableList<? extends ImmutableTerm> terms = function.getTerms();
        sb.append("\"");
        getNestedConcats(sb, terms.get(0), terms.get(1));
        sb.append("\"");
        return sb.toString();
    }

    private TargetQueryRenderer() {
        // Prevent initialization
    }

    private static class UnexpectedTermException extends OntopInternalBugException {
        private UnexpectedTermException(Term term) {
            super("Unexpected type " + term.getClass() + " for term: " + term);
        }

        private UnexpectedTermException(ImmutableTerm term) {
            super("Unexpected type " + term.getClass() + " for term: " + term);
        }
    }
}
