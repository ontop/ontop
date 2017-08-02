package it.unibz.inf.ontop.substitution;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableDataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;

/**
 * Declaration that the substitution is immutable and only refer to ImmutableTerms.
 */
public interface ImmutableSubstitution<T extends ImmutableTerm> extends LocallyImmutableSubstitution {

    ImmutableMap<Variable, T> getImmutableMap();

    boolean isDefining(Variable variable);

    ImmutableSet<Variable> getDomain();

    @Override
    T get(Variable variable);

    /**
     * Applies the substitution to an immutable term.
     */
    ImmutableTerm apply(ImmutableTerm term);

    /**
     * This method can be applied to simple variables
     */
    ImmutableTerm applyToVariable(Variable variable);

    ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm);

    /**
     * Please use applyToFunctionalTerm() instead if you can.
     */
    Function applyToMutableFunctionalTerm(Function mutableFunctionalTerm);

    ImmutableExpression applyToBooleanExpression(ImmutableExpression booleanExpression);

    /**
     * Only guaranteed for T extends VariableOrGroundTerm.
     * <p>
     * If T == ImmutableTerm, throws a ConversionException if
     * a substituted term is not a VariableOrGroundTerm.
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
     * TODO: explain
     */
    Optional<ImmutableSubstitution<? extends ImmutableTerm>> unionHeterogeneous(
            ImmutableSubstitution<? extends ImmutableTerm> other);

    /**
     * Applies the current substitution to the "target" part of another substitution
     */
    ImmutableSubstitution<ImmutableTerm> applyToTarget(ImmutableSubstitution<? extends ImmutableTerm> otherSubstitution);

    /**
     * Returns a "similar" substitution that avoids (if possible) to substitute certain variables.
     * <p>
     * Acts on equality between variables.
     * <p>
     * The first variable in the list has the highest priority.
     * <p>
     * This method requires the domain and the range to be disjoint.
     */
    ImmutableSubstitution<T> orientate(ImmutableList<Variable> priorityVariables);

    Optional<ImmutableExpression> convertIntoBooleanExpression();

    Var2VarSubstitution getVar2VarFragment();

    ImmutableSubstitution<GroundTerm> getVar2GroundTermFragment();

    /**
     * Reduces the substitution's domain to its intersection with the argument domain
     */
    ImmutableSubstitution<T> reduceDomainToIntersectionWith(ImmutableSet<Variable> restrictingDomain);

}
