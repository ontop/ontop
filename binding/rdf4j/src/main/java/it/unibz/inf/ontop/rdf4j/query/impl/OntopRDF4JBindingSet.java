package it.unibz.inf.ontop.rdf4j.query.impl;


import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.rdf4j.utils.RDF4JHelper;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.AbstractBindingSet;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.impl.SimpleBinding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class OntopRDF4JBindingSet extends AbstractBindingSet implements BindingSet {

    private static final long serialVersionUID = -8455466574395305166L;

    private OntopBindingSet ontopBindingSet;
    private final byte[] salt;

    public OntopRDF4JBindingSet(OntopBindingSet ontopBindingSet,
                                byte[] salt) {
        this.ontopBindingSet = ontopBindingSet;
        this.salt = salt;
    }

    @Override
    @Nullable
    public Binding getBinding(String bindingName) {
        OntopBinding ontopBinding = ontopBindingSet.getBinding(bindingName);
        return ontopBinding == null?
                null:
                convertBinding(ontopBinding);
    }

    @Override
    public Set<String> getBindingNames() {
        return Arrays.stream((ontopBindingSet.getBindingNames())).collect(Collectors.toSet());
    }

    @Override
    @Nullable
    public Value getValue(String variableName) {
        try {
            final RDFConstant constant = ontopBindingSet.getConstant(variableName);
            return constant == null?
                    null:
                    RDF4JHelper.getValue(constant, salt);
        } catch (OntopResultConversionException e) {
            throw new RuntimeException(e);
        }
    }

    /** Inefficient*/
    @Override
    public boolean hasBinding(String bindingName) {
        return ontopBindingSet.hasBinding(bindingName);
    }

    @Override
    @Nonnull
    public Iterator<Binding> iterator() {
        return Arrays.stream(ontopBindingSet.getBindings())
                .map(this::convertBinding)
                .iterator();
    }

    private Binding convertBinding(OntopBinding ontopBinding) {
//        try {
            return new SimpleBinding(
                    ontopBinding.getName(),
                    RDF4JHelper.getValue(ontopBinding.getValue(), salt)
            );
//        } catch (OntopResultConversionException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public int size() {
        return ontopBindingSet.getBindingNames().length;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }
}
