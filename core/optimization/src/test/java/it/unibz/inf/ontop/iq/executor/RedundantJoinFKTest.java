package it.unibz.inf.ontop.iq.executor;

import it.unibz.inf.ontop.dbschema.*;
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
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;

/**
 * Optimizations for inner joins based on foreign keys
 */
public class RedundantJoinFKTest {

    private final static RelationPredicate TABLE1_PREDICATE;
    private final static RelationPredicate TABLE2_PREDICATE;
    private final static RelationPredicate TABLE3_PREDICATE;
    private final static RelationPredicate TABLE4_PREDICATE;
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
        DummyDBMetadataBuilder dbMetadata = DEFAULT_DUMMY_DB_METADATA;
        DBTermType integerDBType =  dbMetadata.getDBTypeFactory().getDBLargeIntegerType();

        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation("TABLE1",
            "col1", integerDBType, false,
            "col2", integerDBType, false);
        TABLE1_PREDICATE = table1Def.getAtomPredicate();

        DatabaseRelationDefinition table2Def = dbMetadata.createDatabaseRelation("TABLE2",
            "col1", integerDBType, false,
            "col2", integerDBType, false);
        ForeignKeyConstraint.of("fk2-1", table2Def.getAttribute(2), table1Def.getAttribute(1));
        TABLE2_PREDICATE = table2Def.getAtomPredicate();

        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation("TABLE3",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        TABLE3_PREDICATE = table3Def.getAtomPredicate();

        DatabaseRelationDefinition table4Def = dbMetadata.createDatabaseRelation("TABLE4",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        ForeignKeyConstraint.builder("fk2-1", table4Def, table3Def)
                .add(table4Def.getAttribute(2), table3Def.getAttribute(1))
                .add(table4Def.getAttribute(3), table3Def.getAttribute(2))
                .build();
        TABLE4_PREDICATE = table4Def.getAtomPredicate();
    }


    @Test
    public void testForeignKeyOptimization() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE_1, A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(constructionNode, joinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, D, A));

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
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, D, A));

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
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, ONE));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, B, A));

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

        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, D, A));

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
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, A));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, B, A));

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
        ExtensionalDataNode dataNode1_1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, A));
        ExtensionalDataNode dataNode1_2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
        ExtensionalDataNode dataNode1_3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, C, E));
        ExtensionalDataNode dataNode1_4 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, A, F));
        ExtensionalDataNode dataNode2_1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, D, A));
        ExtensionalDataNode dataNode2_2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, B, A));
        ExtensionalDataNode dataNode2_3 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, D, A));
        ExtensionalDataNode dataNode2_4 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, D, C));

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
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, B, C));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, D, A, B));

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
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, B, C));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, D, B, A));

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
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE3_PREDICATE, A, A, C));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_PREDICATE, A, A, B));

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
