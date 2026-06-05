package it.unibz.inf.ontop.generation.algebra;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.model.term.Variable;

public interface IQTree2SelectFromWhereConverter {

    /**
     * TODO: explicit the assumptions on how the iqTree is supposed to be normalized
     */
    SelectFromWhereWithModifiers convert(IQTree iqTree, ImmutableSet<Variable> signature);
}
