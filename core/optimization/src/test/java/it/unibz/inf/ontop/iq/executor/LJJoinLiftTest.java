package it.unibz.inf.ontop.iq.executor;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;

import static org.junit.Assert.assertEquals;

/**
 * Last tests remaining from UnionLiftInternalTest
 */
public class LJJoinLiftTest {

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
    public void reduceLJTest1() {
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

        query = transform(query);


        // Expected Query
        InnerJoinNode joinNodeExpected = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNodeExpected = IQ_FACTORY.createUnionNode(ImmutableSet.of(AF0));

        ConstructionNode topConstructionExpected = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(AF0), Y, generateURI1(D), Z, generateURI1(F)));

        IQ expectedQuery = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createUnaryIQTree(topConstructionExpected,
                        IQ_FACTORY.createNaryIQTree(joinNodeExpected, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(unionNodeExpected, ImmutableList.of(
                                        IQ_FACTORY.createExtensionalDataNode(TABLE1_AR1, ImmutableMap.of(0, AF0)),
                                        IQ_FACTORY.createExtensionalDataNode(TABLE2_AR1, ImmutableMap.of(0, AF0)))),
                                IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, AF0, 1, D)),
                                IQ_FACTORY.createExtensionalDataNode(TABLE4_AR2, ImmutableMap.of(0, D, 1, F))))));

        System.out.println("\n Optimized query: \n" +  query);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertEquals(expectedQuery, query);
    }

    private IQ transform(IQ query) {
        return UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query);
    }

    @Test
    public void testLjLift1() {
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

        query = transform(query);
        System.out.println("\n Optimized query: \n" +  query);

        // Expected Query

        IQ expectedQuery = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(table1DataNode, table2DataNode)),
                                IQ_FACTORY.createExtensionalDataNode(TABLE5_AR2, ImmutableMap.of(0, A)))),
                        table4DataNode));

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertEquals(expectedQuery, query);
    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }
}
