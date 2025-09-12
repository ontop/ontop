package it.unibz.inf.ontop.iq.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractIQTree implements IQTree {

    protected final IQTreeTools iqTreeTools;
    protected final IntermediateQueryFactory iqFactory;

    protected AbstractIQTree(IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory) {
        this.iqTreeTools = iqTreeTools;
        this.iqFactory = iqFactory;
    }

    protected <T> @Nonnull T getCachedValue(Supplier<T> supplier, Supplier<T> constructor, Consumer<T> storer) {
        // Non-final
        T value = supplier.get();
        if (value == null) {
            value = constructor.get();
            storer.accept(value);
        }
        return value;
    }

}
