package it.unibz.inf.ontop.query.translation.impl;

import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.model.term.GroundTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
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

        if (v instanceof Literal) {
            Literal literal = (Literal) v;
            IRI typeURI = literal.getDatatype();
            String value = literal.getLabel();
            Optional<String> lang = literal.getLanguage();

            if (lang.isPresent()) {
                return termFactory.getRDFLiteralConstant(value, lang.get());
            }
            else {
                RDFDatatype type = (typeURI == null)
                        ? typeFactory.getXsdStringDatatype() // default data type is xsd:string
                        : typeFactory.getDatatype(rdfFactory.createIRI(typeURI.stringValue()));

                if (type == null)
                    return termFactory.getConstantIRI(rdfFactory.createIRI(typeURI.stringValue()));

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
        }

        throw new RuntimeException(new OntopUnsupportedKGQueryException("The value " + v + " is not supported yet!"));
    }
}
