package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertEquals;

public class FlattenUnionOptimizerTest {

    private final static AtomPredicate ANS1_PREDICATE1 = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");

    @Test
    public void flattenUnionTest1() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, X));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(X, Y, Z));

        ConstructionNode subConstructionNode1 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables());
        ConstructionNode subConstructionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables());

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createUnaryIQTree(subConstructionNode1,
                                        IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(
                                                dataNode2,
                                                IQ_FACTORY.createUnaryIQTree(subConstructionNode2,
                                                        IQ_FACTORY.createNaryIQTree(unionNode3, ImmutableList.of(dataNode3, dataNode4))))))))));
        System.out.println("\nBefore optimization: \n" + query1);
        IQ optimizedQuery = query1.normalizeForOptimization();
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                        IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, X)),
                        IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, X)),
                        IQ_FACTORY.createExtensionalDataNode(TABLE4_AR3, ImmutableMap.of(0, X)),
                        IQ_FACTORY.createExtensionalDataNode(TABLE5_AR3, ImmutableMap.of(0, X)))));

        System.out.println("\nExpected: \n" + query2);
        assertEquals(query2, optimizedQuery);
    }

    @Test
    public void flattenUnionTest2() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, X));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(X, Y, Z));

        ConstructionNode subConstructionNode1 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables());
        ConstructionNode subConstructionNode2 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables());

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                       dataNode1,
                        IQ_FACTORY.createUnaryIQTree(subConstructionNode1,
                                IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(dataNode2, dataNode3))),
                        IQ_FACTORY.createUnaryIQTree(subConstructionNode2,
                                IQ_FACTORY.createNaryIQTree(unionNode3, ImmutableList.of(dataNode4, dataNode5))))));
        System.out.println("\nBefore optimization: \n" + query1);
        IQ optimizedQuery = query1.normalizeForOptimization();
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                        dataNode1,
                        IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, X)),
                        IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, X)),
                        IQ_FACTORY.createExtensionalDataNode(TABLE4_AR3, ImmutableMap.of(0, X)),
                        IQ_FACTORY.createExtensionalDataNode(TABLE5_AR3, ImmutableMap.of(0, X)))));

        System.out.println("\nExpected: \n" + query2);
        assertEquals(query2, optimizedQuery);
    }


    @Test
    public void flattenUnionTest3() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, X));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE4_AR3, ImmutableMap.of(0, X, 1, Y));

        ConstructionNode subConstructionNode1 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables());

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                        dataNode1,
                        IQ_FACTORY.createUnaryIQTree(subConstructionNode1,
                                IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(
                                        dataNode4,
                                        IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(dataNode2, dataNode3))))))));

        System.out.println("\nBefore optimization: \n" + query1);
        IQ optimizedQuery = query1.normalizeForOptimization();
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IQ query2 = query1;
        assertEquals(query2, optimizedQuery);
    }

    @Test
    public void flattenUnionTest4() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, X));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode dataNode5 = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(X, Y, Z));

        ConstructionNode subConstructionNode1 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables());
        ConstructionNode subConstructionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables());

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                dataNode1,
                                IQ_FACTORY.createUnaryIQTree(subConstructionNode1,
                                        IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(
                                                dataNode2,
                                                IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(
                                                        IQ_FACTORY.createUnaryIQTree(subConstructionNode2,
                                                                IQ_FACTORY.createNaryIQTree(unionNode3, ImmutableList.of(dataNode4, dataNode5))),
                                                        dataNode3)))))))));

        System.out.println("\nBefore optimization: \n" + query1);
        IQ optimizedQuery = query1.normalizeForOptimization();
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        ConstructionNode newSubConstructionNode1 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables());

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                        IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, X)),
                        IQ_FACTORY.createUnaryIQTree(newSubConstructionNode1,
                                IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(
                                        dataNode2,
                                        IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(
                                                IQ_FACTORY.createExtensionalDataNode(TABLE4_AR3, ImmutableMap.of(0, X, 1, Y)),
                                                IQ_FACTORY.createExtensionalDataNode(TABLE5_AR3, ImmutableMap.of(0, X, 1, Y)),
                                                IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, X, 1, Y))))))))));

        System.out.println("\nExpected: \n" + query2);
        assertEquals(query2, optimizedQuery);
    }

    @Test
    public void flattenUnionTest5() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, X));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(X, Y));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(TABLE4_AR3, ImmutableList.of(X, Y, Z));

        ConstructionNode subConstructionNode1 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables());
        ConstructionNode subConstructionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables());

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                        dataNode1,
                        IQ_FACTORY.createUnaryIQTree(subConstructionNode1,
                                IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(subConstructionNode2,
                                                IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(
                                                        dataNode3, dataNode4
                                                ))),
                                        dataNode2
                                )))
                )));

        System.out.println("\nBefore optimization: \n" + query1);
        IQ optimizedQuery = query1.normalizeForOptimization();
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        ConstructionNode newSubConstructionNode1 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables());

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                        dataNode1,
                        IQ_FACTORY.createUnaryIQTree(newSubConstructionNode1,
                                IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(
                                        dataNode3,
                                        IQ_FACTORY.createExtensionalDataNode(TABLE4_AR3, ImmutableMap.of(0, X, 1, Y))
                                ))),
                        IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, X)))));

        System.out.println("\nExpected: \n" + query2);
        assertEquals(query2, optimizedQuery);
    }
}
