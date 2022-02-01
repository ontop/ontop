package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.model.term.GroundFunctionalTerm;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.TABLE3_AR1;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.LT;
import static junit.framework.TestCase.assertTrue;

public class ValuesNodeTest {

    @Test
    public void test1normalization() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                    .createConstructionNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR)), IQ_FACTORY
                        .createTrueNode());

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test2normalization() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE_STR, TWO_STR), ImmutableList.of(ONE_STR, THREE_STR)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                    .createConstructionNode(ImmutableSet.of(X, Y), SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR)), IQ_FACTORY
                        .createValuesNode(ImmutableList.of(Y), ImmutableList.of(ImmutableList.of(TWO_STR), ImmutableList.of(THREE_STR))));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test3normalization() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE_STR, TWO_STR), ImmutableList.of(ONE_STR, TWO_STR)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                .createConstructionNode(ImmutableSet.of(X, Y), SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR, Y, TWO_STR)), IQ_FACTORY
                    .createValuesNode(ImmutableList.of(), ImmutableList.of(ImmutableList.of(), ImmutableList.of()))
        );

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test4normalization() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(), ImmutableList.of(ImmutableList.of()));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createTrueNode();

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test5normalization() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE_STR, TWO_STR), ImmutableList.of(THREE_STR, FOUR_STR)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE_STR, TWO_STR), ImmutableList.of(THREE_STR, FOUR_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test6substitutionNoChange() {
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR)));
        ImmutableSubstitution<VariableOrGroundTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(Y, ONE_STR);
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR)));

        assertTrue(baseTestApplyDescSubstitution(initialTree, substitution, expectedTree));
    }

    @Test
    public void test7substitutionConstant() {
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(
                        ImmutableList.of(ONE_STR, TWO_STR),
                        ImmutableList.of(ONE_STR, THREE_STR),
                        ImmutableList.of(FOUR_STR, FIVE_STR)));
        ImmutableSubstitution<VariableOrGroundTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR);
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(Y), ImmutableList.of(
                        ImmutableList.of(TWO_STR),
                        ImmutableList.of(THREE_STR)));

        assertTrue(baseTestApplyDescSubstitution(initialTree, substitution, expectedTree));
    }

    @Test
    public void test8substitutionFunction() {
        // Test handling of GroundFunctionalTerm
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR)));
        GroundFunctionalTerm groundFunctionalTerm = (GroundFunctionalTerm) TERM_FACTORY.getDBContains(ImmutableList.of(THREE_STR, FOUR_STR));
        ImmutableSubstitution<VariableOrGroundTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X, groundFunctionalTerm);

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                .createConstructionNode(ImmutableSet.of()),IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                    .createFilterNode(TERM_FACTORY.getStrictEquality(XF0, groundFunctionalTerm)), IQ_FACTORY
                        .createValuesNode(ImmutableList.of(XF0), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR)))));

        assertTrue(baseTestApplyDescSubstitution(initialTree, substitution, expectedTree));
    }

    @Test
    public void test9substitutionVariable() {
        // Test handling of GroundFunctionalTerm
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y, Z), ImmutableList.of(
                        ImmutableList.of(ONE_STR, TWO_STR, THREE_STR),
                        ImmutableList.of(TWO_STR, TWO_STR, FOUR_STR)));
        ImmutableSubstitution<VariableOrGroundTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X, Y, Z, W);

        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(Y, W), ImmutableList.of(ImmutableList.of(TWO_STR, FOUR_STR)));

        assertTrue(baseTestApplyDescSubstitution(initialTree, substitution, expectedTree));
    }

    @Test
    public void test10trivialSubstitutionVariable() {
        // Test handling of GroundFunctionalTerm
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(
                        ImmutableList.of(ONE_STR)));
        ImmutableSubstitution<VariableOrGroundTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X, Y);

        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(Y), ImmutableList.of(
                        ImmutableList.of(ONE_STR)));

        assertTrue(baseTestApplyDescSubstitution(initialTree, substitution, expectedTree));
    }

    @Test
    public void test11substitutionTriple() {
        // Test handling of GroundFunctionalTerm & NonFunctionalTerm
        IQTree initialTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X, Y, Z, W), ImmutableList.of(
                        ImmutableList.of(ONE_STR, TWO_STR, TWO_STR, TWO_STR),
                        ImmutableList.of(TWO_STR, TWO_STR, TWO_STR, ONE_STR),
                        ImmutableList.of(ONE_STR, TWO_STR, THREE_STR, FOUR_STR)));
        GroundFunctionalTerm groundFunctionalTerm = (GroundFunctionalTerm) TERM_FACTORY.getDBContains(ImmutableList.of(THREE_STR, FOUR_STR));
        ImmutableSubstitution<VariableOrGroundTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X, groundFunctionalTerm, Y, Z, W, ONE_STR);

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                .createConstructionNode(ImmutableSet.of(Z)),IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                    .createFilterNode(TERM_FACTORY.getStrictEquality(XF0, groundFunctionalTerm)), IQ_FACTORY
                        .createValuesNode(ImmutableList.of(XF0, Z), ImmutableList.of(
                                ImmutableList.of(TWO_STR, TWO_STR)))));

        assertTrue(baseTestApplyDescSubstitution(initialTree, substitution, expectedTree));
    }

    @Test
    public void test12propagateDownConstraint() {
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                .createFilterNode(TERM_FACTORY.getDBNumericInequality(LT, X, TERM_FACTORY.getDBIntegerConstant(2))), IQ_FACTORY
                    .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE, TWO), ImmutableList.of(TWO, ONE), ImmutableList.of(TWO, TWO))));

        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY
                .createFilterNode(TERM_FACTORY.getDBNumericInequality(LT, X, TERM_FACTORY.getDBIntegerConstant(2))), IQ_FACTORY
                    .createValuesNode(ImmutableList.of(X, Y), ImmutableList.of(ImmutableList.of(ONE, TWO))));

        assertTrue(baseTestPropagateDownConstraints(initialTree, expectedTree));
    }

    @Test
    public void test13normalizationSlice() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(1, 1),
                IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR), ImmutableList.of(THREE_STR))));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(TWO_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test14normalizationDistinct() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createDistinctNode(),
                IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(ONE_STR), ImmutableList.of(THREE_STR))));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(THREE_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test15normalizationSliceUnionValuesValues() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(2, 2),
                    IQ_FACTORY.createNaryIQTree(
                            IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                                    ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                        ImmutableList.of(TWO_STR),
                                        ImmutableList.of(THREE_STR))),
                            IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                    ImmutableList.of(ImmutableList.of(ONE_STR),
                                            ImmutableList.of(TWO_STR),
                                            ImmutableList.of(THREE_STR))))));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(THREE_STR), ImmutableList.of(ONE_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Values enough for whole limit
    @Test
    public void test16normalizationSliceUnionValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(1, 2),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(THREE_STR))),
                                dataNode)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(TWO_STR), ImmutableList.of(THREE_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Values covers only part of limit, remainder is single non-values node
    @Test
    public void test17normalizationSliceUnionValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(2, 2),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(THREE_STR))),
                                dataNode)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(
                IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(THREE_STR))),
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createSliceNode(0, 1), dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Values covers only part of limit, remainder is multiple non-values nodes
    @Test
    public void test18normalizationSliceUnionValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(2, 3),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(THREE_STR))),
                                dataNode,
                                dataNode)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(
                        IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(THREE_STR))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createSliceNode(0, 2),
                                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                                        ImmutableList.of(dataNode, dataNode)))));;

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Offset is too high - no optimization
    @Test
    public void test19normalizationSliceUnionValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(4, 2),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(THREE_STR))),
                                dataNode)));

        // Create expected Tree
        IQTree expectedTree = initialTree;

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Offset is equal to size of values node - no optimization
    @Test
    public void test20normalizationSliceUnionValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(3, 1),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(THREE_STR))),
                                dataNode)));

        // Create expected Tree
        IQTree expectedTree = initialTree;

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    private Boolean baseTestNormalization(IQTree initialTree, IQTree expectedTree) {
        System.out.println('\n' + "Tree before normalizing:");
        System.out.println(initialTree);
        System.out.println('\n' + "Expected tree:");
        System.out.println(expectedTree);
        IQTree normalizedTree = initialTree.normalizeForOptimization(CORE_UTILS_FACTORY
                .createVariableGenerator(initialTree.getVariables()));
        System.out.println('\n' + "Normalized tree:");
        System.out.println(normalizedTree);
        return normalizedTree.equals(expectedTree);
    }

    private Boolean baseTestApplyDescSubstitution(IQTree initialTree,
                                                  ImmutableSubstitution<VariableOrGroundTerm> substitution,
                                                  IQTree expectedTree) {
        System.out.println('\n' + "Tree before applying descending substitution without optimizing:");
        System.out.println(initialTree);
        System.out.println('\n' + "Substitution:");
        System.out.println(substitution);
        System.out.println('\n' + "Expected tree:");
        System.out.println(expectedTree);
        IQTree resultingTree = initialTree.applyDescendingSubstitutionWithoutOptimizing(substitution);
        System.out.println('\n' + "Resulting tree:");
        System.out.println(resultingTree);
        return resultingTree.equals(expectedTree);
    }

    private Boolean baseTestPropagateDownConstraints(IQTree initialTree,
                                                     IQTree expectedTree) {
        System.out.println('\n' + "Tree before propagating down constraint:");
        System.out.println(initialTree);
        System.out.println('\n' + "Expected tree:");
        System.out.println(expectedTree);
        IQTree resultingTree = initialTree.propagateDownConstraint(((FilterNode) initialTree.getRootNode()).getFilterCondition());
        System.out.println('\n' + "Resulting tree:");
        System.out.println(resultingTree);
        return resultingTree.equals(expectedTree);
    }
}
