package unibz.inf.ontop.model;

/**
 * Term that is guaranteed to be immutable.
 *
 * In the future, every term should be immutable
 */
public interface ImmutableTerm extends Term {

    /**
     * Now is trivial to implement
     */
    @Override
    ImmutableTerm clone();

    boolean isGround();
}
