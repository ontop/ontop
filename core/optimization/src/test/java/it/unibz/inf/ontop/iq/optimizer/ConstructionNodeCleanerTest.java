package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import org.apache.commons.rdf.api.IRI;
import org.junit.Test;


import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static junit.framework.TestCase.assertEquals;

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

    private final ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/ds1/", 0);
    private final ImmutableList<Template.Component> URI_TEMPLATE_STR_2_2 = Template.of("http://example.org/ds2/", 0, "/", 1);


    @Test
    public void removeConstructionNodeTest1()  {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        SliceNode limitNode = IQ_FACTORY.createSliceNode(0,100);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y));
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X, R1_IRI, Y));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(limitNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode,
                                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode1)))));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(limitNode,
                        IQ_FACTORY.createUnaryIQTree(distinctNode,
                                IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1))));

        optimizeAndCompare(query1, query2);
    }

    @Test
    public void removeConstructionNodeTest2() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        SliceNode limitNode = IQ_FACTORY.createSliceNode(0,100);
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y));
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X, R1_IRI, Y));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(limitNode,
                                IQ_FACTORY.createUnaryIQTree(distinctNode,
                                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode1)))));

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode3,
                        IQ_FACTORY.createUnaryIQTree(limitNode, dataNode1)));

        optimizeAndCompare(query1, query2);
    }


    @Test
    public void removeConstructionNodeTest3() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y));
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X, R1_IRI, Y));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode1)));

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode3, dataNode1));

        optimizeAndCompare(query1, query2);
    }

    @Test
    public void removeConstructionNodeTest4() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
        SliceNode limitNode1 = IQ_FACTORY.createSliceNode(0,100);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(Z2)));
        SliceNode limitNode2 = IQ_FACTORY.createSliceNode(0,50);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, Z2),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Z)));
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(Z, R1_IRI, Z2));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(limitNode1,
                        IQ_FACTORY.createUnaryIQTree(constructionNode1,
                                IQ_FACTORY.createUnaryIQTree(limitNode2,
                                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode1)))));

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X,generateURI1(Z2), Y, generateURI1(Z)));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode3,
                        IQ_FACTORY.createUnaryIQTree(limitNode2, dataNode1)));

        optimizeAndCompare(query1, query2);
    }


    @Test
    public void removeConstructionNodeTest5() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
        SliceNode limitNode1 = IQ_FACTORY.createSliceNode(0,100);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(Z2)));
        SliceNode limitNode2 = IQ_FACTORY.createSliceNode(0,50);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Y, Z2),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Z)));
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(Z, R1_IRI, Z2));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(limitNode1,
                        IQ_FACTORY.createUnaryIQTree(constructionNode1,
                                IQ_FACTORY.createUnaryIQTree(limitNode2,
                                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode1)))));

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(Z2), Y, generateURI1(Z)));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode3,
                        IQ_FACTORY.createUnaryIQTree(limitNode2, dataNode1)));

        optimizeAndCompare(query1, query2);
    }


    @Test
    public void removeConstructionNodeTest6() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1)));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1)));
        SliceNode limitNode = IQ_FACTORY.createSliceNode(0,100);
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X1, Y1, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateURI1(Z1)));
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X1, Y1, Z1));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(constructionNode2,
                                IQ_FACTORY.createUnaryIQTree(limitNode,
                                        IQ_FACTORY.createUnaryIQTree(constructionNode3, dataNode1)))));

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(X1),
                        Y, generateURI1(Y1),
                        Z, generateURI1(Z1)));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode4,
                        IQ_FACTORY.createUnaryIQTree(limitNode, dataNode1)));

        optimizeAndCompare(query1, query2);
    }


    @Test
    public void removeConstructionNodeTest7() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);
        SliceNode limit50 = IQ_FACTORY.createSliceNode(0,50);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1)));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1)));
        SliceNode limit40 = IQ_FACTORY.createSliceNode(0,40);
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X1, Y1, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateURI1(Z1)));
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X1, Y1, Z1));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(limit50,
                        IQ_FACTORY.createUnaryIQTree(constructionNode1,
                                IQ_FACTORY.createUnaryIQTree(constructionNode2,
                                        IQ_FACTORY.createUnaryIQTree(limit40,
                                                IQ_FACTORY.createUnaryIQTree(constructionNode3, dataNode1))))));

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(X1),
                        Y, generateURI1(Y1),
                        Z, generateURI1(Z1)));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode4,
                        IQ_FACTORY.createUnaryIQTree(limit40, dataNode1)));

        optimizeAndCompare(query1, query2);
    }

    @Test
    public void removeConstructionNodeTest8() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);
        SliceNode limit50 = IQ_FACTORY.createSliceNode(0,50);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1)));
        SliceNode limit80 = IQ_FACTORY.createSliceNode(0,80);
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1)));
        SliceNode limit40 = IQ_FACTORY.createSliceNode(0,40);
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X1, Y1, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateURI1(Z1)));
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X1, Y1, Z1));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(limit50,
                        IQ_FACTORY.createUnaryIQTree(constructionNode1,
                                IQ_FACTORY.createUnaryIQTree(limit80,
                                                IQ_FACTORY.createUnaryIQTree(constructionNode2,
                                                        IQ_FACTORY.createUnaryIQTree(limit40,
                                                                IQ_FACTORY.createUnaryIQTree(constructionNode3, dataNode1)))))));

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(X1),
                        Y, generateURI1(Y1),
                        Z, generateURI1(Z1)));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode4,
                        IQ_FACTORY.createUnaryIQTree(limit40, dataNode1)));

        optimizeAndCompare(query1, query2);
    }


    @Test
    public void removeConstructionNodeTest9() {
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

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(limit130,
                        IQ_FACTORY.createUnaryIQTree(constructionNode1,
                                IQ_FACTORY.createUnaryIQTree(limit100,
                                        IQ_FACTORY.createUnaryIQTree(constructionNode2,
                                                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                                        dataNode1,
                                                        IQ_FACTORY.createUnaryIQTree(distinctNode1,
                                                                IQ_FACTORY.createUnaryIQTree(constructionNode3,
                                                                        IQ_FACTORY.createUnaryIQTree(distinctNode2,
                                                                                IQ_FACTORY.createUnaryIQTree(constructionNode4, dataNode2)))))))))));

        ConstructionNode constructionNode5 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1)));

        ConstructionNode constructionNode6 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1), Z, generateURI1(Z1)));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode5,
                        IQ_FACTORY.createUnaryIQTree(limit100,
                                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                        dataNode1,
                                        IQ_FACTORY.createUnaryIQTree(constructionNode6, dataNode2))))));


        optimizeAndCompare(query1, query2);
    }

    @Test
    public void removeConstructionNodeTest10() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateCompositeURI2(Y, Z)));
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution());
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(Y, R1_IRI, Z));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode1)));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1, dataNode1));

        optimizeAndCompare(query1, query2);
    }

    @Test
    public void removeConstructionNodeTest11() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution());
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1), Y, generateURI1(Y1)));
        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X1, R1_IRI, Y1));

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode1)));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode1));
        optimizeAndCompare(query1, query2);
    }

    @Test
    public void removeConstructionNodeTest12() {
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X1);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X1));
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X1));
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X1));

        IntensionalDataNode dataNode1 = IQ_FACTORY.createIntensionalDataNode(ATOM_FACTORY.getIntensionalTripleAtom(X1, R1_IRI, X2));
        IntensionalDataNode dataNode2 = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X1, R2_IRI, X2));
        IntensionalDataNode dataNode3 = IQ_FACTORY.createIntensionalDataNode(
                ATOM_FACTORY.getIntensionalTripleAtom(X1, R3_IRI, X2));

        ConstructionNode constructionNodeX1 = IQ_FACTORY.createConstructionNode(unionNode1.getVariables());

        IQ query1 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createUnaryIQTree(constructionNode1,
                        IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                                IQ_FACTORY.createUnaryIQTree(constructionNodeX1, dataNode1),
                                IQ_FACTORY.createUnaryIQTree(constructionNode2, dataNode2),
                                IQ_FACTORY.createUnaryIQTree(constructionNode3, dataNode3)))));

        IQ query2 = IQ_FACTORY.createIQ(projectionAtom1,
                IQ_FACTORY.createNaryIQTree(unionNode1, ImmutableList.of(
                        IQ_FACTORY.createUnaryIQTree(constructionNodeX1, dataNode1),
                        IQ_FACTORY.createUnaryIQTree(constructionNodeX1, dataNode2),
                        IQ_FACTORY.createUnaryIQTree(constructionNodeX1, dataNode3))));

        optimizeAndCompare(query1, query2);
    }


    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }

    private ImmutableFunctionalTerm generateCompositeURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_2_2, ImmutableList.of(argument1, argument2));
    }

    private void optimizeAndCompare(IQ initialQuery, IQ expectedQuery) {
        System.out.println("\nBefore optimization: \n" + initialQuery);
        System.out.println("\nExpected: \n" + expectedQuery);
        IQ optimizedQuery = UNION_AND_BINDING_LIFT_OPTIMIZER.optimize(initialQuery)
                .normalizeForOptimization();
        System.out.println("\nAfter optimization: \n" + optimizedQuery);
        assertEquals(expectedQuery, optimizedQuery);
    }
}
