package it.unibz.inf.ontop.teiid.services;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import it.unibz.inf.ontop.teiid.services.util.Signature;
import it.unibz.inf.ontop.teiid.services.util.Tuple;

public abstract class AbstractService implements Service {

    private final String name;

    private final Signature inputSignature;

    private final Signature outputSignature;

    protected AbstractService(final String name, final Signature inputSignature,
            final Signature outputSignature) {
        this.name = Objects.requireNonNull(name);
        this.inputSignature = Objects.requireNonNull(inputSignature);
        this.outputSignature = Objects.requireNonNull(outputSignature);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Signature getInputSignature() {
        return this.inputSignature;
    }

    @Override
    public Signature getOutputSignature() {
        return this.outputSignature;
    }

    @Override
    public Iterator<Tuple> invoke(final Tuple tuple) {
        final Iterable<Tuple> tuples = new Iterable<Tuple>() {

            @Override
            public Iterator<Tuple> iterator() {
                return Iterators.singletonIterator(tuple);
            }
        };
        return invokeBatch(tuples).get(0);
    }

    @Override
    public List<Iterator<Tuple>> invokeBatch(final Iterable<Tuple> tuples) {
        final Class<?> enclosingClass = tuples.getClass().getEnclosingClass();
        if (enclosingClass == AbstractService.class) {
            throw new Error(
                    "At least one of methods invoke() and invokeBatch() should be overridden");
        }
        final List<Iterator<Tuple>> result = Lists.newArrayList();
        for (final Tuple tuple : tuples) {
            result.add(invoke(tuple));
        }
        return result;
    }

    @Override
    public String toString() {
        return this.name + "(" + Joiner.on(", ").join(this.inputSignature) + "): ("
                + Joiner.on(", ").join(this.outputSignature) + ")";
    }

}
