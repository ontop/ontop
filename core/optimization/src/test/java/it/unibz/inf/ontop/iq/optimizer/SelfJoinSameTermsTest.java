package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.BasicDBMetadata;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

public class SelfJoinSameTermsTest {

    public static RelationPredicate T1_AR3;

    static {
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        T1_AR3 = createStringRelationPredicate(dbMetadata, dbTypeFactory, idFactory, 1, 3, true);
    }

    @Test
    public void testSelfJoinElimination1() throws EmptyQueryException {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, B, C));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, D, B, C));

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

        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(constructionNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createFilterNode(
                                                TERM_FACTORY.getConjunction(
                                                        TERM_FACTORY.getDBIsNotNull(B),
                                                        TERM_FACTORY.getDBIsNotNull(C))),
                                        dataNode2
                                ))));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Ignore("TODO: try to support this quite complex case")
    @Test
    public void testSelfJoinElimination2() throws EmptyQueryException {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, B, C));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, D, B, C));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, E, F));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, D, G, H));

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
    public void testSelfJoinElimination3() throws EmptyQueryException {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, B, C));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, D, B, C));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, E, B, C));

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

        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createFilterNode(
                                                TERM_FACTORY.getConjunction(
                                                        TERM_FACTORY.getDBIsNotNull(B),
                                                        TERM_FACTORY.getDBIsNotNull(C))),
                                        dataNode3
                                ))));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testSelfJoinElimination4() throws EmptyQueryException {

        DBConstant constant = TERM_FACTORY.getDBStringConstant("plop");

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, constant, C));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, D, constant, C));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, E, constant, C));

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

        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(C)),
                                        dataNode3
                                ))));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testSelfJoinElimination5() throws EmptyQueryException {

        DBConstant constant = TERM_FACTORY.getDBStringConstant("plop");

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, constant, C));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, D, constant, C));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, E, B, C));

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

        IQ expectedQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(
                                constructionNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(C)),
                                        dataNode2
                                ))));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testSelfJoinElimination6() throws EmptyQueryException {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, B, C));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, D, B, C));

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
    public void testSelfJoinElimination7() throws EmptyQueryException {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, B, C));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, D, B, I));

        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, E, F));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, D, G, H));

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
    public void testSelfJoinNonElimination1() throws EmptyQueryException {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, B, C));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, D, B, C));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode2));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, B, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnaryIQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, joinTree);

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                constructionTree);

        optimizeAndCompare(initialQuery, initialQuery);
    }

    @Test
    public void testSelfJoinNonElimination2() throws EmptyQueryException {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, B, D));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, E, F, C));

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
    public void testSelfJoinNonElimination3() throws EmptyQueryException {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, B, D));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, A, F, C));

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
    public void testSelfJoinNonElimination4() throws EmptyQueryException {

        DBConstant constant1 = TERM_FACTORY.getDBStringConstant("cst1");
        DBConstant constant2 = TERM_FACTORY.getDBStringConstant("cst2");

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, constant1, B, C));

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(
                ATOM_FACTORY.getDataAtom(T1_AR3, constant2, B, C));

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

    private void optimizeAndCompare(IQ initialQuery, IQ expectedQuery) throws EmptyQueryException {
        IQ optimizedQuery = IQ_CONVERTER.convert(
                JOIN_LIKE_OPTIMIZER.optimize(
                        IQ_CONVERTER.convert(initialQuery, EXECUTOR_REGISTRY)));

        assertEquals(expectedQuery, optimizedQuery);
    }


}
