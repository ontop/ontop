package it.unibz.inf.ontop.spec.mapping.serializer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.model.atom.QuadPredicate;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.template.impl.BnodeTemplateFactory;
import it.unibz.inf.ontop.model.template.impl.IRITemplateFactory;
import it.unibz.inf.ontop.model.template.impl.LiteralTemplateFactory;
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
import org.eclipse.rdf4j.rio.turtle.TurtleUtil;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A utility class to render a Target Query object into its representational
 * string.
 */
public class TargetQueryRenderer {

    private final PrefixManager prefixManager;
    private final IRITemplateFactory iriTemplateFactory;
    private final BnodeTemplateFactory bnodeTemplateFactory;
    private final LiteralTemplateFactory literalTemplateFactory;

    /**
     * @param prefixManager that is used to shorten full IRI name.
     */
    public TargetQueryRenderer(PrefixManager prefixManager) {
        this.prefixManager = prefixManager;
        this.iriTemplateFactory = new IRITemplateFactory(null);
        this.bnodeTemplateFactory = new BnodeTemplateFactory(null);
        this.literalTemplateFactory = new LiteralTemplateFactory(null, null);
    }

    /**
     * Transforms a given list of TargetAtoms into a string.
     */
    public String encode(ImmutableList<TargetAtom> body) {

        TurtleWriter turtleWriter = new TurtleWriter();
        for (TargetAtom atom : body) {
            RDFAtomPredicate pred = (RDFAtomPredicate) atom.getProjectionAtom().getPredicate();
            String subject = renderTerm(pred.getSubject(atom.getSubstitutedTerms()));
            String predicate = renderTerm(pred.getProperty(atom.getSubstitutedTerms()));
            String object = renderTerm(pred.getObject(atom.getSubstitutedTerms()));
            Optional<String> graph;
            if (pred instanceof TriplePredicate) {
                graph = Optional.empty();
            }
            else if (pred instanceof QuadPredicate) {
                graph = pred.getGraph(atom.getSubstitutedTerms()).map(this::renderTerm);
            }
            else
                throw new UnsupportedOperationException("unsupported predicate! " + pred);
            turtleWriter.put(subject, predicate, object, graph);
        }
        return turtleWriter.print();
    }

    private String renderTerm(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm ift = (ImmutableFunctionalTerm) term;
            FunctionSymbol fs = ift.getFunctionSymbol();
            if (DBTypeConversionFunctionSymbol.isTemporary(fs)) { // TmpToTEXT(...)
                ImmutableTerm uncast = DBTypeConversionFunctionSymbol.uncast(ift);
                if (uncast instanceof Variable)
                    return renderVariable((Variable)uncast);
                throw new UnexpectedTermException(term);
            }

            // RDF(..)
            if (fs instanceof RDFTermFunctionSymbol)
                return renderRDFFunction(ift);

            if (fs instanceof DBConcatFunctionSymbol)
                return "\"" + TurtleUtil.encodeString(literalTemplateFactory.serializeTemplateTerm(ift)) + "\"";

            return ift.getFunctionSymbol().getName() + "(" + ift.getTerms().stream()
                    .map(this::renderTerm)
                    .collect(Collectors.joining(", ")) + ")";
        }
        if (term instanceof Variable)
            return renderVariable((Variable) term);
        if (term instanceof IRIConstant)
            return renderIRI(((IRIConstant) term).getIRI().getIRIString());
        if (term instanceof RDFLiteralConstant)
            return ((RDFLiteralConstant) term).toString();
        if (term instanceof BNode)
            return ((BNode) term).getInternalLabel();
        throw new UnexpectedTermException(term);
    }

    private static String renderVariable(Variable term) {
        return "{" + term.getName() + "}";
    }


    private String renderRDFFunction(ImmutableFunctionalTerm function) {
        ImmutableTerm lexicalTerm = function.getTerm(0);

        Optional<RDFDatatype> optionalDatatype = function.inferType()
                .flatMap(TermTypeInference::getTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t);

        if (optionalDatatype.isPresent())
            return renderRDFLiteral(lexicalTerm, optionalDatatype.get());

        ImmutableTerm termType = function.getTerm(1);
        if (termType instanceof RDFTermTypeConstant) {
            RDFTermType rdfTermType = ((RDFTermTypeConstant) termType).getRDFTermType();
            if (rdfTermType instanceof BlankNodeTermType) {
                String identifier = ((lexicalTerm instanceof ImmutableFunctionalTerm)
                        && ((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol() instanceof BnodeStringTemplateFunctionSymbol)
                        ? bnodeTemplateFactory.serializeTemplateTerm((ImmutableFunctionalTerm) lexicalTerm)
                        : renderTerm(lexicalTerm); // variable
                return "_:" + identifier;
            }
            if (rdfTermType instanceof IRITermType) {
                String identifier = ((lexicalTerm instanceof ImmutableFunctionalTerm)
                        && ((ImmutableFunctionalTerm) lexicalTerm).getFunctionSymbol() instanceof IRIStringTemplateFunctionSymbol)
                        ? iriTemplateFactory.serializeTemplateTerm((ImmutableFunctionalTerm) lexicalTerm)
                        : renderTerm(lexicalTerm); // variable
                return renderIRI(identifier);
            }
        }

        throw new IllegalArgumentException("unsupported function " + function);
    }

    private String renderRDFLiteral(ImmutableTerm lexicalTerm, RDFDatatype datatype) {
        String lexicalString = (lexicalTerm instanceof DBConstant)
                // Happens when abstract datatypes are used
                ? "\"" + ((DBConstant) lexicalTerm).getValue() + "\""
                : renderTerm(lexicalTerm);

        String suffix = datatype.getLanguageTag()
                .map(tag -> "@" + tag.getFullString())
                .orElseGet(() -> datatype.getIRI().equals(RDFS.LITERAL)
                            ? ""
                            : "^^" + prefixManager.getShortForm(datatype.getIRI().getIRIString()));

        return lexicalString + suffix;
    }

    private String renderIRI(String iri) {
        String shortenedIri = prefixManager.getShortForm(iri);
        if (!shortenedIri.equals(iri))
            return shortenedIri;

        return "<" + iri + ">";
    }

    private static class UnexpectedTermException extends OntopInternalBugException {
        private UnexpectedTermException(ImmutableTerm term) {
            super("Unexpected type " + term.getClass() + " for term: " + term);
        }
    }
}


