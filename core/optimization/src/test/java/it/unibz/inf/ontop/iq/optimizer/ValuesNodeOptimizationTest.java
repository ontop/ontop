package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.TABLE1_AR2;
import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.TABLE3_AR1;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.TWO_STR;
import static it.unibz.inf.ontop.iq.IsDistinctTest.NULLABLE_UC_TABLE1_AR1;
import static junit.framework.TestCase.assertTrue;

public class ValuesNodeOptimizationTest {

    @Test
    public void test1normalizationSlice() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 1),
                IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR), ImmutableList.of(THREE_STR))));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Offset > 0 --> No optimization
    @Test
    public void test2normalizationSlice() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(1, 1),
                IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR), ImmutableList.of(THREE_STR))));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(1, 1),
                IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR), ImmutableList.of(THREE_STR))));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test3normalizationDistinct() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createDistinctNode(),
                IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(ONE_STR), ImmutableList.of(THREE_STR))));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(THREE_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Union node with values to be merged
    @Test
    public void test4normalizationSliceUnionValuesValues() {
        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 4),
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
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR),
                        ImmutableList.of(THREE_STR), ImmutableList.of(ONE_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Values Node records fully cover limit
    @Test
    public void test5normalizationSliceUnionValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 2),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(THREE_STR))),
                                dataNode)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Union node with multiple values nodes and at least one non-values node
    @Test
    public void test5normalizationSliceUnionValuesValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 4),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(THREE_STR))),
                                IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(FOUR_STR))),
                                dataNode)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR),
                        ImmutableList.of(THREE_STR), ImmutableList.of(ONE_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Values Node records cover only part of limit, remainder is single non-values nodes
    @Test
    public void test6normalizationSliceUnionValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 4),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(
                                dataNode,
                                IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(THREE_STR))))));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(
                        IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR),
                                ImmutableList.of(TWO_STR), ImmutableList.of(THREE_STR))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createSliceNode(0, 1), dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Values Node records cover only part of limit, remainder is multiple non-values nodes
    @Test
    public void test7normalizationSliceUnionValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 4),
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
                        IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR),
                                ImmutableList.of(TWO_STR), ImmutableList.of(THREE_STR))),
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createSliceNode(0, 1),
                                IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                                        ImmutableList.of(dataNode, dataNode)))));;

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Distinct union - DISTINCT UNION [VALUES T2 T3 ...] -> DISTINCT UNION [[DISTINCT VALUE] T2 T3 ...]
    // Values Node is distinct, thus Distinct Node not pushed down
    @Test
    public void test8normalizationDistinctUnionValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createDistinctNode(),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(THREE_STR))),
                                dataNode)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createDistinctNode(),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(
                                IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(THREE_STR))),
                                dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Distinct union - DISTINCT UNION [VALUES T2 T3 ...] -> DISTINCT UNION [[DISTINCT VALUE] T2 T3 ...]
    // Since the Values Node is NOT distinct, we push another Distinct Node down
    @Test
    public void test9normalizationDistinctUnionValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createDistinctNode(),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(TWO_STR))),
                                dataNode)));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createDistinctNode(),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(
                                IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR))),
                                dataNode)));;

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Slice Distinct Union - SLICE DISTINCT UNION [T1 ...] -> SLICE DISTINCT UNION [LIMIT T1 ... ]
    // Case where T1 is a VALUES node but NOT distinct - i.e. no optimization for slice
    // However, this is the scenario where Distinct-Union is optimized, so it gets pushed down from the different rule
    @Test
    public void test10normalizationLimitDistinctUnionValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 2),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                                ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                                ImmutableList.of(ImmutableList.of(ONE_STR),
                                                        ImmutableList.of(TWO_STR),
                                                        ImmutableList.of(TWO_STR))),
                                        dataNode))));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                ImmutableList.of(ImmutableList.of(ONE_STR),
                        ImmutableList.of(TWO_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Slice Distinct Union - SLICE DISTINCT UNION [T1 ...] -> SLICE DISTINCT UNION [LIMIT T1 ...] where T1 is a distinct Values Node
    // Case where T1 is a VALUES node and is distinct - i.e. push down limit
    // Since limit is covered by the Values Node, we only produce the Values Node as output
    @Test
    public void test11normalizationLimitDistinctUnionValuesNonValues() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 2),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                                ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                                ImmutableList.of(ImmutableList.of(ONE_STR),
                                                        ImmutableList.of(TWO_STR),
                                                        ImmutableList.of(THREE_STR))),
                                        dataNode))));

        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Limit Distinct Union - LIMIT DISTINCT UNION [T1 ...] -> LIMIT DISTINCT UNION [LIMIT T1 ... ]
    // Case where T1 and T2 are distinct - Limit gets pushed down only for T1
    @Test
    public void test12normalizationLimitDistinctUnionDistinctTree() {
        ExtensionalDataNode dataNode0 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE1_AR1, ImmutableMap.of(0, X));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE1_AR1, ImmutableMap.of(0, X));

        UnaryIQTree tree0 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(X)),
                dataNode0);
        UnaryIQTree tree1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(X)),
                dataNode0);

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 2),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                                ImmutableList.of(tree0, tree1))));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 2),
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createDistinctNode(),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createSliceNode(0, 2), tree0),
                                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createSliceNode(0, 2), tree1)))));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Limit Distinct Union - LIMIT DISTINCT UNION [T1 ...] -> LIMIT DISTINCT UNION [LIMIT T1 ... ]
    // Case where T1 is distinct but not T2 and T3
    // Limit gets pushed down only for T1
    @Test
    public void test13normalizationLimitDistinctUnionNonDistinctTree() {
        ExtensionalDataNode dataNode0 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE1_AR1, ImmutableMap.of(0, X));
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(X));

        UnaryIQTree tree0 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(X)),
                dataNode0);

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 2),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createNaryIQTree(
                                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                                ImmutableList.of(dataNode1, dataNode2, tree0))));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 2),
                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createDistinctNode(),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(
                                dataNode1,
                                dataNode2,
                                IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createSliceNode(0, 2), tree0)))));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // CONSTRUCT [TRUE TRUE] --> CONSTRUCT VALUES
    @Test
    public void test14normalizationConstructionUnionTrueTrue1() {
        ConstructionNode constructionNode0 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR));
        IQTree tree0 = IQ_FACTORY.createUnaryIQTree(constructionNode0, IQ_FACTORY.createTrueNode());
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TWO_STR));
        IQTree tree1 = IQ_FACTORY.createUnaryIQTree(constructionNode1, IQ_FACTORY.createTrueNode());

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(tree0, tree1)
        );

        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // CONSTRUCT [TRUE TRUE] --> CONSTRUCT VALUES
    @Test
    public void test15normalizationConstructionUnionTrueTrue2() {
        ConstructionNode constructionNode0 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR));
        IQTree tree0 = IQ_FACTORY.createUnaryIQTree(constructionNode0, IQ_FACTORY.createTrueNode());

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(tree0, tree0)
        );

        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(ONE_STR)));

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
}
