package it.unibz.inf.ontop.teiid.services.invokers;

import java.util.Iterator;
import java.util.Objects;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import it.unibz.inf.ontop.teiid.services.model.Service;
import it.unibz.inf.ontop.teiid.services.model.Tuple;
import it.unibz.inf.ontop.teiid.services.util.Iteration;

public class AbstractServiceInvoker implements ServiceInvoker {

    private final Service service;

    protected AbstractServiceInvoker(final Service service) {
        this.service = Objects.requireNonNull(service);
    }

    @Override
    public Service getService() {
        return this.service;
    }

    @Override
    public Iterator<Tuple> invoke(final Tuple tuple) {
        final Iterable<Tuple> tuples = new Iterable<Tuple>() {

            @Override
            public Iterator<Tuple> iterator() {
                return Iterators.singletonIterator(tuple);
            }
        };
        return invokeBatch(tuples);
    }

    @Override
    public Iterator<Tuple> invokeBatch(final Iterable<Tuple> tuples) {
        final Class<?> enclosingClass = tuples.getClass().getEnclosingClass();
        if (enclosingClass == AbstractServiceInvoker.class) {
            throw new Error(
                    "At least one of methods invoke() and invokeBatch() should be overridden");
        }
        return Iteration.concat( //
                Iteration.transform(tuples.iterator(), inputTuple -> invoke(inputTuple)));
    }

}
