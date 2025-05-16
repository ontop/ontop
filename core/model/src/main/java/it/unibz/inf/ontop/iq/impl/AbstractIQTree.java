package it.unibz.inf.ontop.iq.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;

public abstract class AbstractIQTree implements IQTree {

    protected final IQTreeTools iqTreeTools;
    protected final IntermediateQueryFactory iqFactory;

    protected AbstractIQTree(IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory) {
        this.iqTreeTools = iqTreeTools;
        this.iqFactory = iqFactory;
    }
}
