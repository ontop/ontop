package it.unibz.inf.ontop.iq;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public interface IQTree {

    QueryNode getRootNode();

    ImmutableList<IQTree> getChildren();

    /**
     * Variables projected by the tree
     */
    ImmutableSet<Variable> getVariables();

    <T> T acceptVisitor(IQVisitor<T> visitor);

    IQTree normalizeForOptimization(VariableGenerator variableGenerator);

    /**
     * Tries to lift unions when they have incompatible definitions
     * for a variable.
     *
     * Union branches with compatible definitions are kept together
     *
     */
    IQTree liftIncompatibleDefinitions(Variable variable, VariableGenerator variableGenerator);

    default boolean isLeaf() {
        return getChildren().isEmpty();
    }

    /**
     * Applies the descending substitution and performs SOME optimizations.
     *
     * Designed to be called DURING the "structural/semantic optimization" phase.
     *
     */
    IQTree applyDescendingSubstitution(DownPropagation dp);

    /**
     * TODO: explain
     *
     * The constraint is used for pruning. It remains enforced by
     * a parent tree.
     *
     */
    IQTree propagateDownConstraint(DownPropagation dp);

    /**
     * Particular type of descending substitution: only renaming some variables by external ones.
     *
     * Isolated from regular descending substitutions as it preserves the properties of the tree
     * (e.g. it remains normalized if it was already)
     *
     */
    IQTree applyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution);

    /**
     * Applies the descending substitution WITHOUT applying any additional optimization.
     *
     * Designed to be called AFTER the "structural/semantic optimization" phase.
     */
    IQTree applyDescendingSubstitutionWithoutOptimizing(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                        VariableGenerator variableGenerator);

    /**
     * Variables present in the tree
     */
    ImmutableSet<Variable> getKnownVariables();

    /**
     * Returns true if the variable is (at least in one branch) constructed by a substitution
     * (in a construction node)
     */
    boolean isConstructed(Variable variable);

    /**
     * Returns true if it guarantees that all its results will be distinct
     */
    boolean isDistinct();

    /**
     * Returns true if corresponds to a EmptyNode
     */
    boolean isDeclaredAsEmpty();

    VariableNullability getVariableNullability();


    /**
     * TODO: find a better name
     */
    IQTree removeDistincts();

    void validate() throws InvalidIntermediateQueryException;


    /**
     * Returns a set of substitutions that define how the projected variables are constructed.
     * If a variable is sometimes not built (i.e. it is "free"), it will not appear in some substitutions.
     *
     * Guarantee: if variable appearing in all the substitutions, its values will always come from one the expressions
     * found in the substitution. Hence, its value is constrained.
     *
     * It is advised to call this method on a normalized IQ to reduce the number of possible substitutions
     * (normalization drops unsatisfiable substitutions).
     *
     *
     * The first intended usage of this method it to determine the definition of a variable from the returned substitution.
     * Therefore, this method is robust to simple variable renaming and to multiple complex substitutions.
     * E.g. if [x/x2], [x2/x3] and [x3/URI("http://myURI{}", x4] are three substitutions found in that order in a same
     * branch,
     * then x/RDF(http://myURI{}(x4), IRI) will be the only output substitution for that branch.
     *
     * It can also be used in the context of partial unfolding, where only some intensional data nodes have been unfolded,
     * not the others. In this context, it is common to have some SPARQL variables that remain free
     * (e.g. if a UNION is present).
     *
     * Property: after complete unfolding of the mapping, all the SPARQL variables will be constructed (i.e. not free).
     *
     */
    ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions();

    /**
     * NOT guaranteed to return all the unique constraints (MAY BE INCOMPLETE)
     *
     * Set of sets of determinants.
     *
     * Warning: some determinants may be nullable!
     */
    ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints();

    /**
     * NOT guaranteed to return all the functional dependencies (MAY BE INCOMPLETE)
     */
    FunctionalDependencies inferFunctionalDependencies();


    /**
     * Strict dependents are dependents of some functional dependencies that never appear as
     * determinants in any functional dependency.
     * Lightweight alternative to functional dependencies (cheaper to compute).
     */
    ImmutableSet<Variable> inferStrictDependents();

    /**
     * Variables that the tree proposes for removal if the ancestor trees do not need them.
     * Some variables can only be removed if some others are removed too.
     */
    VariableNonRequirement getVariableNonRequirement();
}
