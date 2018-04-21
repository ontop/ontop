package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.Constant;

import javax.annotation.Nullable;
import java.util.List;

public class DelegatedIriSQLBindingSet extends AbstractOntopBindingSet implements OntopBindingSet {

    private final JDBC2ConstantConverter constantRetriever;
    private final List<MainTypeLangValues> row;
    private final ImmutableMap<String, Integer> columnMap;

    public DelegatedIriSQLBindingSet(List<MainTypeLangValues> row, ImmutableList<String> signature, ImmutableMap<String, Integer> columnMap,
                                     JDBC2ConstantConverter constantRetriever) {
        super(signature);
        this.row = row;
        this.constantRetriever = constantRetriever;
        this.columnMap = columnMap;
    }

    /**
     * @param column 1-based
     */
    @Override
    @Nullable
    public OntopBinding getBinding(int column) {
        return variableName2BindingMap.isPresent() ?
                variableName2BindingMap.get().get(signature.get(column - 1)) :
                computeBinding(column);
    }

    @Override
    public boolean hasBinding(String bindingName) {
        return variableName2BindingMap.isPresent()?
                variableName2BindingMap.get().containsKey(bindingName):
                signature.contains(bindingName) &&
                        row.get(columnMap.get(bindingName) - 1).getMainValue() != null;
    }

    /***
     * Returns the constant at column "column". Recall that columns start at index 1.
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
        return columnIndex == null?
                null:
                getConstant(columnIndex);
    }

    @Override
    protected OntopBinding computeBinding(String variableName) {
        return computeBinding(columnMap.get(variableName));
    }

    private OntopBinding computeBinding(int column) {
        final MainTypeLangValues cell = row.get(column - 1);
        if (cell.getMainValue() == null) {
            return null;
        } else {
            return new SQLOntopBinding(signature.get(column - 1), cell, constantRetriever);
        }
    }
}
