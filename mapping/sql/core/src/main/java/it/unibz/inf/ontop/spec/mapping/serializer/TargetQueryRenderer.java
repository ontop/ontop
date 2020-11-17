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
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.Templates;

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

    /**
     * Prints the text representation of different terms.
     */
    private String displayTerm(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm ift = (ImmutableFunctionalTerm) term;
            FunctionSymbol fs = ift.getFunctionSymbol();
            if (DBTypeConversionFunctionSymbol.isTemporary(fs)) { // TmpToTEXT(...)
                ImmutableTerm uncast = DBTypeConversionFunctionSymbol.uncast(ift);
                if (uncast instanceof Variable)
                    return displayVariable((Variable)uncast);
                throw new UnexpectedTermException(term);
            }

            // RDF(..)
            if (fs instanceof RDFTermFunctionSymbol)
                return displayRDFFunction(ift);

            if (fs instanceof DBConcatFunctionSymbol) {
                return "\"" + ift.getTerms().stream()
                                .map(TargetQueryRenderer::concatArg2String)
                                .collect(Collectors.joining()) + "\"";
            }

            return ift.getFunctionSymbol().getName() + "(" + ift.getTerms().stream()
                    .map(this::displayTerm)
                    .collect(Collectors.joining(", ")) + ")";
        }
        if (term instanceof Variable)
            return displayVariable((Variable) term);
        if (term instanceof IRIConstant)
            return displayIRI(((IRIConstant) term).getIRI().getIRIString());
        if (term instanceof RDFLiteralConstant)
            return ((RDFLiteralConstant) term).toString();
        if (term instanceof BNode)
            return ((BNode) term).getInternalLabel();
        throw new UnexpectedTermException(term);
    }

    private static String displayVariable(Variable term) {
        return "{" + term.getName() + "}";
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
        if (term instanceof Variable)
            return displayVariable((Variable)term);

        throw new UnexpectedTermException(term);
    }

    private String displayRDFFunction(ImmutableFunctionalTerm function) {
        ImmutableTerm lexicalTerm = function.getTerm(0);

        Optional<RDFDatatype> optionalDatatype = function.inferType()
                .flatMap(TermTypeInference::getTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t);

        if (optionalDatatype.isPresent())
            return displayDatatypeFunction(lexicalTerm, optionalDatatype.get());

        ImmutableTerm termType = function.getTerm(1);
        if (termType instanceof RDFTermTypeConstant) {
            String identifier = (lexicalTerm instanceof ImmutableFunctionalTerm
                            && ((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol() instanceof ObjectStringTemplateFunctionSymbol)
                    ? instantiateTemplate((ImmutableFunctionalTerm) lexicalTerm)
                    // case of RDF(TermToTxt(variable), X) or RDF(variable, X), where X = BNODE / IRI
                    : displayTerm(lexicalTerm);

            RDFTermType rdfTermType = ((RDFTermTypeConstant) termType).getRDFTermType();
            if (rdfTermType instanceof BlankNodeTermType)
                return "_:" + identifier;
            if (rdfTermType instanceof IRITermType)
                return displayIRI(identifier);
        }

        throw new IllegalArgumentException("unsupported function " + function);
    }

    private String displayDatatypeFunction(ImmutableTerm lexicalTerm, RDFDatatype datatype) {
        String lexicalString = (lexicalTerm instanceof DBConstant)
                // Happens when abstract datatypes are used
                ? "\"" + ((DBConstant) lexicalTerm).getValue() + "\""
                : displayTerm(lexicalTerm);

        String suffix = datatype.getLanguageTag()
                .map(tag -> "@" + tag.getFullString())
                .orElseGet(() -> datatype.getIRI().equals(RDFS.LITERAL)
                            ? ""
                            : "^^" + prefixManager.getShortForm(datatype.getIRI().getIRIString(), false));

        return lexicalString + suffix;
    }

    private String displayIRI(String iri) {
        if (iri.equals(RDF.TYPE.getIRIString()))
            return "a";

        String shortenedIri = prefixManager.getShortForm(iri, false);
        if (!shortenedIri.equals(iri))
            return shortenedIri;

        return "<" + iri + ">";
    }

    private static String instantiateTemplate(ImmutableFunctionalTerm ift) {

        ObjectStringTemplateFunctionSymbol fs = (ObjectStringTemplateFunctionSymbol) ift.getFunctionSymbol();

        ImmutableList<Variable> vars = ift.getTerms().stream()
                .map(DBTypeConversionFunctionSymbol::uncast)
                .filter(t -> t instanceof Variable)
                .map(t -> (Variable)t)
                .collect(ImmutableCollectors.toList());

        if (vars.size() != fs.getArity())
            throw new UnexpectedTermException(ift);

        Object[] varNames = vars.stream().map(TargetQueryRenderer::displayVariable).toArray();
        return Templates.format(fs.getTemplate(), varNames);
    }


    private static class UnexpectedTermException extends OntopInternalBugException {
        private UnexpectedTermException(ImmutableTerm term) {
            super("Unexpected type " + term.getClass() + " for term: " + term);
        }
    }
}


