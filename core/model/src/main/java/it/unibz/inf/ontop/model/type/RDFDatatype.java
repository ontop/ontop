package it.unibz.inf.ontop.model.type;

import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public interface RDFDatatype extends RDFTermType {

    Optional<LanguageTag> getLanguageTag();

    boolean isA(IRI baseDatatypeIri);

    IRI getIRI();
}
