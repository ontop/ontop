package it.unibz.inf.ontop.iq.visit.impl;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

public abstract class AbstractIQVisitor<T> implements IQVisitor<T> {

    protected final T transformChild(IQTree child) {
        return child.acceptVisitor(this);
    }

}
