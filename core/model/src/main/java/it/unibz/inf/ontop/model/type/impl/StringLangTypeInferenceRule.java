package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

/**
 * Make sure that, e.g., CONCAT(6,8) does not return an integer but a literal.
 *
 */
public class StringLangTypeInferenceRule extends UnifierTermTypeInferenceRule {

    protected Optional<TermType> postprocessInferredType(Optional<TermType> optionalTermType) {
        return optionalTermType.map(TermTypeInferenceTools::castStringLangType);
    }
}
