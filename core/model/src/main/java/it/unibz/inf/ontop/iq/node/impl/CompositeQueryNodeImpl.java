package it.unibz.inf.ontop.iq.node.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

public abstract class CompositeQueryNodeImpl implements QueryNode {

    protected final IntermediateQueryFactory iqFactory;
    protected final SubstitutionFactory substitutionFactory;
    protected final TermFactory termFactory;
    protected final IQTreeTools iqTreeTools;

    protected CompositeQueryNodeImpl(SubstitutionFactory substitutionFactory, TermFactory termFactory, IntermediateQueryFactory iqFactory, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
        this.iqTreeTools = iqTreeTools;
    }
}
