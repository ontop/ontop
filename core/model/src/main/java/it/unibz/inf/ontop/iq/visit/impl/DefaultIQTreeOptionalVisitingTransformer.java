package it.unibz.inf.ontop.iq.visit.impl;

import java.util.Optional;

public class DefaultIQTreeOptionalVisitingTransformer<T> extends AbstractIQTreeVisitingTransformer<Optional<T>> {

    protected final Optional<T> done() {
        return Optional.empty();
    }
}
