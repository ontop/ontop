package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.node.impl.ImmutableQueryModifiersImpl;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.iq.optimizer.ConstructionNodeCleaner;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.SUBSTITUTION_FACTORY;
import static junit.framework.TestCase.assertTrue;

public class ConstructionNodeCleanerTest {

    private static final ConstructionNodeCleaner constructionNodeCleaner = new ConstructionNodeCleaner();

    private final static AtomPredicate TABLE1_PREDICATE = ATOM_FACTORY.getAtomPredicate("table1", 2);
    private final static AtomPredicate TABLE2_PREDICATE = ATOM_FACTORY.getAtomPredicate("table2", 2);
    private final static AtomPredicate TABLE3_PREDICATE = ATOM_FACTORY.getAtomPredicate("table3", 2);
    private final static AtomPredicate TABLE4_PREDICATE = ATOM_FACTORY.getAtomPredicate("table4", 3);
    private final static AtomPredicate TABLE5_PREDICATE = ATOM_FACTORY.getAtomPredicate("table5", 3);
    private final static AtomPredicate ANS1_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 1);
    private final static AtomPredicate ANS2_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans2", 2);
    private final static AtomPredicate ANS3_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans3", 3);
    private final static Variable X = TERM_FACTORY.getVariable("X");
    private final static Variable X1 = TERM_FACTORY.getVariable("X1");
    private final static Variable X2 = TERM_FACTORY.getVariable("X2");
    private final static Variable Y = TERM_FACTORY.getVariable("Y");
    private final static Variable Y1 = TERM_FACTORY.getVariable("Y1");
    private final static Variable Y2 = TERM_FACTORY.getVariable("Y2");
    private final static Variable Z = TERM_FACTORY.getVariable("Z");
    private final static Variable Z1 = TERM_FACTORY.getVariable("Z1");
    private final static Variable Z2 = TERM_FACTORY.getVariable("Z2");


    private URITemplatePredicate URI_PREDICATE = new URITemplatePredicateImpl(2);
    private URITemplatePredicate URI_2PREDICATE = new URITemplatePredicateImpl(3);

    private Constant URI_TEMPLATE_STR_1 = TERM_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private Constant URI_TEMPLATE_STR_2_2 = TERM_FACTORY.getConstantLiteral("http://example.org/ds2/{}/{}");


    @Test
    public void removeConstructionNodeTest1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(),
                Optional.of(new ImmutableQueryModifiersImpl(true, 100, -1, ImmutableList.of()))
        );

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query1);
        optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution()
        );

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(),
                Optional.of(new ImmutableQueryModifiersImpl(true, 100, -1, ImmutableList.of()))
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(),
                Optional.of(new ImmutableQueryModifiersImpl(true, 100, -1, ImmutableList.of()))
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query1);
        optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void removeConstructionNodeTest3() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, -1, ImmutableList.of()))

        );

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y),
                SUBSTITUTION_FACTORY.getSubstitution(),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, -1, ImmutableList.of()))
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution()
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query1);
        optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(Z2)),
                Optional.of(new ImmutableQueryModifiersImpl(false, 100, -1, ImmutableList.of()))
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y, Z2),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Z)),
                Optional.of(new ImmutableQueryModifiersImpl(false, 50, -1, ImmutableList.of()))
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, Z, Z2));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X,generateURI1(Z2), Y, generateURI1(Z)),
                Optional.of(new ImmutableQueryModifiersImpl(false, 50, -1, ImmutableList.of()))
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query1);
        optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);


        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void removeConstructionNodeTest5() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE, X, Y);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(Z2)),
                Optional.of(new ImmutableQueryModifiersImpl(false, 100, -1, ImmutableList.of()))
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y, Z2),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Z)),
                Optional.of(new ImmutableQueryModifiersImpl(true, 50, -1, ImmutableList.of()))
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, Z, Z2));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(Z2), Y, generateURI1(Z)),
                Optional.of(new ImmutableQueryModifiersImpl(false, 100, -1, ImmutableList.of()))
        );
        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Z, Z2),
                SUBSTITUTION_FACTORY.getSubstitution(),
                Optional.of(new ImmutableQueryModifiersImpl(true, 50, -1, ImmutableList.of()))
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, constructionNode4);
        queryBuilder2.addChild(constructionNode4, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query1);
        optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);


        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void removeConstructionNodeTest6() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1))
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1)),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, -1, ImmutableList.of()))

        );
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y1, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateURI1(Z1)),
                Optional.of(new ImmutableQueryModifiersImpl(false, 100, -1, ImmutableList.of()))
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE4_PREDICATE, X1, Y1, Z1));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, constructionNode3);
        queryBuilder1.addChild(constructionNode3, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(X1),
                        Y, generateURI1(Y1),
                        Z, generateURI1(Z1)
                ),
                Optional.of(new ImmutableQueryModifiersImpl(false, 100, -1, ImmutableList.of()))
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode4);
        queryBuilder2.addChild(constructionNode4, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query1);
        optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void removeConstructionNodeTest7() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1)),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, 50, ImmutableList.of()))
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1)),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, -1, ImmutableList.of()))

        );
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y1, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateURI1(Z1)),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, 40, ImmutableList.of()))
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE4_PREDICATE, X1, Y1, Z1));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, constructionNode3);
        queryBuilder1.addChild(constructionNode3, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(X1),
                        Y, generateURI1(Y1),
                        Z, generateURI1(Z1)
                ),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, 90, ImmutableList.of()))
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode4);
        queryBuilder2.addChild(constructionNode4, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);


        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query1);
        optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);


        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest8() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1)),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, 50, ImmutableList.of()))
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1)),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, 80, ImmutableList.of()))

        );
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y1, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateURI1(Z1)),
                Optional.of(new ImmutableQueryModifiersImpl(true, -1, 40, ImmutableList.of()))
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE4_PREDICATE, X1, Y1, Z1));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, constructionNode3);
        queryBuilder1.addChild(constructionNode3, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(X1),
                        Y, generateURI1(Y1),
                        Z, generateURI1(Z1)
                ),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, 130, ImmutableList.of()))
        );
        ConstructionNode constructionNode5 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y1, Z1),
                SUBSTITUTION_FACTORY.getSubstitution(),
                Optional.of(new ImmutableQueryModifiersImpl(true, -1, 40, ImmutableList.of()))
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode4);
        queryBuilder2.addChild(constructionNode4, constructionNode5);
        queryBuilder2.addChild(constructionNode5, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query1);
        optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void removeConstructionNodeTest9() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, 130, ImmutableList.of()))
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1)),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, 100, ImmutableList.of()))
        );
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1)),
                Optional.of(new ImmutableQueryModifiersImpl(true, -1, -1, ImmutableList.of()))
        );
        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y1, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Z, generateURI1(Z1)),
                Optional.of(new ImmutableQueryModifiersImpl(true, -1, -1, ImmutableList.of()))
        );
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X1, Y, Z));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE4_PREDICATE, X1, Y, Z));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE5_PREDICATE, X1, Y1, Z1));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, constructionNode3);
        queryBuilder1.addChild(constructionNode3, constructionNode4);
        queryBuilder1.addChild(constructionNode4, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNode constructionNode5 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(X1)),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, 230, ImmutableList.of()))
        );
        ConstructionNode constructionNode6 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution(Y, generateURI1(Y1), Z, generateURI1(Z1)),
                Optional.of(new ImmutableQueryModifiersImpl(true, -1, -1, ImmutableList.of()))
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode5);
        queryBuilder2.addChild(constructionNode5, unionNode1);
        queryBuilder2.addChild(unionNode1, dataNode1);
        queryBuilder2.addChild(unionNode1, constructionNode6);
        queryBuilder2.addChild(constructionNode6, dataNode2);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query1);
        optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest10() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateCompositeURI2(Y, Z))
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y, Z),
                SUBSTITUTION_FACTORY.getSubstitution()
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, Y, Z));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);


        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query1);
        optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest11() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
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

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, X1, Y1));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode2);
        queryBuilder2.addChild(constructionNode2, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query1);
        optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest12() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
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

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, X1, X2));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE2_PREDICATE, X1, X2));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE3_PREDICATE, X1, X2));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, constructionNode2);
        queryBuilder1.addChild(unionNode1, constructionNode3);
        queryBuilder1.addChild(constructionNode2, dataNode2);
        queryBuilder1.addChild(constructionNode3, dataNode3);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, unionNode1);
        queryBuilder2.addChild(unionNode1, dataNode1);
        queryBuilder2.addChild(unionNode1, dataNode2);
        queryBuilder2.addChild(unionNode1, dataNode3);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        IntermediateQuery optimizedQuery = BINDING_LIFT_OPTIMIZER.optimize(query1);
        optimizedQuery = constructionNodeCleaner.optimize(optimizedQuery);
        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

    private ImmutableFunctionalTerm generateCompositeURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return TERM_FACTORY.getImmutableFunctionalTerm(URI_2PREDICATE, URI_TEMPLATE_STR_2_2, argument1, argument2);
    }
}
