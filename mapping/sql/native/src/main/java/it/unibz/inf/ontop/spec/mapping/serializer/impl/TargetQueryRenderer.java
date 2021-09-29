package it.unibz.inf.ontop.spec.mapping.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.atom.QuadPredicate;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.template.impl.BnodeTemplateFactory;
import it.unibz.inf.ontop.model.template.impl.IRITemplateFactory;
import it.unibz.inf.ontop.model.template.impl.LiteralTemplateFactory;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.impl.BlankNodeTermType;
import it.unibz.inf.ontop.model.type.impl.IRITermType;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import org.eclipse.rdf4j.rio.turtle.TurtleUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class to render a Target Query object into its representational
 * string.
 */
public class TargetQueryRenderer {

    private final PrefixManager prefixManager;
    private final IRITemplateFactory iriTemplateFactory;
    private final BnodeTemplateFactory bnodeTemplateFactory;
    private final LiteralTemplateFactory literalTemplateFactory;
    private final String rdfType;


    /**
     * @param prefixManager that is used to shorten full IRI name.
     */
    public TargetQueryRenderer(PrefixManager prefixManager) {
        this.prefixManager = prefixManager;
        this.rdfType = prefixManager.getShortForm(RDF.TYPE.getIRIString());
        this.iriTemplateFactory = new IRITemplateFactory(null);
        this.bnodeTemplateFactory = new BnodeTemplateFactory(null);
        this.literalTemplateFactory = new LiteralTemplateFactory(null, null);
    }

    /**
     * Transforms a given list of TargetAtoms into a string.
     */
    public String encode(ImmutableList<TargetAtom> body) {

        Map<Optional<String>, Map<String, Map<String, Set<String>>>> store = new LinkedHashMap<>();
        for (TargetAtom atom : body) {
            RDFAtomPredicate pred = (RDFAtomPredicate) atom.getProjectionAtom().getPredicate();
            Optional<String> graph;
            if (pred instanceof TriplePredicate) {
                graph = Optional.empty();
            }
            else if (pred instanceof QuadPredicate) {
                graph = pred.getGraph(atom.getSubstitutedTerms()).map(this::renderTerm);
            }
            else
                throw new UnsupportedOperationException("unsupported predicate! " + pred);

            String subject = renderTerm(pred.getSubject(atom.getSubstitutedTerms()));
            String predicate = renderTerm(pred.getProperty(atom.getSubstitutedTerms()));
            String object = renderTerm(pred.getObject(atom.getSubstitutedTerms()));

            store.computeIfAbsent(graph, (g) -> new LinkedHashMap<>())
                    .computeIfAbsent(subject, (s) -> new LinkedHashMap<>())
                    .computeIfAbsent(predicate, (p) -> new LinkedHashSet<>())
                    .add(object);
        }
        return store.entrySet().stream()
                .map(e -> e.getKey().isPresent()
                        ? "GRAPH " + e.getKey().get() + " { "
                            + getSubjectPredicateObjects(e.getValue()) + "}"
                        :		getSubjectPredicateObjects(e.getValue()))
                .collect(Collectors.joining(" "));
    }

    private String getPredicateObjects(Map<String, Set<String>> predicateObjects) {
        Set<String> type = predicateObjects.get(rdfType);
        Stream<Map.Entry<String, Set<String>>> stream = type != null
                ? Stream.concat(
                    Stream.of(Maps.immutableEntry("a", type)), // rename and place first
                    predicateObjects.entrySet().stream().filter(e -> !e.getKey().equals(rdfType)))
                : predicateObjects.entrySet().stream();

        return stream
                .map(e -> e.getKey() + " " + String.join(" , ", e.getValue()))
                .collect(Collectors.joining(" ; "));
    }

    private String getSubjectPredicateObjects(Map<String, Map<String, Set<String >>> map) {
        return map.entrySet().stream()
                .map(e -> e.getKey() + " " + getPredicateObjects(e.getValue()) + " . ")
                .collect(Collectors.joining(""));
    }


    private String renderTerm(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm ift = (ImmutableFunctionalTerm) term;
            ImmutableTerm uncast = DBTypeConversionFunctionSymbol.uncast(ift);  // TmpToTEXT(...)
            if (uncast instanceof Variable)
                return renderVariable((Variable)uncast);

            if (uncast != ift)
                throw new MinorOntopInternalBugException("Unexpected type " + term.getClass() + " for term: " + term);

            FunctionSymbol fs = ift.getFunctionSymbol();
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
        throw new MinorOntopInternalBugException("Unexpected type " + term.getClass() + " for term: " + term);
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

        throw new MinorOntopInternalBugException("unsupported function " + function);
    }

    private String renderRDFLiteral(ImmutableTerm lexicalTerm, RDFDatatype datatype) {
        String lexicalString = (lexicalTerm instanceof DBConstant)
                // Happens when abstract datatypes are used
                ? "\"" + ((DBConstant) lexicalTerm).getValue() + "\""
                : renderTerm(lexicalTerm);

        String suffix = datatype.getLanguageTag()
                .map(tag -> "@" + tag.getFullString())
                .orElseGet(() ->
                        // in Turtle, the default datatype is xsd:string
                        datatype.getIRI().equals(XSD.STRING) && (lexicalTerm instanceof RDFLiteralConstant) ||
                        // suppress rdfs:Literal for non-constants
                        datatype.getIRI().equals(RDFS.LITERAL) && !(lexicalTerm instanceof RDFLiteralConstant)
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
}


