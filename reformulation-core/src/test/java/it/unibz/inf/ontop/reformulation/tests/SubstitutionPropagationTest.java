package it.unibz.inf.ontop.reformulation.tests;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.SubstitutionPropagationProposalImpl;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker.areEquivalent;
import static org.junit.Assert.assertTrue;

/**
 * Tests the substitution propagation
 */
public class SubstitutionPropagationTest {


    private static final AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("table1", 2);
    private static final AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("table2", 2);
    private static final AtomPredicate TABLE3_PREDICATE = new AtomPredicateImpl("table3", 2);
    private static final AtomPredicate TABLE4_PREDICATE = new AtomPredicateImpl("table4", 2);
    private static final AtomPredicate TABLE5_PREDICATE = new AtomPredicateImpl("table5", 2);
    private static final AtomPredicate TABLE6_PREDICATE = new AtomPredicateImpl("table6", 2);

    private static final AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 2);



    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private static final Variable X = DATA_FACTORY.getVariable("x");
    private static final Variable Y = DATA_FACTORY.getVariable("y");
    private static final Variable W = DATA_FACTORY.getVariable("w");
    private static final Variable Z = DATA_FACTORY.getVariable("z");
    private static final Variable A = DATA_FACTORY.getVariable("a");
    private static final Variable B = DATA_FACTORY.getVariable("b");
    private static final Variable C = DATA_FACTORY.getVariable("c");
    private static final Variable D = DATA_FACTORY.getVariable("d");
    private static final Variable E = DATA_FACTORY.getVariable("e");
    private static final Variable F = DATA_FACTORY.getVariable("f");
    private static final Variable G = DATA_FACTORY.getVariable("g");
    private static final Variable H = DATA_FACTORY.getVariable("h");
    private static final Variable I = DATA_FACTORY.getVariable("i");
    private static final Variable L = DATA_FACTORY.getVariable("l");
    private static final Variable M = DATA_FACTORY.getVariable("m");
    private static final Variable N = DATA_FACTORY.getVariable("n");


    private static final URITemplatePredicate URI1_PREDICATE =  new URITemplatePredicateImpl(2);
    private static final URITemplatePredicate URI2_PREDICATE =  new URITemplatePredicateImpl(3);
    private static final Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static final Constant URI_TEMPLATE_STR_2 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}/{}");

    private static final ExtensionalDataNode DATA_NODE_1 = buildExtensionalDataNode(TABLE1_PREDICATE, A, B);
    private static final ExtensionalDataNode DATA_NODE_2 = buildExtensionalDataNode(TABLE2_PREDICATE, A, E);
    private static final ExtensionalDataNode DATA_NODE_3 = buildExtensionalDataNode(TABLE3_PREDICATE, C, D);
    private static final ExtensionalDataNode DATA_NODE_4 = buildExtensionalDataNode(TABLE1_PREDICATE, A, B);
    private static final ExtensionalDataNode DATA_NODE_5 = buildExtensionalDataNode(TABLE2_PREDICATE, C, E);
    private static final ExtensionalDataNode DATA_NODE_6 = buildExtensionalDataNode(TABLE3_PREDICATE, E, F);
    private static final ExtensionalDataNode DATA_NODE_7 = buildExtensionalDataNode(TABLE4_PREDICATE, G, H);

    private static final MetadataForQueryOptimization METADATA = new EmptyMetadataForQueryOptimization();
    private static final boolean REQUIRE_USING_IN_PLACE_EXECUTOR = true;


    @Test
    public void testURI1PropOtherBranch() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X, Y);

        ConstructionNode initialRootNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A),
                        Y, generateURI1(B))),
                Optional.empty());
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        ConstructionNode rightConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(C))),
                Optional.empty());
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getDirectBindingSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(joinNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, D));

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);

    }

    @Test
    public void testURI2PropOtherBranch() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X, Y);

        ConstructionNode initialRootNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(A, B),
                        Y, generateURI1(B))),
                Optional.empty());
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        ConstructionNode rightConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(C, D))),
                Optional.empty());
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);
        initialQueryBuilder.addChild(rightConstructionNode, DATA_NODE_3);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getDirectBindingSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        expectedQueryBuilder.addChild(joinNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, B));

        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testURI1PropOtherBranchWithUnion1() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X, Y);

        ConstructionNode initialRootNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A),
                        Y, generateURI1(B))),
                Optional.empty());
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        ConstructionNode rightConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(C))),
                Optional.empty());
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        UnionNode initialUnionNode = new UnionNodeImpl(ImmutableSet.of(C));
        initialQueryBuilder.addChild(rightConstructionNode, initialUnionNode);

        initialQueryBuilder.addChild(initialUnionNode, DATA_NODE_3);
        initialQueryBuilder.addChild(initialUnionNode, DATA_NODE_5);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getDirectBindingSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = new UnionNodeImpl(ImmutableSet.of(A));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, D));
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, A, E));
        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testURI1PropOtherBranchWithUnion2() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X, Y);

        ConstructionNode initialRootNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(A),
                        Y, generateURI1(B))),
                Optional.empty());
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        UnionNode initialUnionNode = new UnionNodeImpl(ImmutableSet.of(X));
        initialQueryBuilder.addChild(joinNode, initialUnionNode);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(C))),
                Optional.empty());
        initialQueryBuilder.addChild(initialUnionNode, constructionNode2);
        initialQueryBuilder.addChild(constructionNode2, DATA_NODE_3);

        ConstructionNode constructionNode3 = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI1(E))),
                Optional.empty());
        initialQueryBuilder.addChild(initialUnionNode, constructionNode3);
        initialQueryBuilder.addChild(constructionNode3, buildExtensionalDataNode(TABLE2_PREDICATE, E, F));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getDirectBindingSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = new UnionNodeImpl(ImmutableSet.of(A));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, D));
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, A, F));
        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testURI2PropOtherBranchWithUnion1() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X, Y);

        ConstructionNode initialRootNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(A, B),
                        Y, generateURI1(B))),
                Optional.empty());
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        ConstructionNode rightConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(C, D))),
                Optional.empty());
        initialQueryBuilder.addChild(joinNode, rightConstructionNode);

        UnionNode initialUnionNode = new UnionNodeImpl(ImmutableSet.of(C, D));
        initialQueryBuilder.addChild(rightConstructionNode, initialUnionNode);

        initialQueryBuilder.addChild(initialUnionNode, DATA_NODE_3);
        initialQueryBuilder.addChild(initialUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, C, D));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getDirectBindingSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = new UnionNodeImpl(ImmutableSet.of(A, B));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, B));
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, A, B));
        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }

    @Test
    public void testURI2PropOtherBranchWithUnion2() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X, Y);

        ConstructionNode initialRootNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        initialQueryBuilder.addChild(initialRootNode, joinNode);

        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(ImmutableSet.of(X, Y),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(A, B),
                        Y, generateURI1(B))),
                Optional.empty());
        initialQueryBuilder.addChild(joinNode, leftConstructionNode);
        initialQueryBuilder.addChild(leftConstructionNode, DATA_NODE_1);


        UnionNode initialUnionNode = new UnionNodeImpl(ImmutableSet.of(X));
        initialQueryBuilder.addChild(joinNode, initialUnionNode);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(C, D))),
                Optional.empty());
        initialQueryBuilder.addChild(initialUnionNode, constructionNode2);
        initialQueryBuilder.addChild(constructionNode2, DATA_NODE_3);

        ConstructionNode constructionNode3 = new ConstructionNodeImpl(ImmutableSet.of(X),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(
                        X, generateURI2(E, F))),
                Optional.empty());
        initialQueryBuilder.addChild(initialUnionNode, constructionNode3);
        initialQueryBuilder.addChild(constructionNode3, buildExtensionalDataNode(TABLE2_PREDICATE, E, F));

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        SubstitutionPropagationProposal<ConstructionNode> propagationProposal =
                new SubstitutionPropagationProposalImpl<>(leftConstructionNode, leftConstructionNode.getDirectBindingSubstitution());

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ConstructionNode newRootNode = leftConstructionNode;
        expectedQueryBuilder.init(projectionAtom, newRootNode);
        expectedQueryBuilder.addChild(newRootNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, DATA_NODE_1);
        UnionNode newUnionNode = new UnionNodeImpl(ImmutableSet.of(A, B));
        expectedQueryBuilder.addChild(joinNode, newUnionNode);
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE3_PREDICATE, A, B));
        expectedQueryBuilder.addChild(newUnionNode, buildExtensionalDataNode(TABLE2_PREDICATE, A, B));
        propagateAndCompare(initialQuery, expectedQueryBuilder.build(), propagationProposal);
    }



    private static void propagateAndCompare(IntermediateQuery query, IntermediateQuery expectedQuery,
                                            SubstitutionPropagationProposal propagationProposal)
            throws EmptyQueryException {

        System.out.println("\n Original query: \n" +  query);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        // Updates the query (in-place optimization)
        query.applyProposal(propagationProposal, REQUIRE_USING_IN_PLACE_EXECUTOR);

        System.out.println("\n Optimized query: \n" +  query);

        assertTrue(areEquivalent(query, expectedQuery));

    }


    private static ImmutableFunctionalTerm generateURI1(ImmutableTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI1_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

    private static ImmutableFunctionalTerm generateURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI2_PREDICATE, URI_TEMPLATE_STR_2, argument1, argument2);
    }
    
    private static ExtensionalDataNode buildExtensionalDataNode(AtomPredicate predicate, VariableOrGroundTerm ... arguments) {
        return new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(predicate, arguments));
    }

}
