package org.semanticweb.ontop.model;

import fj.data.List;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.TypeProposal;

/**
 * TODO: describe it
 */
public interface PredicateLevelProposal {
    List<CQIE> getTypedRules();

    TypeProposal getTypeProposal();

    Predicate getPredicate();
}
