package it.unibz.inf.ontop.model.type;

import java.util.Optional;

/**
 * Maps RDF term types to the natural DB term for their lexical term
 * (before being cast into a string)
 *
 * Excludes strings
 *
 */
public interface RDF2DBTypeMapper {

    Optional<DBTermType> getNaturalNonStringDBType(RDFTermType rdfTermType);
}
