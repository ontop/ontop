package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.HeterogeneousQueryNodeTransformer;
import org.semanticweb.ontop.pivotalrepr.NodeTransformationProposal;
import org.semanticweb.ontop.pivotalrepr.NonStandardNode;


public abstract class HeterogeneousQueryNodeTransformerImpl<P extends NodeTransformationProposal>
        implements HeterogeneousQueryNodeTransformer<P> {

    /**
     * To be overwritten by "non-standard" sub-classes.
     * However standard sub-classes do not need to override it.
     */
    @Override
    public P transform(NonStandardNode nonStandardNode) {
        throw new UnsupportedOperationException("Non-standard nodes are not supported by this class. " +
                "Please create a dedicated sub-class that overrides this method.");
    }
}
