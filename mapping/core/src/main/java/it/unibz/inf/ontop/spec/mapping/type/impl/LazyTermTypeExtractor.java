package it.unibz.inf.ontop.spec.mapping.type.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.spec.mapping.type.TermTypeExtractor;

import java.util.Optional;

public class LazyTermTypeExtractor implements TermTypeExtractor {

    @Inject
    private LazyTermTypeExtractor() {
    }

    @Override
    public Optional<TermType> extractType(ImmutableTerm term, IQTree subTree) {
        return (term instanceof Variable)
                ? extractTypeFromVariable((Variable)term, subTree)
                : extractType((NonVariableTerm) term);
    }

    private Optional<TermType> extractTypeFromVariable(Variable variable, IQTree subTree) {
        throw new RuntimeException("TODO: extract type from variables");
    }

    /**
     * At the moment, we only extract types from:
     *    - ground terms
     *    - non ground functional terms that are able to define their target type independently
     *      of the children variable types
     */
    private Optional<TermType> extractType(NonVariableTerm nonVariableTerm) {
        return nonVariableTerm.inferType()
                // TODO: shall we care here about non fatal error?
                .flatMap(TermTypeInference::getTermType);
    }
}
