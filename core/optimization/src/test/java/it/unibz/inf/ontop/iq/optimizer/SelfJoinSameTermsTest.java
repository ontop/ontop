package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.DBConstant;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

public class SelfJoinSameTermsTest {

    public final static RelationDefinition T1_AR3;

    static {
        OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        T1_AR3 = builder.createRelationWithStringAttributes(1, 3, true);
    }

    @Test
    public void testSelfJoinElimination1() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(T1_AR3, ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode2 = createExtensionalDataNode(T1_AR3, ImmutableList.of(D, B, C));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, B, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnaryIQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, constructionTree));

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(
                T1_AR3, ImmutableMap.of(1, B, 2, C));

        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createFilterNode(
                                                TERM_FACTORY.getConjunction(
                                                        TERM_FACTORY.getDBIsNotNull(B),
                                                        TERM_FACTORY.getDBIsNotNull(C))),
                                        newDataNode2
                                )));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Ignore("TODO: try to support this quite complex case")
    @Test
    public void testSelfJoinElimination2() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(T1_AR3, ImmutableList.of(A, B, C));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(T1_AR3, ImmutableList.of(D, B, C));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(T1_AR3, ImmutableList.of(A, E, F));

        ExtensionalDataNode dataNode4 = createExtensionalDataNode(T1_AR3, ImmutableList.of(D, G, H));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2, dataNode3, dataNode4));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, B, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnaryIQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, constructionTree));

        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(
                                        constructionNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createFilterNode(
                                        TERM_FACTORY.getConjunction(
                                                TERM_FACTORY.getDBIsNotNull(A),
                                                TERM_FACTORY.getDBIsNotNull(B),
                                                TERM_FACTORY.getDBIsNotNull(C))),
                                        dataNode1
                                ))));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testSelfJoinElimination3() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(T1_AR3, ImmutableList.of(A, B, C));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(T1_AR3, ImmutableList.of(D, B, C));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(T1_AR3, ImmutableList.of(E, B, C));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2, dataNode3));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, B, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnaryIQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, constructionTree));

        ExtensionalDataNode newDataNode3 = IQ_FACTORY.createExtensionalDataNode(
                T1_AR3, ImmutableMap.of(1, B, 2, C));

        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createFilterNode(
                                                TERM_FACTORY.getConjunction(
                                                        TERM_FACTORY.getDBIsNotNull(B),
                                                        TERM_FACTORY.getDBIsNotNull(C))),
                                        newDataNode3
                                )));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testSelfJoinElimination4() {

        DBConstant constant = TERM_FACTORY.getDBStringConstant("plop");

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(T1_AR3, ImmutableList.of(A, constant, C));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(T1_AR3, ImmutableList.of(D, constant, C));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(T1_AR3, ImmutableList.of(E, constant, C));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2, dataNode3));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnaryIQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, constructionTree));

        ExtensionalDataNode newDataNode3 = IQ_FACTORY.createExtensionalDataNode(
                T1_AR3, ImmutableMap.of(1, constant, 2, C));

        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(C)),
                                        newDataNode3
                                )));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testSelfJoinElimination5() {

        DBConstant constant = TERM_FACTORY.getDBStringConstant("plop");

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(T1_AR3, ImmutableList.of(A, constant, C));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(T1_AR3, ImmutableList.of(D, constant, C));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(T1_AR3, ImmutableList.of(E, B, C));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2, dataNode3));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnaryIQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, constructionTree));

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(
                T1_AR3, ImmutableMap.of(1, constant, 2, C));

        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(C)),
                                        newDataNode2
                                )));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testSelfJoinElimination6() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(T1_AR3, ImmutableList.of(A, B, C));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(T1_AR3, ImmutableList.of(D, B, C));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnaryIQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, constructionTree));

        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createFilterNode(
                                                TERM_FACTORY.getConjunction(
                                                        TERM_FACTORY.getDBIsNotNull(B),
                                                        TERM_FACTORY.getDBIsNotNull(C))),
                                        dataNode1
                                )));



        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Ignore("TODO: try to support this quite complex case")
    @Test
    public void testSelfJoinElimination7() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(T1_AR3, ImmutableList.of(A, B, C));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(T1_AR3, ImmutableList.of(D, B, I));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(T1_AR3, ImmutableList.of(A, E, F));
        ExtensionalDataNode dataNode4 = createExtensionalDataNode(T1_AR3, ImmutableList.of(D, G, H));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2, dataNode3, dataNode4));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, B, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnaryIQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, constructionTree));

        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                IQ_FACTORY.createUnaryIQTree(
                                IQ_FACTORY.createFilterNode(
                                        TERM_FACTORY.getConjunction(
                                                TERM_FACTORY.getDBIsNotNull(A),
                                                TERM_FACTORY.getDBIsNotNull(B))),
                                dataNode1
                        ))));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    /**
     * No distinct, no optimization
     */
    @Test
    public void testSelfJoinNonElimination1() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                T1_AR3, ImmutableMap.of( 1, B, 2, C));
        ExtensionalDataNode dataNode2 = dataNode1;

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, B, C);

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                joinTree);

        optimizeAndCompare(initialQuery, initialQuery);
    }

    @Test
    public void testSelfJoinNonElimination2() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(T1_AR3, ImmutableList.of(A, B, D));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(T1_AR3, ImmutableList.of(E, F, C));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, B, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnaryIQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree);

        UnaryIQTree distinctTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createDistinctNode(), constructionTree);

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                distinctTree);

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(
                T1_AR3, ImmutableMap.of(1,B));

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(
                T1_AR3, ImmutableMap.of(2,C));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(newDataNode1, newDataNode2));

        UnaryIQTree newDistinctTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createDistinctNode(), newJoinTree);
        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                newDistinctTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testSelfJoinNonElimination2bis() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, B, C);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                T1_AR3, ImmutableMap.of(1,B));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                T1_AR3, ImmutableMap.of(2,C));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        UnaryIQTree newDistinctTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createDistinctNode(), joinTree);
        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                newDistinctTree);

        optimizeAndCompare(initialQuery, initialQuery);
    }

    @Test
    public void testSelfJoinNonElimination3() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                T1_AR3, ImmutableMap.of(0, A, 1,B));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                T1_AR3, ImmutableMap.of(0, A, 2,C));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, B, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnaryIQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree);

        UnaryIQTree distinctTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createDistinctNode(), constructionTree);

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                distinctTree);


        optimizeAndCompare(initialQuery, initialQuery);
    }

    @Test
    public void testSelfJoinNonElimination4() {

        DBConstant constant1 = TERM_FACTORY.getDBStringConstant("cst1");
        DBConstant constant2 = TERM_FACTORY.getDBStringConstant("cst2");

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(T1_AR3, ImmutableList.of(constant1, B, C));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(T1_AR3, ImmutableList.of(constant2, B, C));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, B, C);

        UnaryIQTree distinctTree = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createDistinctNode(), joinTree);

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                distinctTree);


        optimizeAndCompare(initialQuery, initialQuery);
    }

    @Test
    public void testSelfJoinElimination8() {
        ExtensionalDataNode dataNode = createExtensionalDataNode(T1_AR3, ImmutableList.of(A, B, C));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode, dataNode));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, A, B, C);


        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, joinTree));

        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(
                                IQ_FACTORY.createFilterNode(
                                        TERM_FACTORY.getConjunction(
                                                TERM_FACTORY.getDBIsNotNull(A),
                                                TERM_FACTORY.getDBIsNotNull(B),
                                                TERM_FACTORY.getDBIsNotNull(C))),
                                dataNode
                        )));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    private void optimizeAndCompare(IQ initialQuery, IQ expectedQuery) {
        IQ optimizedQuery = JOIN_LIKE_OPTIMIZER.optimize(initialQuery);
        assertEquals(expectedQuery, optimizedQuery);
    }
}
