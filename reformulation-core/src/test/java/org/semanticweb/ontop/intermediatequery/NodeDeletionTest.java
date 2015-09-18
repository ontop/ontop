package org.semanticweb.ontop.intermediatequery;

import com.google.common.base.Optional;
import org.junit.Test;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.AtomPredicateImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.owlrefplatform.core.optimization.BasicJoinOptimizer;
import org.semanticweb.ontop.pivotalrepr.EmptyQueryException;
import org.semanticweb.ontop.owlrefplatform.core.optimization.IntermediateQueryOptimizer;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.*;
import org.semanticweb.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition.LEFT;
import static org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition.RIGHT;

/**
 * TODO: test
 */
public class NodeDeletionTest {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private static final MetadataForQueryOptimization METADATA = new EmptyMetadataForQueryOptimization();


    @Test(expected = EmptyQueryException.class)
    public void testSimpleJoin() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        ConstructionNode rootNode = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ans1", 1), x));

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        queryBuilder.init(rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableBooleanExpression falseCondition = DATA_FACTORY.getImmutableBooleanExpression(OBDAVocabulary.AND, falseValue, falseValue);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(falseCondition));
        queryBuilder.addChild(rootNode, joinNode);

        TableNode table1 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table1", 1), x));
        queryBuilder.addChild(joinNode, table1);

        TableNode table2 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table2", 1), x));
        queryBuilder.addChild(joinNode, table2);

        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        IntermediateQueryOptimizer joinOptimizer = new BasicJoinOptimizer();

        /**
         * Should throw the EmptyQueryException
         */
        IntermediateQuery optimizedQuery = joinOptimizer.optimize(initialQuery);
        System.err.println("Optimized query (should have been rejected): " + optimizedQuery.toString());
    }

    @Test
    public void testInvalidRightPartOfLeftJoin1() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        Variable y = DATA_FACTORY.getVariable("y");

        ConstructionNode rootNode = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(
                new AtomPredicateImpl("ans1", 2), x, y));

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        queryBuilder.init(rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableBooleanExpression falseCondition = DATA_FACTORY.getImmutableBooleanExpression(OBDAVocabulary.AND, falseValue, falseValue);

        LeftJoinNode ljNode = new LeftJoinNodeImpl(Optional.<ImmutableBooleanExpression>absent());
        queryBuilder.addChild(rootNode, ljNode);

        String table1Name = "table1";
        TableNode table1 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl(table1Name, 1), x));
        queryBuilder.addChild(ljNode, table1, LEFT);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(falseCondition));
        queryBuilder.addChild(ljNode, joinNode, RIGHT);

        TableNode table2 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table2", 2), x, y));
        queryBuilder.addChild(joinNode, table2);

        TableNode table3 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table3", 2), x, y));
        queryBuilder.addChild(joinNode, table3);

        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        IntermediateQueryOptimizer joinOptimizer = new BasicJoinOptimizer();

        /**
         * Should replace the left join node by table 1.
         */
        IntermediateQuery optimizedQuery = joinOptimizer.optimize(initialQuery);
        System.out.println("Optimized query : " + optimizedQuery.toString());

        QueryNode viceRootNode = optimizedQuery.getFirstChild(optimizedQuery.getRootConstructionNode()).get();
        assertTrue(viceRootNode instanceof TableNode);
        assertEquals(((TableNode) viceRootNode).getAtom().getPredicate().getName(), table1Name);
        assertTrue(optimizedQuery.getChildren(viceRootNode).isEmpty());
    }

    @Test
    public void testUnion1() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        Variable y = DATA_FACTORY.getVariable("y");

        ConstructionNode rootNode = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(
                new AtomPredicateImpl("ans1", 2), x, y));

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        queryBuilder.init(rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableBooleanExpression falseCondition = DATA_FACTORY.getImmutableBooleanExpression(OBDAVocabulary.AND, falseValue, falseValue);

        UnionNode topUnion = new UnionNodeImpl();
        queryBuilder.addChild(rootNode, topUnion);

        DataAtom subAtom = DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ansu1", 2), x, y);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(subAtom);
        queryBuilder.addChild(topUnion, constructionNode1);

        String table1Name = "table1";
        TableNode table1 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl(table1Name, 2), x, y));
        queryBuilder.addChild(constructionNode1, table1);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(subAtom);
        queryBuilder.addChild(topUnion, constructionNode2);

        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.of(falseCondition));
        queryBuilder.addChild(constructionNode2, joinNode1);

        TableNode table2 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table2", 2), x, y));
        queryBuilder.addChild(joinNode1, table2);

        TableNode table3 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table3", 2), x, y));
        queryBuilder.addChild(joinNode1, table3);

        ConstructionNode constructionNode3 = new ConstructionNodeImpl(subAtom);
        queryBuilder.addChild(topUnion, constructionNode3);

        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.of(falseCondition));
        queryBuilder.addChild(constructionNode3, joinNode2);

        TableNode table4 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table4", 2), x, y));
        queryBuilder.addChild(joinNode2, table4);

        TableNode table5 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table5", 2), x, y));
        queryBuilder.addChild(joinNode2, table5);

        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        IntermediateQueryOptimizer joinOptimizer = new BasicJoinOptimizer();

        /**
         * Should replace the left join node by table 1.
         */
        IntermediateQuery optimizedQuery = joinOptimizer.optimize(initialQuery);
        System.out.println("Optimized query : " + optimizedQuery.toString());

        QueryNode viceRootNode = optimizedQuery.getFirstChild(optimizedQuery.getRootConstructionNode()).get();
        assertTrue(viceRootNode instanceof ConstructionNode);
        assertEquals(optimizedQuery.getChildren(viceRootNode).size(), 1);

        QueryNode viceViceRootNode = optimizedQuery.getFirstChild(viceRootNode).get();
        assertTrue(viceViceRootNode instanceof TableNode);
        assertEquals(((TableNode) viceViceRootNode).getAtom().getPredicate().getName(), table1Name);
        assertTrue(optimizedQuery.getChildren(viceViceRootNode).isEmpty());
    }

    @Test
    public void testUnion2() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        Variable y = DATA_FACTORY.getVariable("y");

        ConstructionNode rootNode = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(
                new AtomPredicateImpl("ans1", 2), x, y));

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        queryBuilder.init(rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableBooleanExpression falseCondition = DATA_FACTORY.getImmutableBooleanExpression(OBDAVocabulary.AND, falseValue, falseValue);

        UnionNode topUnion = new UnionNodeImpl();
        queryBuilder.addChild(rootNode, topUnion);

        DataAtom subAtom = DATA_FACTORY.getDataAtom(new AtomPredicateImpl("ansu1", 2), x, y);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(subAtom);
        queryBuilder.addChild(topUnion, constructionNode1);

        String table1Name = "table1";
        TableNode table1 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl(table1Name, 2), x, y));
        queryBuilder.addChild(constructionNode1, table1);

        ConstructionNode constructionNode2 = new ConstructionNodeImpl(subAtom);
        queryBuilder.addChild(topUnion, constructionNode2);

        InnerJoinNode joinNode1 = new InnerJoinNodeImpl(Optional.of(falseCondition));
        queryBuilder.addChild(constructionNode2, joinNode1);

        TableNode table2 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table2", 2), x, y));
        queryBuilder.addChild(joinNode1, table2);

        TableNode table3 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table3", 2), x, y));
        queryBuilder.addChild(joinNode1, table3);

        ConstructionNode constructionNode3 = new ConstructionNodeImpl(subAtom);
        queryBuilder.addChild(topUnion, constructionNode3);

        InnerJoinNode joinNode2 = new InnerJoinNodeImpl(Optional.<ImmutableBooleanExpression>absent());
        queryBuilder.addChild(constructionNode3, joinNode2);

        TableNode table4 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table4", 2), x, y));
        queryBuilder.addChild(joinNode2, table4);

        TableNode table5 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table5", 2), x, y));
        queryBuilder.addChild(joinNode2, table5);

        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        IntermediateQueryOptimizer joinOptimizer = new BasicJoinOptimizer();

        /**
         * Should replace the left join node by table 1.
         */
        IntermediateQuery optimizedQuery = joinOptimizer.optimize(initialQuery);
        System.out.println("Optimized query : " + optimizedQuery.toString());

        QueryNode viceRootNode = optimizedQuery.getFirstChild(optimizedQuery.getRootConstructionNode()).get();
        assertTrue(viceRootNode instanceof UnionNode);
        assertEquals(optimizedQuery.getChildren(viceRootNode).size(), 2);
    }

    @Test(expected = EmptyQueryException.class)
    public void testInvalidLeftPartOfLeftJoin() throws IntermediateQueryBuilderException, EmptyQueryException {
        Variable x = DATA_FACTORY.getVariable("x");
        Variable y = DATA_FACTORY.getVariable("y");

        ConstructionNode rootNode = new ConstructionNodeImpl(DATA_FACTORY.getDataAtom(
                new AtomPredicateImpl("ans1", 2), x, y));

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
        queryBuilder.init(rootNode);

        ValueConstant falseValue = DATA_FACTORY.getBooleanConstant(false);
        ImmutableBooleanExpression falseCondition = DATA_FACTORY.getImmutableBooleanExpression(OBDAVocabulary.AND, falseValue, falseValue);

        LeftJoinNode ljNode = new LeftJoinNodeImpl(Optional.<ImmutableBooleanExpression>absent());
        queryBuilder.addChild(rootNode, ljNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.of(falseCondition));
        queryBuilder.addChild(ljNode, joinNode, LEFT);

        TableNode table2 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table2", 2), x, y));
        queryBuilder.addChild(joinNode, table2);

        TableNode table3 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table3", 2), x, y));
        queryBuilder.addChild(joinNode, table3);

        TableNode table4 = new TableNodeImpl(DATA_FACTORY.getDataAtom(new AtomPredicateImpl("table4", 1), x));
        queryBuilder.addChild(ljNode, table4, RIGHT);


        IntermediateQuery initialQuery = queryBuilder.build();
        System.out.println("Initial query: " + initialQuery.toString());

        IntermediateQueryOptimizer joinOptimizer = new BasicJoinOptimizer();

        /**
         * Should throw the EmptyQueryException
         */
        IntermediateQuery optimizedQuery = joinOptimizer.optimize(initialQuery);
        System.err.println("Optimized query (should have been rejected): " + optimizedQuery.toString());
    }
}
