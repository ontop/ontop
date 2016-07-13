package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.sun.xml.internal.xsom.XSWildcard;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.IntermediateQueryOptimizer;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.TopDownSubstitutionLiftOptimizer;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.EmptyNode;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.RemoveEmptyNodesProposalImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static junit.framework.TestCase.assertTrue;

/**
 * Test the top down substitution lift optimizer
 */
public class SubstitutionLiftTest {


    private final AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 2);
    private final AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table2", 2);
    private final AtomPredicate TABLE3_PREDICATE = new AtomPredicateImpl("table3", 2);
    private final AtomPredicate TABLE4_PREDICATE = new AtomPredicateImpl("table4", 2);
    private final AtomPredicate TABLE5_PREDICATE = new AtomPredicateImpl("table5", 2);
    private final AtomPredicate TABLE6_PREDICATE = new AtomPredicateImpl("table6", 2);

    private final AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 2);



    private final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final Variable X = DATA_FACTORY.getVariable("x");
    private final Variable Y = DATA_FACTORY.getVariable("y");
    private final Variable W = DATA_FACTORY.getVariable("w");
    private final Variable Z = DATA_FACTORY.getVariable("z");
    private final Variable A = DATA_FACTORY.getVariable("a");
    private final Variable B = DATA_FACTORY.getVariable("b");
    private final Variable C = DATA_FACTORY.getVariable("c");
    private final Variable D = DATA_FACTORY.getVariable("d");
    private final Variable E = DATA_FACTORY.getVariable("e");
    private final Variable F = DATA_FACTORY.getVariable("f");
    private final Variable G = DATA_FACTORY.getVariable("g");
    private final Variable H = DATA_FACTORY.getVariable("h");
    private final Variable I = DATA_FACTORY.getVariable("i");
    private final Variable L = DATA_FACTORY.getVariable("l");
    private final Variable M = DATA_FACTORY.getVariable("m");
    private final Variable N = DATA_FACTORY.getVariable("n");


    private URITemplatePredicate URI_PREDICATE =  new URITemplatePredicateImpl(2);
    private Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private Constant URI_TEMPLATE_STR_2 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}");

    private ExtensionalDataNode EXPECTED_DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, C));
    private ExtensionalDataNode EXPECTED_DATA_NODE_3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, C, D));
    private ExtensionalDataNode EXPECTED_DATA_NODE_4 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
    private ExtensionalDataNode EXPECTED_DATA_NODE_5 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, B, D));
    private ExtensionalDataNode EXPECTED_DATA_NODE_6 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, A, F));

    private ExtensionalDataNode DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
    private ExtensionalDataNode DATA_NODE_2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, A, E));
    private ExtensionalDataNode DATA_NODE_3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, C, D));
    private ExtensionalDataNode DATA_NODE_4 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
    private ExtensionalDataNode DATA_NODE_5 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D));
    private ExtensionalDataNode DATA_NODE_6 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, E, F));
    private ExtensionalDataNode DATA_NODE_7 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, G, H));

    InnerJoinNode joinNode;
    UnionNode unionNode;

    private final ImmutableExpression EXPRESSIONGT = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.GT, Z, Y);

    private MetadataForQueryOptimization METADATA = new MetadataForQueryOptimizationImpl(
            ImmutableMultimap.of(),
            new UriTemplateMatcher());

    public SubstitutionLiftTest() {

    }

    @Test
    public void testSimpleSubstitution() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X, Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        joinNode = new InnerJoinNodeImpl(Optional.of(EXPRESSIONGT));
        queryBuilder.addChild(rootNode, joinNode);

        //construct left side join
        ConstructionNode leftNode = new ConstructionNodeImpl(ImmutableSet.of(X,Z),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Z, generateInt(A))), Optional.empty());
        queryBuilder.addChild(joinNode, leftNode);

        //construct union
        ImmutableSet<Variable> subQueryProjectedVariables = ImmutableSet.of(X,A);
        unionNode = new UnionNodeImpl(subQueryProjectedVariables);

        queryBuilder.addChild(leftNode, unionNode);

        //construct node1 union
        ConstructionNode subQueryConstructionNode1 = new ConstructionNodeImpl(subQueryProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(B))), Optional.empty());

        queryBuilder.addChild(unionNode, subQueryConstructionNode1);
        queryBuilder.addChild(subQueryConstructionNode1, DATA_NODE_1);

        //construct node2 union
        ConstructionNode subQueryConstructionNode2 = new ConstructionNodeImpl(subQueryProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(E))), Optional.empty());
        queryBuilder.addChild(unionNode, subQueryConstructionNode2);

        queryBuilder.addChild(subQueryConstructionNode2, DATA_NODE_2);

        //construct right side join
        ConstructionNode rightNode = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(C),
                        Y, generateInt(D))),
                Optional.empty());

        queryBuilder.addChild(joinNode, rightNode);

        queryBuilder.addChild(rightNode, DATA_NODE_3);

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryOptimizer substitutionOptimizer = new TopDownSubstitutionLiftOptimizer();


        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);



        //----------------------------------------------------------------------
        // Construct expected query
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);


        DistinctVariableOnlyDataAtom expectedProjectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X, Y);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(expectedProjectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(C), Y, generateInt(D))),
                Optional.empty());

        expectedQueryBuilder.init(expectedProjectionAtom, expectedRootNode);

        //construct expected innerjoin
        ImmutableExpression expectedEspressionGT = DATA_FACTORY.getImmutableExpression(ExpressionOperation.GT, generateInt(A), generateInt(D));
        InnerJoinNode expectedJoinNode = new InnerJoinNodeImpl(Optional.of(expectedEspressionGT));
        expectedQueryBuilder.addChild(expectedRootNode, expectedJoinNode);

        expectedQueryBuilder.addChild(expectedJoinNode, EXPECTED_DATA_NODE_1);

        expectedQueryBuilder.addChild(expectedJoinNode, EXPECTED_DATA_NODE_3);

        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }


    @Test
    public void testLeftJoinSubstitution() throws EmptyQueryException {

        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X, W, Z);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        //construct innerjoin
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, leftJoinNode);

        //construct left side left join (join)
        InnerJoinNode joinNodeOnLeft = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(leftJoinNode, joinNodeOnLeft, NonCommutativeOperatorNode.ArgumentPosition.LEFT);

        //construct left side join
        ConstructionNode leftNodeJoin = new ConstructionNodeImpl(ImmutableSet.of(X, W),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), W, generateString(B))), Optional.empty());
        queryBuilder.addChild(joinNodeOnLeft, leftNodeJoin);

        queryBuilder.addChild(leftNodeJoin, DATA_NODE_4 );

        //construct right side join
        ConstructionNode rightNodeJoin = new ConstructionNodeImpl(ImmutableSet.of(W,Z),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        W, generateString(C),
                        Z, generateInt(D))),
                Optional.empty());
        queryBuilder.addChild(joinNodeOnLeft, rightNodeJoin);

        queryBuilder.addChild(rightNodeJoin, DATA_NODE_5 );

        //construct right side left join (union)
        ImmutableSet<Variable> subQueryProjectedVariables = ImmutableSet.of(X,Y);
        UnionNode unionNodeOnRight = new UnionNodeImpl(subQueryProjectedVariables);

        queryBuilder.addChild(leftJoinNode, unionNodeOnRight, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);

        //construct node1 union
        ConstructionNode subQueryConstructionNode1 = new ConstructionNodeImpl(subQueryProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(E), Y, generateInt((F)))), Optional.empty());

        queryBuilder.addChild(unionNodeOnRight, subQueryConstructionNode1);

        queryBuilder.addChild(subQueryConstructionNode1, DATA_NODE_6);

        //construct node2 union
        ConstructionNode subQueryConstructionNode2 = new ConstructionNodeImpl(subQueryProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(G), Y , generateInt(H))), Optional.empty());
        queryBuilder.addChild(unionNodeOnRight, subQueryConstructionNode2);

        queryBuilder.addChild(subQueryConstructionNode2, DATA_NODE_7);



        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);


        IntermediateQueryOptimizer substitutionOptimizer = new TopDownSubstitutionLiftOptimizer();


        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);



        //----------------------------------------------------------------------
        // Construct expected query
        //Construct unoptimized query
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), W, generateString(B), Z, generateInt(D))), Optional.empty());

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        //construct innerjoin
        LeftJoinNode expectedleftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expectedRootNode, expectedleftJoinNode);

        //construct left side left join (join)
        InnerJoinNode expectedJoinNodeOnLeft = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expectedleftJoinNode, expectedJoinNodeOnLeft, NonCommutativeOperatorNode.ArgumentPosition.LEFT);

        expectedQueryBuilder.addChild(expectedJoinNodeOnLeft, EXPECTED_DATA_NODE_4 );

        expectedQueryBuilder.addChild(expectedJoinNodeOnLeft, EXPECTED_DATA_NODE_5);


        //construct right side left join (union)

        expectedQueryBuilder.addChild(expectedleftJoinNode, EXPECTED_DATA_NODE_6, NonCommutativeOperatorNode.ArgumentPosition.RIGHT);


        //build expected query
        IntermediateQuery expectedQuery = expectedQueryBuilder.build();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));


    }

    @Test
    public void testUnionSubstitution() throws EmptyQueryException {
        //Construct unoptimized query
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X, Y);
        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables());

        queryBuilder.init(projectionAtom, rootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(rootNode, joinNode);


        //left side first join
        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(joinNode, joinNode2);

        //left side second join (unionNode 2)

        UnionNode unionNode2 =  new UnionNodeImpl(projectionAtom.getVariables());
        queryBuilder.addChild(joinNode2, unionNode2);

        //first child of unionNode2
        ConstructionNode subQuery1UnionNode2 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(A), Y, generateURI1(B))), Optional.empty());
        queryBuilder.addChild(unionNode2, subQuery1UnionNode2);

        queryBuilder.addChild(subQuery1UnionNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );

        //second child of unionNode2
        ConstructionNode subQuery2UnionNode2 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(C), Y, generateURI1(D))), Optional.empty());
        queryBuilder.addChild(unionNode2, subQuery2UnionNode2);

        queryBuilder.addChild(subQuery2UnionNode2, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, C, D)) );

        //right side second join (unionNode 3)
        UnionNode unionNode3 =  new UnionNodeImpl(projectionAtom.getVariables());
        queryBuilder.addChild(joinNode2, unionNode3);

        //first child of unionNode3
        ConstructionNode subQuery1UnionNode3 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of( Y, generateURI1(F))), Optional.empty());
        queryBuilder.addChild(unionNode3, subQuery1UnionNode3);

        queryBuilder.addChild(subQuery1UnionNode3, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, E, F)) );

        //second child of unionNode3
        ConstructionNode subQuery2UnionNode3 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y,  generateURI2(H))), Optional.empty());
        queryBuilder.addChild(unionNode3, subQuery2UnionNode3);

        queryBuilder.addChild(subQuery2UnionNode3, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE4_PREDICATE, G, H)) );

        //right side first join
        UnionNode unionNode1 =  new UnionNodeImpl(projectionAtom.getVariables());
        queryBuilder.addChild(joinNode, unionNode1);

        //first child of unionNode1
        ConstructionNode subQuery1UnionNode1 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(I))), Optional.empty());
        queryBuilder.addChild(unionNode1, subQuery1UnionNode1);

        queryBuilder.addChild(subQuery1UnionNode1, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE5_PREDICATE, I, L)) );

        //second child of unionNode1
        ConstructionNode subQuery2UnionNode1 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(M))), Optional.empty());
        queryBuilder.addChild(unionNode1, subQuery2UnionNode1);

        queryBuilder.addChild(subQuery2UnionNode1, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, M, N)) );

        //build unoptimized query
        IntermediateQuery unOptimizedQuery = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  unOptimizedQuery);

        IntermediateQueryOptimizer substitutionOptimizer = new TopDownSubstitutionLiftOptimizer();

        IntermediateQuery optimizedQuery = substitutionOptimizer.optimize(unOptimizedQuery);

        System.out.println("\nAfter optimization: \n" +  optimizedQuery);

        //----------------------------------------------------------------------
        //Construct expected query
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode expectedRootNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Y, generateURI1(A))), Optional.empty());

        expectedQueryBuilder.init(projectionAtom, expectedRootNode);

        //constract union Node
        UnionNode expectedUnionNode =  new UnionNodeImpl(projectionAtom.getVariables());

        expectedQueryBuilder.addChild(expectedRootNode, expectedUnionNode );

        ConstructionNode expectedSubQuery1UnionNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(B))), Optional.empty());
        expectedQueryBuilder.addChild(expectedUnionNode, expectedSubQuery1UnionNode);

        InnerJoinNode joinNode11 = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expectedSubQuery1UnionNode, joinNode11);

        InnerJoinNode joinNode12 = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(joinNode11, joinNode12);

        expectedQueryBuilder.addChild(joinNode11, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE5_PREDICATE, A, B)) );

        expectedQueryBuilder.addChild(joinNode12, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B)) );
        expectedQueryBuilder.addChild(joinNode12, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, A, B)) );

        ConstructionNode expectedSubQuery2UnionNode = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI2(C))), Optional.empty());
        expectedQueryBuilder.addChild(expectedUnionNode, expectedSubQuery2UnionNode);

        InnerJoinNode joinNode21 = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(expectedSubQuery2UnionNode, joinNode21);

        InnerJoinNode joinNode22 = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(joinNode21, joinNode22);

        expectedQueryBuilder.addChild(joinNode21, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE6_PREDICATE, C, B)) );

        expectedQueryBuilder.addChild(joinNode22, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, C, B)) );
        expectedQueryBuilder.addChild(joinNode22, new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, C, B)) );




        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\nExpected  query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));


    }

    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

    private ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_2, argument);
    }

    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(Predicate.COL_TYPE.INTEGER),
                argument);
    }

    private ImmutableFunctionalTerm generateString(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(Predicate.COL_TYPE.STRING),
                argument);
    }


    public void testQueryWithEmpty() throws EmptyQueryException {

        //Construct expected intermediate query
        IntermediateQueryBuilder intermediateQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom intermediateProjectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X, Y);

        ConstructionNode intermediateRootNode = new ConstructionNodeImpl(intermediateProjectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(C), Y, generateInt(D))),
                Optional.empty());

        intermediateQueryBuilder.init(intermediateProjectionAtom, intermediateRootNode);

        //construct innerjoin
        ImmutableExpression intermediateEspressionGT = DATA_FACTORY.getImmutableExpression(ExpressionOperation.GT, generateInt(A), generateInt(D));
        InnerJoinNode intermediateJoinNode = new InnerJoinNodeImpl(Optional.of(intermediateEspressionGT));
        intermediateQueryBuilder.addChild(intermediateRootNode, intermediateJoinNode);

        //construct left side join (union)
        ImmutableSet<Variable> intermediateProjectedVariables = ImmutableSet.of(X,A);
        UnionNode intermediateUnionNode = new UnionNodeImpl(intermediateProjectedVariables);

        intermediateQueryBuilder.addChild(intermediateJoinNode, intermediateUnionNode);

        intermediateQueryBuilder.addChild(intermediateUnionNode,  EXPECTED_DATA_NODE_1);

        EmptyNode emptyNode = new EmptyNodeImpl(intermediateProjectedVariables);

        intermediateQueryBuilder.addChild(intermediateUnionNode, emptyNode);

        intermediateQueryBuilder.addChild(intermediateJoinNode, EXPECTED_DATA_NODE_3);

        //build unoptimized query
        IntermediateQuery intermediateQuery = intermediateQueryBuilder.build();
        System.out.println("\nIntermediate result: \n" +  intermediateQuery);

        intermediateQuery.applyProposal(new RemoveEmptyNodesProposalImpl<>(intermediateRootNode));
        System.out.println("\nAfter optimization: \n" +  intermediateQuery);
    }

}
