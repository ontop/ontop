package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.TABLE1_AR2;
import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.TABLE3_AR1;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.iq.IsDistinctTest.NULLABLE_UC_TABLE1_AR1;
import static it.unibz.inf.ontop.iq.IsDistinctTest.NULLABLE_UC_TABLE2_AR2;
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
                .createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                                SUBSTITUTION_FACTORY.getSubstitution(X,  ONE_STR)),
                        IQ_FACTORY.createTrueNode());

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
        IQTree expectedTree = IQ_FACTORY
                .createUnaryIQTree(
                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                                SUBSTITUTION_FACTORY.getSubstitution(X,  TWO_STR)),
                        IQ_FACTORY.createTrueNode());

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
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createSliceNode(0, 1), dataNode),
                        IQ_FACTORY.createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR),
                                ImmutableList.of(TWO_STR), ImmutableList.of(THREE_STR)))));

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
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createSliceNode(0, 4),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                        ImmutableList.of(IQ_FACTORY.createValuesNode(ImmutableList.of(X),
                                        ImmutableList.of(ImmutableList.of(ONE_STR),
                                                ImmutableList.of(TWO_STR),
                                                ImmutableList.of(THREE_STR))),
                                IQ_FACTORY.createUnaryIQTree(
                                    IQ_FACTORY.createSliceNode(0, 1),
                                        dataNode),
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createSliceNode(0, 1),
                                        dataNode))));

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
                                dataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // Slice Distinct Union - SLICE DISTINCT UNION [T1 ...] -> SLICE DISTINCT UNION [LIMIT T1 ... ]
    // Case where T1 is a VALUES node but NOT distinct - i.e. no optimization for slice
    // However, this is the scenario where Distinct-Union is optimized, so it gets pushed down from the different rule
    @Test
    public void test10normalizationLimitDistinctUnionValues() {
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
    public void test11normalizationLimitDistinctUnionValues() {
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
    // Case where T1 and T2 are distinct - Limit gets pushed down for both
    @Test
    public void test12normalizationLimitDistinctUnionDistinctTree() {
        ExtensionalDataNode dataNode0 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE1_AR1, ImmutableMap.of(0, X));
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(NULLABLE_UC_TABLE2_AR2, ImmutableMap.of(0, X));

        UnaryIQTree tree0 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(X)),
                dataNode0);
        UnaryIQTree tree1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(X)),
                dataNode1);

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

    // UNION [CONSTRUCT TRUE] [CONSTRUCT TRUE] --> CONSTRUCT VALUES
    @Test
    public void test14normalizationConstructionUnionTrueTrue() {
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

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY
                .createValuesNode(ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // UNION [CONSTRUCT TRUE] [CONSTRUCT TRUE] T1 --> UNION [VALUES T1]
    @Test
    public void test15normalizationConstructionUnionTrueTrueDataNode() {
        ConstructionNode constructionNode0 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TWO_STR));
        IQTree tree0 = IQ_FACTORY.createUnaryIQTree(constructionNode0, IQ_FACTORY.createTrueNode());
        IQTree tree1 = IQ_FACTORY.createUnaryIQTree(constructionNode1, IQ_FACTORY.createTrueNode());
        ExtensionalDataNode dataNode0 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(tree0, tree1, dataNode0)
        );

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                    ImmutableList.of(IQ_FACTORY.createValuesNode(
                                ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR))),
                            dataNode0));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // 2 DBConstants of same datatype - optimization
    @Test
    public void test17normalizationConstructionUnionTrueTrueDBConstant() {
        DBConstant xValue0 = TERM_FACTORY.getDBConstant("alpha", TYPE_FACTORY.getDBTypeFactory().getDBStringType());
        DBConstant xValue1 = TERM_FACTORY.getDBConstant("beta", TYPE_FACTORY.getDBTypeFactory().getDBStringType());

        ConstructionNode constructionNode0 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue0));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue1));

        IQTree tree0 = IQ_FACTORY.createUnaryIQTree(constructionNode0, IQ_FACTORY.createTrueNode());
        IQTree tree1 = IQ_FACTORY.createUnaryIQTree(constructionNode1, IQ_FACTORY.createTrueNode());
        ExtensionalDataNode dataNode0 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(tree0, tree1, dataNode0));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(IQ_FACTORY.createValuesNode(
                                ImmutableList.of(X), ImmutableList.of(ImmutableList.of(xValue0),
                                ImmutableList.of(xValue1))),
                        dataNode0));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // 2 RDF Constants of different datatypes - no optimization
    @Test
    public void test18normalizationConstructionUnionTrueTrueRDFConstant() {
        RDFConstant xValue0 = TERM_FACTORY.getRDFConstant("http://example.org/ds1/1",
                TYPE_FACTORY.getXsdStringDatatype());
        RDFConstant xValue1 = TERM_FACTORY.getRDFConstant("2.53454",
                TYPE_FACTORY.getXsdDoubleDatatype());

        ConstructionNode constructionNode0 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue0));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue1));

        IQTree tree0 = IQ_FACTORY.createUnaryIQTree(constructionNode0, IQ_FACTORY.createTrueNode());
        IQTree tree1 = IQ_FACTORY.createUnaryIQTree(constructionNode1, IQ_FACTORY.createTrueNode());
        ExtensionalDataNode dataNode0 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(tree0, tree1, dataNode0)
        );

        // NOTE: Order of CONSTRUCT changes due to groupBy, but UNION operation is commutative thus no issue
        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(tree0, tree1, dataNode0));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // 2 RDF Constants same datatype - optimization
    @Test
    public void test19normalizationConstructionUnionTrueTrueRDFConstant() {
        RDFConstant xValue0 = TERM_FACTORY.getRDFConstant("http://example.org/ds1/1",
                TYPE_FACTORY.getXsdStringDatatype());
        RDFConstant xValue1 = TERM_FACTORY.getRDFConstant("http://example.org/ds1/2",
                TYPE_FACTORY.getXsdStringDatatype());
        DBConstant yValue0 = TERM_FACTORY.getDBConstant("http://example.org/ds1/1",
                TYPE_FACTORY.getDBTypeFactory().getDBStringType());
        DBConstant yValue1 = TERM_FACTORY.getDBConstant("http://example.org/ds1/2",
                TYPE_FACTORY.getDBTypeFactory().getDBStringType());

        ConstructionNode constructionNode0 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue0));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue1));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getRDFLiteralFunctionalTerm(A, TYPE_FACTORY.getXsdStringDatatype())));

        IQTree tree0 = IQ_FACTORY.createUnaryIQTree(constructionNode0, IQ_FACTORY.createTrueNode());
        IQTree tree1 = IQ_FACTORY.createUnaryIQTree(constructionNode1, IQ_FACTORY.createTrueNode());
        ExtensionalDataNode dataNode0 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(A));
        IQTree tree2 = IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode0);

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(tree0, tree1, tree2)
        );

        ExtensionalDataNode newDataNode = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(F0F1));

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X,
                                TERM_FACTORY.getRDFLiteralFunctionalTerm(F0F1, TYPE_FACTORY.getXsdStringDatatype()))),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(F0F1)),
                        ImmutableList.of(
                                IQ_FACTORY.createValuesNode(
                                        ImmutableList.of(F0F1),
                                        ImmutableList.of(ImmutableList.of(yValue0), ImmutableList.of(yValue1))),
                                newDataNode)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }


    // If the substitution in the CONSTRUCT has an IRIConstant (not DBConstant) skip optimization for that CONSTRUCT
    @Test
    public void test21normalizationConstructionUnionTrueTrueIRIConstant() {
        ConstructionNode constructionNode0 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TWO_STR));
        GroundFunctionalTerm xValue0 =
                (GroundFunctionalTerm) TERM_FACTORY.getIRIFunctionalTerm(
                        Template.builder().string("http://example.org/ds1/").placeholder().build(),
                        ImmutableList.of(ONE_STR));
        IRIConstant xValue1 = TERM_FACTORY.getConstantIRI("http://example.org/ds1/1");
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue0));
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue1));

        IQTree tree0 = IQ_FACTORY.createUnaryIQTree(constructionNode0, IQ_FACTORY.createTrueNode());
        IQTree tree1 = IQ_FACTORY.createUnaryIQTree(constructionNode1, IQ_FACTORY.createTrueNode());
        IQTree tree2 = IQ_FACTORY.createUnaryIQTree(constructionNode2, IQ_FACTORY.createTrueNode());
        IQTree tree3 = IQ_FACTORY.createUnaryIQTree(constructionNode3, IQ_FACTORY.createTrueNode());
        ExtensionalDataNode dataNode0 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(tree0, tree1, tree2, dataNode0)
        );

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(IQ_FACTORY.createValuesNode(
                                ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR))),
                        tree3,
                        dataNode0));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // If the substitution in the CONSTRUCT does not have a Constant skip optimization
    @Test
    public void test22normalizationConstructionUnionTrueTrueNonConstant() {
        ConstructionNode constructionNode0 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TWO_STR));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getDBIsNotNull(X)));

        IQTree tree0 = IQ_FACTORY.createUnaryIQTree(constructionNode0, IQ_FACTORY.createTrueNode());
        IQTree tree1 = IQ_FACTORY.createUnaryIQTree(constructionNode1, IQ_FACTORY.createTrueNode());
        IQTree tree2 = IQ_FACTORY.createUnaryIQTree(constructionNode2, IQ_FACTORY.createTrueNode());
        ExtensionalDataNode dataNode0 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(X));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(tree0, tree1, tree2, dataNode0)
        );

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(IQ_FACTORY.createValuesNode(
                                ImmutableList.of(X), ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR))),
                        tree2,
                        dataNode0));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    // 2 RDF Constants same datatype with more than one projected variable - optimization
    @Test
    public void test23normalizationConstructionUnionTrueTrueRDFConstant() {
        RDFConstant xValue0 = TERM_FACTORY.getRDFConstant("http://example.org/ds1/1",
                TYPE_FACTORY.getXsdStringDatatype());
        RDFConstant xValue1 = TERM_FACTORY.getRDFConstant("http://example.org/ds1/2",
                TYPE_FACTORY.getXsdStringDatatype());
        RDFConstant xValue2 = TERM_FACTORY.getRDFConstant("http://example.org/ds1/3",
                TYPE_FACTORY.getXsdStringDatatype());
        RDFConstant xValue3 = TERM_FACTORY.getRDFConstant("http://example.org/ds1/4",
                TYPE_FACTORY.getXsdStringDatatype());
        DBConstant yValue0 = TERM_FACTORY.getDBConstant("http://example.org/ds1/1",
                TYPE_FACTORY.getDBTypeFactory().getDBStringType());
        DBConstant yValue1 = TERM_FACTORY.getDBConstant("http://example.org/ds1/2",
                TYPE_FACTORY.getDBTypeFactory().getDBStringType());
        DBConstant yValue2 = TERM_FACTORY.getDBConstant("http://example.org/ds1/3",
                TYPE_FACTORY.getDBTypeFactory().getDBStringType());
        DBConstant yValue3 = TERM_FACTORY.getDBConstant("http://example.org/ds1/4",
                TYPE_FACTORY.getDBTypeFactory().getDBStringType());
        ImmutableFunctionalTerm zValue0 =
                TERM_FACTORY.getRDFFunctionalTerm(F0,
                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getXsdStringDatatype()));
        ImmutableFunctionalTerm zValue1 =
                TERM_FACTORY.getRDFFunctionalTerm(F1,
                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getXsdStringDatatype()));

        ConstructionNode constructionNode0 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue0, Y, xValue1));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue2, Y, xValue3));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, zValue0, Y, zValue1));

        IQTree tree0 = IQ_FACTORY.createUnaryIQTree(constructionNode0, IQ_FACTORY.createTrueNode());
        IQTree tree1 = IQ_FACTORY.createUnaryIQTree(constructionNode1, IQ_FACTORY.createTrueNode());


        // Create initial node
        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y)),
                ImmutableList.of(tree0, tree1)
        );

        // Create expected Tree
        // Additional logic due to lifted bindings in Union Node
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode2,
                        IQ_FACTORY.createValuesNode(
                                ImmutableList.of(F0, F1), ImmutableList.of(ImmutableList.of(yValue0, yValue1),
                        ImmutableList.of(yValue2, yValue3))));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    /**
     * 1 IRIConstant, 1 RDFLiteralConstant of same datatype - just lifting the RDF term
     * Could be further optimized if we start accepting TermTypeConstant in ValuesNode.
     *
     */
    @Test
    public void test24normalizationConstructionUnionTrueTrueRDFConstantSub() {
        ObjectRDFType iriType = TYPE_FACTORY.getIRITermType();
        RDFDatatype xsdStringType = TYPE_FACTORY.getXsdStringDatatype();
        RDFConstant xValue0 = TERM_FACTORY.getRDFConstant("http://example.org/ds1/1", iriType);
        RDFConstant xValue1 = TERM_FACTORY.getRDFConstant("alpha", xsdStringType);

        ConstructionNode constructionNode0 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue0));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, xValue1));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getRDFLiteralFunctionalTerm(A, xsdStringType)));

        IQTree tree0 = IQ_FACTORY.createUnaryIQTree(constructionNode0, IQ_FACTORY.createTrueNode());
        IQTree tree1 = IQ_FACTORY.createUnaryIQTree(constructionNode1, IQ_FACTORY.createTrueNode());
        ExtensionalDataNode dataNode0 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(A));
        IQTree tree2 = IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode0);

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(tree0, tree1, tree2)
        );

        Variable f0f2 = TERM_FACTORY.getVariable("f0f2");
        Variable f1f3 = TERM_FACTORY.getVariable("f1f3");

        ConstructionNode newConstructionNode0 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(f0f2, f1f3),
                SUBSTITUTION_FACTORY.getSubstitution(
                        f0f2, TERM_FACTORY.getDBStringConstant("http://example.org/ds1/1"),
                        f1f3, TERM_FACTORY.getRDFTermTypeConstant(iriType)
                        ));
        ConstructionNode newConstructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(f0f2, f1f3),
                SUBSTITUTION_FACTORY.getSubstitution(
                        f0f2, TERM_FACTORY.getDBStringConstant("alpha"),
                        f1f3, TERM_FACTORY.getRDFTermTypeConstant(xsdStringType)
                ));
        ConstructionNode newConstructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(f0f2, f1f3),
                SUBSTITUTION_FACTORY.getSubstitution(
                        f1f3, TERM_FACTORY.getRDFTermTypeConstant(xsdStringType)
                ));
        IQTree newTree0 = IQ_FACTORY.createUnaryIQTree(newConstructionNode0, IQ_FACTORY.createTrueNode());
        IQTree newTree1 = IQ_FACTORY.createUnaryIQTree(newConstructionNode1, IQ_FACTORY.createTrueNode());
        ExtensionalDataNode newDataNode0 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(f0f2));
        IQTree newTree2 = IQ_FACTORY.createUnaryIQTree(newConstructionNode2, newDataNode0);


        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X,
                                TERM_FACTORY.getRDFFunctionalTerm(f0f2, f1f3))),
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(f0f2, f1f3)),
                        ImmutableList.of(newTree0, newTree1, newTree2)));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test25NoVariableTrueNodesAndValuesNodes() {
        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of()),
                ImmutableList.of(
                        IQ_FACTORY.createTrueNode(),
                        IQ_FACTORY.createTrueNode(),
                        IQ_FACTORY.createValuesNode(
                                ImmutableList.of(),
                                ImmutableList.of(ImmutableList.of(), ImmutableList.of(), ImmutableList.of()))));

        IQTree expectedTree = IQ_FACTORY.createValuesNode(
                ImmutableList.of(),
                ImmutableList.of(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(),
                        ImmutableList.of(), ImmutableList.of()));

        assertTrue(baseTestNormalization(initialTree, expectedTree));
    }

    @Test
    public void test26MergeableCombinationOfTrueConstructionValuesNodes() {
        ConstructionNode constructionNode0 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, ONE_STR, Y, TWO_STR));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, TWO_STR));
        IQTree tree0 = IQ_FACTORY.createUnaryIQTree(constructionNode0, IQ_FACTORY.createTrueNode());
        IQTree tree1 = IQ_FACTORY.createUnaryIQTree(constructionNode1, IQ_FACTORY.createValuesNode(
                ImmutableList.of(Y),
                ImmutableList.of(ImmutableList.of(ONE_STR), ImmutableList.of(TWO_STR))));

        ValuesNode valuesNode2 = IQ_FACTORY.createValuesNode(
                ImmutableList.of(Y, X),
                ImmutableList.of(ImmutableList.of(THREE_STR, ONE_STR), ImmutableList.of(FOUR_STR, TWO_STR)));

        // Create initial node
        IQTree initialTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y)),
                ImmutableList.of(tree0, tree1, valuesNode2)
        );

        // Create expected Tree
        IQTree expectedTree = IQ_FACTORY.createValuesNode(
                ImmutableList.of(Y, X), ImmutableList.of(
                        ImmutableList.of(TWO_STR, ONE_STR),
                        ImmutableList.of(ONE_STR, TWO_STR), ImmutableList.of(TWO_STR, TWO_STR),
                        ImmutableList.of(THREE_STR, ONE_STR), ImmutableList.of(FOUR_STR, TWO_STR)));

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
