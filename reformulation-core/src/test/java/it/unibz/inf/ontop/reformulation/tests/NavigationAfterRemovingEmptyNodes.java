package it.unibz.inf.ontop.reformulation.tests;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.RemoveEmptyNodesProposalImpl;
import org.junit.Test;

import java.util.Optional;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;

public class NavigationAfterRemovingEmptyNodes {

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
    public void testNextSiblingInitiallyFar() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, A);

        ConstructionNode initialRootNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        UnionNode unionNode = new UnionNodeImpl(projectionAtom.getVariables());
        initialQueryBuilder.addChild(initialRootNode, unionNode);
        initialQueryBuilder.addChild(unionNode, DATA_NODE_1);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        initialQueryBuilder.addChild(unionNode, joinNode);

        FilterNode filterNode = new FilterNodeImpl(DATA_FACTORY.getImmutableExpression(ExpressionOperation.GT, A,
                DATA_FACTORY.getConstantLiteral("2")));
        initialQueryBuilder.addChild(joinNode, filterNode);
        initialQueryBuilder.addChild(filterNode, new EmptyNodeImpl(ImmutableSet.of(A)));
        initialQueryBuilder.addChild(joinNode, buildExtensionalDataNode(TABLE2_PREDICATE, A, C));

        ExtensionalDataNode rightMostNode = buildExtensionalDataNode(TABLE3_PREDICATE, A, D);
        initialQueryBuilder.addChild(unionNode, rightMostNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        RemoveEmptyNodesProposalImpl<FilterNode> proposal = new RemoveEmptyNodesProposalImpl<>(filterNode);

        System.out.println("Initial query: \n" + initialQuery);

        NodeCentricOptimizationResults<FilterNode> results = initialQuery.applyProposal(proposal,
                REQUIRE_USING_IN_PLACE_EXECUTOR);

        System.out.println("Optimized query: \n" + initialQuery);

        assertFalse(results.getNewNodeOrReplacingChild().isPresent());
        assertTrue(results.getOptionalNextSibling().isPresent());
        assertTrue(results.getOptionalNextSibling().get().isSyntacticallyEquivalentTo(rightMostNode));
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
