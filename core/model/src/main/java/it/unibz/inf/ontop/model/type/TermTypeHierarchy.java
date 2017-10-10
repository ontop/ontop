package it.unibz.inf.ontop.model.type;

import java.util.stream.Stream;

public interface TermTypeHierarchy<T extends TermType> {

    /**
     * From the most specific to the most general ancestor
     */
    Stream<T> getTermTypes();

    boolean contains(T termType);
}
