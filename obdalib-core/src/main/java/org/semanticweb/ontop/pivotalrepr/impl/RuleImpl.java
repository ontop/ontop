package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.DataAtom;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.Rule;

/**
 * TODO: explain
 */
public class RuleImpl implements Rule {

    private final DataAtom headAtom;
    private final IntermediateQuery bodyQuery;

    protected RuleImpl(DataAtom headAtom, IntermediateQuery body) {
        this.headAtom = headAtom;
        this.bodyQuery = body;
        // TODO: check the consistency between the head and the body (no free variable in the head)
    }

    @Override
    public DataAtom getHead() {
        return headAtom;
    }

    @Override
    public IntermediateQuery getBody() {
        return bodyQuery;
    }
}
