package it.unibz.inf.ontop.model;

import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Declaration that the substitution is immutable and only refer to ImmutableTerms.
 *
 */
public interface ImmutableSubstitution<T extends ImmutableTerm> extends LocallyImmutableSubstitution {

    ImmutableMap<Variable, T> getImmutableMap();

    boolean isDefining(Variable variable);

    @Override
    T get(Variable variable);

   /**
    * Applies the substitution to an immutable term.
    */
    ImmutableTerm apply(ImmutableTerm term);

    /**
     * This method can be applied to simple variables 
     * 
     */
    ImmutableTerm applyToVariable(Variable variable);

    ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm);

    /**
     * Please use applyToFunctionalTerm() instead if you can.
     */
    Function applyToMutableFunctionalTerm(Function mutableFunctionalTerm);

    ImmutableBooleanExpression applyToBooleanExpression(ImmutableBooleanExpression booleanExpression);

    /**
     * Only guaranteed for T extends VariableOrGroundTerm.
     *
     * If T == ImmutableTerm, throws a ConversionException if
     * a substituted term is not a VariableOrGroundTerm.
     *
     */
    DataAtom applyToDataAtom(DataAtom atom) throws ConversionException;

    DistinctVariableDataAtom applyToDistinctVariableDataAtom(DistinctVariableDataAtom dataAtom)
            throws ConversionException;

    DistinctVariableOnlyDataAtom applyToDistinctVariableOnlyDataAtom(DistinctVariableOnlyDataAtom projectionAtom)
        throws ConversionException;

    /**
     * Returns "f o g" where f is this substitution
     */
    ImmutableSubstitution<ImmutableTerm> composeWith(ImmutableSubstitution<? extends ImmutableTerm> g);

    /**
     * Because of the optional cannot be overloaded.
     */
    Optional<ImmutableSubstitution<T>> union(ImmutableSubstitution<T> otherSubstitution);

    /**
     * Applies the current substitution to the "target" part of another substitution
     */
    ImmutableSubstitution<ImmutableTerm> applyToTarget(ImmutableSubstitution<? extends ImmutableTerm> otherSubstitution);

    /**
     * Returns a similar substitution that avoids (if possible) to substitute certain variables.
     */
    ImmutableSubstitution<T> orientate(ImmutableSet<Variable> variablesToTryToKeep);

    Optional<ImmutableBooleanExpression> convertIntoBooleanExpression();
}
