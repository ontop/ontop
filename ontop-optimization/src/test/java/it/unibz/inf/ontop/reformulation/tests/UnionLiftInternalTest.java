package it.unibz.inf.ontop.reformulation.tests;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.UnionLiftProposalImpl;
import it.unibz.inf.ontop.sql.DBMetadataTestingTools;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class UnionLiftInternalTest {

    private static Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static URITemplatePredicate URI_PREDICATE =  new URITemplatePredicateImpl(2);

    private static AtomPredicate P1_PREDICATE = new AtomPredicateImpl("p1", 1);
    private static AtomPredicate P2_PREDICATE = new AtomPredicateImpl("p1", 2);
    private static AtomPredicate P3_PREDICATE = new AtomPredicateImpl("p1", 3);
    private static AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 1);
    private static AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 1);
    private static AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
    private static AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 2);

    private static Variable X = DATA_FACTORY.getVariable("x");
    private static Variable Y = DATA_FACTORY.getVariable("y");
    private static Variable Z = DATA_FACTORY.getVariable("z");
    private static Variable T = DATA_FACTORY.getVariable("t");
    private static Variable A = DATA_FACTORY.getVariable("a");
    private static Variable B = DATA_FACTORY.getVariable("b");
    private static Variable C = DATA_FACTORY.getVariable("c");
    private static Variable D = DATA_FACTORY.getVariable("d");
    private static Variable E = DATA_FACTORY.getVariable("e");
    private static Variable F = DATA_FACTORY.getVariable("f");

    private static DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
            DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P3_PREDICATE, ImmutableList.of(X, Y, Z));

    private static DistinctVariableOnlyDataAtom TABLE1_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(X));
    private static DistinctVariableOnlyDataAtom TABLE2_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(X));
    private static DistinctVariableOnlyDataAtom TABLE3_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P2_PREDICATE, ImmutableList.of(X, Y));
    private static DistinctVariableOnlyDataAtom TABLE4_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            P2_PREDICATE, ImmutableList.of(Y, Z));

    private final MetadataForQueryOptimization metadata;

    public UnionLiftInternalTest() {
        this.metadata = initMetadata();
    }

    private static MetadataForQueryOptimization initMetadata() {
        ImmutableMultimap.Builder<AtomPredicate, ImmutableList<Integer>> uniqueKeyBuilder = ImmutableMultimap.builder();
        return new MetadataForQueryOptimizationImpl(DBMetadataTestingTools.createDummyMetadata(),
                uniqueKeyBuilder.build(), new UriTemplateMatcher());
    }


    @Test
    public void unionLiftInternalTest1 () throws EmptyQueryException {

        /**
         * Original Query
         */
        IntermediateQueryBuilder originalBuilder = createQueryBuilder(metadata);

        ConstructionNode rootConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());

        ConstructionNode table4Construction = new ConstructionNodeImpl(TABLE4_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, generateURI1(E), Z, generateURI1(F))),
                Optional.empty());
        ExtensionalDataNode table4DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, E, F));

        UnionNode unionNode = new UnionNodeImpl(ImmutableSet.of(X));

        ConstructionNode table1Construction = new ConstructionNodeImpl(TABLE1_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A))), Optional.empty());
        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A));

        ConstructionNode table2Construction = new ConstructionNodeImpl(TABLE2_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(B))), Optional.empty());
        ExtensionalDataNode table2DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, B));

        ConstructionNode table3Construction = new ConstructionNodeImpl(TABLE3_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(C), Y, generateURI1(D))),
                Optional.empty());
        ExtensionalDataNode table3DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, C, D));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, joinNode);
        originalBuilder.addChild(joinNode, leftJoinNode);
        originalBuilder.addChild(joinNode, table4Construction);
        originalBuilder.addChild(table4Construction, table4DataNode);

        originalBuilder.addChild(leftJoinNode, unionNode, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        originalBuilder.addChild(leftJoinNode, table3Construction, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);
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
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(metadata);

        InnerJoinNode joinNodeExpected = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNodeExpected = new UnionNodeImpl(ImmutableSet.of(X, Y));
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.empty());
        LeftJoinNode leftJoinNode2 = new LeftJoinNodeImpl(Optional.empty());

        ConstructionNode table3ConstructionExpected = new ConstructionNodeImpl(TABLE3_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(C), Y, generateURI1(D))),
                Optional.empty());
        ExtensionalDataNode table3DataNodeExpected = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, C, D));

        expectedBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        expectedBuilder.addChild(rootConstructionNode, joinNodeExpected);
        expectedBuilder.addChild(joinNodeExpected, unionNodeExpected);
        expectedBuilder.addChild(joinNodeExpected, table4Construction);
        expectedBuilder.addChild(unionNodeExpected, leftJoinNode1);
        expectedBuilder.addChild(unionNodeExpected, leftJoinNode2);
        expectedBuilder.addChild(leftJoinNode1, table1Construction, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        expectedBuilder.addChild(leftJoinNode1, table3Construction, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);
        expectedBuilder.addChild(leftJoinNode2, table2Construction, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        expectedBuilder.addChild(leftJoinNode2, table3ConstructionExpected, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);
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

        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 2);
        AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
        AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 2);
        AtomPredicate TABLE_5 = new AtomPredicateImpl("table5", 3);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder(metadata);

        ConstructionNode rootConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        UnionNode unionNode1  = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode2 = new UnionNodeImpl(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table2DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A, B));
        ExtensionalDataNode table3DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, A, B));
        ExtensionalDataNode table4DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_5, A, B, C));

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
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(metadata);

        UnionNode unionNode3 = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        UnionNode unionNode4 = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode3 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode4 = new InnerJoinNodeImpl(Optional.empty());

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

        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 2);
        AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
        AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 2);
        AtomPredicate TABLE_5 = new AtomPredicateImpl("table5", 3);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder(metadata);

        ConstructionNode rootConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        UnionNode unionNode1  = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode21 = new UnionNodeImpl(ImmutableSet.of(A, B));
        UnionNode unionNode22  = new UnionNodeImpl(ImmutableSet.of(A, C));

        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table2DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A, B));
        ExtensionalDataNode table3DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, A, B));
        ExtensionalDataNode table4DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_5, A, B, C));

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
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(metadata);

        UnionNode unionNode3 = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        UnionNode unionNode4 = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode3 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode4 = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode5 = new UnionNodeImpl(ImmutableSet.of(A, C));
        UnionNode unionNode6 = new UnionNodeImpl(ImmutableSet.of(A, C));
        UnionNode unionNode7 = new UnionNodeImpl(ImmutableSet.of(A, C));

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

        /**
         * Original Query
         */

        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 2);
        AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
        AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 2);
        AtomPredicate TABLE_5 = new AtomPredicateImpl("table5", 3);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder(metadata);

        ConstructionNode rootConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        UnionNode unionNode1  = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode21 = new UnionNodeImpl(ImmutableSet.of(A, B));
        UnionNode unionNode22  = new UnionNodeImpl(ImmutableSet.of(A, C));

        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table2DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A, B));
        ExtensionalDataNode table3DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, A, B));
        ExtensionalDataNode table4DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_5, A, B, C));

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

        /**
         * Expected Query
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(metadata);

        UnionNode unionNode3 = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        UnionNode unionNode4 = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode3 = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode5 = new UnionNodeImpl(ImmutableSet.of(A, B));
        UnionNode unionNode6 = new UnionNodeImpl(ImmutableSet.of(A, B));


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

        /**
         * Original Query
         */

        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 2);
        AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
        AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 2);
        AtomPredicate TABLE_5 = new AtomPredicateImpl("table5", 3);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder(metadata);

        ConstructionNode rootConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        UnionNode unionNode1  = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode21 = new UnionNodeImpl(ImmutableSet.of(A, B));
        UnionNode unionNode22  = new UnionNodeImpl(ImmutableSet.of(A, C));
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.empty());

        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table2DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A, B));
        ExtensionalDataNode table3DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, A, B));
        ExtensionalDataNode table4DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_5, A, B, C));

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

        /**
         * Expected Query
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(metadata);

        UnionNode unionNode3 = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        UnionNode unionNode4 = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        UnionNode unionNode5 = new UnionNodeImpl(ImmutableSet.of(A,C));
        UnionNode unionNode6 = new UnionNodeImpl(ImmutableSet.of(A,C));
        UnionNode unionNode7 = new UnionNodeImpl(ImmutableSet.of(A,C));
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode3 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode4 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode5 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode6 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode7 = new InnerJoinNodeImpl(Optional.empty());


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
        IntermediateQueryBuilder expectedBuilder2 = createQueryBuilder(metadata);

        UnionNode unionNode8 = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        UnionNode unionNode9 = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        UnionNode unionNode10 = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        UnionNode unionNode11 = new UnionNodeImpl(ImmutableSet.of(A,C));
        UnionNode unionNode12 = new UnionNodeImpl(ImmutableSet.of(A,C));
        InnerJoinNode joinNode8 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode9 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode10 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode11 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode12 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode13 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode14 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode15 = new InnerJoinNodeImpl(Optional.empty());


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

        /**
         * Original Query
         */
        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 2);
        AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
        AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 2);
        AtomPredicate TABLE_5 = new AtomPredicateImpl("table5", 3);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder(metadata);

        ConstructionNode rootConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        UnionNode unionNode1  = new UnionNodeImpl(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        UnionNode unionNode2 = new UnionNodeImpl(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table2DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A, B));
        ExtensionalDataNode table3DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, A, B));
        ExtensionalDataNode table4DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_5, A, B, C));

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

        /**
         * Original Query
         */
        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 2);
        AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
        AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 2);
        AtomPredicate TABLE_5 = new AtomPredicateImpl("table5", 2);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder(metadata);

        ConstructionNode rootConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.empty());
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        UnionNode unionNode = new UnionNodeImpl(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table2DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A, B));
        ExtensionalDataNode table3DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, A, E));
        ExtensionalDataNode table4DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_5, A, E));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, joinNode);
        originalBuilder.addChild(joinNode, leftJoinNode);
        originalBuilder.addChild(joinNode, joinNode1);
        originalBuilder.addChild(joinNode1, table3DataNode);
        originalBuilder.addChild(joinNode1, table5DataNode);
        originalBuilder.addChild(leftJoinNode, unionNode, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        originalBuilder.addChild(leftJoinNode, table4DataNode, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);
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

        /**
         * Original Query
         */
        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 2);
        AtomPredicate TABLE_3 = new AtomPredicateImpl("table3", 2);
        AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 2);
        AtomPredicate TABLE_5 = new AtomPredicateImpl("table5", 2);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder(metadata);

        ConstructionNode rootConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.empty());
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        UnionNode unionNode = new UnionNodeImpl(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table2DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A, B));
        ExtensionalDataNode table3DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_3, A, E));
        ExtensionalDataNode table4DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_5, A, E));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, joinNode);
        originalBuilder.addChild(joinNode, leftJoinNode);
        originalBuilder.addChild(joinNode, joinNode1);
        originalBuilder.addChild(joinNode1, table3DataNode);
        originalBuilder.addChild(joinNode1, table5DataNode);
        originalBuilder.addChild(leftJoinNode, unionNode, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);
        originalBuilder.addChild(leftJoinNode, table4DataNode, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
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

        /**
         * Original Query
         */
        AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 2);
        AtomPredicate TABLE_2 = new AtomPredicateImpl("table2", 2);
        AtomPredicate TABLE_4 = new AtomPredicateImpl("table4", 2);
        AtomPredicate TABLE_5 = new AtomPredicateImpl("table5", 2);

        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                DATA_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

        IntermediateQueryBuilder originalBuilder = createQueryBuilder(metadata);

        ConstructionNode rootConstructionNode = new ConstructionNodeImpl(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of()), Optional.empty());

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        UnionNode unionNode = new UnionNodeImpl(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A, B));
        ExtensionalDataNode table2DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_2, A, B));
        ExtensionalDataNode table4DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_4, A, C));
        ExtensionalDataNode table5DataNode = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_5, A, E));

        originalBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        originalBuilder.addChild(rootConstructionNode, joinNode);
        originalBuilder.addChild(joinNode, leftJoinNode);
        originalBuilder.addChild(joinNode, table5DataNode);
        originalBuilder.addChild(leftJoinNode, unionNode, NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        originalBuilder.addChild(leftJoinNode, table4DataNode, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);
        originalBuilder.addChild(unionNode, table1DataNode);
        originalBuilder.addChild(unionNode, table2DataNode);

        IntermediateQuery query = originalBuilder.build();

        System.out.println("\n Original query: \n" +  query);

        query.applyProposal(new UnionLiftProposalImpl(unionNode, joinNode));

        System.out.println("\n Optimized query: \n" +  query);

        /**
         * Expected Query
         */
        IntermediateQueryBuilder expectedBuilder = createQueryBuilder(metadata);

        UnionNode unionNode1 = new UnionNodeImpl(ImmutableSet.of(A, B, C, E));
        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.empty());
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.empty());
        LeftJoinNode leftJoinNode2 = new LeftJoinNodeImpl(Optional.empty());

        expectedBuilder.init(ROOT_CONSTRUCTION_NODE_ATOM, rootConstructionNode);
        expectedBuilder.addChild(rootConstructionNode, unionNode1);
        expectedBuilder.addChild(unionNode1, joinNode1);
        expectedBuilder.addChild(unionNode1, joinNode2);
        expectedBuilder.addChild(joinNode1, leftJoinNode1);
        expectedBuilder.addChild(joinNode1, table5DataNode.clone());
        expectedBuilder.addChild(joinNode2, leftJoinNode2);
        expectedBuilder.addChild(joinNode2, table5DataNode.clone());
        expectedBuilder.addChild(leftJoinNode1, table1DataNode.clone(), NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        expectedBuilder.addChild(leftJoinNode1, table4DataNode.clone(), NonCommutativeOperatorNode.ArgumentPosition.RIGHT);
        expectedBuilder.addChild(leftJoinNode2, table2DataNode.clone(), NonCommutativeOperatorNode.ArgumentPosition.LEFT);
        expectedBuilder.addChild(leftJoinNode2, table4DataNode.clone(), NonCommutativeOperatorNode.ArgumentPosition.RIGHT);

        IntermediateQuery expectedQuery = expectedBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(query, expectedQuery));

    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }



}
