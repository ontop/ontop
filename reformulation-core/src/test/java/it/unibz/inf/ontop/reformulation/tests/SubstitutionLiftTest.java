package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.EmptyNode;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import jdk.nashorn.internal.ir.*;
import org.junit.Test;

import java.util.Optional;

/**
 * Test the top down substitution lift optimizer
 */
public class SubstitutionLiftTest {


    private final AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 2);
    private final AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table2", 2);
    private final AtomPredicate TABLE3_PREDICATE = new AtomPredicateImpl("table3", 2);
    private final AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 2);



    private final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final Variable X = DATA_FACTORY.getVariable("x");
    private final Variable Y = DATA_FACTORY.getVariable("y");
    private final Variable Z = DATA_FACTORY.getVariable("z");


    private final Variable A = DATA_FACTORY.getVariable("a");
    private final Variable B = DATA_FACTORY.getVariable("b");
    private final Variable C = DATA_FACTORY.getVariable("c");
    private final Variable D = DATA_FACTORY.getVariable("d");
    private final Variable E = DATA_FACTORY.getVariable("e");

    private URITemplatePredicate URI_PREDICATE =  new URITemplatePredicateImpl(2);
    private Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private Constant URI_TEMPLATE_STR_2 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}");

    private ExtensionalDataNode EXPECTED_DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, C));
    private ExtensionalDataNode EXPECTED_DATA_NODE_3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, C, D));

    private ExtensionalDataNode DATA_NODE_1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, A, B));
    private ExtensionalDataNode DATA_NODE_2 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, A, E));
    private ExtensionalDataNode DATA_NODE_3 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE3_PREDICATE, C, D));

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

        //----------------------------------------------------------------------
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

        //construct left side join
        ConstructionNode intermediateLeftNode = new ConstructionNodeImpl(ImmutableSet.of(X,Z),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(Z, generateInt(A))), Optional.empty());
        intermediateQueryBuilder.addChild(intermediateJoinNode, intermediateLeftNode);

        //construct union
        ImmutableSet<Variable> intermediateProjectedVariables = ImmutableSet.of(X,A);
        UnionNode intermediateUnionNode = new UnionNodeImpl(intermediateProjectedVariables);

        intermediateQueryBuilder.addChild(intermediateLeftNode, intermediateUnionNode);

        //construct node1 union
        ConstructionNode intermediateConstructionNode1 = new ConstructionNodeImpl(intermediateProjectedVariables,
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(X, generateURI1(C))), Optional.empty());

        intermediateQueryBuilder.addChild(intermediateUnionNode, intermediateConstructionNode1);
        intermediateQueryBuilder.addChild(intermediateConstructionNode1, EXPECTED_DATA_NODE_1);

        EmptyNode emptyNode = new EmptyNodeImpl(intermediateProjectedVariables);

        intermediateQueryBuilder.addChild(intermediateUnionNode, emptyNode);

        //construct right side join
        ConstructionNode intermediateRightNode = new ConstructionNodeImpl(ImmutableSet.of(X,Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(C),
                        Y, generateInt(D))),
                Optional.empty());

        intermediateQueryBuilder.addChild(intermediateJoinNode, intermediateRightNode);

        intermediateQueryBuilder.addChild(intermediateRightNode, EXPECTED_DATA_NODE_3);

        //build unoptimized query
        IntermediateQuery intermediateQuery = intermediateQueryBuilder.build();
        System.out.println("\nIntermediate result: \n" +  intermediateQuery);


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
}
