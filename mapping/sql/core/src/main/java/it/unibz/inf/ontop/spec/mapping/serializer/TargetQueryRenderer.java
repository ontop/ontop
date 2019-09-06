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
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.impl.BlankNodeTermType;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.eclipse.rdf4j.query.algebra.ValueConstant;

import java.util.Optional;
import java.util.stream.Collectors;

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
            String originalString = atom.getProjectionAtom().getPredicate().toString();
            if (originalString.equals("triple")) {
                ImmutableTerm subjectTerm = atom.getSubstitutedTerm(0);
                String subject = getDisplayName(subjectTerm, prefixManager);
                ImmutableTerm predicateTerm = atom.getSubstitutedTerm(1);
                String predicate = getDisplayName(predicateTerm, prefixManager);
                ImmutableTerm objectTerm = atom.getSubstitutedTerm(2);
                String object = getDisplayName(objectTerm, prefixManager);
                turtleWriter.put(subject, predicate, object);
            } else {
                throw new UnsupportedOperationException("unsupported predicate! " + originalString);
            }
        }
        return turtleWriter.print();
    }

    /**
     * Prints the short form of the predicate (by omitting the complete URI and
     * replacing it by a prefix name).
     */
    private static String getAbbreviatedName(String uri, PrefixManager pm, boolean insideQuotes) {
        return pm.getShortForm(uri, insideQuotes);
    }

    private static String concatArg2String(ImmutableTerm term) {
        if (term instanceof Constant) {
            String st = ((Constant) term).getValue();
            if (st.contains("{")) {
                st = st.replace("{", "\\{");
                st = st.replace("}", "\\}");
            }
            return st;
        }
        return "{" + ((Variable) term).getName() + "}";
    }

    /**
     * Prints the text representation of different terms.
     */
    private static String getDisplayName(ImmutableTerm term, PrefixManager prefixManager) {
        if (term instanceof ImmutableFunctionalTerm) // RDF(..) or ||n(...) or TmpToTEXT(...)
            return displayFunction((ImmutableFunctionalTerm) term, prefixManager);
        if (term instanceof Variable)
            return displayVariable((Variable) term);
        if (term instanceof IRIConstant)
            return displayIRIConstant((IRIConstant) term, prefixManager);
        if (term instanceof RDFLiteralConstant)
            return displayValueConstant((RDFLiteralConstant) term);
        if (term instanceof BNode)
            return displayConstantBnode((BNode) term);
        throw new UnexpectedTermException(term);
    }

    private static String displayConstantBnode(Term term) {
        return ((BNode) term).getName();
    }

    private static String displayValueConstant(Term term) {
        return "\"" + ((RDFLiteralConstant) term).getValue() + "\"";
    }

    private static String displayIRIConstant(IRIConstant iri, PrefixManager prefixManager) {
        if (iri.getIRI().getIRIString().equals(RDF.TYPE.getIRIString()))
            return "a";
        return getAbbreviatedName(iri.toString(), prefixManager, false); // shorten the URI if possible
    }

    private static String displayVariable(Variable term) {
        return "{" + term.getName() + "}";
    }

    private static String displayFunction(ImmutableFunctionalTerm function, PrefixManager prefixManager) {

        if (isTemporaryConversionFunction(function))
            return displayVariable(extractUniqueVariableArgument(function));

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
            ImmutableTerm termType = function.getTerm(1);
            if (termType instanceof RDFTermTypeConstant &&
                    ((RDFTermTypeConstant) termType).getRDFTermType() instanceof BlankNodeTermType) {
                if (lexicalTerm instanceof ImmutableFunctionalTerm) {
                    ImmutableFunctionalTerm nestedFunction = (ImmutableFunctionalTerm) lexicalTerm;
                    FunctionSymbol nestedFs = nestedFunction.getFunctionSymbol();
                    if (nestedFs instanceof BnodeStringTemplateFunctionSymbol)
                        return displayFunctionalBnode(nestedFunction);
                    // case of RDF(TermToTxt(variable), BNODE)
                    return displayNonFunctionalBNode(nestedFunction);
                }
                // case of RDF(variable, BNODE)
                return displayNonFunctionalBNode(lexicalTerm);
            }
            if (lexicalTerm instanceof ImmutableFunctionalTerm) {
                ImmutableFunctionalTerm nestedFunction = (ImmutableFunctionalTerm) lexicalTerm;
                FunctionSymbol nestedFs = nestedFunction.getFunctionSymbol();
                if (nestedFs instanceof IRIStringTemplateFunctionSymbol)
                    return displayURITemplate(nestedFunction, prefixManager);
            }
            throw new IllegalArgumentException("unsupported function " + function);
        }
        if (functionSymbol instanceof DBConcatFunctionSymbol)
            return displayConcat(function);


        return displayOrdinaryFunction(function, functionSymbol.getName(), prefixManager);
    }

    private static String displayNonFunctionalBNode(ImmutableTerm term) {
        if (term instanceof Variable)
            return "_:" + displayVariable((Variable) term);
        if (isTemporaryConversionFunction(term))
            return "_:" + displayVariable(
                    extractUniqueVariableArgument((ImmutableFunctionalTerm) term));
        throw new UnexpectedTermException(term);
    }

    private static boolean isTemporaryConversionFunction(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm) {
            FunctionSymbol fs = ((ImmutableFunctionalTerm) term).getFunctionSymbol();
            return (fs instanceof DBTypeConversionFunctionSymbol && ((DBTypeConversionFunctionSymbol) fs).isTemporary());
        }
        return false;
    }

    private static Variable extractUniqueVariableArgument(ImmutableFunctionalTerm fun) {
        if (fun.getArity() == 1) {
            ImmutableTerm arg = fun.getTerm(0);
            if (arg instanceof Variable)
                return (Variable) arg;
        }
        throw new UnexpectedTermException(fun);
    }

    /**
     * If the term is a cast-to-string function, return the first (0-th) argument, which must be a variable.
     * Otherwise return the term
     **/
    private static ImmutableTerm asArg(ImmutableTerm term) {
        if(isTemporaryConversionFunction(term))
            return extractUniqueVariableArgument((ImmutableFunctionalTerm) term);
        return term;
    }

    private static String displayFunctionalBnode(ImmutableFunctionalTerm function) {
        ImmutableTerm firstTerm = function.getTerms().get(0);
        if (firstTerm instanceof ValueConstant) {
            String templateFormat = ((ValueConstant) firstTerm).getValue().stringValue().replace("{}", "%s");
            if (function.getTerms().stream().skip(1).
                    anyMatch(t -> !(t instanceof Variable)))
                throw new UnexpectedTermException(function, "All argument of the BNode template function but the first one are expected to be variables");
            ImmutableList<String> varNames = function.getTerms().stream().skip(1)
                    .filter(t -> t instanceof Variable)
                    .map(t -> (Variable) t)
                    .map(TargetQueryRenderer::displayVariable)
                    .collect(ImmutableCollectors.toList());

            return "_:" + String.format(templateFormat, varNames.toArray());
        }
        throw new UnexpectedTermException(function, "The first argument of the BNode template function is expected to be a template");
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
        final String lexicalString = getDisplayName(lexicalTerm, prefixManager);

        return datatype.getLanguageTag()
                .map(tag -> lexicalString + "@" + tag.getFullString())
                .orElseGet(() -> {
                    final String typePostfix = datatype.getIRI().equals(RDFS.LITERAL) ? "" : "^^"
                            + getAbbreviatedName(datatype.getIRI().getIRIString(), prefixManager, false);
                    return lexicalString + typePostfix;
                });
    }

    private static String displayURITemplate(ImmutableFunctionalTerm function, PrefixManager prefixManager) {
        ImmutableTerm lexicalTerm = function.getTerms().get(0);

        if (lexicalTerm instanceof RDFLiteralConstant)
            return "<" + ((RDFLiteralConstant) lexicalTerm).getValue() + ">";

        if (lexicalTerm instanceof Variable)
            return "<{" + ((Variable) lexicalTerm).getName() + "}>";

        if (lexicalTerm instanceof ImmutableFunctionalTerm) {

            ImmutableFunctionalTerm fun = (ImmutableFunctionalTerm) lexicalTerm;
            if (isTemporaryConversionFunction(fun))
                return "<"+displayVariable(extractUniqueVariableArgument(fun))+">";

            FunctionSymbol fs = function.getFunctionSymbol();
            if (fs instanceof IRIStringTemplateFunctionSymbol) {
                ImmutableFunctionalTerm lexicalFunctionalTerm = (ImmutableFunctionalTerm) lexicalTerm;
                String template = ((IRIStringTemplateFunctionSymbol) lexicalFunctionalTerm.getFunctionSymbol()).getTemplate();

                // Utilize the String.format() method so we replaced placeholders '{}' with '%s'
                String templateFormat = template.replace("{}", "%s");

                final Object[] varNames = function.getTerms().stream()
                        .map(TargetQueryRenderer::asArg)
                        .filter(Variable.class::isInstance)
                        .map(var -> getDisplayName(var, prefixManager))
                        .toArray();

                String originalUri = String.format(templateFormat, varNames);
                if (originalUri.equals(RDF.TYPE.getIRIString()))
                    return "a";

                String shortenUri = getAbbreviatedName(originalUri, prefixManager, false); // shorten the URI if possible
                if (!shortenUri.equals(originalUri))
                    return shortenUri;

                return "<" + originalUri + ">";
            }
        }
        throw new UnexpectedTermException(lexicalTerm);
    }


    /**
     * Concat is expected to be flat
     */
    public static String displayConcat(ImmutableFunctionalTerm function) {
        return "\"" +
                function.getTerms().stream()
                        .map(TargetQueryRenderer::concatArg2String)
                        .collect(Collectors.joining()) +
                "\"";
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

        private UnexpectedTermException(ImmutableTerm term, String message) {
            super("Unexpected term " + term + ":\n" + message);
        }
    }
}

