package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel.GT;
import static junit.framework.TestCase.assertEquals;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;

/**
 * Test the top down substitution lift optimizer
 */
public class BindingLiftTest {

    private final AtomPredicate ANS1_ARITY_2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 2);

    // TEMPORARY HACK!
    private final ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/ds1/", 0);
    private final ImmutableList<Template.Component> URI_TEMPLATE_STR_2 =  Template.of("http://example.org/ds2/", 0);
    private final ImmutableList<Template.Component> URI_TEMPLATE_STR_2_2 = Template.of("http://example.org/ds2/", 0, "/", 1);

    private final ExtensionalDataNode DATA_NODE_1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
    private final ExtensionalDataNode DATA_NODE_2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, E));
    private final ExtensionalDataNode DATA_NODE_3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));
    private final ExtensionalDataNode DATA_NODE_4 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
    private final ExtensionalDataNode DATA_NODE_5 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D));
    private final ExtensionalDataNode DATA_NODE_6 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(E, F));
    private final ExtensionalDataNode DATA_NODE_7 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(G, H));
    private final ExtensionalDataNode DATA_NODE_8 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
    private final ExtensionalDataNode DATA_NODE_9 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));

    private final ImmutableExpression EXPRESSIONGT = TERM_FACTORY.getDBDefaultInequality(GT, Z, Y);

    @Test
    public void testSimpleSubstitution() {
        testSimpleSubstitution(false);
    }

    @Test
    public void testSimpleSubstitutionWithTopConstructionNode() {
        testSimpleSubstitution(true);
    }

    private void testSimpleSubstitution(boolean withTopConstructionNode) {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, X, Y, Z);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(EXPRESSIONGT);
        ConstructionNode leftNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateInt(A)));
        ImmutableSet<Variable> subQueryProjectedVariables = ImmutableSet.of(X,A);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(subQueryProjectedVariables);
        ConstructionNode subQueryConstructionNode1 = IQ_FACTORY.createConstructionNode(subQueryProjectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(B)));
        ConstructionNode subQueryConstructionNode2 = IQ_FACTORY.createConstructionNode(subQueryProjectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(E)));
        ConstructionNode rightNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(C), Y, generateInt(D)));

        IQTree tree = IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                IQ_FACTORY.createUnaryIQTree(leftNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(subQueryConstructionNode1, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(subQueryConstructionNode2, DATA_NODE_2)))),
                IQ_FACTORY.createUnaryIQTree(rightNode, DATA_NODE_3)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                withTopConstructionNode
                        ? IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(projectionAtom.getVariables()), tree)
                        : tree);


        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(C), Y, generateInt(D), Z, generateInt(A)));
        ImmutableExpression expectedExpressionGT = TERM_FACTORY.getDBDefaultInequality(GT, generateInt(A), generateInt(D));
        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode(expectedExpressionGT);

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode,
                        IQ_FACTORY.createNaryIQTree(expectedJoinNode, ImmutableList.of(
                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, C)),
                                createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D))))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }


    @Test
    public void testLeftJoinSubstitution() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, X, W, Z);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        InnerJoinNode joinNodeOnLeft = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftNodeJoin = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, W),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A), W, generateString(B)));
        ConstructionNode rightNodeJoin = IQ_FACTORY.createConstructionNode(ImmutableSet.of(W,Z),
                SUBSTITUTION_FACTORY.getSubstitution(W, generateString(C), Z, generateInt(D)));
        ImmutableSet<Variable> subQueryProjectedVariables = ImmutableSet.of(X,Y);
        UnionNode unionNodeOnRight = IQ_FACTORY.createUnionNode(subQueryProjectedVariables);
        ConstructionNode subQueryConstructionNode1 = IQ_FACTORY.createConstructionNode(subQueryProjectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(E), Y, generateInt((F))));
        ConstructionNode subQueryConstructionNode2 = IQ_FACTORY.createConstructionNode(subQueryProjectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(G), Y , generateInt(H)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                IQ_FACTORY.createNaryIQTree(joinNodeOnLeft, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(leftNodeJoin, DATA_NODE_4),
                                        IQ_FACTORY.createUnaryIQTree(rightNodeJoin, DATA_NODE_5))),
                                IQ_FACTORY.createNaryIQTree(unionNodeOnRight, ImmutableList.of(
                                                IQ_FACTORY.createUnaryIQTree(subQueryConstructionNode1, DATA_NODE_6),
                                        IQ_FACTORY.createUnaryIQTree(subQueryConstructionNode2, DATA_NODE_7))))));

        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(W, generateString(B), X, generateIRIWithTemplate1(A), Z, generateInt(D)));
        LeftJoinNode expectedleftJoinNode = IQ_FACTORY.createLeftJoinNode();
        InnerJoinNode expectedJoinNodeOnLeft = IQ_FACTORY.createInnerJoinNode();

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode,
                IQ_FACTORY.createBinaryNonCommutativeIQTree(expectedleftJoinNode,
                        IQ_FACTORY.createNaryIQTree(expectedJoinNodeOnLeft, ImmutableList.of(
                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B)),
                                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(B, D)))),
                        IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, A)))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testUnionSubstitution() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();

        UnionNode unionNode2 =  IQ_FACTORY.createUnionNode(projectionAtom.getVariables());
        ConstructionNode subQuery1UnionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A), Y, generateIRIWithTemplate1(B)));
        ConstructionNode subQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C), Y, generateIRIWithTemplate1(D)));

        UnionNode unionNode3 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(Y));
        ConstructionNode subQuery1UnionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(F)));
        ConstructionNode subQuery2UnionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI2(H)));

        UnionNode unionNode1 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode subQuery1UnionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(I)));
        ConstructionNode subQuery2UnionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(M)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                        IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(subQuery1UnionNode2,
                                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B))),
                                        IQ_FACTORY.createUnaryIQTree(subQuery2UnionNode2,
                                                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D))))),
                                IQ_FACTORY.createNaryIQTree(unionNode3, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(subQuery1UnionNode3,
                                                createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(E, F))),
                                        IQ_FACTORY.createUnaryIQTree(subQuery2UnionNode3,
                                                createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(G, H))))))),
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(subQuery1UnionNode1,
                                        createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(I, L))),
                                IQ_FACTORY.createUnaryIQTree(subQuery2UnionNode1,
                                        createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(M, N))))))));

        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(F0),
                            //generateIRIWithTemplate1(F0, AF1),
                        Y, generateIRIWithTemplate1(BF1)));
        UnionNode expectedUnionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(BF1, F0));
        ConstructionNode expectedSubQuery1UnionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(F0, generateIRIString(URI_TEMPLATE_STR_1, A)));
        InnerJoinNode joinNode11 = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode expectedSubQuery2UnionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(F0, generateIRIString(URI_TEMPLATE_STR_2, C)));
        InnerJoinNode joinNode21 = IQ_FACTORY.createInnerJoinNode();

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode,
                        IQ_FACTORY.createNaryIQTree(expectedUnionNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(expectedSubQuery1UnionNode,
                                        IQ_FACTORY.createNaryIQTree(joinNode11, ImmutableList.of(
                                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, BF1)),
                                                IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(1, BF1)),
                                                IQ_FACTORY.createExtensionalDataNode(TABLE5_AR2, ImmutableMap.of(0, A))))),
                                IQ_FACTORY.createUnaryIQTree(expectedSubQuery2UnionNode,
                                        IQ_FACTORY.createNaryIQTree(joinNode21, ImmutableList.of(
                                                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, BF1)),
                                                IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(1, BF1)),
                                                IQ_FACTORY.createExtensionalDataNode(TABLE6_AR2, ImmutableMap.of(0, C)))))))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testEqualityLiftingNonProjected1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A)));

        ConstructionNode subConstructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode centralConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(C)));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateIRIWithTemplate1(E),
                        Y, generateIRIWithTemplate1(E)));
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F));

        IQ initialQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                        IQ_FACTORY.createUnaryIQTree(subConstructionNode1,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(centralConstructionNode, DATA_NODE_3),
                                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode, rightDataNode)))))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(AF1)));
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(AF1));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, AF1)),
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, AF1)),
                                        IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, AF1))))))));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testEqualityLiftingNonProjected2WithTopConstructionNode() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);
        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A)));
        ConstructionNode intermediateConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(E)));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode centralConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(C)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, E),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(E)));
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F));

        IQ initialQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(initialRootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(intermediateConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                                IQ_FACTORY.createUnaryIQTree(centralConstructionNode, DATA_NODE_3),
                                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, rightDataNode))))))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(AF1)));
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(AF1));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, AF1)),
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, AF1)),
                                        IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, AF1))))))));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testEqualityLiftingNonProjected2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A)));
        ConstructionNode intermediateConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(E)));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode centralConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(C)));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, E),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(E)));
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(E, F));

        IQ initialQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                        IQ_FACTORY.createUnaryIQTree(intermediateConstructionNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(centralConstructionNode, DATA_NODE_3),
                                        IQ_FACTORY.createUnaryIQTree(rightConstructionNode, rightDataNode)))))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(AF1)));
        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(AF1));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        IQ_FACTORY.createNaryIQTree(newUnionNode, ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, AF1)),
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(0, AF1)),
                                        IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, AF1))))))));

        optimizeAndCompare(initialQuery, expectedQuery);
    }



    private ImmutableFunctionalTerm generateIRIWithTemplate1(VariableOrGroundTerm argument) {
        return generateURI1(URI_TEMPLATE_STR_1, argument);
    }

    private ImmutableFunctionalTerm generateIRIString(ImmutableList<Template.Component> template, Variable... variables) {
        return TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getDBFunctionSymbolFactory().getIRIStringTemplateFunctionSymbol(template),
                variables);
    }

    private ImmutableFunctionalTerm generateURI1(ImmutableList<Template.Component> prefix, VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(prefix, ImmutableList.of(argument));
    }

    private ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_2, ImmutableList.of(argument));
    }

    private ImmutableFunctionalTerm generateCompositeURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_2_2, ImmutableList.of(argument1, argument2));
    }

    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getRDFLiteralFunctionalTerm(argument, XSD.INTEGER);
    }

    private ImmutableFunctionalTerm generateString(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getRDFLiteralFunctionalTerm(argument, XSD.STRING);

    }


    @Test
    public void testNewConstructionNode() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(B)));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(unionNode.getVariables());
        ConstructionNode subQuery1UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(I), Y, NULL));
        ConstructionNode subQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(M), Y, NULL));
        ConstructionNode subQueryJoinNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(subQueryJoinNode,
                                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B))),
                                        createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(E, B))))),
                        IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(subQuery1UnionNode2,
                                        createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(I, L))),
                                IQ_FACTORY.createUnaryIQTree(subQuery2UnionNode2,
                                        createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(M, N))))))));

        Variable f0f1 = TERM_FACTORY.getVariable("f0f1");
        Variable f2 = TERM_FACTORY.getVariable("f2");
        Variable f3 = TERM_FACTORY.getVariable("f3");

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(f0f1),
                        Y, TERM_FACTORY.getRDFFunctionalTerm(f2, f3)));
        UnionNode expectedUnionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(f0f1, f2, f3));
        ConstructionNode expSubQueryUnionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        f2, generateIRIWithTemplate1(B).getTerm(0),
                        f3, TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getIRITermType()),
                        f0f1, generateIRIString(URI_TEMPLATE_STR_1, A)));

        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode expectedRightConstructionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(f2, NULL, f3, NULL));
        UnionNode expectedUnionNode2 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(f0f1));
        ConstructionNode expSubQuery1UnionNode2 = IQ_FACTORY.createConstructionNode(expectedUnionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(f0f1, generateIRIString(URI_TEMPLATE_STR_1, I)));
        ConstructionNode expSubQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(expectedUnionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(f0f1, generateIRIString(URI_TEMPLATE_STR_2, M)));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(topConstructionNode,
                        IQ_FACTORY.createNaryIQTree(expectedUnionNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(expSubQueryUnionNode,
                                        IQ_FACTORY.createNaryIQTree(expectedJoinNode, ImmutableList.of(
                                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B)),
                                                IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(1, B))))),
                                IQ_FACTORY.createUnaryIQTree(expectedRightConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(expectedUnionNode2, ImmutableList.of(
                                                IQ_FACTORY.createUnaryIQTree(expSubQuery1UnionNode2,
                                                        IQ_FACTORY.createExtensionalDataNode(TABLE5_AR2, ImmutableMap.of(0, I))),
                                                IQ_FACTORY.createUnaryIQTree(expSubQuery2UnionNode2,
                                                        IQ_FACTORY.createExtensionalDataNode(TABLE6_AR2, ImmutableMap.of(0, M))))))))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    /**
     * Second optimization needed to lift the bindings of the first union (presence of a join with bindings as a child of the union)
     */

    @Test
    public void testCompositeURITemplateDoubleRun() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        UnionNode unionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));

        ConstructionNode subQuery1UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateCompositeURI2(I, L), Y, NULL));
        ConstructionNode subQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateCompositeURI2(M, N), Y, NULL));

        ConstructionNode subQueryJoinNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateCompositeURI2(A, B), Y, generateIRIWithTemplate1(B)));
        ConstructionNode subQueryJoinNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(F)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(subQueryJoinNode,
                                        createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B))),
                                IQ_FACTORY.createUnaryIQTree(subQueryJoinNode2,
                                        createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(E, F))))),
                        IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(subQuery1UnionNode2,
                                        createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(I, L))),
                                IQ_FACTORY.createUnaryIQTree(subQuery2UnionNode2,
                                        createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(M, N))))))));

        Variable af3 = TERM_FACTORY.getVariable("af3");
        Variable bf4 = TERM_FACTORY.getVariable("bf4");
        Variable f5 = TERM_FACTORY.getVariable("f5");
        Variable f6 = TERM_FACTORY.getVariable("f6");

        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateCompositeURI2(af3,bf4),
                        Y, TERM_FACTORY.getRDFFunctionalTerm(f5, f6)));
        UnionNode expectedUnionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(bf4, af3, f5, f6));
        ConstructionNode expSubQueryUnionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        f5, generateIRIWithTemplate1(bf4).getTerm(0),
                        f6, TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getIRITermType())));
        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode newRightConstructionNode = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(f5, NULL, f6, NULL));
        UnionNode expectedUnionNode2 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(af3, bf4));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode,
                        IQ_FACTORY.createNaryIQTree(expectedUnionNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(expSubQueryUnionNode,
                                        IQ_FACTORY.createNaryIQTree(expectedJoinNode, ImmutableList.of(
                                                createExtensionalDataNode(TABLE1_AR2,ImmutableList.of(af3,bf4)),
                                                IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(1, bf4))))),
                                IQ_FACTORY.createUnaryIQTree(newRightConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(expectedUnionNode2, ImmutableList.of(
                                                createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(af3, bf4)),
                                                createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(af3, bf4)))))))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testCompositeURITemplateDoubleRunYProjectedAway() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, X);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode2 =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X,Y));
        ConstructionNode subQuery1UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateCompositeURI2(I, L), Y, NULL));
        ConstructionNode subQuery2UnionNode2 = IQ_FACTORY.createConstructionNode(unionNode2.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateCompositeURI2(M, N), Y, NULL));
        ConstructionNode subQueryJoinNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateCompositeURI2(A, B), Y, generateIRIWithTemplate1(B)));
        ConstructionNode subQueryJoinNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(F)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(subQueryJoinNode,
                                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B))),
                                        IQ_FACTORY.createUnaryIQTree(subQueryJoinNode2,
                                                createExtensionalDataNode(TABLE3_AR2,ImmutableList.of( E, F))))),
                                IQ_FACTORY.createNaryIQTree(unionNode2, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(subQuery1UnionNode2,
                                                createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(I, L))),
                                        IQ_FACTORY.createUnaryIQTree(subQuery2UnionNode2,
                                                createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(M, N)))))))));

        Variable af3 = TERM_FACTORY.getVariable("af3");
        Variable bf4 = TERM_FACTORY.getVariable("bf4");
        Variable f5 = TERM_FACTORY.getVariable("f5");
        Variable f6 = TERM_FACTORY.getVariable("f6");

        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateCompositeURI2(af3,bf4)));
        UnionNode expectedUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(bf4, af3));
        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode,
                        IQ_FACTORY.createNaryIQTree(expectedUnionNode, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(expectedJoinNode, ImmutableList.of(
                                                createExtensionalDataNode(TABLE1_AR2,ImmutableList.of(af3,bf4)),
                                                IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(1, bf4)))),
                                        createExtensionalDataNode(TABLE5_AR2, ImmutableList.of(af3, bf4)),
                                        createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(af3, bf4))))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testLeftJoinAndUnionLiftSubstitution() {
        testLeftJoinAndUnionLiftSubstitution(false);
    }

    @Test
    public void testLeftJoinAndUnionLiftSubstitutionWithTopConstructionNode() {
        testLeftJoinAndUnionLiftSubstitution(true);
    }

    private void testLeftJoinAndUnionLiftSubstitution(boolean withTopConstructionNode) {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR4_PREDICATE, X, Y, Z, W);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        ConstructionNode rightNodeJoin = IQ_FACTORY.createConstructionNode(ImmutableSet.of(W,Y),
                SUBSTITUTION_FACTORY.getSubstitution(W, generateInt(H), Y, generateInt(G)));

        UnionNode unionNodeOnLeft = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        ConstructionNode leftUnionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A), Y, generateInt(B)));
        ConstructionNode rightUnionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C), Y, generateInt(D)));

        ConstructionNode nodeOnRight = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Z),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(E), Z, generateInt(F)));


        IQTree tree = IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                        IQ_FACTORY.createNaryIQTree(unionNodeOnLeft, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftUnionNode,
                                        createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B))),
                                IQ_FACTORY.createUnaryIQTree(rightUnionNode,
                                        createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D))))),
                        IQ_FACTORY.createUnaryIQTree(nodeOnRight,
                                createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(E, F)))
                        ),
                IQ_FACTORY.createUnaryIQTree(rightNodeJoin, createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(G, H)) )
        ));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                withTopConstructionNode
                        ? IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(projectionAtom.getVariables()), tree)
                        : tree);


        //----------------------------------------------------------------------
        // Construct expected query
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
                        W, generateInt(H),
                        X, TERM_FACTORY.getIRIFunctionalTerm(F0),
                        Y, generateInt(BF1),
                        Z, TERM_FACTORY.getRDFFunctionalTerm(
                                F,
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDBIsNotNull(F),
                                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getDatatype(XSD.INTEGER)))))));
        UnionNode expectedUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(F0, BF1, F, H));

        ConstructionNode expectedNodeOnLeft = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        F0, generateIRIString(URI_TEMPLATE_STR_1, A)));
        LeftJoinNode expectedLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        InnerJoinNode leftInnerJoinNode = IQ_FACTORY.createInnerJoinNode();

        ConstructionNode expectedNodeOnRight = IQ_FACTORY.createConstructionNode(expectedUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(F, NULL,
                        F0, generateIRIString(URI_TEMPLATE_STR_2, C)));
        InnerJoinNode rightInnerJoinNode = IQ_FACTORY.createInnerJoinNode();

        IQ expectedQuery =   IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode,
                        IQ_FACTORY.createNaryIQTree(expectedUnionNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(expectedNodeOnLeft,
                                        IQ_FACTORY.createBinaryNonCommutativeIQTree(expectedLeftJoinNode,
                                                IQ_FACTORY.createNaryIQTree(leftInnerJoinNode, ImmutableList.of(
                                                        createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, BF1)),
                                                        createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(BF1, H)))),
                                                createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, F)))),
                                IQ_FACTORY.createUnaryIQTree(expectedNodeOnRight,
                                        IQ_FACTORY.createNaryIQTree(rightInnerJoinNode, ImmutableList.of(
                                                createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, BF1)),
                                                createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(BF1, H)))))))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testConstantNonPropagationAcrossUnions() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(B)));
        UnionNode topUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, B));

        UnionNode leftUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, B));
        DBConstant two = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A), B, two));
        ExtensionalDataNode dataNode7 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C), B, two));
        ExtensionalDataNode dataNode8 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(C));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        DBConstant three = TERM_FACTORY.getDBConstant("3", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(D), B, three));
        ExtensionalDataNode dataNode9 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(D));
        ConstructionNode lastConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(E)));
        ExtensionalDataNode dataNode10 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(E));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(topUnionNode, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(leftUnionNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode7),
                                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode8))),
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(constructionNode3, dataNode9),
                                        IQ_FACTORY.createUnaryIQTree(lastConstructionNode, dataNode10)))))));

        Variable f0f2 = TERM_FACTORY.getVariable("f0f2");

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(f0f2), Y, generateIRIWithTemplate1(B)));
        UnionNode newTopUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(f0f2, B));

        ConstructionNode newLeftConstructionNode = IQ_FACTORY.createConstructionNode(newTopUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, two));
        UnionNode newLeftUnionNode = IQ_FACTORY.createUnionNode(newLeftConstructionNode.getChildVariables());
        ConstructionNode newConstructionNode1 = IQ_FACTORY.createConstructionNode(newLeftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(f0f2, generateIRIString(URI_TEMPLATE_STR_1, A)));
        ConstructionNode newConstructionNode2 = IQ_FACTORY.createConstructionNode(newLeftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(f0f2, generateIRIString(URI_TEMPLATE_STR_2, C)));

        ConstructionNode newConstructionNode3 = IQ_FACTORY.createConstructionNode(newTopUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(f0f2, generateIRIString(URI_TEMPLATE_STR_1, D), B, three));

        IQ expectedQuery =  IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        IQ_FACTORY.createNaryIQTree(newTopUnionNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(newLeftConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(newLeftUnionNode, ImmutableList.of(
                                                IQ_FACTORY.createUnaryIQTree(newConstructionNode1,
                                                        createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A))),
                                                IQ_FACTORY.createUnaryIQTree(newConstructionNode2,
                                                        createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(C)))))),
                                IQ_FACTORY.createUnaryIQTree(newConstructionNode3,
                                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                                createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(D)),
                                                createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(D)))))))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testConstantNonPropagationAcrossUnions2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(B)));
        UnionNode topUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, B));

        UnionNode leftUnionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, B));
        DBConstant two = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A), B, two));
        ExtensionalDataNode dataNode7 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(leftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C), B, two));
        ExtensionalDataNode dataNode8 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(C));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(D)));
        ExtensionalDataNode dataNode9 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(D));
        ExtensionalDataNode dataNode10 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(B));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(topUnionNode, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(leftUnionNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode7),
                                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode8))),
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(constructionNode3, dataNode9),
                                        dataNode10))))));


        Variable f0f1 = TERM_FACTORY.getVariable("f0f1");
        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(f0f1),
                        Y, generateIRIWithTemplate1(B)));
        UnionNode newTopUnionNode = IQ_FACTORY.createUnionNode(newRootNode.getChildVariables());

        ConstructionNode newLeftConstructionNode = IQ_FACTORY.createConstructionNode(newTopUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, two));
        UnionNode newLeftUnionNode = IQ_FACTORY.createUnionNode(newLeftConstructionNode.getChildVariables());
        ConstructionNode newConstructionNode1 = IQ_FACTORY.createConstructionNode(newLeftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(f0f1, generateIRIString(URI_TEMPLATE_STR_1, A)));
        ConstructionNode newConstructionNode2 = IQ_FACTORY.createConstructionNode(newLeftUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(f0f1, generateIRIString(URI_TEMPLATE_STR_2, C)));

        ConstructionNode constructionNode3b = IQ_FACTORY.createConstructionNode(newTopUnionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(f0f1, generateIRIString(URI_TEMPLATE_STR_1, D)));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        IQ_FACTORY.createNaryIQTree(newTopUnionNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(newLeftConstructionNode,
                                        IQ_FACTORY.createNaryIQTree(newLeftUnionNode, ImmutableList.of(
                                                IQ_FACTORY.createUnaryIQTree(newConstructionNode1,
                                                        createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A))),
                                                IQ_FACTORY.createUnaryIQTree(newConstructionNode2,
                                                        createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(C)))))),
                                IQ_FACTORY.createUnaryIQTree(constructionNode3b,
                                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                                createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(D)),
                                                dataNode10)))))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testTrueNode() {
        testTrueNode(TERM_FACTORY.getStrictEquality(buildSparqlDatatype(X), buildSparqlDatatype(Y)));
    }

    private void testTrueNode(ImmutableExpression joiningCondition) {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        UnionNode unionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode leftChildUnion = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A) ));
        ConstructionNode rightChildUnion = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateString(C)));
        ConstructionNode otherNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateInt(TERM_FACTORY.getDBStringConstant("2"))));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftChildUnion, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(rightChildUnion, DATA_NODE_3))),
                        IQ_FACTORY.createUnaryIQTree(otherNode, IQ_FACTORY.createTrueNode()))));


        Variable af0 = TERM_FACTORY.getVariable("af0");
        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(af0), Y,
                        TERM_FACTORY.getRDFLiteralConstant("2", XSD.INTEGER)));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode,
                        IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, af0))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testJoinAndNotMatchingDatatypesDoubleRun() {
        testJoinAndNotMatchingDatatypesDoubleRun(TERM_FACTORY.getStrictEquality(
                buildSparqlDatatype(X), buildSparqlDatatype(Y)));
    }


    private void testJoinAndNotMatchingDatatypesDoubleRun(ImmutableExpression joiningCondition) {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        UnionNode unionNode =  IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        ConstructionNode leftChildUnion = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        ConstructionNode rightChildUnion = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateString(C)));
        ConstructionNode otherNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateInt(F)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(leftChildUnion, DATA_NODE_1),
                                        IQ_FACTORY.createUnaryIQTree(rightChildUnion, DATA_NODE_3))),
                                IQ_FACTORY.createUnaryIQTree(otherNode, DATA_NODE_6)))));

        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(AF0), Y, generateInt(F)));
        InnerJoinNode expectedJoinNode = IQ_FACTORY.createInnerJoinNode();

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode,
                        IQ_FACTORY.createNaryIQTree(expectedJoinNode, ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, AF0)),
                                IQ_FACTORY.createExtensionalDataNode(TABLE3_AR2, ImmutableMap.of(1, F))))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testDatatypeExpressionEvaluator()  {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        InnerJoinNode jn = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getStrictEquality(
                buildSparqlDatatype(X), buildSparqlDatatype(Y)));
        ConstructionNode leftCn = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A)));
        ConstructionNode rightCn = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateInt(B)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(jn, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(leftCn, DATA_NODE_8),
                                IQ_FACTORY.createUnaryIQTree(rightCn, DATA_NODE_9)))));

        ConstructionNode expectedRootNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateInt(A), Y, generateInt(B)));
        InnerJoinNode jn2 = IQ_FACTORY.createInnerJoinNode();

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(expectedRootNode,
                        IQ_FACTORY.createNaryIQTree(jn2, ImmutableList.of(DATA_NODE_8, DATA_NODE_9))));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testEmptySubstitutionToBeLifted() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        LeftJoinNode leftJoinNode =  IQ_FACTORY.createLeftJoinNode();
        ConstructionNode leftNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateIRIWithTemplate1(B)));
        ConstructionNode rightNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C), Y, generateIRIWithTemplate1(D)));
        ConstructionNode node1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode,
                                IQ_FACTORY.createUnaryIQTree(leftNode, DATA_NODE_9),
                                IQ_FACTORY.createUnaryIQTree(rightNode, DATA_NODE_3)),
                        IQ_FACTORY.createUnaryIQTree(node1, DATA_NODE_8))));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createEmptyNode(projectionAtom.getVariables()));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testUnionRemoval() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        UnionNode unionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A), Y, generateURI2(B)));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(C), Y, generateURI2(D)));
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(E), Y, generateURI2(F)));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createUnaryIQTree(constructionNode1, DATA_NODE_1),
                                        IQ_FACTORY.createUnaryIQTree(constructionNode2, DATA_NODE_3))),
                                IQ_FACTORY.createUnaryIQTree(constructionNode3, DATA_NODE_6)))));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode3, DATA_NODE_6));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testJoinTemplateAndFunctionalGroundTerm1() {
        testJoinTemplateAndFunctionalGroundTerm(true);
    }

    @Test
    public void testJoinTemplateAndFunctionalGroundTerm2() {
        testJoinTemplateAndFunctionalGroundTerm(false);
    }


    private void testJoinTemplateAndFunctionalGroundTerm(boolean trueNodeOnTheLeft) {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A), Y, generateURI2(B)));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(ONE_STR)));
        TrueNode trueNode = IQ_FACTORY.createTrueNode();

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, trueNodeOnTheLeft
                        ? ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(constructionNode2, trueNode),
                                IQ_FACTORY.createUnaryIQTree(constructionNode1, DATA_NODE_1))
                        : ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(constructionNode1, DATA_NODE_1),
                                IQ_FACTORY.createUnaryIQTree(constructionNode2, trueNode))));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(ONE_STR).simplify(), Y, generateURI2(B)));
        ExtensionalDataNode newLeftDataNode = IQ_FACTORY.createExtensionalDataNode(
                DATA_NODE_1.getRelationDefinition(), ImmutableMap.of(0, ONE_STR, 1, B));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, newLeftDataNode));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }



    @Test
    public void testUnionWithJoin() {
        DistinctVariableOnlyDataAtom ROOT_CONSTRUCTION_NODE_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, ImmutableList.of(X,Y, Z));

        ConstructionNode emptyConstructionNode = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables());
        ConstructionNode emptyConstructionNode2 = IQ_FACTORY.createConstructionNode(ROOT_CONSTRUCTION_NODE_ATOM.getVariables());

        UnionNode unionNode1  = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        UnionNode unionNode21 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        UnionNode unionNode22  = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, C));
        InnerJoinNode joinNode1 = IQ_FACTORY.createInnerJoinNode();

        ConstructionNode constructionNode21 = IQ_FACTORY.createConstructionNode(unionNode21.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A), Y, generateURI2(B)));
        ConstructionNode constructionNode22 = IQ_FACTORY.createConstructionNode(unionNode22.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(D)));
        ConstructionNode constructionNode22URI2 = IQ_FACTORY.createConstructionNode(unionNode22.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(D)));
        ConstructionNode constructionNode21URI2 = IQ_FACTORY.createConstructionNode(unionNode21.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI2(A), Y, generateURI2(B)));
        ConstructionNode constructionNode21URI1XY = IQ_FACTORY.createConstructionNode(unionNode21.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(A), Y, generateURI2(B)));
        ConstructionNode constructionNode22Z = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateIRIWithTemplate1(C)));
        ConstructionNode constructionNodeOverJoin2 = IQ_FACTORY.createConstructionNode(unionNode22.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(D)));
        ConstructionNode constructionNodeOverJoin1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Z),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(F), Z, generateIRIWithTemplate1(G)));

        ExtensionalDataNode table1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table2DataNode = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode table4DataNode1 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(D, C));
        ExtensionalDataNode table4DataNode2 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(D, C));
        ExtensionalDataNode table4DataNode3 = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(D, C));
        ExtensionalDataNode table5DataNode1 = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(X, Y, Z));
        ExtensionalDataNode table5DataNode2 = createExtensionalDataNode(TABLE5_AR3, ImmutableList.of(D, E, C));
        ExtensionalDataNode table6DataNode = createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(F, G));

        IQ originalQuery = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(emptyConstructionNode,
                                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(unionNode21, ImmutableList.of(
                                                IQ_FACTORY.createUnaryIQTree(constructionNode21, table1DataNode),
                                                IQ_FACTORY.createUnaryIQTree(constructionNode21URI2, table2DataNode),
                                                IQ_FACTORY.createUnaryIQTree(constructionNode21URI1XY, table3DataNode))),
                                        IQ_FACTORY.createUnaryIQTree(constructionNode22Z,
                                                IQ_FACTORY.createNaryIQTree(unionNode22, ImmutableList.of(
                                                        IQ_FACTORY.createUnaryIQTree(constructionNode22, table4DataNode1),
                                                        IQ_FACTORY.createUnaryIQTree(constructionNode22URI2, table5DataNode2),
                                                        IQ_FACTORY.createUnaryIQTree(constructionNodeOverJoin2,
                                                                IQ_FACTORY.createNaryIQTree(joinNode1, ImmutableList.of(table4DataNode2, table4DataNode3)))))),
                                        IQ_FACTORY.createUnaryIQTree(constructionNodeOverJoin1, table6DataNode)))),
                        IQ_FACTORY.createUnaryIQTree(emptyConstructionNode2, table5DataNode1))));


        Variable bf2f3 = TERM_FACTORY.getVariable("bf2f3");

        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        UnionNode unionNode4 = IQ_FACTORY.createUnionNode(ImmutableSet.of(F, bf2f3));
        UnionNode unionNode5 = IQ_FACTORY.createUnionNode(ImmutableSet.of(F,C));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateIRIWithTemplate1(F), Y, generateURI2(bf2f3), Z, generateIRIWithTemplate1(C)));
        InnerJoinNode joinNode2 = IQ_FACTORY.createInnerJoinNode();
        InnerJoinNode joinNode3 = IQ_FACTORY.createInnerJoinNode();

        ExtensionalDataNode newTable1DataNode = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(F, bf2f3));
        ExtensionalDataNode newTable3DataNode = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(F, bf2f3));
        ExtensionalDataNode newTable4DataNode = createExtensionalDataNode(TABLE4_AR2, ImmutableList.of(F, C));
        ExtensionalDataNode newTable6DataNode = createExtensionalDataNode(TABLE6_AR2, ImmutableList.of(F, C));

        IQ expectedQuery = IQ_FACTORY.createIQ(ROOT_CONSTRUCTION_NODE_ATOM,
                IQ_FACTORY.createNaryIQTree(unionNode3, ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(constructionNode1,
                                IQ_FACTORY.createNaryIQTree(joinNode2, ImmutableList.of(
                                        IQ_FACTORY.createNaryIQTree(unionNode4, ImmutableList.of(newTable1DataNode, newTable3DataNode)),
                                        IQ_FACTORY.createNaryIQTree(unionNode5, ImmutableList.of(
                                                newTable4DataNode,
                                                IQ_FACTORY.createNaryIQTree(joinNode3, ImmutableList.of(newTable4DataNode, newTable4DataNode)))),
                                        newTable6DataNode))),
                        table5DataNode1)));

        optimizeAndCompare(originalQuery, expectedQuery);
    }

    @Test
    public void testProjectionAwaySubQuery() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode leftConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));

        IQ unOptimizedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(leftConstructionNode, DATA_NODE_1),
                        DATA_NODE_8)));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                        IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0,A)),
                        DATA_NODE_8)));

        optimizeAndCompare(unOptimizedQuery, expectedQuery);
    }

    @Test
    public void testLeftJoin2Join1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR1_PREDICATE, A);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(A, B)),
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2))));

        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(A));
        IQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, newDataNode2));

        IQ expectedIQ = IQ_FACTORY.createIQ(initialIQ.getProjectionAtom(), joinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoin2Join2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(B, C));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(B, C)),
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2))));

        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(B, B));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, newDataNode2));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoin2Join3() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2);

        IQTree filterNodeTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(B, ONE)),
                leftJoinTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, filterNodeTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(ONE));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, newDataNode2));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, ONE));

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, newJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Ignore("TODO: support this")
    @Test
    public void testLeftJoin2Join4() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));

        // TODO: use another function (a UDF for instance)
        GroundFunctionalTerm groundTerm = (GroundFunctionalTerm) TERM_FACTORY.getConstantIRI(
                RDF_FACTORY.createIRI("http://my-uri/constant"));

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2);

        ImmutableExpression filterCondition = TERM_FACTORY.getStrictEquality(groundTerm, B);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(filterCondition);

        IQTree filterNodeTree = IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, filterNodeTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, newDataNode2));

        IQTree newFilterNodeTree = IQ_FACTORY.createUnaryIQTree(filterNode, newJoinTree);

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newFilterNodeTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Ignore("TODO: support this")
    @Test
    public void testLeftJoin2Join5() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));

        // TODO: use another function (a UDF for instance)
        ImmutableFunctionalTerm functionalTerm = TERM_FACTORY.getIRIFunctionalTerm(A);

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2);

        ImmutableExpression filterCondition = TERM_FACTORY.getStrictEquality(functionalTerm, B);
        FilterNode filterNode = IQ_FACTORY.createFilterNode(filterCondition);

        IQTree filterNodeTree = IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, filterNodeTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, newDataNode2));

        IQTree newFilterNodeTree = IQ_FACTORY.createUnaryIQTree(filterNode, newJoinTree);

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newFilterNodeTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     * Implicit joining condition
     */
    @Ignore("TODO: support it")
    @Test
    public void testLeftJoin2Join6() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR1, ImmutableList.of(B));

        IQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2);

        InnerJoinNode topJoinNode = IQ_FACTORY.createInnerJoinNode();

        IQTree topJoinNodeTree = IQ_FACTORY.createNaryIQTree(topJoinNode,
                ImmutableList.of(leftJoinTree, dataNode3));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, B);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, topJoinNodeTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(B));
        IQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode1, newDataNode2));

        IQTree newTopJoinNodeTree = IQ_FACTORY.createNaryIQTree(topJoinNode, ImmutableList.of(newJoinTree, dataNode3));

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newTopJoinNodeTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testDescendingEqualityLeftJoinPreserved() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(B, C));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode2);

        UnaryIQTree filterNodeTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createFilterNode(TERM_FACTORY.getStrictEquality(A, B)),
                leftJoinTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, C);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, filterNodeTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ExtensionalDataNode newDataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, A));
        ExtensionalDataNode newDataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, C));
        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), newDataNode1, newDataNode2);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newLeftJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJConstant1() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(B, C));

        ConstructionNode rightConstruction = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, D),
                SUBSTITUTION_FACTORY.getSubstitution(D, ONE));

        UnaryIQTree rightTree = IQ_FACTORY.createUnaryIQTree(rightConstruction, dataNode2);

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, rightTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, D);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, leftJoinTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        Variable prov = TERM_FACTORY.getVariable("prov");
        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2,
                ImmutableMap.of(0, B, 1, prov));

        // Expected
        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, newDataNode2);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(D, generateIfIsNotNullElseNull(prov, ONE)));

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, newLeftJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJConstant2() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(A, B));

        ConstructionNode rightConstruction = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, D),
                SUBSTITUTION_FACTORY.getSubstitution(D, ONE));

        UnaryIQTree rightTree = IQ_FACTORY.createUnaryIQTree(rightConstruction, dataNode2);

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, rightTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, D);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, leftJoinTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        ConstructionNode newRightConstruction = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, F0),
                SUBSTITUTION_FACTORY.getSubstitution(F0, TERM_FACTORY.getProvenanceSpecialConstant()));

        UnaryIQTree newRightTree = IQ_FACTORY.createUnaryIQTree(newRightConstruction, dataNode2);

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, newRightTree);

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(D, generateIfIsNotNullElseNull(F0, ONE)));

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(newRootNode, newLeftJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLJNull() {
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(B, C));

        ConstructionNode rightConstruction = IQ_FACTORY.createConstructionNode(ImmutableSet.of(B, D),
                SUBSTITUTION_FACTORY.getSubstitution(D, NULL));

        UnaryIQTree rightTree = IQ_FACTORY.createUnaryIQTree(rightConstruction, dataNode2);

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, rightTree);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, A, D);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        IQTree constructionTree = IQ_FACTORY.createUnaryIQTree(constructionNode, leftJoinTree);
        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, constructionTree);

        // Expected
        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1,
                IQ_FACTORY.createExtensionalDataNode(
                        TABLE2_AR2, ImmutableMap.of(0, B)));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(D, NULL));

        IQTree newConstructionTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode, newLeftJoinTree);
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newConstructionTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testAscendingSubstitutionNormalization1() {
        ExtensionalDataNode table1 = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(A));
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y, A),
                SUBSTITUTION_FACTORY.getSubstitution(X, A, Y, A));
        UnaryIQTree subTree1 = IQ_FACTORY.createUnaryIQTree(constructionNode1, table1);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Y));
        UnaryIQTree initialIQTree = IQ_FACTORY.createUnaryIQTree(constructionNode2, subTree1);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);
        IQ initialQuery = IQ_FACTORY.createIQ(projectionAtom, initialIQTree);

        // Expected
        ExtensionalDataNode newTable = createExtensionalDataNode(TABLE1_AR1, ImmutableList.of(X));
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, X));
        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(constructionNode3, newTable));

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testRightFunctionalTerm1() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE2_AR2, ImmutableList.of(C, D));

        UnaryIQTree leftChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(A)))),
                dataNode1);

        ImmutableFunctionalTerm concatFunctionalTerm = TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(
                ImmutableList.of(C, D));

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(
                                        URI_TEMPLATE_STR_1, ImmutableList.of(concatFunctionalTerm)),
                                Y, TERM_FACTORY.getRDFLiteralFunctionalTerm(D, XSD.STRING))),
                dataNode2);

        BinaryNonCommutativeIQTree initialLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftChild, rightChild);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS1_ARITY_2_PREDICATE, ImmutableList.of(X, Y));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialLeftJoinTree);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(
                initialLeftJoinTree.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                URI_TEMPLATE_STR_1, ImmutableList.of(A)),
                        Y, TERM_FACTORY.getRDFFunctionalTerm(D,
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDBIsNotNull(D),
                                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getXsdStringDatatype())))));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(A, concatFunctionalTerm)),
                dataNode1, dataNode2);

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(topConstructionNode, newLeftJoinTree));

        assertEquals(expectedIQ, initialIQ.normalizeForOptimization());
    }

    @Test
    public void testRightFunctionalTerm2() {
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2_AR2, ImmutableMap.of(0, E));
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE3_AR2, ImmutableList.of(C, D));

        ImmutableFunctionalTerm iriFunctionalTerm1 = TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(A));
        ImmutableFunctionalTerm iriFunctionalTerm2 = TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_2, ImmutableList.of(E));

        UnaryIQTree leftChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, iriFunctionalTerm1)),
                dataNode1);

        UnaryIQTree leftChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(X, iriFunctionalTerm2)),
                dataNode2);

        NaryIQTree leftUnionTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(X)),
                ImmutableList.of(leftChild1, leftChild2));

        ImmutableFunctionalTerm concatFunctionalTerm = TERM_FACTORY.getNullRejectingDBConcatFunctionalTerm(
                ImmutableList.of(C, D));

        ImmutableFunctionalTerm iriFunctionalTerm3 = TERM_FACTORY.getIRIFunctionalTerm(
                URI_TEMPLATE_STR_1, ImmutableList.of(concatFunctionalTerm));

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, iriFunctionalTerm3,
                                Y, TERM_FACTORY.getRDFFunctionalTerm(D,
                                        TERM_FACTORY.getIfElseNull(
                                                TERM_FACTORY.getDBIsNotNull(D),
                                                TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getXsdStringDatatype()))))),
                dataNode3);

        BinaryNonCommutativeIQTree initialLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftUnionTree, rightChild);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS1_ARITY_2_PREDICATE, ImmutableList.of(X, Y));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialLeftJoinTree);


        UnaryIQTree newLeftChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(F0),
                        SUBSTITUTION_FACTORY.getSubstitution(F0, iriFunctionalTerm1.getTerm(0))),
                dataNode1);

        UnaryIQTree newLeftChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(F0),
                        SUBSTITUTION_FACTORY.getSubstitution(F0, iriFunctionalTerm2.getTerm(0))),
                dataNode2);

        NaryIQTree newLeftUnionTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(F0)),
                ImmutableList.of(newLeftChild1, newLeftChild2));


        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(
                        TERM_FACTORY.getStrictEquality(F0, iriFunctionalTerm3.getTerm(0))),
                newLeftUnionTree, dataNode3);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(
                initialLeftJoinTree.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(F0),
                        Y, TERM_FACTORY.getRDFFunctionalTerm(D,
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDBIsNotNull(D),
                                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getXsdStringDatatype()))))
                        );

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(topConstructionNode, newLeftJoinTree));

        assertEquals(expectedIQ, initialIQ.normalizeForOptimization());
    }

    @Test
    public void testProvenanceVariableAndProjection1() {
        OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        RelationDefinition table1Ar2 = builder.createRelationWithStringAttributes(1, 2, true);
        RelationDefinition table2Ar2 = builder.createRelationWithStringAttributes(2, 2, true);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(table1Ar2,
                ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(table2Ar2, ImmutableList.of(C, D));

        Variable f1 = TERM_FACTORY.getVariable("f1");

        UnaryIQTree leftChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(A)))),
                dataNode1);

        UnaryIQTree rightChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(X, Y),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                X, TERM_FACTORY.getIRIFunctionalTerm(
                                        URI_TEMPLATE_STR_1, ImmutableList.of(C)),
                                Y, TERM_FACTORY.getRDFLiteralConstant("Hi", XSD.STRING))),
                dataNode2);

        BinaryNonCommutativeIQTree initialLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftChild, rightChild);

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ANS1_ARITY_2_PREDICATE, ImmutableList.of(X, Y));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, initialLeftJoinTree);

        ExtensionalDataNode newDataNode2 = IQ_FACTORY.createExtensionalDataNode(
                table2Ar2, ImmutableMap.of(0, A));

        UnaryIQTree newRightChild = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        ImmutableSet.of(A, f1),
                        SUBSTITUTION_FACTORY.getSubstitution(f1, TERM_FACTORY.getProvenanceSpecialConstant())),
                newDataNode2);

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1, newRightChild);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(
                initialLeftJoinTree.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, TERM_FACTORY.getIRIFunctionalTerm(
                                URI_TEMPLATE_STR_1, ImmutableList.of(A)),
                        Y, TERM_FACTORY.getRDFFunctionalTerm(
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDBIsNotNull(f1),
                                        TERM_FACTORY.getDBStringConstant("Hi")),
                                TERM_FACTORY.getIfElseNull(
                                        TERM_FACTORY.getDBIsNotNull(f1),
                                        TERM_FACTORY.getRDFTermTypeConstant(TYPE_FACTORY.getXsdStringDatatype())))));

        IQ expectedIQ = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(topConstructionNode, newLeftJoinTree));

        assertEquals(expectedIQ, initialIQ.normalizeForOptimization());
    }

    private static ImmutableFunctionalTerm generateIfIsNotNullElseNull(Variable rightSpecificVariable,
                                                                       ImmutableTerm conditionalValue) {
        return TERM_FACTORY.getIfElseNull(
                TERM_FACTORY.getDBIsNotNull(rightSpecificVariable),
                conditionalValue);
    }

    private static void optimizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        System.out.println("\n Original query: \n" +  initialIQ);
        System.out.println("\n Expected query: \n" +  expectedIQ);

        IQ optimizedIQ = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(initialIQ);
        System.out.println("\n Optimized query: \n" +  optimizedIQ);

        assertEquals(expectedIQ, optimizedIQ);
    }


    private static ImmutableFunctionalTerm buildSparqlDatatype(ImmutableTerm argument){
        return TERM_FACTORY.getImmutableFunctionalTerm(
                FUNCTION_SYMBOL_FACTORY.getRequiredSPARQLFunctionSymbol(SPARQL.DATATYPE, 1),
                argument);
    }
}
