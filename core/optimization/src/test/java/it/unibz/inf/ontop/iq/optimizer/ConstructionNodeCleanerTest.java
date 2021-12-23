package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import org.apache.commons.rdf.api.IRI;
import org.junit.Ignore;
import org.junit.Test;


import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertTrue;

@Ignore("TODO: support it (and update it)")
public class ConstructionNodeCleanerTest {

    private final static IRI R1_IRI = RDF_FACTORY.createIRI("http://example.com/voc#r1");
    private final static IRI R2_IRI = RDF_FACTORY.createIRI("http://example.com/voc#r2");
    private final static IRI R3_IRI = RDF_FACTORY.createIRI("http://example.com/voc#r3");
    private final static AtomPredicate ANS1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 1);
    private final static AtomPredicate ANS2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(2);
    private final static AtomPredicate ANS3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(3);
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable X1 = TERM_FACTORY.getVariable("X1");
    private final static Variable X2 = TERM_FACTORY.getVariable("X2");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Y1 = TERM_FACTORY.getVariable("Y1");
    private final static Variable Y2 = TERM_FACTORY.getVariable("Y2");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");
    private final static Variable Z1 = TERM_FACTORY.getVariable("Z1");
    private final static Variable Z2 = TERM_FACTORY.getVariable("Z2");

    private ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/ds1/", 0);
    private ImmutableList<Template.Component> URI_TEMPLATE_STR_2_2 = Template.of("http://example.org/ds2/", 0, "/", 1);

    // TODO: choose an implementation
    private static IQOptimizer constructionNodeCleaner = null;


    @Test
    public void removeConstructionNodeTest1() throws EmptyQueryException {


        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);

        SliceNode limitNode = IQ_FACTORY.createSliceNode(0,100);
        queryBuilder1.init(projectionAtom1, limitNode);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        queryBuilder1.addChild(limitNode, distinctNode);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        queryBuilder1.addChild(distinctNode, constructionNode1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y));
        queryBuilder1.addChild(constructionNode1, constructionNode2);

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X, R1_IRI, Y));
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IQ query1 = queryBuilder1.buildIQ();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        queryBuilder2.init(projectionAtom1, limitNode);
        queryBuilder2.addChild(limitNode, distinctNode);
        queryBuilder2.addChild(distinctNode, constructionNode1);
        queryBuilder2.addChild(constructionNode1, dataNode1);

        IQ query2 = queryBuilder2.buildIQ();
        System.out.println("\nExpected: \n" + query2);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query1);
        //optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQ_EQUALITY_CHECK.equal(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        queryBuilder1.init(projectionAtom1, constructionNode1);

        SliceNode limitNode = IQ_FACTORY.createSliceNode(0,100);
        queryBuilder1.addChild(constructionNode1, limitNode);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        queryBuilder1.addChild(limitNode, distinctNode);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y));
        queryBuilder1.addChild(distinctNode, constructionNode2);

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X, R1_IRI, Y));
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IQ query1 = queryBuilder1.buildIQ();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        queryBuilder2.init(projectionAtom1, limitNode);
        queryBuilder2.addChild(limitNode, distinctNode);
        queryBuilder2.addChild(distinctNode, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IQ query2 = queryBuilder2.buildIQ();
        System.out.println("\nExpected: \n" + query2);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query1);
        //optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQ_EQUALITY_CHECK.equal(optimizedQuery, query2));
    }


    @Ignore("TODO: refactor after moving the query modifiers away from the construction node ")
    @Test
    public void removeConstructionNodeTest3() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables());

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y));

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X, R1_IRI, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IQ query1 = queryBuilder1.buildIQ();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution()
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        queryBuilder2.init(projectionAtom1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IQ query2 = queryBuilder2.buildIQ();
        System.out.println("\nExpected: \n" + query2);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query1);
        //optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQ_EQUALITY_CHECK.equal(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);

        SliceNode limitNode1 = IQ_FACTORY.createSliceNode(0,100);
        queryBuilder1.init(projectionAtom1, limitNode1);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(Z2)));
        queryBuilder1.addChild(limitNode1, constructionNode1);

        SliceNode limitNode2 = IQ_FACTORY.createSliceNode(0,50);
        queryBuilder1.addChild(constructionNode1, limitNode2);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y, Z2),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Z)));
        queryBuilder1.addChild(limitNode2, constructionNode2);

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(Z, R1_IRI, Z2));

        queryBuilder1.addChild(constructionNode2, dataNode1);

        IQ query1 = queryBuilder1.buildIQ();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X,generateURI1(Z2), Y, generateURI1(Z)));

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        queryBuilder2.init(projectionAtom1, limitNode2);
        queryBuilder2.addChild(limitNode2, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IQ query2 = queryBuilder2.buildIQ();
        System.out.println("\nExpected: \n" + query2);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query1);
        //optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQ_EQUALITY_CHECK.equal(optimizedQuery, query2));
    }


    @Test
    public void removeConstructionNodeTest5() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);

        SliceNode limitNode1 = IQ_FACTORY.createSliceNode(0,100);
        queryBuilder1.init(projectionAtom1, limitNode1);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(Z2)));
        queryBuilder1.addChild(limitNode1, constructionNode1);

        SliceNode limitNode2 = IQ_FACTORY.createSliceNode(0,50);
        queryBuilder1.addChild(constructionNode1, limitNode2);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y, Z2),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Z)));

        queryBuilder1.addChild(limitNode2, constructionNode2);

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(Z, R1_IRI, Z2));

        queryBuilder1.addChild(constructionNode2, dataNode1);

        IQ query1 = queryBuilder1.buildIQ();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();
        queryBuilder2.init(projectionAtom1, limitNode1);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(Z2), Y, generateURI1(Z)));
        queryBuilder2.addChild(limitNode1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, limitNode2);

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Z, Z2),
                SUBSTITUTION_FACTORY.getSubstitution());
        queryBuilder2.addChild(limitNode2, constructionNode4);
        queryBuilder2.addChild(constructionNode4, dataNode1);

        IQ query2 = queryBuilder2.buildIQ();
        System.out.println("\nExpected: \n" + query2);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query1);
        //optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQ_EQUALITY_CHECK.equal(optimizedQuery, query2));
    }


    @Test
    public void removeConstructionNodeTest6() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1))
        );

        queryBuilder1.init(projectionAtom1, constructionNode1);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1)));

        queryBuilder1.addChild(constructionNode1, constructionNode2);

        SliceNode limitNode = IQ_FACTORY.createSliceNode(0,100);
        queryBuilder1.addChild(constructionNode2, limitNode);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y1, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateURI1(Z1)));
        queryBuilder1.addChild(limitNode, constructionNode3);

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X1, Y1, Z1));
        queryBuilder1.addChild(constructionNode3, dataNode1);

        IQ query1 = queryBuilder1.buildIQ();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();
        queryBuilder2.init(projectionAtom1, limitNode);

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(X1),
                        Y, generateURI1(Y1),
                        Z, generateURI1(Z1)
                ));
        queryBuilder2.addChild(limitNode, constructionNode4);
        queryBuilder2.addChild(constructionNode4, dataNode1);

        IQ query2 = queryBuilder2.buildIQ();
        System.out.println("\nExpected: \n" + query2);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query1);
        //optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQ_EQUALITY_CHECK.equal(optimizedQuery, query2));
    }


    @Test
    public void removeConstructionNodeTest7() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);

        SliceNode limit50 = IQ_FACTORY.createSliceNode(0,50);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1)));

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1)));

        SliceNode limit40 = IQ_FACTORY.createSliceNode(0,40);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y1, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateURI1(Z1)));

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X1, Y1, Z1));

        queryBuilder1.init(projectionAtom1, limit50);
        queryBuilder1.addChild(limit50, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, limit40);
        queryBuilder1.addChild(limit40, constructionNode3);
        queryBuilder1.addChild(constructionNode3, dataNode1);

        IQ query1 = queryBuilder1.buildIQ();

        System.out.println("\nBefore optimization: \n" + query1);

        SliceNode limit90 = IQ_FACTORY.createSliceNode(0,90);

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(X1),
                        Y, generateURI1(Y1),
                        Z, generateURI1(Z1)));

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        queryBuilder2.init(projectionAtom1, limit90);
        queryBuilder2.addChild(limit90, constructionNode4);
        queryBuilder2.addChild(constructionNode4, dataNode1);

        IQ query2 = queryBuilder2.buildIQ();
        System.out.println("\nExpected: \n" + query2);


        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query1);
        //optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQ_EQUALITY_CHECK.equal(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest8() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);

        SliceNode limit50 = IQ_FACTORY.createSliceNode(0,50);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1)));

        SliceNode limit80 = IQ_FACTORY.createSliceNode(0,80);

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1)));

        SliceNode limit40 = IQ_FACTORY.createSliceNode(0,40);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y1, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateURI1(Z1)));

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X1, Y1, Z1));

        queryBuilder1.init(projectionAtom1, limit50);
        queryBuilder1.addChild(limit50, constructionNode1);
        queryBuilder1.addChild(constructionNode1, limit80);
        queryBuilder1.addChild(limit80, constructionNode2);
        queryBuilder1.addChild(constructionNode2, limit40);
        queryBuilder1.addChild(limit40, constructionNode3);
        queryBuilder1.addChild(constructionNode3, dataNode1);

        IQ query1 = queryBuilder1.buildIQ();

        System.out.println("\nBefore optimization: \n" + query1);

        SliceNode limit130 = IQ_FACTORY.createSliceNode(0,130);

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(X1),
                        Y, generateURI1(Y1),
                        Z, generateURI1(Z1)));

        ConstructionNode constructionNode5 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y1, Z1));
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        queryBuilder2.init(projectionAtom1, limit130);
        queryBuilder2.addChild(limit130, constructionNode4);
        queryBuilder2.addChild(constructionNode4, limit40);
        queryBuilder2.addChild(limit40, constructionNode5);
        queryBuilder2.addChild(constructionNode5, dataNode1);

        IQ query2 = queryBuilder2.buildIQ();
        System.out.println("\nExpected: \n" + query2);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query1);
        //optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQ_EQUALITY_CHECK.equal(optimizedQuery, query2));
    }


    @Test
    public void removeConstructionNodeTest9() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);

        SliceNode limit130 = IQ_FACTORY.createSliceNode(0,130);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables());

        SliceNode limit100 = IQ_FACTORY.createSliceNode(0,100);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1)));

        DistinctNode distinctNode1 = IQ_FACTORY.createDistinctNode();
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1)));

        DistinctNode distinctNode2 = IQ_FACTORY.createDistinctNode();
        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y1, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateURI1(Z1)));
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X1, Y, Z));

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X1, Y, Z));
        IntensionalDataNode dataNode2 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X1, Y1, Z1));

        queryBuilder1.init(projectionAtom1, limit130);
        queryBuilder1.addChild(limit130, constructionNode1);
        queryBuilder1.addChild(constructionNode1, limit100);
        queryBuilder1.addChild(limit100, constructionNode2);
        queryBuilder1.addChild(constructionNode2, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, distinctNode1);
        queryBuilder1.addChild(distinctNode1, constructionNode3);
        queryBuilder1.addChild(constructionNode3, distinctNode2);
        queryBuilder1.addChild(distinctNode2, constructionNode4);
        queryBuilder1.addChild(constructionNode4, dataNode2);

        IQ query1 = queryBuilder1.buildIQ();

        System.out.println("\nBefore optimization: \n" + query1);

        SliceNode limit230 = IQ_FACTORY.createSliceNode(0,230);

        ConstructionNode constructionNode5 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1)));


        ConstructionNode constructionNode6 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1), Z, generateURI1(Z1)));
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        queryBuilder2.init(projectionAtom1, limit230);
        queryBuilder2.addChild(limit230, constructionNode5);
        queryBuilder2.addChild(constructionNode5, unionNode1);
        queryBuilder2.addChild(unionNode1, dataNode1);
        queryBuilder2.addChild(unionNode1, distinctNode1);
        queryBuilder2.addChild(distinctNode1, constructionNode6);
        queryBuilder2.addChild(constructionNode6, dataNode2);

        IQ query2 = queryBuilder2.buildIQ();
        System.out.println("\nExpected: \n" + query2);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query1);
        //optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQ_EQUALITY_CHECK.equal(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest10() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateCompositeURI2(Y, Z))
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution()
        );

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(Y, R1_IRI, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IQ query1 = queryBuilder1.buildIQ();

        System.out.println("\nBefore optimization: \n" + query1);


        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query1);
        //optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, dataNode1);

        IQ query2 = queryBuilder2.buildIQ();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQ_EQUALITY_CHECK.equal(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest11() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE,
                X, Y);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution()
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1), Y, generateURI1(Y1))
        );

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X1, R1_IRI, Y1));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IQ query1 = queryBuilder1.buildIQ();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        queryBuilder2.init(projectionAtom1, constructionNode2);
        queryBuilder2.addChild(constructionNode2, dataNode1);

        IQ query2 = queryBuilder2.buildIQ();
        System.out.println("\nExpected: \n" + query2);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query1);
        //optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQ_EQUALITY_CHECK.equal(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest12() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X1);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables()
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1)
        );
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1)
        );
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X1));

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X1, R1_IRI, X2));
        IntensionalDataNode dataNode2 = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X1, R2_IRI, X2));
        IntensionalDataNode dataNode3 = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X1, R3_IRI, X2));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, constructionNode2);
        queryBuilder1.addChild(unionNode1, constructionNode3);
        queryBuilder1.addChild(constructionNode2, dataNode2);
        queryBuilder1.addChild(constructionNode3, dataNode3);

        IQ query1 = queryBuilder1.buildIQ();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder();

        queryBuilder2.init(projectionAtom1, unionNode1);
        queryBuilder2.addChild(unionNode1, dataNode1);
        queryBuilder2.addChild(unionNode1, dataNode2);
        queryBuilder2.addChild(unionNode1, dataNode3);

        IQ query2 = queryBuilder2.buildIQ();
        System.out.println("\nExpected: \n" + query2);

        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(query1);
        //optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQ_EQUALITY_CHECK.equal(optimizedQuery, query2));
    }


    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }

    private ImmutableFunctionalTerm generateCompositeURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_2_2, ImmutableList.of(argument1, argument2));
    }
}
