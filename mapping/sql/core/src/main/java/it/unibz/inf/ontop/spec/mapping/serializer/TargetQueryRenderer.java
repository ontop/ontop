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
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;

import java.util.Optional;

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
			final AtomPredicate p = atom.getProjectionAtom().getPredicate();
            if (p instanceof TriplePredicate) {
                ImmutableTerm subjectTerm = atom.getSubstitutedTerm(0);
                String subject = getDisplayName(subjectTerm, prefixManager);
                ImmutableTerm predicateTerm = atom.getSubstitutedTerm(1);
                String predicate = getDisplayName(predicateTerm, prefixManager);
                ImmutableTerm objectTerm = atom.getSubstitutedTerm(2);
                String object = getDisplayName(objectTerm, prefixManager);
                turtleWriter.put(subject, predicate, object);
            } else {
                throw new UnsupportedOperationException("unsupported predicate! " + p);
			}
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

    /**
     * Prints the text representation of different terms.
     */
    private static String getDisplayName(ImmutableTerm term, PrefixManager prefixManager) {
        if (term instanceof ImmutableFunctionalTerm)
            return displayFunction((ImmutableFunctionalTerm) term, prefixManager);
        if (term instanceof Variable)
            return displayVariable((Variable)term);
        if (term instanceof IRIConstant)
            return displayIRIConstant((IRIConstant)term, prefixManager);
        if (term instanceof RDFLiteralConstant)
            return displayValueConstant((Constant)term);
        if (term instanceof BNode)
            return displayBnode((BNode)term);
        throw new UnexpectedTermException(term);
    }

    private static String displayBnode(Term term) {
        return ((BNode) term).getName();
    }

    private static String displayValueConstant(Term term) {
        return "\"" + ((RDFLiteralConstant) term).getValue() + "\"";
    }

    private static String displayIRIConstant(IRIConstant iri, PrefixManager prefixManager) {
        if (iri.getIRI().getIRIString().equals(RDF.TYPE.getIRIString())) {
            return "a";
        } else {
            return getAbbreviatedName(iri.toString(), prefixManager, false); // shorten the URI if possible
        }
    }

    private static String displayVariable(Variable term) {
        return "{" + term.getName() + "}";
    }

    private static String displayFunction(ImmutableFunctionalTerm function, PrefixManager prefixManager) {
        FunctionSymbol functionSymbol = function.getFunctionSymbol();
        if (functionSymbol instanceof RDFTermFunctionSymbol) {
            ImmutableTerm lexicalTerm = function.getTerm(0);

            Optional<RDFDatatype> optionalDatatype = function.inferType()
                    .flatMap(TermTypeInference::getTermType)
                    .filter(t -> t instanceof RDFDatatype)
                    .map(t -> (RDFDatatype) t);

            if (optionalDatatype.isPresent()) {
                return displayDatatypeFunction(lexicalTerm, optionalDatatype.get(), prefixManager);
            }

            if (lexicalTerm instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm lexicalFunctionalTerm = (ImmutableFunctionalTerm) lexicalTerm;
                FunctionSymbol lexicalFunctionSymbol = lexicalFunctionalTerm.getFunctionSymbol();
                if (lexicalFunctionSymbol instanceof IRIStringTemplateFunctionSymbol)
                    return displayURITemplate(lexicalFunctionalTerm, prefixManager);
                else if (lexicalFunctionSymbol instanceof BnodeStringTemplateFunctionSymbol)
                    return displayFunctionalBnode(lexicalFunctionalTerm);
            }
        }
        if (functionSymbol instanceof DBConcatFunctionSymbol)
            return displayConcat(function);
        return displayOrdinaryFunction(function, functionSymbol.getName(), prefixManager);
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

    private static String displayDatatypeFunction(ImmutableTerm lexicalTerm, RDFDatatype datatype, PrefixManager prefixManager) {
        String lexicalString = getDisplayName(lexicalTerm, prefixManager);

        return datatype.getLanguageTag()
                .map(tag -> lexicalString + "@" + tag.getFullString())
                .orElseGet(() -> lexicalString + "^^"
                        + getAbbreviatedName(datatype.getIRI().getIRIString(), prefixManager, false));
    }


    private static String displayURITemplate(ImmutableFunctionalTerm function, PrefixManager prefixManager) {
        StringBuilder sb = new StringBuilder();

        String template = ((IRIStringTemplateFunctionSymbol) function.getFunctionSymbol()).getTemplate();

        // Utilize the String.format() method so we replaced placeholders '{}' with '%s'
        String templateFormat = template.replace("{}", "%s");

        final Object[] varNames = function.getTerms().stream()
                .filter(Variable.class::isInstance)
                .map(var -> getDisplayName(var, prefixManager))
                .toArray();

        String originalUri = String.format(templateFormat, varNames);
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

        return sb.toString();
    }

    /**
     * Concat is now expected to be flat
     */
    public static String displayConcat(ImmutableFunctionalTerm function) {
        StringBuilder sb = new StringBuilder();
        ImmutableList<? extends ImmutableTerm> terms = function.getTerms();
        sb.append("\"");
        terms.forEach(TargetQueryRenderer::appendTerms);
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
