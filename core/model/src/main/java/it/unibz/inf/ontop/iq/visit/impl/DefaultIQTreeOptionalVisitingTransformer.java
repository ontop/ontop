package it.unibz.inf.ontop.iq.visit.impl;

import java.util.Optional;

/**
 * A superclass for {@code IQTreeVisitor} that transforms a given {@code IQTree} into an {@code Optional<T>}
 *
 * The default implementation of all visitor methods is non-recursive
 * and simply returns the empty {@code Optional}.
 *
 * @param <T>
 */

public class DefaultIQTreeOptionalVisitingTransformer<T> extends AbstractIQTreeGenericVisitingTransformer<Optional<T>> {

    protected final Optional<T> done() {
        return Optional.empty();
    }
}
