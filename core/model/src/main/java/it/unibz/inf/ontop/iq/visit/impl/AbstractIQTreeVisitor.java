package it.unibz.inf.ontop.iq.visit.impl;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.visit.IQTreeVisitor;

public abstract class AbstractIQTreeVisitor<T> implements IQTreeVisitor<T> {

    public final T transform(IQTree tree) {
        return tree.acceptVisitor(this);
    }
}
