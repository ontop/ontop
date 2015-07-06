package org.semanticweb.ontop.model;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 * Declaration that the substitution is immutable and only refer to ImmutableTerms.
 *
 */
public interface ImmutableSubstitution<T extends ImmutableTerm> extends LocallyImmutableSubstitution {

    ImmutableMap<VariableImpl, T> getImmutableMap();

    boolean isDefining(VariableImpl variable);

    @Override
    T get(VariableImpl variable);

   /**
    * Applies the substitution to an immutable term.
    */
    ImmutableTerm apply(ImmutableTerm term);

    ImmutableTerm applyToVariable(VariableImpl variable);

    ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm);

    ImmutableBooleanExpression applyToBooleanExpression(ImmutableBooleanExpression booleanExpression);

    /**
     * Only guaranteed for T extends VariableOrGroundTerm.
     *
     * If T == ImmutableTerm, throws a ConversionException if
     * a substituted term is not a VariableOrGroundTerm.
     *
     */
    DataAtom applyToDataAtom(DataAtom atom) throws ConversionException;

    /**
     * Returns "f o g" where f is this substitution
     */
    ImmutableSubstitution<ImmutableTerm> composeWith(ImmutableSubstitution<? extends ImmutableTerm> g);

    /**
     * Because of the optional cannot be overloaded.
     */
    Optional<ImmutableSubstitution<T>> union(ImmutableSubstitution<T> otherSubstitution);
}
