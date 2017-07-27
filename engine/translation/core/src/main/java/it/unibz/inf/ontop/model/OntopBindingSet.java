package it.unibz.inf.ontop.model;

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.Constant;

import java.util.Iterator;
import java.util.List;

public interface OntopBindingSet extends Iterable<Binding> {
    
    @Override
    Iterator<Binding> iterator();

    List<String> getBidingNames();

    /***
     * Returns the constant at column "column" recall that columns start at index 1.
     *
     * @param column The column index of the value to be returned, start at 1
     * @return a constant
     */
    Constant getConstant(int column) throws OntopConnectionException, OntopResultConversionException;

    Constant getConstant(String name) throws OntopConnectionException, OntopResultConversionException;

    Binding getBinding(int column) throws OntopConnectionException, OntopResultConversionException;

    Binding getBinding(String name) throws OntopConnectionException, OntopResultConversionException;
}
