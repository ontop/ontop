package it.unibz.inf.ontop.model;

import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.Constant;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public interface OntopBindingSet extends Iterable<OntopBinding> {
    
    @Override
    Iterator<OntopBinding> iterator();

    List<String> getBindingNames();

    /***
     * Returns the constant at column "column" recall that columns start at index 1.
     *
     * @param column The column index of the value to be returned, start at 1
     * @return a constant
     */
    @Nullable
    Constant getConstant(int column) throws OntopResultConversionException;

    @Nullable
    Constant getConstant(String name) throws OntopResultConversionException;

    @Nullable
    OntopBinding getBinding(int column);

    @Nullable
    OntopBinding getBinding(String name);

    /**
     * Checks whether this BindingSet has a binding with the specified name.
     *
     * @param bindingName
     *        The name of the binding.
     * @return <tt>true</tt> if this BindingSet has a binding with the specified name, <tt>false</tt>
     *         otherwise.
     */
    boolean hasBinding(String bindingName);
}
