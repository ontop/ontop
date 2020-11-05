package it.unibz.inf.ontop.substitution;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;

/**
 * Declaration that the substitution is immutable and only refer to ImmutableTerms.
 *
 * See SubstitutionFactory for creating new instances
 *
 * NB: implementations depend of the AtomFactory
 */
public interface ImmutableSubstitution<T extends ImmutableTerm> extends ProtoSubstitution<T> {

    /**
     * Only guaranteed for T extends VariableOrGroundTerm.
     * <p>
     * If T == ImmutableTerm, throws a ConversionException if
     * a substituted term is not a VariableOrGroundTerm.
     */
    <P extends AtomPredicate> DataAtom<P> applyToDataAtom(DataAtom<P> atom) throws ConversionException;

    /**
     * Only guaranteed for T extends VariableOrGroundTerm.
     * <p>
     * If T == ImmutableTerm, throws a ConversionException if
     * a substituted term is not a VariableOrGroundTerm.
     */
    ImmutableMap<Integer, ? extends VariableOrGroundTerm> applyToArgumentMap(ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap)
            throws ConversionException;

    DistinctVariableOnlyDataAtom applyToDistinctVariableOnlyDataAtom(DistinctVariableOnlyDataAtom projectionAtom)
            throws ConversionException;

    /**
     * Viewing a substitution as a function (takes a term, returns a term).
     * This method yield the substitution "(g o f)" where g is this substitution.
     * NB: (g o f)(x) = g(f(x))
     */
    ImmutableSubstitution<ImmutableTerm> composeWith(ImmutableSubstitution<? extends ImmutableTerm> f);

    ImmutableSubstitution<T> composeWith2(ImmutableSubstitution<? extends T> f);

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
    ImmutableSubstitution<VariableOrGroundTerm> getVariableOrGroundTermFragment();
    ImmutableSubstitution<NonGroundFunctionalTerm> getNonGroundFunctionalTermFragment();
    ImmutableSubstitution<GroundFunctionalTerm> getGroundFunctionalTermFragment();
    ImmutableSubstitution<NonFunctionalTerm> getNonFunctionalTermFragment();
    ImmutableSubstitution<ImmutableFunctionalTerm> getFunctionalTermFragment();
    ImmutableSubstitution<NonVariableTerm> getNonVariableTermFragment();
    ImmutableSubstitution<Constant> getConstantFragment();
    ImmutableSubstitution<GroundTerm> getGroundTermFragment();

    /**
     * Reduces the substitution's domain to its intersection with the argument domain
     */
    ImmutableSubstitution<T> reduceDomainToIntersectionWith(ImmutableSet<Variable> restrictingDomain);

    ImmutableSubstitution<ImmutableTerm> simplifyValues(VariableNullability variableNullability);

    ImmutableSubstitution<ImmutableTerm> simplifyValues();

    TermFactory getTermFactory();

}
