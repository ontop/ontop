package it.unibz.inf.ontop.spec.mapping.serializer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.atom.QuadPredicate;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.impl.BlankNodeTermType;
import it.unibz.inf.ontop.model.type.impl.IRITermType;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A utility class to render a Target Query object into its representational
 * string.
 */
public class TargetQueryRenderer {

    private final PrefixManager prefixManager;

    public TargetQueryRenderer(PrefixManager prefixManager) {
        this.prefixManager = prefixManager;
    }

    /**
     * Transforms the given <code>OBDAQuery</code> into a string. The method requires
     * a prefix manager to shorten full IRI name.
     */
    public String encode(ImmutableList<TargetAtom> body) {

        TurtleWriter turtleWriter = new TurtleWriter();
        for (TargetAtom atom : body) {
            RDFAtomPredicate pred = (RDFAtomPredicate) atom.getProjectionAtom().getPredicate();
            String subject = displayTerm(pred.getSubject(atom.getSubstitutedTerms()));
            String predicate = displayTerm(pred.getProperty(atom.getSubstitutedTerms()));
            String object = displayTerm(pred.getObject(atom.getSubstitutedTerms()));
            if (pred instanceof TriplePredicate) {
                turtleWriter.put(subject, predicate, object);
            }
            else if (pred instanceof QuadPredicate) {
                String graph = displayTerm(pred.getGraph(atom.getSubstitutedTerms()).get());
                turtleWriter.put(subject, predicate, object, graph);
            }
            else {
                throw new UnsupportedOperationException("unsupported predicate! " + pred);
            }
        }
        return turtleWriter.print();
    }

    private static String concatArg2String(ImmutableTerm term) {
        if (term instanceof Constant) {
            String st = ((Constant) term).getValue();
            if (st.contains("{")) {   // TODO: check this condition - not clear why it does not escape the sole } (and does not escape \ at all)
                st = st.replace("{", "\\{");
                st = st.replace("}", "\\}");
            }
            return st;
        }
        return displayVariable((Variable)term);
    }

    /**
     * Prints the text representation of different terms.
     */
    private String displayTerm(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm ift = (ImmutableFunctionalTerm) term;
            if (DBTypeConversionFunctionSymbol.isTemporary(ift.getFunctionSymbol())) // TmpToTEXT(...)
                return displayVariable(extractUniqueVariableArgument(ift));

            return displayNonTemporaryFunction(ift);  // RDF(..)
        }
        if (term instanceof Variable)
            return displayVariable((Variable) term);
        if (term instanceof IRIConstant)
            return displayPredicateIRI(((IRIConstant) term).getIRI().getIRIString());
        if (term instanceof RDFLiteralConstant)
            return ((RDFLiteralConstant) term).toString();
        if (term instanceof BNode)
            return ((BNode) term).getInternalLabel();
        throw new UnexpectedTermException(term);
    }

    private String displayPredicateIRI(String iri) {
        if (iri.equals(RDF.TYPE.getIRIString()))
            return "a";

        return prefixManager.getShortForm(iri, false);
    }

    private static String displayVariable(Variable term) {
        return "{" + term.getName() + "}";
    }

    private String displayNonTemporaryFunction(ImmutableFunctionalTerm function) {

        FunctionSymbol functionSymbol = function.getFunctionSymbol();

        if (functionSymbol instanceof DBConcatFunctionSymbol)
            return displayConcat(function);

        if (functionSymbol instanceof RDFTermFunctionSymbol)
            return displayRDFFunction(function);

        return displayOrdinaryFunction(function, functionSymbol.getName());
    }

    private String displayRDFFunction(ImmutableFunctionalTerm function) {
        ImmutableTerm lexicalTerm = function.getTerm(0);

        Optional<RDFDatatype> optionalDatatype = function.inferType()
                .flatMap(TermTypeInference::getTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t);

        if (optionalDatatype.isPresent()) {
            return displayDatatypeFunction(lexicalTerm, optionalDatatype.get());
        }
        ImmutableTerm termType = function.getTerm(1);
        if (termType instanceof RDFTermTypeConstant) {
            RDFTermType rdfTermType = ((RDFTermTypeConstant) termType).getRDFTermType();
            if (rdfTermType instanceof BlankNodeTermType) {
                if (lexicalTerm instanceof ImmutableFunctionalTerm) {
                    ImmutableFunctionalTerm nestedFunction = (ImmutableFunctionalTerm) lexicalTerm;
                    if (nestedFunction.getFunctionSymbol() instanceof BnodeStringTemplateFunctionSymbol) {
                        if (nestedFunction.getFunctionSymbol() instanceof BnodeStringTemplateFunctionSymbol) {
                            return "_:" + instantiateTemplate(nestedFunction);
                        }
                        throw new UnexpectedTermException(nestedFunction);
                    }
                    // case of RDF(TermToTxt(variable), BNODE)
                    return "_:" + displayTerm(nestedFunction);
                }
                // case of RDF(variable, BNODE)
                return "_:" + displayTerm(lexicalTerm);
            }
            if (rdfTermType instanceof IRITermType) {
                if (lexicalTerm instanceof ImmutableFunctionalTerm) {
                    ImmutableFunctionalTerm nestedFunction = (ImmutableFunctionalTerm) lexicalTerm;
                    if (nestedFunction.getFunctionSymbol() instanceof IRIStringTemplateFunctionSymbol)
                        return displayPredicateIRI(instantiateTemplate(nestedFunction));
                }
                return displayIRI(displayTerm(lexicalTerm));
            }
        }

        throw new IllegalArgumentException("unsupported function " + function);
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
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm ift = (ImmutableFunctionalTerm)term;
            if (DBTypeConversionFunctionSymbol.isTemporary(ift.getFunctionSymbol()))
                return extractUniqueVariableArgument(ift);
        }
        return term;
    }

    private String displayOrdinaryFunction(ImmutableFunctionalTerm function, String fname) {
        return fname + "(" + function.getTerms().stream()
                .map(this::displayTerm)
                .collect(Collectors.joining(", "))
                + ")";
    }

    private String displayDatatypeFunction(ImmutableTerm lexicalTerm, RDFDatatype datatype) {
        final String lexicalString = (lexicalTerm instanceof DBConstant)
                // Happens when abstract datatypes are used
                ? "\"" + ((DBConstant) lexicalTerm).getValue() + "\""
                : displayTerm(lexicalTerm);

        return datatype.getLanguageTag()
                .map(tag -> lexicalString + "@" + tag.getFullString())
                .orElseGet(() -> {
                    String typePostfix = datatype.getIRI().equals(RDFS.LITERAL)
                            ? ""
                            : "^^" + prefixManager.getShortForm(datatype.getIRI().getIRIString(), false);
                    return lexicalString + typePostfix;
                });
    }

    private String displayIRI(String s) {
        String shortenedUri = prefixManager.getShortForm(s, false);
        if (!shortenedUri.equals(s))
            return shortenedUri;

        return "<" + s + ">";
    }

    private String instantiateTemplate(ImmutableFunctionalTerm function) {

        String template = ((ObjectStringTemplateFunctionSymbol) function.getFunctionSymbol()).getTemplate();

        // Utilize the String.format() method so we replaced placeholders '{}' with '%s'
        String templateFormat = template.replace("{}", "%s");

        final Object[] varNames = function.getTerms().stream()
                .map(TargetQueryRenderer::asArg)
                .filter(Variable.class::isInstance)
                .map(this::displayTerm)
                .toArray();

        return String.format(templateFormat, varNames);
    }


    /**
     * Concat is expected to be flat
     */
    private static String displayConcat(ImmutableFunctionalTerm function) {
        return "\"" +
                function.getTerms().stream()
                        .map(TargetQueryRenderer::concatArg2String)
                        .collect(Collectors.joining()) +
                "\"";
    }

    private static class UnexpectedTermException extends OntopInternalBugException {
        private UnexpectedTermException(ImmutableTerm term) {
            super("Unexpected type " + term.getClass() + " for term: " + term);
        }
    }
}


