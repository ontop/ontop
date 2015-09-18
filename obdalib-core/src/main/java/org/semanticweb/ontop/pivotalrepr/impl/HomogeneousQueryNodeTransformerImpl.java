package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.*;


public abstract class HomogeneousQueryNodeTransformerImpl<T1 extends QueryNodeTransformationException,
        T2 extends QueryNodeTransformationException> implements HomogeneousQueryNodeTransformer<T1, T2> {

    /**
     * To be overwritten by "non-standard" sub-classes.
     * However standard sub-classes do not need to override it.
     */
    @Override
    public NonStandardNode transform(NonStandardNode nonStandardNode) throws T1, T2, NotNeededNodeException {
        throw new UnsupportedOperationException("Non-standard nodes are not supported by this class. " +
                "Please create a dedicated sub-class that overrides this method.");
    }
}
