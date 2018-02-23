package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.Iterator;

import static java.util.stream.Collectors.joining;

public abstract class AbstractOntopBindingSet implements OntopBindingSet {

    protected final ImmutableList<String> signature;

    protected AbstractOntopBindingSet(ImmutableList<String> signature) {
        this.signature = signature;
    }

    @Override
    @Nonnull
    public Iterator<OntopBinding> iterator() {
        ImmutableList<OntopBinding> bindings = computeAllBindings();
        return bindings.iterator();
    }

    private ImmutableList<OntopBinding> computeAllBindings() {
        final ImmutableList.Builder<OntopBinding> bindingsBuilder = ImmutableList.builder();
        for (String name : signature) {
            final OntopBinding binding = getBinding(name);
            if (binding != null)
                bindingsBuilder.add(binding);
        }
        return bindingsBuilder.build();
    }

    @Override
    public ImmutableList<String> getBindingNames() {
        return this.signature.stream().filter(this::hasBinding).collect(ImmutableCollectors.toList());
    }

    @Override
    public String toString() {
        return computeAllBindings().stream().map(OntopBinding::toString)
                .collect(joining(",", "[", "]"));
    }
}
