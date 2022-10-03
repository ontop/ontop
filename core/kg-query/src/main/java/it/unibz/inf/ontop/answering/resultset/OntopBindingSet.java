package it.unibz.inf.ontop.answering.resultset;

import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.RDFConstant;

import javax.annotation.Nullable;

import java.util.Iterator;

public interface OntopBindingSet extends Iterable<OntopBinding> {

    @Override
    Iterator<OntopBinding> iterator();

    OntopBinding[] getBindings();

    String[] getBindingNames();

    @Nullable
    RDFConstant getConstant(String name) throws OntopResultConversionException;

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

    String getRowUUIDStr();
}
