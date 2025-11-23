package it.unibz.inf.ontop.query.translation.impl;

import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.model.term.GroundTerm;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Triple;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;

import java.util.Optional;

public class RDF4JValueTranslator {

    private final TermFactory termFactory;
    private final RDF rdfFactory;
    private final TypeFactory typeFactory;

    public RDF4JValueTranslator(TermFactory termFactory, RDF rdfFactory, TypeFactory typeFactory) {
        this.termFactory = termFactory;
        this.rdfFactory = rdfFactory;
        this.typeFactory = typeFactory;
    }

    public GroundTerm getTermForLiteralOrIri(Value v) {

        if (v instanceof IRI)
            return termFactory.getConstantIRI(rdfFactory.createIRI(((IRI) v).stringValue()));

        if (v instanceof Literal)
            return getLiteralTerm((Literal) v);

        if (v instanceof BNode)
            return termFactory.getConstantBNode(((BNode) v).getID());

        if (v instanceof Triple)
            return getTripleTerm((Triple) v);

        throw new RuntimeException(new OntopUnsupportedKGQueryException("The value " + v + " is not supported yet!"));
    }

    private GroundTerm getLiteralTerm(Literal literal) {
        IRI typeURI = literal.getDatatype();
        String value = literal.getLabel();
        Optional<String> lang = literal.getLanguage();

        if (lang.isPresent()) {
            return termFactory.getRDFLiteralConstant(value, lang.get());
        }

        RDFDatatype type = (typeURI == null)
                ? typeFactory.getXsdStringDatatype() // default data type is xsd:string
                : typeFactory.getDatatype(rdfFactory.createIRI(typeURI.stringValue()));

        if (type == null) {
            if (typeURI == null)
                throw new RuntimeException(new OntopUnsupportedKGQueryException(
                        "Missing datatype IRI for literal " + literal));
            return termFactory.getConstantIRI(rdfFactory.createIRI(typeURI.stringValue()));
        }

        // BC-march-19: it seems that SPARQL does not forbid invalid lexical forms
        //     (e.g. when interpreted as an EBV, they evaluate to false)
        // However, it is unclear in which cases it would be interesting to offer a (partial) robustness to
        // such errors coming from the input query
        // check if the value is (lexically) correct for the specified datatype
        if (!XMLDatatypeUtil.isValidValue(value, typeURI))
            throw new RuntimeException(new OntopUnsupportedKGQueryException(
                    String.format("Invalid lexical forms are not accepted. Found for %s: %s", type, value)));

        return termFactory.getRDFLiteralConstant(value, type);
    }

    private GroundTerm getTripleTerm(Triple triple) {
        GroundTerm subject = getTermForLiteralOrIri(triple.getSubject());
        GroundTerm predicate = getTermForLiteralOrIri(triple.getPredicate());
        GroundTerm object = getTermForLiteralOrIri(triple.getObject());

        if (!(predicate instanceof IRIConstant))
            throw new RuntimeException(new OntopUnsupportedKGQueryException(
                    "RDF-star predicate must be an IRI: " + predicate));

        ImmutableTerm tripleTerm = termFactory.getRDFStarTripleFunctionalTerm(subject, predicate, object).simplify();
        if (tripleTerm instanceof GroundTerm)
            return (GroundTerm) tripleTerm;

        throw new RuntimeException(new OntopUnsupportedKGQueryException(
                "Only ground RDF-star triples are supported: " + triple));
    }
}
