package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.node.impl.ImmutableQueryModifiersImpl;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.impl.URITemplatePredicateImpl;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.owlrefplatform.core.optimization.ConstructionNodeRemovalOptimizer;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;
import static junit.framework.TestCase.assertTrue;

public class ConstructionNodeRemovalOptimizerTest {

    private final static AtomPredicate TABLE1_PREDICATE = DATA_FACTORY.getAtomPredicate("table1", 2);
    private final static AtomPredicate TABLE2_PREDICATE = DATA_FACTORY.getAtomPredicate("table2", 2);
    private final static AtomPredicate TABLE3_PREDICATE = DATA_FACTORY.getAtomPredicate("table3", 2);
    private final static AtomPredicate ANS1_PREDICATE = DATA_FACTORY.getAtomPredicate("ans1", 1);
    private final static AtomPredicate ANS2_PREDICATE = DATA_FACTORY.getAtomPredicate("ans2", 2);
    private final static Variable X = DATA_FACTORY.getVariable("X");
    private final static Variable X1 = DATA_FACTORY.getVariable("X1");
    private final static Variable X2 = DATA_FACTORY.getVariable("X2");
    private final static Variable Y = DATA_FACTORY.getVariable("Y");
    private final static Variable Y1 = DATA_FACTORY.getVariable("Y1");
    private final static Variable Y2 = DATA_FACTORY.getVariable("Y2");
    private final static Variable Z = DATA_FACTORY.getVariable("Z");
    private final static Variable Z1 = DATA_FACTORY.getVariable("Z1");
    private final static Variable Z2 = DATA_FACTORY.getVariable("Z2");


    private URITemplatePredicate URI_PREDICATE =  new URITemplatePredicateImpl(2);
    private URITemplatePredicate URI_2PREDICATE =  new URITemplatePredicateImpl(3);

    private Constant URI_TEMPLATE_STR_1 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds1/{}");
    private Constant URI_TEMPLATE_STR_2 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}");
    private Constant URI_TEMPLATE_STR_2_2 =  DATA_FACTORY.getConstantLiteral("http://example.org/ds2/{}/{}");


    @Test
    public void removeConstructionNodeTest1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(X, Y));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, Y)
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y),
                DATA_FACTORY.getSubstitution(Y, Z)
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, Z, Z2));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, Z)
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest3() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, Y)
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y),
                DATA_FACTORY.getSubstitution(Y, generateURI1(Z))
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, Z, Z2));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, generateURI1(Z))
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, generateURI1(Y))
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y),
                DATA_FACTORY.getSubstitution(Y, Z)
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, Z, Z2));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, generateURI1(Z))
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest5() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, generateCompositeURI2(Y, Z))
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y, Z),
                DATA_FACTORY.getSubstitution(Y, Y1, Z, Z1)
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, Y1, Z1));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, generateCompositeURI2(Y1, Z1))
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest6() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE,
                X1, X2);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X1, Y1, X2, Y2)
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y1, Y2),
                DATA_FACTORY.getSubstitution(Y1, generateURI1(Z1), Y2, generateURI1(Z2))
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, Z1, Z2));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X1, generateURI1(Z1), X2, generateURI1(Z2))
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest7() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE,
                X1, X2);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X1, generateCompositeURI2(Y1, Y2), X2, Y2)
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y1, Y2),
                DATA_FACTORY.getSubstitution(Y1, generateURI1(Z1), Y2, Z2)
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, Z1, Z2));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();
        IntermediateQuery snapshot = query1.createSnapshot();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        System.out.println("\nExpected: \n" + snapshot);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, snapshot));
    }

    @Test
    public void removeConstructionNodeTest8() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables()
        );

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y),
                DATA_FACTORY.getSubstitution(),
                Optional.of(new ImmutableQueryModifiersImpl(true, 100, -1, ImmutableList.of()))
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery snapshot = query1.createSnapshot();

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        System.out.println("\nExpected: \n" + snapshot);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, snapshot));
    }

    @Test
    public void removeConstructionNodeTest9() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables()
        );

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y),
                DATA_FACTORY.getSubstitution(),
                Optional.of(new ImmutableQueryModifiersImpl(false, -1, -1, ImmutableList.of()))
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest10() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(),
                Optional.of(new ImmutableQueryModifiersImpl(true, -1, 100, ImmutableList.of()))
        );

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y)
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE1_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

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
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, X1)
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1),
                DATA_FACTORY.getSubstitution(X1, X2)
        );
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X2),
                DATA_FACTORY.getSubstitution(X2, Z1)
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, Z1, Z2));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, constructionNode3);
        queryBuilder1.addChild(constructionNode3, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, Z1)
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode4);
        queryBuilder2.addChild(constructionNode4, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest12() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE,
                X1, X2);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X1, generateCompositeURI2(Y1, Y2), X2, Y2)
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y1, Y2),
                DATA_FACTORY.getSubstitution(Y1, Z1, Y2, Z2)
        );
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Z1, Z2),
                DATA_FACTORY.getSubstitution(Z1, X, Z2, Y)
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, constructionNode3);
        queryBuilder1.addChild(constructionNode3, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X1, generateCompositeURI2(X, Y), X2, Y)
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode4);
        queryBuilder2.addChild(constructionNode4, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest13() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE,
                X1, X2);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X1, generateCompositeURI2(Y1, Y2), X2, Y2)
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y1, Y2),
                DATA_FACTORY.getSubstitution(Y1, Z1, Y2, Z2)
        );
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Z1, Z2),
                DATA_FACTORY.getSubstitution(Z1, generateURI1(X), Z2, generateURI2(Y))
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, X, Y));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, constructionNode3);
        queryBuilder1.addChild(constructionNode3, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X1, generateCompositeURI2(Z1, Z2), X2, Z2)
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode4);
        queryBuilder2.addChild(constructionNode4, constructionNode3);
        queryBuilder2.addChild(constructionNode3, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void removeConstructionNodeTest14() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, X1)
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1),
                DATA_FACTORY.getSubstitution(X1, X2)
        );
        ConstructionNode constructionNode3 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X2),
                DATA_FACTORY.getSubstitution(X2, Y)
        );
        ConstructionNode constructionNode4 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(Y),
                DATA_FACTORY.getSubstitution(Y, Y1)
        );

        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode();
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, Z1, X2));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE2_PREDICATE, Z2, Y1));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, innerJoinNode);
        queryBuilder1.addChild(innerJoinNode, dataNode1);
        queryBuilder1.addChild(innerJoinNode, constructionNode3);
        queryBuilder1.addChild(constructionNode3, constructionNode4);
        queryBuilder1.addChild(constructionNode4, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        ConstructionNode constructionNode5 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, X2)
        );
        ConstructionNode constructionNode6 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X2),
                DATA_FACTORY.getSubstitution(X2, Y1)
        );
        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode5);
        queryBuilder2.addChild(constructionNode5, innerJoinNode);
        queryBuilder2.addChild(innerJoinNode, dataNode1);
        queryBuilder2.addChild(innerJoinNode, constructionNode6);
        queryBuilder2.addChild(constructionNode6, dataNode2);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest15() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X1);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables()
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1)
        );
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X1));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, X1, X2));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE2_PREDICATE, X1, X2));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, unionNode1);
        queryBuilder2.addChild(unionNode1, dataNode1);
        queryBuilder2.addChild(unionNode1, dataNode2);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest16() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, X1);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables()
        );
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1),
                DATA_FACTORY.getSubstitution(X1, Y1)
        );
        UnionNode unionNode1 = IQ_FACTORY.createUnionNode(ImmutableSet.of(X1));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, X1, X2));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE2_PREDICATE, Y1, Y2));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, unionNode1);
        queryBuilder1.addChild(unionNode1, dataNode1);
        queryBuilder1.addChild(unionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode2);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        IntermediateQuery snapshot = query1.createSnapshot();

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        System.out.println("\nExpected: \n" + snapshot);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, snapshot));
    }

    @Test
    public void removeConstructionNodeTest17() throws EmptyQueryException {

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

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, unionNode1);
        queryBuilder2.addChild(unionNode1, dataNode1);
        queryBuilder2.addChild(unionNode1, dataNode2);
        queryBuilder2.addChild(unionNode1, dataNode3);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }


    @Test
    public void removeConstructionNodeTest18() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE,
                X, Y);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables());

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X, Y),
                DATA_FACTORY.getSubstitution(X, X1, Y, Y1)
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, X1, Y1));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode2);
        queryBuilder2.addChild(constructionNode2, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }

    @Test
    public void removeConstructionNodeTest19() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder1 = createQueryBuilder(EMPTY_METADATA);
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS2_PREDICATE,
                X, Y);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom1.getVariables(),
                DATA_FACTORY.getSubstitution(X, X1, Y, Y1)
        );

        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(
                ImmutableSet.of(X1, Y1)
        );

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom
                (TABLE1_PREDICATE, X1, Y1));

        queryBuilder1.init(projectionAtom1, constructionNode1);
        queryBuilder1.addChild(constructionNode1, constructionNode2);
        queryBuilder1.addChild(constructionNode2, dataNode1);

        IntermediateQuery query1 = queryBuilder1.build();

        System.out.println("\nBefore optimization: \n" + query1);

        ConstructionNodeRemovalOptimizer constructionNodeRemovalOptimizer = new ConstructionNodeRemovalOptimizer();
        IntermediateQuery optimizedQuery = constructionNodeRemovalOptimizer.optimize(query1);

        System.out.println("\nAfter optimization: \n" + optimizedQuery);

        IntermediateQueryBuilder queryBuilder2 = createQueryBuilder(EMPTY_METADATA);

        queryBuilder2.init(projectionAtom1, constructionNode1);
        queryBuilder2.addChild(constructionNode1, dataNode1);

        IntermediateQuery query2 = queryBuilder2.build();
        System.out.println("\nExpected: \n" + query2);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query2));
    }
    private ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_1, argument);
    }

    private ImmutableFunctionalTerm generateURI2(VariableOrGroundTerm argument) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_PREDICATE, URI_TEMPLATE_STR_2, argument);
    }

    private ImmutableFunctionalTerm generateCompositeURI2(ImmutableTerm argument1, ImmutableTerm argument2) {
        return DATA_FACTORY.getImmutableFunctionalTerm(URI_2PREDICATE, URI_TEMPLATE_STR_2_2, argument1, argument2);
    }
}
