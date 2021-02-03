package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultIdentityIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * Provides a provenance variable for the right child of a LJ.
 *
 * Such a provenance variable is guaranteed to be non-null on the right.
 *
 * This provenance variable might be:
 *   - 1) an existing non-null variable not shared with the left child
 *   - 2) a fresh non-nullable variable inserted in a sparse data node of the right child
 *   - 3) a binding in a construction node (with a special constant, so as to be never lifted).
 *
 * This normalizer may alter the right child.
 *
 * Note that in the case #3, we take advantage of the inserted construction node for projecting away non-required variables
 *  from the right child (in anticipation of what would have been done later on otherwise).
 *
 *
 */
@Singleton
public class RightProvenanceNormalizer {

    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    protected RightProvenanceNormalizer(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.iqFactory = coreSingletons.getIQFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.termFactory = coreSingletons.getTermFactory();
    }

    public RightProvenance normalizeRightProvenance(IQTree rightTree, ImmutableSet<Variable> leftVariables,
                                                    Optional<ImmutableExpression> leftJoinExpression,
                                                    VariableGenerator variableGenerator) {
        ImmutableSet<Variable> rightVariables = rightTree.getVariables();

        VariableNullability rightNullability = leftJoinExpression
                .flatMap(e -> termFactory.getConjunction(
                        e.flattenAND()
                                .filter(e1 -> rightVariables.containsAll(e1.getVariables()))))
                .map(e -> iqFactory.createUnaryIQTree(
                        iqFactory.createFilterNode(e),
                        rightTree).getVariableNullability())
                .orElseGet(rightTree::getVariableNullability);

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

        if (nonNullableRightVariable.isPresent()) {
            return new RightProvenance(nonNullableRightVariable.get(), rightTree);
        }

        IQTree transformedRightTree = new FreshVariableTransformer(coreSingletons, variableGenerator)
                .transform(rightTree);

        return (transformedRightTree != rightTree) && (!transformedRightTree.getVariables().equals(rightVariables))
                ? extractProvenanceFromTransformedTree(transformedRightTree, rightVariables)
                : createProvenanceInConstructionNode(rightTree, rightRequiredVariables, variableGenerator);
    }

    private RightProvenance extractProvenanceFromTransformedTree(IQTree transformedRightTree, ImmutableSet<Variable> rightVariables) {
        Variable newVariable = Sets.difference(transformedRightTree.getVariables(), rightVariables).iterator().next();
        return new RightProvenance(newVariable, transformedRightTree);
    }

    private RightProvenance createProvenanceInConstructionNode(IQTree rightTree,
                                                               ImmutableSet<Variable> rightRequiredVariables,
                                                               VariableGenerator variableGenerator) {
        /*
         * Otherwise, creates a fresh variable and its construction node
         */
        Variable provenanceVariable = variableGenerator.generateNewVariable();

        ImmutableSet<Variable> newRightProjectedVariables =
                Stream.concat(
                        Stream.of(provenanceVariable),
                        rightRequiredVariables.stream())
                        .collect(ImmutableCollectors.toSet());

        ConstructionNode newRightConstructionNode = iqFactory.createConstructionNode(
                newRightProjectedVariables,
                substitutionFactory.getSubstitution(provenanceVariable,
                        termFactory.getProvenanceSpecialConstant()));

        UnaryIQTree newRightTree = iqFactory.createUnaryIQTree(newRightConstructionNode, rightTree);
        return new RightProvenance(provenanceVariable, newRightTree);
    }

    /**
     * Tries to insert a fresh variable into a sparse data node of the right tree
     */
    protected static class FreshVariableTransformer extends DefaultIdentityIQTreeVisitingTransformer {

        private final IntermediateQueryFactory iqFactory;
        private final VariableGenerator variableGenerator;

        public FreshVariableTransformer(CoreSingletons coreSingletons, VariableGenerator variableGenerator) {
            this.iqFactory = coreSingletons.getIQFactory();
            this.variableGenerator = variableGenerator;
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
            List<Attribute> attributes = dataNode.getRelationDefinition().getAttributes();
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap = dataNode.getArgumentMap();
            Optional<Integer> optionalIndex = IntStream.range(0, attributes.size())
                    .filter(i -> !argumentMap.containsKey(i))
                    .filter(i -> !attributes.get(i).isNullable())
                    .boxed()
                    .findFirst();

            return optionalIndex
                    // Creates a fresh variable and inserts it into the map
                    .map(i -> Stream.concat(
                            argumentMap.entrySet().stream(),
                            Stream.of(Maps.immutableEntry(i, variableGenerator.generateNewVariable("prov"))))
                            .collect(ImmutableCollectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue)))
                    .map(m -> iqFactory.createExtensionalDataNode(dataNode.getRelationDefinition(), m))
                    .orElse(dataNode);
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            IQTree newLeftChild = leftChild.acceptTransformer(this);
            return  hasNotChanged(newLeftChild, leftChild)
                    // No variable inserted
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, rightChild);
        }

        boolean hasNotChanged(IQTree newChild, IQTree formerChild) {
            return newChild == formerChild || newChild.getVariables().equals(formerChild.getVariables());
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            for (int i=0; i < children.size(); i++) {
                IQTree child = children.get(i);
                IQTree newChild = child.acceptTransformer(this);
                if (!hasNotChanged(newChild, child)) {
                    int indexToReplace = i;
                    ImmutableList<IQTree> newChildren = IntStream.range(0, children.size())
                            .mapToObj(j -> j == indexToReplace ? newChild : children.get(j))
                            .collect(ImmutableCollectors.toList());

                    return iqFactory.createNaryIQTree(rootNode, newChildren);
                }
            }
            // No fresh variable inserted
            return tree;
        }
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
