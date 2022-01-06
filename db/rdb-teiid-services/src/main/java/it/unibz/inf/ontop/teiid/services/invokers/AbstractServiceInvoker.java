package it.unibz.inf.ontop.teiid.services.invokers;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import it.unibz.inf.ontop.teiid.services.model.Service;
import it.unibz.inf.ontop.teiid.services.model.Tuple;

public class AbstractServiceInvoker implements ServiceInvoker {

    private final Service service;

    protected AbstractServiceInvoker(final Service service) {
        this.service = Objects.requireNonNull(service);
    }

    public Service getService() {
        return this.service;
    }

    public Iterator<Tuple> invoke(final Tuple tuple) {
        final Iterable<Tuple> tuples = new Iterable<Tuple>() {

            @Override
            public Iterator<Tuple> iterator() {
                return Iterators.singletonIterator(tuple);
            }
        };
        return invokeBatch(tuples).get(0);
    }

    public List<Iterator<Tuple>> invokeBatch(final Iterable<Tuple> tuples) {
        final Class<?> enclosingClass = tuples.getClass().getEnclosingClass();
        if (enclosingClass == AbstractServiceInvoker.class) {
            throw new Error(
                    "At least one of methods invoke() and invokeBatch() should be overridden");
        }
        final List<Iterator<Tuple>> result = Lists.newArrayList();
        for (final Tuple tuple : tuples) {
            result.add(invoke(tuple));
        }
        return result;
    }

}
