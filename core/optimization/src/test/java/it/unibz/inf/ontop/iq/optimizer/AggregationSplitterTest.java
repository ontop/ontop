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
import it.unibz.inf.ontop.iq.impl.QueryNodeRenamer;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.HomogeneousIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

public class AggregationSplitterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationSplitterTest.class);
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

    private IQTree generateLiftedAggregateSubTree(Variable sampleVariable, Substitution<ImmutableFunctionalTerm> oldSubstitution, IQTree child) {
        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(sampleVariable));

        AggregationNode newAggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        sampleVariable,
                        TERM_FACTORY.getDBSample(ONE, TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType())
                ).compose(oldSubstitution).transform(t -> (ImmutableFunctionalTerm)t)
        );

        IQTree newChild = IQ_FACTORY.createUnaryIQTree(filterNode,
                                IQ_FACTORY.createUnaryIQTree(newAggregationNode,
                                                child)
                        );
        return newChild;
    }

    @Test
    public void testLiftAggregation1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        Substitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, ONE);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        Substitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TWO);

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

        IQTree newAggregationTree1 = generateLiftedAggregateSubTree(AGGV, SUBSTITUTION_FACTORY.getSubstitution(
                        Y, TERM_FACTORY.getDBCount(A, false)), dataNode1);

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution1),
                applyInDepthRenaming(
                        newAggregationTree1,
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF0).injective()));

        IQTree newAggregationTree2 = generateLiftedAggregateSubTree(AGGVF0, SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)), dataNode2);

        IQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution2),
                applyInDepthRenaming(
                        newAggregationTree2,
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF1).injective()));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newUnionNode,
                ImmutableList.of(newChild1, newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    private IQTree applyInDepthRenaming(IQTree tree, InjectiveSubstitution<Variable> renaming) {
        QueryNodeRenamer nodeTransformer = new QueryNodeRenamer(IQ_FACTORY, renaming, ATOM_FACTORY, SUBSTITUTION_FACTORY);
        HomogeneousIQTreeVisitingTransformer iqTransformer = new HomogeneousIQTreeVisitingTransformer(nodeTransformer, IQ_FACTORY);
        return iqTransformer.transform(tree);
    }

    @Test
    public void testLiftAggregation2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A, 1, C));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        Substitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        Substitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_2, ImmutableList.of(C)).getTerm(0));

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
                applyInDepthRenaming(
                    IQ_FACTORY.createUnaryIQTree(
                            IQ_FACTORY.createAggregationNode(ImmutableSet.of(B), SUBSTITUTION_FACTORY.getSubstitution(
                                        Y, TERM_FACTORY.getDBCount(A, false))),
                        dataNode1),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF0).injective()));

        UnaryIQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution2),
                applyInDepthRenaming(
                        IQ_FACTORY.createUnaryIQTree(
                            IQ_FACTORY.createAggregationNode(ImmutableSet.of(C), SUBSTITUTION_FACTORY.getSubstitution(
                                Y, TERM_FACTORY.getDBCount(A, false))),
                        dataNode2),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF1).injective()));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newUnionNode,
                ImmutableList.of(
                        newChild1,
                        newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testLiftAggregation3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A, 1, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(T3_AR3, ImmutableMap.of(0, A, 1, D, 2, E));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        Substitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        Substitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_2, ImmutableList.of(C)).getTerm(0));
        Substitution<ImmutableTerm> xSubstitution3 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_4, ImmutableList.of(D, E)).getTerm(0));

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

        IQTree newChild1 = applyInDepthRenaming(IQ_FACTORY.createUnaryIQTree(
                aggregationNode,
                IQ_FACTORY.createNaryIQTree(
                        unionNode,
                        ImmutableList.of(
                                child1,
                                child3))),
                SUBSTITUTION_FACTORY.getSubstitution(A, AF0).injective());

        UnaryIQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution2),
                applyInDepthRenaming(
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createAggregationNode(ImmutableSet.of(C), SUBSTITUTION_FACTORY.getSubstitution(
                                Y, TERM_FACTORY.getDBCount(A, false))),
                        dataNode2),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF1).injective()));

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

        Substitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        Substitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_2, ImmutableList.of(C)).getTerm(0));
        Substitution<ImmutableTerm> xSubstitution3 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_5, ImmutableList.of(D)).getTerm(0));

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

        IQTree newChild1 = applyInDepthRenaming( IQ_FACTORY.createUnaryIQTree(
                        aggregationNode,
                        IQ_FACTORY.createNaryIQTree(
                                unionNode,
                                ImmutableList.of(
                                        child1,
                                        child3))),
                SUBSTITUTION_FACTORY.getSubstitution(A, AF0).injective());

        UnaryIQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newTopUnionNode.getVariables(), xSubstitution2),
                applyInDepthRenaming(
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createAggregationNode(ImmutableSet.of(C), SUBSTITUTION_FACTORY.getSubstitution(
                                Y, TERM_FACTORY.getDBCount(A, false))),
                        dataNode2),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF1).injective()));

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

        Substitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, ONE);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        Substitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TWO);

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

        IQTree newAggregationTree1 = generateLiftedAggregateSubTree(AGGV, SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)), IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createUnionNode(ImmutableSet.of(A)),
                ImmutableList.of(dataNode1, dataNode1)));

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution1),
                applyInDepthRenaming(
                        newAggregationTree1,
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF0).injective()));

        IQTree newAggregationTree2 = generateLiftedAggregateSubTree(AGGVF0, SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)), dataNode2);

        IQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution2),
                applyInDepthRenaming(newAggregationTree2,
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF1).injective()));

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

        Substitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, NULL);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        Substitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TWO);

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

        IQTree newAggregationTree1 = generateLiftedAggregateSubTree(AGGV, SUBSTITUTION_FACTORY.getSubstitution(
                        Y, TERM_FACTORY.getDBCount(A, false)), IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createUnionNode(ImmutableSet.of(A)),
                        ImmutableList.of(dataNode1, dataNode1)));

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution1),
                applyInDepthRenaming(
                        newAggregationTree1,
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF0).injective()));

        IQTree newAggregationTree2 = generateLiftedAggregateSubTree(AGGVF0, SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)), dataNode2);

        IQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), xSubstitution2),
                applyInDepthRenaming(newAggregationTree2,
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF1).injective()));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newUnionNode,
                ImmutableList.of(newChild1, newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testLiftAggregation7() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, X, Y, Z);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(Z, A));

        Substitution<ImmutableTerm> zSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(Z, ONE);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), zSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        Substitution<ImmutableTerm> zSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(Z, TWO);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                zSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2));

        ConstructionNode commonConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Z, A),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                        TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), Z, A)));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X,Z), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode,
                        IQ_FACTORY.createUnaryIQTree(
                                commonConstructionNode,
                                unionTree)));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        AggregationNode newAggregationNode1 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), zSubstitution1),
                applyInDepthRenaming(
                IQ_FACTORY.createUnaryIQTree(
                        newAggregationNode1,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,A),
                                                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                                                        TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), ONE, A))),
                                                dataNode1)),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF0).injective()));

        AggregationNode newAggregationNode2 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), zSubstitution2),
                applyInDepthRenaming(
                IQ_FACTORY.createUnaryIQTree(
                        newAggregationNode2,
                        IQ_FACTORY.createUnaryIQTree(
                                IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,A),
                                        SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                                                TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), TWO, A))),
                                dataNode2)),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF1).injective()));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newUnionNode,
                ImmutableList.of(newChild1, newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testLiftAggregation8() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, X, Y, Z);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A, 1, B));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(Z, A, B));

        Substitution<ImmutableTerm> zSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(Z, ONE);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), zSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        Substitution<ImmutableTerm> zSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(Z, TWO);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                zSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2));

        ConstructionNode commonConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Z, A),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                        TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), Z, B)));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X,Z), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode,
                        IQ_FACTORY.createUnaryIQTree(
                                commonConstructionNode,
                                unionTree)));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        AggregationNode newAggregationNode1 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), zSubstitution1),
                applyInDepthRenaming(
                IQ_FACTORY.createUnaryIQTree(
                                newAggregationNode1,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,A),
                                                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                                                        TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), ONE, B))),
                                        dataNode1)),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF0).injective()));

        AggregationNode newAggregationNode2 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), zSubstitution2),
                applyInDepthRenaming(
                IQ_FACTORY.createUnaryIQTree(
                                newAggregationNode2,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,A),
                                                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                                                        TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), TWO, B))),
                                        dataNode2)),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF1).injective()));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newUnionNode,
                ImmutableList.of(newChild1, newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testLiftAggregationDistinct1() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A, 1, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(T3_AR3, ImmutableMap.of(0, A, 1, D));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, A));

        Substitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        Substitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_2, ImmutableList.of(C)).getTerm(0));
        Substitution<ImmutableTerm> xSubstitution3 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_5, ImmutableList.of(D)).getTerm(0));

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

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode,
                        IQ_FACTORY.createUnaryIQTree(
                                distinctNode, unionTree)));

        UnionNode newTopUnionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        IQTree newChild1 = applyInDepthRenaming(IQ_FACTORY.createUnaryIQTree(
                aggregationNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        IQ_FACTORY.createNaryIQTree(
                                unionNode,
                                ImmutableList.of(
                                        child1,
                                        child3)))),
                SUBSTITUTION_FACTORY.getSubstitution(A, AF0).injective());

        UnaryIQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newTopUnionNode.getVariables(), xSubstitution2),
                applyInDepthRenaming(
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createAggregationNode(ImmutableSet.of(C), SUBSTITUTION_FACTORY.getSubstitution(
                                Y, TERM_FACTORY.getDBCount(A, false))),
                        IQ_FACTORY.createUnaryIQTree(
                                distinctNode,
                                dataNode2)),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF1).injective()));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newTopUnionNode,
                ImmutableList.of(
                        newChild1,
                        newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testLiftAggregationDistinct2() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, X, Y, Z);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A, 1, B));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(Z, A, B));

        Substitution<ImmutableTerm> zSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(Z, ONE);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), zSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        Substitution<ImmutableTerm> zSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(Z, TWO);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                zSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2));

        ConstructionNode commonConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Z, A),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                        TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), Z, B)));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X,Z), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode,
                        IQ_FACTORY.createUnaryIQTree(
                                distinctNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        commonConstructionNode,
                                        unionTree))));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        AggregationNode newAggregationNode1 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), zSubstitution1),
                applyInDepthRenaming(
                IQ_FACTORY.createUnaryIQTree(
                                newAggregationNode1,
                                IQ_FACTORY.createUnaryIQTree(
                                                distinctNode,
                                                IQ_FACTORY.createUnaryIQTree(
                                                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,A),
                                                                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                                                                        TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), ONE, B))),
                                                        dataNode1))),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF0).injective()));

        AggregationNode newAggregationNode2 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), zSubstitution2),
                applyInDepthRenaming(
                IQ_FACTORY.createUnaryIQTree(
                                newAggregationNode2,
                                IQ_FACTORY.createUnaryIQTree(
                                        distinctNode,
                                    IQ_FACTORY.createUnaryIQTree(
                                            IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,A),
                                                    SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                                                            TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), TWO, B))),
                                            dataNode2))),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF1).injective()));

        NaryIQTree newUnionTree = IQ_FACTORY.createNaryIQTree(
                newUnionNode,
                ImmutableList.of(newChild1, newChild2));

        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom, newUnionTree);

        optimizeAndCompare(initialQuery, expectedQuery);
    }

    @Test
    public void testLiftAggregationDistinct3() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, X, Y, Z);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A, 1, B));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(Z, A, B));

        Substitution<ImmutableTerm> zSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(Z, ONE);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), zSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        Substitution<ImmutableTerm> zSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(Z, TWO);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(),
                zSubstitution2);

        UnaryIQTree child2 = IQ_FACTORY.createUnaryIQTree(
                constructionNode2,
                dataNode2);

        NaryIQTree unionTree = IQ_FACTORY.createNaryIQTree(
                unionNode,
                ImmutableList.of(
                        child1,
                        child2));

        ConstructionNode commonConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,Z, A),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                        TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), Z, B)));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X,Z), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode,
                        IQ_FACTORY.createUnaryIQTree(
                                commonConstructionNode,
                                IQ_FACTORY.createUnaryIQTree(
                                        distinctNode,
                                        unionTree))));

        UnionNode newUnionNode = IQ_FACTORY.createUnionNode(projectionAtom.getVariables());

        AggregationNode newAggregationNode1 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild1 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), zSubstitution1),
                applyInDepthRenaming(IQ_FACTORY.createUnaryIQTree(
                                newAggregationNode1,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,A),
                                                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                                                        TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), ONE, B))),
                                        IQ_FACTORY.createUnaryIQTree(
                                                distinctNode,
                                                dataNode1))),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF0).injective()));

        AggregationNode newAggregationNode2 = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)));

        IQTree newChild2 = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(newUnionNode.getVariables(), zSubstitution2),
                applyInDepthRenaming(
                IQ_FACTORY.createUnaryIQTree(
                                newAggregationNode2,
                                IQ_FACTORY.createUnaryIQTree(
                                        IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,A),
                                                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                                                        TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), TWO, B))),
                                        IQ_FACTORY.createUnaryIQTree(
                                                distinctNode,
                                                dataNode2))),
                        SUBSTITUTION_FACTORY.getSubstitution(A, AF1).injective()));

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

        Substitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        Substitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_3, ImmutableList.of(C, D)).getTerm(0));

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

        Substitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        Substitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_2, ImmutableList.of(E)).getTerm(0));
        Substitution<ImmutableTerm> xSubstitution3 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_3, ImmutableList.of(C, D)).getTerm(0));

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

        Substitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_1, ImmutableList.of(B)).getTerm(0));
        Substitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getIRIFunctionalTerm(TEMPLATE_2, ImmutableList.of(E)).getTerm(0));

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

    @Test
    public void testNoLiftAggregation4() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, Y);

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(T1_AR3, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(T2_AR3, ImmutableMap.of(0, A));

        UnionNode unionNode = IQ_FACTORY.createUnionNode(ImmutableSet.of(B, A));

        Substitution<ImmutableTerm> xSubstitution1 = SUBSTITUTION_FACTORY.getSubstitution(B, ONE);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(unionNode.getVariables(), xSubstitution1);

        UnaryIQTree child1 = IQ_FACTORY.createUnaryIQTree(
                constructionNode1,
                dataNode1);

        Substitution<ImmutableTerm> xSubstitution2 = SUBSTITUTION_FACTORY.getSubstitution(B, TWO);

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

        ConstructionNode commonConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X,A),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getDBBinaryNumericFunctionalTerm("+",
                        TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType(), B, B)));

        AggregationNode aggregationNode = IQ_FACTORY.createAggregationNode(ImmutableSet.of(X), SUBSTITUTION_FACTORY.getSubstitution(
                Y, TERM_FACTORY.getDBCount(A, false)
        ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                IQ_FACTORY.createUnaryIQTree(aggregationNode,
                        IQ_FACTORY.createUnaryIQTree(
                                commonConstructionNode,
                                unionTree)));

        optimizeAndCompare(initialQuery, initialQuery);
    }

    private void optimizeAndCompare(IQ initialQuery, IQ expectedQuery) {
        IQ optimizedQuery = GENERAL_STRUCTURAL_AND_SEMANTIC_IQ_OPTIMIZER.optimize(initialQuery);
        LOGGER.debug("Initial query: {}", initialQuery);
        assertEquals(expectedQuery, optimizedQuery);
        LOGGER.debug("Optimized query: {}", optimizedQuery);
    }
}
