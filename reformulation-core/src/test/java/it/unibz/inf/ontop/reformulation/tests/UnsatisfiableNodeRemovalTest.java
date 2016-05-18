package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.RemoveUnsatisfiableNodesProposalImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker.areEquivalent;
import static org.junit.Assert.assertTrue;

public class UnsatisfiableNodeRemovalTest {

    private static MetadataForQueryOptimization METADATA = new MetadataForQueryOptimizationImpl(
            ImmutableMultimap.of(),
            new UriTemplateMatcher());
    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private static AtomPredicate ANS1_PREDICATE = new AtomPredicateImpl("ans1", 1);
    private static Variable X = DATA_FACTORY.getVariable("x");
    private static Variable A = DATA_FACTORY.getVariable("a");
    private static Variable B = DATA_FACTORY.getVariable("b");
    private static DistinctVariableOnlyDataAtom PROJECTION_ATOM = DATA_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS1_PREDICATE, ImmutableList.of(X));
    private static URITemplatePredicate URI_PREDICATE =  new URITemplatePredicateImpl(2);
    private static Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private static AtomPredicate TABLE_1 = new AtomPredicateImpl("table1", 1);
    private static boolean REQUIRE_USING_IN_PLACE_EXECUTOR = true;

    @Test
    public void testUnionRemoval() throws EmptyQueryException {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        ImmutableSet<Variable> projectedVariables = PROJECTION_ATOM.getVariables();

        ConstructionNode rootNode = new ConstructionNodeImpl(projectedVariables);
        queryBuilder.init(PROJECTION_ATOM, rootNode);

        UnionNode unionNode = new UnionNodeImpl();
        queryBuilder.addChild(rootNode, unionNode);

        ImmutableSubstitutionImpl<ImmutableTerm> leftBindings = new ImmutableSubstitutionImpl<>(
                ImmutableMap.of(X, generateURI1(A)));
        ConstructionNode leftConstructionNode = new ConstructionNodeImpl(projectedVariables, leftBindings,
                Optional.empty());
        queryBuilder.addChild(unionNode, leftConstructionNode);

        ExtensionalDataNode dataNode1 = new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE_1, A));
        queryBuilder.addChild(leftConstructionNode, dataNode1);

        UnsatisfiableNode unsatisfiableNode = new UnsatisfiableNodeImpl();
        queryBuilder.addChild(unionNode, unsatisfiableNode);

        IntermediateQuery query = queryBuilder.build();

        // Updates the query (in-place optimization)
        query.applyProposal(new RemoveUnsatisfiableNodesProposalImpl(), REQUIRE_USING_IN_PLACE_EXECUTOR);


        /**
         * Expected query
         */
        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        expectedQueryBuilder.init(PROJECTION_ATOM, leftConstructionNode);
        expectedQueryBuilder.addChild(leftConstructionNode, dataNode1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        assertTrue(areEquivalent(query, expectedQuery));
    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

}
