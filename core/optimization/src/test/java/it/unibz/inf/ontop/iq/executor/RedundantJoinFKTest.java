package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

/**
 * Optimizations for inner joins based on foreign keys
 */
public class RedundantJoinFKTest {

    private final static NamedRelationDefinition TABLE1;
    private final static NamedRelationDefinition TABLE2;
    private final static NamedRelationDefinition TABLE3;
    private final static NamedRelationDefinition TABLE4;

    private final static Variable A = TERM_FACTORY.getVariable("A");
    private final static Variable B = TERM_FACTORY.getVariable("B");
    private final static Variable C = TERM_FACTORY.getVariable("C");
    private final static Variable D = TERM_FACTORY.getVariable("D");
    private final static Variable E = TERM_FACTORY.getVariable("E");
    private final static Variable F = TERM_FACTORY.getVariable("F");

    private static final Constant ONE = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

    private final static ImmutableExpression EXPRESSION = TERM_FACTORY.getStrictEquality(B, ONE);

    static {

        /*
         * build the FKs
         */
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType =  builder.getDBTypeFactory().getDBLargeIntegerType();

        TABLE1 = builder.createDatabaseRelation("TABLE1",
            "col1", integerDBType, false,
            "col2", integerDBType, false);

        TABLE2 = builder.createDatabaseRelation("TABLE2",
            "col1", integerDBType, false,
            "col2", integerDBType, false);
        ForeignKeyConstraint.of("fk2-1", TABLE2.getAttribute(2), TABLE1.getAttribute(1));

        TABLE3 = builder.createDatabaseRelation("TABLE3",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);

        TABLE4 = builder.createDatabaseRelation("TABLE4",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        ForeignKeyConstraint.builder("fk2-1", TABLE4, TABLE3)
                .add(2, 1)
                .add(3, 2)
                .build();
    }


    @Test
    public void testForeignKeyOptimization() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE2, ImmutableList.of(D, A));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(dataNode1, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(1, A));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newDataNode);

        optimize(initialIQ, expectedIQ);
    }


    @Test
    public void testForeignKeyNonOptimization()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(2), A,B);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE2, ImmutableList.of(D, A));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(dataNode1, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(1, A));
        IQTree newTree = IQ_FACTORY.createNaryIQTree(
                joinNode,
                ImmutableList.of(dataNode1, newDataNode2));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimize(initialIQ, expectedIQ);
    }


    @Test
    public void testForeignKeyNonOptimization1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(A, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE2, ImmutableList.of(B, A));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(dataNode1, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(1, A));
        IQTree newTree = IQ_FACTORY.createNaryIQTree(
                joinNode,
                ImmutableList.of(dataNode1, newDataNode2));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimize(initialIQ, expectedIQ);
    }

    @Test
    public void testForeignKeyNonOptimization2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(2), A, D);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(D, A));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(filterNode,
                        IQ_FACTORY.createNaryIQTree(
                                joinNode,
                                ImmutableList.of(dataNode1, dataNode2))));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newDataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, ONE));
        IQTree newTree = IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(newDataNode1, dataNode2));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimize(initialIQ, expectedIQ);
    }

    @Test
    public void testForeignKeyNonOptimization3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(B, A));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(dataNode1, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(1, A));

        IQTree newTree = IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(dataNode1, newDataNode2));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimize(initialIQ, expectedIQ);
    }

    @Test
    public void testForeignKeyOptimization1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1_1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, A));
        ExtensionalDataNode dataNode1_2 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode1_3 = createExtensionalDataNode(TABLE1, ImmutableList.of(C, E));
        ExtensionalDataNode dataNode1_4 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, F));
        ExtensionalDataNode dataNode2_1 = createExtensionalDataNode(TABLE2, ImmutableList.of(D, A));
        ExtensionalDataNode dataNode2_2 = createExtensionalDataNode(TABLE2, ImmutableList.of(B, A));
        ExtensionalDataNode dataNode2_3 = createExtensionalDataNode(TABLE2, ImmutableList.of(D, A));
        ExtensionalDataNode dataNode2_4 = createExtensionalDataNode(TABLE2, ImmutableList.of(D, C));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(dataNode1_1, dataNode1_2, dataNode1_3, dataNode1_4,
                                dataNode2_1, dataNode2_2, dataNode2_3, dataNode2_4)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newDataNode2_4 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, D));

        IQTree newTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(dataNode1_1, dataNode1_2, dataNode2_1, dataNode2_2, dataNode2_3, newDataNode2_4)));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimize(initialIQ, expectedIQ);
    }

    @Test
    public void testForeignKeyOptimization2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(A, B, C));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4, ImmutableList.of(D, A, B));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(dataNode1, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(1, A));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newDataNode);

        optimize(initialIQ, expectedIQ);
    }

    @Test
    public void testForeignKeyNonOptimization4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(A, B, C));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4, ImmutableList.of(D, B, A));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(dataNode1, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(1, B, 2, A));

        IQTree newTree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(newDataNode1, newDataNode2)));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimize(initialIQ, expectedIQ);
    }

    @Test
    public void testForeignKeyNonOptimization5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1), A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE3, ImmutableList.of(A, A, C));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE4, ImmutableList.of(A, A, B));

        IQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(dataNode1, dataNode2)));
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, tree);

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, A, 1, A));
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 1, A));

        IQTree newTree = IQ_FACTORY.createNaryIQTree(
                        joinNode,
                        ImmutableList.of(newDataNode1, newDataNode2));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimize(initialIQ, expectedIQ);
    }

    private void optimize(IQ initialQuery, IQ expectedQuery) {
        System.out.println("\nBefore optimization: \n" +  initialQuery);

        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialQuery);
        System.out.println("\n After optimization: \n" +  optimizedIQ);

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertEquals(expectedQuery, optimizedIQ);
    }
}
