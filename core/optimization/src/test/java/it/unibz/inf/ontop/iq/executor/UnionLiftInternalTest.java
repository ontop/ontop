package it.unibz.inf.ontop.iq.executor;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;

import static org.junit.Assert.assertEquals;

@Ignore("TODO: see if something interesting could be ported to immutable IQs")
public class UnionLiftInternalTest {

    private static final ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/ds1/", 0);
    private static final AtomPredicate P1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 1);
    private static final AtomPredicate P2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 2);
    private static final AtomPredicate P3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 3);

    private static final Variable X = TERM_FACTORY.getVariable("x");
    private static final Variable Y = TERM_FACTORY.getVariable("y");
    private static final Variable Z = TERM_FACTORY.getVariable("z");
    private static final Variable T = TERM_FACTORY.getVariable("t");
    private static final Variable A = TERM_FACTORY.getVariable("a");
    private static final Variable B = TERM_FACTORY.getVariable("b");
    private static final Variable C = TERM_FACTORY.getVariable("c");
    private static final Variable D = TERM_FACTORY.getVariable("d");
    private static final Variable E = TERM_FACTORY.getVariable("e");
    private static final Variable F = TERM_FACTORY.getVariable("f");

    private static final DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
            ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            P3_PREDICATE, ImmutableList.of(X, Y, Z));

    private static final DistinctVariableOnlyDataAtom TABLE1_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(X));
    private static final DistinctVariableOnlyDataAtom TABLE2_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            P1_PREDICATE, ImmutableList.of(X));
    private static final DistinctVariableOnlyDataAtom TABLE3_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            P2_PREDICATE, ImmutableList.of(X, Y));
    private static final DistinctVariableOnlyDataAtom TABLE4_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            P2_PREDICATE, ImmutableList.of(Y, Z));


    @Test
    public void unionLiftInternalTest1() {
        // Original Query
        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));

        ConstructionNode table4Construction = IQ_FACTORY.createConstructionNode(TABLE4_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(E), Z, generateURI1(F)));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(E, F));
        ConstructionNode table1Construction = IQ_FACTORY.createConstructionNode(TABLE1_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A)));
        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ConstructionNode table2Construction = IQ_FACTORY.createConstructionNode(TABLE2_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(B)));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));
        ConstructionNode table3Construction = IQ_FACTORY.createConstructionNode(TABLE3_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C), Y, generateURI1(D)));
        ExtensionalDataNode table3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));

        IQ query = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                                IQ_FACTORY.createUnaryIQTree(table1Construction, table1DataNode),
                                                IQ_FACTORY.createUnaryIQTree(table2Construction, table2DataNode))),
                                        IQ_FACTORY.createUnaryIQTree(table3Construction, table3DataNode)),
                                IQ_FACTORY.createUnaryIQTree(table4Construction, table4DataNode)))));
        System.out.println("\n Original query: \n" +  query);

        query = transform(query, unionNode, leftJoinNode);


        // Expected Query
        InnerJoinNode joinNodeExpected = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNodeExpected = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode();

        ConstructionNode table3ConstructionExpected = IQ_FACTORY.createConstructionNode(TABLE3_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C), Y, generateURI1(D)));
        ExtensionalDataNode table3DataNodeExpected = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));

        IQ expectedQuery = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNodeExpected, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(unionNodeExpected, ImmutableList.of(
                                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode1,
                                                IQ_FACTORY.createUnaryIQTree(table1Construction, table1DataNode),
                                                IQ_FACTORY.createUnaryIQTree(table3Construction, table3DataNode)),
                                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode2,
                                                IQ_FACTORY.createUnaryIQTree(table2Construction, table2DataNode),
                                                IQ_FACTORY.createUnaryIQTree(table3ConstructionExpected, table3DataNodeExpected)))),
                                IQ_FACTORY.createUnaryIQTree(table4Construction, table4DataNode)))));

        System.out.println("\n Optimized query: \n" +  query);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertEquals(expectedQuery, query);
    }

    private IQ transform(IQ query, UnionNode unionNode, QueryNode targetNode) {
        throw new RuntimeException("TODO: see how to do something similar with immutable IQs");
    }

    @Test
    public void unionLiftInternalTest2 ()  {
        // Original Query
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                        P3_PREDICATE, ImmutableList.of(A, B, C));

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

        IQ query = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(table1DataNode, table2DataNode, table3DataNode)),
                                        table4DataNode)),
                                table5DataNode))));
        System.out.println("\n Original query: \n" +  query);

        query = transform(query, unionNode2, joinNode);
        System.out.println("\n Optimized query: \n" +  query);


        // Expected Query
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        UnionNode unionNode4 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode4 = IQ_FACTORY.createInnerJoinNode();

        IQ expectedQuery = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode3, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(unionNode4, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(table1DataNode, table4DataNode)),
                                        IQ_FACTORY.createNaryIQTree(joinNode3, ImmutableList.of(table2DataNode, table4DataNode)),
                                        IQ_FACTORY.createNaryIQTree(joinNode4, ImmutableList.of(table3DataNode, table4DataNode)))),
                                table5DataNode))));

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertEquals(expectedQuery, query);
    }

    @Test
    public void unionLiftFirstUnion () {
        // Original Query
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(P3_PREDICATE, ImmutableList.of(A, B, C));

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

        IQ query = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        table4DataNode,
                                        IQ_FACTORY.createNaryIQTree(unionNode21, ImmutableList.of(table1DataNode, table2DataNode, table3DataNode)),
                                        IQ_FACTORY.createNaryIQTree(unionNode22, ImmutableList.of(table4DataNode, table5DataNode)))),
                                table5DataNode))));

        System.out.println("\n Original query: \n" +  query);

        query = transform(query, unionNode21, joinNode);
        System.out.println("\n Optimized query: \n" +  query);

        // Expected Query
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        UnionNode unionNode4 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode4 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode5 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, C));
        UnionNode unionNode6 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, C));
        UnionNode unionNode7 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, C));

        IQ expectedQuery = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode3, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(unionNode4, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(
                                                table4DataNode,
                                                table1DataNode,
                                                IQ_FACTORY.createNaryIQTree(unionNode5, ImmutableList.of(table4DataNode, table5DataNode)))),
                                        IQ_FACTORY.createNaryIQTree(joinNode3, ImmutableList.of(
                                                table4DataNode,
                                                table2DataNode,
                                                IQ_FACTORY.createNaryIQTree(unionNode6, ImmutableList.of(table4DataNode, table5DataNode)))),
                                        IQ_FACTORY.createNaryIQTree(joinNode4, ImmutableList.of(
                                                table4DataNode,
                                                table3DataNode,
                                                IQ_FACTORY.createNaryIQTree(unionNode7, ImmutableList.of(table4DataNode, table5DataNode)))))),
                                table5DataNode))));

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertEquals(expectedQuery, query);
    }

    @Test
    public void unionLiftSecondUnion () {
        // Original Query
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(P3_PREDICATE, ImmutableList.of(A, B, C));

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

        IQ query = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        table4DataNode,
                                        IQ_FACTORY.createNaryIQTree(unionNode21, ImmutableList.of(table1DataNode, table2DataNode, table3DataNode)),
                                        IQ_FACTORY.createNaryIQTree(unionNode22, ImmutableList.of(table4DataNode, table5DataNode)))),
                                table5DataNode))));
        System.out.println("\n Original query: \n" +  query);

        query = transform(query, unionNode22, joinNode);
        System.out.println("\n Optimized query: \n" +  query);

        /*
         * Expected Query
         */
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        UnionNode unionNode4 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode5 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));
        UnionNode unionNode6 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));

        IQ expectedQuery = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode3, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(unionNode4, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(
                                                table4DataNode,
                                                IQ_FACTORY.createNaryIQTree(unionNode5, ImmutableList.of(table1DataNode, table2DataNode, table3DataNode)),
                                                table4DataNode)),
                                        IQ_FACTORY.createNaryIQTree(joinNode3, ImmutableList.of(
                                                table4DataNode,
                                                IQ_FACTORY.createNaryIQTree(unionNode6, ImmutableList.of(table1DataNode, table2DataNode, table3DataNode)),
                                                table5DataNode)))),
                                table5DataNode))));

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertEquals(expectedQuery, query);
    }

    @Test
    public void unionLiftDoubleLift () {
        // Original Query
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(P3_PREDICATE, ImmutableList.of(A, B, C));

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

        IQ query = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(unionNode21, ImmutableList.of(table1DataNode, table2DataNode, table3DataNode)),
                                        IQ_FACTORY.createNaryIQTree(unionNode22, ImmutableList.of(
                                                table4DataNode,
                                                table5DataNode,
                                                IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(table4DataNode, table4DataNode)))),
                                        table4DataNode)),
                                table5DataNode))));
        System.out.println("\n Original query: \n" +  query);

        query = transform(query, unionNode21, joinNode);
        System.out.println("\n Optimized query: \n" +  query);

        // Expected Query
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

        IQ expectedQuery = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode3, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(unionNode4, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(
                                                table1DataNode,
                                                IQ_FACTORY.createNaryIQTree(unionNode5, ImmutableList.of(
                                                        table4DataNode,
                                                        table5DataNode,
                                                        IQ_FACTORY.createNaryIQTree(joinNode5, ImmutableList.of(table4DataNode, table4DataNode)))),
                                                table4DataNode)),
                                        IQ_FACTORY.createNaryIQTree(joinNode3, ImmutableList.of(
                                                table2DataNode,
                                                IQ_FACTORY.createNaryIQTree(unionNode6, ImmutableList.of(
                                                        table4DataNode,
                                                        table5DataNode,
                                                        IQ_FACTORY.createNaryIQTree(joinNode6, ImmutableList.of(table4DataNode, table4DataNode)))),
                                                table4DataNode)),
                                        IQ_FACTORY.createNaryIQTree(joinNode4, ImmutableList.of(
                                                table3DataNode,
                                                IQ_FACTORY.createNaryIQTree(unionNode7, ImmutableList.of(
                                                        table4DataNode,
                                                        table5DataNode,
                                                        IQ_FACTORY.createNaryIQTree(joinNode7, ImmutableList.of(table4DataNode, table4DataNode)))),
                                                table4DataNode)))),
                                table5DataNode))));
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertEquals(expectedQuery, query);


        System.out.println("\n Continue from the expected query: \n" +  expectedQuery);

        query = transform(query, unionNode5, joinNode2);
        IQ query2 = expectedQuery;

        System.out.println("\n Optimized query: \n" +  query2);


        // Second Expected Query
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

        IQ expectedQuery2 = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode8, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(unionNode9, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(unionNode10, ImmutableList.of(
                                                IQ_FACTORY.createNaryIQTree(joinNode8, ImmutableList.of(table1DataNode, table4DataNode, table4DataNode)),
                                                IQ_FACTORY.createNaryIQTree(joinNode11, ImmutableList.of(table1DataNode, table5DataNode, table4DataNode)),
                                                IQ_FACTORY.createNaryIQTree(joinNode12, ImmutableList.of(
                                                        table1DataNode,
                                                        IQ_FACTORY.createNaryIQTree(joinNode13, ImmutableList.of(table4DataNode, table4DataNode))))
                                        )),
                                        IQ_FACTORY.createNaryIQTree(joinNode9, ImmutableList.of(
                                                table2DataNode,
                                                IQ_FACTORY.createNaryIQTree(unionNode11, ImmutableList.of(
                                                        table4DataNode,
                                                        table5DataNode,
                                                        IQ_FACTORY.createNaryIQTree(joinNode14, ImmutableList.of(table4DataNode, table4DataNode)))),
                                                table4DataNode)),
                                        IQ_FACTORY.createNaryIQTree(joinNode10, ImmutableList.of(
                                                table3DataNode,
                                                IQ_FACTORY.createNaryIQTree(unionNode12, ImmutableList.of(
                                                        table4DataNode,
                                                        table5DataNode,
                                                        IQ_FACTORY.createNaryIQTree(joinNode15, ImmutableList.of(table4DataNode, table4DataNode)))),
                                                table4DataNode)))),
                                table5DataNode))));

        System.out.println("\n Expected query: \n" +  expectedQuery2);

        assertEquals(expectedQuery2, query2);
    }

    // Was expecting a InvalidQueryOptimizationProposalException
    @Ignore
    @Test
    public void unionLiftInternalTest3 () {
         // Original Query
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(P3_PREDICATE, ImmutableList.of(A, B, C));

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

        IQ query = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(table1DataNode, table2DataNode, table3DataNode)),
                                        table4DataNode)),
                                table5DataNode))));

        System.out.println("\n Original query: \n" +  query);

        query = transform(query, unionNode2, unionNode1);
    }

    // Was expecting a InvalidQueryOptimizationProposalException
    @Ignore
    @Test
    public void unionLiftInternalTest4 () {
        // Original Query
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(P3_PREDICATE, ImmutableList.of(A, B, C));

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

        IQ query = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(table1DataNode, table2DataNode)),
                                        table4DataNode),
                                IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(table1DataNode, table5DataNode))))));

        System.out.println("\n Original query: \n" +  query);

        query = transform(query, unionNode, joinNode1);
        System.out.println("\n Optimized query: \n" +  query);
    }

    // Was expecting a InvalidQueryOptimizationProposalException
    @Ignore
    @Test
    public void unionLiftInternalTest5 () {
        // Original Query
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(P3_PREDICATE, ImmutableList.of(A, B, C));

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

        IQ query = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                        table4DataNode,
                                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(table1DataNode, table2DataNode))),
                                IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(table3DataNode, table5DataNode))))));

        System.out.println("\n Original query: \n" +  query);

        query = transform(query, unionNode, joinNode1);
        System.out.println("\n Optimized query: \n" +  query);
    }

    @Test
    public void unionLiftInternalTest6 () {
        // Original Query
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM =
                ATOM_FACTORY.getDistinctVariableOnlyDataAtom(P3_PREDICATE, ImmutableList.of(A, B, C));

        ConstructionNode rootConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B));

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(A, C));
        ExtensionalDataNode table5DataNode = createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(A, E));

        IQ query = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(table1DataNode, table2DataNode)),
                                        table4DataNode),
                                table5DataNode))));

        System.out.println("\n Original query: \n" +  query);

        query = transform(query, unionNode, joinNode);
        System.out.println("\n Optimized query: \n" +  query);

        // Expected Query
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(A, B, C, E));
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        LeftJoinNode leftJoinNode2 = IQ_FACTORY.createLeftJoinNode();

        IQ expectedQuery = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootConstructionNode,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(
                                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, table1DataNode, table4DataNode),
                                        table5DataNode)),
                                IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(
                                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode2, table2DataNode, table4DataNode),
                                        table5DataNode))))));

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertEquals(expectedQuery, query);
    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }
}
