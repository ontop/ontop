package it.unibz.inf.ontop.iq.executor;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.RemoveEmptyNodeProposal;
import it.unibz.inf.ontop.iq.proposal.impl.InnerJoinOptimizationProposalImpl;
import it.unibz.inf.ontop.iq.proposal.impl.RemoveEmptyNodeProposalImpl;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;

public class NavigationAfterRemovingEmptyNodeTest {

    private static final AtomPredicate TABLE1_PREDICATE = ATOM_FACTORY.getAtomPredicate("table1", 2);
    private static final AtomPredicate TABLE2_PREDICATE = ATOM_FACTORY.getAtomPredicate("table2", 2);
    private static final AtomPredicate TABLE3_PREDICATE = ATOM_FACTORY.getAtomPredicate("table3", 2);
    private static final AtomPredicate TABLE4_PREDICATE = ATOM_FACTORY.getAtomPredicate("table4", 2);
    private static final AtomPredicate TABLE5_PREDICATE = ATOM_FACTORY.getAtomPredicate("table5", 2);
    private static final AtomPredicate TABLE6_PREDICATE = ATOM_FACTORY.getAtomPredicate("table6", 2);

    private static final AtomPredicate ANS1_ARITY_1_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 1);
    private static final AtomPredicate ANS1_ARITY_2_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 2);

    private static final Variable X = TERM_FACTORY.getVariable("x");
    private static final Variable Y = TERM_FACTORY.getVariable("y");
    private static final Variable W = TERM_FACTORY.getVariable("w");
    private static final Variable Z = TERM_FACTORY.getVariable("z");
    private static final Variable A = TERM_FACTORY.getVariable("a");
    private static final Variable B = TERM_FACTORY.getVariable("b");
    private static final Variable C = TERM_FACTORY.getVariable("c");
    private static final Variable D = TERM_FACTORY.getVariable("d");
    private static final Variable E = TERM_FACTORY.getVariable("e");
    private static final Variable F = TERM_FACTORY.getVariable("f");
    private static final Variable G = TERM_FACTORY.getVariable("g");
    private static final Variable H = TERM_FACTORY.getVariable("h");
    private static final Variable I = TERM_FACTORY.getVariable("i");
    private static final Variable L = TERM_FACTORY.getVariable("l");
    private static final Variable M = TERM_FACTORY.getVariable("m");
    private static final Variable N = TERM_FACTORY.getVariable("n");


    private static final URITemplatePredicate URI1_PREDICATE =  TERM_FACTORY.getURITemplatePredicate(2);
    private static final URITemplatePredicate URI2_PREDICATE =  TERM_FACTORY.getURITemplatePredicate(3);
    private static final Constant URI_TEMPLATE_STR_1 =  TERM_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static final Constant URI_TEMPLATE_STR_2 =  TERM_FACTORY.getConstantLiteral("http://example.org/ds2/{}/{}");

    private static final ExtensionalDataNode DATA_NODE_1 = buildExtensionalDataNode(TABLE1_PREDICATE, A, B);
    private static final ExtensionalDataNode DATA_NODE_2 = buildExtensionalDataNode(TABLE2_PREDICATE, A, E);
    private static final ExtensionalDataNode DATA_NODE_3 = buildExtensionalDataNode(TABLE3_PREDICATE, C, D);
    private static final ExtensionalDataNode DATA_NODE_4 = buildExtensionalDataNode(TABLE4_PREDICATE, A, B);
    private static final ExtensionalDataNode DATA_NODE_5 = buildExtensionalDataNode(TABLE2_PREDICATE, C, E);
    private static final ExtensionalDataNode DATA_NODE_6 = buildExtensionalDataNode(TABLE3_PREDICATE, E, F);
    private static final ExtensionalDataNode DATA_NODE_7 = buildExtensionalDataNode(TABLE4_PREDICATE, G, H);

    @Test
    public void testNextSiblingInitiallyFar() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, A);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());
        initialQueryBuilder.addChild(initialRootNode, unionNode);
        initialQueryBuilder.addChild(unionNode, DATA_NODE_1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        initialQueryBuilder.addChild(unionNode, joinNode);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getImmutableExpression(ExpressionOperation.GT, A,
                TERM_FACTORY.getConstantLiteral("2")));
        initialQueryBuilder.addChild(joinNode, filterNode);
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A));
        initialQueryBuilder.addChild(filterNode, emptyNode);
        initialQueryBuilder.addChild(joinNode, buildExtensionalDataNode(TABLE2_PREDICATE, A, C));

        ExtensionalDataNode rightMostNode = buildExtensionalDataNode(TABLE3_PREDICATE, A, D);
        initialQueryBuilder.addChild(unionNode, rightMostNode);

        IntermediateQuery initialQuery = initialQueryBuilder.build();

        RemoveEmptyNodeProposal proposal = new RemoveEmptyNodeProposalImpl(emptyNode, false);

        System.out.println("Initial query: \n" + initialQuery);

        NodeCentricOptimizationResults<EmptyNode> results = initialQuery.applyProposal(proposal);

        System.out.println("Optimized query: \n" + initialQuery);

        assertFalse(results.getNewNodeOrReplacingChild().isPresent());
        assertTrue(results.getOptionalNextSibling().isPresent());
        assertTrue(results.getOptionalNextSibling().get().isSyntacticallyEquivalentTo(rightMostNode));
    }

    @Test
    public void testInsatisfiedJoinCondition() throws EmptyQueryException {
        IntermediateQueryBuilder initialQueryBuilder = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_1_PREDICATE, A);

        ConstructionNode initialRootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        initialQueryBuilder.init(projectionAtom, initialRootNode);

        UnionNode unionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());
        initialQueryBuilder.addChild(initialRootNode, unionNode);

        InnerJoinNode unsatisfiedJoinNode = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getImmutableExpression(
                ExpressionOperation.EQ,
                TERM_FACTORY.getConstantLiteral("2", XSD.INTEGER),
                TERM_FACTORY.getConstantLiteral("3", XSD.INTEGER)));
        initialQueryBuilder.addChild(unionNode, unsatisfiedJoinNode);
        initialQueryBuilder.addChild(unsatisfiedJoinNode, DATA_NODE_1);
        initialQueryBuilder.addChild(unsatisfiedJoinNode, DATA_NODE_2);

        initialQueryBuilder.addChild(unionNode, DATA_NODE_4);

        IntermediateQuery query = initialQueryBuilder.build();

        System.out.println("Initial query: \n" + query);

        InnerJoinOptimizationProposal proposal = new InnerJoinOptimizationProposalImpl(unsatisfiedJoinNode);
        NodeCentricOptimizationResults<InnerJoinNode> results = query.applyProposal(proposal);

        System.out.println("Optimized query: \n" + query);

        assertFalse(results.getNewNodeOrReplacingChild().isPresent());
        assertTrue(results.getOptionalNextSibling().isPresent());
        assertTrue(results.getOptionalNextSibling().get().isSyntacticallyEquivalentTo(DATA_NODE_4));
    }

    private static ImmutableFunctionalTerm generateURI1(ImmutableTerm argument) {
        return TERM_FACTORY.getImmutableFunctionalTerm(URI1_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

    private static ImmutableFunctionalTerm generateURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return TERM_FACTORY.getImmutableFunctionalTerm(URI2_PREDICATE, URI_TEMPLATE_STR_2, argument1, argument2);
    }

    private static ExtensionalDataNode buildExtensionalDataNode(AtomPredicate predicate, VariableOrGroundTerm... arguments) {
        return IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(predicate, arguments));
    }

}
