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
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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

    public RightProvenance normalizeRightProvenance(IQTree rightTree, ImmutableSet<Variable> leftVariables,
                                                    VariableGenerator variableGenerator,
                                                    VariableNullability rightNullability) {

        Optional<Variable> nonNullableRightVariable = rightTree.getVariables().stream()
                .filter(v -> !leftVariables.contains(v))
                .filter(v -> !rightNullability.isPossiblyNullable(v))
                .findFirst();

        if (nonNullableRightVariable.isPresent())
            return new RightProvenance(nonNullableRightVariable.get(), rightTree);

        Variable provenanceVariable = variableGenerator.generateNewVariable(PROV);
        return new RightProvenance(provenanceVariable, createProvenanceInConstructionNode(provenanceVariable, rightTree));
    }

    public UnaryIQTree createProvenanceInConstructionNode(Variable provenanceVariable, IQTree rightTree) {
        return createProvenanceInConstructionNode(provenanceVariable, rightTree, rightTree.getVariables());
    }

    public UnaryIQTree createProvenanceInConstructionNode(Variable provenanceVariable, IQTree rightTree, Set<Variable> rightRequiredVariables) {
        ConstructionNode newRightConstructionNode = iqTreeTools.createExtendingConstructionNode(
                rightRequiredVariables,
                substitutionFactory.getSubstitution(provenanceVariable, termFactory.getProvenanceSpecialConstant()));

        return iqFactory.createUnaryIQTree(newRightConstructionNode, rightTree);
    }

    /**
     * Elements that keep track that the right part contributed to the intermediate results:
     *
     * - provenance variable: right-specific, not nullable on the right
     * - right tree: may have been updated so as to provide the provenance variable
     */
    public static class RightProvenance {
        private final Variable provenanceVariable;
        private final IQTree tree;

        RightProvenance(Variable provenanceVariable, IQTree tree) {
            this.provenanceVariable = provenanceVariable;
            this.tree = tree;
        }

        public Variable getProvenanceVariable() {
            return provenanceVariable;
        }

        public IQTree getTree() {
            return tree;
        }
    }
}
