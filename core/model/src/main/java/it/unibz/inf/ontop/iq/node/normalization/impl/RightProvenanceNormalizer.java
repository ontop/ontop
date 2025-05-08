package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 *
 * Provides a provenance variable for the right child of a LJ.
 *
 * Such a provenance variable is guaranteed to be non-null on the right.
 *
 * This provenance variable might be:
 *   - 1) an existing non-null variable not shared with the left child
 *   - 2) a binding in a construction node (with a special constant, so as to be never lifted).
 *
 * This normalizer may alter the right child.
 *
 * Note that in the case #2, we take advantage of the inserted construction node for projecting away non-required variables
 *  from the right child (in anticipation of what would have been done later on otherwise).
 *
 *
 */
@Singleton
public class RightProvenanceNormalizer {

    public static final String PROV = "prov";
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    protected RightProvenanceNormalizer(CoreSingletons coreSingletons, IQTreeTools iqTreeTools) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = iqTreeTools;
    }

    public RightProvenance normalizeRightProvenance(IQTree rightTree, ImmutableSet<Variable> leftVariables,
                                                    Optional<ImmutableExpression> leftJoinExpression,
                                                    VariableGenerator variableGenerator) {
        ImmutableSet<Variable> rightVariables = rightTree.getVariables();

        VariableNullability rightNullability = iqTreeTools.createOptionalUnaryIQTree(
                        leftJoinExpression
                                .flatMap(e -> termFactory.getConjunction(
                                        e.flattenAND()
                                                .filter(e1 -> rightVariables.containsAll(e1.getVariables()))))
                                .map(iqFactory::createFilterNode),
                        rightTree)
                .getVariableNullability();

        return normalizeRightProvenance(rightTree, leftVariables, rightTree.getVariables(), variableGenerator,
                rightNullability);
    }

    public RightProvenance normalizeRightProvenance(IQTree rightTree, ImmutableSet<Variable> leftVariables,
                                                    ImmutableSet<Variable> rightRequiredVariables,
                                                    VariableGenerator variableGenerator) {
        return normalizeRightProvenance(rightTree, leftVariables, rightRequiredVariables, variableGenerator,
                rightTree.getVariableNullability());
    }

    private RightProvenance normalizeRightProvenance(IQTree rightTree, ImmutableSet<Variable> leftVariables,
                                                    ImmutableSet<Variable> rightRequiredVariables,
                                                    VariableGenerator variableGenerator,
                                                    VariableNullability rightNullability) {
        ImmutableSet<Variable> rightVariables = rightTree.getVariables();

        Optional<Variable> nonNullableRightVariable = rightVariables.stream()
                .filter(v -> !leftVariables.contains(v))
                .filter(v -> !rightNullability.isPossiblyNullable(v))
                .findFirst();

        return nonNullableRightVariable
                .map(variable -> new RightProvenance(variable, rightTree))
                .orElseGet(() -> createProvenanceInConstructionNode(rightTree, rightRequiredVariables, variableGenerator));

    }

    private RightProvenance createProvenanceInConstructionNode(IQTree rightTree,
                                                               ImmutableSet<Variable> rightRequiredVariables,
                                                               VariableGenerator variableGenerator) {
        /*
         * Otherwise, creates a fresh variable and its construction node
         */
        Variable provenanceVariable = variableGenerator.generateNewVariable(PROV);

        ImmutableSet<Variable> newRightProjectedVariables = Sets.union(ImmutableSet.of(provenanceVariable), rightRequiredVariables)
                .immutableCopy();

        ConstructionNode newRightConstructionNode = iqFactory.createConstructionNode(
                newRightProjectedVariables,
                substitutionFactory.getSubstitution(provenanceVariable,
                        termFactory.getProvenanceSpecialConstant()));

        UnaryIQTree newRightTree = iqFactory.createUnaryIQTree(newRightConstructionNode, rightTree);
        return new RightProvenance(provenanceVariable, newRightTree);
    }

    /**
     * Elements that keep track that the right part contributed to the intermediate results:
     *
     * - provenance variable: right-specific, not nullable on the right
     * - right tree: may have been updated so as to provide the provenance variable
     */
    public static class RightProvenance {

        private final Variable variable;
        private final IQTree rightTree;

        protected RightProvenance(Variable provenanceVariable, IQTree rightTree) {
            this.variable = provenanceVariable;
            this.rightTree = rightTree;
        }

        public Variable getProvenanceVariable() {
            return variable;
        }

        public IQTree getRightTree() {
            return rightTree;
        }
    }

}
