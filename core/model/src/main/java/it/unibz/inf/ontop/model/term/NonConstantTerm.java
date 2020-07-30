package it.unibz.inf.ontop.model.term;

public interface NonConstantTerm extends ImmutableTerm {

    @Override
    default boolean isNull() {
        return false;
    }
}
