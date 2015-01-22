package org.semanticweb.ontop.owlrefplatform.core.unfolding;

import fj.data.List;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.TypeProposal;

/**
 * For URI templates using more than one variable.
 *
 * TODO: implement it
 */
public class MultiVariateUriTemplateTypeProposal extends TypeProposalImpl {

    /**
     * TODO: update it
     */
    public MultiVariateUriTemplateTypeProposal(Function proposedHead) {
        super(proposedHead);
    }

    @Override
    public List<CQIE> applyType(List<CQIE> initialRules) {
        return null;
    }

    @Override
    public List<CQIE> removeType(List<CQIE> initialRules) {
        return null;
    }
}
