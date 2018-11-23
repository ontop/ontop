package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

import static java.util.stream.Collectors.joining;

public abstract class AbstractOntopBindingSet implements OntopBindingSet {

    /* Integer values start at 0 */
    /* (note: ImmutableMap preserves ordering) */
    private final ImmutableMap<String, Integer> bindingName2Index;

    /**
     * Lazy
     */
    @Nullable
    private ImmutableList<RDFConstant> values;
    @Nullable
    private ImmutableList<OntopBinding> bindings;

    AbstractOntopBindingSet(ImmutableMap<String, Integer> bindingName2Index) {
        this.bindingName2Index = bindingName2Index;
        this.bindings = null;
        this.values = null;
    }

    @Override
    @Nonnull
    public Iterator<OntopBinding> iterator() {
        return getBindings().iterator();
    }

    @Override
    public ImmutableList<OntopBinding> getBindings() {
        if (bindings == null) {
            bindings = computeBindings();
        }
        return bindings;
    }

    @Override
    public ImmutableList<RDFConstant> getValues() {
        if (values == null) {
            values = computeValues();
        }
        return values;
    }

    @Override
    public ImmutableList<String> getBindingNames() {
        return ImmutableList.copyOf(bindingName2Index.keySet());
    }

    @Nullable
    @Override
    public RDFConstant getConstant(int column) {
        return getValues().get(column - 1);
    }

    @Nullable
    @Override
    public RDFConstant getConstant(String name) {
        return getValues().get(bindingName2Index.get(name));
    }

    @Override
    public String toString() {
        return getBindings().stream()
                .map(OntopBinding::toString)
                .collect(joining(",", "[", "]"));
    }

    @Override
    public boolean hasBinding(String bindingName) {
        return bindingName2Index.containsKey(bindingName);
    }

    @Override
    @Nullable
    public OntopBinding getBinding(String name) {
        return getBindings().get(bindingName2Index.get(name));
    }

    @Override
    @Nullable
    public OntopBinding getBinding(int index) {
        return getBindings().get(index - 1);
    }

    private ImmutableList<OntopBinding> computeBindings() {
        Iterator<RDFConstant> it = getValues().iterator();
        return bindingName2Index.keySet().stream()
                .map(k -> new OntopBindingImpl(
                        k,
                        it.next()
                ))
                .collect(ImmutableCollectors.toList());
    }

    protected abstract ImmutableList<RDFConstant> computeValues();

}
