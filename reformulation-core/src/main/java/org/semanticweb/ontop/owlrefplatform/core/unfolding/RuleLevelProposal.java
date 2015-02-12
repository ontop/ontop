package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Substitution;

/**
 * Derives a substitution at the level of one definition rule from the child proposals.
 *
 * Is also in charge of typing the rule.
 *
 */
public interface RuleLevelProposal {

    Substitution getTypingSubstitution();

    /**
     * Rule just after type propagation.
     */
    CQIE getTypedRule();

    /**
     * Rule after type removal.
     */
    CQIE getDetypedRule();
}
