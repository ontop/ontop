package it.unibz.inf.ontop.spec.mapping.type;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

/**
 * To be used on mapping assertions
 */
public interface TermTypeExtractor {

    Optional<TermType> extractType(ImmutableTerm term, IQTree subTree);
}
