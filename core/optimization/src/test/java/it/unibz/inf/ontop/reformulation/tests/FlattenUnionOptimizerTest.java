package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.node.impl.ConstructionNodeImpl;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.ExpressionOperation;
import it.unibz.inf.ontop.model.predicate.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.FlattenUnionOptimizer;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.EMPTY_METADATA;
import static it.unibz.inf.ontop.OptimizationTestingTools.IQ_FACTORY;
import static it.unibz.inf.ontop.OptimizationTestingTools.createQueryBuilder;
import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;
import static it.unibz.inf.ontop.model.predicate.Predicate.COL_TYPE.INTEGER;
import static junit.framework.TestCase.assertTrue;

public class FlattenUnionOptimizerTest {


    private final static AtomPredicate TABLE1_PREDICATE = DATA_FACTORY.getAtomPredicate("table1", 2);
    private final static AtomPredicate TABLE2_PREDICATE = DATA_FACTORY.getAtomPredicate("table2", 2);
    private final static AtomPredicate TABLE3_PREDICATE = DATA_FACTORY.getAtomPredicate("table3", 2);
    private final static AtomPredicate TABLE4_PREDICATE = DATA_FACTORY.getAtomPredicate("table4", 3);
    private final static AtomPredicate TABLE5_PREDICATE = DATA_FACTORY.getAtomPredicate("table5", 3);
    private final static AtomPredicate TABLE6_PREDICATE = DATA_FACTORY.getAtomPredicate("table6", 3);
    private final static AtomPredicate ANS1_PREDICATE1 = DATA_FACTORY.getAtomPredicate("ans1", 1);
    private final static Variable X = DATA_FACTORY.getVariable("X");
    private final static Variable Y = DATA_FACTORY.getVariable("Y");
    private final static Variable Z = DATA_FACTORY.getVariable("Z");
    private final static Variable W = DATA_FACTORY.getVariable("W");
    private final static Variable A = DATA_FACTORY.getVariable("A");
    private final static Variable B = DATA_FACTORY.getVariable("B");


    private final static ImmutableExpression EXPRESSION1 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.NEQ, Y, Z);
    private final static ImmutableExpression EXPRESSION2 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.NEQ, W, X);


    private ImmutableFunctionalTerm generateInt(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(
                DATA_FACTORY.getDatatypeFactory().getTypePredicate(INTEGER),
                argument
        );
    }

    @Test
    public void flattenUnionTest1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE5_PREDICATE, X, Y, Z));

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

        FlattenUnionOptimizer flattenUnionOptimizer = new FlattenUnionOptimizer();
        IntermediateQuery optimizedQuery = flattenUnionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, unionNode1);
        queryBuilder2.addChild(unionNode1, dataNode1);
        queryBuilder2.addChild(unionNode1, dataNode2);
        queryBuilder2.addChild(unionNode1, dataNode3);
        queryBuilder2.addChild(unionNode1, dataNode4);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void flattenUnionTest2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE3_PREDICATE, X, Y));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE5_PREDICATE, X, Y, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, unionNode2);
        queryBuilder1.addChild(unionNode1, unionNode3);
        queryBuilder1.addChild(unionNode2, dataNode2);
        queryBuilder1.addChild(unionNode2, dataNode3);
        queryBuilder1.addChild(unionNode3, dataNode4);
        queryBuilder1.addChild(unionNode3, dataNode5);


        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        FlattenUnionOptimizer flattenUnionOptimizer = new FlattenUnionOptimizer();
        IntermediateQuery optimizedQuery = flattenUnionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, unionNode1);
        queryBuilder2.addChild(unionNode1, dataNode1);
        queryBuilder2.addChild(unionNode1, dataNode2);
        queryBuilder2.addChild(unionNode1, dataNode3);
        queryBuilder2.addChild(unionNode1, dataNode4);
        queryBuilder2.addChild(unionNode1, dataNode5);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void flattenUnionTest3() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE3_PREDICATE, X, Y));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE4_PREDICATE, X, Y, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, innerJoinNode);
        queryBuilder1.addChild(innerJoinNode, dataNode4);
        queryBuilder1.addChild(innerJoinNode, unionNode2);
        queryBuilder1.addChild(unionNode2, dataNode2);
        queryBuilder1.addChild(unionNode2, dataNode3);


        IntermediateQuery query1 = queryBuilder1.build();
        IntermediateQuery snapshot = query1.createSnapshot();

        System.out.println("\nBefore optimization: \n" + query1);

        FlattenUnionOptimizer flattenUnionOptimizer = new FlattenUnionOptimizer();
        IntermediateQuery optimizedQuery = flattenUnionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, snapshot));
    }

    @Test
    public void flattenUnionTest4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE1, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X));
        UnionNode unionNode2 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y));
        UnionNode unionNode3 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X, Y, Z));
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Y));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE3_PREDICATE, X, Y));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE4_PREDICATE, X, Y, Z));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE5_PREDICATE, X, Y, Z));

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

        FlattenUnionOptimizer flattenUnionOptimizer = new FlattenUnionOptimizer();
        IntermediateQuery optimizedQuery = flattenUnionOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, unionNode1);
        queryBuilder2.addChild(unionNode1, dataNode1);
        queryBuilder2.addChild(unionNode1, innerJoinNode);
        queryBuilder2.addChild(innerJoinNode, dataNode2);
        queryBuilder2.addChild(innerJoinNode, unionNode2);
        queryBuilder2.addChild(unionNode2, dataNode3);
        queryBuilder2.addChild(unionNode2, dataNode4);
        queryBuilder2.addChild(unionNode2, dataNode5);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }
}
