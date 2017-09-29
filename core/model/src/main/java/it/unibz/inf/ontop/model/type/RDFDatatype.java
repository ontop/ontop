package it.unibz.inf.ontop.model.type;

import org.eclipse.rdf4j.model.IRI;

import java.util.Optional;

public interface RDFDatatype extends RDFTermType {

    Optional<LanguageTag> getLanguageTag();

    boolean isCompatibleWith(IRI baseDatatypeIri);
}
