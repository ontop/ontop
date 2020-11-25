package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;

import java.util.Optional;

public class MappingParserHelper {
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;

    public MappingParserHelper(TermFactory termFactory, TypeFactory typeFactory) {
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
    }

    public ImmutableFunctionalTerm getVariable(String id) {
        if (id.contains("."))
            throw new IllegalArgumentException("Fully qualified columns as " + id + " are not accepted.\nPlease, use an alias instead.");

        return termFactory.getPartiallyDefinedToStringCast(termFactory.getVariable(id));
    }

    public Optional<RDFDatatype> extractDatatype(Optional<String> lang, Optional<IRI> iri) {
        // Datatype -> first try: language tag
        Optional<RDFDatatype> datatype = lang
                .filter(tag -> !tag.isEmpty())
                .map(typeFactory::getLangTermType);

        if (datatype.isPresent())
            return datatype;

        // Second try: explicit datatype
         return iri.map(typeFactory::getDatatype);
    }
}
