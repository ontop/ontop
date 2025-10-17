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
    protected RightProvenanceNormalizer(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
    }

    private VariableNullability getRightNullability(IQTree rightTree,
                                                   Optional<ImmutableExpression> leftJoinExpression) {
        ImmutableSet<Variable> rightVariables = rightTree.getVariables();

        var optionalFilter = iqTreeTools.createOptionalFilterNode(leftJoinExpression.flatMap(e -> termFactory.getConjunction(
                e.flattenAND().filter(e1 -> rightVariables.containsAll(e1.getVariables())))));
        
        return iqTreeTools.unaryIQTreeBuilder()
                .append(optionalFilter)
                .build(rightTree)
                .getVariableNullability();
    }

    public RightProvenance normalizeRightProvenance(IQTree rightTree, ImmutableSet<Variable> leftVariables,
                                                    VariableGenerator variableGenerator,
                                                    Optional<ImmutableExpression> leftJoinExpression) {

        var rightNullability = getRightNullability(rightTree, leftJoinExpression);

        Optional<Variable> nonNullableRightVariable = getNonNullableRightVariable(rightTree, leftVariables, rightNullability);

        return nonNullableRightVariable
                .map(variable -> new RightProvenance(variable, rightTree))
                .orElseGet(() -> createProvenanceInConstructionNode(variableGenerator.generateNewVariable(PROV), rightTree, rightTree.getVariables()));
    }

    public Optional<Variable> getNonNullableRightVariable(IQTree rightTree, ImmutableSet<Variable> leftVariables, VariableNullability rightNullability) {
        return rightTree.getVariables().stream()
                .filter(v -> !leftVariables.contains(v))
                .filter(v -> !rightNullability.isPossiblyNullable(v))
                .findFirst();
    }

    public RightProvenance createProvenanceInConstructionNode(Variable provenanceVariable, IQTree rightTree,
                                                               ImmutableSet<Variable> rightRequiredVariables) {

        ConstructionNode newRightConstructionNode = iqTreeTools.createExtendingConstructionNode(
                rightRequiredVariables,
                substitutionFactory.getSubstitution(provenanceVariable, termFactory.getProvenanceSpecialConstant()));

        UnaryIQTree newRightTree = iqFactory.createUnaryIQTree(newRightConstructionNode, rightTree);
        return new RightProvenance(provenanceVariable, newRightTree);
    }

    /**
     * Elements that keep track that the right part contributed to the intermediate results:
     *
     * - provenance variable: right-specific, not nullable on the right
     * - right tree: may have been updated so as to provide the provenance variable
     */
    public class RightProvenance {

        private final Variable provenanceVariable;
        private final IQTree tree;

        protected RightProvenance(Variable provenanceVariable, IQTree tree) {
            this.provenanceVariable = provenanceVariable;
            this.tree = tree;
        }

        public Variable getProvenanceVariable() {
            return provenanceVariable;
        }

        public ImmutableExpression getProvenanceExpression() {
            return termFactory.getDBIsNotNull(provenanceVariable);
        }

        public IQTree getTree() {
            return tree;
        }
    }

}
