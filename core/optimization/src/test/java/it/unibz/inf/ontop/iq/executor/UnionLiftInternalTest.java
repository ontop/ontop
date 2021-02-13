package it.unibz.inf.ontop.iq.executor;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.impl.UnionLiftProposalImpl;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;

import static org.junit.Assert.assertTrue;

public class UnionLiftInternalTest {

    private static ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/ds1/", 0);
    private static AtomPredicate P1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 1);
    private static AtomPredicate P2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 2);
    private static AtomPredicate P3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 3);

    private static Variable X = TERM_FACTORY.getVariable("x");
    private static Variable Y = TERM_FACTORY.getVariable("y");
    private static Variable Z = TERM_FACTORY.getVariable("z");
    private static Variable T = TERM_FACTORY.getVariable("t");
    private static Variable A = TERM_FACTORY.getVariable("a");
    private static Variable B = TERM_FACTORY.getVariable("b");
    private static Variable C = TERM_FACTORY.getVariable("c");
    private static Variable D = TERM_FACTORY.getVariable("d");
    private static Variable E = TERM_FACTORY.getVariable("e");
    private static Variable F = TERM_FACTORY.getVariable("f");

    private static DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
            ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            P3_PREDICATE, ImmutableList.of(X, Y, Z));

    private static DistinctVariableOnlyDataAtom TABLE1_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(X));
    private static DistinctVariableOnlyDataAtom TABLE2_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(X));
    private static DistinctVariableOnlyDataAtom TABLE3_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            P2_PREDICATE, ImmutableList.of(X, Y));
    private static DistinctVariableOnlyDataAtom TABLE4_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            P2_PREDICATE, ImmutableList.of(Y, Z));


    @Test
    public void unionLiftInternalTest1 () throws EmptyQueryException {

        /**
         * Original Query
         */
        IntermediateQueryBuilder originalBuilder = createQueryBuilder();

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        ConstructionNode table4Construction = IQ_FACTORY.createConstructionNode(TABLE4_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(E), Z, generateURI1(F)));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(E, F));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));

        ConstructionNode table1Construction = IQ_FACTORY.createConstructionNode(TABLE1_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));

        ConstructionNode table2Construction = IQ_FACTORY.createConstructionNode(TABLE2_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(B)));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));

        ConstructionNode table3Construction = IQ_FACTORY.createConstructionNode(TABLE3_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C), Y, generateURI1(D)));
        ExtensionalDataNode table3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, joinNode);
        originalBuilder.addChild(joinNode, leftJoinNode);
        originalBuilder.addChild(joinNode, table4Construction);
        originalBuilder.addChild(table4Construction, table4DataNode);

        originalBuilder.addChild(leftJoinNode, unionNode, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        originalBuilder.addChild(leftJoinNode, table3Construction, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);
        originalBuilder.addChild(unionNode, table1Construction);
        originalBuilder.addChild(unionNode, table2Construction);

        originalBuilder.addChild(table3Construction, table3DataNode);
        originalBuilder.addChild(table2Construction, table2DataNode);
        originalBuilder.addChild(table1Construction, table1DataNode);

        IntermediateQuery query = originalBuilder.build();

        System.out.println("\n Original query: \n" +  query);

        query.applyProposal(new UnionLiftProposalImpl(unionNode, leftJoinNode));

        /**
         * Expected Query
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder();

        InnerJoinNode joinNodeExpected = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNodeExpected = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode();

        ConstructionNode table3ConstructionExpected = IQ_FACTORY.createConstructionNode(TABLE3_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C), Y, generateURI1(D)));
        ExtensionalDataNode table3DataNodeExpected = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));

        expectedBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        expectedBuilder.addChild(rootConstructionNode, joinNodeExpected);
        expectedBuilder.addChild(joinNodeExpected, unionNodeExpected);
        expectedBuilder.addChild(joinNodeExpected, table4Construction);
        expectedBuilder.addChild(unionNodeExpected, leftJoinNode1);
        expectedBuilder.addChild(unionNodeExpected, leftJoinNode2);
        expectedBuilder.addChild(leftJoinNode1, table1Construction, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        expectedBuilder.addChild(leftJoinNode1, table3Construction, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);
        expectedBuilder.addChild(leftJoinNode2, table2Construction, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        expectedBuilder.addChild(leftJoinNode2, table3ConstructionExpected, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);
        expectedBuilder.addChild(table1Construction, table1DataNode);
        expectedBuilder.addChild(table2Construction, table2DataNode);
        expectedBuilder.addChild(table3Construction, table3DataNode);
        expectedBuilder.addChild(table4Construction, table4DataNode);
        expectedBuilder.addChild(table3ConstructionExpected, table3DataNodeExpected);

        IntermediateQuery expectedQuery = expectedBuilder.build();

        System.out.println("\n Optimized query: \n" +  query);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));

    }

    @Test
    public void unionLiftInternalTest2 () throws EmptyQueryException {

        /**
         * Original Query
         */

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder();

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode table5DataNode = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(A, B, C));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, unionNode1);
        originalBuilder.addChild(unionNode1, joinNode);
        originalBuilder.addChild(unionNode1, table5DataNode);
        originalBuilder.addChild(joinNode, unionNode2);
        originalBuilder.addChild(joinNode, table4DataNode);
        originalBuilder.addChild(unionNode2, table1DataNode);
        originalBuilder.addChild(unionNode2, table2DataNode);
        originalBuilder.addChild(unionNode2, table3DataNode);


        IntermediateQuery query = originalBuilder.build();

        System.out.println("\n Original query: \n" +  query);

        query.applyProposal(new UnionLiftProposalImpl(unionNode2, joinNode));

        System.out.println("\n Optimized query: \n" +  query);

        /**
         * Expected Query
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder();

        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        UnionNode unionNode4 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode4 = IQ_FACTORY.createInnerJoinNode();

        expectedBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        expectedBuilder.addChild(rootConstructionNode, unionNode3);
        expectedBuilder.addChild(unionNode3, unionNode4);
        expectedBuilder.addChild(unionNode3, table5DataNode);
        expectedBuilder.addChild(unionNode4, joinNode2);
        expectedBuilder.addChild(unionNode4, joinNode3);
        expectedBuilder.addChild(unionNode4, joinNode4);
        expectedBuilder.addChild(joinNode2, table1DataNode);
        expectedBuilder.addChild(joinNode2, table4DataNode);
        expectedBuilder.addChild(joinNode3, table2DataNode);
        expectedBuilder.addChild(joinNode3, table4DataNode.clone());
        expectedBuilder.addChild(joinNode4, table3DataNode);
        expectedBuilder.addChild(joinNode4, table4DataNode.clone());

        IntermediateQuery expectedQuery = expectedBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void unionLiftFirstUnion () throws EmptyQueryException {

        /**
         * Original Query
         */

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder();

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode21 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        UnionNode unionNode22  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, C));

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode table5DataNode = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(A, B, C));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, unionNode1);
        originalBuilder.addChild(unionNode1, joinNode);
        originalBuilder.addChild(unionNode1, table5DataNode);
        originalBuilder.addChild(joinNode, table4DataNode);
        originalBuilder.addChild(joinNode, unionNode21);
        originalBuilder.addChild(joinNode, unionNode22);
        originalBuilder.addChild(unionNode21, table1DataNode);
        originalBuilder.addChild(unionNode21, table2DataNode);
        originalBuilder.addChild(unionNode21, table3DataNode);
        originalBuilder.addChild(unionNode22, table4DataNode.clone());
        originalBuilder.addChild(unionNode22, table5DataNode.clone());


        IntermediateQuery query = originalBuilder.build();

        System.out.println("\n Original query: \n" +  query);

        NodeCentricOptimizationResults<UnionNode> unionNodeNodeCentricOptimizationResults = query.applyProposal(new UnionLiftProposalImpl(unionNode21, joinNode));

        System.out.println("\n Optimized query: \n" +  query);

        /**
         * Expected Query
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder();

        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        UnionNode unionNode4 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode4 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode5 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, C));
        UnionNode unionNode6 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, C));
        UnionNode unionNode7 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, C));

        expectedBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        expectedBuilder.addChild(rootConstructionNode, unionNode3);
        expectedBuilder.addChild(unionNode3, unionNode4);
        expectedBuilder.addChild(unionNode3, table5DataNode);
        expectedBuilder.addChild(unionNode4, joinNode2);
        expectedBuilder.addChild(unionNode4, joinNode3);
        expectedBuilder.addChild(unionNode4, joinNode4);
        expectedBuilder.addChild(joinNode2, table4DataNode);
        expectedBuilder.addChild(joinNode2, table1DataNode);
        expectedBuilder.addChild(joinNode2, unionNode5);
        expectedBuilder.addChild(unionNode5, table4DataNode.clone());
        expectedBuilder.addChild(unionNode5, table5DataNode.clone());
        expectedBuilder.addChild(joinNode3, table4DataNode.clone());
        expectedBuilder.addChild(joinNode3, table2DataNode);
        expectedBuilder.addChild(joinNode3, unionNode6);
        expectedBuilder.addChild(unionNode6, table4DataNode.clone());
        expectedBuilder.addChild(unionNode6, table5DataNode.clone());
        expectedBuilder.addChild(joinNode4, table4DataNode.clone());
        expectedBuilder.addChild(joinNode4, table3DataNode);
        expectedBuilder.addChild(joinNode4, unionNode7);
        expectedBuilder.addChild(unionNode7, table4DataNode.clone());
        expectedBuilder.addChild(unionNode7, table5DataNode.clone());

        IntermediateQuery expectedQuery = expectedBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void unionLiftSecondUnion () throws EmptyQueryException {

        /*
         * Original Query
         */

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder();

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode21 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        UnionNode unionNode22  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, C));

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode table5DataNode = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(A, B, C));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, unionNode1);
        originalBuilder.addChild(unionNode1, joinNode);
        originalBuilder.addChild(unionNode1, table5DataNode);
        originalBuilder.addChild(joinNode, table4DataNode);
        originalBuilder.addChild(joinNode, unionNode21);
        originalBuilder.addChild(joinNode, unionNode22);
        originalBuilder.addChild(unionNode21, table1DataNode);
        originalBuilder.addChild(unionNode21, table2DataNode);
        originalBuilder.addChild(unionNode21, table3DataNode);
        originalBuilder.addChild(unionNode22, table4DataNode.clone());
        originalBuilder.addChild(unionNode22, table5DataNode.clone());


        IntermediateQuery query = originalBuilder.build();

        System.out.println("\n Original query: \n" +  query);

        NodeCentricOptimizationResults<UnionNode> unionNodeNodeCentricOptimizationResults = query.applyProposal(new UnionLiftProposalImpl(unionNode22, joinNode));

        System.out.println("\n Optimized query: \n" +  query);

        /*
         * Expected Query
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder();

        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        UnionNode unionNode4 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode5 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        UnionNode unionNode6 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));


        expectedBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        expectedBuilder.addChild(rootConstructionNode, unionNode3);
        expectedBuilder.addChild(unionNode3, unionNode4);
        expectedBuilder.addChild(unionNode3, table5DataNode);
        expectedBuilder.addChild(unionNode4, joinNode2);
        expectedBuilder.addChild(unionNode4, joinNode3);
        expectedBuilder.addChild(joinNode2, table4DataNode);
        expectedBuilder.addChild(joinNode2, unionNode5);
        expectedBuilder.addChild(joinNode2, table4DataNode.clone());
        expectedBuilder.addChild(unionNode5, table1DataNode.clone());
        expectedBuilder.addChild(unionNode5, table2DataNode.clone());
        expectedBuilder.addChild(unionNode5, table3DataNode.clone());
        expectedBuilder.addChild(joinNode3, table4DataNode.clone());
        expectedBuilder.addChild(joinNode3, unionNode6);
        expectedBuilder.addChild(joinNode3, table5DataNode.clone());
        expectedBuilder.addChild(unionNode6, table1DataNode.clone());
        expectedBuilder.addChild(unionNode6, table2DataNode.clone());
        expectedBuilder.addChild(unionNode6, table3DataNode.clone());

        IntermediateQuery expectedQuery = expectedBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));
    }

    @Test
    public void unionLiftDoubleLift () throws EmptyQueryException {

        /*
         * Original Query
         */

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder();

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode21 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        UnionNode unionNode22  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, C));
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode table5DataNode = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(A, B, C));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, unionNode1);
        originalBuilder.addChild(unionNode1, joinNode);
        originalBuilder.addChild(unionNode1, table5DataNode);

        originalBuilder.addChild(joinNode, unionNode21);
        originalBuilder.addChild(joinNode, unionNode22);
        originalBuilder.addChild(joinNode, table4DataNode);
        originalBuilder.addChild(unionNode21, table1DataNode);
        originalBuilder.addChild(unionNode21, table2DataNode);
        originalBuilder.addChild(unionNode21, table3DataNode);
        originalBuilder.addChild(unionNode22, table4DataNode.clone());
        originalBuilder.addChild(unionNode22, table5DataNode.clone());
        originalBuilder.addChild(unionNode22, joinNode1);
        originalBuilder.addChild(joinNode1, table4DataNode.clone());
        originalBuilder.addChild(joinNode1, table4DataNode.clone());



        IntermediateQuery query = originalBuilder.build();

        System.out.println("\n Original query: \n" +  query);

        NodeCentricOptimizationResults<UnionNode> unionNodeNodeCentricOptimizationResults = query.applyProposal(
                new UnionLiftProposalImpl(unionNode21, joinNode));

        System.out.println("\n Optimized query: \n" +  query);

        /*
         * Expected Query
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder();

        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        UnionNode unionNode4 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        UnionNode unionNode5 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A,C));
        UnionNode unionNode6 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A,C));
        UnionNode unionNode7 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A,C));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode4 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode5 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode6 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode7 = IQ_FACTORY.createInnerJoinNode();


        expectedBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        expectedBuilder.addChild(rootConstructionNode, unionNode3);
        expectedBuilder.addChild(unionNode3, unionNode4);
        expectedBuilder.addChild(unionNode3, table5DataNode);
        expectedBuilder.addChild(unionNode4, joinNode2);
        expectedBuilder.addChild(unionNode4, joinNode3);
        expectedBuilder.addChild(unionNode4, joinNode4);
        expectedBuilder.addChild(joinNode2, table1DataNode);
        expectedBuilder.addChild(joinNode2, unionNode5);
        expectedBuilder.addChild(unionNode5, table4DataNode.clone());
        expectedBuilder.addChild(unionNode5, table5DataNode.clone());
        expectedBuilder.addChild(unionNode5, joinNode5);
        expectedBuilder.addChild(joinNode5, table4DataNode.clone());
        expectedBuilder.addChild(joinNode5, table4DataNode.clone());
        expectedBuilder.addChild(joinNode2, table4DataNode);
        expectedBuilder.addChild(joinNode3, table2DataNode);
        expectedBuilder.addChild(joinNode3, unionNode6);
        expectedBuilder.addChild(unionNode6, table4DataNode.clone());
        expectedBuilder.addChild(unionNode6, table5DataNode.clone());
        expectedBuilder.addChild(unionNode6, joinNode6);
        expectedBuilder.addChild(joinNode6, table4DataNode.clone());
        expectedBuilder.addChild(joinNode6, table4DataNode.clone());
        expectedBuilder.addChild(joinNode3, table4DataNode.clone());
        expectedBuilder.addChild(joinNode4, table3DataNode);
        expectedBuilder.addChild(joinNode4, unionNode7);
        expectedBuilder.addChild(unionNode7, table4DataNode.clone());
        expectedBuilder.addChild(unionNode7, table5DataNode.clone());
        expectedBuilder.addChild(unionNode7, joinNode7);
        expectedBuilder.addChild(joinNode7, table4DataNode.clone());
        expectedBuilder.addChild(joinNode7, table4DataNode.clone());
        expectedBuilder.addChild(joinNode4, table4DataNode.clone());

        IntermediateQuery expectedQuery = expectedBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));


        System.out.println("\n Continue from the expected query: \n" +  expectedQuery);

        NodeCentricOptimizationResults<UnionNode> unionNodeNodeCentricOptimizationResults2 = expectedQuery.applyProposal(new UnionLiftProposalImpl(unionNode5, joinNode2));
        IntermediateQuery query2 = expectedQuery;

        System.out.println("\n Optimized query: \n" +  query2);

        /**
         * Second Expected Query
         */
        IntermediateQueryBuilder expectedBuilder2 = createQueryBuilder();

        UnionNode unionNode8 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        UnionNode unionNode9 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        UnionNode unionNode10 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        UnionNode unionNode11 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A,C));
        UnionNode unionNode12 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A,C));
        InnerJoinNode joinNode8 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode9 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode10 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode11 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode12 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode13 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode14 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode15 = IQ_FACTORY.createInnerJoinNode();


        expectedBuilder2.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        expectedBuilder2.addChild(rootConstructionNode, unionNode8);
        expectedBuilder2.addChild(unionNode8, unionNode9);
        expectedBuilder2.addChild(unionNode8, table5DataNode);
        expectedBuilder2.addChild(unionNode9, unionNode10);
        expectedBuilder2.addChild(unionNode9, joinNode9);
        expectedBuilder2.addChild(unionNode9, joinNode10);
        expectedBuilder2.addChild(unionNode10, joinNode8);
        expectedBuilder2.addChild(joinNode8, table1DataNode.clone());
        expectedBuilder2.addChild(joinNode8, table4DataNode.clone());
        expectedBuilder2.addChild(joinNode8, table4DataNode.clone());
        expectedBuilder2.addChild(unionNode10, joinNode11);
        expectedBuilder2.addChild(joinNode11, table1DataNode.clone());
        expectedBuilder2.addChild(joinNode11, table5DataNode.clone());
        expectedBuilder2.addChild(joinNode11, table4DataNode.clone());
        expectedBuilder2.addChild(unionNode10, joinNode12);
        expectedBuilder2.addChild(joinNode12, table1DataNode.clone());
        expectedBuilder2.addChild(joinNode12, joinNode13);
        expectedBuilder2.addChild(joinNode12, table4DataNode.clone());
        expectedBuilder2.addChild(joinNode13, table4DataNode.clone());
        expectedBuilder2.addChild(joinNode13, table4DataNode.clone());
        expectedBuilder2.addChild(joinNode9, table2DataNode.clone());
        expectedBuilder2.addChild(joinNode9, unionNode11);
        expectedBuilder2.addChild(unionNode11,  table4DataNode.clone());
        expectedBuilder2.addChild(unionNode11,  table5DataNode.clone());
        expectedBuilder2.addChild(unionNode11,  joinNode14);
        expectedBuilder2.addChild(joinNode14, table4DataNode.clone());
        expectedBuilder2.addChild(joinNode14, table4DataNode.clone());
        expectedBuilder2.addChild(joinNode9, table4DataNode.clone());
        expectedBuilder2.addChild(joinNode10, table3DataNode.clone());
        expectedBuilder2.addChild(joinNode10, unionNode12);
        expectedBuilder2.addChild(unionNode12,  table4DataNode.clone());
        expectedBuilder2.addChild(unionNode12,  table5DataNode.clone());
        expectedBuilder2.addChild(unionNode12,  joinNode15);
        expectedBuilder2.addChild(joinNode15, table4DataNode.clone());
        expectedBuilder2.addChild(joinNode15, table4DataNode.clone());
        expectedBuilder2.addChild(joinNode10, table4DataNode.clone());




        IntermediateQuery expectedQuery2 = expectedBuilder2.build();

        System.out.println("\n Expected query: \n" +  expectedQuery2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query2, expectedQuery2));
    }

    @Test(expected = InvalidQueryOptimizationProposalException.class)
    public void unionLiftInternalTest3 () throws EmptyQueryException {

        /*
         * Original Query
         */

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder();

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode table5DataNode = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(A, B, C));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, unionNode1);
        originalBuilder.addChild(unionNode1, joinNode);
        originalBuilder.addChild(unionNode1, table5DataNode);
        originalBuilder.addChild(joinNode, unionNode2);
        originalBuilder.addChild(joinNode, table4DataNode);
        originalBuilder.addChild(unionNode2, table1DataNode);
        originalBuilder.addChild(unionNode2, table2DataNode);
        originalBuilder.addChild(unionNode2, table3DataNode);


        IntermediateQuery query = originalBuilder.build();

        System.out.println("\n Original query: \n" +  query);

        query.applyProposal(new UnionLiftProposalImpl(unionNode2, unionNode1))
                ;
    }

    @Test(expected = InvalidQueryOptimizationProposalException.class)
    public void unionLiftInternalTest4 () throws EmptyQueryException {

        /*
         * Original Query
         */

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder();

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, E));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode table5DataNode = createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(A, E));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, joinNode);
        originalBuilder.addChild(joinNode, leftJoinNode);
        originalBuilder.addChild(joinNode, joinNode1);
        originalBuilder.addChild(joinNode1, table3DataNode);
        originalBuilder.addChild(joinNode1, table5DataNode);
        originalBuilder.addChild(leftJoinNode, unionNode, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        originalBuilder.addChild(leftJoinNode, table4DataNode, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);
        originalBuilder.addChild(unionNode, table1DataNode);
        originalBuilder.addChild(unionNode, table2DataNode);

        IntermediateQuery query = originalBuilder.build();

        System.out.println("\n Original query: \n" +  query);

        query.applyProposal(new UnionLiftProposalImpl(unionNode, joinNode1))
                ;

        System.out.println("\n Optimized query: \n" +  query);

    }

    @Test(expected = InvalidQueryOptimizationProposalException.class)
    public void unionLiftInternalTest5 () throws EmptyQueryException {

        /*
         * Original Query
         */

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder();

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, E));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode table5DataNode = createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(A, E));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, joinNode);
        originalBuilder.addChild(joinNode, leftJoinNode);
        originalBuilder.addChild(joinNode, joinNode1);
        originalBuilder.addChild(joinNode1, table3DataNode);
        originalBuilder.addChild(joinNode1, table5DataNode);
        originalBuilder.addChild(leftJoinNode, unionNode, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);
        originalBuilder.addChild(leftJoinNode, table4DataNode, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        originalBuilder.addChild(unionNode, table1DataNode);
        originalBuilder.addChild(unionNode, table2DataNode);

        IntermediateQuery query = originalBuilder.build();

        System.out.println("\n Original query: \n" +  query);

        query.applyProposal(new UnionLiftProposalImpl(unionNode, joinNode1))
                ;

        System.out.println("\n Optimized query: \n" +  query);


    }

    @Test
    public void unionLiftInternalTest6 () throws EmptyQueryException {

        /*
         * Original Query
         */

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder();

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode table5DataNode = createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(A, E));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, joinNode);
        originalBuilder.addChild(joinNode, leftJoinNode);
        originalBuilder.addChild(joinNode, table5DataNode);
        originalBuilder.addChild(leftJoinNode, unionNode, BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        originalBuilder.addChild(leftJoinNode, table4DataNode, BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);
        originalBuilder.addChild(unionNode, table1DataNode);
        originalBuilder.addChild(unionNode, table2DataNode);

        IntermediateQuery query = originalBuilder.build();

        System.out.println("\n Original query: \n" +  query);

        query.applyProposal(new UnionLiftProposalImpl(unionNode, joinNode));

        System.out.println("\n Optimized query: \n" +  query);

        /*
         * Expected Query
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder();

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C, E));
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode();

        expectedBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        expectedBuilder.addChild(rootConstructionNode, unionNode1);
        expectedBuilder.addChild(unionNode1, joinNode1);
        expectedBuilder.addChild(unionNode1, joinNode2);
        expectedBuilder.addChild(joinNode1, leftJoinNode1);
        expectedBuilder.addChild(joinNode1, table5DataNode.clone());
        expectedBuilder.addChild(joinNode2, leftJoinNode2);
        expectedBuilder.addChild(joinNode2, table5DataNode.clone());
        expectedBuilder.addChild(leftJoinNode1, table1DataNode.clone(), BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        expectedBuilder.addChild(leftJoinNode1, table4DataNode.clone(), BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);
        expectedBuilder.addChild(leftJoinNode2, table2DataNode.clone(), BinaryOrderedOperatorNode.ArgumentPosition.LEFT);
        expectedBuilder.addChild(leftJoinNode2, table4DataNode.clone(), BinaryOrderedOperatorNode.ArgumentPosition.RIGHT);

        IntermediateQuery expectedQuery = expectedBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));

    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }
}
