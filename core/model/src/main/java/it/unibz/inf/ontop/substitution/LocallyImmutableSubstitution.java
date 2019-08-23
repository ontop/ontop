package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Substitution objects that cannot be updated
 * but that may refer to mutable functional terms.
 *
 * For a fully safe substitution, see ImmutableSubstitution.
 *
 * Implementations must not supported any mutable operation.
 *
 */
public interface LocallyImmutableSubstitution extends Substitution {

    /**
     * Please use use getImmutableMap instead
     */
    @Override
    ImmutableMap<Variable, Term> getMap();

    /**
     * Not supported (mutable operation)
     *
     * TODO: remove this operation from Substitution and from here
     */
    @Deprecated
    @Override
    boolean compose(Substitution s);


    /**
     * Not supported (mutable operation)
     *
     * TODO: remove this operation from Substitution and from here
     */
    @Deprecated
    @Override
    boolean composeTerms(Term term1, Term term2);

    /**
     * Not supported (mutable operation)
     *
     * TODO: remove this operation from Substitution and from here
     */
    @Deprecated
    @Override
    boolean composeFunctions(Function term1, Function term2);

}
