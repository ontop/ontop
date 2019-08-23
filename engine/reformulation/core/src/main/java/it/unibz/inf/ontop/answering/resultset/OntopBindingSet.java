package it.unibz.inf.ontop.answering.resultset;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.Constant;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.stream.Stream;

public interface OntopBindingSet extends Iterable<OntopBinding> {

    @Override
    Iterator<OntopBinding> iterator();

    Stream<OntopBinding> getBindings();

    ImmutableList<String> getBindingNames();

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

    /** If all bindings are needed, less efficient than getBindings() or the iterator*/
    @Nullable
    OntopBinding getBinding(int column);

    /** If all bindings are needed, less efficient than getBindings() or the iterator*/
    @Nullable
    OntopBinding getBinding(String name);

    /**
     * Checks whether this BindingSet has a binding with the specified name.
     * If the binding value is needed, getBinding() is more efficient
     *
     * @param bindingName
     *        The name of the binding.
     * @return <tt>true</tt> if this BindingSet has a binding with the specified name, <tt>false</tt>
     *         otherwise.
     */
    boolean hasBinding(String bindingName);
}
