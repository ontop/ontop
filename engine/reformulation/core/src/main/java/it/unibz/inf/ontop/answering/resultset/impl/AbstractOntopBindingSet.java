package it.unibz.inf.ontop.answering.resultset.impl;

import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.model.term.RDFConstant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

public abstract class AbstractOntopBindingSet implements OntopBindingSet {

    private final OntopBinding[] bindings;

    // LAZY
    @Nullable
    private String uuid;

    AbstractOntopBindingSet(OntopBinding[] bindings) {
        this.bindings = bindings;
    }

    @Override
    @Nonnull
    public Iterator<OntopBinding> iterator() {
        return Arrays.stream(getBindings()).iterator();
    }

    @Override
    public OntopBinding[] getBindings() {
        return bindings;
    }

    @Override
    public String[] getBindingNames() {
        String[] bindingNames = new String[bindings.length];
        for (int i = 0; i < bindings.length; i++) {
            bindingNames[i] = bindings[i].getName();
        }
        return bindingNames;
    }

    @Nullable
    @Override
    public RDFConstant getConstant(String name) {
        OntopBinding binding = getBinding(name);
        return (binding == null)
                ? null
                : binding.getValue();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(bindings[0].toString());
        for (int i = 1; i < bindings.length; i++){
            builder.append(bindings[i].toString()).append(",");
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean hasBinding(String bindingName) {
        return indexOf(bindingName) >= 0;
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
        int index = indexOf(name);
        if (index < 0){
            return null;
        }
        return bindings[index];
    }

    /**
     * Finds the index of the binding name. If none match, -1 is returned.
     *
     * @param bindingName is the binding's name
     * @return the index of the binding inside the array
     */
    private int indexOf(String bindingName){
        for (int i = 0; i < bindings.length; i++){
            if (bindings[i].getName().equals(bindingName)){
                return i;
            }
        }
        return -1;
    }
}
