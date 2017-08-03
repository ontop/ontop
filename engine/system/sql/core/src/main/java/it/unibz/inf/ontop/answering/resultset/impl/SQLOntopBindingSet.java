package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.model.term.Constant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class SQLOntopBindingSet implements OntopBindingSet {

    private final List<MainTypeLangValues> row;
    private final List<String> signature;
    private final Map<String, Integer> columnMap;
    private final JDBC2ConstantConverter constantRetriever;

    public SQLOntopBindingSet(List<MainTypeLangValues> row, List<String> signature, Map<String, Integer> columnMap,
                              JDBC2ConstantConverter constantRetriever) {
        this.row = row;
        this.signature = signature;
        this.columnMap = columnMap;
        this.constantRetriever = constantRetriever;
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
    public List<String> getBindingNames() {
        return this.signature.stream().filter(this::hasBinding).collect(Collectors.toList());
    }

    /**
     * @param column 1-based
     */
    @Override
    @Nullable
    public OntopBinding getBinding(int column) {
        final MainTypeLangValues cell = row.get(column - 1);
        if (cell.getMainValue() == null) {
            return null;
        } else {
            return new SQLOntopBinding(signature.get(column - 1), cell, constantRetriever);
        }

    }

    @Override
    @Nullable
    public OntopBinding getBinding(String name) {
        return getBinding(columnMap.get(name));
    }

    @Override
    public boolean hasBinding(String bindingName) {
        return signature.contains(bindingName) &&
                row.get(columnMap.get(bindingName) - 1).getMainValue() != null;
    }

    /***
     * Returns the constant at column "column" recall that columns start at index 1.
     */
    @Override
    @Nullable
    public Constant getConstant(int column) throws OntopResultConversionException {
        final MainTypeLangValues cell = row.get(column - 1);
        if (cell.getMainValue() == null) {
            return null;
        } else {
            return constantRetriever.getConstantFromJDBC(cell);
        }
    }

    @Override
    @Nullable
    public Constant getConstant(String name) throws OntopResultConversionException {
        Integer columnIndex = columnMap.get(name);
        return getConstant(columnIndex);
    }

    @Override
    public String toString() {
        return computeAllBindings().stream().map(OntopBinding::toString)
                .collect(joining(",", "[", "]"));
    }
}
