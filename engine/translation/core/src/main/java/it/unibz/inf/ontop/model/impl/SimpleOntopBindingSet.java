package it.unibz.inf.ontop.model.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.OntopBinding;
import it.unibz.inf.ontop.model.OntopBindingSet;
import it.unibz.inf.ontop.model.term.Constant;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SimpleOntopBindingSet implements OntopBindingSet{

    private final ResultSet jdbcResultSet;
    private final List<String> signature;
    private final Map<String, Integer> columnMap;
    private final OntopConstantRetrieverFromJdbcResultSet constantRetriever;

    public SimpleOntopBindingSet(ResultSet jdbcResultSet, List<String> signature, Map<String, Integer> columnMap,
                                 OntopConstantRetrieverFromJdbcResultSet constantRetriever) {
        this.jdbcResultSet = jdbcResultSet;
        this.signature = signature;
        this.columnMap = columnMap;
        this.constantRetriever = constantRetriever;
    }

    @Override
    public Iterator<OntopBinding> iterator() {
        final ImmutableList.Builder<OntopBinding> bindingsBuilder = ImmutableList.builder();
        for(String name: signature){
            try {
                bindingsBuilder.add(getBinding(name));
            } catch (OntopConnectionException | OntopResultConversionException e) {
                // ugly bit
                throw new RuntimeException(e);
            }
        }

        return bindingsBuilder.build().iterator();
    }

    @Override
    public List<String> getBidingNames() {
        return this.signature;
    }

    /**
     *
     * @param column 1-based
     *
     */
    @Override
    public OntopBinding getBinding(int column) throws OntopConnectionException, OntopResultConversionException {
        return new SimpleOntopBinding(signature.get(column-1), getConstant(column));
    }

    @Override
    public OntopBinding getBinding(String name) throws OntopConnectionException, OntopResultConversionException {
        return new SimpleOntopBinding(name, getConstant(name));
    }

    /***
     * Returns the constant at column "column" recall that columns start at index 1.
     */
    @Override
    public Constant getConstant(int column) throws OntopConnectionException, OntopResultConversionException {
        return constantRetriever.getConstantFromJDBC(jdbcResultSet, column);
    }

    @Override
    public Constant getConstant(String name) throws OntopConnectionException, OntopResultConversionException {
        Integer columnIndex = columnMap.get(name);
        return getConstant(columnIndex);
    }
}
