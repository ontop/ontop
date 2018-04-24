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
import it.unibz.inf.ontop.model.IriConstants;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BNodePredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;

import java.util.ArrayList;
import java.util.List;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

/**
 * A utility class to render a Target Query object into its representational
 * string.
 */
public class TargetQueryRenderer {

    /**
     * Transforms the given <code>OBDAQuery</code> into a string. The method requires
     * a prefix manager to shorten full IRI name.
     */
    public static String encode(ImmutableList<ImmutableFunctionalTerm> body, PrefixManager prefixManager) {

        TurtleWriter turtleWriter = new TurtleWriter();
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
            } else if (originalString.equals("triple")) {
                Term subjectTerm = atom.getTerm(0);
                subject = getDisplayName(subjectTerm, prefixManager);
                Term predicateTerm = atom.getTerm(1);
                predicate = getDisplayName(predicateTerm, prefixManager);
                Term objectTerm = atom.getTerm(2);
                object = getDisplayName(objectTerm, prefixManager);
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
     */
    private static String getAbbreviatedName(String uri, PrefixManager pm, boolean insideQuotes) {
        return pm.getShortForm(uri, insideQuotes);
    }

    private static String appendTerms(Term term) {
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
    public static void getNestedConcats(StringBuilder stb, Term term1, Term term2) {
        if (term1 instanceof Function) {
            Function f = (Function) term1;
            getNestedConcats(stb, f.getTerms().get(0), f.getTerms().get(1));
        } else {
            stb.append(appendTerms(term1));
        }
        if (term2 instanceof Function) {
            Function f = (Function) term2;
            getNestedConcats(stb, f.getTerms().get(0), f.getTerms().get(1));
        } else {
            stb.append(appendTerms(term2));
        }
    }

    /**
     * Prints the text representation of different terms.
     */
    private static String getDisplayName(Term term, PrefixManager prefixManager) {
        if (term instanceof Function)
            return displayFunction((Function) term, prefixManager);
        if (term instanceof Variable)
            return displayVariable(term);
        if (term instanceof URIConstant)
            return displayURIConstant(term, prefixManager);
        if (term instanceof ValueConstant)
            return displayValueConstant(term);
        if (term instanceof BNode)
            return displayBnode(term);
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

    private static String displayVariable(Term term) {
        return "{" + ((Variable) term).getName() + "}";
    }

    private static String displayFunction(Function function, PrefixManager prefixManager) {
        Predicate functionSymbol = function.getFunctionSymbol();
        String fname = getAbbreviatedName(functionSymbol.toString(), prefixManager, false);
        if (function.isDataTypeFunction())
            return displayDatatypeFunction(function, functionSymbol, fname, prefixManager);
        if (functionSymbol instanceof URITemplatePredicate)
            return displayURITemplate(function, prefixManager);
        if (functionSymbol == ExpressionOperation.CONCAT)
            return displayConcat(function);
        if (functionSymbol instanceof BNodePredicate)
            return displayFunctionalBnode(function);
        return displayOrdinaryFunction(function, fname, prefixManager);
    }

    private static String displayFunctionalBnode(Function function) {
        StringBuilder sb = new StringBuilder("_:");
        sb.append("{"+function.getTerm(0)+"}");
        function.getTerms().stream()
                .skip(1)
                .forEach(t -> sb.append("_{"+t.toString()+"}"));
        return sb.toString();
    }

    private static String displayOrdinaryFunction(Function function, String fname, PrefixManager prefixManager) {
        StringBuilder sb = new StringBuilder();
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
        return sb.toString();
    }

    private static String displayDatatypeFunction(Function function, Predicate functionSymbol, String fname, PrefixManager prefixManager) {
        StringBuilder sb = new StringBuilder();
        // Language tag case
        if (TYPE_FACTORY.isString(functionSymbol) && (functionSymbol.getArity() == 2)) {
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
        } else { // for the other data types
            Term var = function.getTerms().get(0);
            sb.append(getDisplayName(var, prefixManager));
            sb.append("^^");
            sb.append(fname);
        }
        return sb.toString();
    }


    private static String displayURITemplate(Function function, PrefixManager prefixManager) {
        StringBuilder sb = new StringBuilder();
        Term firstTerm = function.getTerms().get(0);

        if (firstTerm instanceof Variable) {
            sb.append("<{");
            sb.append(((Variable) firstTerm).getName());
            sb.append("}>");
        } else {
            String template = ((ValueConstant) firstTerm).getValue();

            // Utilize the String.format() method so we replaced placeholders '{}' with '%s'
            String templateFormat = template.replace("{}", "%s");
            List<String> varNames = new ArrayList<String>();
            for (Term innerTerm : function.getTerms()) {
                if (innerTerm instanceof Variable) {
                    varNames.add(getDisplayName(innerTerm, prefixManager));
                }
            }
            String originalUri = String.format(templateFormat, varNames.toArray());
            if (originalUri.equals(IriConstants.RDF_TYPE)) {
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

    private static String displayConcat(Function function) {
        StringBuilder sb = new StringBuilder();
        List<Term> terms = function.getTerms();
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
    }
}
