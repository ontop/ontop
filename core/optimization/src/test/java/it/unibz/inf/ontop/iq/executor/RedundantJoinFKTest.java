package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.proposal.impl.InnerJoinOptimizationProposalImpl;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;

/**
 * Optimizations for inner joins based on foreign keys
 */
public class RedundantJoinFKTest {

    private final static DatabaseRelationDefinition TABLE1;
    private final static DatabaseRelationDefinition TABLE2;
    private final static DatabaseRelationDefinition TABLE3;
    private final static DatabaseRelationDefinition TABLE4;
    private final static AtomPredicate ANS1_PREDICATE_1 = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final static AtomPredicate ANS1_PREDICATE_2 = ATOM_FACTORY.getRDFAnswerPredicate(2);
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable A = TERM_FACTORY.getVariable("A");
    private final static Variable B = TERM_FACTORY.getVariable("B");
    private final static Variable C = TERM_FACTORY.getVariable("C");
    private final static Variable D = TERM_FACTORY.getVariable("D");
    private final static Variable E = TERM_FACTORY.getVariable("E");
    private final static Variable F = TERM_FACTORY.getVariable("F");

    private static Constant ONE = TERM_FACTORY.getDBConstant("1",
            TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

    private final static ImmutableExpression EXPRESSION = TERM_FACTORY.getStrictEquality(B, ONE);

    static {

        /**
         * build the FKs
         */
        OfflineMetadataProviderBuilder builder = createMetadataBuilder();
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
    public void testForeignKeyOptimization() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE2, ImmutableList.of(D, A));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, constructionNode);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A));
        expectedQueryBuilder.addChild(constructionNode, filterNode);
        expectedQueryBuilder.addChild(filterNode, dataNode2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }


    @Test
    public void testForeignKeyNonOptimization() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, A,B);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE2, ImmutableList.of(D, A));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }


    @Test
    public void testForeignKeyNonOptimization1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(A, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE2, ImmutableList.of(B, A));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testForeignKeyNonOptimization2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_2, A, D);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(EXPRESSION);
        queryBuilder.addChild(constructionNode, filterNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(filterNode, joinNode);

        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(D, A));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testForeignKeyNonOptimization3() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2, ImmutableList.of(B, A));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testForeignKeyOptimization1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1_1 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, A));
        ExtensionalDataNode dataNode1_2 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode1_3 = createExtensionalDataNode(TABLE1, ImmutableList.of(C, E));
        ExtensionalDataNode dataNode1_4 = createExtensionalDataNode(TABLE1, ImmutableList.of(A, F));
        ExtensionalDataNode dataNode2_1 = createExtensionalDataNode(TABLE2, ImmutableList.of(D, A));
        ExtensionalDataNode dataNode2_2 = createExtensionalDataNode(TABLE2, ImmutableList.of(B, A));
        ExtensionalDataNode dataNode2_3 = createExtensionalDataNode(TABLE2, ImmutableList.of(D, A));
        ExtensionalDataNode dataNode2_4 = createExtensionalDataNode(TABLE2, ImmutableList.of(D, C));

        queryBuilder.addChild(joinNode, dataNode1_1);
        queryBuilder.addChild(joinNode, dataNode1_2);
        queryBuilder.addChild(joinNode, dataNode1_3);
        queryBuilder.addChild(joinNode, dataNode1_4);
        queryBuilder.addChild(joinNode, dataNode2_1);
        queryBuilder.addChild(joinNode, dataNode2_2);
        queryBuilder.addChild(joinNode, dataNode2_3);
        queryBuilder.addChild(joinNode, dataNode2_4);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        expectedQueryBuilder.init(projectionAtom1, constructionNode);
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getConjunction(
                TERM_FACTORY.getDBIsNotNull(A),
                TERM_FACTORY.getDBIsNotNull(C)));
        expectedQueryBuilder.addChild(constructionNode, joinNode1);
        expectedQueryBuilder.addChild(joinNode1, dataNode1_1);
        expectedQueryBuilder.addChild(joinNode1, dataNode1_2);
        expectedQueryBuilder.addChild(joinNode1, dataNode2_1);
        expectedQueryBuilder.addChild(joinNode1, dataNode2_2);
        expectedQueryBuilder.addChild(joinNode1, dataNode2_3);
        expectedQueryBuilder.addChild(joinNode1, dataNode2_4);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testForeignKeyOptimization2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE3, ImmutableList.of(A, B, C));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE4, ImmutableList.of(D, A, B));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, constructionNode);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(
                TERM_FACTORY.getDBIsNotNull(A),
                TERM_FACTORY.getDBIsNotNull(B)));
        expectedQueryBuilder.addChild(constructionNode, filterNode);
        expectedQueryBuilder.addChild(filterNode, dataNode2);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testForeignKeyNonOptimization4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE3, ImmutableList.of(A, B, C));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE4, ImmutableList.of(D, B, A));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void testForeignKeyNonOptimization5() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE3, ImmutableList.of(A, A, C));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE4, ImmutableList.of(A, A, B));

        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        query.applyProposal(new InnerJoinOptimizationProposalImpl(joinNode));

        System.out.println("\n After optimization: \n" +  query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }
}
