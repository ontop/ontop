package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import org.junit.Test;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertTrue;

public class FlattenUnionOptimizerTest {

    
    private final static AtomPredicate ANS1_PREDICATE1 = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");

    @Test
    public void flattenUnionTest1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(DB_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE4_AR3, X, Y, Z));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE5_AR3, X, Y, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, unionNode2);
        queryBuilder1.addChild(unionNode2, dataNode2);
        queryBuilder1.addChild(unionNode2, unionNode3);
        queryBuilder1.addChild(unionNode3, dataNode3);
        queryBuilder1.addChild(unionNode3, dataNode4);


        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(DB_METADATA);

        queryBuilder2.init(projectionAtom1, unionNode1);
        queryBuilder2.addChild(unionNode1, IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2.getRelationDefinition(),
                ImmutableMap.of(0, X)));
        queryBuilder2.addChild(unionNode1, IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2.getRelationDefinition(),
                ImmutableMap.of(0, X)));
        queryBuilder2.addChild(unionNode1, IQ_FACTORY.createExtensionalDataNode(
                TABLE4_AR3.getRelationDefinition(),
                ImmutableMap.of(0, X)));
        queryBuilder2.addChild(unionNode1, IQ_FACTORY.createExtensionalDataNode(
                TABLE5_AR3.getRelationDefinition(),
                ImmutableMap.of(0, X)));

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    private IntermediateQuery optimize(IntermediateQuery initialQuery) throws EmptyQueryException {
        IQ initialIQ = IQ_CONVERTER.convert(initialQuery);
        IQ optimizedIQ = initialIQ.normalizeForOptimization();
        return IQ_CONVERTER.convert(optimizedIQ, initialQuery.getExecutorRegistry());
    }

    @Test
    public void flattenUnionTest2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(DB_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2.getRelationDefinition(),
                ImmutableMap.of(0, X));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE3_AR2, X, Y));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE4_AR3, X, Y, Z));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE5_AR3, X, Y, Z));

        queryBuilder1.init(projectionAtom1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, unionNode2);
        queryBuilder1.addChild(unionNode1, unionNode3);
        queryBuilder1.addChild(unionNode2, dataNode2);
        queryBuilder1.addChild(unionNode2, dataNode3);
        queryBuilder1.addChild(unionNode3, dataNode4);
        queryBuilder1.addChild(unionNode3, dataNode5);


        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(DB_METADATA);

        queryBuilder2.init(projectionAtom1, unionNode1);
        queryBuilder2.addChild(unionNode1, dataNode1);
        queryBuilder2.addChild(unionNode1, IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2.getRelationDefinition(),
                ImmutableMap.of(0, X)));
        queryBuilder2.addChild(unionNode1, IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2.getRelationDefinition(),
                ImmutableMap.of(0, X)));
        queryBuilder2.addChild(unionNode1, IQ_FACTORY.createExtensionalDataNode(
                TABLE4_AR3.getRelationDefinition(),
                ImmutableMap.of(0, X)));
        queryBuilder2.addChild(unionNode1, IQ_FACTORY.createExtensionalDataNode(
                TABLE5_AR3.getRelationDefinition(),
                ImmutableMap.of(0, X)));

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void flattenUnionTest3() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(DB_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2.getRelationDefinition(),
                ImmutableMap.of(0, X));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE3_AR2, X, Y));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(
                TABLE4_AR3.getRelationDefinition(),
                ImmutableMap.of(0, X, 1, Y));

        queryBuilder1.init(projectionAtom1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, innerJoinNode);
        queryBuilder1.addChild(innerJoinNode, dataNode4);
        queryBuilder1.addChild(innerJoinNode, unionNode2);
        queryBuilder1.addChild(unionNode2, dataNode2);
        queryBuilder1.addChild(unionNode2, dataNode3);


        IntermediateQuery query1 = queryBuilder1.build();
        IntermediateQuery snapshot = query1.createSnapshot();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, snapshot));
    }

    @Test
    public void flattenUnionTest4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(DB_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_AR2, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE3_AR2, X, Y));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE4_AR3, X, Y, Z));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE5_AR3, X, Y, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, innerJoinNode);
        queryBuilder1.addChild(innerJoinNode, dataNode2);
        queryBuilder1.addChild(innerJoinNode, unionNode2);
        queryBuilder1.addChild(unionNode2, unionNode3);
        queryBuilder1.addChild(unionNode2, dataNode3);
        queryBuilder1.addChild(unionNode3, dataNode4);
        queryBuilder1.addChild(unionNode3, dataNode5);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(DB_METADATA);

        queryBuilder2.init(projectionAtom1, unionNode1);
        queryBuilder2.addChild(unionNode1, IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2.getRelationDefinition(),
                ImmutableMap.of(0, X)));
        queryBuilder2.addChild(unionNode1, innerJoinNode);
        queryBuilder2.addChild(innerJoinNode, dataNode2);
        queryBuilder2.addChild(innerJoinNode, unionNode2);
        queryBuilder2.addChild(unionNode2, IQ_FACTORY.createExtensionalDataNode(
                TABLE4_AR3.getRelationDefinition(),
                ImmutableMap.of(0, X, 1, Y)));
        queryBuilder2.addChild(unionNode2, IQ_FACTORY.createExtensionalDataNode(
                TABLE5_AR3.getRelationDefinition(),
                ImmutableMap.of(0, X, 1, Y)));
        queryBuilder2.addChild(unionNode2, IQ_FACTORY.createExtensionalDataNode(
                TABLE3_AR2.getRelationDefinition(),
                ImmutableMap.of(0, X, 1, Y)));

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void flattenUnionTest5() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(DB_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(
                TABLE1_AR2.getRelationDefinition(),
                ImmutableMap.of(0, X));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_AR2, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE3_AR2, X, Y));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE4_AR3, X, Y, Z));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE5_AR3, X, Y, Z));

        queryBuilder1.init(projectionAtom1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, unionNode2);
        queryBuilder1.addChild(unionNode2, innerJoinNode);
        queryBuilder1.addChild(unionNode2, dataNode2);
        queryBuilder1.addChild(innerJoinNode, dataNode3);
        queryBuilder1.addChild(innerJoinNode, dataNode4);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery optimizedQuery = optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(DB_METADATA);

        queryBuilder2.init(projectionAtom1, unionNode1);
        queryBuilder2.addChild(unionNode1, dataNode1);
        queryBuilder2.addChild(unionNode1, innerJoinNode);
        queryBuilder2.addChild(unionNode1, IQ_FACTORY.createExtensionalDataNode(
                TABLE2_AR2.getRelationDefinition(),
                ImmutableMap.of(0, X)));
        queryBuilder2.addChild(innerJoinNode, dataNode3);
        queryBuilder2.addChild(innerJoinNode, IQ_FACTORY.createExtensionalDataNode(
                TABLE4_AR3.getRelationDefinition(),
                ImmutableMap.of(0, X, 1, Y)));

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }
}
