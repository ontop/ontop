package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;

import static java.util.stream.Collectors.joining;

public abstract class AbstractOntopBindingSet implements OntopBindingSet {

    //LinkedHashMap to preserve variable ordering
    private final LinkedHashMap<String, OntopBinding> bindingMap;

    // LAZY
    @Nullable
    private String uuid;

    AbstractOntopBindingSet(LinkedHashMap<String, OntopBinding> bindingMap) {
        this.bindingMap = bindingMap;
    }

    @Override
    @Nonnull
    public Iterator<OntopBinding> iterator() {
        return getBindings().iterator();
    }

    @Override
    public ImmutableList<OntopBinding> getBindings() {
        return bindingMap.values().stream()
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ImmutableList<RDFConstant> getValues() {
        return bindingMap.values().stream()
                .map(OntopBinding::getValue)
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ImmutableList<String> getBindingNames() {
        return bindingMap.keySet().stream()
                .collect(ImmutableCollectors.toList());
    }

    @Nullable
    @Override
    public RDFConstant getConstant(String name) {
        OntopBinding binding = bindingMap.get(name);
        return (binding == null)
                ? null
                : binding.getValue();
    }

    @Override
    public String toString() {
        return getBindings().stream()
                .map(OntopBinding::toString)
                .collect(joining(",", "[", "]"));
    }

    @Override
    public boolean hasBinding(String bindingName) {
        return bindingMap.containsKey(bindingName);
    }

    @Override
    public synchronized String getRowUUIDStr() {
        if (uuid == null)
            uuid = UUID.randomUUID().toString();
        return uuid;
    }

    @Override
    @Nullable
    public OntopBinding getBinding(String name) {
        return bindingMap.get(name);
    }
}
