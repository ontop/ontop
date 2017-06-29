package it.unibz.inf.ontop.model.type.impl;


import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;


/**
 * Make sure that, e.g., SUBSTR(66,1) does not return an integer but a literal.
 *
 */
public class FirstStringLangArgTermTypeInferenceRule extends FirstArgumentTermTypeInferenceRule {

    protected Optional<TermType> postprocessInferredType(Optional<TermType> optionalTermType) {
        return optionalTermType.map(TermTypeInferenceTools::castStringLangType);
    }
}
