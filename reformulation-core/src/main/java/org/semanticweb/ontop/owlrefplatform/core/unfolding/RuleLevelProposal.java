package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.Unifier;

/**
 * TODO: describe it
 */
public interface RuleLevelProposal {
    Unifier getSubstitution();

    CQIE getTypedRule();
}
