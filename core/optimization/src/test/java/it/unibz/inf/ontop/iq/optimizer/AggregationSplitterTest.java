package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.OptimizationTestingTools;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.AggregationNode;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

public class AggregationSplitterTest {

    public final static RelationDefinition T1_AR3, T2_AR3, T3_AR3;
    private static final ImmutableList<Template.Component> TEMPLATE_1, TEMPLATE_2, TEMPLATE_3, TEMPLATE_4, TEMPLATE_5;

    static {
        OptimizationTestingTools.OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        T1_AR3 = builder.createRelationWithIntAttributes(1, 3, true);
        T2_AR3 = builder.createRelationWithIntAttributes(2, 3, true);
        T3_AR3 = builder.createRelationWithIntAttributes(3, 3, true);


        TEMPLATE_1 = Template.builder()
                .addSeparator("https://ex.org/template1/")
                .addColumn()
                .build();

        TEMPLATE_2 = Template.builder()
                .addSeparator("https://ex.org/template2/")
                .addColumn()
                .build();

        TEMPLATE_3 = Template.builder()
                .addSeparator("https://ex.org/template")
                .addColumn()
                .addSeparator("/")
                .addColumn()
                .build();

        TEMPLATE_4 = Template.builder()
                .addSeparator("https://ex.org/template1")
                .addColumn()
                .addSeparator("/")
                .addColumn()
                .build();

        TEMPLATE_5 = Template.builder()
                .addSeparator("https://ex.org/template1/")
                .addColumn()
                .addSeparator("2")
                .build();
    }

    @Test
    public void testLiftAggregation1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        ImmutableSubstitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, ONE);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        ImmutableSubstitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TWO);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                xSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode, unionTree));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        AggregationNode newAggregationNode1 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution1),
                IQ_FACTORY.createUnaryIQTree(newAggregationNode1, dataNode1)
                        .applyFreshRenamingToAllVariables(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(
                                ImmutableMap.of(A, AF0))));

        AggregationNode newAggregationNode2 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution2),
                IQ_FACTORY.createUnaryIQTree(newAggregationNode2, dataNode2)
                        .applyFreshRenamingToAllVariables(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(
                                ImmutableMap.of(A, AF1))));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newUnionNode,
                ImmutableList.of(newChild1, newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testLiftAggregation2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A, 1, C));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        ImmutableSubstitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        ImmutableSubstitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_2, ImmutableList.of(C)).getTerm(0));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode, unionTree));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        UnaryIQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution1),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createAggregationNode(ImmutableSet.of(B), SUBSTITUTION_FACTORY.getSubstitution(
                                        Y, TERM_FACTORY.getDBCount(A, false))),
                        dataNode1).applyFreshRenamingToAllVariables(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(
                        ImmutableMap.of(A, AF0))));

        UnaryIQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution2),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createAggregationNode(ImmutableSet.of(C), SUBSTITUTION_FACTORY.getSubstitution(
                                Y, TERM_FACTORY.getDBCount(A, false))),
                        dataNode2).applyFreshRenamingToAllVariables(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(
                        ImmutableMap.of(A, AF1))));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newUnionNode,
                ImmutableList.of(
                        newChild1,
                        newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Ignore("TODO: fix a problem with IRI template comparison")
    @Test
    public void testLiftAggregation3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A, 1, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(T3_AR3, ImmutableMap.of(0, A, 1, D, 2, E));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        ImmutableSubstitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        ImmutableSubstitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_2, ImmutableList.of(C)).getTerm(0));
        ImmutableSubstitution<ImmutableTerm> xSubstitution3 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_4, ImmutableList.of(D, E)).getTerm(0));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution3);

        UnaryIQTree child3 = IQ_FACTORY.createUnaryIQTree(
                constructionNode3,
                dataNode3);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2,
                        child3));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode, unionTree));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                aggregationNode,
                IQ_FACTORY.createNaryIQTree(
                        unionNode,
                        ImmutableList.of(
                                child1,
                                child3))).applyFreshRenamingToAllVariables(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(
                ImmutableMap.of(A, AF0)));

        UnaryIQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution2),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createAggregationNode(ImmutableSet.of(C), SUBSTITUTION_FACTORY.getSubstitution(
                                Y, TERM_FACTORY.getDBCount(A, false))),
                        dataNode2).applyFreshRenamingToAllVariables(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(
                        ImmutableMap.of(A, AF1))));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newUnionNode,
                ImmutableList.of(
                        newChild1,
                        newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testLiftAggregation4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A, 1, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(T3_AR3, ImmutableMap.of(0, A, 1, D));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        ImmutableSubstitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        ImmutableSubstitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_2, ImmutableList.of(C)).getTerm(0));
        ImmutableSubstitution<ImmutableTerm> xSubstitution3 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_5, ImmutableList.of(D)).getTerm(0));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution3);

        UnaryIQTree child3 = IQ_FACTORY.createUnaryIQTree(
                constructionNode3,
                dataNode3);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2,
                        child3));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode, unionTree));

        UnionNode newTopUnionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                        aggregationNode,
                        IQ_FACTORY.createNaryIQTree(
                                unionNode,
                                ImmutableList.of(
                                        child1,
                                        child3))).applyFreshRenamingToAllVariables(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(
                        ImmutableMap.of(A, AF0)));

        UnaryIQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newTopUnionNode.getVariables(), xSubstitution2),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createAggregationNode(ImmutableSet.of(C), SUBSTITUTION_FACTORY.getSubstitution(
                                Y, TERM_FACTORY.getDBCount(A, false))),
                        dataNode2).applyFreshRenamingToAllVariables(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(
                        ImmutableMap.of(A, AF1))));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newTopUnionNode,
                ImmutableList.of(
                        newChild1,
                        newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testLiftAggregation5() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        ImmutableSubstitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, ONE);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        ImmutableSubstitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TWO);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                xSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2,
                        // Duplicate
                        child1));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode, unionTree));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        AggregationNode newAggregationNode1 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution1),
                IQ_FACTORY.createUnaryIQTree(newAggregationNode1,
                                IQ_FACTORY.createNaryIQTree(
                                        IQ_FACTORY.createUnionNode(ImmutableSet.of(A)),
                                        ImmutableList.of(dataNode1, dataNode1)))
                        .applyFreshRenamingToAllVariables(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(
                                ImmutableMap.of(A, AF0))));

        AggregationNode newAggregationNode2 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution2),
                IQ_FACTORY.createUnaryIQTree(newAggregationNode2, dataNode2)
                        .applyFreshRenamingToAllVariables(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(
                                ImmutableMap.of(A, AF1))));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newUnionNode,
                ImmutableList.of(newChild1, newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testLiftAggregation6() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        ImmutableSubstitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, NULL);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        ImmutableSubstitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TWO);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                xSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2,
                        // Duplicate
                        child1));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode, unionTree));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        AggregationNode newAggregationNode1 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution1),
                IQ_FACTORY.createUnaryIQTree(newAggregationNode1,
                                IQ_FACTORY.createNaryIQTree(
                                        IQ_FACTORY.createUnionNode(ImmutableSet.of(A)),
                                        ImmutableList.of(dataNode1, dataNode1)))
                        .applyFreshRenamingToAllVariables(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(
                                ImmutableMap.of(A, AF0))));

        AggregationNode newAggregationNode2 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution2),
                IQ_FACTORY.createUnaryIQTree(newAggregationNode2, dataNode2)
                        .applyFreshRenamingToAllVariables(SUBSTITUTION_FACTORY.getInjectiveVar2VarSubstitution(
                                ImmutableMap.of(A, AF1))));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newUnionNode,
                ImmutableList.of(newChild1, newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testNoLiftAggregation1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A, 1, C, 2, D));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        ImmutableSubstitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        ImmutableSubstitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_3, ImmutableList.of(C, D)).getTerm(0));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode, unionTree));

        optimizeAndCompare(initialQuery, initialQuery);
    }

    @Test
    public void testNoLiftAggregation2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A, 1, E));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(T3_AR3, ImmutableMap.of(0, A, 1, C, 2, D));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        ImmutableSubstitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        ImmutableSubstitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_2, ImmutableList.of(E)).getTerm(0));
        ImmutableSubstitution<ImmutableTerm> xSubstitution3 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_3, ImmutableList.of(C, D)).getTerm(0));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution3);

        UnaryIQTree child3 = IQ_FACTORY.createUnaryIQTree(
                constructionNode3,
                dataNode3);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2,
                        child3));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode, unionTree));

        optimizeAndCompare(initialQuery, initialQuery);
    }

    @Test
    public void testNoLiftAggregation3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A, 1, E));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(T3_AR3, ImmutableMap.of(0, A, 1, X));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        ImmutableSubstitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        ImmutableSubstitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_2, ImmutableList.of(E)).getTerm(0));

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2,
                        dataNode3));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode, unionTree));

        optimizeAndCompare(initialQuery, initialQuery);
    }

    private void optimizeAndCompare(IQ initialQuery, IQ expectedQuery) {
        IQ optimizedQuery = GENERAL_STRUCTURAL_AND_SEMANTIC_IQ_OPTIMIZER.optimize(initialQuery);
        assertEquals(expectedQuery, optimizedQuery);
    }
}
