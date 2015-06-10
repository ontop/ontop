package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.pivotalrepr.DetypingProposal;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.InvalidLocalOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

/**
 * Immutable
 */
public class DetypingProposalImpl extends LocalOptimizationProposalImpl implements DetypingProposal {

    private final ImmutableMap<Function, Variable> typesToReplace;

    public DetypingProposalImpl(QueryNode queryNode, IntermediateQuery targetQuery,
                                ImmutableMap<Function, Variable> typesToReplace) {
        super(queryNode, targetQuery);
        this.typesToReplace = typesToReplace;
    }

    @Override
    public ImmutableMap<Function, Variable> getTypesToReplace() {
        return typesToReplace;
    }

    @Override
    public QueryNode apply() throws InvalidLocalOptimizationProposalException {
        return getTargetQuery().applyDetypingProposal(this);
    }
}
